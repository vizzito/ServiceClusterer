package org.clusterer.services;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.core.*;
import weka.core.converters.*;
import weka.classifiers.Classifier;
import weka.classifiers.trees.*;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.filters.*;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.WSDLException;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.xml.sax.InputSource;

/**
 * Example class that converts HTML files stored in a directory structure into 
 * and ARFF file using the TextDirectoryLoader converter. It then applies the
 * StringToWordVector to the data and feeds a J48 classifier with it.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class test {

  /**
   * Expects the first parameter to point to the directory with the text files.
   * In that directory, each sub-directory represents a class and the text
   * files in these sub-directories will be labeled as such.
   *
   * @param args        the commandline arguments
   * @throws Exception  if something goes wrong
   */
	
	public Instances createDataset(String directoryPath) throws Exception {
		 
	    FastVector atts = new FastVector(2);
	    atts.addElement(new Attribute("filename", (FastVector) null));
	    atts.addElement(new Attribute("contents", (FastVector) null));
	    Instances data = new Instances("text_files_in_" + directoryPath, atts, 0);
	 
	    File dir = new File(directoryPath);
	    
	    
	    String[] files = dir.list();
	    for (int i = 0; i < files.length; i++) {
	      if (files[i].endsWith(".wsdl")) {
	    	
	    try {
	      double[] newInst = new double[2];
	      newInst[0] = (double)data.attribute(0).addStringValue(files[i]);
	      File txt = new File(directoryPath + File.separator + files[i]);
	      InputStreamReader is;
	      is = new InputStreamReader(new FileInputStream(txt));
	      StringBuffer txtStr = new StringBuffer();
	      int c;
	      while ((c = is.read()) != -1) {
	        txtStr.append((char)c);
	      }
	      newInst[1] = (double)data.attribute(1).addStringValue(txtStr.toString());
	      data.add(new DenseInstance(1.0, newInst));
	    } catch (Exception e) {
	      //System.err.println("failed to convert file: " + directoryPath + File.separator + files[i]);
	    }
	      }
	    }
	    return data;
	  }
	

  public static void main(String[] args) throws Exception {
    // convert the directory into a dataset
	  
	 
	  
    TextDirectoryLoader loader = new TextDirectoryLoader();
    
    TextDirectoryToArff tdta = new TextDirectoryToArff();
    Instances dataset = tdta.createDataset("/home/panther/botomUp1");
    
    
    
  //  loader.setDirectory(new File("/home/panther/test"));
   // Instances dataRaw = loader.getDataSet();
    //System.out.println("\n\nImported data:\n\n" + dataRaw);

    // apply the StringToWordVector
    // (see the source code of setOptions(String[]) method of the filter
    // if you want to know which command-line option corresponds to which
    // bean property)
    StringToWordVector filter = new StringToWordVector();
    filter.setInputFormat(dataset);
    
    
    
    Instances dataFiltered = Filter.useFilter(dataset, filter);

    
    
//    AttributeSelection filter2 = new AttributeSelection();  // package weka.filters.supervised.attribute!
//    CfsSubsetEval eval = new CfsSubsetEval();
//    GreedyStepwise search = new GreedyStepwise();
//    search.setSearchBackwards(true);
//    filter2.setEvaluator(eval);
//    filter2.setSearch(search);
//    filter2.setInputFormat(dataset);
//    
//    Instances dataFiltered2 = Filter.useFilter(dataFiltered, filter2);
    SimpleKMeans kmeans = new SimpleKMeans();
    
    kmeans.setNumClusters(17);
    kmeans.buildClusterer(dataFiltered);
    
    String[] options = new String[2];
    options[0] = "-I";                 // max. iterations
    options[1] = "100";
    EM em = new EM();   // new instance of clusterer
 //   em.setOptions(options);     // set the options
    em.setNumClusters(17);
    em.buildClusterer(dataFiltered);    // build the clusterer
   
  
    for (int i = 0; i < dataFiltered.numInstances(); i++) {
    	System.out.println( dataFiltered.instance(i) );
    	System.out.println( dataset.instance(i).dataset().get(i).stringValue(0));
    //	System.out.println( dataFiltered.attribute(i).name());
    	System.out.println(" is in cluster ");
    	System.out.println( kmeans.clusterInstance(dataFiltered.instance(i)) + 1 );
    	}
  //  EM clusterer = new EM();
 //   clusterer.buildClusterer(dataFiltered);
    ClusterEvaluation evaluation = new ClusterEvaluation();
    evaluation.setClusterer(em);
    evaluation.evaluateClusterer(dataFiltered);
    System.out.println(evaluation.clusterResultsToString());
    
    ClusterEvaluation evaluation2 = new ClusterEvaluation();
    evaluation2.setClusterer(kmeans);
    evaluation2.evaluateClusterer(dataFiltered);
    System.out.println(evaluation2.clusterResultsToString());
    
  }
}