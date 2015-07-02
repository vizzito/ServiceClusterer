package org.clusterer.strategy;


import java.util.LinkedList;
import java.util.List;

import net.sf.json.JSONObject;

import org.clusterer.similarity.ISimilarityFunction;
import org.clusterer.similarity.OverlappingSimilarityFunction;
import org.clusterer.util.DataTypeNode;
import org.clusterer.util.OperationNode;
import org.ow2.easywsdl.wsdl.api.Operation;

import weka.clusterers.AbstractClusterer;

public abstract class ClusteringStrategy {
	protected List<Operation> operations;
	protected List<DataTypeNode> dataTypeNodes;
	protected List<DataTypeNode> unmergedNodes;
	protected List<DataTypeNode> mergedNodes;
	protected List<OperationNode> operationNodes;
	private double threshold = 0.75;
	private AbstractClusterer clusterer;
	public ClusteringStrategy() {
		dataTypeNodes = new LinkedList<DataTypeNode>();
		operationNodes = new LinkedList<OperationNode>();
		unmergedNodes = new LinkedList<DataTypeNode>();
		mergedNodes = new LinkedList<DataTypeNode>();
	}
	protected void mergeOperations() {
		// System.out.println("nodos iniciales: " + dataTypeNodes.size());
		while (dataTypeNodes.size() > 0) {
			DataTypeNode node = dataTypeNodes.remove(0);
			// System.out.println("remove: " + dataTypeNodes.size());
			List<DataTypeNode> sharedNodes = findSharedNodes(node
					.getRelatedOperations());
			if (sharedNodes.size() == 0) {
				mergedNodes.add(node);
				// System.out.println("add a merge node: " +
				// mergedNodes.size());
			} else {
				for (DataTypeNode n : sharedNodes) {
					DataTypeNode sharedNode = dataTypeNodes
							.remove(dataTypeNodes.indexOf(n));
					// System.out.println("remove shared node: " +
					// dataTypeNodes.size());
					node.addRelatedOperations(sharedNode.getRelatedOperations());
				}
				dataTypeNodes.add(0, node);
				// System.out.println("added node again: " +
				// dataTypeNodes.size());
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



	public List<DataTypeNode> getClusters() {
		return unmergedNodes;
	}

	public List<DataTypeNode> getMergedClusters() {
		return mergedNodes;
	}
	
	
	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}
	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public abstract void generateCluster();
	public abstract JSONObject validateCluster();
	public AbstractClusterer getClusterer() {
		return clusterer;
	}
	public void setClusterer(AbstractClusterer clusterer) {
		this.clusterer = clusterer;
	}
}