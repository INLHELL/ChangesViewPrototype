package com.github.inlhell.cvp.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AvailableTextFiles {
	
	private static class Handler {
		
		private static AvailableTextFiles instance = new AvailableTextFiles();
	}
	
	private List<TextFile> availableTextFiles = new CopyOnWriteArrayList<TextFile>();
	
	private AvailableTextFiles() {
	}
	
	public static AvailableTextFiles getInstance() {
		return Handler.instance;
	}
	
	public void addTextFile(final TextFile textFile) {
		this.availableTextFiles.add(textFile);
	}
	
	public List<TextFile> getAvailableTextFiles() {
		return this.availableTextFiles;
	}
	
	public void setAvailableTextFiles(final List<TextFile> availableTextFiles) {
		this.availableTextFiles = availableTextFiles;
	}
	
}
