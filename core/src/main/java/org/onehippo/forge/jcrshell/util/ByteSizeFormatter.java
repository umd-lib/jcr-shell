/*
 * Copyright 2010 Hippo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.jcrshell.util;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Formats numeric byte values into human-readable strings.
 */
public final class ByteSizeFormatter {

    private static final int DEFAULT_DECIMAL_PLACES = 0;
    private static final long BASE = 1024L;

    private ByteSizeFormatter() {
    }

    public enum Format {
        NONE(1L, ""),
        BYTE(1L, "B"),
        KB(BASE * BYTE.factor, "KB"),
        MB(BASE * KB.factor, "MB"),
        GB(BASE * MB.factor, "GB"),
        TB(BASE * GB.factor, "TB"),
        PB(BASE * TB.factor, "PB");

        private final long factor;
        private final String suffix;

        private Format(long factor, String suffix) {
            this.factor = factor;
            this.suffix = suffix;
        }

        public long getFactor() {
            return factor;
        }

        public String getSuffix() {
            return suffix;
        }
    }


    /** Formats file size in bytes as appropriate to bytes, KB, MB, GB, TB or PB.
     *
     * @param fileSize in bytes
     * @return formatted file size
     * @throws NumberFormatException when the fileSize is negative or cannot be formatted.
     **/
    public static String format(final long fileSize) {
        return format(fileSize, DEFAULT_DECIMAL_PLACES);
    }
    
    /** Formats file size in bytes as appropriate to bytes, KB, MB, GB, TB or PB.
     *
     * @param fileSize in bytes
     * @param maxDecimals set max number of decimals
     * @return formatted file size
     * @throws NumberFormatException when the fileSize is negative or cannot be formatted.
     **/
    public static String format(final long fileSize, final int maxDecimals) {
        if (fileSize < 0L) {
            throw new NumberFormatException("Invalid negative file size: " + fileSize);
        }
        DecimalFormat numberFormat = new DecimalFormat("#,##0.###");
        numberFormat.setMaximumFractionDigits(maxDecimals);
        Format format;
        if (fileSize < Format.KB.getFactor()) {
            format = Format.BYTE;
        } else if (fileSize < Format.MB.getFactor()) {
            format = Format.KB;
        } else if (fileSize < Format.GB.getFactor()) {
            format = Format.MB;
        } else if (fileSize < Format.TB.getFactor()) {
            format = Format.GB;
        } else if (fileSize < Format.PB.getFactor()) {
            format = Format.TB;
        } else {
            format = Format.PB;
        }
        return new StringBuilder(numberFormat.format(fileSize / (double) format.getFactor())).append(' ').append(
                format.getSuffix()).toString();
    }

    /** Decodes a formatted file size in bytes as appropriate to bytes.
     *
     * @param sizeString the string with a suffix
     * @return number of bytes
     * @throws NumberFormatException when string is null, empty or cannot be parsed.
     **/
    public static long decode(final String sizeString) {
        if (sizeString == null || sizeString.length() == 0) {
            throw new NumberFormatException("Invalid size string: " + sizeString);
        }
        String size = sizeString.toUpperCase(Locale.ENGLISH).replaceAll(",", "").replaceAll(" ", "");
        Format format = Format.NONE;
        if (size.endsWith(Format.PB.getSuffix())) {
            format = Format.PB;
        } else if (size.endsWith(Format.TB.getSuffix())) {
            format = Format.TB;
        } else if (size.endsWith(Format.GB.getSuffix())) {
            format = Format.GB;
        } else if (size.endsWith(Format.MB.getSuffix())) {
            format = Format.MB;
        } else if (size.endsWith(Format.KB.getSuffix())) {
            format = Format.KB;
        } else if (size.endsWith(Format.BYTE.getSuffix())) {
            format = Format.BYTE;
        }
        return Long.parseLong(size.substring(0, size.length() - format.getSuffix().length())) * format.getFactor();
    }
}