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
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.util.CndWriter;

/**
 * List allowed child nodes of current node.
 */
public class NodeAllowed extends AbstractCommand {

    public NodeAllowed() {
        super("nodeallowed", new String[] { "allowednodes" }, "nodeallowed",
                "show a list of (child) nodes allowed for current node");
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        Node node = JcrWrapper.getCurrentNode();
        NodeType nt = node.getPrimaryNodeType();

        JcrShellPrinter.println("Allowed child nodes: ");
        JcrShellPrinter.println("");

        JcrShellPrinter.println("main: ");
        NodeDefinition[] nodeDefs = nt.getDeclaredChildNodeDefinitions();
        for (NodeDefinition nodeDef : nodeDefs) {
            JcrShellPrinter.println(getNodeDefString(nodeDef));
        }

        JcrShellPrinter.println("");
        JcrShellPrinter.println("inherited: ");
        NodeType[] superTypes = nt.getDeclaredSupertypes();
        for (NodeType superType : superTypes) {
            NodeDefinition[] superDefs = superType.getDeclaredChildNodeDefinitions();
            for (NodeDefinition nodeDef : superDefs) {
                JcrShellPrinter.println(getNodeDefString(nodeDef));
            }

        }
        JcrShellPrinter.println("");
        return true;
    }

    private String getNodeDefString(NodeDefinition nodeDef) {
        StringBuffer def = new StringBuffer("+ ");

        String name = nodeDef.getName();
        if (name.equals("*")) {
            def.append('*');
        } else {
            def.append(CndWriter.resolve(name));
        }
        NodeType[] reqTypes = nodeDef.getRequiredPrimaryTypes();
        if (reqTypes != null && reqTypes.length > 0) {
            String delim = " (";
            for (int i = 0; i < reqTypes.length; i++) {
                def.append(delim);
                def.append(CndWriter.resolve(reqTypes[i].getName()));
                delim = ", ";
            }
            def.append(")");
        }
        NodeType defaultType = nodeDef.getDefaultPrimaryType();
        if (defaultType != null && !defaultType.getName().equals("*")) {
            def.append(" = ");
            def.append(CndWriter.resolve(defaultType.getName()));
        }

        if (nodeDef.isMandatory()) {
            def.append(" mandatory");
        }
        if (nodeDef.isAutoCreated()) {
            def.append(" autocreated");
        }
        if (nodeDef.isProtected()) {
            def.append(" protected");
        }
        if (nodeDef.allowsSameNameSiblings()) {
            def.append(" multiple");
        }
        return def.toString();
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1;
    }
}
