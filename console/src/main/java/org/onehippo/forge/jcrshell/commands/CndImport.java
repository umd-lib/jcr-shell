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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.HippoNodeType;
import org.onehippo.forge.jcrshell.console.FsWrapper;
import org.onehippo.forge.jcrshell.JcrWrapper;

/**
 * Import cnd.
 */
public class CndImport extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.FILE };

    public CndImport() {
        super("importcnd", new String[] { "cndimport" }, "importcnd <file>",
                "Import a content node definition (cnd) file.", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     * @throws FileNotFoundException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException, FileNotFoundException {
        String fileName = args[1];
        Node rootNode = JcrWrapper.getCurrentNode().getSession().getRootNode();
        Node initNode = rootNode.getNode(HippoNodeType.CONFIGURATION_PATH + "/" + HippoNodeType.INITIALIZE_PATH);

        if (initNode.hasNode("import-cnd")) {
            initNode.getNode("import-cnd").remove();
        }
        Node node = initNode.addNode("import-cnd", HippoNodeType.NT_INITIALIZEITEM);

        File file = new File(FsWrapper.getCwd(), fileName);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        node.setProperty(HippoNodeType.HIPPO_NODETYPES, new BufferedInputStream(bis));
        rootNode.getSession().save();
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 2;
    }
}
