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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class FileNameCompleterTest {

    private static final String FILE_NAME = "fnc-completer-test-file";
    private static final String FILE_NAME_START = "fnc-c";
    private static final String DIR_NAME = "fnc-completer-test-directory";
    private static final String DIR_NAME_START = "fnc-c";
    private static final String SUBDIR_NAME = "fnc-completer-test-subdir";
    private static final String SUBDIR_NAME_START = "fnc-c";

    @Test
    public void testFileNameCompletion() throws IOException {
        File file = new File(FILE_NAME);
        file.createNewFile();
        try {
            FileNameCompleter fnc = new FileNameCompleter();
            List<CharSequence> candidates = new LinkedList<CharSequence>();
            int result = fnc.complete(FILE_NAME_START, 1, candidates);
            assertEquals(0, result);
            assertEquals(1, candidates.size());
            assertEquals(FILE_NAME + " ", candidates.get(0).toString());
        } finally {
            file.delete();
        }
    }

    @Test
    public void testFileInSubdirCompletion() throws IOException {
        File dir = new File(DIR_NAME);
        dir.mkdir();
        File file = new File(dir, FILE_NAME);
        file.createNewFile();
        try {
            FileNameCompleter fnc = new FileNameCompleter();
            List<CharSequence> candidates = new LinkedList<CharSequence>();
            int result = fnc.complete(DIR_NAME + "/" + FILE_NAME_START, 1, candidates);
            assertEquals(DIR_NAME.length() + 1, result);
            assertEquals(1, candidates.size());
            assertEquals(FILE_NAME + " ", candidates.get(0).toString());
        } finally {
            file.delete();
            dir.delete();
        }
    }

    @Test
    public void testDirNameCompletion() throws IOException {
        File file = new File(DIR_NAME);
        file.mkdir();
        try {
            DirNameCompleter fnc = new DirNameCompleter();
            List<CharSequence> candidates = new LinkedList<CharSequence>();
            int result = fnc.complete(DIR_NAME_START, 1, candidates);
            assertEquals(0, result);
            assertEquals(1, candidates.size());
            assertEquals(DIR_NAME + "/", candidates.get(0).toString());
        } finally {
            file.delete();
        }
    }

    @Test
    public void testDirInSubdirCompletion() throws IOException {
        File dir = new File(DIR_NAME);
        dir.mkdir();
        File file = new File(dir, SUBDIR_NAME);
        file.mkdir();
        try {
            DirNameCompleter fnc = new DirNameCompleter();
            List<CharSequence> candidates = new LinkedList<CharSequence>();
            int result = fnc.complete(DIR_NAME + "/" + SUBDIR_NAME_START, 1, candidates);
            assertEquals(DIR_NAME.length() + 1, result);
            assertEquals(1, candidates.size());
            assertEquals(SUBDIR_NAME + "/", candidates.get(0).toString());
        } finally {
            file.delete();
            dir.delete();
        }
    }

    @Test
    public void testHomeFileNameCompletion() throws IOException {
        File file = new File(new File(System.getProperty("user.home")), FILE_NAME);
        file.createNewFile();
        try {
            FileNameCompleter fnc = new FileNameCompleter();
            List<CharSequence> candidates = new LinkedList<CharSequence>();
            int result = fnc.complete("~/" + FILE_NAME_START, 1, candidates);
            assertEquals(2, result);
            assertEquals(1, candidates.size());
            assertEquals(FILE_NAME + " ", candidates.get(0).toString());
        } finally {
            file.delete();
        }
    }

}
