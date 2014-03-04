package org.clusterer.similarity;

import org.clusterer.util.DataTypeNode;

public interface ISimilarityFunction {
	
	public static final double DEFAULT_THRESHOLD = 0.75; 
	
	/**
	 * Returns a double value what denotes the similarity between two data type nodes.
	 * @param type1
	 * @param type2
	 * @return
	 */
	double getSimilarity(DataTypeNode type1, DataTypeNode type2);
}
