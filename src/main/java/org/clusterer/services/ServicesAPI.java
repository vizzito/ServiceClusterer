package org.clusterer.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.clusterer.edgebundles.io.DataReader;
import org.clusterer.edgebundles.io.HEBServiceAdapter;
import org.clusterer.services.response.VisualTreeResponse;

import weka.clusterers.SimpleKMeans;

import com.google.gson.Gson;


@MultipartConfig
public class ServicesAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private List<URL> list;
	private static int BOTTHRESHOLD = 30;
	private static int TOPTHRESHOLD = 80;
	private static String CLUSTERING_STRATEGY = "";
	private static String DIRFILES;

	public ServicesAPI() {
		// TODO ver el tema de la carga del propertie file
		loadPropertyFile();
		
	}

	private void loadPropertyFile() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			String filename = "config.properties";
    		input = ServicesAPI.class.getClassLoader().getResourceAsStream(filename);
    		if(input==null){
    	            System.out.println("Sorry, unable to find " + filename);
    		    return;
    		}
    		prop.load(input);
	//		input = new FileInputStream("config.properties");
//			prop.load(input);
			DIRFILES = prop.getProperty("tomcat.dir");	 
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
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
		list = new ArrayList<URL>();
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List<FileItem> fields = upload.parseRequest(request);
				for (Iterator<FileItem> it = fields.iterator(); it.hasNext();) {
					FileItem fileItem = it.next();
					if (!fileItem.isFormField()) {
						File fichero = new File(fileItem.getName());
						String fileRoute = DIRFILES + "/" + fichero.getName();
						fichero = new File(fileRoute);
						list.add(new URL("file:" + DIRFILES + "/"
								+ fichero.getName()));
						try {
							fileItem.write(fichero);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						String name = fileItem.getFieldName();
						if (name.equals("files[bottomsimil]"))
							BOTTHRESHOLD = Integer.parseInt(fileItem
									.getString());
						if (name.equals("files[topsimil]"))
							TOPTHRESHOLD = Integer.parseInt(fileItem
									.getString());
						if (name.equals("files[clusteringstrategy]"))
							CLUSTERING_STRATEGY = fileItem.getString();
					}
				}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		
		DataReader data = new HEBServiceAdapter(list, BOTTHRESHOLD / 100.0,
				TOPTHRESHOLD / 100.0,CLUSTERING_STRATEGY);

		String jsonTreeMap = createJsonTreeData(data);
		String jsonFileMap = createJsonMapData(data);
		PrintWriter out = response.getWriter();
		out.println(jsonTreeMap);
		out.println(jsonFileMap);
	}
	private String createJsonMapData(DataReader jsonData){
		AbstractMap<String, String> mapFiles = jsonData.getMapParentFile();
		Gson g = new Gson();
		String jsonResult = g.toJson(mapFiles);
		return jsonResult;
		}
	
	private String createJsonTreeData(DataReader jsonData) {
		int parents[] = jsonData.getParents();
		int adjacencyList[][] = jsonData.getAdjacencyList();
		Hashtable<Integer, Integer> parentsMap = new Hashtable<Integer, Integer>();
		AbstractMap<Integer, String> namesMap = jsonData.getNamesMap();
		List<VisualTreeResponse> arrayResult = new ArrayList<VisualTreeResponse>();
		int countNodes = jsonData.getNodesCount();
		for (int i = 2; i < countNodes; i++) {
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

	private String getParam(Part part) throws IOException {
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
