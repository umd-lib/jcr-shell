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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;

/**
 * List child nodes of current node.
 */
public class NamespaceList extends AbstractCommand {

    public NamespaceList() {
        super("namespacelist", new String[] { "listnamespaces" }, "namespacelist", "list registered namespaces");
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        Map<String, String> namespaces = JcrWrapper.getNamespaces();
        List<String[]> rows = new ArrayList<String[]>();
        rows.add(new String[] { "Prefix", "URI" });
        for (Iterator<Map.Entry<String, String>> iter = namespaces.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, String> entry = iter.next();
            rows.add(new String[] { entry.getValue(), entry.getKey() });
        }
        JcrShellPrinter.printTableWithHeader(rows);
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1;
    }
}
