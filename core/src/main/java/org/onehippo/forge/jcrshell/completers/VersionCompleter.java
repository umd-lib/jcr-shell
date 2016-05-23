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
package org.onehippo.forge.jcrshell.completers;

import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import jline.console.completer.Completer;

import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.Command.ArgumentType;
import org.onehippo.forge.jcrshell.Command.ArgumentType.Flags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line completer for node names.
 */
public class VersionCompleter implements Completer {

    /** logger */
    private static final Logger log = LoggerFactory.getLogger(VersionCompleter.class);

    private ArgumentType argType;

    public VersionCompleter() {
        this(ArgumentType.VERSION);
    }

    public VersionCompleter(ArgumentType type) {
        this.argType = type;
    }

    /**
     * {@inheritDoc}
     */
    public int complete(final String buf, final int cursor, final List<CharSequence> clist) {
        VersionHistory vh;
        try {
            vh = JcrWrapper.getVersionHistory();
        } catch (UnsupportedRepositoryOperationException e) {
            log.debug("Unable to complete because the node is not versioned.");
            return -1;

        } catch (RepositoryException e) {
            log.error("Error while trying to get version history", e);
            return -1;
        }

        String start = (buf == null) ? "" : buf;

        try {
            VersionIterator vi = vh.getAllVersions();
            while (vi.hasNext()) {
                Version v = vi.nextVersion();
                if (!argType.getFlags().contains(Flags.ONLY_LABELS) && v.getName().startsWith(start)) {
                    clist.add(v.getName());
                }
                if (!argType.getFlags().contains(Flags.NO_LABELS)) {
                    String[] labels = vh.getVersionLabels(v);
                    for (String label : labels) {
                        if (label.startsWith(start)) {
                            clist.add(label);
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("Error while trying to generate version completion", e);
        }

        if (clist.size() == 1) {
            clist.set(0, ((String) clist.get(0)) + " ");
        }
        return (clist.size() == 0) ? (-1) : 0;
    }

}
