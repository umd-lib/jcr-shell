/*
 *  Copyright 2008 Hippo.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;

import org.apache.commons.io.IOUtils;
import org.onehippo.forge.jcrshell.console.FsWrapper;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.util.CndWriter;

/**
 * Export cnd.
 */
public class CndExport extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.PREFIX, ArgumentType.FILE };

    public CndExport() {
        super("exportcnd", new String[] { "cndexport" }, "exportcnd <prefix> [ <file> ]",
                "Export or print a content node definition for a namespace.", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     * @throws IOException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException, IOException {
        String prefix = args[1];

        OutputStream os = System.out;
        if (args.length > 2) {
            String fileName = args[2];
            File file = new File(FsWrapper.getCwd(), fileName);
            os = new FileOutputStream(file);
        }

        Workspace workspace = JcrWrapper.getCurrentNode().getSession().getWorkspace();
        CndWriter cndWriter = new CndWriter(workspace.getNamespaceRegistry(), os);
        cndWriter.printCnd(workspace.getNodeTypeManager(), prefix);

        if (os != System.out) {
            IOUtils.closeQuietly(os);
        }

        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length >= 2;
    }
}
