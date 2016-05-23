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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import jline.console.completer.Completer;

import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.Command.ArgumentType;
import org.onehippo.forge.jcrshell.Command.ArgumentType.Flags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line completer for property names.
 */
public class PropertyNameCompleter implements Completer {

    /** logger */
    private static final Logger log = LoggerFactory.getLogger(PropertyNameCompleter.class);

    private ArgumentType argType;

    public PropertyNameCompleter() {
        this(ArgumentType.PROPERTY);
    }

    public PropertyNameCompleter(ArgumentType type) {
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
        String path = (buf == null) ? "" : buf;
        String head = null;
        if (path.contains("/")) {
            head = path.substring(0, path.lastIndexOf('/'));
            String tail = path.substring(path.lastIndexOf('/') + 1);
            try {
                if (path.startsWith("/")) {
                    if (!"".equals(head)) {
                        node = node.getSession().getRootNode().getNode(head);
                    } else {
                        node = node.getSession().getRootNode();
                    }
                } else {
                    node = node.getNode(head);
                }
            } catch (RepositoryException e) {
                log.error("Error during property type completion", e);
                return -1;
            }
            path = tail;
        }

        SortedSet<String> candidates;
        try {
            candidates = JcrWrapper.getPropertyNameList(node);
        } catch (RepositoryException e) {
            log.error("Error during property type completion", e);
            return -1;
        }

        SortedSet<String> matches = candidates.tailSet(path);

        for (Iterator<String> i = matches.iterator(); i.hasNext();) {
            String can = i.next();
            if (!(can.startsWith(path))) {
                break;
            }
            try {
                if (argType.getFlags().contains(Flags.WRITE) || argType.getFlags().contains(Flags.REMOVE)) {
                    PropertyDefinition pd = node.getProperty(can).getDefinition();
                    if (pd.isProtected()) {
                        continue;
                    }
                    if ("*".equals(pd.getName())) {
                        if (pd.isMultiple()) {
                            if (!argType.getFlags().contains(Flags.MULTI)) {
                                continue;
                            }
                        } else {
                            if (argType.getFlags().contains(Flags.MULTI)) {
                                continue;
                            }
                        }
                    }
                }
            } catch (RepositoryException e) {
                log.error("Error during node type completion", e);
                return -1;
            }
            if (head != null) {
                clist.add(head + "/" + can);
            } else {
                clist.add(can);
            }
        }

        if (argType.getFlags().contains(Flags.WRITE) || argType.getFlags().contains(Flags.REMOVE)) {
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
                    PropertyDefinition[] propDefs = nt.getPropertyDefinitions();
                    for (PropertyDefinition propDef : propDefs) {
                        String name = propDef.getName();
                        if ("*".equals(name)) {
                            continue;
                        }
                        if (name.startsWith(path) && !propDef.isProtected()) {
                            if (propDef.isMultiple()) {
                                if (!argType.getFlags().contains(Flags.MULTI)) {
                                    continue;
                                }
                            } else {
                                if (argType.getFlags().contains(Flags.MULTI)) {
                                    continue;
                                }
                            }
                            clist.add(propDef.getName());
                        }
                    }
                }
            } catch (RepositoryException e) {
                log.error("Error during node type completion", e);
                return -1;
            }
        }

        if (clist.size() == 1) {
            clist.set(0, ((String) clist.get(0)) + " ");
        }
        return (clist.size() == 0) ? (-1) : 0;
    }
}
