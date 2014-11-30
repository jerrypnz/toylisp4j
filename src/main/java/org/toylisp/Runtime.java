package org.toylisp;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    static Symbol DO = Symbol.intern("do");
    static Symbol COND = Symbol.intern("cond");
    static Symbol LAMBDA = Symbol.intern("lambda");
    static Symbol QUOTE = Symbol.intern("quote");
    static Symbol DEF = Symbol.intern("def");

    public static Cons cons(Object car, Object cdr) {
        return new Cons(car, cdr);
    }

    public static boolean bool(Object obj) {
        return obj != null && !obj.equals(Boolean.FALSE);
    }

    public static Env createRootEnv() {
        return Env.createRoot()
                  .set(Symbol.intern("cons"), cons)
                  .set(Symbol.intern("car"), car)
                  .set(Symbol.intern("cdr"), cdr)
                  .set(Symbol.intern("eq?"), eq)
                  .set(Symbol.intern("+"), plus)
                  .set(Symbol.intern("-"), minus)
                  .set(Symbol.intern("*"), multiply)
                  .set(Symbol.intern("/"), divide)
                  .set(Symbol.intern("t"), Boolean.TRUE);
    }

    private static void ensureArity(String name, int expected, Cons args) {
        int n = 0;
        while (args != null) {
            n++;
            args = (Cons)args.cdr();
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
                // function call
                IFunc func = (IFunc) eval(operator, env);
                List<Object> args = new ArrayList<>();
                while (params != null) {
                    // eval arguments
                    args.add(eval(params.car(), env));
                    params = (Cons) params.cdr();
                }
                return func.invoke(args.toArray());
            }
        } else {
            // Everything else evaluates to itself.
            return form;
        }
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

    static final IFunc eq = new IFunc() {
        @Override
        public Object invoke(Object... args) {
            ensureArity("eq", 2, args.length);
            return (args[0] instanceof Symbol) &&
                   (args[1] instanceof Symbol) &&
                   (args[0] == args[1]);
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

    // Special Forms
    enum SpecialForm {

        _cond(COND) {
            @Override
            Object run(Cons args, Env env) {
                List<Object> forms = args.toList();
                if (forms.size() == 0 || forms.size() % 2 != 0) {
                    throw new IllegalArgumentException("cond: invalid args(need even number of args)");
                }
                for (int i = 0; i < forms.size(); i += 2) {
                    if (bool(eval(forms.get(i), env))) {
                        return eval(forms.get(i + 1), env);
                    }
                }
                return null;
            }
        },

        _def(DEF) {
            @Override
            Object run(Cons args, Env env) {
                ensureArity("def", 2, args);
                Env curEnv = env;
                while (env.pop() != null) {
                    env = env.pop();
                }
                Symbol name = (Symbol) args.car();
                Object form = ((Cons) args.cdr()).car();

                Object obj = eval(form, curEnv);
                env.set(name, obj);
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
                Cons args = (Cons) definition.car();
                Cons body = cons(DO, definition.cdr());
                List<Symbol> argNames = new ArrayList<>();
                while (args != null) {
                    argNames.add((Symbol) args.car());
                    args = (Cons) args.cdr();
                }
                return new Func(Collections.unmodifiableList(argNames), body, env);
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

}
