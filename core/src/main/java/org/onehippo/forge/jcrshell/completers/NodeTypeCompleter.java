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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import jline.console.completer.Completer;

import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.Command.ArgumentType;
import org.onehippo.forge.jcrshell.Command.ArgumentType.Flags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line completer for property names.
 */
public class NodeTypeCompleter implements Completer {

    /** logger */
    private static final Logger log = LoggerFactory.getLogger(NodeTypeCompleter.class);

    private ArgumentType argType;

    public NodeTypeCompleter() {
        this(ArgumentType.TYPE);
    }

    public NodeTypeCompleter(ArgumentType type) {
        this.argType = type;
    }

    /**
     * {@inheritDoc}
     */
    public int complete(final String buf, final int cursor, final List<CharSequence> clist) {
        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return -1;
        }

        // fetch node list
        String start = (buf == null) ? "" : buf;
        try {
            boolean nsOnly = !start.contains(":");
            Workspace ws = node.getSession().getWorkspace();
            Set<String> prefixes = new TreeSet<String>();
            if (nsOnly) {
                NamespaceRegistry nsReg = ws.getNamespaceRegistry();
                for (String prefix : nsReg.getPrefixes()) {
                    if (prefix.startsWith(start)) {
                        prefixes.add(prefix);
                    }
                }
            }
            if (prefixes.size() > 1) {
                clist.addAll(prefixes);
            } else {
                NodeTypeManager ntMgr = ws.getNodeTypeManager();
                NodeTypeIterator ntIter;
                if (argType.getFlags().contains(Flags.PRIMARY_TYPE)) {
                    ntIter = ntMgr.getPrimaryNodeTypes();
                }else if (argType.getFlags().contains(Flags.MIXIN)) {
                    ntIter = ntMgr.getMixinNodeTypes();
                } else {
                    ntIter = ntMgr.getAllNodeTypes();
                }
                while (ntIter.hasNext()) {
                    NodeType nt = ntIter.nextNodeType();
                    String name = nt.getName();
                    if (name.startsWith(start)) {
                        clist.add(name);
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("Error during node type completion", e);
            return -1;
        }

        if (clist.size() == 1) {
            clist.set(0, ((String) clist.get(0)) + " ");
        }
        return (clist.size() == 0) ? (-1) : 0;
    }
}
