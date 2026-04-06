package assetmanager.manifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import assetmanager.catalog.AssetKey;

public final class AssetAuthoringManifestAdapter {

    private static final Set<String> DEFAULT_SUPPORTED_CLIPS = Set.of("default", "idle", "damaged", "burning", "wreck");
    private static final String DEFAULT_GLYPHS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789:-/+.%()[]<> ";
    private static final Pattern FRAME_FILE_PATTERN = Pattern.compile("([A-Za-z0-9\\-]+)_(\\d+)\\.[A-Za-z0-9]+$");

    public AssetManifest load(Path authoringPath) {
        return loadWithContext(authoringPath).manifest();
    }

    AuthoringLoadResult loadWithContext(Path authoringPath) {
        if (authoringPath == null) {
            throw new IllegalArgumentException("An authoring manifest path is required.");
        }
        try {
            Object rootValue = new ManifestJsonParser(Files.readString(authoringPath)).parse();
            Map<String, Object> root = requireObject(rootValue, "authoring manifest root");
            Set<String> supportedClipNames = decodeSupportedClipNames(root);
            AssetManifest manifest = new AssetManifest(
                decodeEntities(root, authoringPath, supportedClipNames),
                decodeThemes(root),
                decodeRootMetadata(root, authoringPath),
                authoringPath
            );
            return new AuthoringLoadResult(manifest, supportedClipNames);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read authoring asset manifest: " + authoringPath, exception);
        }
    }

    private static Set<String> decodeSupportedClipNames(Map<String, Object> root) {
        LinkedHashSet<String> clipNames = new LinkedHashSet<>(DEFAULT_SUPPORTED_CLIPS);
        Map<String, Object> conventions = optionalObject(root.get("conventions"));
        if (conventions != null) {
            clipNames.addAll(stringList(conventions.get("clip_names")));
        }
        for (Object rawEntity : array(root.get("entities"))) {
            Map<String, Object> entity = optionalObject(rawEntity);
            if (entity == null) {
                continue;
            }
            Map<String, Object> clips = optionalObject(entity.get("clips"));
            if (clips != null) {
                clipNames.addAll(clips.keySet());
            }
        }
        return Set.copyOf(clipNames);
    }

    private static List<EntityManifestEntry> decodeEntities(Map<String, Object> root, Path authoringPath, Set<String> supportedClipNames) {
        ArrayList<EntityManifestEntry> entries = new ArrayList<>();
        Map<String, Object> conventions = optionalObject(root.get("conventions"));
        List<String> preferredClipOrder = conventions == null ? List.of() : stringList(conventions.get("clip_names"));
        for (Object rawEntity : array(root.get("entities"))) {
            Map<String, Object> node = requireObject(rawEntity, "authoring entity entry");
            String assetKey = string(node, "asset_key", "");
            if (assetKey.isBlank()) {
                continue;
            }

            List<String> files = normalizeFiles(node.get("files"), authoringPath);
            Map<String, Object> clips = optionalObject(node.get("clips"));
            List<String> clipNames = collectClipNames(clips, files, preferredClipOrder, supportedClipNames);
            List<FrameManifestEntry> frames = buildFrames(assetKey, clips, files, clipNames);

            entries.add(new EntityManifestEntry(
                assetKey,
                files,
                frames.isEmpty() ? files.size() : frames.size(),
                decodeCanvas(node.get("canvas_size")),
                clipNames,
                frames,
                MeshOptionsManifest.defaults(),
                decodeAnchors(node.get("anchors")),
                extractMetadata(node, Set.of("asset_key", "canvas_size", "anchors", "clips", "files"))
            ));
        }
        return List.copyOf(entries);
    }

    private static List<ThemeManifestEntry> decodeThemes(Map<String, Object> root) {
        Map<String, Object> ui = optionalObject(root.get("ui"));
        if (ui == null) {
            return List.of();
        }

        Map<String, Object> project = optionalObject(root.get("project"));
        String themeName = string(ui, "theme_name", string(project, "theme_default", "default"));
        String themeKey = string(ui, "theme_key", "shardharbor:ui-" + themeName + "-core");
        AssetKey libraryKey = AssetKey.parse(themeKey);
        String paletteKey = AssetKey.of(libraryKey.getNamespace(), libraryKey.getName() + "-palette").asString();

        Map<String, Object> typography = optionalObject(ui.get("typography"));
        String fontKey = typography != null && !string(typography, "asset_key", "").isBlank()
            ? string(typography, "asset_key", "")
            : AssetKey.of(libraryKey.getNamespace(), libraryKey.getName() + "-font").asString();

        List<PaletteRoleManifestEntry> paletteRoles = decodePaletteRoles(ui.get("palette_roles"));
        TypographyManifestEntry typographyEntry = decodeTypography(typography, paletteRoles);
        List<UiModuleManifestEntry> uiModules = decodeUiModules(ui, root.get("ui_module_mapping"), libraryKey);

        Map<String, Object> metadata = extractMetadata(
            ui,
            Set.of("theme_key", "theme_name", "palette_roles", "typography", "panel_assemblies")
        );
        List<Object> uiModuleMapping = array(root.get("ui_module_mapping"));
        if (!uiModuleMapping.isEmpty()) {
            metadata = mergeMetadata(metadata, Map.of("ui_module_mapping", deepCopy(uiModuleMapping)));
        }
        if (typography != null) {
            metadata = mergeMetadata(metadata, Map.of("typography_roles", deepCopy(typography.get("font_roles"))));
        }

        return List.of(new ThemeManifestEntry(
            themeName,
            libraryKey.asString(),
            paletteKey,
            fontKey,
            "",
            paletteRoles,
            typographyEntry,
            uiModules,
            metadata
        ));
    }

    private static Map<String, Object> decodeRootMetadata(Map<String, Object> root, Path authoringPath) {
        Map<String, Object> metadata = extractMetadata(root, Set.of("entities", "ui"));
        return mergeMetadata(metadata, Map.of("authoring_source_path", authoringPath.toString()));
    }

    private static List<String> normalizeFiles(Object rawFiles, Path authoringPath) {
        ArrayList<String> files = new ArrayList<>();
        for (String file : stringList(rawFiles)) {
            files.add(resolveAuthoringFile(authoringPath, file).toString());
        }
        return List.copyOf(files);
    }

    private static List<String> collectClipNames(
        Map<String, Object> clips,
        List<String> files,
        List<String> preferredClipOrder,
        Set<String> supportedClipNames
    ) {
        LinkedHashSet<String> clipNames = new LinkedHashSet<>();
        LinkedHashSet<String> inferredFromFiles = inferClipNames(files);

        for (String clipName : preferredClipOrder) {
            if ((clips != null && clips.containsKey(clipName)) || inferredFromFiles.contains(clipName)) {
                clipNames.add(clipName);
            }
        }
        if (clips != null) {
            clipNames.addAll(clips.keySet());
        }
        clipNames.addAll(inferredFromFiles);
        if (clipNames.isEmpty()) {
            clipNames.add("idle");
        }
        for (String supportedClip : supportedClipNames) {
            if (clipNames.contains(supportedClip)) {
                continue;
            }
        }
        return List.copyOf(clipNames);
    }

    private static LinkedHashSet<String> inferClipNames(List<String> files) {
        LinkedHashSet<String> clipNames = new LinkedHashSet<>();
        for (String file : files) {
            FrameName frameName = parseFrameName(file);
            clipNames.add(frameName.clipName());
        }
        return clipNames;
    }

    private static List<FrameManifestEntry> buildFrames(String assetKey, Map<String, Object> clips, List<String> files, List<String> clipNames) {
        LinkedHashMap<String, ArrayList<FileFrame>> clipFiles = new LinkedHashMap<>();
        for (String file : files) {
            FrameName frameName = parseFrameName(file);
            clipFiles.computeIfAbsent(frameName.clipName(), ignored -> new ArrayList<>()).add(new FileFrame(file, frameName.orderIndex()));
        }
        for (ArrayList<FileFrame> clipGroup : clipFiles.values()) {
            clipGroup.sort((left, right) -> Integer.compare(left.orderIndex(), right.orderIndex()));
        }

        ArrayList<FrameManifestEntry> frames = new ArrayList<>();
        String assetName = AssetKey.parse(assetKey).getName();
        for (String clipName : clipNames) {
            ArrayList<FileFrame> clipGroup = clipFiles.getOrDefault(clipName, new ArrayList<>());
            Map<String, Object> clipSpec = clips == null ? null : optionalObject(clips.get(clipName));
            long duration = clipSpec == null ? 140L : longValue(clipSpec, "frame_duration_ms", 140L);
            for (int index = 0; index < clipGroup.size(); index++) {
                FileFrame fileFrame = clipGroup.get(index);
                frames.add(new FrameManifestEntry(
                    assetName + "-" + clipName + "-" + index,
                    clipName,
                    fileFrame.orderIndex(),
                    duration,
                    fileFrame.file()
                ));
            }
        }
        return List.copyOf(frames);
    }

    private static CanvasManifest decodeCanvas(Object value) {
        if (value instanceof List<?> list && list.size() >= 2) {
            return new CanvasManifest(asInt(list.get(0), 0), asInt(list.get(1), 0));
        }
        Map<String, Object> object = optionalObject(value);
        if (object != null) {
            return new CanvasManifest(integer(object, "width", 0), integer(object, "height", 0));
        }
        return new CanvasManifest(0, 0);
    }

    private static List<AnchorManifestEntry> decodeAnchors(Object value) {
        ArrayList<AnchorManifestEntry> anchors = new ArrayList<>();
        for (Object rawAnchor : array(value)) {
            Map<String, Object> node = requireObject(rawAnchor, "authoring anchor entry");
            List<Object> normalized = array(node.get("normalized_xy"));
            double x = normalized.size() >= 2 ? asDouble(normalized.get(0), 0.0) : doubleValue(node, "x", 0.0);
            double y = normalized.size() >= 2 ? asDouble(normalized.get(1), 0.0) : doubleValue(node, "y", 0.0);
            anchors.add(new AnchorManifestEntry(string(node, "name", "anchor-" + anchors.size()), x, y));
        }
        return List.copyOf(anchors);
    }

    private static List<PaletteRoleManifestEntry> decodePaletteRoles(Object value) {
        Map<String, Object> roles = optionalObject(value);
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        ArrayList<PaletteRoleManifestEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Object> entry : roles.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            entries.add(new PaletteRoleManifestEntry(entry.getKey(), decodeColor(entry.getValue()), 1.0));
        }
        return List.copyOf(entries);
    }

    private static TypographyManifestEntry decodeTypography(Map<String, Object> typography, List<PaletteRoleManifestEntry> paletteRoles) {
        if (typography == null) {
            return new TypographyManifestEntry("Dialog", "REGULAR", 16, 1.18, true, DEFAULT_GLYPHS, pickFallbackRole(paletteRoles));
        }

        Map<String, Object> fontRoles = optionalObject(typography.get("font_roles"));
        Map<String, Object> selectedRole = pickFontRole(fontRoles);
        String styleDescriptor = selectedRole == null ? "" : string(selectedRole, "style", "");
        return new TypographyManifestEntry(
            inferFontFamily(styleDescriptor),
            inferFontStyle(styleDescriptor),
            selectedRole == null ? 16 : integer(selectedRole, "size_px", 16),
            1.18,
            true,
            DEFAULT_GLYPHS,
            selectedRole == null ? pickFallbackRole(paletteRoles) : string(selectedRole, "color_role", pickFallbackRole(paletteRoles))
        );
    }

    private static List<UiModuleManifestEntry> decodeUiModules(Object rawUi, Object rawMapping, AssetKey libraryKey) {
        Map<String, Object> ui = optionalObject(rawUi);
        if (ui == null) {
            return List.of();
        }

        Map<String, Map<String, Object>> assemblyByModule = new LinkedHashMap<>();
        for (Object rawAssembly : array(ui.get("panel_assemblies"))) {
            Map<String, Object> assembly = requireObject(rawAssembly, "ui panel assembly");
            String moduleName = string(assembly, "module_name", "");
            if (!moduleName.isBlank()) {
                assemblyByModule.put(moduleName, assembly);
            }
        }

        ArrayList<UiModuleManifestEntry> modules = new ArrayList<>();
        for (Object rawModule : array(rawMapping)) {
            Map<String, Object> node = requireObject(rawModule, "ui module mapping");
            String authoringModuleName = string(node, "module_name", "");
            if (authoringModuleName.isBlank()) {
                continue;
            }
            String runtimeModuleName = toRuntimeModuleName(authoringModuleName);
            Map<String, Object> assembly = assemblyByModule.get(authoringModuleName);
            ModuleLayout layout = resolveLayout(runtimeModuleName, assembly);
            String moduleKey = AssetKey.of(libraryKey.getNamespace(), runtimeModuleName, libraryKey.getName()).asString();
            Map<String, Object> metadata = extractMetadata(node, Set.of("module_name"));
            if (assembly != null) {
                metadata = mergeMetadata(metadata, Map.of("panel_assembly", deepCopy(assembly)));
            }
            modules.add(new UiModuleManifestEntry(
                moduleKey,
                runtimeModuleName,
                1280.0,
                720.0,
                buildDefaultUiElements(runtimeModuleName, layout, string(node, "recommended_palette_role", "primary")),
                metadata
            ));
        }
        return List.copyOf(modules);
    }

    private static List<UiElementManifestEntry> buildDefaultUiElements(String moduleName, ModuleLayout layout, String accentRole) {
        ArrayList<UiElementManifestEntry> elements = new ArrayList<>();
        double x = layout.x();
        double y = layout.y();
        double width = layout.width();
        double height = layout.height();

        elements.add(new UiElementManifestEntry(moduleName + "-panel", "PANEL", x, y, width, height, "panel", "", ""));
        elements.add(new UiElementManifestEntry(moduleName + "-frame", "FRAME", x + 6.0, y + 6.0, width - 12.0, height - 12.0, "panelLine", "", ""));
        elements.add(new UiElementManifestEntry(moduleName + "-header", "TEXT", x + 18.0, y + 12.0, width - 36.0, 18.0, "text", "", panelTitle(moduleName)));

        switch (moduleName) {
            case "minimap-panel" -> {
                elements.add(new UiElementManifestEntry(moduleName + "-map-frame", "FRAME", x + 18.0, y + 38.0, width - 36.0, height - 56.0, "accent", "", ""));
                elements.add(new UiElementManifestEntry(moduleName + "-subtitle", "TEXT", x + 18.0, y + height - 22.0, width - 36.0, 14.0, "muted", "", "Sector view"));
            }
            case "alarm-panel" -> {
                elements.add(new UiElementManifestEntry(moduleName + "-badge", "BADGE", x + 18.0, y + 38.0, 22.0, 22.0, "critical", "", ""));
                elements.add(new UiElementManifestEntry(moduleName + "-status", "TEXT", x + 52.0, y + 38.0, width - 70.0, 16.0, "warning", "", "Threat state"));
                elements.add(new UiElementManifestEntry(moduleName + "-bar", "PROGRESS_BAR", x + 18.0, y + height - 18.0, width - 36.0, 8.0, "critical", "", ""));
            }
            case "objective-panel" -> {
                elements.add(new UiElementManifestEntry(moduleName + "-icon", "BADGE", x + 18.0, y + 38.0, 22.0, 22.0, "objective", "", ""));
                elements.add(new UiElementManifestEntry(moduleName + "-status", "TEXT", x + 52.0, y + 38.0, width - 70.0, 16.0, "primary", "", "Sector objective"));
                elements.add(new UiElementManifestEntry(moduleName + "-bar", "PROGRESS_BAR", x + 18.0, y + height - 18.0, width - 36.0, 8.0, "objective", "", ""));
            }
            case "fleet-strip-panel" -> {
                for (int index = 0; index < 3; index++) {
                    elements.add(new UiElementManifestEntry(
                        moduleName + "-slot-" + index,
                        "SLOT",
                        x + 18.0 + index * 160.0,
                        y + 40.0,
                        146.0,
                        42.0,
                        "fleet",
                        "",
                        ""
                    ));
                }
            }
            case "loot-panel" -> {
                elements.add(new UiElementManifestEntry(moduleName + "-badge", "BADGE", x + 18.0, y + 40.0, 18.0, 18.0, "salvage", "", ""));
                elements.add(new UiElementManifestEntry(moduleName + "-status", "TEXT", x + 46.0, y + 40.0, width - 64.0, 16.0, "salvage", "", "Salvage pressure"));
                elements.add(new UiElementManifestEntry(moduleName + "-bar", "PROGRESS_BAR", x + 18.0, y + height - 18.0, width - 36.0, 8.0, "salvage", "", ""));
            }
            case "repair-panel" -> {
                elements.add(new UiElementManifestEntry(moduleName + "-status", "TEXT", x + 18.0, y + 40.0, width - 36.0, 16.0, "repair", "", "Repair target"));
                elements.add(new UiElementManifestEntry(moduleName + "-bar", "PROGRESS_BAR", x + 18.0, y + height - 18.0, width - 36.0, 8.0, "repair", "", ""));
                addButtonTriplet(elements, moduleName, x, y, "repair");
            }
            case "routing-panel" -> {
                elements.add(new UiElementManifestEntry(moduleName + "-status", "TEXT", x + 18.0, y + 40.0, width - 36.0, 16.0, "power", "", "Power routing"));
                elements.add(new UiElementManifestEntry(moduleName + "-bar", "PROGRESS_BAR", x + 18.0, y + height - 18.0, width - 36.0, 8.0, "power", "", ""));
                addButtonTriplet(elements, moduleName, x, y, "power");
            }
            case "escape-status-panel" -> {
                elements.add(new UiElementManifestEntry(moduleName + "-status", "TEXT", x + 18.0, y + 40.0, width - 36.0, 16.0, "objective", "", "Extraction corridor"));
                elements.add(new UiElementManifestEntry(moduleName + "-bar", "PROGRESS_BAR", x + 18.0, y + height - 18.0, width - 36.0, 8.0, "objective", "", ""));
            }
            default -> {
                elements.add(new UiElementManifestEntry(moduleName + "-status", "TEXT", x + 18.0, y + 40.0, width - 36.0, 16.0, accentRole, "", ""));
                elements.add(new UiElementManifestEntry(moduleName + "-bar", "PROGRESS_BAR", x + 18.0, y + height - 18.0, width - 36.0, 8.0, accentRole, "", ""));
            }
        }
        return List.copyOf(elements);
    }

    private static void addButtonTriplet(List<UiElementManifestEntry> elements, String moduleName, double x, double y, String accentRole) {
        for (int index = 0; index < 3; index++) {
            elements.add(new UiElementManifestEntry(
                moduleName + "-button-" + index,
                "BUTTON",
                x + 18.0 + index * 110.0,
                y + 78.0,
                90.0,
                24.0,
                index == 1 ? accentRole : "muted",
                "",
                ""
            ));
        }
    }

    private static ModuleLayout resolveLayout(String moduleName, Map<String, Object> assembly) {
        double width = assemblySize(assembly, 0, defaultWidth(moduleName));
        double height = assemblySize(assembly, 1, defaultHeight(moduleName));
        return switch (moduleName) {
            case "minimap-panel" -> new ModuleLayout(18.0, 18.0, width, height);
            case "alarm-panel" -> new ModuleLayout(278.0, 18.0, width, height);
            case "objective-panel" -> new ModuleLayout(962.0, 18.0, width, height);
            case "fleet-strip-panel" -> new ModuleLayout(18.0, 592.0, width, height);
            default -> new ModuleLayout(898.0, 538.0, width, height);
        };
    }

    private static double defaultWidth(String moduleName) {
        return switch (moduleName) {
            case "minimap-panel" -> 240.0;
            case "alarm-panel" -> 360.0;
            case "objective-panel" -> 300.0;
            case "fleet-strip-panel" -> 510.0;
            default -> 364.0;
        };
    }

    private static double defaultHeight(String moduleName) {
        return switch (moduleName) {
            case "minimap-panel" -> 174.0;
            case "alarm-panel" -> 84.0;
            case "objective-panel" -> 112.0;
            case "fleet-strip-panel" -> 98.0;
            default -> 152.0;
        };
    }

    private static double assemblySize(Map<String, Object> assembly, int index, double fallback) {
        if (assembly == null) {
            return fallback;
        }
        List<Object> size = array(assembly.get("size"));
        if (size.size() <= index) {
            return fallback;
        }
        return asDouble(size.get(index), fallback);
    }

    private static String toRuntimeModuleName(String moduleName) {
        return switch (moduleName) {
            case "minimap" -> "minimap-panel";
            case "alarm" -> "alarm-panel";
            case "loot" -> "loot-panel";
            case "repair" -> "repair-panel";
            case "routing" -> "routing-panel";
            case "fleet-strip" -> "fleet-strip-panel";
            case "escape-status" -> "escape-status-panel";
            case "objective" -> "objective-panel";
            default -> normalizeModuleName(moduleName);
        };
    }

    private static String normalizeModuleName(String moduleName) {
        if (moduleName == null || moduleName.isBlank()) {
            return "module-panel";
        }
        String normalized = moduleName.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
        return normalized.endsWith("-panel") ? normalized : normalized + "-panel";
    }

    private static String panelTitle(String moduleName) {
        return switch (moduleName) {
            case "minimap-panel" -> "MINIMAP";
            case "alarm-panel" -> "ALARM";
            case "objective-panel" -> "OBJECTIVE";
            case "loot-panel" -> "SALVAGE";
            case "repair-panel" -> "REPAIR";
            case "routing-panel" -> "POWER";
            case "fleet-strip-panel" -> "FLEET";
            case "escape-status-panel" -> "ESCAPE";
            default -> moduleName.replace("-panel", "").replace('-', ' ').toUpperCase(Locale.ROOT);
        };
    }

    private static Map<String, Object> pickFontRole(Map<String, Object> fontRoles) {
        if (fontRoles == null || fontRoles.isEmpty()) {
            return null;
        }
        for (String preferred : List.of("body", "header", "subheader", "numeric", "button")) {
            Map<String, Object> candidate = optionalObject(fontRoles.get(preferred));
            if (candidate != null) {
                return candidate;
            }
        }
        for (Object candidate : fontRoles.values()) {
            Map<String, Object> object = optionalObject(candidate);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    private static String inferFontFamily(String descriptor) {
        String normalized = descriptor == null ? "" : descriptor.toLowerCase(Locale.ROOT);
        if (normalized.contains("mono")) {
            return "Monospaced";
        }
        if (normalized.contains("serif")) {
            return normalized.contains("sans") ? "SansSerif" : "Serif";
        }
        if (normalized.contains("technical")) {
            return "DialogInput";
        }
        return "SansSerif";
    }

    private static String inferFontStyle(String descriptor) {
        String normalized = descriptor == null ? "" : descriptor.toLowerCase(Locale.ROOT);
        boolean bold = normalized.contains("bold");
        boolean italic = normalized.contains("italic");
        if (bold && italic) {
            return "BOLD_ITALIC";
        }
        if (bold) {
            return "BOLD";
        }
        if (italic) {
            return "ITALIC";
        }
        return "REGULAR";
    }

    private static String pickFallbackRole(List<PaletteRoleManifestEntry> paletteRoles) {
        for (PaletteRoleManifestEntry role : paletteRoles) {
            if ("text".equalsIgnoreCase(role.role())) {
                return role.role();
            }
        }
        return paletteRoles.isEmpty() ? "text" : paletteRoles.get(0).role();
    }

    private static ManifestColor decodeColor(Object value) {
        if (value instanceof String text) {
            return ManifestColor.fromString(text);
        }
        Map<String, Object> node = optionalObject(value);
        if (node != null) {
            return new ManifestColor(
                integer(node, "r", 255),
                integer(node, "g", 255),
                integer(node, "b", 255),
                integer(node, "a", 255)
            );
        }
        return new ManifestColor(255, 255, 255, 255);
    }

    private static Path resolveAuthoringFile(Path authoringPath, String file) {
        Path direct = Path.of(file);
        if (direct.isAbsolute()) {
            return direct.normalize();
        }

        Path repoCandidate = Path.of(".").toAbsolutePath().resolve(file).normalize();
        if (Files.exists(repoCandidate)) {
            return repoCandidate;
        }

        String normalized = file.replace('\\', '/');
        String shardHarborPrefix = "assets/shardharbor/";
        if (normalized.startsWith(shardHarborPrefix)) {
            Path shardCandidate = Path.of(".")
                .toAbsolutePath()
                .resolve("shardharbor")
                .resolve("assets")
                .resolve(normalized.substring(shardHarborPrefix.length()))
                .normalize();
            if (Files.exists(shardCandidate)) {
                return shardCandidate;
            }
        }

        Path relativeToAuthoring = authoringPath.toAbsolutePath().getParent().resolve(file).normalize();
        if (Files.exists(relativeToAuthoring)) {
            return relativeToAuthoring;
        }
        return repoCandidate;
    }

    private static FrameName parseFrameName(String file) {
        String filename = Path.of(file).getFileName().toString();
        Matcher matcher = FRAME_FILE_PATTERN.matcher(filename);
        if (matcher.find()) {
            return new FrameName(matcher.group(1), Integer.parseInt(matcher.group(2)));
        }
        int extensionIndex = filename.lastIndexOf('.');
        String clip = extensionIndex >= 0 ? filename.substring(0, extensionIndex) : filename;
        return new FrameName(clip, 0);
    }

    private static Map<String, Object> extractMetadata(Map<String, Object> node, Set<String> knownKeys) {
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : node.entrySet()) {
            if (!knownKeys.contains(entry.getKey())) {
                metadata.put(entry.getKey(), deepCopy(entry.getValue()));
            }
        }
        return Map.copyOf(metadata);
    }

    private static Map<String, Object> mergeMetadata(Map<String, Object> left, Map<String, Object> right) {
        LinkedHashMap<String, Object> merged = new LinkedHashMap<>();
        if (left != null) {
            merged.putAll(left);
        }
        if (right != null) {
            merged.putAll(right);
        }
        return Map.copyOf(merged);
    }

    private static Object deepCopy(Object value) {
        if (value instanceof Map<?, ?> map) {
            LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                copy.put(Objects.toString(entry.getKey(), ""), deepCopy(entry.getValue()));
            }
            return Map.copyOf(copy);
        }
        if (value instanceof List<?> list) {
            ArrayList<Object> copy = new ArrayList<>(list.size());
            for (Object item : list) {
                copy.add(deepCopy(item));
            }
            return List.copyOf(copy);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> requireObject(Object value, String label) {
        if (value instanceof Map<?, ?> map) {
            LinkedHashMap<String, Object> normalized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                normalized.put(Objects.toString(entry.getKey(), ""), (Object) entry.getValue());
            }
            return normalized;
        }
        throw new IllegalStateException("Expected " + label + " to be a JSON object.");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> optionalObject(Object value) {
        if (value instanceof Map<?, ?> map) {
            LinkedHashMap<String, Object> normalized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                normalized.put(Objects.toString(entry.getKey(), ""), (Object) entry.getValue());
            }
            return normalized;
        }
        return null;
    }

    private static List<Object> array(Object value) {
        if (value instanceof List<?> list) {
            return List.copyOf(list);
        }
        return List.of();
    }

    private static List<String> stringList(Object value) {
        ArrayList<String> strings = new ArrayList<>();
        for (Object item : array(value)) {
            if (item != null) {
                strings.add(Objects.toString(item, "").trim());
            }
        }
        return List.copyOf(strings);
    }

    private static String string(Map<String, Object> node, String key, String fallback) {
        Object value = node.get(key);
        if (value == null) {
            return fallback;
        }
        String text = Objects.toString(value, fallback).trim();
        return text.isEmpty() ? fallback : text;
    }

    private static int integer(Map<String, Object> node, String key, int fallback) {
        return asInt(node.get(key), fallback);
    }

    private static long longValue(Map<String, Object> node, String key, long fallback) {
        Object value = node.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text.trim());
        }
        return fallback;
    }

    private static double doubleValue(Map<String, Object> node, String key, double fallback) {
        return asDouble(node.get(key), fallback);
    }

    private static int asInt(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text.trim());
        }
        return fallback;
    }

    private static double asDouble(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Double.parseDouble(text.trim());
        }
        return fallback;
    }

    private record FileFrame(String file, int orderIndex) {
    }

    private record FrameName(String clipName, int orderIndex) {
    }

    private record ModuleLayout(double x, double y, double width, double height) {
    }

    record AuthoringLoadResult(AssetManifest manifest, Set<String> supportedClipNames) {
    }
}
