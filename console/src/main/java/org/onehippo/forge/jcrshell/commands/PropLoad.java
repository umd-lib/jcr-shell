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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.onehippo.forge.jcrshell.console.FsWrapper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.Command.ArgumentType.Flags;

/**
 * Set a single value property.
 */
public class PropLoad extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] {
            new ArgumentType(EnumSet.of(Flags.PROPERTY, Flags.WRITE)), ArgumentType.FILE };

    public PropLoad() {
        super("propload", new String[] { "loadprop" }, "propload <binary property> <file>",
                "Load the contents of the file into to binary property", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException, IOException {
        Node node = JcrWrapper.getCurrentNode();

        String propName = args[1];
        String fileName = FsWrapper.getFullFileName(args[2]);

        if (node.hasProperty(propName) && !isBinaryProperty(node.getProperty(propName))) {
            JcrShellPrinter.printWarnln("Property " + propName + " is not a binary property.");
            return false;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            JcrShellPrinter.printWarnln("File " + args[2] + " does not exist.");
            return false;
        }

        InputStream is = new BufferedInputStream(new FileInputStream(file)); 
        try {
            node.setProperty(propName, is);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 3;
    }

    private boolean isBinaryProperty(Property p) throws RepositoryException {
        int type = p.getType();
        if (type != PropertyType.BINARY) {
            return false;
        }
        return true;
    }
}
