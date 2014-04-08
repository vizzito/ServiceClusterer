package org.clusterer.services.request;

import java.util.ArrayList;

public class TreeGeneratorRequest {
	private String bottomSimil;
	private String topSimil;
	private ArrayList<String> files;

	TreeGeneratorRequest(String bottomSimil, String topSimil, ArrayList<String> files){
		this.setBottomSimil(bottomSimil);
		this.setTopSimil(topSimil);
		this.setFiles(files);
	}

	public String getBottomSimil() {
		return bottomSimil;
	}

	public void setBottomSimil(String bottomSimil) {
		this.bottomSimil = bottomSimil;
	}

	public String getTopSimil() {
		return topSimil;
	}

	public void setTopSimil(String topSimil) {
		this.topSimil = topSimil;
	}

	public ArrayList<String> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<String> files) {
		this.files = files;
	}
}
