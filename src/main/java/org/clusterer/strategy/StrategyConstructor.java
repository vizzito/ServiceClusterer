package org.clusterer.strategy;

import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;


public class StrategyConstructor
{


	public static ClusteringStrategy getStrategy(final String strategyName, final Integer numberCluster) throws Exception
	{
		ClusteringStrategy strategy;
		switch (strategyName)
		{
			case "hierarchy":
				return new ClusteringHierarchyStrategy();
			case "kmeans":
			{
				final SimpleKMeans kmeans = new SimpleKMeans();
				kmeans.setNumClusters(numberCluster);
				strategy = new KmeansStrategy();
				strategy.setClusterer(kmeans);
				return strategy;
			}
			case "em":
			{
				final EM em = new EM();
				em.setNumClusters(numberCluster);
				strategy = new EMStrategy();
				strategy.setClusterer(em);
				return strategy;
			}
			default:
				return null;
		}

	}
}
