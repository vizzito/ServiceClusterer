package org.clusterer.strategy;

import java.util.LinkedList;
import java.util.List;

import net.sf.json.JSONObject;

import org.clusterer.similarity.ISimilarityFunction;
import org.clusterer.similarity.OverlappingSimilarityFunction;
import org.clusterer.util.DataTypeNode;
import org.clusterer.util.OperationNode;
import org.ow2.easywsdl.wsdl.api.Operation;


public class ClusteringHierarchyStrategy extends ClusteringStrategy
{
	private ISimilarityFunction function = new OverlappingSimilarityFunction(getThreshold());

	public ClusteringHierarchyStrategy()
	{
		dataTypeNodes = new LinkedList<DataTypeNode>();
		operationNodes = new LinkedList<OperationNode>();
		unmergedNodes = new LinkedList<DataTypeNode>();
		mergedNodes = new LinkedList<DataTypeNode>();
	}

	public ClusteringHierarchyStrategy(final List<Operation> operations)
	{
		this.operations = operations;
		dataTypeNodes = new LinkedList<DataTypeNode>();
		operationNodes = new LinkedList<OperationNode>();
		unmergedNodes = new LinkedList<DataTypeNode>();
		mergedNodes = new LinkedList<DataTypeNode>();
	}

	public ClusteringHierarchyStrategy(final List<Operation> operations, final ISimilarityFunction similarityFunction)
	{
		this.operations = operations;
		dataTypeNodes = new LinkedList<DataTypeNode>();
		operationNodes = new LinkedList<OperationNode>();
		unmergedNodes = new LinkedList<DataTypeNode>();
		mergedNodes = new LinkedList<DataTypeNode>();
		function = similarityFunction;
	}


	public void generateCluster()
	{
		for (final Operation op : getOperations())
		{
			// Create OperationNode
			final OperationNode operation = new OperationNode(op);

			// Set input node
			DataTypeNode input = new DataTypeNode(op.getInput().getElement());
			// DataTypeNode input = new DataTypeNode(op.getInput().getParts());
			input.addRelatedOperation(op);
			input.setParameterType(DataTypeNode.INPUT);

			if (!isSimilar(input))
			{
				dataTypeNodes.add(input);
			}
			else
			{
				input = updateNode(input, op);
			}

			DataTypeNode output = new DataTypeNode(op.getOutput().getElement());
			output.addRelatedOperation(op);
			output.setParameterType(DataTypeNode.OUTPUT);
			if (!isSimilar(output))
			{
				dataTypeNodes.add(output);
			}
			else
			{
				output = updateNode(output, op);
			}

			// Set input & output to the created OperationNode
			operation.setInput(input);
			operation.setOutput(output);
			operationNodes.add(operation);
		}
		unmergedNodes.addAll(dataTypeNodes);
		mergeOperations();

	}

	protected boolean isSimilar(final DataTypeNode node)
	{
		for (final DataTypeNode n : dataTypeNodes)
		{
			if (function.getSimilarity(n, node) >= getThreshold())
			{
				return true;
			}
		}
		return false;
	}

	private DataTypeNode getSimilarNode(final DataTypeNode node)
	{
		for (final DataTypeNode n : dataTypeNodes)
		{
			if (function.getSimilarity(n, node) >= getThreshold())
			{
				return n;
			}
		}
		return null;
	}

	public ISimilarityFunction getSimilarityFunction()
	{
		return function;
	}

	public void setSimilarityFunction(final ISimilarityFunction function)
	{
		this.function = function;
	}

	public DataTypeNode updateNode(final DataTypeNode node, final Operation op)
	{
		final DataTypeNode tempNode = getSimilarNode(node);
		if (tempNode != null)
		{
			tempNode.addRelatedOperation(op);
			return tempNode;
		}
		return null;
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
