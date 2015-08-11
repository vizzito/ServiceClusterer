package org.clusterer.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import net.sf.json.JSONObject;

import org.clusterer.util.DataTypeNode;
import org.clusterer.util.OperationNode;
import org.ow2.easywsdl.wsdl.api.Operation;

import weka.clusterers.AbstractClusterer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


public class ClusteringDistanceStrategy extends ClusteringStrategy
{
	protected Instances dataset;
	protected Hashtable<Integer, ArrayList<Instance>> hashClustering = new Hashtable<Integer, ArrayList<Instance>>();

	@Override
	public void generateCluster()
	{

		final ArrayList<String> allTerms = new ArrayList<String>(generateTermsVector());
		final ArrayList<Attribute> atts = new ArrayList<Attribute>();
		int index = 1;
		for (int i = 1; i <= allTerms.size(); i++)
		{
			atts.add(new Attribute("Qtype" + index));
			index++;
		}
		final ArrayList<String> operationNames = new ArrayList<String>();
		// Instances dataset = null;
		dataset = new Instances("distanceClustering", atts, 0);

		for (final Operation op : getOperations())
		{
			operationNames.add(op.getQName().getLocalPart().toString());
			final double[] newInst = new double[dataset.numAttributes()];
			ArrayList<String> terms = new ArrayList<String>();
			terms = flattenOperationTerms(op);
			for (int i = 0; i < allTerms.size(); i++)
			{
				final Integer freq = Collections.frequency(terms, allTerms.get(i));
				newInst[i] = freq;
			}
			dataset.add(new DenseInstance(1.0, newInst));
		}

		try
		{
			final AbstractClusterer clusterer = getClusterer();
			clusterer.buildClusterer(dataset);
			final Hashtable<Integer, List<String>> clusterInstances = new Hashtable<Integer, List<String>>();
			final Hashtable<String, Integer> OpCluster = new Hashtable<String, Integer>();
			for (int i = 0; i < dataset.numInstances(); i++)
			{
				OpCluster.put(operationNames.get(i), clusterer.clusterInstance(dataset.instance(i)));
				if (clusterInstances.get(clusterer.clusterInstance(dataset.instance(i))) == null)
				{
					final List<String> newCluster = new ArrayList<String>();
					newCluster.add(operationNames.get(i));
					clusterInstances.put(clusterer.clusterInstance(dataset.instance(i)), newCluster);
				}
				else
				{
					clusterInstances.get(clusterer.clusterInstance(dataset.instance(i))).add(operationNames.get(i));
				}

				final int instance = clusterer.clusterInstance(dataset.instance(i));
				if (hashClustering.get(instance) == null)
				{ // gets the value
					// for an id)
					hashClustering.put(instance, new ArrayList<Instance>());
				}
				hashClustering.get(instance).add(dataset.instance(i));
				hashClustering.put(instance, hashClustering.get(instance));
			}

			final Hashtable<String, DataTypeNode> similarNodes = new Hashtable<String, DataTypeNode>();
			final List<Integer> clusAux1 = new ArrayList<Integer>();
			final List<Integer> clusAux2 = new ArrayList<Integer>();
			for (final Operation op : getOperations())
			{
				final OperationNode operation = new OperationNode(op);
				DataTypeNode input = new DataTypeNode(op.getInput().getElement());
				input.addRelatedOperation(op);
				input.setParameterType(DataTypeNode.INPUT);

				final Integer currentClust = OpCluster.get(op.getQName().getLocalPart());
				if (!clusAux1.contains(currentClust))
				{
					dataTypeNodes.add(input);
					similarNodes.put(op.getQName().getLocalPart() + "-I", input);
					clusAux1.add(currentClust);
				}
				else
				{
					final List<String> opByCluster = clusterInstances.get(currentClust);
					stop: for (final String currentOp : opByCluster)
					{
						if (!currentOp.equals(op.getQName().getLocalPart()))
						{
							for (final Operation op2 : getOperations())
							{
								if (op2.getQName().getLocalPart().equals(currentOp))
								{
									input = similarNodes.get(currentOp + "-I");
									if (input != null)
									{
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

				if (!clusAux2.contains(currentClust))
				{
					dataTypeNodes.add(output);
					similarNodes.put(op.getQName().getLocalPart() + "-O", output);
					clusAux2.add(currentClust);
				}
				else
				{
					final List<String> opByCluster = clusterInstances.get(currentClust);
					stop: for (final String currentOp : opByCluster)
					{
						if (!currentOp.equals(op.getQName().getLocalPart()))
						{
							for (final Operation op2 : getOperations())
							{
								if (op2.getQName().getLocalPart().equals(currentOp))
								{
									output = similarNodes.get(currentOp + "-O");
									if (output != null)
									{
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
		}
		catch (final Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		unmergedNodes.addAll(dataTypeNodes);
		mergeOperations();

	}

	private HashSet<String> generateTermsVector()
	{
		final HashSet<String> uniqueTerms = new HashSet<String>();
		for (final Operation op : getOperations())
		{
			uniqueTerms.addAll(flattenOperationTerms(op));
		}
		return uniqueTerms;
	}

	private ArrayList<String> flattenOperationTerms(final Operation op)
	{
		final ArrayList<String> terms = new ArrayList<String>();
		final DataTypeNode d1 = new DataTypeNode(op.getInput().getElement());
		final Hashtable<String, String> childrensInput = d1.internFlattenDataTypes(op.getInput().getElement());
		final Hashtable<String, String> childrensOutput = d1.internFlattenDataTypes(op.getOutput().getElement());

		terms.add(op.getQName().getLocalPart().toString());
		terms.add(op.getInput().getElement().getQName().getLocalPart());
		terms.add(op.getOutput().getElement().getQName().getLocalPart());
		for (final String children : childrensInput.keySet())
		{
			terms.add(children);
			terms.add(String.valueOf(childrensInput.get(children)));
		}
		for (final String children : childrensOutput.keySet())
		{
			terms.add(children);
			terms.add(String.valueOf(childrensOutput.get(children)));
		}
		return terms;
	}

	@Override
	public JSONObject validateCluster()
	{
		final JSONObject json = new JSONObject();
		json.element("squaredError", 0);
		json.element("intraDistance", 0);
		json.element("interDistance", 0);

		return json;
	}

}
