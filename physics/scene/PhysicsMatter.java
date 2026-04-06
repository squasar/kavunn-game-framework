package physics.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import core.EntitiesGraph;
import core.Params;
import physics.body.PhysicsBody;

public class PhysicsMatter extends EntitiesGraph implements Iterable<Map.Entry<String, Params>> {

    private final LinkedHashMap<String, Params> physicsParams = new LinkedHashMap<>();
    private final List<PhysicsBody> bodies = new ArrayList<>();
    private final List<PhysicsBody> bodiesView = Collections.unmodifiableList(this.bodies);

    public PhysicsMatter(int id, String label) {
        setId(id);
        setLabel(label);
    }

    public void addPhysicsParam(String key, Params value) {
        this.physicsParams.put(key, value);
    }

    public Params getPhysicsParam(String key) {
        return this.physicsParams.get(key);
    }

    public Params removePhysicsParam(String key) {
        return this.physicsParams.remove(key);
    }

    public LinkedHashMap<String, Params> getPhysicsParams() {
        return this.physicsParams;
    }

    public void addBody(PhysicsBody body) {
        this.bodies.add(body);
    }

    public void removeBody(PhysicsBody body) {
        this.bodies.remove(body);
    }

    public List<PhysicsBody> getBodies() {
        return this.bodiesView;
    }

    public void appendBodiesTo(List<PhysicsBody> target) {
        target.addAll(this.bodies);
    }

    @Override
    public Iterator<Map.Entry<String, Params>> iterator() {
        return this.physicsParams.entrySet().iterator();
    }
}
