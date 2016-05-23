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

import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;

/**
 * Order a child node.
 */
public class Order extends AbstractCommand {

    private static final String[] ORDERINGS = new String[] { "before", "after", "first", "last", "up", "down" };

    public Order() {
        super("order", "order <child node> (before|after|up|down|first|last) [<child node>]",
                "Order one child with respect to another or all other children.");
        setArgumentTypes(new ArgumentType[] { ArgumentType.NODE, new ArgumentType(ORDERINGS), ArgumentType.NODE });
    }

    @Override
    protected boolean executeCommand(String[] args) throws RepositoryException {
        String keyword = args[2];
        boolean foundKeyword = false;
        for (String ordering : ORDERINGS) {
            if (ordering.equals(keyword)) {
                foundKeyword = true;
                break;
            }
        }
        if (!foundKeyword) {
            JcrShellPrinter.println(help());
            return false;
        }

        String src = args[1];

        // tab completion slash
        if (src.endsWith("/")) {
            src = src.substring(0, src.length() - 1);
        }

        Node node = JcrWrapper.getCurrentNode();
        // FIXME: handle absolute paths, deal with non-existing paths gracefully
        Node srcNode = node.getNode(src);

        Node parent = srcNode.getParent();
        if (!parent.getPrimaryNodeType().hasOrderableChildNodes()) {
            boolean orderableMixin = false;
            for (NodeType mixin : parent.getMixinNodeTypes()) {
                if (mixin.hasOrderableChildNodes()) {
                    orderableMixin = true;
                    break;
                }
            }
            if (!orderableMixin) {
                JcrShellPrinter.printWarnln("Parent node is not orderable");
                return false;
            }
        }
        if ("first".equals(keyword)) {
            Node sibling = parent.getNodes().nextNode();
            parent.orderBefore(srcNode.getName() + "[" + srcNode.getIndex() + "]", sibling.getName());
        } else if ("last".equals(keyword)) {
            NodeIterator siblings = parent.getNodes();
            boolean reordering = false;
            List<Node> orderBefore = new LinkedList<Node>();
            while (siblings.hasNext()) {
                Node sibling = siblings.nextNode();
                if (!reordering) {
                    if (sibling.isSame(srcNode)) {
                        reordering = true;
                        continue;
                    }
                } else {
                    orderBefore.add(sibling);
                }
            }
            for (Node sibling : orderBefore) {
                String siblingName = sibling.getName() + "[" + sibling.getIndex() + "]";
                String srcName = srcNode.getName() + "[" + srcNode.getIndex() + "]";
                parent.orderBefore(siblingName, srcName);
            }
        } else if ("up".equals(keyword)) {
            NodeIterator siblings = parent.getNodes();
            Node previous = null;
            while (siblings.hasNext()) {
                Node sibling = siblings.nextNode();
                if (sibling.isSame(srcNode)) {
                    if (previous != null) {
                        String siblingName = previous.getName() + "[" + previous.getIndex() + "]";
                        String srcName = srcNode.getName() + "[" + srcNode.getIndex() + "]";
                        parent.orderBefore(srcName, siblingName);
                    }
                    break;
                }
                previous = sibling;
            }
        } else if ("down".equals(keyword)) {
            NodeIterator siblings = parent.getNodes();
            boolean found = false;
            while (siblings.hasNext()) {
                Node sibling = siblings.nextNode();
                if (found) {
                    String siblingName = sibling.getName() + "[" + sibling.getIndex() + "]";
                    String srcName = srcNode.getName() + "[" + srcNode.getIndex() + "]";
                    parent.orderBefore(siblingName, srcName);
                    break;
                }
                if (sibling.isSame(srcNode)) {
                    found = true;
                }
            }
        } else {
            if (args.length != 4) {
                JcrShellPrinter.printErrorln("Specify a sibling for orderings 'before' and 'after'.");
                return false;
            }
            Node sibling = node.getNode(args[3]);
            if (!sibling.getParent().isSame(parent)) {
                JcrShellPrinter.printErrorln("Destination does not the have the same parent as source.");
                return false;
            }
            String siblingName = sibling.getName() + "[" + sibling.getIndex() + "]";
            String srcName = srcNode.getName() + "[" + srcNode.getIndex() + "]";
            if ("before".equals(keyword)) {
                parent.orderBefore(srcName, siblingName);
            } else {
                parent.orderBefore(siblingName, srcName);
            }
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 3 || args.length == 4;
    }
}
