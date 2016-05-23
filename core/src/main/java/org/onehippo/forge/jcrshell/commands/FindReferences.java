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

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;

import static org.onehippo.forge.jcrshell.output.Output.out;

/**
 * Find references and print path.
 */
public class FindReferences extends AbstractCommand {

    public FindReferences() {
        super("findreferences", new String[] { "refs", "noderefs" }, "findreferences",
                "find references to the current node");
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        Node node = JcrWrapper.getCurrentNode();
        if (node.isNodeType("mix:referenceable")) {
            PropertyIterator iter = node.getReferences();
            while (iter.hasNext()) {
                Property prop = iter.nextProperty();
                Node n = prop.getParent();
                JcrShellPrinter.println(String.format("%-60s\n", n.getPath()));
            }
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1;
    }
}
