package org.clusterer.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.clusterer.edgebundles.io.DataReader;
import org.clusterer.edgebundles.io.HEBServiceAdapter;
import org.clusterer.services.response.VisualTreeResponse;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;


//@MultipartConfig
@Controller
@EnableAutoConfiguration
@RequestMapping("/services")
public class ServicesAPI //extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private List<URL> list;
	private static int BOTTHRESHOLD = 30;
	private static int TOPTHRESHOLD = 80;
	private static String CLUSTERING_STRATEGY = "";
	private static Integer CLUSTER_COUNT = 0;
	private static String DIRFILES;

	public ServicesAPI()
	{
		loadPropertyFile();
	}

	private void loadPropertyFile()
	{
		final Properties prop = new Properties();
		InputStream input = null;
		try
		{
			final String filename = "config.properties";
			input = ServicesAPI.class.getClassLoader().getResourceAsStream(filename);
			if (input == null)
			{
				System.out.println("Sorry, unable to find " + filename);
				return;
			}
			prop.load(input);
			DIRFILES = prop.getProperty("tomcat.dir");
		}
		catch (final IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	//	@Override
	@RequestMapping(value = "check", method = RequestMethod.GET)
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
			IOException
	{
		final PrintWriter out = response.getWriter();
		out.println("Service Running!");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	//@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
			IOException
	{
		list = new ArrayList<URL>();
		final FileItemFactory factory = new DiskFileItemFactory();
		final ServletFileUpload upload = new ServletFileUpload(factory);
		try
		{
			final List<FileItem> fields = upload.parseRequest(request);
			for (final Iterator<FileItem> it = fields.iterator(); it.hasNext();)
			{
				final FileItem fileItem = it.next();
				if (!fileItem.isFormField())
				{
					File fichero = new File(fileItem.getName());
					final String fileRoute = DIRFILES + "/" + fichero.getName();
					fichero = new File(fileRoute);
					list.add(new URL("file:" + DIRFILES + "/" + fichero.getName()));
					try
					{
						fileItem.write(fichero);
					}
					catch (final Exception e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					final String name = fileItem.getFieldName();
					if (name.equals("files[bottomsimil]"))
					{
						BOTTHRESHOLD = Integer.parseInt(fileItem.getString());
					}
					if (name.equals("files[topsimil]"))
					{
						TOPTHRESHOLD = Integer.parseInt(fileItem.getString());
					}
					if (name.equals("files[clusteringstrategy]"))
					{
						CLUSTERING_STRATEGY = fileItem.getString();
					}

					if (name.equals("files[numberofclusters]"))
					{
						CLUSTER_COUNT = Integer.parseInt(fileItem.getString());
					}
				}
			}
		}
		catch (final FileUploadException e)
		{
			e.printStackTrace();
		}

		final DataReader data = new HEBServiceAdapter(list, BOTTHRESHOLD / 100.0, TOPTHRESHOLD / 100.0, CLUSTERING_STRATEGY,
				CLUSTER_COUNT);

		final String jsonTreeMap = createJsonTreeData(data);
		final String jsonFileMap = createJsonMapData(data);
		final PrintWriter out = response.getWriter();
		out.println(jsonTreeMap);
		out.println(jsonFileMap);
		out.println(data.getNumberOfClusters());
		out.println(data.getValidationInfo());
	}

	private String createJsonMapData(final DataReader jsonData)
	{
		final AbstractMap<String, String> mapFiles = jsonData.getMapParentFile();
		final Gson g = new Gson();
		final String jsonResult = g.toJson(mapFiles);
		return jsonResult;
	}

	private String createJsonTreeData(final DataReader jsonData)
	{
		final int parents[] = jsonData.getParents();
		final int adjacencyList[][] = jsonData.getAdjacencyList();
		final Hashtable<Integer, Integer> parentsMap = new Hashtable<Integer, Integer>();
		final AbstractMap<Integer, String> namesMap = jsonData.getNamesMap();
		final List<VisualTreeResponse> arrayResult = new ArrayList<VisualTreeResponse>();
		final int countNodes = jsonData.getNodesCount();
		for (int i = 2; i < countNodes; i++)
		{
			parentsMap.put(i + 1, parents[i] + 1);
		}
		for (int i = 2; i < countNodes; i++)
		{
			if (parents[i] > 0)
			{

				final String nameKey = doName(parents[i], i);
				final String name = namesMap.get(i);
				final String[] imports = doImports(adjacencyList[i], parentsMap);
				final VisualTreeResponse r = new VisualTreeResponse(nameKey, name, imports);
				arrayResult.add(r);
			}
		}
		final Gson g = new Gson();
		final String jsonResult = g.toJson(arrayResult);
		return jsonResult;
	}

	private String doName(final int parentName, final int nodeName)
	{
		final String p = String.valueOf(parentName + 1);
		final String n = String.valueOf(nodeName + 1);
		return p + '.' + n;
	}

	private String[] doImports(final int[] adj, final Hashtable<Integer, Integer> parentsMap)
	{
		if (adj.length == 0)
		{
			final String t[] = new String[0];
			return t;
		}
		else
		{
			final int l = adj.length;
			final String[] r = new String[l];
			for (int i = 0; i < l; i++)
			{
				r[i] = String.valueOf(parentsMap.get(adj[i] + 1)) + "." + String.valueOf(adj[i] + 1);
			}
			return r;
		}
	}
}
