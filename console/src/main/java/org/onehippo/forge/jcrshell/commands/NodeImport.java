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
import java.util.HashMap;
import java.util.Map;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.hippoecm.repository.api.ImportMergeBehavior;
import org.hippoecm.repository.api.ImportReferenceBehavior;
import org.onehippo.forge.jcrshell.console.FsWrapper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;

/**
 * Copy a child node.
 */
public class NodeImport extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.FILE };

    public NodeImport() {
        super("nodeimport", new String[] { "import" },
                "nodeimport <xml file> [<uuidBehavior> [<referenceBehavior> [<mergeBehavior>]]]", null, ARGUMENTS);
    }

    public static class LookupHashMap<K, V> extends HashMap<K, V> {
        private static final long serialVersionUID = 9065806784464553409L;

        public K getFirstKey(Object value) {
            if (value == null) {
                return null;
            }
            for (Map.Entry<K, V> e : entrySet()) {
                if (value.equals(e.getValue())) {
                    return e.getKey();
                }
            }
            return null;
        }
    }

    private final LookupHashMap<Integer, String> uuidOpts = new LookupHashMap<Integer, String>();
    private final LookupHashMap<Integer, String> mergeOpts = new LookupHashMap<Integer, String>();
    private final LookupHashMap<Integer, String> derefOpts = new LookupHashMap<Integer, String>();

    private void initMaps() {
        uuidOpts.put(Integer.valueOf(ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING),
                "Remove existing node with same uuid");
        uuidOpts.put(Integer.valueOf(ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING),
                "Replace existing node with same uuid");
        uuidOpts.put(Integer.valueOf(ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW), "Throw error on uuid collision");
        uuidOpts.put(Integer.valueOf(ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW), "Create new uuids on import");

        mergeOpts.put(Integer.valueOf(ImportMergeBehavior.IMPORT_MERGE_ADD_OR_OVERWRITE),
                "Try to add, else overwrite same name nodes");
        mergeOpts.put(Integer.valueOf(ImportMergeBehavior.IMPORT_MERGE_ADD_OR_SKIP),
                "Try to add, else skip same name nodes");
        mergeOpts.put(Integer.valueOf(ImportMergeBehavior.IMPORT_MERGE_OVERWRITE), "Overwrite same name nodes");
        mergeOpts.put(Integer.valueOf(ImportMergeBehavior.IMPORT_MERGE_SKIP), "Skip same name nodes");
        mergeOpts.put(Integer.valueOf(ImportMergeBehavior.IMPORT_MERGE_THROW), "Throw error on naming conflict");

        derefOpts.put(Integer.valueOf(ImportReferenceBehavior.IMPORT_REFERENCE_NOT_FOUND_REMOVE),
                "Remove reference when not found");
        derefOpts.put(Integer.valueOf(ImportReferenceBehavior.IMPORT_REFERENCE_NOT_FOUND_THROW),
                "Throw error when not found");
        derefOpts.put(Integer.valueOf(ImportReferenceBehavior.IMPORT_REFERENCE_NOT_FOUND_TO_ROOT),
                "Add reference to root node when not found");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String help() {
        initMaps();
        StringBuffer buf = new StringBuffer();
        buf.append("import xml export file to the current node").append("\n");

        buf.append("uuidBehavior: ").append("\n");
        for (Map.Entry<Integer, String> map : uuidOpts.entrySet()) {
            buf.append("  ").append(map.getKey()).append(" : ").append(map.getValue()).append("\n");
        }

        buf.append("referenceBehavior: ").append("\n");
        for (Map.Entry<Integer, String> map : derefOpts.entrySet()) {
            buf.append("  ").append(map.getKey()).append(" : ").append(map.getValue()).append("\n");
        }

        buf.append("mergeBehavior: ").append("\n");
        for (Map.Entry<Integer, String> map : mergeOpts.entrySet()) {
            buf.append("  ").append(map.getKey()).append(" : ").append(map.getValue()).append("\n");
        }
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     * @throws IOException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException, IOException {
        Node node = JcrWrapper.getCurrentNode();

        int uuidBehavior = ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW;
        int referenceBehavior = ImportReferenceBehavior.IMPORT_REFERENCE_NOT_FOUND_TO_ROOT;
        int mergeBehavior = ImportMergeBehavior.IMPORT_MERGE_ADD_OR_OVERWRITE;

        String fileName = args[1];
        if (args.length > 2) {
            uuidBehavior = Integer.valueOf(args[2]);
        }
        if (args.length > 3) {
            referenceBehavior = Integer.valueOf(args[3]);
        }
        if (args.length > 4) {
            mergeBehavior = Integer.valueOf(args[4]);
        }

        File file = new File(FsWrapper.getCwd(), fileName);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try {
            JcrWrapper.importXml(node.getPath(), bis, uuidBehavior, referenceBehavior, mergeBehavior);
        } finally {
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(fis);
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        if (args.length < 2 || args.length > 5) {
            return false;
        }
        if (args.length > 2) {
            try {
                Integer.valueOf(args[2]);
            } catch (NumberFormatException e) {
                JcrShellPrinter.println("Invalid uuidBehavior: " + args[2]);
                JcrShellPrinter.println(usage());
                return false;
            }
        }
        if (args.length > 3) {
            try {
                Integer.valueOf(args[3]);
            } catch (NumberFormatException e) {
                JcrShellPrinter.println("Invalid referenceBehavior: " + args[3]);
                JcrShellPrinter.println(usage());
                return false;
            }
        }
        if (args.length > 4) {
            try {
                Integer.valueOf(args[4]);
            } catch (NumberFormatException e) {
                JcrShellPrinter.println("Invalid mergeBehavior: " + args[4]);
                JcrShellPrinter.println(usage());
                return false;
            }
        }
        return true;
    }
}
