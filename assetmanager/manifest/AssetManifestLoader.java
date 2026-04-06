package assetmanager.manifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class AssetManifestLoader {

    public AssetManifest load(Path manifestPath) {
        if (manifestPath == null) {
            throw new IllegalArgumentException("An asset manifest path is required.");
        }
        try {
            return parse(Files.readString(manifestPath), manifestPath);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read asset manifest: " + manifestPath, exception);
        }
    }

    AssetManifest parse(String json, Path sourcePath) {
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("Asset manifest content is empty.");
        }

        Object rootValue = new ManifestJsonParser(json).parse();
        Map<String, Object> root = requireObject(rootValue, "manifest root");
        return new AssetManifest(
            decodeEntities(root),
            decodeThemes(root),
            extractMetadata(root, Set.of("entities", "themes", "metadata")),
            sourcePath
        );
    }

    private static List<EntityManifestEntry> decodeEntities(Map<String, Object> root) {
        ArrayList<EntityManifestEntry> entries = new ArrayList<>();
        for (Object rawEntry : array(root.get("entities"))) {
            Map<String, Object> node = requireObject(rawEntry, "entity entry");
            List<FrameManifestEntry> frames = decodeFrames(node);
            List<String> files = stringList(node.get("files"));
            List<String> clipNames = stringList(node.get("clip_names"));
            if (clipNames.isEmpty() && !frames.isEmpty()) {
                LinkedHashSet<String> inferred = new LinkedHashSet<>();
                for (FrameManifestEntry frame : frames) {
                    inferred.add(frame.clipName());
                }
                clipNames = new ArrayList<>(inferred);
            }

            entries.add(new EntityManifestEntry(
                string(node, "asset_key", ""),
                files,
                integer(node, "frame_count", Math.max(files.size(), frames.size())),
                decodeCanvas(node.get("canvas_size")),
                clipNames,
                frames,
                decodeMeshOptions(node.get("mesh_options")),
                decodeAnchors(node.get("anchors")),
                extractMetadata(node, Set.of("asset_key", "files", "frame_count", "canvas_size", "clip_names", "frames", "mesh_options", "anchors", "metadata"))
            ));
        }
        return List.copyOf(entries);
    }

    private static List<ThemeManifestEntry> decodeThemes(Map<String, Object> root) {
        ArrayList<ThemeManifestEntry> entries = new ArrayList<>();
        for (Object rawEntry : array(root.get("themes"))) {
            Map<String, Object> node = requireObject(rawEntry, "theme entry");
            entries.add(new ThemeManifestEntry(
                string(node, "theme_id", ""),
                string(node, "library_key", ""),
                string(node, "palette_key", ""),
                string(node, "font_key", ""),
                string(node, "material_key", ""),
                decodePaletteRoles(node.get("palette_roles")),
                decodeTypography(node.get("typography")),
                decodeUiModules(node.get("ui_modules")),
                extractMetadata(node, Set.of("theme_id", "library_key", "palette_key", "font_key", "material_key", "palette_roles", "typography", "ui_modules", "metadata"))
            ));
        }
        return List.copyOf(entries);
    }

    private static List<FrameManifestEntry> decodeFrames(Map<String, Object> node) {
        ArrayList<FrameManifestEntry> frames = new ArrayList<>();
        for (Object rawFrame : array(node.get("frames"))) {
            Map<String, Object> frameNode = requireObject(rawFrame, "frame entry");
            frames.add(new FrameManifestEntry(
                string(frameNode, "id", ""),
                string(frameNode, "clip", "idle"),
                integer(frameNode, "order", frames.size()),
                longValue(frameNode, "duration_ms", 140L),
                string(frameNode, "file", "")
            ));
        }
        return List.copyOf(frames);
    }

    private static CanvasManifest decodeCanvas(Object value) {
        Map<String, Object> node = optionalObject(value);
        if (node == null) {
            return new CanvasManifest(0, 0);
        }
        return new CanvasManifest(integer(node, "width", 0), integer(node, "height", 0));
    }

    private static MeshOptionsManifest decodeMeshOptions(Object value) {
        Map<String, Object> node = optionalObject(value);
        if (node == null) {
            return MeshOptionsManifest.defaults();
        }
        return new MeshOptionsManifest(
            bool(node, "normalize_to_center", true),
            integer(node, "alpha_threshold", 20),
            integer(node, "max_contour_points", 128),
            integer(node, "contour_samples", 64),
            integer(node, "max_palette_colors", 5)
        );
    }

    private static List<AnchorManifestEntry> decodeAnchors(Object value) {
        ArrayList<AnchorManifestEntry> anchors = new ArrayList<>();
        for (Object rawAnchor : array(value)) {
            Map<String, Object> node = requireObject(rawAnchor, "anchor entry");
            anchors.add(new AnchorManifestEntry(
                string(node, "name", "anchor-" + anchors.size()),
                doubleValue(node, "x", 0.0),
                doubleValue(node, "y", 0.0)
            ));
        }
        return List.copyOf(anchors);
    }

    private static List<PaletteRoleManifestEntry> decodePaletteRoles(Object value) {
        ArrayList<PaletteRoleManifestEntry> roles = new ArrayList<>();
        for (Object rawRole : array(value)) {
            Map<String, Object> node = requireObject(rawRole, "palette role entry");
            roles.add(new PaletteRoleManifestEntry(
                string(node, "role", "primary"),
                decodeColor(node.get("color")),
                doubleValue(node, "weight", 1.0)
            ));
        }
        return List.copyOf(roles);
    }

    private static TypographyManifestEntry decodeTypography(Object value) {
        Map<String, Object> node = optionalObject(value);
        if (node == null) {
            return new TypographyManifestEntry("Dialog", "REGULAR", 16, 1.0, true, "", "text");
        }
        return new TypographyManifestEntry(
            string(node, "family", "Dialog"),
            string(node, "style", "REGULAR"),
            integer(node, "point_size", 16),
            doubleValue(node, "line_height", 1.0),
            bool(node, "antialias", true),
            string(node, "glyph_set", ""),
            string(node, "color_role", "text")
        );
    }

    private static List<UiModuleManifestEntry> decodeUiModules(Object value) {
        ArrayList<UiModuleManifestEntry> modules = new ArrayList<>();
        for (Object rawModule : array(value)) {
            Map<String, Object> node = requireObject(rawModule, "ui module entry");
            modules.add(new UiModuleManifestEntry(
                string(node, "key", ""),
                string(node, "module_type", "module"),
                doubleValue(node, "design_width", 1280.0),
                doubleValue(node, "design_height", 720.0),
                decodeUiElements(node.get("elements")),
                extractMetadata(node, Set.of("key", "module_type", "design_width", "design_height", "elements", "metadata"))
            ));
        }
        return List.copyOf(modules);
    }

    private static List<UiElementManifestEntry> decodeUiElements(Object value) {
        ArrayList<UiElementManifestEntry> elements = new ArrayList<>();
        for (Object rawElement : array(value)) {
            Map<String, Object> node = requireObject(rawElement, "ui element entry");
            elements.add(new UiElementManifestEntry(
                string(node, "element_id", "element-" + elements.size()),
                string(node, "type", "PANEL"),
                doubleValue(node, "x", 0.0),
                doubleValue(node, "y", 0.0),
                doubleValue(node, "width", 0.0),
                doubleValue(node, "height", 0.0),
                string(node, "palette_role", "primary"),
                string(node, "material_key", ""),
                string(node, "text", "")
            ));
        }
        return List.copyOf(elements);
    }

    private static ManifestColor decodeColor(Object value) {
        if (value instanceof String stringValue) {
            return ManifestColor.fromString(stringValue);
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

    private static Map<String, Object> extractMetadata(Map<String, Object> node, Set<String> knownKeys) {
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        Map<String, Object> explicitMetadata = optionalObject(node.get("metadata"));
        if (explicitMetadata != null) {
            for (Map.Entry<String, Object> entry : explicitMetadata.entrySet()) {
                metadata.put(entry.getKey(), deepCopy(entry.getValue()));
            }
        }
        for (Map.Entry<String, Object> entry : node.entrySet()) {
            if (!knownKeys.contains(entry.getKey()) && !"metadata".equals(entry.getKey())) {
                metadata.put(entry.getKey(), deepCopy(entry.getValue()));
            }
        }
        return Map.copyOf(metadata);
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
        if (value == null) {
            return null;
        }
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
        Object value = node.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Integer.parseInt(stringValue.trim());
        }
        return fallback;
    }

    private static long longValue(Map<String, Object> node, String key, long fallback) {
        Object value = node.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Long.parseLong(stringValue.trim());
        }
        return fallback;
    }

    private static double doubleValue(Map<String, Object> node, String key, double fallback) {
        Object value = node.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Double.parseDouble(stringValue.trim());
        }
        return fallback;
    }

    private static boolean bool(Map<String, Object> node, String key, boolean fallback) {
        Object value = node.get(key);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Boolean.parseBoolean(stringValue.trim());
        }
        return fallback;
    }
}

final class AssetManifest {

    private final List<EntityManifestEntry> entities;
    private final List<ThemeManifestEntry> themes;
    private final Map<String, Object> metadata;
    private final Path sourcePath;

    AssetManifest(List<EntityManifestEntry> entities, List<ThemeManifestEntry> themes, Map<String, Object> metadata, Path sourcePath) {
        this.entities = List.copyOf(entities == null ? List.of() : entities);
        this.themes = List.copyOf(themes == null ? List.of() : themes);
        this.metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        this.sourcePath = sourcePath;
    }

    List<EntityManifestEntry> entities() {
        return this.entities;
    }

    List<ThemeManifestEntry> themes() {
        return this.themes;
    }

    Map<String, Object> metadata() {
        return this.metadata;
    }

    Path sourcePath() {
        return this.sourcePath;
    }
}

record EntityManifestEntry(
    String assetKey,
    List<String> files,
    int frameCount,
    CanvasManifest canvasSize,
    List<String> clipNames,
    List<FrameManifestEntry> frames,
    MeshOptionsManifest meshOptions,
    List<AnchorManifestEntry> anchors,
    Map<String, Object> metadata
) {
}

record CanvasManifest(int width, int height) {
}

record FrameManifestEntry(String id, String clipName, int orderIndex, long durationMillis, String file) {
}

record MeshOptionsManifest(
    boolean normalizeToCenter,
    int alphaThreshold,
    int maxContourPoints,
    int contourSamples,
    int maxPaletteColors
) {
    static MeshOptionsManifest defaults() {
        return new MeshOptionsManifest(true, 20, 128, 64, 5);
    }
}

record AnchorManifestEntry(String name, double x, double y) {
}

record ThemeManifestEntry(
    String themeId,
    String libraryKey,
    String paletteKey,
    String fontKey,
    String materialKey,
    List<PaletteRoleManifestEntry> paletteRoles,
    TypographyManifestEntry typography,
    List<UiModuleManifestEntry> uiModules,
    Map<String, Object> metadata
) {
}

record PaletteRoleManifestEntry(String role, ManifestColor color, double weight) {
}

record TypographyManifestEntry(
    String family,
    String style,
    int pointSize,
    double lineHeight,
    boolean antialias,
    String glyphSet,
    String colorRole
) {
}

record UiModuleManifestEntry(
    String key,
    String moduleType,
    double designWidth,
    double designHeight,
    List<UiElementManifestEntry> elements,
    Map<String, Object> metadata
) {
}

record UiElementManifestEntry(
    String elementId,
    String type,
    double x,
    double y,
    double width,
    double height,
    String paletteRole,
    String materialKey,
    String text
) {
}

record ManifestColor(int red, int green, int blue, int alpha) {

    static ManifestColor fromString(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.length() == 6) {
            int rgb = Integer.parseInt(normalized, 16);
            return new ManifestColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 255);
        }
        if (normalized.length() == 8) {
            long argb = Long.parseLong(normalized, 16);
            return new ManifestColor((int) ((argb >> 16) & 0xFF), (int) ((argb >> 8) & 0xFF), (int) (argb & 0xFF), (int) ((argb >> 24) & 0xFF));
        }
        throw new IllegalStateException("Unsupported color value in asset manifest: " + value);
    }
}

final class ManifestJsonParser {

    private final String text;
    private int index;

    ManifestJsonParser(String text) {
        this.text = text;
    }

    Object parse() {
        skipWhitespace();
        Object value = parseValue();
        skipWhitespace();
        if (this.index != this.text.length()) {
            throw error("Unexpected trailing content.");
        }
        return value;
    }

    private Object parseValue() {
        skipWhitespace();
        if (this.index >= this.text.length()) {
            throw error("Unexpected end of JSON.");
        }

        return switch (this.text.charAt(this.index)) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't' -> parseLiteral("true", Boolean.TRUE);
            case 'f' -> parseLiteral("false", Boolean.FALSE);
            case 'n' -> parseLiteral("null", null);
            default -> parseNumber();
        };
    }

    private Map<String, Object> parseObject() {
        expect('{');
        LinkedHashMap<String, Object> object = new LinkedHashMap<>();
        skipWhitespace();
        if (peek('}')) {
            this.index++;
            return object;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            object.put(key, parseValue());
            skipWhitespace();
            if (peek('}')) {
                this.index++;
                return object;
            }
            expect(',');
        }
    }

    private List<Object> parseArray() {
        expect('[');
        ArrayList<Object> array = new ArrayList<>();
        skipWhitespace();
        if (peek(']')) {
            this.index++;
            return array;
        }
        while (true) {
            array.add(parseValue());
            skipWhitespace();
            if (peek(']')) {
                this.index++;
                return array;
            }
            expect(',');
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (this.index < this.text.length()) {
            char current = this.text.charAt(this.index++);
            if (current == '"') {
                return builder.toString();
            }
            if (current != '\\') {
                builder.append(current);
                continue;
            }
            if (this.index >= this.text.length()) {
                throw error("Unexpected end of escape sequence.");
            }
            char escaped = this.text.charAt(this.index++);
            switch (escaped) {
                case '"', '\\', '/' -> builder.append(escaped);
                case 'b' -> builder.append('\b');
                case 'f' -> builder.append('\f');
                case 'n' -> builder.append('\n');
                case 'r' -> builder.append('\r');
                case 't' -> builder.append('\t');
                case 'u' -> builder.append(parseUnicode());
                default -> throw error("Unsupported escape sequence: \\" + escaped);
            }
        }
        throw error("Unterminated string literal.");
    }

    private char parseUnicode() {
        if (this.index + 4 > this.text.length()) {
            throw error("Incomplete unicode escape.");
        }
        String hex = this.text.substring(this.index, this.index + 4);
        this.index += 4;
        return (char) Integer.parseInt(hex, 16);
    }

    private Object parseLiteral(String literal, Object value) {
        if (this.text.startsWith(literal, this.index)) {
            this.index += literal.length();
            return value;
        }
        throw error("Expected literal " + literal + ".");
    }

    private Number parseNumber() {
        int start = this.index;
        if (peek('-')) {
            this.index++;
        }
        while (this.index < this.text.length() && Character.isDigit(this.text.charAt(this.index))) {
            this.index++;
        }
        if (peek('.')) {
            this.index++;
            while (this.index < this.text.length() && Character.isDigit(this.text.charAt(this.index))) {
                this.index++;
            }
        }
        if (peek('e') || peek('E')) {
            this.index++;
            if (peek('+') || peek('-')) {
                this.index++;
            }
            while (this.index < this.text.length() && Character.isDigit(this.text.charAt(this.index))) {
                this.index++;
            }
        }
        String token = this.text.substring(start, this.index);
        if (token.contains(".") || token.contains("e") || token.contains("E")) {
            return Double.parseDouble(token);
        }
        return Long.parseLong(token);
    }

    private void skipWhitespace() {
        while (this.index < this.text.length() && Character.isWhitespace(this.text.charAt(this.index))) {
            this.index++;
        }
    }

    private void expect(char expected) {
        skipWhitespace();
        if (this.index >= this.text.length() || this.text.charAt(this.index) != expected) {
            throw error("Expected '" + expected + "'.");
        }
        this.index++;
    }

    private boolean peek(char expected) {
        return this.index < this.text.length() && this.text.charAt(this.index) == expected;
    }

    private IllegalStateException error(String message) {
        return new IllegalStateException(message + " (at index " + this.index + ")");
    }
}
