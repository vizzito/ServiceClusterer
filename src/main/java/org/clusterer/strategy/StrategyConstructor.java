package org.clusterer.strategy;

import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;


public class StrategyConstructor {
	
	
	public static ClusteringStrategy getStrategy(String strategyName, Integer numberCluster) throws Exception{
		ClusteringStrategy strategy;
		switch(strategyName){
		case "hierarchy":return new ClusteringHierarchyStrategy();
		case "kmeans":{
			SimpleKMeans kmeans = new SimpleKMeans();
			kmeans.setNumClusters(numberCluster);
			strategy = new KmeansStrategy();
			strategy.setClusterer(kmeans);
			return strategy;
		}
		case "em":
			{
				EM em = new EM();
				em.setNumClusters(numberCluster);
				strategy = new ClusteringDistanceStrategy();
				strategy.setClusterer(em);
				return strategy;
			}
		default:return null;
		}
		
}
}
