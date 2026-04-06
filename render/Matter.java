package render;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import core.EntitiesGraph;
import core.Params;

/*
The render package is responsible for rendering and handling physics of the dynamics.
Matter consists of the entites and relations which came together for creating a specific context.
This context should have a metadata for rendering purposes.

The EntitiesGraph structure is more strict. Stricter than the one in the universe.
In here, its purpose is to create a real Matter.
A mattter is consisted of compounds and compounds are consisted of atoms.
In here, the context is much more about atoms but in Universe.java the context is more about compounds.
*/

public class Matter extends EntitiesGraph implements Iterable<Map.Entry<String, Params>> {

    private final LinkedHashMap<String, Params> renderParams = new LinkedHashMap<>();

    public Matter(int group_id, String group_label) {
        this.setId(group_id);
        this.setLabel(group_label);
    }

    public void addRenderParam(String key, Params value) {
        this.renderParams.put(key, value);
    }

    public Params getRenderParam(String key) {
        return this.renderParams.get(key);
    }

    public Params removeRenderParam(String key) {
        return this.renderParams.remove(key);
    }

    public LinkedHashMap<String, Params> getRenderParams() {
        return this.renderParams;
    }

    @Override
    public Iterator<Map.Entry<String, Params>> iterator() {
        return this.renderParams.entrySet().iterator();
    }
}
