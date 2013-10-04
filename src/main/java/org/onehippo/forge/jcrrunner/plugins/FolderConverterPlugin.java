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
package org.onehippo.forge.jcrrunner.plugins;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Change folder type by moving all subnodes to a new node
 */
public class FolderConverterPlugin extends AbstractRunnerPlugin {

    private static Logger log = LoggerFactory.getLogger(FolderConverterPlugin.class);

    private static final String OLD_TYPE = "hippostd:directory";
    private static final String NEW_TYPE = "hippostd:folder";
    private static final String TMP_NAME = "tmptmptmptmp";


    @Override
    public void init(Session session) {
    }

    public void visit(Node node) {
        try {
            log.debug("Visit node {}", node.getPath());
            if (!node.getPrimaryNodeType().getName().equals(OLD_TYPE)) {
                return;
            }
            log.info("Found folder of type {} : {}", OLD_TYPE, node.getPath());

            Node parent = node.getParent();
            Node newNode = createTmpNode(parent);
            String path = node.getPath();
            String newPath = newNode.getPath();
            
            // move subnodes
            NodeIterator iter = node.getNodes();
            while (iter.hasNext()) {
                Node child = iter.nextNode();
                parent.getSession().move(child.getPath(), newPath + "/" + child.getName());
            }
            
            // remove old node
            node.remove();
            
            // rename tmp
            parent.getSession().move(newNode.getPath(), path);
            
            node.getSession().save();

            log.info("Changed folder " + path + " from type " + OLD_TYPE + " to type " + NEW_TYPE);
        } catch (RepositoryException e) {
            log.error("Error getting node path", e);
        }
    }

    @Override
    public void destroy(Session session) {
    }

    private Node createTmpNode(Node parent) throws RepositoryException {
        Node node = parent.addNode(TMP_NAME, NEW_TYPE);
        node.addMixin("hippo:harddocument");
        node.setProperty("hippostd:foldertype", new String[] {"New Folder Text", "New Text"});
        return node;
    }


}
