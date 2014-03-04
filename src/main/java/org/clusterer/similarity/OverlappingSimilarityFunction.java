package org.clusterer.similarity;

import java.util.Hashtable;

import javax.xml.namespace.QName;

import org.clusterer.util.DataTypeNode;

/**
 * This class allows detect overlapping data between two data types.
 * 
 * @author Gonzalo Salvatierra
 *
 */
public class OverlappingSimilarityFunction extends BaseSimilarityFunction {
	
	/**
	 * Constructor.
	 */
	public OverlappingSimilarityFunction(double threshold) {
		super(threshold);
	}
	
	/**
	 * Returns a count of the overlapping data between two data types.
	 * If the count is less than 5, is considered as 0. 
	 * @param dataType
	 * @param analyzedDataType
	 * @return overlappingDataCount.
	 */
	public double getOverlappingDataProbability(DataTypeNode type1, DataTypeNode type2) {
		int overlappingDataCount = 0;
		Hashtable<QName, QName> childrens1 = type1.flattenDataTypes();
		Hashtable<QName, QName> childrens2 = type2.flattenDataTypes();
		for (QName children : childrens1.keySet())
			if (isIncluded(children, childrens1.get(children), childrens2))
				overlappingDataCount++;
		return (2 * overlappingDataCount) / ((double)(childrens1.size() + childrens2.size()));
	}
	
	private boolean isIncluded(QName element, QName type, Hashtable<QName, QName> childrens) {
		for (QName e : childrens.keySet())
			//If the elements appear within both lists, it is considered as a hit
			if (e.equals(element) && childrens.get(e).equals(type))
				return true;
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see clusterer.similarity.BaseSimilarityFunction#getSimilarity(clusterer.util.DataTypeNode, clusterer.util.DataTypeNode)
	 */
	@Override
	public double getSimilarity(DataTypeNode type1, DataTypeNode type2) {
		return getOverlappingDataProbability(type1, type2);
	}
}
