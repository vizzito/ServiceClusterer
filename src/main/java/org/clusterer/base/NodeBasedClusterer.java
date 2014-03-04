package org.clusterer.base;

import java.util.LinkedList;
import java.util.List;

import org.clusterer.similarity.ISimilarityFunction;
import org.clusterer.similarity.OverlappingSimilarityFunction;
import org.clusterer.util.DataTypeNode;
import org.clusterer.util.OperationNode;
import org.ow2.easywsdl.wsdl.api.Operation;


public class NodeBasedClusterer {
	private List<Operation> operations;
	private List<DataTypeNode> dataTypeNodes;
	private List<DataTypeNode> unmergedNodes;
	private List<DataTypeNode> mergedNodes;
	private List<OperationNode> operationNodes;
	private double threshold = 0.75;
	private ISimilarityFunction function = new OverlappingSimilarityFunction(threshold);
	
	public NodeBasedClusterer(List<Operation> operations) {
		this.operations = operations;
		dataTypeNodes = new LinkedList<DataTypeNode>();
		operationNodes = new LinkedList<OperationNode>();
		unmergedNodes = new LinkedList<DataTypeNode>();
		mergedNodes = new LinkedList<DataTypeNode>();
	}
	
	public NodeBasedClusterer(List<Operation> operations, ISimilarityFunction similarityFunction) {
		this.operations = operations;
		dataTypeNodes = new LinkedList<DataTypeNode>();
		operationNodes = new LinkedList<OperationNode>();
		unmergedNodes = new LinkedList<DataTypeNode>();
		mergedNodes = new LinkedList<DataTypeNode>();
		function = similarityFunction;
	}
	
	public void generateGraph(double threshold) {
		this.threshold = threshold;
		for (Operation op : operations) {
			//Create OperationNode
			OperationNode operation = new OperationNode(op);
			
			//Set input node
			DataTypeNode input = new DataTypeNode(op.getInput().getElement());
//			DataTypeNode input = new DataTypeNode(op.getInput().getParts());
			input.addRelatedOperation(op);
			input.setParameterType(DataTypeNode.INPUT);
			if (!isSimilar(input))
				dataTypeNodes.add(input);
			else
				input = updateNode(input, op);
			
			//Set output node
			DataTypeNode output = new DataTypeNode(op.getOutput().getElement());
//			DataTypeNode output = new DataTypeNode(op.getOutput().getParts());
			output.addRelatedOperation(op);
			output.setParameterType(DataTypeNode.OUTPUT);
			if (!isSimilar(output))
				dataTypeNodes.add(output);
			else
				output = updateNode(output, op);
			
			//Set input & output to the created OperationNode
			operation.setInput(input);
			operation.setOutput(output);
			operationNodes.add(operation);
		}
		unmergedNodes.addAll(dataTypeNodes);
		mergeOperations();
	}
	
	private void mergeOperations() {
//		System.out.println("nodos iniciales: " + dataTypeNodes.size());
		while (dataTypeNodes.size() > 0) {
			DataTypeNode node = dataTypeNodes.remove(0);
	//		System.out.println("remove: " + dataTypeNodes.size());
			List<DataTypeNode> sharedNodes = findSharedNodes(node.getRelatedOperations());
			if (sharedNodes.size() == 0 ) {
				mergedNodes.add(node);
		//		System.out.println("add a merge node: " + mergedNodes.size());
			}
			else {
				for (DataTypeNode n : sharedNodes) {
					DataTypeNode sharedNode = dataTypeNodes.remove(dataTypeNodes.indexOf(n));
			//		System.out.println("remove shared node: " + dataTypeNodes.size());
					node.addRelatedOperations(sharedNode.getRelatedOperations());
				}
				dataTypeNodes.add(0, node);
		//		System.out.println("added node again: " + dataTypeNodes.size());
			}
		}
	}
	
	private List<DataTypeNode> findSharedNodes(List<Operation> operations) {
		List<DataTypeNode> sharedNodes = new LinkedList<DataTypeNode>();
		for (Operation op : operations) {
			for (DataTypeNode node : findSharedOperation(op)) {
				if (!isNodeIncluded(sharedNodes, node))
					sharedNodes.add(node);
			}
		}
		return sharedNodes;
	}
	
	private List<DataTypeNode> findSharedOperation(Operation op) {
		List<DataTypeNode> relatedNodes = new LinkedList<DataTypeNode>();
		for (int i = 0; i < dataTypeNodes.size(); i++) {
			DataTypeNode node = dataTypeNodes.get(i);
			if (node.contains(op)) {
				relatedNodes.add(node);
			}
		}
		return relatedNodes;
	}
	
	private boolean isNodeIncluded(List<DataTypeNode> nodes, DataTypeNode node) {
		for (DataTypeNode n : nodes)
			if (n.equals(node))
				return true;
		return false;
	}
	
	public DataTypeNode updateNode(DataTypeNode node, Operation op) {
		DataTypeNode tempNode = getSimilarNode(node);
		if (tempNode != null) {
			tempNode.addRelatedOperation(op);
			return tempNode;
		}
		return null;
	}
	
	private boolean isSimilar(DataTypeNode node) {
		for (DataTypeNode n : dataTypeNodes)
			if (function.getSimilarity(n, node) >= threshold)
				return true;
		return false;
	}
	
	private DataTypeNode getSimilarNode(DataTypeNode node) {
		for (DataTypeNode n : dataTypeNodes) {
			if (function.getSimilarity(n, node) >= threshold)
				return n;
		}
		return null;
	}
	
	public List<DataTypeNode> getClusters() {
		return unmergedNodes;
	}
	
	public List<DataTypeNode> getMergedClusters() {
		return mergedNodes;
	}
	
	public ISimilarityFunction getSimilarityFunction() {
		return function;
	}
	
	public void setSimilarityFunction(ISimilarityFunction function) {
		this.function = function;
	}
}