package org.clusterer.strategy;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.namespace.QName;

import org.clusterer.util.DataTypeNode;
import org.clusterer.util.OperationNode;
import org.ow2.easywsdl.wsdl.api.Operation;

import weka.clusterers.AbstractClusterer;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class ClusteringDistanceStrategy extends ClusteringStrategy{
	
	@Override
	public void generateCluster() {
		Instances dataset = null;
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		atts.add(new Attribute("operation",(ArrayList<String>)null));
		atts.add(new Attribute("input",(ArrayList<String>)null));
		atts.add(new Attribute("output",(ArrayList<String>)null));
		
			DataTypeNode d = new DataTypeNode(getOperations().iterator().next().getInput().getElement());
			Hashtable<QName, QName> childrens1 = d.flattenDataTypes();
			int index= 0;
			for (QName children : childrens1.keySet()){
				atts.add(new Attribute("Qtype"+index,(ArrayList<String>)null));
				index++;
			}
			dataset = new Instances("text_files_in_kmeans", atts,0);
			for (Operation op : getOperations()) {
			
				double[] newInst = new double[dataset.numAttributes()];
				for(int i = 3;i<dataset.numAttributes();i++){
					newInst[i]=1;
				}
				d = new DataTypeNode(op.getInput().getElement());
				childrens1 = d.flattenDataTypes();
				
			newInst[0] = dataset.attribute(0).addStringValue(op.getQName().getLocalPart().toString());// .substring(0, 5));
			newInst[1] = dataset.attribute(1).addStringValue(op.getInput().getElement().getQName().getLocalPart().toString());
			newInst[2] = dataset.attribute(2).addStringValue(op.getOutput().getElement().getQName().getLocalPart().toString());
			index= 3;
			
			
			for (QName children : childrens1.keySet()){
				if(index<dataset.numAttributes()){
//				if(children.getLocalPart().length()>5){
//					newInst[index] = dataset.attribute(index).addStringValue(children.getLocalPart().substring(0,5));
//				}
//				else{
					newInst[index] = dataset.attribute(index).addStringValue(children.getLocalPart());
			//	}
				index++;
				}
			}
			
			dataset.add(new DenseInstance(1.0,newInst));
		}
		
		StringToWordVector filter = new StringToWordVector();
		try {
			filter.setInputFormat(dataset);
			Instances dataFiltered = Filter.useFilter(dataset, filter);
			
			AbstractClusterer clusterer = getClusterer();
		
			//EM em = new EM();
		//	clusterer.setNumClusters(12);
			clusterer.buildClusterer(dataFiltered);
			
			
			Hashtable<Integer, List<String>> clusterInstances = new Hashtable<Integer, List<String>>();
			Hashtable<String, Integer> OpCluster = new Hashtable<String, Integer>();
			for (int i = 0; i < dataFiltered.numInstances(); i++) {
				OpCluster.put(dataset.instance(i).dataset().get(i).stringValue(0),
						clusterer.clusterInstance(dataFiltered.instance(i)));
				if (clusterInstances.get(clusterer.clusterInstance(dataFiltered
						.instance(i))) == null) {
					List<String> newCluster = new ArrayList<String>();
					newCluster.add(dataset.instance(i).dataset().get(i)
							.stringValue(0));
					clusterInstances.put(
							clusterer.clusterInstance(dataFiltered.instance(i)),
							newCluster);
				} else {
					clusterInstances.get(
							clusterer.clusterInstance(dataFiltered.instance(i))).add(
							dataset.instance(i).dataset().get(i).stringValue(0));
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
							for (Operation op2 : getOperations()) {
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		unmergedNodes.addAll(dataTypeNodes);
		mergeOperations();
		
	}

}
