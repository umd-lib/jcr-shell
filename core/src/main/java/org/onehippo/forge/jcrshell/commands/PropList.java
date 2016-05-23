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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;

/**
 * List properties of current node.
 */
public class PropList extends AbstractCommand {

    private static final int MAX_NAME_LENGTH = 50;
    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.NODE };

    public PropList() {
        super("proplist", new String[] { "listprops", "list" }, "proplist [<node>]",
                "show a list of properties of the node", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        String path = null;
        if (args.length == 2) {
            path = args[1];
        }

        PropertyIterator iter = JcrWrapper.getProperties(path);
        if (iter == null) {
            JcrShellPrinter.printWarnln("Path not found: " + path);
            return false;
        }

        SortedMap<String, Map<String, String>> sortedProps = new TreeMap<String, Map<String, String>>();
        while (iter.hasNext()) {
            Property p = iter.nextProperty();
            Map<String, String> vals = new HashMap<String, String>(2);
            try {
                vals.put("type", PropertyType.nameFromValue(p.getType()));
                vals.put("value", printValue(p));
                sortedProps.put(JcrWrapper.fullName(p), vals);
            } catch (RepositoryException e) {
                JcrShellPrinter.printErrorln("Error: " + e.getMessage());
            }
        }
        List<String[]> rows = new ArrayList<String[]>();
        rows.add(new String[] { "Name", "Type", "Value" });
        Iterator<Entry<String, Map<String, String>>> propIter = sortedProps.entrySet().iterator();
        while (propIter.hasNext()) {
            Entry<String, Map<String, String>> e = propIter.next();
            rows.add(new String[] { e.getKey(), e.getValue().get("type"), e.getValue().get("value") });
        }
        JcrShellPrinter.printTableWithHeader(rows);
        return true;
    }

    /**
     * Helper method for pretty printing property list.
     * @param p Property
     * @return String formatted String
     * @throws RepositoryException when unable to print property
     */
    private String printValue(final Property p) throws RepositoryException {
        if (p.getDefinition().isMultiple()) {
            return "[multivalue]";
        }
        int type = p.getType();
        switch (type) {
        case PropertyType.STRING:
        case PropertyType.LONG:
        case PropertyType.BOOLEAN:
        case PropertyType.DOUBLE:
        case PropertyType.PATH:
        case PropertyType.REFERENCE:
        case PropertyType.NAME:
            String val = p.getString();
            if (val.length() > MAX_NAME_LENGTH) {
                String more = " [more..]";
                return val.substring(0, MAX_NAME_LENGTH - more.length()) + more;
            } else {
                return val;
            }
        case PropertyType.BINARY:
            return "[binary data]";
        case PropertyType.UNDEFINED:
            return "[undefined]";
        case PropertyType.DATE:
            return p.getValue().getString();
        default:
            return "[unknown type]: " + type;
        }
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1 || args.length == 2;
    }
}
