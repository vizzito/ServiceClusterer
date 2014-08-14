package org.clusterer.services;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.junit.Test;

public class ServiceAPITest {
	@Test
	public void getPropertiesFile(){
		Properties prop = new Properties();
		InputStream input = null;
	 
		try {
	 
			input = new FileInputStream("src/main/resources/config.properties");
	 
			// load a properties file
			prop.load(input);
	 
			// get the property value and print it out
			System.out.println(prop.getProperty("tomcat.dir"));	 
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
}
