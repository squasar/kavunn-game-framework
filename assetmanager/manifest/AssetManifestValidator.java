package assetmanager.manifest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import assetmanager.catalog.AssetKey;

public final class AssetManifestValidator {

    private static final Set<String> DEFAULT_SUPPORTED_CLIPS = Set.of("default", "idle", "damaged", "burning", "wreck");

    private final String requiredNamespace;
    private final Set<String> supportedClipNames;

    public AssetManifestValidator() {
        this("shardharbor", DEFAULT_SUPPORTED_CLIPS);
    }

    public AssetManifestValidator(String requiredNamespace, Set<String> supportedClipNames) {
        this.requiredNamespace = requiredNamespace == null || requiredNamespace.isBlank() ? "shardharbor" : requiredNamespace.trim();
        this.supportedClipNames = supportedClipNames == null || supportedClipNames.isEmpty()
            ? DEFAULT_SUPPORTED_CLIPS
            : Set.copyOf(supportedClipNames);
    }

    public void validateOrThrow(AssetManifest manifest) {
        List<String> issues = validate(manifest);
        if (!issues.isEmpty()) {
            throw new IllegalStateException("Asset manifest validation failed:" + System.lineSeparator() + String.join(System.lineSeparator(), issues));
        }
    }

    public List<String> validate(AssetManifest manifest) {
        ArrayList<String> issues = new ArrayList<>();
        if (manifest == null) {
            issues.add("- Manifest is null.");
            return issues;
        }

        Set<String> seenEntityKeys = new LinkedHashSet<>();
        for (EntityManifestEntry entity : manifest.entities()) {
            validateEntity(entity, manifest.sourcePath(), issues, seenEntityKeys);
        }
        for (ThemeManifestEntry theme : manifest.themes()) {
            validateTheme(theme, issues);
        }
        return List.copyOf(issues);
    }

    private void validateEntity(EntityManifestEntry entity, Path sourcePath, List<String> issues, Set<String> seenEntityKeys) {
        AssetKey assetKey = AssetKey.parse(entity.assetKey());
        if (!this.requiredNamespace.equals(assetKey.getNamespace())) {
            issues.add("- Entity asset_key must use namespace '" + this.requiredNamespace + "': " + entity.assetKey());
        }
        if (!seenEntityKeys.add(assetKey.asString())) {
            issues.add("- Duplicate entity asset_key found: " + assetKey.asString());
        }
        if (entity.files().isEmpty()) {
            issues.add("- Entity " + assetKey + " does not define files[].");
        }
        if (entity.frameCount() != entity.files().size()) {
            issues.add("- Entity " + assetKey + " frame_count does not match files[] size.");
        }
        if (!entity.frames().isEmpty() && entity.frameCount() != entity.frames().size()) {
            issues.add("- Entity " + assetKey + " frame_count does not match frames[] size.");
        }
        if (entity.canvasSize().width() <= 0 || entity.canvasSize().height() <= 0) {
            issues.add("- Entity " + assetKey + " has invalid canvas_size.");
        }
        if (entity.clipNames().isEmpty()) {
            issues.add("- Entity " + assetKey + " must declare clip_names.");
        }
        for (String clipName : entity.clipNames()) {
            if (!this.supportedClipNames.contains(clipName)) {
                issues.add("- Entity " + assetKey + " uses unsupported clip_name: " + clipName);
            }
        }

        Path manifestDirectory = sourcePath == null ? Path.of(".") : sourcePath.toAbsolutePath().getParent();
        for (String file : entity.files()) {
            Path resolved = resolveFile(manifestDirectory, file);
            if (!Files.exists(resolved)) {
                issues.add("- Missing asset file for " + assetKey + ": " + resolved);
                continue;
            }
            validateCanvasSize(assetKey, resolved, entity.canvasSize(), issues);
        }

        for (FrameManifestEntry frame : entity.frames()) {
            if (!entity.files().contains(frame.file())) {
                issues.add("- Frame file for " + assetKey + " is not listed in files[]: " + frame.file());
            }
            if (!this.supportedClipNames.contains(frame.clipName())) {
                issues.add("- Frame clip for " + assetKey + " is unsupported: " + frame.clipName());
            }
            if (!entity.clipNames().contains(frame.clipName())) {
                issues.add("- Frame clip for " + assetKey + " is not declared in clip_names: " + frame.clipName());
            }
        }
    }

    private void validateTheme(ThemeManifestEntry theme, List<String> issues) {
        if (!theme.libraryKey().isBlank() && !this.requiredNamespace.equals(AssetKey.parse(theme.libraryKey()).getNamespace())) {
            issues.add("- Theme library_key must use namespace '" + this.requiredNamespace + "': " + theme.libraryKey());
        }
        if (!theme.paletteKey().isBlank() && !this.requiredNamespace.equals(AssetKey.parse(theme.paletteKey()).getNamespace())) {
            issues.add("- Theme palette_key must use namespace '" + this.requiredNamespace + "': " + theme.paletteKey());
        }
        if (!theme.fontKey().isBlank() && !this.requiredNamespace.equals(AssetKey.parse(theme.fontKey()).getNamespace())) {
            issues.add("- Theme font_key must use namespace '" + this.requiredNamespace + "': " + theme.fontKey());
        }
        for (UiModuleManifestEntry module : theme.uiModules()) {
            if (!module.key().isBlank() && !this.requiredNamespace.equals(AssetKey.parse(module.key()).getNamespace())) {
                issues.add("- UI module key must use namespace '" + this.requiredNamespace + "': " + module.key());
            }
        }
    }

    private void validateCanvasSize(AssetKey assetKey, Path file, CanvasManifest expectedSize, List<String> issues) {
        try {
            BufferedImage image = ImageIO.read(file.toFile());
            if (image == null) {
                issues.add("- Asset file is not a readable image for " + assetKey + ": " + file);
                return;
            }
            if (expectedSize.width() > 0 && expectedSize.height() > 0
                && (image.getWidth() != expectedSize.width() || image.getHeight() != expectedSize.height())) {
                issues.add("- Canvas size mismatch for " + assetKey + ": expected "
                    + expectedSize.width() + "x" + expectedSize.height()
                    + " but found " + image.getWidth() + "x" + image.getHeight()
                    + " in " + file);
            }
        } catch (IOException exception) {
            issues.add("- Failed to inspect image for " + assetKey + ": " + file + " (" + exception.getMessage() + ")");
        }
    }

    private static Path resolveFile(Path manifestDirectory, String file) {
        Path path = Path.of(file);
        return path.isAbsolute() ? path : manifestDirectory.resolve(path).normalize();
    }
}
