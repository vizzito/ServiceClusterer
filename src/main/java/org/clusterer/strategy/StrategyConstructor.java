package org.clusterer.strategy;


public class StrategyConstructor {
	
	
	public static ClusteringStrategy getStrategy(String strategyName){
		switch(strategyName){
		case "hierarchy":return new ClusteringHierarchyStrategy();
		case "kmeans":return new ClusteringKmeansStrategy();
		default:return null;
		}
		
}
}
