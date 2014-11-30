package org.toylisp;

import java.util.Arrays;

/**
 * TestUtil <br/>
 *
 * @author jerry created 14/11/29
 */
public class TestUtil {

    /**
     * Short method to easily construct cons cells
     */
    public static Cons _(Object... args) {
        return Cons.fromList(Arrays.asList(args));
    }
}
