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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;

import org.onehippo.forge.jcrshell.util.HippoJcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JcrDiff {

    /** logger */
    private static final Logger log = LoggerFactory.getLogger(JcrDiff.class);

    private static final Set<String> IGNORED_PROPERTIES = new TreeSet<String>();

    static { 
        IGNORED_PROPERTIES.add("jcr:uuid");
        IGNORED_PROPERTIES.add("hippo:paths");
    }

    /** hide constructor */
    private JcrDiff() {
    }
    
    public static Iterator<Change> compare(Node base, Node current) {
        return new ChangeIterator(base, current);
    }

    static class ChangeIterator implements Iterator<Change> {
        private boolean initialized = false;
        private Node base;
        private Node current;
        private Iterator<Change> propChanges = null;
        private List<Iterator<Change>> nested = new LinkedList<Iterator<Change>>();

        ChangeIterator(Node base, Node current) {
            this.base = base;
            this.current = current;
        }

        void init() {
            initialized = true;
            try {
                SortedSet<Change> changes = new TreeSet<Change>();
                for (PropertyIterator pi = base.getProperties(); pi.hasNext();) {
                    Property baseProp = pi.nextProperty();
                    String name = baseProp.getName();
                    if (IGNORED_PROPERTIES.contains(name)) {
                        continue;
                    }
                    if (current.hasProperty(name)) {
                        Property currentProp = current.getProperty(name);
                        PropertyDefinition pd = baseProp.getDefinition();
                        if (pd.getRequiredType() == PropertyType.REFERENCE) {
                            continue;
                        }
                        if (baseProp.getDefinition().isMultiple()) {
                            if (currentProp.getDefinition().isMultiple()) {
                                Value[] baseVals = baseProp.getValues();
                                Value[] currentVals = currentProp.getValues();
                                if (baseVals.length == currentVals.length) {
                                    for (int i = 0; i < baseVals.length; i++) {
                                        Value baseVal = baseVals[i];
                                        Value currentVal = currentVals[i];
                                        if (baseVal.getType() != currentVal.getType()
                                                || !baseVal.getString().equals(currentVal.getString())) {
                                            Change valChange = new PropertyChanged(baseProp, currentProp);
                                            changes.add(valChange);
                                            break;
                                        }
                                    }
                                } else {
                                    Change multi = new PropertyChanged(baseProp, currentProp);
                                    changes.add(multi);
                                }
                            } else {
                                Change multi = new PropertyChanged(baseProp, currentProp);
                                changes.add(multi);
                            }
                        } else {
                            if (!currentProp.getDefinition().isMultiple()) {
                                Value baseVal = baseProp.getValue();
                                Value currentVal = currentProp.getValue();
                                if (baseVal.getType() != currentVal.getType()
                                        || !baseVal.getString().equals(currentVal.getString())) {
                                    Change valChange = new PropertyChanged(baseProp, currentProp);
                                    changes.add(valChange);
                                }
                            } else {
                                Change multi = new PropertyChanged(baseProp, currentProp);
                                changes.add(multi);
                            }
                        }
                    } else {
                        Change removed = new PropertyRemoved(baseProp);
                        changes.add(removed);
                    }
                }
                for (PropertyIterator pi = current.getProperties(); pi.hasNext();) {
                    Property currentProp = pi.nextProperty();
                    String name = currentProp.getName();
                    if (IGNORED_PROPERTIES.contains(name)) {
                        continue;
                    }
                    if (currentProp.getDefinition().getRequiredType() == PropertyType.REFERENCE) {
                        continue;
                    }
                    if (!base.hasProperty(name)) {
                        Change added = new PropertyAdded(currentProp);
                        changes.add(added);
                    }
                }
                for (NodeIterator ni = base.getNodes(); ni.hasNext();) {
                    Node baseNode = ni.nextNode();
                    if (HippoJcrUtils.isVirtual(baseNode)) {
                        continue;
                    }
                    String name = baseNode.getName() + "[" + baseNode.getIndex() + "]";
                    if (current.hasNode(name)) {
                        nested.add(compare(baseNode, current.getNode(name)));
                    } else {
                        changes.add(new NodeRemoved(baseNode));
                    }
                }
                for (NodeIterator ni = current.getNodes(); ni.hasNext();) {
                    Node currentNode = ni.nextNode();
                    if (HippoJcrUtils.isVirtual(currentNode)) {
                        continue;
                    }
                    String name = currentNode.getName() + "[" + currentNode.getIndex() + "]";
                    if (!base.hasNode(name)) {
                        changes.add(new NodeAdded(currentNode));
                    }
                }
                propChanges = changes.iterator();
            } catch (RepositoryException e) {
                log.warn("Exception while create change iterator", e);
                Set<Change> set = Collections.emptySet();
                propChanges = set.iterator();
            }
        }

        public boolean hasNext() {
            if (!initialized) {
                init();
            }
            if (propChanges.hasNext()) {
                return true;
            }
            while (nested.size() > 0) {
                Iterator<Change> iter = nested.get(0);
                if (iter.hasNext()) {
                    return true;
                }
                nested.remove(0);
            }
            return false;
        }

        public Change next() {
            if (!initialized) {
                init();
            }
            if (propChanges.hasNext()) {
                return propChanges.next();
            }
            while (nested.size() > 0) {
                Iterator<Change> iter = nested.get(0);
                if (iter.hasNext()) {
                    return iter.next();
                }
                nested.remove(0);
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
