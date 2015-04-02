/*
 * Based on org.onehippo.forge.utilities.commons.jcrmockup.JcrMockUp from
 * the Hippo Utilities project.
 * 
 *   https://forge.onehippo.org/gf/project/hippo-utilities
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pfs.de.test.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onehippo.forge.utilities.commons.jcrmockup.MockNode;
import org.onehippo.forge.utilities.commons.jcrmockup.MockProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock utils for JCR node tree and session.
 * <p>
 * Howto:
 * Export a branch / node / tree from the hippo cms console as xml and mock it up as a root node / with session.
 * </p>
 * <p>
 * Limitations:
 * <ul>
 *  <li>No support of patterns for getProperty and getNode</li>
 *  <li>No support of type constraints according to cnd (e. g. the property defintions is multiple when there are multiple values present)</li>
 *  <li>javax.jcr.Node#isType() doesn't support sub types</li>
 *  <li>javax.jcr.Value#setValue is not supported</li>
 *  <li>workspaces are not supported</li>
 *  <li>session.copy is not supported</li>
 * </ul>
 * </p>
 * @version $id$
 */
public final class JcrMockUtils {

    private static Logger logger = LoggerFactory.getLogger(JcrMockUtils.class);

    private JcrMockUtils() {
        // private constructor for utility
    }

    /**
     * Construct a mock node from resource data.
     * @param resourceName The name of the resource containing the exported XML of the node data.
     * @return Mock node.
     */
    private static MockNode mockNode(String resourceName) {
        try {
            final InputStream inputStream = JcrMockUtils.class.getResourceAsStream(resourceName);
            try {
                return mockNode(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (Exception exception) {
            logger.error("Error occurred mocking a node for resource: " + resourceName, exception);
        }
        return null;
    }

    /**
     * Construct a mock node from resource data.
     * @param inputStream The exported XML of the node data.
     * @return Mock node.
     */
    private static MockNode mockNode(InputStream inputStream) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(MockNode.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            MockNode rootNode = (MockNode) unmarshaller.unmarshal(inputStream);
            rootNode.buildTree(rootNode);
            return rootNode;
        } catch (Exception exception) {
            logger.error("Error occurred mocking a node for input stream: " + inputStream, exception);
        }
        return null;
    }
    
    /**
     * Create a mocked root node.
     * @return Root node mock.
     */
    private static MockNode mockRootNode() {
    	MockNode.invalidateSession();
        final MockNode rootNode = new MockNode();
        rootNode.setMockNodeName("jcr:root");
        final MockProperty nodeTypeProperty = new MockProperty();
        nodeTypeProperty.setMockPropertyName("jcr:primaryType");
        nodeTypeProperty.setMockValues(Arrays.asList("rep:root"));
        rootNode.setMockProperty(nodeTypeProperty);
        final MockProperty mixinsTypeProperty = new MockProperty();
        mixinsTypeProperty.setMockPropertyName("jcr:mixins");
        mixinsTypeProperty.setMockValues(Arrays.asList("mix:referenceable"));
        rootNode.setMockProperty(mixinsTypeProperty);
        final MockProperty uuidProperty = new MockProperty();
        uuidProperty.setMockPropertyName("jcr:uuid");
        uuidProperty.setMockValues(Arrays.asList("cafebabe-cafe-babe-cafe-babecafebabe"));
        rootNode.setMockProperty(uuidProperty);
        return rootNode;
    }
    
    /**
     * Create a mocked JCR session from a set of resources. Each resource is read into
     * a mock node, and then added below the root node of the mocked repository.
     * @param resources Resource set.
     * @return Session from mocked repository.
     * @throws RepositoryException
     */
    public static Session mockJcrSession(String... resources) throws RepositoryException {
    	//Build root node
    	MockNode rootNode = mockRootNode();
        
        //Read input data
        for (String resource: resources) {
        	MockNode childNode = mockNode(resource);
        	rootNode.addMockChildNode(childNode);
        }
        
        enhanceRepository(rootNode);
        
        //Generate mocked node
        final Node jcrRootNode = rootNode.getJcrMock();
        return jcrRootNode.getSession();
    }

    /**
     * Create a mocked JCR session from a set of input streams. Each stream is read into
     * a mock node, and then added below the root node of the mocked repository.
     * @param inputStreams Input streams.
     * @return Session from mocked repository.
     * @throws RepositoryException
     */
    public static Session mockJcrSession(InputStream... inputStreams) throws RepositoryException {
    	//Build root node
    	MockNode rootNode = mockRootNode();
        
        //Read input data
        for (InputStream in: inputStreams) {
        	MockNode childNode = mockNode(in);
        	rootNode.addMockChildNode(childNode);
        }
        
        //Generate mocked node
        final Node jcrRootNode = rootNode.getJcrMock();
        return jcrRootNode.getSession();
    }
    
    /**
     * Enhance a mocked repository with more mocked function calls.
     * @param rootNode The root node of the repository.
     * @throws RepositoryException
     */
    @SuppressWarnings("deprecation")
	private static void enhanceRepository(MockNode rootNode) throws RepositoryException {
    	Map<String, MockNode> nodes = new HashMap<>();
    	collectUUIDs(nodes, rootNode);
    	UUIDAnswer uuidAnswer = new UUIDAnswer(nodes);
    	
    	Session session = rootNode.getJcrMock().getSession();
    	Mockito.when(session.getNodeByUUID(Matchers.anyString())).thenAnswer(uuidAnswer);
    	Mockito.when(session.getNodeByIdentifier(Matchers.anyString())).thenAnswer(uuidAnswer);
    }
    
    /**
     * Collect all UUIDs from a node tree into a map. Fills a map with this node and all 
     * child nodes. Will be called recursively for all child nodes.
     * @param nodes Map of UUID -&gt; {@link Node}. The map will be filled.
     * @param current Current node.
     * @throws RepositoryException
     */
    private static void collectUUIDs(Map<String, MockNode> nodes, MockNode current) throws RepositoryException {
    	//Collect UUID of current node from properties
    	String uuid = null;
    	final MockProperty uuidProperty = current.getMockProperty("jcr:uuid");
        if (uuidProperty != null) {
            final List<String> values = uuidProperty.getMockValues();
            if (values.size() == 1) {
            	uuid = values.get(0);
            }
        }
        if (uuid != null) {
        	nodes.put(uuid, current);
        }
    	
    	//Collect children
        for (MockNode child: current.getMockChildNodes()) {
    		collectUUIDs(nodes, child);
    	}
    }
    
    /**
	 * Create a mocked JCR session from test content.
	 * @return Mocked session with test content.
	 * @throws RepositoryException Error creating JCR content.
	 * @throws IOException Error reading input files.
	 */
	public static Session mockDefaultJcrSession() throws RepositoryException, IOException {
		//Files containing repository content
		List<String> jcrContentFiles = Arrays.asList("content.xml", "formdata.xml", "hippo_configuration.xml", "hippo_log.xml", "hippo_namespaces.xml", "hst_hst.xml", "jcr_system.xml");
		
		String[] resources = new String[jcrContentFiles.size()];
		int index = 0;
		
		for (String contentFile: jcrContentFiles) {
			resources[index++] = "/test-repository/" + contentFile;
		}
		
		return JcrMockUtils.mockJcrSession(resources);
	}
    
    /**
     * This class can answer requests to find a node by UUID.
     * 
     * @author Martin Dreier <martin@martindreier.de>
     *
     */
    private static class UUIDAnswer implements Answer<Node> {

    	/**
    	 * Map of UUID to node.
    	 */
    	private Map<String, MockNode> nodeMap;

    	/**
    	 * Create a new answer instance.
    	 * @param nodeMap Map of UUID to node.
    	 */
		private UUIDAnswer(Map<String, MockNode> nodeMap) {
			this.nodeMap = nodeMap;
    		
    	}
    	
		@Override
		public Node answer(InvocationOnMock invocation) throws Throwable {
			//Get requested UUID from invocation
			String uuid = null;
			if (invocation.getArguments().length > 0) {
				Object first = invocation.getArguments()[0];
				if (first != null) {
					//Accept any argument type
					uuid = first.toString();
				}
			}
			//Get reply from node map
			if (uuid == null || !nodeMap.containsKey(uuid)) {
				return null;
			} else {
				return nodeMap.get(uuid).getJcrMock(); 
			}
		}
    	
    }
}
