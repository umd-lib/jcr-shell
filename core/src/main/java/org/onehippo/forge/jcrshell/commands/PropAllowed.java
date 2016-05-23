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
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.util.CndWriter;

/**
 * List allowed properties of current node.
 */
public class PropAllowed extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.PROPERTY };

    public PropAllowed() {
        super("propallowed", new String[] { "allowedprops" }, "propallowed",
                "show a list of properties of allowed for current node", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean executeCommand(final String[] args) {
        Node node = JcrWrapper.getCurrentNode();

        NodeType nt;
        try {
            nt = node.getPrimaryNodeType();
        } catch (RepositoryException e) {
            JcrShellPrinter.printErrorln("Error: " + e.getMessage());
            return false;
        }

        JcrShellPrinter.println("Allowed child nodes: ");
        JcrShellPrinter.println("");

        JcrShellPrinter.println("main: ");
        PropertyDefinition[] propDefs = nt.getDeclaredPropertyDefinitions();
        for (PropertyDefinition propDef : propDefs) {
            JcrShellPrinter.println(getPropDefString(propDef));
        }

        JcrShellPrinter.println("");
        JcrShellPrinter.println("inherited: ");
        NodeType[] superTypes = nt.getDeclaredSupertypes();
        for (NodeType superType : superTypes) {
            PropertyDefinition[] superDefs = superType.getDeclaredPropertyDefinitions();
            for (PropertyDefinition propDef : superDefs) {
                JcrShellPrinter.println(getPropDefString(propDef));
            }

        }
        JcrShellPrinter.println("");
        return true;
    }

    private String getPropDefString(PropertyDefinition propDef) {
        StringBuffer def = new StringBuffer("- ");
        def.append(CndWriter.resolve(propDef.getName()));
        def.append(" (").append(PropertyType.nameFromValue(propDef.getRequiredType())).append(')');

        Value[] dv = propDef.getDefaultValues();
        if (dv != null && dv.length > 0) {
            String delim = " = '";
            for (int i = 0; i < dv.length; i++) {
                def.append(delim);
                try {
                    def.append(CndWriter.escape(dv[i].getString()));
                } catch (RepositoryException e) {
                    def.append(CndWriter.escape(dv[i].toString()));
                }
                def.append("'");
                delim = ", '";
            }
        }
        if (propDef.isMandatory()) {
            def.append(" mandatory");
        }
        if (propDef.isAutoCreated()) {
            def.append(" autocreated");
        }
        if (propDef.isProtected()) {
            def.append(" protected");
        }
        if (propDef.isMultiple()) {
            def.append(" multiple");
        }
        String[] vca = propDef.getValueConstraints();
        if (vca != null && vca.length > 0) {
            String vc = vca[0];
            def.append(" < '");
            def.append(CndWriter.escape(vc));
            def.append("'");
            for (int i = 1; i < vca.length; i++) {
                vc = vca[i];
                def.append(", '");
                def.append(CndWriter.escape(vc));
                def.append("'");
            }
        }
        return def.toString();
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1;
    }
}
