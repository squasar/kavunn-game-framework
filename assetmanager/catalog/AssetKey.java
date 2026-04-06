package assetmanager.catalog;

import java.util.Objects;

public final class AssetKey {

    private final String namespace;
    private final String name;
    private final String variant;

    public AssetKey(String namespace, String name) {
        this(namespace, name, "");
    }

    public AssetKey(String namespace, String name, String variant) {
        this.namespace = normalize(namespace, "global");
        this.name = normalize(name, "unnamed");
        this.variant = variant == null ? "" : variant.trim();
    }

    public static AssetKey of(String namespace, String name) {
        return new AssetKey(namespace, name);
    }

    public static AssetKey of(String namespace, String name, String variant) {
        return new AssetKey(namespace, name, variant);
    }

    public static AssetKey parse(String value) {
        if (value == null || value.isBlank()) {
            return new AssetKey("global", "unnamed");
        }

        String[] variantSplit = value.split("#", 2);
        String base = variantSplit[0];
        String variant = variantSplit.length > 1 ? variantSplit[1] : "";
        String[] namespaceSplit = base.split(":", 2);

        if (namespaceSplit.length == 1) {
            return new AssetKey("global", namespaceSplit[0], variant);
        }
        return new AssetKey(namespaceSplit[0], namespaceSplit[1], variant);
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getName() {
        return this.name;
    }

    public String getVariant() {
        return this.variant;
    }

    public boolean hasVariant() {
        return !this.variant.isBlank();
    }

    public String asString() {
        return hasVariant()
            ? this.namespace + ":" + this.name + "#" + this.variant
            : this.namespace + ":" + this.name;
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AssetKey assetKey)) {
            return false;
        }
        return Objects.equals(this.namespace, assetKey.namespace)
            && Objects.equals(this.name, assetKey.name)
            && Objects.equals(this.variant, assetKey.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.namespace, this.name, this.variant);
    }

    private static String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
