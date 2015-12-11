package org.clusterer.strategy;

import net.sf.json.JSONObject;


public interface IClusteringStrategy
{
	public void generateCluster();

	public JSONObject validateCluster();
}
