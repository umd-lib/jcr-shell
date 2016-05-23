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

import java.io.IOException;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.util.CndWriter;

/**
 * List child nodes of current node.
 */
public class NodeTypeGet extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.TYPE };

    public NodeTypeGet() {
        super("nodetypeget", new String[] { "getnodetype", "ntget" }, "nodetypeget <name>", "Get node type definition",
                ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws IOException 
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws IOException, RepositoryException {
        NodeType nt = JcrWrapper.getNodeType(args[1]);
        NamespaceRegistry namespaceRegistry = JcrWrapper.getCurrentNode().getSession().getWorkspace()
                .getNamespaceRegistry();
        CndWriter cndWriter = new CndWriter(namespaceRegistry, System.out);
        cndWriter.printNodeTypeDef(nt);
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 2;
    }
}
