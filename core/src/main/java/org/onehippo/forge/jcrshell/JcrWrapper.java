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
package org.onehippo.forge.jcrshell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionHistory;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.jcr2dav.Jcr2davRepositoryFactory;
import org.apache.jackrabbit.rmi.client.ClientRepositoryFactory;
import org.apache.jackrabbit.rmi.client.RemoteRepositoryException;
import org.apache.jackrabbit.rmi.client.RemoteRuntimeException;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.hippoecm.repository.api.HippoSession;
import org.hippoecm.repository.api.NodeNameCodec;
import org.onehippo.forge.jcrshell.output.Output;
import org.onehippo.forge.jcrshell.output.TextOutput;
import org.onehippo.forge.jcrshell.util.HippoJcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper class for commonly used jcr calls.
 */
public final class JcrWrapper {

    private static final Logger log = LoggerFactory.getLogger(JcrWrapper.class);

    private static ThreadLocal<JcrShellSession> sessions = new ThreadLocal<JcrShellSession>();

    public static void setShellSession(JcrShellSession session) {
        sessions.set(session);
    }

    public static JcrShellSession getShellSession() {
        return sessions.get();
    }

    private static boolean isHippoRepository = true;

    private JcrWrapper() {
        super();
    }

    public static void setHippoRepository(final boolean isHippo) {
        isHippoRepository = isHippo;
    }

    /* private */ static void setCurrentNode(final Node node) {
        getShellSession().setCurrentNode(node);
    }

    public static boolean isConnected() {
        try {
            if (getShellSession().session != null && getShellSession().session.isLive()) {
                return true;
            }
        } catch (RemoteRuntimeException e) {
            JcrShellPrinter.printErrorln("Error communicating with server: " + e.getMessage());
            setConnected(false);
        }
        return getShellSession().connected;
    }

    public static void setConnected(final boolean connected) {
        JcrWrapper.getShellSession().connected = connected;
    }

    public static char[] getPassword() {
        return getShellSession().password.clone();
    }

    public static void setPassword(final String password) {
        JcrWrapper.getShellSession().password = password.toCharArray();
    }

    public static String getServer() {
        return getShellSession().server;
    }

    public static void setServer(final String server) {
        JcrWrapper.getShellSession().server = server;
    }

    public static String getUsername() {
        return getShellSession().username;
    }

    public static void setUsername(final String username) {
        JcrWrapper.getShellSession().username = username;
    }

    public static void clearCaches() {
        synchronized (getShellSession().mutex) {
            getShellSession().propertyNameCache.clear();
            getShellSession().nodeNameCache.clear();
        }
    }

    public static void removeFromCache(final String nodePath) {
        synchronized (getShellSession().mutex) {
            getShellSession().propertyNameCache.remove(nodePath);
            getShellSession().nodeNameCache.remove(nodePath);
        }
    }

    public static void updateCaches(EventIterator events) {
        Set<String> paths = new HashSet<String>();
        while (events.hasNext()) {
            Event event = events.nextEvent();
            try {
                String path = event.getPath();
                switch (event.getType()) {
                case Event.NODE_REMOVED:
                    synchronized (getShellSession().mutex) {
                        Iterator<Map.Entry<String, SortedSet<String>>> tail = getShellSession().nodeNameCache.tailMap(path).entrySet()
                                .iterator();
                        while (tail.hasNext()) {
                            Map.Entry<String, SortedSet<String>> entry = tail.next();
                            if (!entry.getKey().startsWith(path)) {
                                break;
                            }
                            tail.remove();
                        }
                        tail = getShellSession().propertyNameCache.tailMap(path).entrySet().iterator();
                        while (tail.hasNext()) {
                            Map.Entry<String, SortedSet<String>> entry = tail.next();
                            if (!entry.getKey().startsWith(path)) {
                                break;
                            }
                            tail.remove();
                        }
                    }
                case Event.NODE_ADDED:
                    paths.add(path);
                    // fall through
                case Event.PROPERTY_ADDED:
                case Event.PROPERTY_CHANGED:
                case Event.PROPERTY_REMOVED:
                    path = path.substring(0, path.lastIndexOf('/'));
                    paths.add(path);
                    break;

                default:
                    JcrShellPrinter.printWarnln("Unknown event type: " + event.getType());
                    break;
                }
            } catch (RepositoryException e) {
                JcrShellPrinter.printWarnln("Error while updating cache: " + e.getMessage());
            }
        }
        for (String path : paths) {
            JcrWrapper.removeFromCache(path);
        }
    }

    public static String getStatus() throws RepositoryException {
        if (!isConnected()) {
            return getShellSession().username + "@" + getShellSession().server + " not connected.";
        } else {
            return getShellSession().username + "@" + getShellSession().server + ", session: " + getShellSession().session.getClass().getSimpleName() + ", pendingChanges: "
                    + getShellSession().session.hasPendingChanges();
        }
    }

    public static void connect() {
        if (isConnected()) {
            return;
        }

        // get the repository login and get getShellSession()
        String msg;
        Throwable cause;
        String repositoryAddress = getServer();

        try {
            JcrShellPrinter.println("");
            TextOutput text = Output.out();

            if (isHippoRepository) {
                text = text.a("Connecting to Hippo Repository at '" + repositoryAddress + "' : ");
                HippoRepository repository = HippoRepositoryFactory.getHippoRepository(repositoryAddress);
                getShellSession().session = repository.login(new SimpleCredentials(getUsername(), getPassword()));
            } else {
                if (repositoryAddress.startsWith("http:") || repositoryAddress.startsWith("https:")) {
                    Jcr2davRepositoryFactory factory = new Jcr2davRepositoryFactory();
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(JcrUtils.REPOSITORY_URI, repositoryAddress);
                    Repository repository = factory.getRepository(params);
                    getShellSession().session = repository.login(new SimpleCredentials(getUsername(), getPassword()));
                } else {
                    text = text.a("Connecting to JCR Repository at '" + repositoryAddress + "' : ");
                    ClientRepositoryFactory factory = new ClientRepositoryFactory();
                    Repository repository = factory.getRepository(repositoryAddress);
                    getShellSession().session = repository.login(new SimpleCredentials(getUsername(), getPassword()));
                }
            }

            setConnected(true);
            setCurrentNode(getShellSession().session.getRootNode());
            JcrShellPrinter.print(text.ok("done."));

            // start listener for caches
            ObservationManager obMgr = getShellSession().session.getWorkspace().getObservationManager();
            getShellSession().cacheListener = new EventListener() {
                public void onEvent(EventIterator events) {
                    JcrWrapper.updateCaches(events);
                }
            };
            obMgr.addEventListener(getShellSession().cacheListener, Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED
                    | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED, "/", true, null, null, true);
            clearCaches();
            return;
        } catch (RemoteRepositoryException e) {
            if (e.getCause() == null) {
                msg = "Error: " + e.getMessage();
                cause = e;
            } else {
                msg = "Error: " + e.getCause().getMessage();
                cause = e.getCause();
            }
        } catch (RemoteException e) {
            if (e.getCause() == null) {
                msg = "Remote exception: " + e.getMessage();
                cause = e;
            } else {
                msg = "Remote exception, cause: " + e.getCause().getMessage();
                cause = e.getCause();
            }
        } catch (LoginException e) {
            msg = "Login failed: " + e.getMessage();
            cause = e;
        } catch (RepositoryException e) {
            msg = "Repository exception: " + e.getMessage();
            cause = e;
        } catch (MalformedURLException e) {
            msg = "Repository url incorrect '" + repositoryAddress + "': " + e.getMessage();
            cause = e;
        } catch (ClassCastException e) {
            msg = "Wrong repository type: " + e.getMessage();
            cause = e;
        } catch (NotBoundException e) {
            msg = "Unable to connect to '" + repositoryAddress + "': " + e.getMessage();
            cause = e;
        }

        JcrShellPrinter.printErrorln("failed.");
        throw new NoConnectionException(msg, cause);
    }

    public static void refresh(final boolean keepChanges) throws RepositoryException {
        connect();
        getShellSession().session.refresh(keepChanges);
        clearCaches();
    }

    public static void login() {
        connect();
    }

    public static void logout() {
        if (isConnected()) {
            try {
                ObservationManager obMgr = getShellSession().session.getWorkspace().getObservationManager();
                obMgr.removeEventListener(getShellSession().cacheListener);
            } catch (RepositoryException e) {
                log.info("Error while remove listener.", e);
            }
            getShellSession().session.logout();
            setConnected(false);
            clearCaches();
            getShellSession().destroy();
        }
    }

    public static boolean save() throws RepositoryException {
        connect();
        getShellSession().session.save();
        return true;
    }

    public static Node getCurrentNode() {
        connect();
        return getShellSession().getCurrentNode();
    }

    public static boolean removeNode(final Node node) throws RepositoryException {
        removeFromCache(node.getPath());
        if (node.getDepth() > 0) {
            removeFromCache(node.getParent().getPath());
        }
        node.remove();
        return true;
    }

    public static boolean addNode(final Node parent, final String name, final String nodeType)
            throws RepositoryException {
        removeFromCache(parent.getPath());
        parent.addNode(name, nodeType);
        return true;
    }

    public static boolean moveNode(final Node srcNode, final String destAbsPath) throws RepositoryException {
        connect();
        removeFromCache(srcNode.getPath());
        removeFromCache(srcNode.getParent().getPath());
        getShellSession().session.move(srcNode.getPath(), destAbsPath);
        int lastSlash = destAbsPath.lastIndexOf('/');
        if (lastSlash > 0) {
            removeFromCache(destAbsPath.substring(0, lastSlash - 1));
        } else {
            removeFromCache("/");
        }
        return true;
    }

    public static boolean copyNode(final Node srcNode, final String destAbsPath) throws RepositoryException {
        connect();
        if (!HippoJcrUtils.isHippoSession(getShellSession().session)) {
            JcrShellPrinter.printErrorln("Copy only available in HippoSessions.");
        }
        // Hippo specific..
        Node destNode = ((HippoSession) getShellSession().session).copy(srcNode, destAbsPath);
        removeFromCache(destNode.getParent().getPath());
        return true;
    }

    public static SortedSet<String> getNodeNameList(final Node node) throws RepositoryException {
        connect();
        synchronized (getShellSession().nodeNameCache) {
            if (getShellSession().nodeNameCache.containsKey(node.getPath())) {
                return getShellSession().nodeNameCache.get(node.getPath());
            }
            SortedSet<String> names = new TreeSet<String>();
            if (node.getDepth() != 0) {
                names.add("..");
            }
            NodeIterator iter = node.getNodes();
            while (iter.hasNext()) {
                names.add(fullName(iter.nextNode()));
            }
            getShellSession().nodeNameCache.put(node.getPath(), Collections.unmodifiableSortedSet(names));
            return getShellSession().nodeNameCache.get(node.getPath());
        }
    }

    public static SortedSet<String> getPropertyNameList(final Node node) throws RepositoryException {
        connect();
        synchronized (getShellSession().propertyNameCache) {
            if (getShellSession().propertyNameCache.containsKey(node.getPath())) {
                return getShellSession().propertyNameCache.get(node.getPath());
            }
            SortedSet<String> names = new TreeSet<String>();
            PropertyIterator iter = node.getProperties();
            while (iter.hasNext()) {
                names.add(fullName(iter.nextProperty()));
            }
            getShellSession().propertyNameCache.put(node.getPath(), Collections.unmodifiableSortedSet(names));
            return getShellSession().propertyNameCache.get(node.getPath());
        }
    }

    public static Node resolvePath(final String path) throws RepositoryException {
        if (path == null || path.length() == 0) {
            return getShellSession().getCurrentNode();
        } else if (path.equals(".")) {
            return getShellSession().getCurrentNode();
        } else if (path.equals("/")) {
            return getShellSession().session.getRootNode();
        } else if (path.equals("..")) {
            return getShellSession().getCurrentNode().getParent();
        } else {
            String[] elements = path.split("\\/");
            StringBuffer pathEncoded = new StringBuffer(elements[0]);
            for (int i = 1; i < elements.length; i++) {
                pathEncoded.append("/");
                if ("..".equals(elements[i])) {
                    pathEncoded.append(elements[i]);
                } else {
                    String element = elements[i];
                    String name = element;
                    int index = 1;
                    if (element.indexOf('[') > 0 && element.lastIndexOf(']') == element.length() - 1) {
                        name = element.substring(0, element.lastIndexOf('['));
                        try {
                            index = Integer.parseInt(element.substring(element.lastIndexOf('[') + 1, element
                                    .lastIndexOf(']')));
                        } catch (NumberFormatException nfe) {
                            name = element;
                        }
                    }
                    pathEncoded.append(NodeNameCodec.encode(name) + "[" + index + "]");
                }
            }

            if (path.startsWith("/")) {
                return getShellSession().session.getRootNode().getNode(pathEncoded.toString().substring(1));
            }
            Node refNode = null;
            if (path.indexOf('/') == -1 && getShellSession().getCurrentNode().hasProperty(pathEncoded.toString())) {
                // try reference
                Property p = getShellSession().getCurrentNode().getProperty(pathEncoded.toString());
                if (p.getType() == PropertyType.REFERENCE) {
                    if (p.getDefinition().isMultiple()) {
                        Value[] vals = p.getValues();
                        if (vals.length > 0) {
                            refNode = getShellSession().session.getNodeByUUID(p.getValues()[0].getString());
                        }
                    } else {
                        refNode = getShellSession().session.getNodeByUUID(p.getString());
                    }
                }
            }
            if (refNode == null) {
                return getShellSession().getCurrentNode().getNode(pathEncoded.toString());
            } else {
                return refNode;
            }
        }
    }

    public static boolean cdPrevious() {
        return getShellSession().cdPrevious();
    }

    public static boolean cd(final String path) throws RepositoryException {
        connect();
        try {
            Node node = resolvePath(path);
            setCurrentNode(node);
            return true;
        } catch (PathNotFoundException e) {
            JcrShellPrinter.printWarnln("Path not found: " + path);
            return false;
        }
    }

    public static NodeIterator getNodes(final String path) throws RepositoryException {
        connect();
        return resolvePath(path).getNodes();
    }

    public static PropertyIterator getProperties(final String path) throws RepositoryException {
        connect();
        Node node = null;
        node = resolvePath(path);
        return node.getProperties();
    }

    public static VersionHistory getVersionHistory() throws RepositoryException {
        connect();
        return getShellSession().getCurrentNode().getVersionHistory();
    }

    public static boolean cduuid(final String uuid) throws RepositoryException {
        connect();
        Node node = null;

        try {
            node = getShellSession().session.getNodeByUUID(uuid);
            setCurrentNode(node);
            return true;
        } catch (ItemNotFoundException e) {
            JcrShellPrinter.printWarnln("UUID not found: " + uuid);
            return false;
        }
    }

    public static String finduuid(final String uuid) throws RepositoryException {
        connect();
        Node node = null;

        try {
            node = getShellSession().session.getNodeByUUID(uuid);
            return node.getPath();
        } catch (ItemNotFoundException e) {
            JcrShellPrinter.printWarnln("UUID not found: " + uuid);
            return null;
        }
    }

    public static Map<String, String> getNamespaces() throws RepositoryException {
        connect();
        SortedMap<String, String> namespaces = new TreeMap<String, String>();
        NamespaceRegistry nsReg;
        nsReg = getShellSession().session.getWorkspace().getNamespaceRegistry();
        String[] uris = nsReg.getURIs();
        for (String uri : uris) {
            try {
                if (!"".equals(uri)) {
                    namespaces.put(uri, nsReg.getPrefix(uri));
                }
            } catch (NamespaceException e) {
                JcrShellPrinter.printErrorln("Unable to resolve uri: " + uri);
            }
        }
        return namespaces;
    }

    public static boolean addNamespace(String prefix, String uri) throws RepositoryException {
        connect();
        NamespaceRegistry nsReg;
        nsReg = getShellSession().session.getWorkspace().getNamespaceRegistry();
        try {
            nsReg.registerNamespace(prefix, uri);
            return true;
        } catch (UnsupportedRepositoryOperationException e) {
            JcrShellPrinter.printErrorln("Not supported: " + e.getMessage());
        } catch (AccessDeniedException e) {
            JcrShellPrinter.printErrorln("Not allowed: " + e.getMessage());
        }
        return false;
    }

    public static boolean removeNamespace(String prefix) throws RepositoryException {
        connect();
        NamespaceRegistry nsReg;
        nsReg = getShellSession().session.getWorkspace().getNamespaceRegistry();
        try {
            nsReg.unregisterNamespace(prefix);
            return true;
        } catch (NamespaceException e) {
            JcrShellPrinter.printErrorln("Failed: " + e.getMessage());
        } catch (UnsupportedRepositoryOperationException e) {
            JcrShellPrinter.printErrorln("Not supported: " + e.getMessage());
        } catch (AccessDeniedException e) {
            JcrShellPrinter.printErrorln("Not allowed: " + e.getMessage());
        }
        return false;
    }

    public static NodeType getNodeType(String name) throws RepositoryException {
        connect();
        NodeType nt = null;
        try {
            nt = getShellSession().session.getWorkspace().getNodeTypeManager().getNodeType(name);
        } catch (NoSuchNodeTypeException e) {
            JcrShellPrinter.printErrorln("No such node type: " + name);
        }
        return nt;
    }

    public static SortedSet<String> getNodeTypes(String type) throws RepositoryException {
        connect();
        NodeTypeIterator iter;
        SortedSet<String> types = new TreeSet<String>();

        if ("primary".equals(type)) {
            iter = getShellSession().session.getWorkspace().getNodeTypeManager().getPrimaryNodeTypes();
        } else if ("mixin".equals(type)) {
            iter = getShellSession().session.getWorkspace().getNodeTypeManager().getMixinNodeTypes();
        } else {
            iter = getShellSession().session.getWorkspace().getNodeTypeManager().getAllNodeTypes();
        }
        while (iter.hasNext()) {
            types.add(iter.nextNodeType().getName());
        }
        return types;
    }

    public static QueryResult query(final String statement, final String language) throws RepositoryException {
        connect();
        QueryManager qm;
        qm = getShellSession().session.getWorkspace().getQueryManager();
        Query q = qm.createQuery(statement, language);
        return q.execute();
    }

    public static void exportXml(final String absPath, final OutputStream out, final boolean skipBinary)
            throws IOException, RepositoryException {
        if (HippoJcrUtils.isHippoSession(getShellSession().session)) {
            ((HippoSession) getShellSession().session).exportDereferencedView(absPath, out, skipBinary, false);
        } else {
            getShellSession().session.exportSystemView(absPath, out, skipBinary, false);
        }
    }

    public static void importXml(String parentAbsPath, InputStream in, int uuidBehavior, int referenceBehavior,
            int mergeBehavior) throws IOException, RepositoryException {
        JcrWrapper.removeFromCache(parentAbsPath);
        if (HippoJcrUtils.isHippoSession(getShellSession().session)) {
            ((HippoSession) getShellSession().session).importDereferencedXML(parentAbsPath, in, uuidBehavior, referenceBehavior,
                    mergeBehavior);
        } else {
            getShellSession().session.importXML(parentAbsPath, in, uuidBehavior);
        }
    }

    public static String fullName(final Item item) throws RepositoryException {
        StringBuffer buf = new StringBuffer();
        if (item.getDepth() == 0) {
            buf.append('/');
        }
        String path = item.getPath();
        buf.append(path.substring(path.lastIndexOf('/') + 1));
        return buf.toString();
    }
}
