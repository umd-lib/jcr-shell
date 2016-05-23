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
package org.onehippo.forge.jcrshell.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.onehippo.forge.jcrshell.console.FsWrapper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.util.ByteSizeFormatter;

/**
 * List child nodes of the working directory.
 */
public class FileList extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.DIRECTORY };

    public FileList() {
        super("filelist", new String[] { "lls", "ldir" }, "filelist [<directory>]",
                "list contents of a specified directory, or the working directory", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean executeCommand(final String[] args) {
        File[] entries;
        if (args.length == 2) {
            File dir = new File(FsWrapper.getFullFileName(args[1]));
            if (!dir.exists() ) {
                JcrShellPrinter.printWarnln("Path does not exist: " + args[1]);
                return false;
            } else if (!dir.isDirectory()) {
                JcrShellPrinter.printWarnln("Path does not a directory: " + args[1]);
                return false;
            }
            entries = dir.listFiles();
        } else {
            entries = FsWrapper.list();
        }
        
        TreeMap<String, File> ordered = new TreeMap<String, File>();
        for (File entry : entries) {
            ordered.put(entry.getName(), entry);
        }

        List<String[]> rows = new ArrayList<String[]>();
        rows.add(new String[] { "Name", "Size" });
        
        if (!FsWrapper.isRoot()) {
            rows.add(new String[] { "..", "" });
        }
        for (File entry : ordered.values()) {
            rows.add(new String[] { entry.getName(), ByteSizeFormatter.format(entry.length()) });
        }
        JcrShellPrinter.printTableWithHeader(rows);
        return true;
    }

    @Override
    protected boolean needsLiveSession() {
        return false;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1 || args.length == 2;
    }
}
