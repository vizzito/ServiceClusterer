package org.clusterer.similarity;

import org.clusterer.util.DataTypeNode;

public abstract class BaseSimilarityFunction implements ISimilarityFunction {
	
	protected double similarity = 0;
	protected double threshold = 0;
	
	public BaseSimilarityFunction(double threshold) {
		this.threshold = threshold;
	}
	
	public double getThreshold() {
		return threshold;
	}
	
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	
	/*
	 * (non-Javadoc)
	 * @see clusterer.similarity.ISimilarityFunction#getSimilarity(clusterer.util.DataTypeNode, clusterer.util.DataTypeNode)
	 */
	//aca borre este override, nose q onda
	//@Override
	public double getSimilarity(DataTypeNode type1, DataTypeNode type2) {
		return 0;
	}
}
