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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.parsers.ParserConfigurationException;

import org.onehippo.forge.jcrshell.console.FsWrapper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.export.XmlFormatter;
import org.xml.sax.SAXException;

/**
 * Copy a child node.
 */
public class NodeExport extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.NODE, ArgumentType.FILE };

    public NodeExport() {
        super("nodeexport", new String[] { "export" }, "nodeexport <nodename> <xml file> [<skipBinaries>]",
                "export the target node in xml format and store it into the file, default skipBinaries is false",
                ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        boolean skipBinaries = false;

        Node node = JcrWrapper.getCurrentNode();

        String src = args[1];
        String fileName = args[2];

        String srcAbsPath = null;

        // tab completion slash
        if (src.endsWith("/")) {
            src = src.substring(0, src.length() - 1);
        }

        if (src.startsWith("/")) {
            if (!node.getSession().getRootNode().hasNode(src.substring(1))) {
                JcrShellPrinter.println("Src node not found: " + src);
                return false;
            }
            srcAbsPath = node.getSession().getRootNode().getNode(src.substring(1)).getPath();
        } else {
            if (!node.hasNode(src)) {
                JcrShellPrinter.println("Src node not found: " + src);
                return false;
            }
            srcAbsPath = node.getNode(src).getPath();
        }

        File file = new File(FsWrapper.getCwd(), fileName);

        File tmp = null;
        try {
            tmp = File.createTempFile("export", ".xml");
            FileOutputStream fos = new FileOutputStream(tmp);
            try {
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                try {
                    JcrWrapper.exportXml(srcAbsPath, bos, skipBinaries);
                } finally {
                    bos.close();
                }
            } finally {
                fos.close();
            }
            XmlFormatter.format(tmp, file);
        } catch (IOException e) {
            JcrShellPrinter.printErrorln("Unable to write to file '" + fileName + "': " + e.getMessage());
            return false;
        } catch (ParserConfigurationException e) {
            JcrShellPrinter.printErrorln("ParserConfiguration error: " + e.getMessage());
            return false;
        } catch (SAXException e) {
            JcrShellPrinter.printErrorln("SAXException: " + e.getMessage());
            return false;
        } finally {
            if (tmp != null && !tmp.delete()) {
                JcrShellPrinter.printWarnln("Unable to delete temp file: " + tmp.getName());
            }
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 3 || args.length == 4;
    }
}
