package jvm.classparser;

import static org.junit.Assert.*;

import org.junit.Test;

import jvm.classparser.JType;

public class JTypeTest {

    @Test
    public void testArgSize() {
        assertEquals(1, JType.argsSize("([C)V"));
        assertEquals(2, JType.argsSize("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"));
        assertEquals(2, JType.retSize("(D)J"));
    }

}


