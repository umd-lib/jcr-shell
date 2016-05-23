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
package org.onehippo.forge.jcrshell.util;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.api.HippoNode;
import org.hippoecm.repository.api.HippoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HippoJcrUtils {

    /** logger */
    private static final Logger log = LoggerFactory.getLogger(HippoJcrUtils.class);
    
    /**
     * Hide constructor
     */
    private HippoJcrUtils() {
    }

    public static boolean isHippoRepository(Repository repository) {
        return (repository instanceof HippoRepository);
    }

    public static boolean isHippoSession(Session session) {
        return (session instanceof HippoSession);
    }

    public static boolean isHippoNode(Node node) {
        return (node instanceof HippoNode);
}

    public static boolean isVirtual(Node node) {
        if (node == null) {
            return false;
        }
        if (!isHippoNode(node)) {
            return false;
        }
        HippoNode hippoNode = (HippoNode) node;
        try {
            Node canonical = hippoNode.getCanonicalNode();
            if (canonical == null) {
                return true;
            }
            return !hippoNode.isSame(canonical);
        } catch (RepositoryException e) {
            log.warn("Error while checking if node is virtual: " + e.getMessage());
            return false;
        }
    }
}
