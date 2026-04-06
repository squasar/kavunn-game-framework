package assetmanager.catalog;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class AssetCatalog<T> {

    private final Map<AssetKey, T> assets = new LinkedHashMap<>();

    public void register(AssetKey key, T asset) {
        if (key == null) {
            throw new IllegalArgumentException("Asset registration requires a key.");
        }
        if (asset == null) {
            throw new IllegalArgumentException("Asset registration requires a value.");
        }
        this.assets.put(key, asset);
    }

    public T get(AssetKey key) {
        return this.assets.get(key);
    }

    public T require(AssetKey key) {
        T value = get(key);
        if (value == null) {
            throw new IllegalStateException("Asset not found: " + key);
        }
        return value;
    }

    public boolean contains(AssetKey key) {
        return this.assets.containsKey(key);
    }

    public int size() {
        return this.assets.size();
    }

    public boolean isEmpty() {
        return this.assets.isEmpty();
    }

    public Collection<T> values() {
        return Collections.unmodifiableCollection(this.assets.values());
    }

    public Set<AssetKey> keys() {
        return Collections.unmodifiableSet(this.assets.keySet());
    }
}
