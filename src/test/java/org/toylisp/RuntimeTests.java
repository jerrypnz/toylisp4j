package org.toylisp;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.toylisp.TestUtil._;

public class RuntimeTests {

    @Test
    public void testBool() {
        assertTrue(Runtime.bool("foo"));
        assertTrue(Runtime.bool(new Object()));
        assertTrue(Runtime.bool(1));
        assertTrue(Runtime.bool(0));
        assertFalse(Runtime.bool(null));
        assertFalse(Runtime.bool(false));
        assertFalse(Runtime.bool(Boolean.FALSE));
    }

    @Test
    public void testEvalSymbol() {
        Integer val = 409600;
        Symbol a = Symbol.intern("a");
        Env env = Env.createRoot().set(a, val);
        assertEquals(val, Runtime.eval(a, env));
    }

    @Test
    public void testEvalOtherAtoms() {
        Integer val = 409600;
        String foo = "foobar";
        Env env = Env.createRoot();
        assertEquals(val, Runtime.eval(val, env));
        assertEquals(foo, Runtime.eval(foo, env));
    }

    @Test
    public void testFunctionCall() {
        Symbol identity = Symbol.intern("identity");
        Symbol arg = Symbol.intern("arg");
        Env env = Env.createRoot();
        Env funcEnv = env.push();
        IFunc identityFunc = new Func(Arrays.asList(arg), arg, funcEnv);

        env.set(identity, identityFunc);

        assertEquals("foobar", Runtime.eval(_(identity, "foobar"), env));
        assertEquals(42, Runtime.eval(_(identity, 42), env));
    }

    @Test
    public void testClosure() {
        Symbol foo = Symbol.intern("foo");
        Symbol v = Symbol.intern("v");
        Env env = Env.createRoot();
        Env funcEnv = env.push();
        funcEnv.set(v, "hello world");
        IFunc closure = new Func(Collections.<Symbol>emptyList(), v, funcEnv);

        env.set(foo, closure);

        assertEquals("hello world", Runtime.eval(_(foo), env));
    }

    @Test
    public void testLambdaIdentity() {
        Symbol lambda = Symbol.intern("lambda");
        Symbol arg = Symbol.intern("arg");

        Env env = Runtime.createRootEnv();
        Cons progn = _(_(lambda,
                         _(arg),
                         arg),
                       "foobar");
        assertEquals("foobar", Runtime.eval(progn, env));
    }

    @Test
    public void testCarCdr() {
        Symbol car = Symbol.intern("car");
        Symbol cdr = Symbol.intern("cdr");
        Symbol quote = Symbol.intern("quote");

        Cons data = _("foo", "bar");

        Object expectedCar = data.car();
        Object expectedCdr = data.cdr();

        Env env = Runtime.createRootEnv();

        assertEquals(expectedCar, Runtime.eval(_(car, _(quote, data)), env));
        assertEquals(expectedCdr, Runtime.eval(_(cdr, _(quote, data)), env));
    }


    @Test
    public void testLambdaCadr() {
        Symbol lambda = Symbol.intern("lambda");
        Symbol arg = Symbol.intern("arg");
        Symbol car = Symbol.intern("car");
        Symbol cdr = Symbol.intern("cdr");
        Symbol quote = Symbol.intern("quote");

        Env env = Runtime.createRootEnv();
        Cons code = _(_(lambda, _(arg),
                         _(car, _(cdr, arg))),
                       _(quote, _("foo", "bar")));
        assertEquals("bar", Runtime.eval(code, env));
    }

    @Test
    public void testCond() {
        Symbol lambda = Symbol.intern("lambda");
        Symbol cond = Symbol.intern("cond");
        Symbol eq = Symbol.intern("eq");
        Symbol quote = Symbol.intern("quote");
        Symbol x = Symbol.intern("x");
        Symbol t = Symbol.intern("t");
        Symbol a = Symbol.intern("a");
        Symbol b = Symbol.intern("b");

        Env env = Runtime.createRootEnv();

        Cons condFunc = _(lambda, _(x),
                          _(cond,
                            _(eq, x, _(quote, a)), "foo",
                            _(eq, x, _(quote, b)), "bar",
                            t, "oops"));
        assertEquals("foo", Runtime.eval(_(condFunc, _(quote, a)), env));
        assertEquals("bar", Runtime.eval(_(condFunc, _(quote, b)), env));
        assertEquals("oops", Runtime.eval(_(condFunc, "nop"), env));

    }

    @Test
    public void testCondWithNilValue() {
        Symbol cond = Symbol.intern("cond");
        Symbol t = Symbol.intern("t");
        Env env = Runtime.createRootEnv();

        assertNull(Runtime.eval(_(cond, t, null), env));
    }

}