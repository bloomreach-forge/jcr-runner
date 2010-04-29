/*
 *  Copyright 2009 Hippo.
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

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.rmi.client.ClientRepositoryFactory;
import org.apache.jackrabbit.rmi.client.RemoteRepositoryException;
import org.apache.jackrabbit.rmi.client.RemoteRuntimeException;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.hippoecm.repository.api.HippoNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final public class JcrHelper {

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

    public static String getStatus() {
        if (!isConnected()) {
            return username + "@" + server + " connected: " + connected;
        } else {
            return username + "@" + server + " session: " + session.getClass();
        }
    }

    public static boolean connect() {
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
        return false;
    }

    public static void refresh(final boolean keepChanges) {
        if (connect()) {
            try {
                session.refresh(keepChanges);
            } catch (RepositoryException e) {
                log.error("Error while refresing the session.", e);
            }
        }
    }

    public static boolean login() {
        return connect();
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
        if (connect()) {
            try {
                session.save();
                return true;
            } catch (AccessDeniedException e) {
                log.error("Error while saving the session.", e);
            } catch (ItemExistsException e) {
                log.error("Error while saving the session.", e);
            } catch (ConstraintViolationException e) {
                log.error("Error while saving the session.", e);
            } catch (InvalidItemStateException e) {
                log.error("Error while saving the session.", e);
            } catch (VersionException e) {
                log.error("Error while saving the session.", e);
            } catch (LockException e) {
                log.error("Error while saving the session.", e);
            } catch (NoSuchNodeTypeException e) {
                log.error("Error while saving the session.", e);
            } catch (RepositoryException e) {
                log.error("Error while saving the session.", e);
            }
        }
        return false;
    }

    public static boolean isVirtual(Node jcrNode) {
        if (jcrNode == null) {
            return false;
        }
        if (!isHippoRepository()) {
            return false;
        }
        HippoNode hippoNode = (HippoNode) jcrNode;
        try {
            Node canonical = hippoNode.getCanonicalNode();
            if (canonical == null) {
                return true;
            }
            return !hippoNode.getCanonicalNode().isSame(hippoNode);
        } catch (RepositoryException e) {
            log.error("Error while determining if the node is virtual", e);
            return false;
        }
    }

    public static Node getNode(final String path) throws PathNotFoundException, RepositoryException {
        if (!connect()) {
            return null;
        }
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

    public static final Node getRootNode() throws RepositoryException {
        return session.getRootNode();
    }

}
