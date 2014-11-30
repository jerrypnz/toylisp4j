package org.toylisp;

import java.util.List;

/**
 * Func <br/>
 *
 * @author jerry created 14/11/26
 */
public class Func implements IFunc {

    private final List<Symbol> argNames;
    private final Object body;
    private final Env closureEnv;

    public Func(List<Symbol> argNames, Object body, Env env) {
        this.argNames = argNames;
        this.body = body;
        this.closureEnv = env;
    }

    @Override
    public Object invoke(Object... args) {
        Env newEnv = closureEnv.push();
        if (args.length != argNames.size()) {
            throw new IllegalArgumentException("Wrong arity: " +
                                               argNames.size() +
                                               " args expected, " +
                                               args.length +
                                               " given.");
        }

        for (int i = 0; i < args.length; i++) {
            newEnv.set(argNames.get(i), args[i]);
        }
        return Runtime.eval(body, newEnv);
    }

}
