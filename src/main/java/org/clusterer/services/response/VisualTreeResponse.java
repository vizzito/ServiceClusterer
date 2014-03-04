package org.clusterer.services.response;

public class VisualTreeResponse extends Response {
	
	public String name;
	public String nameKey;
	public String [] imports;
	
	public VisualTreeResponse(String nameKey, String name, String[] imports){
		this.nameKey = nameKey;
		this.name=name;
		this.imports=imports;
	}
}
