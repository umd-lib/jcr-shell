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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;

/**
 * Print the path of the current node and it's uuid, when available.
 */
public class Pwd extends AbstractCommand {

    public Pwd() {
        super("pwd", "pwd", "Show path to current node");
    }

    @Override
    protected boolean executeCommand(String[] args) throws RepositoryException {
        Node node = JcrWrapper.getCurrentNode();
        if (node.isNodeType("mix:referenceable")) {
            JcrShellPrinter.println(node.getPath() + "  (" + node.getUUID() + ")");
        } else {
            JcrShellPrinter.println(node.getPath());
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1;
    }
}
