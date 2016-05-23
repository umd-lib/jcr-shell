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

import org.onehippo.forge.jcrshell.console.FsWrapper;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DirNameCompleter extends FileNameCompleter {

    @Override
    public int matchFiles(String buffer, String translated, File[] files, List<CharSequence> candidates) {
        Set<File> dirs = new HashSet<File>();
        File parent = new File(FsWrapper.getFullFileName(".."));
        if (parent.exists()) {
            dirs.add(parent);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                dirs.add(file);
            }
        }
        return super.matchFiles(buffer, translated, dirs.toArray(new File[dirs.size()]), candidates);
    }

}
