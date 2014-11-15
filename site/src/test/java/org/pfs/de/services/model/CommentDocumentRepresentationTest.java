/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pfs.de.services.model;

import java.util.Date;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.easymock.EasyMock;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.junit.Test;
import org.pfs.de.beans.BlogDocument;
import org.pfs.de.beans.CommentDocument;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CommentDocumentRepresentation}.
 * @author pfs-programmierer
 */
public class CommentDocumentRepresentationTest {
    
    /*
     * Test that data from the bean is copied correctly
     * into the representation.
     * */
    @Test
    public void representBean() throws RepositoryException {
        
        //Test data
        String author = "Test Author";
        String link = "http://www.example.com";
        String text = "Comment text.";
        Date creationDate = new Date();
        BlogDocument referenceDocument = new BlogDocument();
        
        //Create instance under test
        CommentDocumentRepresentation rep = new CommentDocumentRepresentation();
        CommentDocument doc = createCommentDocumentMock(author, link, text, creationDate, referenceDocument);
        replay(doc);
        
        //Perform test
        rep.represent(doc);
        
        //Test representation
        assertEquals("Author incorrect", author, rep.getAuthor());
        assertEquals("Link incorrect", link, rep.getLink());
        assertEquals("Comment text incorrect", text, rep.getText());
        assertEquals("Creation date incorrect", creationDate, rep.getDate());
    }
    
    /**
     * Create a mocked comment document with the given data.
     * @param author Author name.
     * @param link Author link.
     * @param text Comment text.
     * @param date Creation date.
     * @return The mocked comment document. This mock is not switched to replay yet.
     */
    protected CommentDocument createCommentDocumentMock(String author, String link, String text, Date date, HippoDocument refrenceDocument) throws RepositoryException {
        CommentDocument doc = EasyMock.createMock(CommentDocument.class);
        
        //Mock for the node
        NodeType nodeTypeMock = EasyMock.createNiceMock(NodeType.class);
        expect(nodeTypeMock.getName()).andReturn("website:commentdocument");
        Node nodeMock = EasyMock.createNiceMock(Node.class);
        expect(nodeMock.getPrimaryNodeType()).andReturn(nodeTypeMock);
        
        replay(nodeTypeMock);
        replay(nodeMock);
        
        //Generic
        String name = CommentDocument.class.getName();
        expect(doc.getName()).andReturn(name);
        expect(doc.getLocalizedName()).andReturn(name);
        expect(doc.getPath()).andReturn("/comments/" + name);
        expect(doc.getCanonicalUUID()).andReturn(UUID.randomUUID().toString());
        expect(doc.getCanonicalHandleUUID()).andReturn(UUID.randomUUID().toString());
        expect(doc.getNode()).andReturn(nodeMock);
        expect(doc.isLeaf()).andReturn(Boolean.TRUE);
        
        //Comment specific
        expect(doc.getAuthor()).andReturn(author);
        expect(doc.getLink()).andReturn(link);
        expect(doc.getText()).andReturn(text);
        expect(doc.getDate()).andReturn(date);
        expect(doc.getReferencedDocument()).andReturn(refrenceDocument);
        
        return doc;
    }
}
