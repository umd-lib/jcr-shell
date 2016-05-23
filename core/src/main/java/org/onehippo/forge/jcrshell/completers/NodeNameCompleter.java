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
package org.onehippo.forge.jcrshell.completers;

import jline.console.completer.Completer;
import org.onehippo.forge.jcrshell.Command.ArgumentType;
import org.onehippo.forge.jcrshell.Command.ArgumentType.Flags;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Command line completer for node names.
 */
public class NodeNameCompleter implements Completer {

    /** logger */
    private static final Logger log = LoggerFactory.getLogger(NodeNameCompleter.class);

    public static final Pattern MOVING_UP = Pattern.compile("^([.]{2}/)*[.]{1,2}/?$");

    private ArgumentType argType;

    public NodeNameCompleter() {
        this(ArgumentType.NODE);
    }

    public NodeNameCompleter(ArgumentType type) {
        this.argType = type;
    }

    /**
     * {@inheritDoc}
     */
    public int complete(final String buf, final int cursor, final List<CharSequence> clist) {
        Node node = JcrWrapper.getCurrentNode();
        // sanity check
        if (node == null) {
            return -1;
        }

        // get path and node part
        String start = (buf == null) ? "" : buf;
        String path = null;
        boolean absolute = start.startsWith("/");
        int lastSlash = start.lastIndexOf('/');
        boolean movingup = MOVING_UP.matcher(start).matches();

        try {
            if (absolute) {
                if (lastSlash > 0) {
                    path = start.substring(0, lastSlash) + 1;

                    // strip path from start of matcher
                    start = start.substring(lastSlash + 1);

                    node = node.getSession().getRootNode().getNode(path.substring(1, path.length() - 1));
                } else {
                    path = "/";
                    start = start.substring(1);
                    node = node.getSession().getRootNode();
                }
            } else {
                if (lastSlash > -1) {
                    path = start.substring(0, lastSlash) + 1;

                    // strip path from start of matcher
                    start = start.substring(lastSlash + 1);

                    if (path.equals("../")) {
                        node = node.getParent();
                    } else {
                        node = node.getNode(path.substring(0, path.length() - 1));
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("Error during node name completion", e);
        }

        // fetch node list
        SortedSet<String> candidates;
        try {
            candidates = JcrWrapper.getNodeNameList(node);
        } catch (RepositoryException e) {
            log.error("Error during node name completion", e);
            return -1;
        }

        // strip first part of list that do not match
        SortedSet<String> matches = candidates.tailSet(start);

        // find and add matches
        for (Iterator<String> i = matches.iterator(); i.hasNext();) {
            String can = i.next();
            // list is sorted, bail out
            if (!(can.startsWith(start))) {
                break;
            }
            if (!movingup && "..".equals(can)) {
                continue;
            }
            if (argType.getFlags().contains(Flags.WRITE) || argType.getFlags().contains(Flags.REMOVE)
                    || argType.getFlags().contains(Flags.ADD)) {
                try {
                    Node child = node.getNode(can);
                    NodeDefinition def = child.getDefinition();
                    if (def.isProtected()) {
                        continue;
                    }
                    if (argType.getFlags().contains(Flags.ADD) && !def.allowsSameNameSiblings()) {
                        continue;
                    }
                } catch (RepositoryException e) {
                    log.error("Error during node name completion", e);
                    return -1;
                }
            }
            // add path if needed
//            if (path != null) {
//                clist.add(path + "/" + can + "/");
//            } else {
                clist.add(can + "/");
//            }
        }

        if (argType.getFlags().contains(Flags.WRITE) || argType.getFlags().contains(Flags.ADD)) {
            try {
                Set<String> types = new TreeSet<String>();
                types.add(node.getPrimaryNodeType().getName());
                if (node.hasProperty("jcr:mixinTypes")) {
                    for (Value val : node.getProperty("jcr:mixinTypes").getValues()) {
                        types.add(val.getString());
                    }
                }
                NodeTypeManager ntmgr = node.getSession().getWorkspace().getNodeTypeManager();
                for (String type : types) {
                    NodeType nt = ntmgr.getNodeType(type);
                    NodeDefinition[] childNodeDefs = nt.getChildNodeDefinitions();
                    for (NodeDefinition childNodeDef : childNodeDefs) {
                        String name = childNodeDef.getName();
                        if (childNodeDef.isProtected()) {
                            continue;
                        }
                        if (!"*".equals(name) && !name.startsWith(start)) {
                            continue;
                        }
                        if ("*".equals(name)) {
                            name = start;
                        }
                        boolean exists = node.hasNode(name);
                        if (exists && argType.getFlags().contains(Flags.ADD) && !childNodeDef.allowsSameNameSiblings()) {
                            continue;
                        }
                        clist.add(name + (exists ? "/" : ""));
                    }
                }
            } catch (RepositoryException e) {
                log.error("Error during node name completion", e);
                return -1;
            }
        }

        return (clist.size() == 0) ? (-1) : (path != null ? path.length() : 0);
    }

}
