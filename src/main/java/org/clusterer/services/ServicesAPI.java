package org.clusterer.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.clusterer.edgebundles.io.DataReader;
import org.clusterer.edgebundles.io.HEBServiceAdapter;
import org.clusterer.services.request.TreeGeneratorRequest;
import org.clusterer.services.response.VisualTreeResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@MultipartConfig
public class ServicesAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private List<URL> list;
	private static int BOTTHRESHOLD = 30;
	private static int TOPTHRESHOLD = 80;
	private Properties prop;
	private static final String DIRFILES = "/tomcat/apache-tomcat-7.0.52/webapps/ServiceClusterer";

	public ServicesAPI() {
		//TODO ver el tema de la carga del propertie file
		//loadPropertyFile();
	}

	private void loadPropertyFile(){
		try {
			prop = new Properties();
			InputStream input = null;
			input = new FileInputStream("config.properties");
			// load a properties file
			prop.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
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

		DataReader data = new HEBServiceAdapter(list, BOTTHRESHOLD / 100.0,
				TOPTHRESHOLD / 100.0);
		// dataVisualiser.setData(data);

		// Armo el la respuesta q tendra el servicio
		// String jsonData = createJsonData(data);

		// /////////////////////////////////////////
		String json = createJsonData(data);
		out.println(json);
	}

	private String extractFileName(Part part) {
		
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length() - 1);
			}
		}
		return null;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub		
		list = new ArrayList<URL>();
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List<FileItem> fields = upload.parseRequest(request);
			for (Iterator<FileItem> it = fields.iterator(); it.hasNext();){
				FileItem fileItem = it.next();
				if(!fileItem.isFormField()){
					File fichero = new File(fileItem.getName());
		               // nos quedamos solo con el nombre y descartamos el path
					 String sRootPath = new File("").getAbsolutePath();
					// String pathe = prop.getProperty("tomcat.dir");
		             fichero = new  File(sRootPath +"/"+fichero.getName());

		             // escribimos el fichero colgando del nuevo path
		             try {
						fileItem.write(fichero);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					String name = fileItem.getFieldName();
				
					if(name.equals("files[bottomsimil]"))
						BOTTHRESHOLD = Integer.parseInt(fileItem.getString());
						
					if(name.equals("files[topsimil]"))
						TOPTHRESHOLD = Integer.parseInt(fileItem.getString());
					
				}
			}
           
		      
		    
			
			
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		generateListFiles();
		DataReader data = new HEBServiceAdapter(list,
				BOTTHRESHOLD / 100.0,
				TOPTHRESHOLD / 100.0);

		String json = createJsonData(data);
		PrintWriter out = response.getWriter();
		out.println(json);
	}

	private String createJsonData(DataReader jsonData) {
		Object dataObject = new Object();
		int parents[] = jsonData.getParents();
		int adjacencyList[][] = jsonData.getAdjacencyList();
		Hashtable<Integer, Integer> parentsMap = new Hashtable<Integer, Integer>();
		AbstractMap<Integer, String> namesMap = jsonData.getNamesMap();
		List arrayResult = new ArrayList();

		int countNodes = jsonData.getNodesCount();
		for (int i = 2; i < countNodes; i++) {
			// String name = doName(parents[i],i);
			parentsMap.put(i + 1, parents[i] + 1);
		}

		for (int i = 2; i < countNodes; i++) {
			if (parents[i] > 0) {

				String nameKey = doName(parents[i], i);
				String name = namesMap.get(i);
				String[] imports = doImports(adjacencyList[i], parentsMap);
				VisualTreeResponse r = new VisualTreeResponse(nameKey, name,
						imports);
				arrayResult.add(r);
			}
		}

		Gson g = new Gson();
		String jsonResult = g.toJson(arrayResult);

		return jsonResult;

	}

	private String doName(int parentName, int nodeName) {
		String p = String.valueOf(parentName + 1);
		String n = String.valueOf(nodeName + 1);
		return p + '.' + n;
	}

	private String[] doImports(int[] adj, Hashtable<Integer, Integer> parentsMap) {
		if (adj.length == 0) {
			String t[] = new String[0];
			return t;
		} else {
			int l = adj.length;
			String[] r = new String[l];
			for (int i = 0; i < l; i++) {
				r[i] = String.valueOf(parentsMap.get(adj[i] + 1)) + "."
						+ String.valueOf(adj[i] + 1);
			}
			return r;
		}
	}

	private void generateListFiles()
			throws MalformedURLException {
		System.setProperty("user.dir","/home/panther"+DIRFILES);
		String userdir = System.getProperty("user.dir");

		File folder = new File(userdir);
		File[] listOfFiles = folder.listFiles();
		for (int i=0;i<listOfFiles.length;i++){
			if(listOfFiles[i].isFile())
				list.add(new URL("file:" +userdir+"/"+listOfFiles[i].getName()));
		}
//		
//		list.add(new URL("file:" + userdir + "/botomUp1/busquedarub.wsdl"));
//		list.add(new URL("file:" + userdir
//				+ "/botomUp1/DatosdePersonaporCuip.wsdl"));
//		list.add(new URL("file:" + userdir
//				+ "/botomUp1/InformacionDePersona.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/DatosdePersona.wsdl"));
//		list.add(new URL("file:" + userdir
//				+ "/botomUp1/DatosdeEstadoCivil.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/DatosdeDictamenes.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/AltadeRelaciones.wsdl"));
//		list.add(new URL(
//				"file:"
//						+ userdir
//						+ "/botomUp1/CambiodeEst_GC_SinModif_DatosFiliatorios_AccionesdeActualizacion.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ConsultaDeHijos.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/DatosdeApostilles.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/DatosdePartida.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/DatosdeSentencia.wsdl"));
//		list.add(new URL("file:" + userdir
//				+ "/botomUp1/DatosInfoSumarialyDDJJ.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/Est_GC_Reversion.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ListadoPersxDoc.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWA.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWB.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWC.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWD.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWE.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWF.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWH.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWR.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWT.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/ServicioHLWV.wsdl"));
//		list.add(new URL("file:" + userdir
//				+ "/botomUp1/ValidaciondePartida.wsdl"));
//		list.add(new URL("file:" + userdir
//				+ "/botomUp1/ValidaciondeRelaciones.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/WsPw01.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/WsPw02.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/WsPw03.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/WsPw04.wsdl"));
//		list.add(new URL("file:" + userdir + "/botomUp1/WsPw10.wsdl"));

	}
	
	private String getParam(Part part) throws IOException{
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
		  InputStream inputStream = part.getInputStream();
		if (inputStream != null) {
			   bufferedReader = new BufferedReader(new InputStreamReader(
			inputStream));
			   char[] charBuffer = new char[128];
			   int bytesRead = -1;
			   while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
			    stringBuilder.append(charBuffer, 0, bytesRead);
			   }
			  } else {
			   stringBuilder.append("");
			  }
			} catch (IOException ex) {
			  throw ex;
			} finally {
			  if (bufferedReader != null) {
			   try {
			    bufferedReader.close();
			   } catch (IOException ex) {
			    throw ex;
			   }
			  }
	     }
		return stringBuilder.toString();
	}

}
