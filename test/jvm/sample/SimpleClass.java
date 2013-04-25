package jvm.sample;

import java.util.HashMap;

@SuppressWarnings("rawtypes")
public class SimpleClass extends HashMap implements Runnable, Interface {
    private static final long serialVersionUID = -6335697124949305633L;
    int x = 2;
    long y = 3;
    static int z;
    public final String CONST = "23";

    static int s = -5;

    public static int invoked(){
        return 5;
    }

    public int method(){
        return 2;
    }

    public int method(int i){
        s++;
        return i;
    }

    public static int factorial(int n){
        if (n == 0)
            return 1;

        return n * factorial(n - 1);
    }

    public static class Inner {
        public static int x = 10;
    }

    public static int main(String args[]) {
        //System.out.println("X");
        return factorial(1) + variable;
    }

    public static int main_(String args[]) {
        int i = 2;
        int j = (int) (i + 3.0);
        int k = j * i;
        z = k;
        SimpleClass ref = new SimpleClass();
        s = (int) (z * ref.y * invoked() * ref.method() * ref.method(3) * Inner.x);
        return factorial(1);
    }

    @Override
    public void run() {
    }
}

interface Interface {
    public static final int variable = 100;
}
