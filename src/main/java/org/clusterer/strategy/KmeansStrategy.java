package org.clusterer.strategy;

import java.util.ArrayList;

import net.sf.json.JSONObject;
import weka.clusterers.SimpleKMeans;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;


public class KmeansStrategy extends ClusteringDistanceStrategy
{

	@Override
	public JSONObject validateCluster()
	{
		// getClusterer().
		final SimpleKMeans kmeansClusterer = (SimpleKMeans) getClusterer();

		final Instances centroids = kmeansClusterer.getClusterCentroids();
		final DistanceFunction euclidean = new EuclideanDistance(dataset);
		final int numClusters = kmeansClusterer.getNumClusters();
		final Double squaredError = getEsquaredErrors(numClusters, euclidean, centroids);
		final Double squaredError2 = getEsquaredErrors2(numClusters, euclidean, centroids);

		final Double interCluster = getInterCluster(numClusters, euclidean, centroids);
		final Double interCluster2 = getInterCluster2(numClusters, euclidean, centroids);
		final Double interCluster3 = getInterCluster3(numClusters, euclidean, centroids);
		final Double interCluster4 = getInterCluster4(numClusters, euclidean, centroids);
		final Double interCluster5 = getInterCluster5(numClusters, euclidean, centroids);
		final Double intraCluster = getIntraCluster(numClusters, euclidean);

		final JSONObject json = new JSONObject();
		json.element("squaredError", squaredError2);
		json.element("intraDistance", intraCluster);
		json.element("interDistance", interCluster);

		return json;
		//		Double interCluster = getInterCluster(numClusters, euclidean,centroids);
		//		Double interCluster2 = getInterCluster2(numClusters, euclidean, centroids);
		//		Double interCluster3 = getInterCluster3(numClusters, euclidean, centroids);

	}

	private Double getInterCluster(final int numClusters, final DistanceFunction euclidean, final Instances centroids)
	{
		double sum = 0;
		double interCluster = 0;
		for (int i = 0; i < numClusters - 1; i++)
		{
			sum = 0;
			for (int j = i + 1; j < numClusters; j++)
			{
				sum += euclidean.distance(centroids.get(i), centroids.get(j));
			}
			interCluster += sum;
		}
		sum = 0;
		for (int i = 1; i < numClusters - 1; i++)
		{
			sum += i;
		}
		if (numClusters == 1)
		{
			return (double) 0;
		}
		else
		{
			return interCluster / sum;
		}
	}


	private Double getInterCluster2(final int numClusters, final DistanceFunction euclidean, final Instances centroids)
	{
		double sum = 0;
		double interCluster = 0;
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
			interCluster += sum;
		}
		sum = 0;
		for (int i = 1; i < numClusters - 1; i++)
		{
			sum += i;
		}

		return interCluster / 2;
	}

	private Double getInterCluster3(final int numClusters, final DistanceFunction euclidean, final Instances centroids)
	{
		double sum = 0;
		double interCluster = 0;
		for (int i = 0; i < numClusters - 1; i++)
		{

			sum = 0;
			for (int j = i + 1; j < numClusters; j++)
			{
				sum += euclidean.distance(centroids.get(i), centroids.get(j));
			}
			interCluster += sum;
		}
		sum = 0;
		for (int i = 1; i < numClusters - 1; i++)
		{
			sum += i;
		}

		return interCluster;
	}

	// esta es la formula del informe
	private Double getInterCluster4(final int numClusters, final DistanceFunction euclidean, final Instances centroids)
	{
		double sum = 0;
		double interCluster = 0;
		for (int i = 0; i < numClusters - 1; i++)
		{
			sum = 0;
			for (int j = i + 1; j < numClusters; j++)
			{
				sum += euclidean.distance(centroids.get(i), centroids.get(j));
			}
			interCluster += sum;
		}
		sum = 0;
		for (int i = 1; i < numClusters; i++)
		{
			sum += i;
		}
		return interCluster / sum;
	}

	private Double getInterCluster5(final int numClusters, final DistanceFunction euclidean, final Instances centroids)
	{
		double max = 0;
		double min = Double.MAX_VALUE;
		double sum = 0;
		int count = 0;

		for (int i = 0; i < numClusters; i++)
		{
			for (int j = i + 1; j < numClusters; j++)
			{
				final double d = euclidean.distance(centroids.get(i), centroids.get(j));
				min = Math.min(d, min);
				max = Math.max(d, max);
				sum += d;
				count++;
			}
		}

		System.out.println("Maximum Intercluster Distance: " + max);
		System.out.println("Minimum Intercluster Distance: " + min);
		final Double inter = (sum / count - min) / (max - min);

		return inter;
	}

	private Double getIntraCluster(final int numClusters, final DistanceFunction euclidean)
	{

		double intraCluster = 0;
		for (int i = 0; i < numClusters; i++)
		{
			double sum = 0;
			final ArrayList<Instance> clusterInstances = hashClustering.get(i);
			final int sizeCluster = clusterInstances.size();
			for (int z = 0; z < clusterInstances.size(); z++)
			{
				for (int t = 0; t < clusterInstances.size(); t++)
				{
					final Double dist = euclidean.distance(clusterInstances.get(t), clusterInstances.get(z));
					sum += dist;
				}

			}
			intraCluster += sum / sizeCluster;
		}
		return intraCluster / numClusters;
	}

	private double getEsquaredErrors(final int numClusters, final DistanceFunction euclidean, final Instances centroids)
	{
		double sum = 0;
		double error = 0;
		for (int i = 0; i < numClusters; i++)
		{

			final ArrayList<Instance> clusterInstances = hashClustering.get(i);
			sum = 0;
			for (int z = 0; z < clusterInstances.size(); z++)
			{
				final Double dist = euclidean.distance(clusterInstances.get(z), centroids.get(i));
				sum += dist * dist;
			}
			error += sum;
		}

		return error;
	}

	private double getEsquaredErrors2(final int numClusters, final DistanceFunction euclidean, final Instances centroids)
	{
		double sum = 0;
		double error = 0;
		for (int i = 0; i < numClusters; i++)
		{

			final ArrayList<Instance> clusterInstances = hashClustering.get(i);
			sum = 0;
			for (int z = 0; z < clusterInstances.size(); z++)
			{
				final Double dist = euclidean.distance(clusterInstances.get(z), centroids.get(i));
				sum += dist;
			}
			error += sum / clusterInstances.size();
		}

		return error / numClusters;
	}

}
