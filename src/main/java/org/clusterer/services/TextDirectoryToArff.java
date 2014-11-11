package org.clusterer.services;

/*
 *    TextDirectoryToArff.java
 *    Copyright (C) 2002 Richard Kirkby
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
 
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.InterfaceType;
import org.ow2.easywsdl.wsdl.api.Operation;
import org.ow2.easywsdl.wsdl.api.WSDLException;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.xml.sax.InputSource;

import weka.core.*;
 
/**
 * Builds an arff dataset from the documents in a given directory.
 * Assumes that the file names for the documents end with ".txt".
 *
 * Usage:<p/>
 *
 * TextDirectoryToArff <directory path> <p/>
 *
 * @author Richard Kirkby (rkirkby at cs.waikato.ac.nz)
 * @version 1.0
 */
public class TextDirectoryToArff {
 
  public Instances createDataset(String directoryPath) throws Exception {
 
	
	  ArrayList<Attribute> atts = new ArrayList<Attribute>(3);
   // atts.add(new Attribute("filename", (FastVector) null));
	  atts.add(new Attribute("operation",(ArrayList<String>)null));
    atts.add(new Attribute("input",(ArrayList<String>)null));
    atts.add(new Attribute("output",(ArrayList<String>)null));
    Instances data = new Instances("text_files_in_" + directoryPath, atts,100000000);
   
    
    File dir = new File(directoryPath);
    String[] files = dir.list();
    for (int i = 0; i < files.length; i++) {
      if (files[i].endsWith(".wsdl")) {
    	  
    	  URL url = new URL("file://"+directoryPath + File.separator + files[i]);
    	  Description d = readLocation(url);
    try {
 //     double[] newInst = new double[2];
  //    newInst[0] = (double)data.attribute(0).addStringValue(files[i]);
//      File txt = new File(directoryPath + File.separator + files[i]);
//      InputStreamReader is;
//      is = new InputStreamReader(new FileInputStream(txt));
//      StringBuffer txtStr = new StringBuffer();
//      int c;
//      while ((c = is.read()) != -1) {
//        txtStr.append((char)c);
//      }
      
      InterfaceType portType = d.getInterfaces().get(0);
		for (Operation operation : portType.getOperations()) {
			//operation.getInput().getElement().getForm();
			 double[] newInst = new double[3];
			 newInst[0] = (double)data.attribute(0).addStringValue(operation.getQName().getLocalPart());//.substring(0, 5));
			 newInst[1] = (double)data.attribute(1).addStringValue(operation.getInput().getElement().getQName().getLocalPart().substring(0, 5));
			 newInst[2] = (double)data.attribute(2).addStringValue(operation.getOutput().getElement().getQName().getLocalPart().substring(0, 5));
			                            //  cambiar este 1.0 a ver q onda
			 data.add(new DenseInstance(1.0,newInst));
		//	 data.add(new SparseInstance(1.0,newInst));
		} 
   //   newInst[0] = (double)data.attribute(1).addStringValue(txtStr.toString());
  
      
    //  Instance inst = new DenseInstance(3); 

   // Set instance's values for the attributes "length", "weight", and "position"
//   inst.setValue(0, 5.3); 
//   inst.setValue(1, 300); 
  // inst.setValue(2, 2); 
   
//   data.add(inst);
   
    } catch (Exception e) {
      System.err.println("failed to convert file: " + directoryPath + File.separator + files[i]);
    }
      }
    }
    return data;
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
  public static void main(String[] args) {
	
  //  if (args.length == 1) {
      TextDirectoryToArff tdta = new TextDirectoryToArff();
      try {
    Instances dataset = tdta.createDataset("/home/panther/botomUp1");
    System.out.println(dataset);
      } catch (Exception e) {
    System.err.println(e.getMessage());
    e.printStackTrace();
      }
//    } else {
//      System.out.println("Usage: java TextDirectoryToArff <directory name>");
//    }
  }
}