/**
 * 
 */
package org.pfs.de.test.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ListIterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.HippoNode;
import org.onehippo.forge.utilities.commons.jcrmockup.MockNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhancement of {@link MockNode} with improved handling of paths and names.
 * Creates {@link HippoNode} objects instead of {@link Node} objects.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class EnhancedMockNode extends MockNode {
	
	/**
	 * Log instance.
	 */
	private static Logger logger = LoggerFactory.getLogger(EnhancedMockNode.class);

	/**
	 * Improved implementation of {@link MockNode#getMockChildNode(String)} which can handle
	 * relative paths.
	 * 
	 * @see org.onehippo.forge.utilities.commons.jcrmockup.MockNode#getMockChildNode(java.lang.String)
	 */
	@Override
	public MockNode getMockChildNode(String name) {
		if (name.equals(".")) {
			return this;
		}
		if (name.equals("..")) {
			return this.getParent();
		}
		if (name.contains("/")) {
			//Node is path, not handled correctly by parent class
			String[] path = name.split("/");
			MockNode child = this;
			for (int pos = 0; pos < path.length; pos++) {
				if (path[pos].equals(".")) {
					continue;
				}
				if (path[pos].equals("..")) {
					child = child.getParent();
				}
				child = child.getMockChildNode(path[pos]);
				if (child == null) {
					//Child not found
					return null;
				}
			}
			return child;
		} else {
			return super.getMockChildNode(name);
		}
	}

	/**
	 * @see org.onehippo.forge.utilities.commons.jcrmockup.MockNode#addMockChildNode(org.onehippo.forge.utilities.commons.jcrmockup.MockNode)
	 */
	@Override
	public void addMockChildNode(MockNode childNode) {
		if (!(childNode instanceof EnhancedMockNode)){
			childNode = enhance(childNode);
		}
		if (childNode.getMockNodeName().contains("/")) {
			//Separate path and name
			int lastSlash = childNode.getMockNodeName().lastIndexOf("/");
			String newNodeName = childNode.getMockNodeName().substring(lastSlash + 1);
			String path = childNode.getMockNodeName().substring(0, lastSlash);
			//Set actual name
			childNode.setMockNodeName(newNodeName);
			//Determine actual parent mock node and add child there
			MockNode actualParent = getMockChildNode(path);
			actualParent.addMockChildNode(childNode);
		} else {
			super.addMockChildNode(childNode);
		}
	}
	
	/**
	 * Convert a {@link MockNode} object into an {@link EnhancedMockNode} object.
	 * 
	 * @param mock Original object.
	 * @return New object.
	 */
	public static EnhancedMockNode enhance(MockNode mock) {
		if (mock instanceof EnhancedMockNode) {
			return (EnhancedMockNode) mock;
		}
		EnhancedMockNode enhancedMock = new EnhancedMockNode();
		
		for (Field field: MockNode.class.getDeclaredFields()) {
			//Skip static fields
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			//Make field writable
			boolean wasAccessible = field.isAccessible();
			field.setAccessible(true);
			
			//Copy field content to enhanced object
			try {
				field.set(enhancedMock, field.get(mock));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error(String.format("Cannot copy field %s to enhanced mock node", field.getName()), e);
			}
			
			//Reset field access
			field.setAccessible(wasAccessible);
		}
		return enhancedMock;
	}

	/**
	 * Recursively enhance a list of mock nodes and their child nodes.
	 *  
	 * @param mockNodes Mock nodes. These entries are replaced by the
	 * enhanced mocks.
	 */
	public static void enhanceChildren(MockNode parent) {
		try {
			Field childNodeField = MockNode.class.getDeclaredField("childNodes");
			childNodeField.setAccessible(true);
			
			@SuppressWarnings("unchecked")
			List<MockNode> mockNodes = (List<MockNode>) childNodeField.get(parent);
			if (mockNodes == null) {
				//Node has no children
				return;
			}
			
			ListIterator<MockNode> iterator = mockNodes.listIterator();
			while (iterator.hasNext()) {
				MockNode mock = iterator.next();
				//Enhance mock and replace list entry
				EnhancedMockNode enhancedMock = enhance(mock);
				iterator.remove();
				iterator.add(enhancedMock);
				enhancedMock.setParent(parent);
				//Recurse into children
				enhanceChildren(mock);
			}
			
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			logger.error(String.format("Cannot enhance children of node %s", parent.getMockNodeName()), e);
		}
	}

	/**
	 * @see org.onehippo.forge.utilities.commons.jcrmockup.MockNode#getJcrMock()
	 */
	@Override
	public Node getJcrMock() throws RepositoryException {
		return JcrMockUtils.wrap(super.getJcrMock());
	}
}
