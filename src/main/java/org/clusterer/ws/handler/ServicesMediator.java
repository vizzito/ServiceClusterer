package org.clusterer.ws.handler;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONObject;

import org.clusterer.strategy.ClusteringStrategy;
import org.clusterer.strategy.StrategyConstructor;
import org.clusterer.util.Pair;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.Operation;
import org.springframework.web.multipart.MultipartFile;


public class ServicesMediator
{

	//private AbstractMap <String,Integer> mapOps= new HashMap<String,Integer>();
	private final ClusteringHandler ch;
	private final OperarionSimilarityHandler osh;
	private final List<MultipartFile> listFiles;
	private Integer clusterNumber;
	private JSONObject validationInfo;
	private HashMap<String, Object> clusteredInfo;
	AbstractMap<Pair, Double> relatedOperations;
	private final double topThreshold;
	private final double bottomThreshold;
	private String clusteringStrategy;

	public ServicesMediator(final List<MultipartFile> listFiles, final double botThreshold, final double topThreshold)
	{
		this.topThreshold = topThreshold;
		this.bottomThreshold = botThreshold;
		this.listFiles = listFiles;
		ch = new ClusteringHandler();
		osh = new OperarionSimilarityHandler();


	}

	private void doClustering() throws Exception
	{
		final ClusteringStrategy strategy = StrategyConstructor.getStrategy(getClusteringStrategy(), getClusterNumber());
		strategy.setThreshold(topThreshold);
		ch.setClustererStrategy(strategy);
		// operaciones agrupadas por similitud
		clusteredInfo = ch.clusterWSDLDocumentsForCluster(listFiles, topThreshold);

		@SuppressWarnings("unchecked")
		final List<List<Operation>> clusteredOperations = (List<List<Operation>>) clusteredInfo.get("clusterOperations");
		System.out.println("Cantidad de Grupos> " + clusteredOperations.size() + "threshold:" + topThreshold);
		int inum = 0;
		List<Operation> lo;

		for (final Iterator<List<Operation>> i = clusteredOperations.iterator(); i.hasNext(); inum++)
		{
			lo = i.next();
			System.out.println("Grupo: " + inum + " cant op: " + lo.size());
			System.out.println("ops: " + lo.toString());
			System.out.println("**************************************");
		}
		setClusterNumber(inum++);
		setValidationInfo(strategy.validateCluster());
	}

	private void doSimilRelations()
	{
		relatedOperations = osh.generateOperationSimilarity(listFiles, 10, bottomThreshold, topThreshold);

		System.out.println("Relaciones entre operaciones");
		for (final Iterator<Entry<Pair, Double>> i = relatedOperations.entrySet().iterator(); i.hasNext();)
		{
			final Entry<Pair, Double> ent = i.next();
			System.out.println("Par" + ent.getKey().getLeft() + " : " + ent.getKey().getRight() + " : " + ent.getValue());

		}

		System.out.println("Relaciones entre operaciones adyacentes: ");
		for (final Iterator<Entry<String, Set<String>>> i = osh.getOpSimilSet().entrySet().iterator(); i.hasNext();)
		{
			final Entry<String, Set<String>> ent = i.next();
			System.out.println("Par ad" + ent.getKey() + " links: " + ent.getValue());
		}
	}

	public void doAllInferences()
	{
		try
		{
			doClustering();
		}
		catch (final Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doSimilRelations();
	}

	public HashMap<String, Object> getClusteredOperations()
	{
		return clusteredInfo;
	}

	public AbstractMap<Pair, Double> getRelatedOperations()
	{
		return relatedOperations;
	}

	public AbstractMap<Operation, Description> getFileDescriptions()
	{
		return ch.getFileDescriptions();
	}

	public Description getOperationURL(final Operation o)
	{
		return getFileDescriptions().get(o);
	}

	public Double getLinkValue(final Pair p)
	{
		return relatedOperations.get(p);
	}

	public Set<String> getRelatedOperations(final String op)
	{
		return osh.getOpSimilSet().get(op);
	}

	public Integer getClusterNumber()
	{
		return clusterNumber;
	}

	public void setClusterNumber(final Integer clusterNumber)
	{
		this.clusterNumber = clusterNumber;
	}

	public String getClusteringStrategy()
	{
		return clusteringStrategy;
	}

	public void setClusteringStrategy(final String clusteringStrategy)
	{
		this.clusteringStrategy = clusteringStrategy;
	}

	public JSONObject getValidationInfo()
	{
		return validationInfo;
	}

	public void setValidationInfo(final JSONObject validationInfo)
	{
		this.validationInfo = validationInfo;
	}
}
