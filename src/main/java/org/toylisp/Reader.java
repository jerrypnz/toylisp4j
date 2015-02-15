package org.toylisp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Lisp Reader <br/>
 *
 * @author jerry created 14/11/29
 */
public class Reader {

    static final Symbol QUOTE = Symbol.intern("quote");
    static final Symbol CONCAT = Symbol.intern("concat");
    static final Symbol LIST = Symbol.intern("list");

    private static void finishToken(StringBuilder token, List<String> tokens) {
        if (token.length() > 0) {
            tokens.add(token.toString());
            token.delete(0, token.length());
        }
    }

    public static List<String> tokenize(String input) {
        StringBuilder currentToken = new StringBuilder();
        List<String> tokens = new ArrayList<>(256);

        boolean isInStr = false;
        boolean isInComment = false;
        char c = 0;
        char c0;
        for (int i = 0; i < input.length(); i++) {
            c0 = c;
            c = input.charAt(i);
            if (isInStr) {
                currentToken.append(c);
                if (c == '"' && c0 != '\\') {
                    finishToken(currentToken, tokens);
                    isInStr = false;
                }
            } else if (isInComment) {
                if (c == '\r' || c == '\n') {
                    isInComment = false;
                }
            } else {
                switch (c) {
                    case '"':
                        finishToken(currentToken, tokens);
                        currentToken.append(c);
                        isInStr = true;
                        break;

                    case ';':
                        isInComment = true;
                        break;

                    case ',':
                        finishToken(currentToken, tokens);
                        currentToken.append(c);
                        // Look ahead to see if it is ",@".
                        if (i < input.length() - 1 && input.charAt(i + 1) == '@') {
                            currentToken.append('@');
                            i++;
                        }
                        finishToken(currentToken, tokens);
                        break;

                    case '\'':
                    case '`':
                    case '(':
                    case ')':
                        finishToken(currentToken, tokens);
                        currentToken.append(c);
                        finishToken(currentToken, tokens);
                        break;

                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        finishToken(currentToken, tokens);
                        break;

                    default:
                        currentToken.append(c);
                }

            }
        }

        finishToken(currentToken, tokens);
        return tokens;
    }

    public static List<Object> read(String input) {
        return read(tokenize(input));
    }

    public static List<Object> read(List<String> tokens) {
        List<Object> results = new ArrayList<>();
        Iterator<String> tokenIter = tokens.iterator();
        while (tokenIter.hasNext()) {
            String currentToken = tokenIter.next();
            results.add(read(currentToken, tokenIter, false));
        }
        return results;
    }

    static Object read(String currentToken, Iterator<String> tokenIterator, boolean isInBackQuote) {
        if (currentToken.equals(")")) {
            throw new IllegalArgumentException("Unmatched parentheses: unexpected )");
        }
        Character firstChar = currentToken.charAt(0);
        ObjectReader reader = readers.get(firstChar);
        if (reader != null) {
            return reader.readObj(currentToken, tokenIterator, isInBackQuote);
        } else if (Character.isDigit(firstChar)) {
            return ObjectReader.NUMBER_READER.readObj(currentToken, tokenIterator, isInBackQuote);
        } else {
            return ObjectReader.SYMBOL_READER.readObj(currentToken, tokenIterator, isInBackQuote);
        }
    }

    private static final Map<Character, ObjectReader> readers;

    static {
        Map<Character, ObjectReader> readersMap = new HashMap<>();
        readersMap.put('"', ObjectReader.STRING_READER);
        readersMap.put('(', ObjectReader.SEXP_READER);
        readersMap.put('\'', ObjectReader.QUOTE_READER);
        readersMap.put('`', ObjectReader.BACKQUOTE_READER);
        readersMap.put(',', ObjectReader.UNQUOTE_READER);
        readers = Collections.unmodifiableMap(readersMap);
    }

    static enum ObjectReader {

        STRING_READER {
            @Override
            Object readObj(String currentToken, Iterator<String> tokenIterator, boolean inBackQuote) {
                StringBuilder val = new StringBuilder();
                boolean escaping = false;
                // Ignore quotes
                for (int i = 1; i < currentToken.length() - 1; i++) {
                    char c = currentToken.charAt(i);
                    if (escaping) {
                        switch (c) {
                            case 'n':
                                val.append('\n');
                                break;

                            case 'r':
                                val.append('\r');
                                break;

                            case 't':
                                val.append('\t');
                                break;

                            default:
                                val.append(c);
                        }
                        escaping = false;
                    } else {
                        if (c == '\\') {
                            escaping = true;
                        } else {
                            val.append(c);
                        }
                    }
                }

                return val.toString();
            }
        },

        NUMBER_READER {
            @Override
            Object readObj(String currentToken, Iterator<String> tokenIterator, boolean inBackQuote) {
                return new BigDecimal(currentToken);
            }
        },

        SYMBOL_READER {
            @Override
            Object readObj(String currentToken, Iterator<String> tokenIterator, boolean inBackQuote) {
                if (currentToken.equals("nil") || currentToken.equals("null")) {
                    return null;
                } else {
                    Symbol symbol = Symbol.intern(currentToken);
                    return inBackQuote ? new Cons(QUOTE, new Cons(symbol, null)) : symbol;
                }
            }
        },

        SEXP_READER {
            @Override
            Object readObj(String currentToken, Iterator<String> tokenIterator, boolean inBackQuote) {
                List<Object> objs = new ArrayList<>();
                if (inBackQuote) {
                    objs.add(CONCAT);
                }
                while (tokenIterator.hasNext()) {
                    currentToken = tokenIterator.next();
                    if (")".equals(currentToken)) {
                        return Cons.fromList(objs);
                    } else {
                        objs.add(read(currentToken, tokenIterator, inBackQuote));
                    }
                }
                throw new IllegalArgumentException("Unmatched parentheses: need ) to match");
            }
        },

        BACKQUOTE_READER {
            @Override
            Object readObj(String currentToken, Iterator<String> tokenIterator, boolean inBackQuote) {
                currentToken = tokenIterator.next();
                return read(currentToken, tokenIterator, true);
            }
        },

        UNQUOTE_READER {
            @Override
            Object readObj(String currentToken, Iterator<String> tokenIterator, boolean inBackQuote) {
                if (!tokenIterator.hasNext()) {
                    throw new IllegalArgumentException("No argument found for unquote");
                }
                String nextToken = tokenIterator.next();
                Object obj = read(nextToken, tokenIterator, false);
                if (currentToken.contains("@")) {
                    return obj;
                } else {
                    return new Cons(LIST, new Cons(obj, null));
                }
            }
        },

        QUOTE_READER {
            @Override
            Object readObj(String currentToken, Iterator<String> tokenIterator, boolean inBackQuote) {
                if (!tokenIterator.hasNext()) {
                    throw new IllegalArgumentException("No argument found for quote");
                }
                return new Cons(QUOTE, new Cons(read(tokenIterator.next(), tokenIterator, inBackQuote), null));
            }
        };

        abstract Object readObj(String currentToken, Iterator<String> tokenIterator, boolean inBackQuote);
    }


}
