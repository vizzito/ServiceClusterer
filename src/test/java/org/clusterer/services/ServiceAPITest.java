package org.clusterer.services;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.Properties;

import org.junit.Test;

public class ServiceAPITest {
	@Test
	public void getPropertiesFile(){
		Properties prop = new Properties();
		URL url = ClassLoader.getSystemResource("config.properties");
		try{
		prop.load(url.openStream());
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	    System.out.println(prop);
	    String tomcatDir = prop.getProperty("tomcat.dir");
        assertNotNull(tomcatDir);
        
	}
}
