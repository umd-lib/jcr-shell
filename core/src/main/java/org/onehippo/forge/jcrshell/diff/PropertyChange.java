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
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PropertyChange extends Change {

    /** logger */
    private static final Logger log = LoggerFactory.getLogger(PropertyChange.class);

    PropertyChange(Property prop) throws RepositoryException {
        super(prop);
    }

    @Override
    public boolean isPropertyChange() {
        return true;
    }

    @Override
    public String getType() {
        try {
            return PropertyType.nameFromValue(((Property) getItem()).getType());
        } catch (RepositoryException e) {
            log.warn("Error while getting type for property '{}': {}", getPath(), e.getMessage());
            log.debug("Stack:", e);
            return "< unknown >";
        }
    }

    public String getValue() {
        try {
            Property p = (Property) getItem();
            if (p.getType() == PropertyType.BINARY) {
                return "< binary >";
            }
            if (p.getDefinition().isMultiple()) {
                StringBuilder sb = new StringBuilder();
                sb.append("[ ");
                Value[] values = p.getValues();
                boolean first = true;
                for (int i = 0; i < values.length; i++) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(values[i].getString());
                }
                sb.append(" ]");
                return sb.toString();
            } else {
                return p.getValue().getString();
            }
        } catch (RepositoryException e) {
            log.warn("Error while getting value for property '{}': {}", getPath(), e.getMessage());
            log.debug("Stack:", e);
            return "< error >";
        }
    }
}