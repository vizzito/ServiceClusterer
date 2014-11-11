package org.clusterer.base;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.clusterer.services.TextDirectoryToArff;
import org.clusterer.similarity.ISimilarityFunction;
import org.clusterer.similarity.OverlappingSimilarityFunction;
import org.clusterer.util.DataTypeNode;
import org.clusterer.util.OperationNode;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.InterfaceType;
import org.ow2.easywsdl.wsdl.api.Operation;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class NodeBasedClusterer {
	private List<Operation> operations;
	private List<DataTypeNode> dataTypeNodes;
	private List<DataTypeNode> unmergedNodes;
	private List<DataTypeNode> mergedNodes;
	private List<OperationNode> operationNodes;
	private double threshold = 0.75;
	private ISimilarityFunction function = new OverlappingSimilarityFunction(
			threshold);

	public NodeBasedClusterer(List<Operation> operations) {
		this.operations = operations;
		dataTypeNodes = new LinkedList<DataTypeNode>();
		operationNodes = new LinkedList<OperationNode>();
		unmergedNodes = new LinkedList<DataTypeNode>();
		mergedNodes = new LinkedList<DataTypeNode>();
	}

	public NodeBasedClusterer(List<Operation> operations,
			ISimilarityFunction similarityFunction) {
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

	/**
	 * @throws Exception
	 ************************************************/
	public void kmeansTest() throws Exception {
		TextDirectoryToArff tdta = new TextDirectoryToArff();

		ArrayList<Attribute> atts = new ArrayList<Attribute>(3);
		atts.add(new Attribute("operation", (ArrayList<String>) null));
		atts.add(new Attribute("input", (ArrayList<String>) null));
		atts.add(new Attribute("output", (ArrayList<String>) null));

		Instances dataset = new Instances("text_files_in_kmeans", atts,
				100000000);

		for (Operation op : operations) {
			double[] newInst = new double[3];
			newInst[0] = (double) dataset.attribute(0).addStringValue(
					op.getQName().getLocalPart());// .substring(0, 5));
			newInst[1] = (double) dataset.attribute(1).addStringValue(
					op.getInput().getElement().getQName().getLocalPart()
							.substring(0, 5));
			newInst[2] = (double) dataset.attribute(2).addStringValue(
					op.getOutput().getElement().getQName().getLocalPart()
							.substring(0, 5));
			dataset.add(new DenseInstance(1.0, newInst));

		}

		StringToWordVector filter = new StringToWordVector();
		filter.setInputFormat(dataset);
		Instances dataFiltered = Filter.useFilter(dataset, filter);
		SimpleKMeans kmeans = new SimpleKMeans();
		kmeans.setNumClusters(12);
		kmeans.buildClusterer(dataFiltered);

		for (int i = 0; i < dataFiltered.numInstances(); i++) {
			System.out.println(dataFiltered.instance(i));
			System.out.println(dataset.instance(i).dataset().get(i)
					.stringValue(0));
			System.out.println(" is in cluster ");
			System.out
					.println(kmeans.clusterInstance(dataFiltered.instance(i)) + 1);
		}

		Hashtable<Integer, List<String>> clusterInstances = new Hashtable<Integer, List<String>>();
		Hashtable<String, Integer> OpCluster = new Hashtable<String, Integer>();
		for (int i = 0; i < dataFiltered.numInstances(); i++) {
			OpCluster.put(dataset.instance(i).dataset().get(i).stringValue(0),
					kmeans.clusterInstance(dataFiltered.instance(i)));
			if (clusterInstances.get(kmeans.clusterInstance(dataFiltered
					.instance(i))) == null) {
				List<String> newCluster = new ArrayList<String>();
				newCluster.add(dataset.instance(i).dataset().get(i)
						.stringValue(0));
				clusterInstances.put(
						kmeans.clusterInstance(dataFiltered.instance(i)),
						newCluster);
			} else {
				clusterInstances.get(
						kmeans.clusterInstance(dataFiltered.instance(i))).add(
						dataset.instance(i).dataset().get(i).stringValue(0));
			}
		}

		Hashtable<String, DataTypeNode> similarNodes = new Hashtable<String, DataTypeNode>();
		int i = 0;
		List<Integer> clusAux1 = new ArrayList<Integer>();
		List<Integer> clusAux2 = new ArrayList<Integer>();
		for (Operation op : operations) {
			OperationNode operation = new OperationNode(op);
			DataTypeNode input = new DataTypeNode(op.getInput().getElement());
			input.addRelatedOperation(op);
			input.setParameterType(DataTypeNode.INPUT);

			Integer currentClust = OpCluster.get(op.getQName().getLocalPart());
			if (!clusAux1.contains(currentClust)) {
				dataTypeNodes.add(input);
				similarNodes.put(op.getQName().getLocalPart() + "-I", input);
				clusAux1.add(currentClust);
			} else {
				List<String> opByCluster = clusterInstances.get(currentClust);
				stop: for (String currentOp : opByCluster) {
					if (!currentOp.equals(op.getQName().getLocalPart())) {
						for (Operation op2 : operations) {
							if (op2.getQName().getLocalPart().equals(currentOp)) {
								input = similarNodes.get(currentOp + "-I");
								input.addRelatedOperation(op);
								break stop;
							}
						}
					}
				}
			}

			DataTypeNode output = new DataTypeNode(op.getOutput().getElement());
			output.addRelatedOperation(op);
			output.setParameterType(DataTypeNode.OUTPUT);

			if (!clusAux2.contains(currentClust)) {
				dataTypeNodes.add(output);
				similarNodes.put(op.getQName().getLocalPart() + "-O", output);
				clusAux2.add(currentClust);
			} else {
				List<String> opByCluster = clusterInstances.get(currentClust);
				stop: for (String currentOp : opByCluster) {
					if (!currentOp.equals(op.getQName().getLocalPart())) {
						for (Operation op2 : operations) {
							if (op2.getQName().getLocalPart().equals(currentOp)) {
								output = similarNodes.get(currentOp + "-O");
								output.addRelatedOperation(op);
								break stop;
							}

						}
					}
				}
			}

			operation.setInput(input);
			operation.setOutput(output);
			operationNodes.add(operation);

		}
		unmergedNodes.addAll(dataTypeNodes);
		mergeOperations();
	}


	/**************************************************/

	private void mergeOperations() {
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