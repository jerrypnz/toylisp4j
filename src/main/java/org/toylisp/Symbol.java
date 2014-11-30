package org.toylisp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Symbol <br/>
 *
 * @author jerry created 14/11/26
 */
public class Symbol {

    private static ConcurrentMap<String, Symbol> allSymbols = new ConcurrentHashMap<>(128);

    private final String name;

    private Symbol(String name) {this.name = name;}

    public static Symbol intern(String name) {
        Symbol sym = allSymbols.get(name);
        if (sym == null) {
            Symbol newSymbol = new Symbol(name);
            if ((sym = allSymbols.putIfAbsent(name, newSymbol)) == null) {
                sym = newSymbol;
            }
        }
        return sym;
    }

    @Override
    public String toString() {
        return name;
    }

}
