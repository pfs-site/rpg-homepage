/**
 * 
 */
package org.pfs.de.test.utilities.workflow;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for workflow producers.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class WorkflowProducerFactory {

	/**
	 * Log instance.
	 */
	private static Logger log = LoggerFactory.getLogger(WorkflowProducerFactory.class);
	
	/**
	 * Map of producers for each workflow category.
	 */
	private static Map<String, WorkflowProducer<?>> producers = new HashMap<>();
	
	static {
		registerWorkflowProducer("threepane", new FolderWorkflowProducer());
	}
	
	/**
	 * Get a producer for a workflow category.
	 * @param workflowCategory The workflow category.
	 * @return Workflow producer for the given category.
	 */
	public static WorkflowProducer<?> getProducer(String workflowCategory) {
		if (workflowCategory == null) {
			throw new IllegalArgumentException("Workflow category may not be null");
		}
		if (producers.containsKey(workflowCategory)) {
			return producers.get(workflowCategory);
		} else {
			throw new IllegalArgumentException(String.format("No producer available for workflow category %s", workflowCategory));
		}
	}
	
	/**
	 * Register a new producer for a workflow category.
	 * @param producer Workflow producer.
	 * @param workflowCategory Workflow category.
	 */
	public static void registerWorkflowProducer(String workflowCategory, WorkflowProducer<?> producer) {
		if (producer == null) {
			throw new IllegalArgumentException("Workflow producer may not be null");
		}
		if (workflowCategory == null) {
			throw new IllegalArgumentException("Workflow category may not be null");
		}
		if (producers.containsValue(workflowCategory)) {
			log.info("Replacing producer for workflow category %s with %s", workflowCategory, producer.getClass().getName());
		}
		producers.put(workflowCategory, producer);
	}
}
