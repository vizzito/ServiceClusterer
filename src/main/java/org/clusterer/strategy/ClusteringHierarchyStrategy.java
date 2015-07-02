package org.clusterer.strategy;

import java.util.LinkedList;
import java.util.List;

import net.sf.json.JSONObject;

import org.clusterer.similarity.ISimilarityFunction;
import org.clusterer.similarity.OverlappingSimilarityFunction;
import org.clusterer.util.DataTypeNode;
import org.clusterer.util.OperationNode;
import org.ow2.easywsdl.wsdl.api.Operation;

public class ClusteringHierarchyStrategy extends ClusteringStrategy {
	private ISimilarityFunction function = new OverlappingSimilarityFunction(
			getThreshold());
	
	public ClusteringHierarchyStrategy() {
		dataTypeNodes = new LinkedList<DataTypeNode>();
		operationNodes = new LinkedList<OperationNode>();
		unmergedNodes = new LinkedList<DataTypeNode>();
		mergedNodes = new LinkedList<DataTypeNode>();
	}
	public ClusteringHierarchyStrategy(List<Operation> operations) {
		this.operations = operations;
		dataTypeNodes = new LinkedList<DataTypeNode>();
		operationNodes = new LinkedList<OperationNode>();
		unmergedNodes = new LinkedList<DataTypeNode>();
		mergedNodes = new LinkedList<DataTypeNode>();
	}

	public ClusteringHierarchyStrategy(List<Operation> operations,
			ISimilarityFunction similarityFunction) {
		this.operations = operations;
		dataTypeNodes = new LinkedList<DataTypeNode>();
		operationNodes = new LinkedList<OperationNode>();
		unmergedNodes = new LinkedList<DataTypeNode>();
		mergedNodes = new LinkedList<DataTypeNode>();
		function = similarityFunction;
	}
	
	@Override
	public void generateCluster() {
		for (Operation op : getOperations()) {
			// Create OperationNode
			OperationNode operation = new OperationNode(op);

			// Set input node
			DataTypeNode input = new DataTypeNode(op.getInput().getElement());
			// DataTypeNode input = new DataTypeNode(op.getInput().getParts());
			input.addRelatedOperation(op);
			input.setParameterType(DataTypeNode.INPUT);

			if (!isSimilar(input))
				dataTypeNodes.add(input);
			else
				input = updateNode(input, op);

			// Set output node
			DataTypeNode output = new DataTypeNode(op.getOutput().getElement());
			// DataTypeNode output = new
			// DataTypeNode(op.getOutput().getParts());
			output.addRelatedOperation(op);
			output.setParameterType(DataTypeNode.OUTPUT);
			if (!isSimilar(output))
				dataTypeNodes.add(output);
			else
				output = updateNode(output, op);

			// Set input & output to the created OperationNode
			operation.setInput(input);
			operation.setOutput(output);
			operationNodes.add(operation);
		}
		unmergedNodes.addAll(dataTypeNodes);
		mergeOperations();

	}

	protected boolean isSimilar(DataTypeNode node) {
		for (DataTypeNode n : dataTypeNodes)
			if (function.getSimilarity(n, node) >= getThreshold())
				return true;
		return false;
	}

	private DataTypeNode getSimilarNode(DataTypeNode node) {
		for (DataTypeNode n : dataTypeNodes) {
			if (function.getSimilarity(n, node) >= getThreshold())
				return n;
		}
		return null;
	}

	public ISimilarityFunction getSimilarityFunction() {
		return function;
	}

	public void setSimilarityFunction(ISimilarityFunction function) {
		this.function = function;
	}
	
	public DataTypeNode updateNode(DataTypeNode node, Operation op) {
		DataTypeNode tempNode = getSimilarNode(node);
		if (tempNode != null) {
			tempNode.addRelatedOperation(op);
			return tempNode;
		}
		return null;
	}
	@Override
	public JSONObject validateCluster() {
		return null;
		// TODO Auto-generated method stub
		
	}
}
