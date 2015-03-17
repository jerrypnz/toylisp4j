package org.toylisp;

import java.math.BigDecimal;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.toylisp.Reader.tokenize;
import static org.toylisp.TestUtil._;

public class ReaderTests {

    @Test
    public void testTokenize() throws Exception {
        // Basic
        assertEquals(asList("a"), tokenize("a"));
        assertEquals(asList("a", "b"), tokenize("a b"));
        assertEquals(asList("(", "a", ")"), tokenize("(a)"));
        assertEquals(asList("(", "a", "b", ")"), tokenize("(a b)"));

        // Strings
        assertEquals(asList("(", "a", "\"foo bar\"", ")"), tokenize("(a \"foo bar\")"));
        assertEquals(asList("(", "a", "\"foo bar \\\"hello\\\"\"", ")"), tokenize("(a \"foo bar \\\"hello\\\"\")"));

        // Quotes
        assertEquals(asList("'", "(", "a", "b", "c", ")"), tokenize("'(a b c)"));

        // Comments
        assertEquals(asList("(", "a", ")"), tokenize(";comment\r(a)"));
        assertEquals(asList("(", "a", ")"), tokenize(";comment\n(a)"));
        assertEquals(asList("(", "a", ")"), tokenize(";comment\r\n(a)"));
        assertEquals(asList("(", "a", "b", ")"), tokenize(";comment1\n;comment2\n(a b)"));

        // Backquote
        assertEquals(asList("`", "(", "a", "b", "c", ")"), tokenize("`(a b c)"));
        assertEquals(asList("`", "(", "a", ",", "b", "c", ")"), tokenize("`(a ,b c)"));
        assertEquals(asList("`", "(", "a", ",@", "b", "c", ")"), tokenize("`(a ,@b c)"));
        assertEquals(asList("`", "(", "a", ",@", "(", "b", "1", "2", ")", "c", ")"), tokenize("`(a ,@(b 1 2) c)"));
    }

    @Test
    public void testTokenToObject() throws Exception {
        assertEquals(new BigDecimal("14"), Reader.read("14", null, false));
        assertEquals("14", Reader.read("\"14\"", null, false));
        assertEquals(Symbol.intern("foo"), Reader.read("foo", null, false));
        assertEquals("foo\r\ncol1\tcol2\t hello", Reader.read("\"foo\\r\\ncol1\\tcol2\\t hello\"", null, false));
    }

    @Test
    public void testRead_NormalCases() throws Exception {
        Symbol foobar = Symbol.intern("foobar");
        assertEquals(asList((Object)_(foobar, "1", "2")), Reader.read(asList("(", "foobar", "\"1\"", "\"2\"", ")")));
    }

    @Test
    public void testRead_Quotes() throws Exception {
        Symbol quote = Symbol.intern("quote");
        Symbol foobar = Symbol.intern("foobar");
        assertEquals(asList((Object) _(quote, foobar)), Reader.read(asList("'", "foobar")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRead_UnmatchedParentheses1() throws Exception {
        Reader.read(asList("("));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRead_UnmatchedParentheses2() throws Exception {
        Reader.read(asList(")"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRead_UnmatchedParentheses3() throws Exception {
        Reader.read(asList("(", "foo", "bar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRead_UnmatchedParentheses4() throws Exception {
        Reader.read(asList("bar", "foo", ")"));
    }

    @Test
    public void testReadString() throws Exception {
        Symbol lambda = Symbol.intern("lambda");
        Symbol quote = Symbol.intern("quote");
        Symbol arg = Symbol.intern("arg");
        Symbol car = Symbol.intern("car");
        Symbol cdr = Symbol.intern("cdr");
        Symbol a = Symbol.intern("a");
        Symbol b = Symbol.intern("b");
        Symbol c = Symbol.intern("c");
        Symbol def = Symbol.intern("def");

        assertEquals(asList((Object) //To avoid warnings
                        _(_(lambda, _(arg), _(car, _(cdr, arg))), _(quote, _(a, b, c))),
                        _(def, a, "foo bar"),
                        _(def, b, new BigDecimal("12345"))
                     ),
                     Reader.read("((lambda (arg) (car (cdr arg))) '(a b c))\n" +
                                 "(def a \"foo bar\")\n" +
                                 "(def b 12345)"));

        // Null
        assertEquals(null, Reader.read("null").get(0));
        assertEquals(_(a, b, null), Reader.read("(a b nil)").get(0));
    }

    static final Symbol quote = Symbol.intern("quote");
    static final Symbol concat = Symbol.intern("concat");
    static final Symbol list = Symbol.intern("list");

    @Test
    public void testBackquote() throws Exception {
        Symbol a = Symbol.intern("a");
        Symbol b = Symbol.intern("b");
        Symbol c = Symbol.intern("c");
        Symbol e = Symbol.intern("e");
        Symbol f = Symbol.intern("f");
        assertEquals(_(concat, _(list, _(quote, a)), _(list, b), c), Reader.read("`(a ,b ,@c)").get(0));
        assertEquals(_(concat,
                       _(list, _(quote, e)),
                       _(list, _(quote, f)),
                       _(list, _(concat, _(list, _(quote, a)), _(list, b), c))),
                     Reader.read("`(e f (a ,b ,@c))").get(0));
    }

}