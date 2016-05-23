package org.onehippo.forge.jcrshell;

import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.config.ConfigurationException;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;

public abstract class JcrTest {

    static TransientRepository repository;

    private static Session session;
    private Node testRoot;

    @BeforeClass
    public static void startRepository() throws IOException, ConfigurationException, RepositoryException {
        RepositoryConfig config = RepositoryConfig.create(
                JcrTest.class.getClassLoader().getResourceAsStream(
                "repository.xml"), "repository");
        repository = new TransientRepository(config);
        session = repository.login();
    }

    @AfterClass
    public static void shutdownRepository() {
        session.logout();
    }

    @Before
    public void startSession() throws LoginException, RepositoryException {
        testRoot = session.getRootNode().addNode("test");
        JcrWrapper.setShellSession(new JcrShellSession());
        JcrWrapper.setUsername("test-user");
        JcrWrapper.setCurrentNode(testRoot);
        JcrWrapper.setConnected(true);
    }

    @After
    public void stopSession() throws RepositoryException {
        JcrWrapper.setConnected(false);
        JcrWrapper.setShellSession(null);
        testRoot.remove();
    }

    protected final Session getSession() {
        return session;
    }

    protected final Node getTestRoot() {
        return testRoot;
    }

}
