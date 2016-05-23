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

import javax.jcr.Property;
import javax.jcr.RepositoryException;

public final class PropertyChanged extends PropertyChange {
    private Property newProp;

    PropertyChanged(Property oldProp, Property newProp) throws RepositoryException {
        super(oldProp);
        this.newProp = newProp;
    }

    public PropertyChange getRemoval() throws RepositoryException {
        return new PropertyRemoved((Property) getItem());
    }

    public PropertyChange getAddition() throws RepositoryException {
        return new PropertyAdded(newProp);
    }
}