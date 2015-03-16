package org.toylisp;

import java.util.List;

/**
 * Macro is essentially a function with an IMacro marker <br/>
 *
 * @author jerry created 18/02/15
 */
public class Macro extends Func implements IMacro {

    public Macro(List<Symbol> argNames, Object body, Env env) {
        super(argNames, body, env);
    }

}
