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

import java.util.EnumSet;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.Command.ArgumentType.Flags;

/**
 * Set a single value property.
 */
public class PropSet extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] {
            new ArgumentType(EnumSet.of(Flags.PROPERTY, Flags.WRITE)), ArgumentType.STRING, ArgumentType.PRIMITIVE };

    public PropSet() {
        super("propset", new String[] { "setprop" }, "propset <property> <value> [<type>]",
                "Set the value of a existing property or create a new property, default type is String", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        Node node = JcrWrapper.getCurrentNode();

        String propName = args[1];
        String propValue = args[2];

        int propType = PropertyType.UNDEFINED;
        if (args.length == 4) {
            String propTypeName = args[3];
            try {
                propType = PropertyType.valueFromName(propTypeName);
            } catch (IllegalArgumentException e) {
                JcrShellPrinter.printWarnln("Uknown property type: " + propTypeName);
                return false;
            }
        }

        if (node.hasProperty(propName)) {
            Property p = node.getProperty(propName);
            int type = p.getType();
            if (propType != PropertyType.UNDEFINED) {
                if (type != propType) {
                    JcrShellPrinter.printWarnln("Property type doesn't match type of current property: "
                            + PropertyType.nameFromValue(type));
                    return false;
                }
            } else {
                propType = type;
            }
            if (p.getDefinition().isMultiple()) {
                JcrShellPrinter.printWarnln("Use propadd to add values to a multivalue.");
                return false;
            }
        } else if (propType == PropertyType.UNDEFINED) {
            propType = PropertyType.STRING;
        }
        try {
            Value value = node.getSession().getValueFactory().createValue(propValue, propType);
            node.setProperty(propName, value);
            JcrWrapper.removeFromCache(node.getPath());
            return true;
        } catch (ValueFormatException e) {
            JcrShellPrinter.printErrorln("Unable to create value: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 3 || args.length == 4;
    }
}
