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
package org.onehippo.forge.jcrshell.completers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import jline.console.completer.Completer;

import org.onehippo.forge.jcrshell.console.FsWrapper;

/**
 * fork of the sonatype jline filenamecompleter
 */
public class FileNameCompleter implements Completer {

    public int complete(final String buf, final int cursor, final List<CharSequence> candidates) {
        String buffer = (buf == null) ? "" : buf;
        String translated = FsWrapper.getFullFileName(buffer);

        File file = new File(translated);
        File[] entries = null;
        
        final File dir;
        if (translated.endsWith(File.separator)) {
            dir = file;
        } else {
            dir = file.getParentFile();
        }

        if (dir != null) {
            entries = dir.listFiles();
        } 
        if (entries == null) {
            entries = new File[0];
        }

        // filter out dot files
        String remainder = buffer;
        int lastIndex = buffer.lastIndexOf(File.separator);
        if (lastIndex > -1) {
            remainder = buffer.substring(lastIndex + 1);
        }
        if (!remainder.startsWith(".") || remainder.startsWith("..")) {
            List<File> filtered = new LinkedList<File>();
            for (File entry : entries) {
                if (!entry.getName().startsWith(".")) {
                    filtered.add(entry);
                }
            }
            entries = filtered.toArray(new File[filtered.size()]);
        }
        
        return matchFiles(buffer, translated, entries, candidates);
    }

    public int matchFiles(final String buffer, final String translated, final File[] files,
            final List<CharSequence> candidates) {
        if (files == null) {
            return -1;
        }

        int matches = 0;

        // first pass: just count the matches
        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                matches++;
            }
        }
        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                CharSequence name = file.getName() + (matches == 1 && file.isDirectory() ? File.separator : " ");
                candidates.add(render(file, name).toString());
            }
        }

        final int index = buffer.lastIndexOf(File.separator);
        return index + File.separator.length();
    }

    protected CharSequence render(final File file, final CharSequence name) {
        assert file != null;
        assert name != null;
        return name;
    }

}
