package org.clusterer.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.xml.namespace.QName;

import org.clusterer.util.DataTypeNode;
import org.clusterer.util.OperationNode;
import org.ow2.easywsdl.wsdl.api.Operation;

import weka.clusterers.AbstractClusterer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class ClusteringDistanceStrategy extends ClusteringStrategy{
	
	@Override
	public void generateCluster() {
		
		ArrayList<String> allTerms = new ArrayList<String>(generateTermsVector());
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		int index= 1;
		for(int i=1;i<=allTerms.size();i++){
			atts.add(new Attribute("Qtype"+index));
			index++;
		}
		ArrayList<String> operationNames = new ArrayList<String>();
		Instances dataset = null;
		dataset = new Instances("distanceClustering", atts,0);
			
			for (Operation op : getOperations()) {
				operationNames.add(op.getQName().getLocalPart().toString());
				double[] newInst = new double[dataset.numAttributes()];
				ArrayList<String> terms = new ArrayList<String>();
				terms = flattenOperationTerms(op);
				for(int i=0;i<allTerms.size();i++){
					Integer freq = Collections.frequency(terms,allTerms.get(i));
					newInst[i]=freq;
				}
			dataset.add(new DenseInstance(1.0,newInst));
		}
		
		//StringToWordVector filter = new StringToWordVector();
		try {
		//	filter.setInputFormat(dataset);
		//	Instances dataFiltered = Filter.useFilter(dataset, filter);
			AbstractClusterer clusterer = getClusterer();
		
			clusterer.buildClusterer(dataset);
			
			Hashtable<Integer, List<String>> clusterInstances = new Hashtable<Integer, List<String>>();
			Hashtable<String, Integer> OpCluster = new Hashtable<String, Integer>();
			for (int i = 0; i < dataset.numInstances(); i++) {
				OpCluster.put(operationNames.get(i),
						clusterer.clusterInstance(dataset.instance(i)));
				if (clusterInstances.get(clusterer.clusterInstance(dataset
						.instance(i))) == null) {
					List<String> newCluster = new ArrayList<String>();
					newCluster.add(operationNames.get(i));
					clusterInstances.put(
							clusterer.clusterInstance(dataset.instance(i)),
							newCluster);
				} else {
					clusterInstances.get(
							clusterer.clusterInstance(dataset.instance(i))).add(operationNames.get(i));
				}
			}
			
			Hashtable<String, DataTypeNode> similarNodes = new Hashtable<String, DataTypeNode>();
			List<Integer> clusAux1 = new ArrayList<Integer>();
			List<Integer> clusAux2 = new ArrayList<Integer>();
			for (Operation op : getOperations()) {
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
							for (Operation op2 : getOperations()) {
								if (op2.getQName().getLocalPart().equals(currentOp)) {									
									input = similarNodes.get(currentOp + "-I");
									if(input!=null){
										input.addRelatedOperation(op);
										break stop;
										}
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
							for (Operation op2 : getOperations()) {
								if (op2.getQName().getLocalPart().equals(currentOp)) {
									output = similarNodes.get(currentOp + "-O");
									if(output!=null){
										output.addRelatedOperation(op);
										break stop;
									}
								}

							}
						}
					}
				}

				operation.setInput(input);
				operation.setOutput(output);
				operationNodes.add(operation);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		unmergedNodes.addAll(dataTypeNodes);
		mergeOperations();
		
	}

	private HashSet<String> generateTermsVector() {
		HashSet<String> uniqueTerms = new HashSet<String>();
		for (Operation op : getOperations()) {
			uniqueTerms.addAll(flattenOperationTerms(op));
		}
		return uniqueTerms;
	}

	private ArrayList<String> flattenOperationTerms(Operation op) {
		ArrayList<String> terms = new ArrayList<String>();
		DataTypeNode d1 = new DataTypeNode(op.getInput().getElement());
		Hashtable<String, String> childrensInput = d1.internFlattenDataTypes(op.getInput().getElement());
		Hashtable<String, String> childrensOutput = d1.internFlattenDataTypes(op.getOutput().getElement());

		terms.add(op.getQName().getLocalPart().toString());
		terms.add(op.getInput().getElement().getQName().getLocalPart());
		terms.add(op.getOutput().getElement().getQName().getLocalPart());
		for (String children : childrensInput.keySet()){
			terms.add(children);
			terms.add(String.valueOf(childrensInput.get(children)));
		}
		for (String children : childrensOutput.keySet()){
				terms.add(children);
				terms.add(String.valueOf(childrensOutput.get(children)));
		}
		return terms;
	}

}
