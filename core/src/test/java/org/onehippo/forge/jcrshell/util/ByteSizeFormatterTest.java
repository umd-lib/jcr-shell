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
import static org.junit.Assert.fail;

import org.junit.Test;
import org.onehippo.forge.jcrshell.util.ByteSizeFormatter.Format;

public class ByteSizeFormatterTest {

    @Test
    public void bytesFormatTest() {
        assertEquals("0 B", ByteSizeFormatter.format(0L));
        assertEquals("1 B", ByteSizeFormatter.format(1L));
        assertEquals("123 B", ByteSizeFormatter.format(123L));
        assertEquals("1,023 B", ByteSizeFormatter.format(1023L));
    }

    @Test
    public void kiloBytesFormatTest() {
        assertEquals("1 KB", ByteSizeFormatter.format(1024L));
        assertEquals("1 KB", ByteSizeFormatter.format(1025L));
        assertEquals("2 KB", ByteSizeFormatter.format(2047L));
        assertEquals("2 KB", ByteSizeFormatter.format(2048L));
        assertEquals("1.5 KB", ByteSizeFormatter.format(512L + Format.KB.getFactor(), 2));
        assertEquals("1,024 KB", ByteSizeFormatter.format(Format.MB.getFactor() - 1L));
    }

    @Test
    public void megaBytesFormatTest() {
        assertEquals("1 MB", ByteSizeFormatter.format(Format.MB.getFactor()));
        assertEquals("1,024 MB", ByteSizeFormatter.format(Format.GB.getFactor() - 1L));
    }

    @Test
    public void gigaBytesFormatTest() {
        assertEquals("1 GB", ByteSizeFormatter.format(Format.GB.getFactor()));
        assertEquals("1,024 GB", ByteSizeFormatter.format(Format.TB.getFactor() - 1L));
    }

    @Test
    public void teraBytesFormatTest() {
        assertEquals("1 TB", ByteSizeFormatter.format(Format.TB.getFactor()));
        assertEquals("1,024 TB", ByteSizeFormatter.format(Format.PB.getFactor() - 1L));
    }

    @Test
    public void petaBytesFormatTest() {
        assertEquals("1 PB", ByteSizeFormatter.format(Format.PB.getFactor()));
        assertEquals("1,024 PB", ByteSizeFormatter.format(Format.PB.getFactor() * 1024L));
        assertEquals("2,048 PB", ByteSizeFormatter.format(Format.PB.getFactor() * 2048L));
    }

    @Test
    public void maxValueFormatTest() {
        assertEquals("8,192 PB", ByteSizeFormatter.format(Long.MAX_VALUE));
    }

    @Test
    public void invalidSizesTest() {
        try {
            String s = ByteSizeFormatter.format(-1L);
            fail("Invalid number should raise a NumberFormatException, got " + s);
        } catch (NumberFormatException e) {
            // expected
        }
        try {
            String s = ByteSizeFormatter.format(Long.MIN_VALUE);
            fail("Invalid number should rais a NumberFormatException, got " + s);
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void bytesDecodeTest() {
        assertEquals(0L, ByteSizeFormatter.decode("0"));
        assertEquals(0L, ByteSizeFormatter.decode("0 "));
        assertEquals(0L, ByteSizeFormatter.decode("0 B"));
        assertEquals(0L, ByteSizeFormatter.decode("0B"));
        assertEquals(0L, ByteSizeFormatter.decode("0 b"));
        assertEquals(0L, ByteSizeFormatter.decode("0b"));
        assertEquals(1L, ByteSizeFormatter.decode("1"));
        assertEquals(1L, ByteSizeFormatter.decode("1 "));
        assertEquals(1L, ByteSizeFormatter.decode("1 B"));
        assertEquals(1L, ByteSizeFormatter.decode("1B"));
        assertEquals(1L, ByteSizeFormatter.decode("1 b"));
        assertEquals(1L, ByteSizeFormatter.decode("1b"));

        assertEquals(123456789L, ByteSizeFormatter.decode("123456789"));
        assertEquals(123456789L, ByteSizeFormatter.decode("123456789 B"));
    }

    @Test
    public void kiloBytesDecodeTest() {
        assertEquals(0L, ByteSizeFormatter.decode("0 KB"));
        assertEquals(123L * Format.KB.getFactor(), ByteSizeFormatter.decode("123 KB"));
    }

    @Test
    public void megaBytesDecodeTest() {
        assertEquals(0L, ByteSizeFormatter.decode("0 MB"));
        assertEquals(123L * Format.MB.getFactor(), ByteSizeFormatter.decode("123 MB"));
    }

    @Test
    public void gigaBytesDecodeTest() {
        assertEquals(0L, ByteSizeFormatter.decode("0 GB"));
        assertEquals(123L * Format.GB.getFactor(), ByteSizeFormatter.decode("123 GB"));
    }

    @Test
    public void teraBytesDecodeTest() {
        assertEquals(0L, ByteSizeFormatter.decode("0 TB"));
        assertEquals(123L * Format.TB.getFactor(), ByteSizeFormatter.decode("123 TB"));
    }

    @Test
    public void petaBytesDecodeTest() {
        assertEquals(0L, ByteSizeFormatter.decode("0 PB"));
        assertEquals(123L * Format.PB.getFactor(), ByteSizeFormatter.decode("123 PB"));
    }

    @Test
    public void invalidNumbersDecodeTest() {
        try {
            ByteSizeFormatter.decode(null);
        } catch (NumberFormatException e) {
            //expected
        }
        try {
            ByteSizeFormatter.decode("");
        } catch (NumberFormatException e) {
            //expected
        }
        try {
            ByteSizeFormatter.decode(" ");
        } catch (NumberFormatException e) {
            //expected
        }
        try {
            ByteSizeFormatter.decode("asdf");
        } catch (NumberFormatException e) {
            //expected
        }

        try {
            ByteSizeFormatter.decode("asdfKB");
        } catch (NumberFormatException e) {
            //expected
        }
        try {
            ByteSizeFormatter.decode("123.12 KB");
        } catch (NumberFormatException e) {
            //expected
        }
    }
}
