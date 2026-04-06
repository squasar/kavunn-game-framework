package physics.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import physics.body.PhysicsBody;
import physics.geometry.Bounds2;

final class SpatialHashBroadphase {

    private final double cellSize;
    private final Map<Long, ArrayList<PhysicsBody>> buckets = new HashMap<>();
    private final List<Long> touchedKeys = new ArrayList<>();
    private int occupiedCellCount;
    private int maxBucketSize;

    SpatialHashBroadphase(double cellSize) {
        this.cellSize = Math.max(16.0, cellSize);
    }

    void rebuild(List<PhysicsBody> bodies) {
        clearBuckets();
        this.occupiedCellCount = 0;
        this.maxBucketSize = 0;
        for (PhysicsBody body : bodies) {
            if (body == null || !body.isEnabled()) {
                continue;
            }
            insert(body);
        }
    }

    void collectCandidates(PhysicsBody body, List<PhysicsBody> target, HashSet<Long> emittedPairs) {
        target.clear();
        if (body == null || !body.isEnabled()) {
            return;
        }

        Bounds2 bounds = body.getWorldBounds();
        int minCellX = toCell(bounds.getMinX());
        int maxCellX = toCell(bounds.getMaxX());
        int minCellY = toCell(bounds.getMinY());
        int maxCellY = toCell(bounds.getMaxY());

        for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
            for (int cellY = minCellY; cellY <= maxCellY; cellY++) {
                ArrayList<PhysicsBody> bucket = this.buckets.get(cellKey(cellX, cellY));
                if (bucket == null) {
                    continue;
                }

                for (PhysicsBody other : bucket) {
                    if (other == body || !other.isEnabled()) {
                        continue;
                    }
                    if (!emittedPairs.add(pairKey(body, other))) {
                        continue;
                    }
                    target.add(other);
                }
            }
        }
    }

    private void insert(PhysicsBody body) {
        Bounds2 bounds = body.getWorldBounds();
        int minCellX = toCell(bounds.getMinX());
        int maxCellX = toCell(bounds.getMaxX());
        int minCellY = toCell(bounds.getMinY());
        int maxCellY = toCell(bounds.getMaxY());

        for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
            for (int cellY = minCellY; cellY <= maxCellY; cellY++) {
                bucketFor(cellKey(cellX, cellY)).add(body);
            }
        }
    }

    private ArrayList<PhysicsBody> bucketFor(long key) {
        ArrayList<PhysicsBody> bucket = this.buckets.get(key);
        if (bucket == null) {
            bucket = new ArrayList<>();
            this.buckets.put(key, bucket);
        }
        if (bucket.isEmpty()) {
            this.touchedKeys.add(key);
            this.occupiedCellCount++;
        }
        this.maxBucketSize = Math.max(this.maxBucketSize, bucket.size() + 1);
        return bucket;
    }

    private void clearBuckets() {
        for (Long key : this.touchedKeys) {
            ArrayList<PhysicsBody> bucket = this.buckets.get(key);
            if (bucket != null) {
                bucket.clear();
            }
        }
        this.touchedKeys.clear();
    }

    int getOccupiedCellCount() {
        return this.occupiedCellCount;
    }

    int getMaxBucketSize() {
        return this.maxBucketSize;
    }

    private int toCell(double coordinate) {
        return (int) Math.floor(coordinate / this.cellSize);
    }

    private static long pairKey(PhysicsBody first, PhysicsBody second) {
        int minId = Math.min(first.getId(), second.getId());
        int maxId = Math.max(first.getId(), second.getId());
        return ((long) minId << 32) ^ (maxId & 0xffffffffL);
    }

    private static long cellKey(int cellX, int cellY) {
        return ((long) cellX << 32) ^ (cellY & 0xffffffffL);
    }
}
