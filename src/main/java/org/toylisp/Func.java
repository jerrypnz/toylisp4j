package org.toylisp;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Func <br/>
 *
 * @author jerry created 14/11/26
 */
public class Func implements IFunc {

    static final Symbol AND = Symbol.intern("&");

    private final List<Symbol> argNames;
    private final Symbol restArgsName;
    private final Object body;
    private final Env closureEnv;

    public Func(List<Symbol> argNames, Object body, Env env) {
        Symbol restArgsName = null;
        int i = argNames.indexOf(AND);
        if (i >= 0) {
            // There can only be one rest arg
            // eg. (bindings & body) is valid, (bindings & body1 body2) is not.
            if (i != argNames.size() - 2) {
                throw new IllegalArgumentException("Invalid rest arg declaration");
            }
            restArgsName = argNames.get(i + 1);
            argNames = argNames.subList(0, i);
        }
        this.argNames = argNames;
        this.restArgsName = restArgsName;
        this.body = body;
        this.closureEnv = env;
    }

    @Override
    public Object invoke(Object... args) {
        Env newEnv = closureEnv.push();
        if (args.length < argNames.size()) {
            throw new IllegalArgumentException("Wrong arity: " +
                                               argNames.size() +
                                               " args expected, " +
                                               args.length +
                                               " given.");
        }

        for (int i = 0; i < argNames.size(); i++) {
            newEnv.set(argNames.get(i), args[i]);
        }

        if (restArgsName != null) {
            newEnv.set(restArgsName, Cons.fromList(asList(args).subList(argNames.size(), args.length)));
        }
        return Runtime.eval(body, newEnv);
    }

}
