package org.clusterer.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.clusterer.edgebundles.io.DataReader;
import org.clusterer.edgebundles.io.HEBServiceAdapter;
import org.clusterer.services.response.VisualTreeResponse;

import com.google.gson.Gson;


public class ServicesAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private List<URL> list= new ArrayList<URL>();
	private static int BOTTHRESHOLD = 30;
	private static int TOPTHRESHOLD = 80;
	public ServicesAPI() {

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		request.getParameterMap();
		String top = request.getParameter("topsimil");
		PrintWriter out = response.getWriter();
		
		
		
		DataReader data = new HEBServiceAdapter(list,
				BOTTHRESHOLD / 100.0,
				TOPTHRESHOLD / 100.0);
		//dataVisualiser.setData(data);

		// Armo el la respuesta q tendra el servicio
		// String jsonData = createJsonData(data);

		// /////////////////////////////////////////
		String json = createJsonData(data);
		out.println(json);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.getParameterMap();
		int TOPTHRESHOLD = Integer.parseInt(request.getParameter("topsimil"));
		int BOTTHRESHOLD = Integer.parseInt(request.getParameter("bottomsimil"));
		generateListFiles();
		DataReader data = new HEBServiceAdapter(list,
				BOTTHRESHOLD / 100.0,
				TOPTHRESHOLD / 100.0);

		String json = createJsonData(data);
		PrintWriter out = response.getWriter();
		out.println(json);
	}
	
	 private String createJsonData(DataReader jsonData){
	    	Object dataObject = new Object();
	    	int parents[] = jsonData.getParents();
	    	int adjacencyList[][] = jsonData.getAdjacencyList();
	    	Hashtable<Integer, Integer> parentsMap = new Hashtable<Integer,Integer>();
	    	AbstractMap<Integer,String> namesMap = jsonData.getNamesMap();
	    	List arrayResult = new ArrayList();
	    	
	    	int countNodes = jsonData.getNodesCount();
	    	for(int i=2;i<countNodes;i++){
	   // 		String name = doName(parents[i],i);
	    		parentsMap.put(i+1,parents[i]+1);
	    	}
	    	
	    	for(int i=2;i<countNodes;i++){
	    		if(parents[i]>0){
	    			
	    			String nameKey = doName(parents[i],i);
	    			String name = namesMap.get(i);
	    			String [] imports = doImports(adjacencyList[i],parentsMap);
	    			VisualTreeResponse r = new VisualTreeResponse(nameKey,name,imports);
	    			arrayResult.add(r);
	    		}
	    	}
	    	
	    	Gson g = new Gson();
	    	String jsonResult =  g.toJson(arrayResult);
	    	
			return jsonResult;
	    	
	  //  	var myJsonString = JSON.stringify(array);
	    //	var i = 9;
	    }
	    
	    private String doName(int parentName,int nodeName){
	    	String p = String.valueOf(parentName+1);
	    	String n = String.valueOf(nodeName+1);
	    	return p+'.'+n;
	    }
	    
	    private String[] doImports(int[] adj,Hashtable<Integer, Integer> parentsMap){
	    	if (adj.length==0){
	    		 String t[] = new String[0];
	    		 return t;
	    	}
	    	else{
	    		int l = adj.length;
	    		String [] r = new String[l];
	    		for (int i=0;i<l;i++){
	    			r[i]=String.valueOf(parentsMap.get(adj[i]+1))+"."+String.valueOf(adj[i]+1);
	    		}
	    		return r;
	    	}
	    }
	private void generateListFiles() throws MalformedURLException{
		System.setProperty("user.dir","/home/panther/Escritorio/tesis/Fuentes-Prototipo/WebServicesClusterer");
		String userdir=System.getProperty("user.dir");
		list.add(new URL("file:" + userdir + "/botomUp1/busquedarub.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/DatosdePersonaporCuip.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/InformacionDePersona.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/DatosdePersona.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/DatosdeEstadoCivil.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/DatosdeDictamenes.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/AltadeRelaciones.wsdl"));
//		
		list.add(new URL("file:" + userdir + "/botomUp1/CambiodeEst_GC_SinModif_DatosFiliatorios_AccionesdeActualizacion.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ConsultaDeHijos.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/DatosdeApostilles.wsdl"));
//		
//		
		list.add(new URL("file:" + userdir + "/botomUp1/DatosdePartida.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/DatosdeSentencia.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/DatosInfoSumarialyDDJJ.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/Est_GC_Reversion.wsdl"));
//		
		list.add(new URL("file:" + userdir + "/botomUp1/ListadoPersxDoc.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWA.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWB.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWC.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWD.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWE.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWF.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWH.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWR.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWT.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWV.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ValidaciondePartida.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/ValidaciondeRelaciones.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/WsPw01.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/WsPw02.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/WsPw03.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/WsPw04.wsdl"));
		list.add(new URL("file:" + userdir + "/botomUp1/WsPw10.wsdl"));

	}
	    
}
