/*
 *  Copyright 2009-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.forge.jcrrunner;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.rmi.client.ClientRepositoryFactory;
import org.apache.jackrabbit.rmi.client.RemoteRepositoryException;
import org.apache.jackrabbit.rmi.client.RemoteRuntimeException;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.hippoecm.repository.api.HippoNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to deal with raw JCR
 */
public final class JcrHelper {

    private static final Logger log = LoggerFactory.getLogger(JcrHelper.class);

    private static String server;

    private static String username;

    private static char[] password;

    private static Session session;

    private static boolean connected;

    private static boolean isHippoRepository = true;

    private JcrHelper() {
    }

    public static void setHippoRepository(final boolean isHippo) {
        isHippoRepository = isHippo;
    }

    public static boolean isHippoRepository() {
        return isHippoRepository;
    }

    public static void setConnected(final boolean connected) {
        JcrHelper.connected = connected;
    }

    public static char[] getPassword() {
        return password.clone();
    }

    public static void setPassword(final String password) {
        JcrHelper.password = password.toCharArray();
    }

    public static String getServer() {
        return server;
    }

    public static void setServerUrl(final String server) {
        JcrHelper.server = server;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(final String username) {
        JcrHelper.username = username;
    }

    public static boolean isConnected() {
        try {
            if (session != null && session.isLive()) {
                return true;
            }
        } catch (RemoteRuntimeException e) {
            log.error("Error communicating with server. ", e);
            setConnected(false);
        }
        return connected;
    }

    public static String getStatus() {
        if (!isConnected()) {
            return username + "@" + server + " connected: " + connected;
        } else {
            return username + "@" + server + " session: " + session.getClass();
        }
    }

    public static boolean ensureConnected() {
        if (isConnected()) {
            return true;
        }
        // get the repository login and get session
        try {
            if (isHippoRepository()) {
                log.info("Connecting to Hippo Repository at '" + getServer() + "' : ");
                HippoRepository repository = HippoRepositoryFactory.getHippoRepository(getServer());
                session = repository.login(new SimpleCredentials(getUsername(), getPassword()));
            } else {
                log.info("Connecting to JCR Repository at '" + getServer() + "' : ");
                ClientRepositoryFactory factory = new ClientRepositoryFactory();
                Repository repository = factory.getRepository(getServer());
                session = repository.login(new SimpleCredentials(getUsername(), getPassword()));
            }
            setConnected(true);
            log.debug("Connected.");
            return true;
        } catch (RemoteRepositoryException e) {
            log.error("Remote error while connection to server: " + getServer(), e);
        } catch (LoginException e) {
            log.error("Unable to login to server: " + getServer(), e);
        } catch (RepositoryException e) {
            log.error("Error while connection to server: " + getServer(), e);
        } catch (MalformedURLException e) {
            log.error("Invalid connection url: " + getServer(), e);
        } catch (ClassCastException e) {
            log.error("ClassCastException while connection to server: " + getServer(), e);
        } catch (RemoteException e) {
            log.error("RemoteException while connection to server: " + getServer(), e);
        } catch (NotBoundException e) {
            log.error("Server not found in rmi lookup: " + getServer(), e);
        }
        throw new IllegalStateException("No connection to repository.");
    }

    public static void refresh(final boolean keepChanges) {
        ensureConnected();
        try {
            session.refresh(keepChanges);
        } catch (RepositoryException e) {
            log.error("Error while refresing the session.", e);
        }
    }

    public static boolean login() {
        ensureConnected();
        return isConnected();
    }

    public static void disconnect() {
        if (isConnected()) {
            log.info("Disconnecting from '" + getServer() + "' : ");
            session.logout();
            log.debug("Disconnected.");
            setConnected(false);
        }
    }

    public static boolean save() {
        ensureConnected();
        try {
            session.save();
            return true;
        } catch (RepositoryException e) {
            log.error("Error while saving the session.", e);
            return false;
        }
    }

    public static boolean saveAndWait(long savePauseMillis) {
        boolean saveResult = JcrHelper.save();
        try { 
            Thread.sleep(savePauseMillis); 
        } catch (InterruptedException ignored) {
            // ignore
        }
        return saveResult;
    }

    public static boolean isVirtual(Node jcrNode) {
        ensureConnected();
        if (jcrNode == null) {
            return false;
        }
        if (!isHippoRepository()) {
            return false;
        }
        try {
            return ((HippoNode) jcrNode).isVirtual();
        } catch (RepositoryException e) {
            log.error("Error while determining if the node is virtual", e);
            return false;
        }
    }

    public static Node getNode(final String path) throws RepositoryException {
        ensureConnected();
        if (path == null) {
            throw new IllegalArgumentException("Path can not be null");
        }
        if ("/".equals(path) || "".equals(path)) {
            return session.getRootNode();
        } else {
            if (path.startsWith("/")) {
                return session.getRootNode().getNode(path.substring(1));
            } else {
                return session.getRootNode().getNode(path);
            }
        }
    }

    public static Node getRootNode() throws RepositoryException {
        ensureConnected();
        return session.getRootNode();
    }

    public static Session getSession() {
        ensureConnected();
        return session;
    }

    public static String getSavePath(Node node) {
        try {
            return node.getPath();
        } catch (RepositoryException e) {
            log.warn("Unable to get path from node", e);
            return null;
        }
    }

}
