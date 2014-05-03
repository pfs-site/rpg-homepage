package org.pfs.de.beans;

import javax.jcr.RepositoryException;
import org.hippoecm.hst.content.beans.ContentNodeBindingException;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.pfs.de.services.model.BaseDocumentRepresentation;

/**
 * Basis for all documents. Provides common methods.
 * @author Martin Dreier
 */
@Node(jcrType = "website:basedocument")
public abstract class BaseDocument extends HippoDocument {
    /**
     * Node type of a mirror node.
     */
    private static final String NODE_TYPE_MIRROR = "hippo:mirror";
    /**
     * Doc base property.
     */
    private static final String NODE_PROPERTY_DOCBASE = "hippo:docbase";

    /**
     * Add a <code>hippo:mirror</code> node. If the node with name 
     * <code>nodeName</code> already exists, it is updated. If it does not yet
     * exist, it will be created.
     * @param node The parent node.
     * @param nodeName The name of the mirror node.
     * @param targetNodeId The UUID or identifier of the target node.
     * @throws ContentNodeBindingException
     * @throws RepositoryException 
     */
    protected void addMirrorNode(javax.jcr.Node node, String nodeName, String targetNodeId) throws ContentNodeBindingException, RepositoryException {
        if (node.hasNode(nodeName)) {
            javax.jcr.Node mirrorNode = node.getNode(nodeName);
            if (!mirrorNode.isNodeType(NODE_TYPE_MIRROR)) {
                throw new ContentNodeBindingException(
                        "Expected node of type 'hippo:mirror' but was'" + mirrorNode.getPrimaryNodeType().getName() + "'  ");
            }
            mirrorNode.setProperty(NODE_PROPERTY_DOCBASE, targetNodeId);
        } else {
            javax.jcr.Node mirrorNode = node.addNode(nodeName, NODE_TYPE_MIRROR);
            mirrorNode.setProperty(NODE_PROPERTY_DOCBASE, targetNodeId);
        }
    }

    /**
     * Update the document content from a representation.
     * @param representation The representation containing the new data.
     */
    public abstract void update(BaseDocumentRepresentation representation) throws RepositoryException;
}
