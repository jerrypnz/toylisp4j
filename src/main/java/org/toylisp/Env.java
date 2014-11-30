package org.toylisp;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment <br/>
 *
 * @author jerry created 14/11/26
 */
public class Env {

    private final Env parent;
    private final Map<Symbol, Object> bindings = new HashMap<>();

    private Env(Env parent) {this.parent = parent;}

    public static Env createRoot() {
        return new Env(null);
    }

    public Env push() {
        return new Env(this);
    }

    public Env pop() {
        return this.parent;
    }

    public Env set(Symbol name, Object val) {
        if (bindings.containsKey(name)) {
            bindings.put(name, val);
        } else if (parent != null) {
            parent.set(name, val);
        } else {
            bindings.put(name, val);
        }
        return this;
    }

    public Object get(Symbol name) {
        if (bindings.containsKey(name)) {
            return bindings.get(name);
        } else if (parent != null) {
            return parent.get(name);
        }
        throw new IllegalStateException("Symbol " + name + " not found");
    }

}
