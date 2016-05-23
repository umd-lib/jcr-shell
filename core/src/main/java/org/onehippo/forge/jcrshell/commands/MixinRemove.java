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

import org.onehippo.forge.jcrshell.JcrWrapper;

/**
 * Add a child node to the current node.
 */
public class MixinRemove extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.MIXIN };

    public MixinRemove() {
        super("mixinremove", new String[] { "removemixin", "mixindelete", "rmmixin" }, "mixinremove <mixin type>",
                "remove a mixin from the current node", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        Node node = JcrWrapper.getCurrentNode();
        String mixinName = args[1];
        node.removeMixin(mixinName);
        JcrWrapper.removeFromCache(node.getPath());
        return true;

    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 2;
    }
}
