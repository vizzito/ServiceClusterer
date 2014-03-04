package org.clusterer.ws.handler;

import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



import org.ow2.easywsdl.wsdl.api.Operation;
import org.ow2.easywsdl.wsdl.impl.wsdl11.OperationImpl;

import org.clusterer.util.Pair;

public class OperarionSimilarityHandler {
	//private AbstractMap <Operation,Set<Operation>> opSimilSet;
	private AbstractMap <String,Set<String>> opSimilSet;
	
	public AbstractMap<String, Set<String>> getOpSimilSet() {
		return opSimilSet;
	}


	public   OperarionSimilarityHandler() {
		opSimilSet= new HashMap<String,Set<String>>();
		
	}
	
	
	public AbstractMap <Pair,Double> generateOperationSimilarity(List<URL> WSDLLocations) {
		return generateOperationSimilarity(WSDLLocations,10,0.0d,0.8d);
	}
	
	public AbstractMap <Pair,Double> generateOperationSimilarity(List<URL> WSDLLocations, int levels, double botLimit ,double topLimit) {
		
		
		AbstractMap <Pair,Double> opSimil= new HashMap<Pair,Double>();
		double simil=0;
		
		System.out.println("Simil Total Limit bottom " + botLimit + " top: " + topLimit );
		
		for (int si=1; si <= levels && simil < topLimit ; si++) {
			simil=botLimit+(double)si/levels*(topLimit-botLimit);
			
			System.out.println("Simil: " + simil );
			ClusteringHandler ch=new ClusteringHandler();
			List<List<Operation>> res=ch.clusterWSDLDocuments(WSDLLocations, simil);

			for (Iterator<List<Operation>> i=res.iterator() ;i.hasNext();) {
				List<Operation> group=i.next();
				for (Iterator<Operation> j=group.iterator() ;j.hasNext();) {
					OperationImpl o1=(OperationImpl)j.next();
					for (Iterator<Operation> k=group.iterator() ;k.hasNext();) {
						
						OperationImpl o2=(OperationImpl)k.next();
						if (o1.getModel().getName() != o2.getModel().getName()) {
							if (simil== topLimit) {
								opSimil.remove(new Pair(o1.getModel().getName(),o2.getModel().getName()));
								opSimilSet.get(o1.getModel().getName()).remove(o2.getModel().getName());
							}
							else {
								opSimil.put(new Pair(o1.getModel().getName(),o2.getModel().getName()), simil);
								if (opSimilSet.containsKey(o1.getModel().getName())) opSimilSet.get(o1.getModel().getName()).add(o2.getModel().getName());
								else { 
									Set<String> set=new HashSet<String>();
									set.add(o2.getModel().getName());
									opSimilSet.put(o1.getModel().getName(), set);
								}
							}
						}
							//opSimil.put(o1.getModel().getName()+":"+o2.getModel().getName(), simil);
						//la opcion comentada agrega uri a las claves... por si hay conflicto de nombres
						//opSimil.put(o1.getQName()+":"+o2.getQName(), simil);
					}
					
				}
				
			}
		}
		return opSimil;

	}

}
