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
public class PropAdd extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] {
            new ArgumentType(EnumSet.of(Flags.PROPERTY, Flags.MULTI, Flags.WRITE)), ArgumentType.PRIMITIVE };

    public PropAdd() {
        super("propadd", new String[] { "addprop" }, "propadd <property> [<type>]",
                "Create an empty multi valued property, default type is String", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        Node node = JcrWrapper.getCurrentNode();

        String propName = args[1];
        String propTypeName = null;
        int propType = -1;
        if (args.length == 3) {
            propTypeName = args[2];
        } else {
            propTypeName = "String";
        }

        try {
            propType = PropertyType.valueFromName(propTypeName);
        } catch (IllegalArgumentException e) {
            JcrShellPrinter.printWarnln("Uknown property type: " + propTypeName);
            return false;
        }

        if (node.hasProperty(propName)) {
            JcrShellPrinter.printWarnln("Property '" + propName + "' already exists.");
            return false;
        }
        try {
            node.setProperty(propName, new Value[] {}, propType);
            JcrWrapper.removeFromCache(node.getPath());
            return true;
        } catch (ValueFormatException e) {
            JcrShellPrinter.printErrorln("Unable to create value: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 2 || args.length == 3;
    }
}
