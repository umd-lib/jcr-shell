/*
 *  Copyright 2010 Hippo.
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
package org.onehippo.forge.jcrshell.diff;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NodeChange extends Change {

    /** logger */
    private static final Logger log = LoggerFactory.getLogger(NodeChange.class);

    NodeChange(Node node) throws RepositoryException {
        super(node);
    }

    private Node getNode() {
        return (Node) getItem();
    }

    @Override
    public boolean isPropertyChange() {
        return false;
    }

    @Override
    public String getType() {
        try {
            return getNode().getPrimaryNodeType().getName();
        } catch (RepositoryException e) {
            log.warn("Error while getting type for '{}': {}", getPath(), e.getMessage());
            log.debug("Stack:", e);
            return "< unknown >";
        }
    }

    protected int getIndex() {
        try {
            return getNode().getIndex();
        } catch (RepositoryException e) {
            log.warn("Error while getting index for '{}': {}", getPath(), e.getMessage());
            log.debug("Stack:", e);
            return 0;
        }
    }
    @Override
    public int compareTo(Change o) {
        int superCmp = super.compareTo(o);
        if (superCmp != 0) {
            return superCmp;
        }
        NodeChange nc = (NodeChange) o;
        if (getIndex() == nc.getIndex()) {
            return 0;
        }
        return (getIndex() < nc.getIndex() ? -1 : 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Change) {
            return compareTo((Change) obj) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + getIndex();
    }

}