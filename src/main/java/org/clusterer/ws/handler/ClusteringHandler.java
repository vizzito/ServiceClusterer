package org.clusterer.ws.handler;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.clusterer.base.NodeBasedClusterer;
import org.clusterer.util.DataTypeNode;
import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.InterfaceType;
import org.ow2.easywsdl.wsdl.api.Operation;
import org.ow2.easywsdl.wsdl.api.WSDLException;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.xml.sax.InputSource;

/**
 * Business Logic for calling the clustering algorithm.
 * 
 * @author Gonzalo Salvatierra
 *
 */
public class ClusteringHandler {

	/**
	 * Returns a WSDL description given a URL location.
	 * @param url
	 * @return
	 * 
	 */
	private AbstractMap <Operation,URL> operationsURLs;
	
	public AbstractMap<Operation, URL> getOperationsURLs() {
		return operationsURLs;
	}

	

	protected Description readLocation(URL url) {
		WSDLReader reader = null;
		try {
			reader = WSDLFactory.newInstance().newWSDLReader();
		} catch (WSDLException e) {
			e.printStackTrace();
		}
		try {
			URLConnection connection = url.openConnection();
			connection.connect();
			InputStream input = connection.getInputStream();
			Description description = reader.read(new InputSource(input));
			return description;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	/**
	 * CLUSTERING METHOD: It receives URL representing a set of WSDL documents
	 * and a <b>similarity threshold</b>, and returns clusters of Web Service operations.
	 * @param WSDLLocations
	 * @param threshold
	 * @return cluster of WS operations
	 */
	public List<List<Operation>> clusterWSDLDocuments(List<URL> WSDLLocations, double threshold) {
		List<List<Operation>> clusters = new LinkedList<List<Operation>>();
		List<Operation> operations = new LinkedList<Operation>();
		
		operationsURLs= new HashMap<Operation,URL>() ;
		System.out.println("URLS:"+operationsURLs);
		for (URL documentURL : WSDLLocations) {
			Description description = readLocation(documentURL);
			//For testing purposes: For now, we can focus on SOAP operations (in general, the first portType given by parsers)
			//So far, clustering HTTP operations with SOAP operations does not make sense.
		//	for (InterfaceType portType : description.getInterfaces()) {
				InterfaceType portType = description.getInterfaces().get(0);
				for (Operation operation : portType.getOperations()) {
					operations.add(operation);
					operationsURLs.put(operation,documentURL );
									
				}
			//}
		}
		
		NodeBasedClusterer clusterer = new NodeBasedClusterer(operations);
		clusterer.generateGraph(threshold);
//		clusterer.generateGraph(0.75);
		List<DataTypeNode> nodes = clusterer.getMergedClusters();
		for (DataTypeNode node : nodes) {
			clusters.add(node.getRelatedOperations());
		}
		return clusters;
	}
	
		
}
