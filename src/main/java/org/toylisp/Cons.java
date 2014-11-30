package org.toylisp;

import java.util.LinkedList;
import java.util.List;

/**
 * Cons <br/>
 *
 * @author jerry created 14/11/26
 */
public class Cons {

    private final Object car;
    private final Object cdr;

    public Cons(Object car, Object cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    public static Cons fromList(List<Object> list) {
        Object head = list.get(0);
        List<Object> tail = list.subList(1, list.size());
        if (tail.isEmpty()) {
            return new Cons(head, null);
        } else {
            return new Cons(head, fromList(tail));
        }
    }

    public List<Object> toList() {
        Cons tail = this;
        List<Object> list = new LinkedList<>();
        while (tail != null) {
            list.add(tail.car);
            tail = ((Cons) tail.cdr());
        }
        return list;
    }

    public Object car() {
        return car;
    }

    public Object cdr() {
        return cdr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Cons cons = (Cons) o;

        return !(car != null ? !car.equals(cons.car) : cons.car != null) &&
               !(cdr != null ? !cdr.equals(cons.cdr) : cons.cdr != null);

    }

    @Override
    public int hashCode() {
        int result = car != null ? car.hashCode() : 0;
        result = 31 * result + (cdr != null ? cdr.hashCode() : 0);
        return result;
    }

    private void appendToStr(StringBuilder buf) {
        buf.append(car.toString());
        Object tail = cdr;
        while (tail != null) {
            buf.append(' ');
            if (tail instanceof Cons) {
                Cons cons = (Cons) tail;
                buf.append(cons.car());
                tail = cons.cdr();
            } else {
                buf.append(tail.toString());
                tail = null;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(32);
        buf.append('(');
        appendToStr(buf);
        buf.append(')');
        return buf.toString();
    }
}
