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
 * Add a value to or create a multi value property to current node.
 */
public class ValueAdd extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] {
            new ArgumentType(EnumSet.of(Flags.PROPERTY, Flags.MULTI, Flags.WRITE)), ArgumentType.STRING,
            ArgumentType.PRIMITIVE };

    public ValueAdd() {
        super("valueadd", new String[] { "addvalue" }, "valueadd <propertyname> <value> [<type>]",
                "add the value to a existing multi value property or create a new property, default type is String",
                ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        Node node = JcrWrapper.getCurrentNode();

        String propName = args[1];
        StringBuffer propValue = new StringBuffer(args[2]);
        for (int i = 3; i < args.length; i++) {
            propValue.append(" ").append(args[i]);
        }
        String propTypeName = null;
        int propType = -1;
        if (args.length == 4) {
            propTypeName = args[3];
        } else {
            propTypeName = "String";
        }

        try {
            propType = PropertyType.valueFromName(propTypeName);
        } catch (IllegalArgumentException e) {
            JcrShellPrinter.printWarnln("Uknown property type: " + propTypeName);
            return false;
        }

        Value value;
        try {
            value = node.getSession().getValueFactory().createValue(propValue.toString(), propType);
        } catch (ValueFormatException e) {
            JcrShellPrinter.printWarnln("Unable to create value: " + e.getMessage());
            return false;
        }

        if (node.hasProperty(propName)) {
            Property p = node.getProperty(propName);
            int type = p.getType();
            if (type != propType) {
                JcrShellPrinter.printWarnln("Property type doesn't match type of current property: "
                        + PropertyType.nameFromValue(type));
                return false;
            }
            if (!p.getDefinition().isMultiple()) {
                JcrShellPrinter.printWarnln("Use propset to set single value properties.");
                return false;
            }
            Value[] values = p.getValues();
            Value[] newValues = new Value[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
            node.setProperty(propName, newValues);
        } else {
            node.setProperty(propName, new Value[] { value });
        }
        JcrWrapper.removeFromCache(node.getPath());

        return true;

    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 3 || args.length == 4;
    }
}
