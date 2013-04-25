/*
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javac.com.sun.tools.javac;

import gwtjava.io.fs.FileSystem;

public class Main {
    public static void compile(String fileName, String code) {
        FileSystem fs = FileSystem.instance();
        fs.reset();
        fs.addFile(fileName, code);

        new javac.com.sun.tools.javac.main.Main("javac").compile(new String[] { fileName });
    }

    public static String getClassName(String content) {
        final String CLASS_KWD = "class ";
        int nameStart = content.indexOf(CLASS_KWD) + CLASS_KWD.length();
        int len = content.substring(nameStart).indexOf(' ');

        return content.substring(nameStart, nameStart + len);
    }
}
