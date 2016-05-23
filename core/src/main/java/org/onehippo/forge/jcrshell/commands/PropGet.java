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
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.util.ByteSizeFormatter;

/**
 * Display property value(s).
 */
public class PropGet extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.PROPERTY };

    public PropGet() {
        super("propget", new String[] { "get", "getprop" }, "propget <property> [<property> [..]]",
                "get the value(s) of properties from the current node", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        Node node = JcrWrapper.getCurrentNode();

        final String propName = args[1];
        if (!node.hasProperty(propName)) {
            JcrShellPrinter.printWarnln("Node doesn't have a property with name: " + propName);
            return false;
        }
        Property p = node.getProperty(propName);

        if (!p.getDefinition().isMultiple()) {
            JcrShellPrinter.println(getPrintablePropertyValue(p));
        } else {
            List<String[]> rows = new ArrayList<String[]>();
            rows.add(new String[] { "Index", "Value" });
            int index = 0;
            for (Value val : p.getValues()) {
                rows.add(new String[] {Integer.toString(index), getPrintableValue(val) });
            }
            JcrShellPrinter.printTableWithHeader(rows);
        }

        return true;
    }

    /**
     * Helper method for pretty printing property values.
     * @param p Property
     * @return a String representation of the property value
     * @throws RepositoryException when unable to print value
     */
    private String getPrintablePropertyValue(final Property p) throws RepositoryException {
        Value v = p.getValue();
        int type = v.getType();
        switch (type) {
        case PropertyType.BINARY:
            return "binary data (size: "+ ByteSizeFormatter.format(p.getLength()) + ")";
        case PropertyType.UNDEFINED:
            return "undefined";
        default:
            return v.getString();
        }
    }

    /**
     * Helper method for pretty printing property values.
     * @param v Value
     * @return a String representation of the property value
     * @throws RepositoryException when unable to print value
     */
    private String getPrintableValue(final Value v) throws RepositoryException {
        int type = v.getType();
        switch (type) {
        case PropertyType.BINARY:
            return "binary data";
        case PropertyType.UNDEFINED:
            return "undefined";
        default:
            return v.getString();
        }
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 2;
    }
}
