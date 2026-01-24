package gwtjava.util;

import java.util.HashSet;

public class BitSet extends HashSet<Integer> {
    private static final long serialVersionUID = -5738841052284866475L;

    public BitSet(int max) {
        super(max);
    }

    public boolean get(Integer i) {
        return contains(i);
    }

    public void set(Integer i) {
        add(i);
    }
}
