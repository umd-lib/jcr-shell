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

import javax.jcr.Item;
import javax.jcr.RepositoryException;

public abstract class Change implements Comparable<Change> {
    private final Item item;
    private final String path;
    private final String name;

    Change(Item item) throws RepositoryException{
        this.item = item;
        this.name = item.getName();
        this.path = item.getPath();
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public  Item getItem() {
        return item;
    }
    public abstract String getType();

    public abstract boolean isPropertyChange();

    public boolean isRemoval() {
        return false;
    }

    public boolean isAddition() {
        return false;
    }

    public int compareTo(Change o) {
        if (isPropertyChange() && !o.isPropertyChange()) {
            return -1;
        } else if (!isPropertyChange() && o.isPropertyChange()) {
            return 1;
        }
        return getName().compareTo(o.getName());
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
        return getName().hashCode();
    }
}