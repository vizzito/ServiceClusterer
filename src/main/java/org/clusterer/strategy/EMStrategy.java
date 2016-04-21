package org.clusterer.strategy;

import java.util.ArrayList;
import java.util.Random;

import net.sf.json.JSONObject;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;


public class EMStrategy extends ClusteringDistanceStrategy
{
	protected int m_executionSlots = 1;
	private final Random m_rr = new Random(10);

	@Override
	public JSONObject validateCluster()
	{
		// getClusterer().
		final EM emClusterer = (EM) getClusterer();

		SimpleKMeans bestK = null;
		double bestSqE = 1.7976931348623157E+308D;
		for (int i = 100; i < 110; ++i)
		{
			final SimpleKMeans sk = new SimpleKMeans();

			try
			{
				sk.setOptions(emClusterer.getOptions());
			}
			catch (final Exception e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			sk.setSeed(i);
			try
			{
				sk.setNumClusters(emClusterer.getNumClusters());
			}
			catch (final Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sk.setNumExecutionSlots(1);
			sk.setDisplayStdDevs(true);
			try
			{
				sk.buildClusterer(dataset);
			}
			catch (final Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (sk.getSquaredError() < bestSqE)
			{
				bestSqE = sk.getSquaredError();
				bestK = sk;
			}

		}
		final Instances centroids = bestK.getClusterCentroids();

		final DistanceFunction euclidean = new EuclideanDistance(dataset);
		final int numClusters = emClusterer.getNumClusters();
		final Double interCluster = getInterCluster(numClusters, euclidean, centroids);
		final Double intraCluster = getIntraCluster(numClusters, euclidean);

		final Double sq = getEsquaredErrors(numClusters, euclidean, centroids);

		final JSONObject json = new JSONObject();
		json.element("squaredError", bestK.getSquaredError());
		json.element("intraDistance", intraCluster);
		json.element("interDistance", interCluster * cutOffValue);
		return json;

	}

	private Double getInterCluster(final int numClusters, final DistanceFunction euclidean, final Instances centroids)
	{
		final double[] avgCluster = new double[numClusters];
		double sum = 0;
		for (int i = 0; i < numClusters; i++)
		{
			sum = 0;
			for (int j = 0; j < numClusters; j++)
			{
				if (j != i)
				{
					sum += euclidean.distance(centroids.get(i), centroids.get(j));
				}
			}
			avgCluster[i] = sum / numClusters;
		}
		return sumArray(avgCluster) / numClusters;
	}


	private Double getIntraCluster(final int numClusters, final DistanceFunction euclidean)
	{

		double sumIntraCluster = 0;
		final double[] avgCluster = new double[numClusters];
		for (int i = 0; i < numClusters; i++)
		{
			final ArrayList<Instance> clusterInstances = hashClustering.get(i);
			final int sizeCluster = clusterInstances.size();
			double[] avgByCluster = null;
			avgByCluster = new double[sizeCluster];
			for (int z = 0; z < sizeCluster; z++)
			{
				double alfa = 0;
				for (int t = 0; t < sizeCluster; t++)
				{
					if (t != z)
					{
						final Double dist = euclidean.distance(clusterInstances.get(t), clusterInstances.get(z));
						alfa += dist;
					}

				}

				if (sizeCluster > 1)
				{
					avgByCluster[z] = alfa / (sizeCluster - 1);
				}
				else
				{
					avgByCluster[z] = alfa;
				}
			}
			if (sizeCluster > 1)
			{
				avgCluster[i] = (sumArray(avgByCluster) / (sizeCluster));
			}
			else
			{
				avgCluster[i] = (sumArray(avgByCluster));
			}
		}
		for (int f = 0; f < numClusters; f++)
		{
			sumIntraCluster += avgCluster[f];
		}
		return sumIntraCluster / numClusters;
	}

	private double sumArray(final double[] a)
	{
		double total = 0;
		for (int i = 0; i < a.length; i++)
		{
			total += a[i];
		}
		return total;
	}

}
