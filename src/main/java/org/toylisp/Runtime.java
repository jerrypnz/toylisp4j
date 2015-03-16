package org.toylisp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime <br/>
 *
 * @author jerry created 14/11/26
 */
public class Runtime {

    static final Symbol DO = Symbol.intern("do");
    static final Symbol COND = Symbol.intern("cond");
    static final Symbol LAMBDA = Symbol.intern("lambda");
    static final Symbol QUOTE = Symbol.intern("quote");
    static final Symbol DEF = Symbol.intern("def");
    static final Symbol DEFMACRO = Symbol.intern("defmacro");

    public static Cons cons(Object car, Object cdr) {
        return new Cons(car, cdr);
    }

    public static boolean bool(Object obj) {
        return obj != null && !obj.equals(Boolean.FALSE);
    }

    public static Env getRootEnv() {
        return rootEnv;
    }

    private static void ensureArity(String name, int expected, Cons args) {
        int n = 0;
        while (args != null) {
            n++;
            args = (Cons) args.cdr();
        }
        ensureArity(name, expected, n);
    }

    private static void ensureArity(String name, int expected, int actual) {
        if (expected != actual) {
            throw new IllegalArgumentException(name + ": expect " + expected + " args, " + actual + " given");
        }
    }

    public static Object eval(Object form, Env env) {
        if (form instanceof Symbol) {
            return env.get((Symbol) form);
        } else if (form instanceof Cons) {
            Cons cons = (Cons) form;
            Object operator = cons.car();
            Cons params = (Cons) cons.cdr();
            SpecialForm specialForm;
            if (operator instanceof Symbol &&
                (specialForm = SpecialForm.getSpecialForm((Symbol) operator)) != null) {
                return specialForm.run(params, env);
            } else {
                // function call or macro
                IFunc func = (IFunc) eval(operator, env);
                if (func instanceof IMacro) {
                    return eval(macroExpand(func, params), env);
                } else {
                    return callFunc(func, params, env);
                }
            }
        } else {
            // Everything else evaluates to itself.
            return form;
        }
    }

    private static Object callFunc(IFunc func, Cons params, Env env) {List<Object> args = new ArrayList<>();
        while (params != null) {
            // eval arguments
            args.add(eval(params.car(), env));
            params = (Cons) params.cdr();
        }
        return func.invoke(args.toArray());
    }

    private static Object macroExpand(IFunc macro, Cons params) {
        List<Object> args = new ArrayList<>();
        while (params != null) {
            // eval arguments
            args.add(params.car());
            params = (Cons) params.cdr();
        }
        return macro.invoke(args.toArray());
    }

    // Basic functions
    static final IFunc cons = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            ensureArity("cons", 2, args.length);
            return cons(args[0], args[1]);
        }
    };

    static final IFunc car = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            ensureArity("car", 1, args.length);
            return ((Cons) args[0]).car();
        }
    };

    static final IFunc cdr = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            ensureArity("cdr", 1, args.length);
            return ((Cons) args[0]).cdr();
        }
    };

    static final IFunc list = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            if (args.length == 0) {
                return null;
            }
            return Cons.fromList(Arrays.asList(args));
        }
    };

    static final IFunc concat = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            return Cons.concat(args);
        }
    };

    static final IFunc eq = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            ensureArity("eq", 2, args.length);
            return (args[0] instanceof Symbol) &&
                   (args[1] instanceof Symbol) &&
                   (args[0] == args[1]);
        }
    };

    static final IFunc equal = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            ensureArity("eq", 2, args.length);
            if (args[0] == null) {
                if (args[1] == null) {
                    return true;
                }
            } else {
                return args[0].equals(args[1]);
            }
            return false;
        }
    };

    static final IFunc prn = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            StringBuilder msg = new StringBuilder(64);
            for (Object arg : args) {
                msg.append(arg);
            }
            System.out.println(msg.toString());
            return null;
        }
    };

    static final IFunc plus = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            if (args.length == 0) {
                return new BigDecimal(0);
            }
            BigDecimal res = (BigDecimal) args[0];
            for (int i = 1; i < args.length; i++) {
                res = res.add((BigDecimal) args[i]);
            }
            return res;
        }
    };

    static final IFunc minus = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            if (args.length == 0) {
                return new BigDecimal(0);
            }
            BigDecimal res = (BigDecimal) args[0];
            for (int i = 1; i < args.length; i++) {
                res = res.subtract((BigDecimal) args[i]);
            }
            return res;
        }
    };

    static final IFunc multiply = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            if (args.length == 0) {
                return new BigDecimal(1);
            }
            BigDecimal res = (BigDecimal) args[0];
            for (int i = 1; i < args.length; i++) {
                res = res.multiply((BigDecimal) args[i]);
            }
            return res;
        }
    };

    static final IFunc divide = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            if (args.length == 0) {
                return new BigDecimal(1);
            }
            BigDecimal res = (BigDecimal) args[0];
            for (int i = 1; i < args.length; i++) {
                res = res.divideToIntegralValue((BigDecimal) args[i]);
            }
            return res;
        }
    };

    static final IFunc macroexpand = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            ensureArity("eq", 1, args.length);
            Object form = args[0];
            if (!(form instanceof Cons)) {
                return form;
            }
            Cons cons = (Cons) form;
            Object operator = cons.car();
            Cons params = (Cons) cons.cdr();
            if (operator instanceof Symbol &&
                (SpecialForm.getSpecialForm((Symbol) operator)) == null) {
                IFunc func = (IFunc) eval(operator, getRootEnv());
                if (func instanceof IMacro) {
                    return macroExpand(func, params);
                }
            }
            return form;
        }
    };

    private static List<Symbol> getArgNames(Cons args) {
        List<Symbol> argNames = new ArrayList<>();
        while (args != null) {
            argNames.add((Symbol) args.car());
            args = (Cons) args.cdr();
        }
        return argNames;
    }

    // Special Forms
    enum SpecialForm {

        _cond(COND) {
            @Override
            Object run(Cons args, Env env) {
                if (args == null) {
                    throw new IllegalStateException("cond: no clause found");
                }
                while (args != null) {
                    Cons clause = (Cons) args.car();
                    Object pred = clause.car();
                    Cons expr = (Cons) clause.cdr();
                    if (clause == null || pred == null || expr == null || expr.cdr() != null) {
                        throw new IllegalArgumentException("cond: invalid clause");
                    }
                    if (bool(eval(pred, env))) {
                        return eval(expr.car(), env);
                    }
                    args = (Cons) args.cdr();
                }
                return null;
            }
        },

        _def(DEF) {
            @Override
            Object run(Cons args, Env env) {
                ensureArity("def", 2, args);

                Symbol name = (Symbol) args.car();
                Object form = ((Cons) args.cdr()).car();

                Object obj = eval(form, env);
                getRootEnv().set(name, obj);
                return obj;
            }
        },

        _quote(QUOTE) {
            @Override
            Object run(Cons args, Env env) {
                if (args.cdr() != null) {
                    throw new IllegalArgumentException("Can only quote one argument");
                }
                return args.car();
            }
        },

        _lambda(LAMBDA) {
            @Override
            Object run(Cons definition, Env env) {
                List<Symbol> argNames = getArgNames((Cons) definition.car());
                Cons body = cons(DO, definition.cdr());
                return new Func(Collections.unmodifiableList(argNames), body, env);
            }
        },

        _defmacro(DEFMACRO) {
            @Override
            Object run(Cons definition, Env env) {
                Symbol name = (Symbol) definition.car();
                Cons argsBody = (Cons) definition.cdr();
                List<Symbol> argNames = getArgNames((Cons) argsBody.car());
                Cons body = cons(DO, argsBody.cdr());
                Macro macro = new Macro(Collections.unmodifiableList(argNames), body, env);
                getRootEnv().set(name, macro);
                return macro;
            }
        },

        _do(DO) {
            @Override
            Object run(Cons args, Env env) {
                Object ret = null;
                while (args != null) {
                    ret = Runtime.eval(args.car(), env);
                    args = (Cons) args.cdr();
                }
                return ret;
            }
        };

        private static final Map<Symbol, SpecialForm> specialForms = new IdentityHashMap<>();

        static {
            for (SpecialForm form : SpecialForm.values()) {
                specialForms.put(form.operator, form);
            }
        }

        final Symbol operator;

        SpecialForm(Symbol operator) {this.operator = operator;}

        abstract Object run(Cons args, Env env);

        public static SpecialForm getSpecialForm(Symbol operator) {
            return specialForms.get(operator);
        }
    }

    static final Env rootEnv = Env.createRoot()
                                  .set(Symbol.intern("cons"), cons)
                                  .set(Symbol.intern("car"), car)
                                  .set(Symbol.intern("cdr"), cdr)
                                  .set(Symbol.intern("list"), list)
                                  .set(Symbol.intern("concat"), concat)
                                  .set(Symbol.intern("eq?"), eq)
                                  .set(Symbol.intern("="), equal)
                                  .set(Symbol.intern("prn"), prn)
                                  .set(Symbol.intern("+"), plus)
                                  .set(Symbol.intern("-"), minus)
                                  .set(Symbol.intern("*"), multiply)
                                  .set(Symbol.intern("/"), divide)
                                  .set(Symbol.intern("macroexpand"), macroexpand)
                                  .set(Symbol.intern("t"), Boolean.TRUE);

}
