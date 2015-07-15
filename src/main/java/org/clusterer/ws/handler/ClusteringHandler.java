package org.clusterer.ws.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

//import org.clusterer.base.NodeBasedClusterer;
import org.clusterer.strategy.ClusteringHierarchyStrategy;
import org.clusterer.strategy.ClusteringStrategy;
import org.clusterer.util.DataTypeNode;
import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.InterfaceType;
import org.ow2.easywsdl.wsdl.api.Operation;
import org.ow2.easywsdl.wsdl.api.WSDLException;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;


/**
 * Business Logic for calling the clustering algorithm.
 * 
 * @author Gonzalo Salvatierra
 * 
 */
public class ClusteringHandler
{

	/**
	 * Returns a WSDL description given a URL location.
	 * 
	 * @param url
	 * @return
	 * 
	 */
	private AbstractMap<Operation, Description> fileDescriptions;
	private ClusteringStrategy clustererStrategy;

	public AbstractMap<Operation, Description> getFileDescriptions()
	{
		return fileDescriptions;
	}



	protected Description readLocation(final URL url)
	{
		WSDLReader reader = null;
		try
		{
			reader = WSDLFactory.newInstance().newWSDLReader();
		}
		catch (final WSDLException e)
		{
			e.printStackTrace();
		}
		try
		{
			final URLConnection connection = url.openConnection();
			connection.connect();
			final InputStream input = connection.getInputStream();
			final Description description = reader.read(new InputSource(input));
			return description;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * CLUSTERING METHOD: It receives URL representing a set of WSDL documents and a <b>similarity threshold</b>, and
	 * returns clusters of Web Service operations.
	 * 
	 * @param WSDLLocations
	 * @param threshold
	 * @return cluster of WS operations
	 */
	public HashMap<String, Object> clusterWSDLDocumentsForCluster(final List<MultipartFile> listFiles, final double threshold)
	{
		final List<List<Operation>> clusters = new LinkedList<List<Operation>>();
		final HashMap<String, String> mapFiles = new HashMap<String, String>();
		final List<Operation> operations = new LinkedList<Operation>();
		fileDescriptions = new HashMap<Operation, Description>();
		for (final MultipartFile wsdlFile : listFiles)
		{
			final Description description = getDescriptionFromWSDLFile(wsdlFile);
			//For testing purposes: For now, we can focus on SOAP operations (in general, the first portType given by parsers)
			//So far, clustering HTTP operations with SOAP operations does not make sense.
			//for (InterfaceType portType : description.getInterfaces()) {
			final InterfaceType portType = description.getInterfaces().get(0);
			for (final Operation operation : portType.getOperations())
			{
				operations.add(operation);
				fileDescriptions.put(operation, description);
				mapFiles.put(operation.getQName().getLocalPart(), wsdlFile.getOriginalFilename());
			}
		}

		final ClusteringStrategy clusterer = getClustererStrategy();
		clusterer.setOperations(operations);
		clusterer.generateCluster();

		final List<DataTypeNode> nodes = clusterer.getMergedClusters();
		for (final DataTypeNode node : nodes)
		{
			clusters.add(node.getRelatedOperations());
		}
		final HashMap<String, Object> clusterInfo = new HashMap<String, Object>();
		clusterInfo.put("clusterOperations", clusters);
		clusterInfo.put("mapFiles", mapFiles);
		return clusterInfo;
	}



	private Description getDescriptionFromWSDLFile(final MultipartFile wsdlFile)
	{
		WSDLReader reader = null;
		try
		{
			reader = WSDLFactory.newInstance().newWSDLReader();
		}
		catch (final WSDLException e)
		{
			e.printStackTrace();
		}
		try
		{
			final Description description = reader.read(new InputSource(wsdlFile.getInputStream()));
			return description;
		}
		catch (WSDLException | URISyntaxException | IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}



	public ClusteringStrategy getClustererStrategy()
	{
		return clustererStrategy;
	}



	public void setClustererStrategy(final ClusteringStrategy clustererStrategy)
	{
		this.clustererStrategy = clustererStrategy;
	}

	public HashMap<String, Object> clusterWSDLDocuments(final List<MultipartFile> listFiles, final double threshold)
	{
		final List<List<Operation>> clusters = new LinkedList<List<Operation>>();
		final HashMap<String, String> mapFiles = new HashMap<String, String>();
		final List<Operation> operations = new LinkedList<Operation>();
		fileDescriptions = new HashMap<Operation, Description>();
		for (final MultipartFile wsdlFile : listFiles)
		{
			final Description description = getDescriptionFromWSDLFile(wsdlFile);
			//For testing purposes: For now, we can focus on SOAP operations (in general, the first portType given by parsers)
			//So far, clustering HTTP operations with SOAP operations does not make sense.
			//for (InterfaceType portType : description.getInterfaces()) {
			final InterfaceType portType = description.getInterfaces().get(0);
			for (final Operation operation : portType.getOperations())
			{
				operations.add(operation);
				fileDescriptions.put(operation, description);
				mapFiles.put(operation.getQName().getLocalPart(), wsdlFile.getOriginalFilename());
			}
		}

		final ClusteringHierarchyStrategy clusterer = new ClusteringHierarchyStrategy(operations);
		try
		{
			clusterer.setThreshold(threshold);
			clusterer.generateCluster();
		}
		catch (final Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final List<DataTypeNode> nodes = clusterer.getMergedClusters();
		for (final DataTypeNode node : nodes)
		{
			clusters.add(node.getRelatedOperations());
		}
		final HashMap<String, Object> clusterInfo = new HashMap<String, Object>();
		clusterInfo.put("clusterOperations", clusters);
		clusterInfo.put("mapFiles", mapFiles);
		return clusterInfo;
	}

}
