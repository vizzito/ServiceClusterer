package org.clusterer.edgebundles.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ow2.easywsdl.wsdl.api.Operation;
import org.ow2.easywsdl.wsdl.impl.wsdl11.OperationImpl;
import org.clusterer.util.Pair;
import org.clusterer.ws.handler.ClusteringHandler;
import org.clusterer.ws.handler.OperarionSimilarityHandler;
import org.clusterer.ws.handler.ServicesMediator;


public class HEBServiceAdapter implements DataReader {

    int[] parentNodes;
    int[][] adjacencyList;
    AbstractMap <Integer,String> mapOpsIS = new HashMap<Integer,String>();
    AbstractMap <String,Integer> mapOpsSI = new HashMap<String,Integer>();
    AbstractMap <String, String> mapParentFile = new HashMap<String, String>();

	@SuppressWarnings("unchecked")
	public HEBServiceAdapter(List<URL> WSDLLocations, double botThreshold,double topThreshold,String clusteringStrategy) throws IOException {
    	
    	ServicesMediator serMed=new ServicesMediator(WSDLLocations, botThreshold, topThreshold);
    	serMed.setClusteringStrategy(clusteringStrategy);
    	serMed.doAllInferences();
    	HashMap<String, Object> clusteredInfo= serMed.getClusteredOperations();
    	mapParentFile = (AbstractMap<String, String>) clusteredInfo.get("mapFiles");
    	List<List<Operation>> res =  (List<List<Operation>>) clusteredInfo.get("clusterOperations");
    	
    	int cantOp=0;
    	for (Iterator<List<Operation>> i=res.iterator() ;i.hasNext();) 
    		cantOp+=i.next().size();
			
    	//raiz + grupos de servicios + cant operaciones
    	parentNodes = new int[1+res.size()+cantOp];
    	
    	parentNodes[0]=-1; //raiz sin padre
    	
    	
    	for (int i=1;i<= res.size();i++)
    		parentNodes[i]=0;	//los grupos de operaciones tienen a la raiz "0" como padre
    	
    	int index=res.size()+1;
    	int igroup=1;
    	for (Iterator<List<Operation>> i=res.iterator() ;i.hasNext();) { 
    		List<Operation> group=i.next();
    		int ops=group.size();
    		for (Iterator<Operation> j=group.iterator() ;j.hasNext();) {
    			OperationImpl op=(OperationImpl)j.next();
    			mapOpsIS.put(index,op.getModel().getName());
    			mapOpsSI.put(op.getModel().getName(),index);
    			parentNodes[index++]=igroup;    			
    		}
    		igroup++;    		
    	}
    	
      
    	
    	AbstractMap <Pair,Double> res2=serMed.getRelatedOperations();
    	
        adjacencyList = new int[getNodesCount()][];
        
        for (int i = 0; i < adjacencyList.length; i++) {
            if (adjacencyList[i] == null) {
            	Set<String> relops=serMed.getRelatedOperations(mapOpsIS.get(i));
            	if (relops == null) adjacencyList[i] = new int[0];
            	else {
            		adjacencyList[i] = new int[relops.size()];
            		int j=0;
            		for (String oper : relops ) {
            			adjacencyList[i][j++]=mapOpsSI.get(oper);
            		}
            	}
            }
        }
        
        
        
        System.out.println("Parent Nodes");
        for (int i=0; i < parentNodes.length ; i++ )
        	System.out.print(parentNodes[i]);
        
        System.out.println("Adjacency List");
        for (int i=0; i < adjacencyList.length ; i++ ) {
        	int[] fila=adjacencyList[i];
        	System.out.println("fila:");
        	for (int j=0; j < fila.length ; j++ )
        		System.out.print(fila[j] + ",");
        }
        
        
   }

    public int getNodesCount() {
        return parentNodes.length;
    }

    public int[] getAdjacentNodes(int i) {
        return adjacencyList[i];
    }

    public int getParent(int node) {
        return parentNodes[node];
    }

	public int[] getParents() {
		return parentNodes;
	}
	
	public int[][] getAdjacencyList(){
		return adjacencyList;
	}
	
	public AbstractMap<String, String> getMapParentFile() {
			return mapParentFile;
	}

	public AbstractMap<Integer, String> getNamesMap() {
		// TODO Auto-generated method stub
		return mapOpsIS;
	}
}
