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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void emptyArrayTest() {
        assertEquals("", StringUtils.join(null));
        assertEquals("", StringUtils.join( new String[0]));
        assertEquals("", StringUtils.join( new String[]{""}));
    }


    @Test
    public void defaultSeperatorTest() {
        assertEquals("1", StringUtils.join( new String[]{"1"}));
        assertEquals("1 2", StringUtils.join( new String[]{"1", "2"}));
        assertEquals("1 2 3", StringUtils.join( new String[]{"1", "2", "3"}));
    }

    @Test
    public void customSeperatorTest() {
        assertEquals("1", StringUtils.join( new String[]{"1"}, ", "));
        assertEquals("1, 2", StringUtils.join( new String[]{"1", "2"}, ", "));
        assertEquals("1, 2, 3", StringUtils.join( new String[]{"1", "2", "3"}, ", "));
    }

    @Test
    public void offsetTest() {
        assertEquals("1", StringUtils.join( new String[]{"1"}, 0));
        assertEquals("", StringUtils.join( new String[]{"1"}, 1));
        assertEquals("", StringUtils.join( new String[]{"1"}, 2));
        assertEquals("", StringUtils.join( new String[]{"1", "2"}, 2));
        assertEquals("3", StringUtils.join( new String[]{"1", "2", "3"}, 2));
        assertEquals("3 4", StringUtils.join( new String[]{"1", "2", "3", "4"}, 2));
    }

    @Test
    public void offsetCustomSeperatorTest() {
        assertEquals("3", StringUtils.join( new String[]{"1", "2", "3"}, 2, ", "));
        assertEquals("3, 4", StringUtils.join( new String[]{"1", "2", "3", "4"}, 2, ", "));
    }
}
