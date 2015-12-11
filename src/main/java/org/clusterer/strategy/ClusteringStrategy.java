package org.clusterer.strategy;


import java.util.LinkedList;
import java.util.List;

import org.clusterer.util.DataTypeNode;
import org.clusterer.util.OperationNode;
import org.ow2.easywsdl.wsdl.api.Operation;

import weka.clusterers.AbstractClusterer;


public abstract class ClusteringStrategy implements IClusteringStrategy
{
	protected List<Operation> operations;
	protected List<DataTypeNode> dataTypeNodes;
	protected List<DataTypeNode> unmergedNodes;
	protected List<DataTypeNode> mergedNodes;
	protected List<OperationNode> operationNodes;
	private double threshold = 0.75;
	private AbstractClusterer clusterer;

	public ClusteringStrategy()
	{
		dataTypeNodes = new LinkedList<DataTypeNode>();
		operationNodes = new LinkedList<OperationNode>();
		unmergedNodes = new LinkedList<DataTypeNode>();
		mergedNodes = new LinkedList<DataTypeNode>();
	}

	protected void mergeOperations()
	{
		while (dataTypeNodes.size() > 0)
		{
			final DataTypeNode node = dataTypeNodes.remove(0);
			final List<DataTypeNode> sharedNodes = findSharedNodes(node.getRelatedOperations());
			if (sharedNodes.size() == 0)
			{
				mergedNodes.add(node);
			}
			else
			{
				for (final DataTypeNode n : sharedNodes)
				{
					final DataTypeNode sharedNode = dataTypeNodes.remove(dataTypeNodes.indexOf(n));
					node.addRelatedOperations(sharedNode.getRelatedOperations());
				}
				dataTypeNodes.add(0, node);
			}
		}
	}

	private List<DataTypeNode> findSharedNodes(final List<Operation> operations)
	{
		final List<DataTypeNode> sharedNodes = new LinkedList<DataTypeNode>();
		for (final Operation op : operations)
		{
			for (final DataTypeNode node : findSharedOperation(op))
			{
				if (!isNodeIncluded(sharedNodes, node))
				{
					sharedNodes.add(node);
				}
			}
		}
		return sharedNodes;
	}

	private List<DataTypeNode> findSharedOperation(final Operation op)
	{
		final List<DataTypeNode> relatedNodes = new LinkedList<DataTypeNode>();
		for (int i = 0; i < dataTypeNodes.size(); i++)
		{
			final DataTypeNode node = dataTypeNodes.get(i);
			if (node.contains(op))
			{
				relatedNodes.add(node);
			}
		}
		return relatedNodes;
	}

	private boolean isNodeIncluded(final List<DataTypeNode> nodes, final DataTypeNode node)
	{
		for (final DataTypeNode n : nodes)
		{
			if (n.equals(node))
			{
				return true;
			}
		}
		return false;
	}



	public List<DataTypeNode> getClusters()
	{
		return unmergedNodes;
	}

	public List<DataTypeNode> getMergedClusters()
	{
		return mergedNodes;
	}


	public List<Operation> getOperations()
	{
		return operations;
	}

	public void setOperations(final List<Operation> operations)
	{
		this.operations = operations;
	}

	public double getThreshold()
	{
		return threshold;
	}

	public void setThreshold(final double threshold)
	{
		this.threshold = threshold;
	}


	public AbstractClusterer getClusterer()
	{
		return clusterer;
	}

	public void setClusterer(final AbstractClusterer clusterer)
	{
		this.clusterer = clusterer;
	}
}
