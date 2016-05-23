/*
 *  Copyright 2010 Hippo.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.forge.jcrshell.util;

public final class StringUtils {

    /**
     * Hide constructor
     */
    private StringUtils() {
    }

    /**
     * Join the string arguments with a space.
     * @param args
     * @return
     */
    public static String join(String[] args) {
        return join(args, 0, " ");
    }

    /**
     * Join the string arguments with the specified separator.
     * @param args
     * @param seperator
     * @return
     */
    public static String join(String[] args, CharSequence seperator) {
        return join(args, 0, seperator);
    }

    /**
     * Join the string with a space starting from the element at offset.
     * @param args
     * @param offset
     * @return
     */
    public static String join(String[] args, int offset) {
        return join(args, offset, " ");
    }

    /**
     * Join the string with a space starting from the element at offset with the specified separator.
     * @param args
     * @param offset
     * @param seperator
     * @return
     */
    public static String join(String[] args, int offset, CharSequence seperator) {
        if (args == null || args.length == 0) {
            return "";
        }
        if (offset >= args.length) {
            return "";
        }
        int start = offset > 0 ? offset : 0;
        StringBuilder sb = new StringBuilder(args[start]);
        for (int i = (start + 1); i < args.length; i++) {
            sb.append(seperator);
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
