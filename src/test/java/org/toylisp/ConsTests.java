package org.toylisp;

import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.toylisp.Runtime.cons;
import static org.toylisp.TestUtil._;

public class ConsTests {

    @Test
    public void testConsToString() {
        Cons test = cons(cons("a", cons("b", null)), cons(1, cons(2, cons(3, null))));
        assertEquals("((a b) 1 2 3)", test.toString());
    }

    @Test
    public void testToList() {
        Cons cons = _("1", "2", "3", 4, 5, 6);
        assertEquals(Arrays.<Object>asList("1", "2", "3", 4, 5, 6), cons.toList());
        cons = _("1", "2", null);
        assertEquals(Arrays.<Object>asList("1", "2", null), cons.toList());
    }

    @Test
    public void testFromList() {
        Cons test = cons("a", cons(null, null));
        assertEquals(test, Cons.fromList(Arrays.asList((Object)"a", null)));
        System.out.println(test);
    }

}