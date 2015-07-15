package org.clusterer.edgebundles.io;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import org.clusterer.ws.handler.ServicesMediator;
import org.ow2.easywsdl.wsdl.api.Operation;
import org.ow2.easywsdl.wsdl.impl.wsdl11.OperationImpl;
import org.springframework.web.multipart.MultipartFile;


public class HEBServiceAdapter implements DataReader
{
	JSONObject validationInfo;
	int numberOfClusters;
	int[] parentNodes;
	int[][] adjacencyList;
	AbstractMap<Integer, String> mapOpsIS = new HashMap<Integer, String>();
	AbstractMap<String, Integer> mapOpsSI = new HashMap<String, Integer>();
	AbstractMap<String, String> mapParentFile = new HashMap<String, String>();

	/**
	 * @param listFiles
	 * @param WSDLLocations
	 * @param botThreshold
	 * @param topThreshold
	 * @param clusteringStrategy
	 * @param clusterCount
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public HEBServiceAdapter(final List<MultipartFile> listFiles, final double botThreshold, final double topThreshold,
			final String clusteringStrategy, final Integer clusterCount) throws IOException
	{

		final ServicesMediator serMed = new ServicesMediator(listFiles, botThreshold, topThreshold);
		serMed.setClusteringStrategy(clusteringStrategy);
		serMed.setClusterNumber(clusterCount);
		serMed.doAllInferences();
		numberOfClusters = serMed.getClusterNumber();
		validationInfo = serMed.getValidationInfo();
		final HashMap<String, Object> clusteredInfo = serMed.getClusteredOperations();
		mapParentFile = (AbstractMap<String, String>) clusteredInfo.get("mapFiles");
		final List<List<Operation>> res = (List<List<Operation>>) clusteredInfo.get("clusterOperations");
		int cantOp = 0;
		for (final Iterator<List<Operation>> i = res.iterator(); i.hasNext();)
		{
			cantOp += i.next().size();
		}

		//raiz + grupos de servicios + cant operaciones
		parentNodes = new int[1 + res.size() + cantOp];
		parentNodes[0] = -1; //raiz sin padre
		for (int i = 1; i <= res.size(); i++)
		{
			parentNodes[i] = 0; //los grupos de operaciones tienen a la raiz "0" como padre
		}

		int index = res.size() + 1;
		int igroup = 1;
		for (final Iterator<List<Operation>> i = res.iterator(); i.hasNext();)
		{
			final List<Operation> group = i.next();
			for (final Iterator<Operation> j = group.iterator(); j.hasNext();)
			{
				final OperationImpl op = (OperationImpl) j.next();
				mapOpsIS.put(index, op.getQName().getLocalPart());
				mapOpsSI.put(op.getQName().getLocalPart(), index);
				parentNodes[index++] = igroup;
			}
			igroup++;
		}

		adjacencyList = new int[getNodesCount()][];

		for (int i = 0; i < adjacencyList.length; i++)
		{
			if (adjacencyList[i] == null)
			{
				final Set<String> relops = serMed.getRelatedOperations(mapOpsIS.get(i));
				if (relops == null)
				{
					adjacencyList[i] = new int[0];
				}
				else
				{
					adjacencyList[i] = new int[relops.size()];
					int j = 0;
					for (final String oper : relops)
					{
						adjacencyList[i][j++] = mapOpsSI.get(oper);
					}
				}
			}
		}

		System.out.println("Parent Nodes");
		for (int i = 0; i < parentNodes.length; i++)
		{
			System.out.print(parentNodes[i]);
		}

		System.out.println("Adjacency List");
		for (int i = 0; i < adjacencyList.length; i++)
		{
			final int[] fila = adjacencyList[i];
			System.out.println("fila:");
			for (int j = 0; j < fila.length; j++)
			{
				System.out.print(fila[j] + ",");
			}
		}

	}

	public int getNodesCount()
	{
		return parentNodes.length;
	}

	public int[] getAdjacentNodes(final int i)
	{
		return adjacencyList[i];
	}

	public int getParent(final int node)
	{
		return parentNodes[node];
	}

	public int[] getParents()
	{
		return parentNodes;
	}

	public int[][] getAdjacencyList()
	{
		return adjacencyList;
	}

	public AbstractMap<String, String> getMapParentFile()
	{
		return mapParentFile;
	}

	public AbstractMap<Integer, String> getNamesMap()
	{
		// TODO Auto-generated method stub
		return mapOpsIS;
	}

	public int getNumberOfClusters()
	{
		// TODO Auto-generated method stub
		return numberOfClusters;
	}

	@Override
	public JSONObject getValidationInfo()
	{
		// TODO Auto-generated method stub
		return validationInfo;
	}


}
