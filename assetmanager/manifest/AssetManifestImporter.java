package assetmanager.manifest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import assetmanager.catalog.AssetKey;
import assetmanager.catalog.EntityAssetBundle;
import assetmanager.font.FontStyle;
import assetmanager.mesh.MeshPreparationOptions;
import assetmanager.opengl.OpenGLDraftLibrary;
import assetmanager.opengl.OpenGLFontDraft;
import assetmanager.opengl.OpenGLPaletteDraft;
import assetmanager.opengl.OpenGLUiElementDraft;
import assetmanager.opengl.OpenGLUiModuleDraft;
import assetmanager.source.EntityImageSequence;
import assetmanager.source.ImageFrame;
import assetmanager.source.PathImageAssetSource;
import assetmanager.ui.UiElementType;
import physics.importing.RasterShapeOptions;
import physics.palette.PaletteSet;
import physics.palette.PaletteSwatch;
import physics.palette.PhysicsColor;

public final class AssetManifestImporter {

    public ImportResult importPath(Path manifestPath) {
        AssetManifestLoader loader = new AssetManifestLoader();
        AssetManifest manifest = loader.load(manifestPath);
        new AssetManifestValidator().validateOrThrow(manifest);
        return importManifest(manifest);
    }

    public AuthoringImportAttempt importAuthoringPath(Path authoringPath) {
        AssetAuthoringManifestAdapter adapter = new AssetAuthoringManifestAdapter();
        AssetAuthoringManifestAdapter.AuthoringLoadResult loadResult = adapter.loadWithContext(authoringPath);
        AssetManifest manifest = loadResult.manifest();

        AssetManifestValidator validator = new AssetManifestValidator("shardharbor", loadResult.supportedClipNames());
        List<String> diagnostics = new ArrayList<>(validator.validate(manifest));
        ImportResult imported = importManifest(manifest);

        Set<String> importableKeys = collectImportableEntityKeys(manifest);
        ArrayList<EntityAssetBundle> usableBundles = new ArrayList<>();
        for (EntityAssetBundle bundle : imported.entityBundles()) {
            if (importableKeys.contains(bundle.getKey().asString())) {
                usableBundles.add(bundle);
            }
        }
        if (usableBundles.size() != imported.entityBundles().size()) {
            diagnostics.add(
                "Authoring manifest loaded "
                    + usableBundles.size()
                    + " of "
                    + imported.entityBundles().size()
                    + " entity bundles with file-backed frames."
            );
        }

        return new AuthoringImportAttempt(
            new ImportResult(
                List.copyOf(usableBundles),
                imported.draftLibraries(),
                imported.themeLibrariesByName(),
                imported.entityMetadata(),
                imported.entityAnchors(),
                imported.libraryMetadata(),
                imported.uiModuleMetadata(),
                imported.manifestMetadata()
            ),
            List.copyOf(diagnostics),
            diagnostics.isEmpty() && usableBundles.size() == imported.entityBundles().size()
        );
    }

    public ImportResult importManifest(AssetManifest manifest) {
        if (manifest == null) {
            throw new IllegalArgumentException("An asset manifest is required.");
        }

        ArrayList<EntityAssetBundle> entityBundles = new ArrayList<>();
        ArrayList<OpenGLDraftLibrary> draftLibraries = new ArrayList<>();
        LinkedHashMap<String, AssetKey> themeLibraries = new LinkedHashMap<>();
        LinkedHashMap<AssetKey, Map<String, Object>> entityMetadata = new LinkedHashMap<>();
        LinkedHashMap<AssetKey, List<ImportedAnchor>> entityAnchors = new LinkedHashMap<>();
        LinkedHashMap<AssetKey, Map<String, Object>> libraryMetadata = new LinkedHashMap<>();
        LinkedHashMap<AssetKey, Map<String, Object>> uiModuleMetadata = new LinkedHashMap<>();

        for (EntityManifestEntry entity : manifest.entities()) {
            AssetKey assetKey = AssetKey.parse(entity.assetKey());
            EntityImageSequence sequence = new EntityImageSequence(assetKey);
            for (FrameManifestEntry frame : effectiveFrames(entity)) {
                Path file = resolveFile(manifest.sourcePath(), frame.file());
                sequence.addFrame(new ImageFrame(
                    frame.id(),
                    frame.clipName(),
                    frame.orderIndex(),
                    frame.durationMillis(),
                    new PathImageAssetSource(file, frame.file())
                ));
            }
            MeshOptionsManifest meshOptions = entity.meshOptions() == null ? MeshOptionsManifest.defaults() : entity.meshOptions();
            EntityAssetBundle bundle = new EntityAssetBundle(
                assetKey,
                sequence,
                new MeshPreparationOptions(
                    new RasterShapeOptions(
                        meshOptions.alphaThreshold(),
                        meshOptions.maxContourPoints(),
                        meshOptions.contourSamples(),
                        meshOptions.maxPaletteColors()
                    ),
                    meshOptions.normalizeToCenter()
                )
            );
            entityBundles.add(bundle);
            if (!entity.metadata().isEmpty()) {
                entityMetadata.put(assetKey, entity.metadata());
            }
            if (!entity.anchors().isEmpty()) {
                ArrayList<ImportedAnchor> anchors = new ArrayList<>();
                for (AnchorManifestEntry anchor : entity.anchors()) {
                    anchors.add(new ImportedAnchor(anchor.name(), anchor.x(), anchor.y()));
                }
                entityAnchors.put(assetKey, List.copyOf(anchors));
            }
        }

        for (ThemeManifestEntry theme : manifest.themes()) {
            AssetKey libraryKey = theme.libraryKey().isBlank()
                ? AssetKey.of("shardharbor", "drafts-" + normalize(theme.themeId()))
                : AssetKey.parse(theme.libraryKey());
            AssetKey paletteKey = theme.paletteKey().isBlank()
                ? AssetKey.of(libraryKey.getNamespace(), libraryKey.getName() + "-palette")
                : AssetKey.parse(theme.paletteKey());
            AssetKey fontKey = theme.fontKey().isBlank()
                ? AssetKey.of(libraryKey.getNamespace(), libraryKey.getName() + "-font")
                : AssetKey.parse(theme.fontKey());
            AssetKey materialKey = theme.materialKey().isBlank() ? null : AssetKey.parse(theme.materialKey());

            OpenGLDraftLibrary library = new OpenGLDraftLibrary(libraryKey);
            if (!theme.paletteRoles().isEmpty()) {
                library.registerPalette(new OpenGLPaletteDraft(paletteKey, new PaletteSet(toSwatches(theme.paletteRoles()))));
            }
            TypographyManifestEntry typography = theme.typography();
            if (typography != null) {
                library.registerFont(new OpenGLFontDraft(
                    fontKey,
                    typography.family(),
                    parseFontStyle(typography.style()),
                    typography.pointSize(),
                    typography.lineHeight(),
                    pickFontColor(theme.paletteRoles(), typography.colorRole()),
                    typography.antialias(),
                    typography.glyphSet()
                ));
            }
            for (UiModuleManifestEntry module : theme.uiModules()) {
                AssetKey moduleKey = module.key().isBlank()
                    ? AssetKey.of(libraryKey.getNamespace(), normalize(module.moduleType()), libraryKey.getName())
                    : AssetKey.parse(module.key());
                ArrayList<OpenGLUiElementDraft> elements = new ArrayList<>();
                for (UiElementManifestEntry element : module.elements()) {
                    elements.add(new OpenGLUiElementDraft(
                        element.elementId(),
                        parseElementType(element.type()),
                        element.x(),
                        element.y(),
                        element.width(),
                        element.height(),
                        element.paletteRole(),
                        element.materialKey().isBlank() ? materialKey : AssetKey.parse(element.materialKey()),
                        element.text()
                    ));
                }
                library.registerUiModule(new OpenGLUiModuleDraft(
                    moduleKey,
                    module.moduleType(),
                    module.designWidth(),
                    module.designHeight(),
                    paletteKey,
                    fontKey,
                    elements
                ));
                if (!module.metadata().isEmpty()) {
                    uiModuleMetadata.put(moduleKey, module.metadata());
                }
            }

            draftLibraries.add(library);
            if (!theme.themeId().isBlank()) {
                themeLibraries.put(theme.themeId().trim(), libraryKey);
            }
            if (!theme.metadata().isEmpty()) {
                libraryMetadata.put(libraryKey, theme.metadata());
            }
        }

        return new ImportResult(
            List.copyOf(entityBundles),
            List.copyOf(draftLibraries),
            Map.copyOf(themeLibraries),
            Map.copyOf(entityMetadata),
            Map.copyOf(entityAnchors),
            Map.copyOf(libraryMetadata),
            Map.copyOf(uiModuleMetadata),
            manifest.metadata()
        );
    }

    private static List<FrameManifestEntry> effectiveFrames(EntityManifestEntry entity) {
        if (!entity.frames().isEmpty()) {
            return entity.frames();
        }

        String clip = entity.clipNames().isEmpty() ? "idle" : entity.clipNames().get(0);
        ArrayList<FrameManifestEntry> frames = new ArrayList<>();
        for (int index = 0; index < entity.files().size(); index++) {
            String file = entity.files().get(index);
            frames.add(new FrameManifestEntry(
                AssetKey.parse(entity.assetKey()).getName() + "-" + clip + "-" + index,
                clip,
                index,
                140L,
                file
            ));
        }
        return List.copyOf(frames);
    }

    private static List<PaletteSwatch> toSwatches(List<PaletteRoleManifestEntry> paletteRoles) {
        ArrayList<PaletteSwatch> swatches = new ArrayList<>();
        for (PaletteRoleManifestEntry role : paletteRoles) {
            swatches.add(new PaletteSwatch(role.role(), toColor(role.color()), role.weight()));
        }
        return swatches;
    }

    private static PhysicsColor pickFontColor(List<PaletteRoleManifestEntry> paletteRoles, String colorRole) {
        String requestedRole = colorRole == null || colorRole.isBlank() ? "text" : colorRole.trim();
        for (PaletteRoleManifestEntry role : paletteRoles) {
            if (requestedRole.equalsIgnoreCase(role.role())) {
                return toColor(role.color());
            }
        }
        return paletteRoles.isEmpty() ? new PhysicsColor(255, 255, 255, 255) : toColor(paletteRoles.get(0).color());
    }

    private static PhysicsColor toColor(ManifestColor color) {
        return new PhysicsColor(color.red(), color.green(), color.blue(), color.alpha());
    }

    private static FontStyle parseFontStyle(String value) {
        if (value == null || value.isBlank()) {
            return FontStyle.REGULAR;
        }
        String normalized = normalize(value);
        try {
            return FontStyle.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            return FontStyle.REGULAR;
        }
    }

    private static UiElementType parseElementType(String value) {
        if (value == null || value.isBlank()) {
            return UiElementType.PANEL;
        }
        try {
            return UiElementType.valueOf(normalize(value));
        } catch (IllegalArgumentException exception) {
            return UiElementType.PANEL;
        }
    }

    private static String normalize(String value) {
        return value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
    }

    private static Path resolveFile(Path sourcePath, String file) {
        Path path = Path.of(file);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        Path manifestDirectory = sourcePath == null ? Path.of(".") : sourcePath.toAbsolutePath().getParent();
        return manifestDirectory.resolve(path).normalize();
    }

    private static Set<String> collectImportableEntityKeys(AssetManifest manifest) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (EntityManifestEntry entity : manifest.entities()) {
            if (isEntityFileBacked(entity, manifest.sourcePath())) {
                keys.add(AssetKey.parse(entity.assetKey()).asString());
            }
        }
        return Set.copyOf(keys);
    }

    private static boolean isEntityFileBacked(EntityManifestEntry entity, Path sourcePath) {
        if (entity.files().isEmpty()) {
            return false;
        }
        for (String file : entity.files()) {
            Path resolved = resolveFile(sourcePath, file);
            if (!Files.exists(resolved)) {
                return false;
            }
            if (!matchesCanvas(resolved, entity.canvasSize())) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesCanvas(Path file, CanvasManifest expected) {
        try {
            BufferedImage image = ImageIO.read(file.toFile());
            if (image == null) {
                return false;
            }
            return expected.width() <= 0
                || expected.height() <= 0
                || (image.getWidth() == expected.width() && image.getHeight() == expected.height());
        } catch (IOException exception) {
            return false;
        }
    }

    public static record ImportedAnchor(String name, double x, double y) {
    }

    public static record AuthoringImportAttempt(ImportResult importResult, List<String> diagnostics, boolean fullyImported) {
    }

    public static record ImportResult(
        List<EntityAssetBundle> entityBundles,
        List<OpenGLDraftLibrary> draftLibraries,
        Map<String, AssetKey> themeLibrariesByName,
        Map<AssetKey, Map<String, Object>> entityMetadata,
        Map<AssetKey, List<ImportedAnchor>> entityAnchors,
        Map<AssetKey, Map<String, Object>> libraryMetadata,
        Map<AssetKey, Map<String, Object>> uiModuleMetadata,
        Map<String, Object> manifestMetadata
    ) {
    }
}
