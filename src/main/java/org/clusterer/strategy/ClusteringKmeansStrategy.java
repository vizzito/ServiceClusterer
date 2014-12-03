package org.clusterer.strategy;

import java.io.Console;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.namespace.QName;

import org.clusterer.util.DataTypeNode;
import org.clusterer.util.OperationNode;
import org.ow2.easywsdl.wsdl.api.Operation;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class ClusteringKmeansStrategy extends ClusteringStrategy{

	@Override
	public void generateCluster() {
	//	TextDirectoryToArff tdta = new TextDirectoryToArff();
		Instances dataset = null;
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		atts.add(new Attribute("operation",(ArrayList<String>)null));
		atts.add(new Attribute("input",(ArrayList<String>)null));
		atts.add(new Attribute("output",(ArrayList<String>)null));
		
		
		
		
		

		//Instances dataset = new Instances("text_files_in_kmeans", atts,100000000);

		//for (Operation op : getOperations()) {
			
			DataTypeNode d = new DataTypeNode(getOperations().iterator().next().getInput().getElement());
			Hashtable<QName, QName> childrens1 = d.flattenDataTypes();
			int index= 0;
			for (QName children : childrens1.keySet()){
				atts.add(new Attribute("Qtype"+index,(ArrayList<String>)null));
				index++;
	//			newInst.add((double) dataset.attribute(1).addStringValue(children.getLocalPart()));
			}
			dataset = new Instances("text_files_in_kmeans", atts,0);
			int base_index = index+3;
			for (Operation op : getOperations()) {
			
				double[] newInst = new double[dataset.numAttributes()];
			
			
			
			//double[] newInst = new double[3];
			newInst[0] = dataset.attribute(0).addStringValue(op.getQName().getLocalPart().toString());// .substring(0, 5));
			newInst[1] = dataset.attribute(1).addStringValue(op.getInput().getElement().getQName().getLocalPart().toString());
			newInst[2] = dataset.attribute(2).addStringValue(op.getOutput().getElement().getQName().getLocalPart().toString());
			index= 3;
			
			
			for (QName children : childrens1.keySet()){
				if(children.getLocalPart().length()>5){
					newInst[index] = dataset.attribute(index).addStringValue(children.getLocalPart().substring(0,5));
				}
				else{
					newInst[index] = dataset.attribute(index).addStringValue(children.getLocalPart());
				}
				index++;
			}
			
			dataset.add(new DenseInstance(1.0,newInst));
			
			//dataset.add(new DenseInstance(1.0, newInst));
				
//				
//				ArrayList<Attribute> atts2 = new ArrayList<Attribute>(12);
//		        ArrayList<String> classVal = new ArrayList<String>();
//		        classVal.add("A");
//		        classVal.add("B");
//		        atts2.add(new Attribute("content1",(ArrayList<String>)null));
//		        atts2.add(new Attribute("content2",(ArrayList<String>)null));
//		        atts2.add(new Attribute("content3",(ArrayList<String>)null));
//		        atts2.add(new Attribute("content4",(ArrayList<String>)null));
//		        atts2.add(new Attribute("content5",(ArrayList<String>)null));
//		        atts2.add(new Attribute("content6",(ArrayList<String>)null));
//		        atts2.add(new Attribute("content7",(ArrayList<String>)null));
//		        atts2.add(new Attribute("content8",(ArrayList<String>)null));
//		        atts2.add(new Attribute("content9",(ArrayList<String>)null));
//		        atts2.add(new Attribute("content10",(ArrayList<String>)null));
//		        atts2.add(new Attribute("content11",(ArrayList<String>)null));
//		        atts2.add(new Attribute("@@class@@",classVal));
//
//		        Instances dataRaw = new Instances("TestInstances",atts2,0);
//		        System.out.println("Before adding any instance");
//		        System.out.println("--------------------------");
//		        System.out.println(dataRaw);
//		        System.out.println("--------------------------");
//
//		        double[] instanceValue1 = new double[dataRaw.numAttributes()];
//
//		        instanceValue1[0] = dataRaw.attribute(0).addStringValue("ObtenerDatosEstadoCivil");
//		        instanceValue1[1] = dataRaw.attribute(1).addStringValue("ObtenerDatosEstadoCivil");
//		        instanceValue1[2] = dataRaw.attribute(2).addStringValue("ObtenerDatosEstadoCivilResponse");
//		        instanceValue1[3] = dataRaw.attribute(3).addStringValue("This idvsfs a string!");
//		        instanceValue1[4] = dataRaw.attribute(4).addStringValue("This ivccs a string!");
//		        instanceValue1[5] = dataRaw.attribute(5).addStringValue("This ivvs a string!");
//		        instanceValue1[6] = dataRaw.attribute(6).addStringValue("This ccis a string!");
//		        instanceValue1[7] = dataRaw.attribute(7).addStringValue("This weris a string!");
//		        instanceValue1[8] = dataRaw.attribute(8).addStringValue("This is fea string!");
//		        instanceValue1[9] = dataRaw.attribute(9).addStringValue("Thsdfis is a string!");
//		        instanceValue1[10] = dataRaw.attribute(10).addStringValue("This is a strsdfing!");
//		        instanceValue1[11] = 0;
//
//		        dataRaw.add(new DenseInstance(1.0, instanceValue1));
//
//		        System.out.println("After adding a instance");
//		        System.out.println("--------------------------");
//		        System.out.println(dataRaw);
//		        System.out.println("--------------------------");
//
//		        double[] instanceValue2 = new double[dataRaw.numAttributes()];
//
//		        instanceValue2[0] = dataRaw.attribute(0).addStringValue("This is second string!");
//		        instanceValue2[1] = 1;
//
//		        dataRaw.add(new DenseInstance(1.0, instanceValue2));
//
//		        System.out.println("After adding second instance");
//		        System.out.println("--------------------------");
//		        System.out.println(dataRaw);
//		        System.out.println("--------------------------");
		        
		        
		        
			
//			ArrayList<Double> newInst = new ArrayList<Double>();
//			newInst.add((double) dataset.attribute(0).addStringValue(op.getQName().getLocalPart()));
//			newInst.add((double) dataset.attribute(1).addStringValue(op.getInput().getElement().getQName().getLocalPart().substring(0, 5)));
//			newInst.add((double) dataset.attribute(2).addStringValue(op.getOutput().getElement().getQName().getLocalPart().substring(0, 5)));
		//	dataset.add(new DenseInstance(1.0,newInst));
			
			
			/********************/
			//DataTypeNode d = new DataTypeNode(op.getInput().getElement());
			//Hashtable<QName, QName> childrens1 = d.flattenDataTypes();
			
//			for (QName children : childrens1.keySet()){
//				if(children.getLocalPart().length()>5)
//				newInst.add((double) dataset.attribute(index).addStringValue(children.getLocalPart().substring(0,5)));
//				else
//					newInst.add((double) dataset.attribute(index).addStringValue(children.getLocalPart()));
//					
//			}
			
		//	double[] instArray = new double[newInst.length];
//			for(int i = 0;i<instArray.length;i++){
//				instArray[i]=newInst.get(i);
//			}
//			try{
//				new DenseInstance(1.0,instArray).setDataset(dataset);
//			    dataset.add(new DenseInstance(1.0,instArray));
//			}
//			catch(Exception e){
//				System.out.println(e.getStackTrace());
//			}
			
			/********************/

		}
		
		StringToWordVector filter = new StringToWordVector();
		try {
			filter.setInputFormat(dataset);
			Instances dataFiltered = Filter.useFilter(dataset, filter);
			SimpleKMeans kmeans = new SimpleKMeans();
			kmeans.setNumClusters(12);
			kmeans.buildClusterer(dataFiltered);
			
			
//			for (int i = 0; i < dataFiltered.numInstances(); i++) {
//				System.out.println(dataFiltered.instance(i));
//				System.out.println(dataset.instance(i).dataset().get(i)
//						.stringValue(0));
//				System.out.println(" is in cluster ");
//				System.out
//						.println(kmeans.clusterInstance(dataFiltered.instance(i)) + 1);
//			}
			
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
	//		int i = 0;
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
