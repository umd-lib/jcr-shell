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

import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.JcrWrapper;

/**
 * Command for traversing to a node with the given uuid.
 */
public class CdUuid extends AbstractCommand {

    private static final String COMMAND = "cduuid";
    private static final String USAGE = "cduuid <uuid>";
    private static final String HELP = "Change directory to a node by uuid";
    private static final String[] ALIASES = new String[] {};
    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.UUID };

    public CdUuid() {
        super(COMMAND, ALIASES, USAGE, HELP, ARGUMENTS);
    }

    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        return JcrWrapper.cduuid(args[1]);
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length >= 2;
    }
}
