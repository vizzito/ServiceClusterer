package org.clusterer.ws.handler;

import java.net.URL;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.clusterer.util.Pair;
import org.ow2.easywsdl.wsdl.api.Operation;

public class ServicesMediator {
	
		//private AbstractMap <String,Integer> mapOps= new HashMap<String,Integer>();
		private ClusteringHandler ch;
		private OperarionSimilarityHandler osh;
		private List<URL> WSDLLocations;
		private List<List<Operation>> clusteredOperations;
		AbstractMap <Pair,Double> relatedOperations;
		private double topThreshold;
		private double bottomThreshold;

    	public ServicesMediator(List<URL> WSDLLocations, double botThreshold,double topThreshold){
    		this.WSDLLocations=WSDLLocations;
    		this.topThreshold=topThreshold;
    		this.bottomThreshold=botThreshold;
    		ch=new ClusteringHandler();
    		osh=new OperarionSimilarityHandler();
    	}
    	
    	private void doClustering() {
    		// operaciones agrupadas por similitud
    		clusteredOperations=ch.clusterWSDLDocuments(WSDLLocations, topThreshold);
    		
    		System.out.println("Cantidad de Grupos> " + clusteredOperations.size() + "threshold:" + topThreshold);
    		int inum=0;
    		List<Operation> lo;
    		
    		for (Iterator<List<Operation>> i=clusteredOperations.iterator() ;i.hasNext();inum++) {
    			lo=i.next();
    			System.out.println("Grupo: " + inum + " cant op: " + lo.size() );
    			System.out.println("ops: " + lo.toString());
    			System.out.println("**************************************");
    		}
    		
    		
    	}
    	
    	private void doSimilRelations() {
    		relatedOperations=osh.generateOperationSimilarity(WSDLLocations,10,bottomThreshold,topThreshold);
    		
    		System.out.println("Relaciones entre operaciones");
    		for (Iterator<Entry<Pair, Double>> i=relatedOperations.entrySet().iterator() ;i.hasNext();) {
    			Entry<Pair, Double> ent=i.next();
    			System.out.println("Par"  + ent.getKey().getLeft() + " : " + ent.getKey().getRight()+ " : " + ent.getValue() );
    			
    		}
    		
    		
    		System.out.println("Relaciones entre operaciones adyacentes: ");
    		for (Iterator<Entry<String, Set<String>>> i=osh.getOpSimilSet().entrySet().iterator() ;i.hasNext();) {
    			Entry<String, Set<String>> ent=i.next();
    			System.out.println("Par ad"  + ent.getKey() + " links: " +  ent.getValue() );
    			
    		}
    		
    	}
    	
    	    	
    	public void doAllInferences() {
    		doClustering();
    		doSimilRelations();
    	}
    	
    	public List<List<Operation>> getClusteredOperations() {
    		return clusteredOperations;
    	}
    	
    	public AbstractMap <Pair,Double> getRelatedOperations() {
    		return relatedOperations;
    	}
    	
    	public AbstractMap<Operation, URL> getOperationsURLs() {
    		return ch.getOperationsURLs();
    	}
    	
    	public URL getOperationURL(Operation o) {
    		return getOperationsURLs().get(o);
    	}
    	
    	public Double getLinkValue(Pair p) {
    		return relatedOperations.get(p);
    	}
    	
    	public Set<String> getRelatedOperations(String op) {
    		return osh.getOpSimilSet().get(op);
    	}
}