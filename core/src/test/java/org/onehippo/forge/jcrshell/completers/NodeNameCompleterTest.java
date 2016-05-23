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
package org.onehippo.forge.jcrshell.completers;

import org.junit.Test;
import org.onehippo.forge.jcrshell.JcrTest;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class NodeNameCompleterTest extends JcrTest {

    @Test
    public void testAbsolute() throws ItemExistsException, PathNotFoundException, VersionException,
            ConstraintViolationException, LockException, RepositoryException {
        Node test = getTestRoot().addNode("bladie");
        NodeNameCompleter completer = new NodeNameCompleter();
        List<CharSequence> candidates = new LinkedList<CharSequence>();

        int offset = completer.complete("/test/bla", "/test/bla".length(), candidates);
        assertEquals("Candidate node is found", 1, candidates.size());
        assertEquals("Suggestion is correct", "bladie/", candidates.get(0));
        assertEquals("Offset at end of match", "/test/".length(), offset);
    }

    @Test
    public void testRelative() throws ItemExistsException, PathNotFoundException, VersionException,
            ConstraintViolationException, LockException, RepositoryException {
        Node test = getTestRoot().addNode("bladie");
        NodeNameCompleter completer = new NodeNameCompleter();
        List<CharSequence> candidates = new LinkedList<CharSequence>();

        int offset = completer.complete("bla", "bla".length(), candidates);
        assertEquals("Candidate node is found", 1, candidates.size());
        assertEquals("Suggestion is correct", "bladie/", candidates.get(0));
        assertEquals("Offset at end of match", 0, offset);
    }

    @Test
    public void testUpCompletion() {
        Pattern pattern = NodeNameCompleter.MOVING_UP;
        assertTrue(pattern.matcher("../").matches());
        assertTrue(pattern.matcher("..").matches());
        assertTrue(pattern.matcher("../..").matches());
        assertTrue(pattern.matcher("../../").matches());
        assertTrue(pattern.matcher("../.").matches());
        assertFalse(pattern.matcher("../xx").matches());
        assertFalse(pattern.matcher("yy/..").matches());
        assertFalse(pattern.matcher("/../").matches());
        assertFalse(pattern.matcher(".../").matches());
    }
}
