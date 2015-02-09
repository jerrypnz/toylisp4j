package org.toylisp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Lisp Reader <br/>
 *
 * @author jerry created 14/11/29
 */
public class Reader {

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
                        currentToken.append(c);
                        isInStr = true;
                        break;

                    case ';':
                        isInComment = true;
                        break;

                    case '\'':
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
        LinkedList<List<Object>> levels = new LinkedList<>();

        for (String token : tokens) {
            Object obj = null;
            // It is possible we read a nil/null,
            // so we have to use another flag to indicate
            // whether we read something or not
            boolean readValidObj = false;
            switch (token) {
                case "(":
                    levels.push(new ArrayList<>(10));
                    break;
                case "'":
                    List<Object> level = new ArrayList<>(10);
                    level.add(Runtime.QUOTE);
                    levels.push(level);
                    break;
                case ")":
                    if (levels.size() == 0) {
                        throw new IllegalArgumentException("Unmatched parentheses");
                    }
                    obj = Cons.fromList(levels.pop());
                    readValidObj = true;
                    break;
                default:
                    obj = tokenToObject(token);
                    readValidObj = true;
                    break;
            }

            while (levels.size() != 0 && readValidObj) {
                List<Object> level = levels.peek();
                level.add(obj);
                if (level.get(0) == Runtime.QUOTE) {
                    obj = Cons.fromList(level);
                    levels.pop();
                } else {
                    obj = null;
                    readValidObj = false;
                }
            }

            if (readValidObj) {
                results.add(obj);
            }
        }

        if (!levels.isEmpty()) {
            throw new IllegalArgumentException("Unmatched parentheses");
        }

        return results;
    }

    protected static Object tokenToObject(String token) {
        char firstChar = token.charAt(0);
        if (Character.isDigit(firstChar)) {
            return new BigDecimal(token);
        } else if (firstChar == '"') {
            return handleString(token);
        } else if (token.equals("nil") || token.equals("null")) {
            return null;
        } else {
            return Symbol.intern(token);
        }
    }

    protected static String handleString(String token) {
        StringBuilder val = new StringBuilder();
        boolean escaping = false;
        // Ignore quotes
        for (int i = 1; i < token.length() - 1; i++) {
            char c = token.charAt(i);
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

}
