package org.clusterer.services;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

//import detector.AntipatternDetector;
//import detector.antipattern.Antipattern;

public class ApDetectorService extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
// 		String str_request = IOUtils.toString(request.getInputStream());
//		str_request = str_request+"&";
//		String regex  = "\\=([-0-9a-zA-Z.+_]*)\\&";
//		Pattern p = Pattern.compile(regex);
//		Matcher m = p.matcher(str_request);
//		ArrayList<String> files = new ArrayList<String>();
//		 while (m.find()) {
//			 files.add(m.group(1));
//			 }
//		
//		AntipatternDetector s = new AntipatternDetector();
//		ArrayList<Antipattern[]> antiPatterns = new ArrayList<Antipattern[]>();
//		try {
//			for(String url : files){
//			s.setWsdlUrl(new File(url).toURI().toURL());
//			Antipattern[] result = s.analyze();
//			antiPatterns.add(result);
//			}
//			
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		Gson g = new Gson();
//		String jsonResult = g.toJson(antiPatterns);
//		PrintWriter out = response.getWriter();
//		out.println(jsonResult);
	}
}
