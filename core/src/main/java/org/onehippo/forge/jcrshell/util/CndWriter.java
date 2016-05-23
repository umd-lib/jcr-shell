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
package org.onehippo.forge.jcrshell.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.NamespaceRegistry;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.OnParentVersionAction;

import org.hippoecm.repository.api.StringCodecFactory;

public class CndWriter {

    private static final String INDENT = "  ";
    private final NamespaceRegistry nsReg;

    private Writer out;

    static class NamespaceVisitor {

        private final String prefix;
        private final SortedSet<String> usedNamespaces = new TreeSet<String>();
        private final HashSet<NodeType> result = new LinkedHashSet<NodeType>();
        private final Set<String> visited = new HashSet<String>();

        public NamespaceVisitor(String prefix) {
            this.prefix = prefix;
        }

        public void add(NodeType nt) {
            visit(nt);
        }

        public SortedSet<String> getUsedNamespaces() {
            return usedNamespaces;
        }

        public HashSet<NodeType> getTypes() {
            return result;
        }

        private void visit(NodeDefinition nd) {
            resolve(nd.getName());
            for (NodeType childType : nd.getRequiredPrimaryTypes()) {
                visit(childType);
            }
            NodeType defaultPriType = nd.getDefaultPrimaryType();
            if (defaultPriType != null) {
                visit(defaultPriType);
            }
        }

        private void visit(PropertyDefinition pd) {
            resolve(pd.getName());
        }

        private void visit(NodeType nt) {
            String name = nt.getName();
            if (visited.contains(name)) {
                return;
            }
            visited.add(name);
            resolve(name);

            if (name.startsWith(prefix + ":")) {
                result.add(nt);
                for (NodeType superType : nt.getSupertypes()) {
                    visit(superType);
                }
                for (NodeDefinition nd : nt.getDeclaredChildNodeDefinitions()) {
                    visit(nd);
                }
                for (PropertyDefinition nd : nt.getDeclaredPropertyDefinitions()) {
                    visit(nd);
                }
            }
        }

        private void resolve(String name) {
            if (name.indexOf(':') > 0) {
                usedNamespaces.add(name.substring(0, name.indexOf(':')));
            }
        }
    }

    public CndWriter(NamespaceRegistry nsReg, OutputStream outputStream) {
        this(nsReg, new OutputStreamWriter(outputStream));
    }

    public CndWriter(NamespaceRegistry nsReg, Writer out) {
        this.nsReg = nsReg;
        this.out = out;
    }

    public void printNodeTypeDef(NodeType nt) throws IOException {
        writeName(nt);
        writeSupertypes(nt);
        writeOptions(nt);
        writePropDefs(nt);
        writeNodeDefs(nt);
        out.write("\n\n");
        out.flush();
    }

    public void printCnd(NodeTypeManager ntMgr, String prefix) throws IOException, RepositoryException {
        NamespaceVisitor visitor = new NamespaceVisitor(prefix);

        NodeTypeIterator ntIter = ntMgr.getAllNodeTypes();
        while (ntIter.hasNext()) {
            NodeType nt = ntIter.nextNodeType();
            if (nt.getName().startsWith(prefix + ":")) {
                visitor.visit(nt);
            }
        }

        for (String nsPrefix : visitor.getUsedNamespaces()) {
            out.write("<'");
            out.write(nsPrefix);
            out.write("'='");
            out.write(escape(nsReg.getURI(nsPrefix)));
            out.write("'>\n");
        }
        out.write("\n");

        for (NodeType type : visitor.getTypes()) {
            printNodeTypeDef(type);
        }
        out.flush();
    }

    private void writeName(NodeType nt) throws IOException {
        out.write("[");
        out.write(resolve(nt.getName()));
        out.write("]");
    }

    private void writeSupertypes(NodeType nt) throws IOException {
        NodeType[] superTypes = nt.getDeclaredSupertypes();
        String delim = " > ";
        for (NodeType sn : superTypes) {
            out.write(delim);
            out.write(resolve(sn.getName()));
            delim = ", ";
        }
    }

    private void writeOptions(NodeType nt) throws IOException {
        if (nt.hasOrderableChildNodes()) {
            out.write("\n" + INDENT);
            out.write("orderable");
            if (nt.isMixin()) {
                out.write(" mixin");
            }
        } else if (nt.isMixin()) {
            out.write("\n" + INDENT);
            out.write("mixin");
        }
    }

    private void writePropDefs(NodeType nt) throws IOException {
        PropertyDefinition[] propdefs = nt.getDeclaredPropertyDefinitions();
        for (PropertyDefinition propdef : propdefs) {
            writePropDef(nt, propdef);
        }
    }

    private void writePropDef(NodeType nt, PropertyDefinition pd) throws IOException {
        out.write("\n" + INDENT + "- ");
        writeItemDefName(pd.getName());
        out.write(" (");
        out.write(PropertyType.nameFromValue(pd.getRequiredType()).toLowerCase(Locale.ENGLISH));
        out.write(")");

        writeDefaultValues(pd.getDefaultValues());
        out.write(nt.getPrimaryItemName() != null && nt.getPrimaryItemName().equals(pd.getName()) ? " primary" : "");
        if (pd.isMandatory()) {
            out.write(" mandatory");
        }
        if (pd.isAutoCreated()) {
            out.write(" autocreated");
        }
        if (pd.isProtected()) {
            out.write(" protected");
        }
        if (pd.isMultiple()) {
            out.write(" multiple");
        }
        if (pd.getOnParentVersion() != OnParentVersionAction.COPY) {
            out.write(" ");
            out.write(OnParentVersionAction.nameFromValue(pd.getOnParentVersion()).toLowerCase(Locale.ENGLISH));
        }
        writeValueConstraints(pd.getValueConstraints());
    }

    private void writeNodeDefs(NodeType nt) throws IOException {
        NodeDefinition[] childnodeDefs = nt.getDeclaredChildNodeDefinitions();
        for (NodeDefinition childnodeDef : childnodeDefs) {
            writeNodeDef(nt, childnodeDef);
        }
    }

    private void writeNodeDef(NodeType nt, NodeDefinition nd) throws IOException {
        out.write("\n" + INDENT + "+ ");

        String name = nd.getName();
        if (name.equals("*")) {
            out.write('*');
        } else {
            writeItemDefName(name);
        }
        writeRequiredTypes(nd.getRequiredPrimaryTypes());
        writeDefaultType(nd.getDefaultPrimaryType());
        out.write(nt.getPrimaryItemName() != null && nt.getPrimaryItemName().equals(nd.getName()) ? " primary" : "");
        if (nd.isMandatory()) {
            out.write(" mandatory");
        }
        if (nd.isAutoCreated()) {
            out.write(" autocreated");
        }
        if (nd.isProtected()) {
            out.write(" protected");
        }
        if (nd.allowsSameNameSiblings()) {
            out.write(" multiple");
        }
        if (nd.getOnParentVersion() != OnParentVersionAction.COPY) {
            out.write(" ");
            out.write(OnParentVersionAction.nameFromValue(nd.getOnParentVersion()).toLowerCase(Locale.ENGLISH));
        }
    }

    private void writeRequiredTypes(NodeType[] reqTypes) throws IOException {
        if (reqTypes != null && reqTypes.length > 0) {
            String delim = " (";
            for (NodeType reqType : reqTypes) {
                out.write(delim);
                out.write(resolve(reqType.getName()));
                delim = ", ";
            }
            out.write(")");
        }
    }

    /**
     * write default types
     * @param defType
     */
    private void writeDefaultType(NodeType defType) throws IOException {
        if (defType != null && !defType.getName().equals("*")) {
            out.write(" = ");
            out.write(resolve(defType.getName()));
        }
    }

    private void writeValueConstraints(String[] vca) throws IOException {
        if (vca != null && vca.length > 0) {
            String vc = vca[0];
            out.write(" < '");
            out.write(escape(vc));
            out.write("'");
            for (int i = 1; i < vca.length; i++) {
                vc = vca[i];
                out.write(", '");
                out.write(escape(vc));
                out.write("'");
            }
        }
    }

    private void writeItemDefName(String name) throws IOException {
        out.write(resolve(name));
    }

    public static String resolve(String name) {
        if (name == null) {
            return "";
        }

        if (name.indexOf(':') > -1) {

            String prefix = name.substring(0, name.indexOf(':'));
            if (!"".equals(prefix)) {
                prefix += ":";
            }

            String encLocalName = StringCodecFactory.ISO9075Helper.encodeLocalName(name
                    .substring(name.indexOf(':') + 1));
            String resolvedName = prefix + encLocalName;

            // check for '-' and '+'
            if (resolvedName.indexOf('-') >= 0 || resolvedName.indexOf('+') >= 0) {
                return "'" + resolvedName + "'";
            } else {
                return resolvedName;
            }
        } else {
            return name;
        }

    }

    public static String escape(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '\\') {
                sb.insert(i, '\\');
                i++;
            } else if (sb.charAt(i) == '\'') {
                sb.insert(i, '\'');
                i++;
            }
        }
        return sb.toString();
    }

    private void writeDefaultValues(Value[] dva) throws IOException {
        if (dva != null && dva.length > 0) {
            String delim = " = '";
            for (Value element : dva) {
                out.write(delim);
                try {
                    out.write(escape(element.getString()));
                } catch (RepositoryException e) {
                    out.write(escape(element.toString()));
                }
                out.write("'");
                delim = ", '";
            }
        }
    }
}