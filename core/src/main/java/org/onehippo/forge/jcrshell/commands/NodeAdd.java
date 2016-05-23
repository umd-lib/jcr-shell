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

import java.util.EnumSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;

/**
 * Add a child node to the current node.
 */
public class NodeAdd extends AbstractCommand {

    private static final String DEFAULT_NODETYPE = "nt:unstructured";
    
    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] {
            new ArgumentType(EnumSet.of(ArgumentType.Flags.NODE, ArgumentType.Flags.ADD)), ArgumentType.PRIMARY_TYPE };

    public NodeAdd() {
        super("nodeadd", new String[] { "addnode" }, "nodeadd <nodename> [<type>]",
                "add a child node, default type is nt:unstructured", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        String nodePath = args[1];
        String nodeName = getNodeName(nodePath);
        Node parent = getParentNode(JcrWrapper.getCurrentNode(), nodePath);
        
        String nodeType;
        if (args.length == 3) {
            nodeType = args[2];
        } else {
            nodeType = guessNodeType(parent, nodeName);
        }

        if (JcrWrapper.addNode(parent, nodeName, nodeType)) {
            JcrShellPrinter.printOkln("Node '" + nodePath + "' added.");
        } else {
            JcrShellPrinter.printWarnln("Failed to add node: " + nodePath);
            return false;
        }

        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 2 || args.length == 3;
    }
    
    private String getNodeName(String nodePath) {
        if (nodePath.contains("/")) {
            return nodePath.substring(nodePath.lastIndexOf('/') + 1);
        } else {
            return nodePath;
        }
    }
    private Node getParentNode(Node currentNode, String nodePath) throws RepositoryException {
        if (nodePath.contains("/")) {
            return currentNode.getNode(nodePath.substring(0, nodePath.lastIndexOf('/')));
        } else {
            return currentNode;
        }
    }
    
    private String guessNodeType(Node parent, String nodeName) throws RepositoryException {

        String nodeType = DEFAULT_NODETYPE;

        // Use an available default or req'd type; fall back to nt:unstructured
        NodeType nt = parent.getPrimaryNodeType();
        for (NodeDefinition nd : nt.getChildNodeDefinitions()) {
            if ("*".equals(nd.getName())) {
                NodeType defaultType = nd.getDefaultPrimaryType();
                if (defaultType != null) {
                    nodeType = defaultType.getName();
                } else {
                    NodeType[] reqd = nd.getRequiredPrimaryTypes();
                    if (!"nt:base".equals(reqd[0].getName())) {
                        nodeType = reqd[0].getName();
                    }
                }
            } else if (nodeName.equals(nd.getName())) {
                NodeType defaultType = nd.getDefaultPrimaryType();
                if (defaultType != null) {
                    nodeType = defaultType.getName();
                } else {
                    NodeType[] reqd = nd.getRequiredPrimaryTypes();
                    if (!"nt:base".equals(reqd[0].getName())) {
                        nodeType = reqd[0].getName();
                    } else {
                        nodeType = DEFAULT_NODETYPE;
                    }
                }
                break;
            }
        }
        return nodeType;
    }
}
