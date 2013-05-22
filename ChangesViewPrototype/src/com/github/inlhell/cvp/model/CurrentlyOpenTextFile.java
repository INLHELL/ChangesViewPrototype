package com.github.inlhell.cvp.model;

public class CurrentlyOpenTextFile {
	
	private static class Handler {
		
		private static CurrentlyOpenTextFile instance = new CurrentlyOpenTextFile();
	}
	
	private TextFile currentlyOpenTextFile = null;
	private int numberOfAvailableFiles = 0;
	
	private CurrentlyOpenTextFile() {
	}
	
	public static CurrentlyOpenTextFile getInstance() {
		return Handler.instance;
	}
	
	public TextFile getCurrentlyOpenTextFile() {
		return this.currentlyOpenTextFile;
	}
	
	public int getNumberOfAvailableFiles() {
		return this.numberOfAvailableFiles;
	}
	
	public void increaseNumberOfAvailableFiles(final int number) {
		this.numberOfAvailableFiles += number;
	}
	
	public void incrementNumberOfOpenFiles() {
		this.numberOfAvailableFiles++;
	}
	
	public void setCurrentlyOpenTextFile(final TextFile currentlyOpenTextFile) {
		this.currentlyOpenTextFile = currentlyOpenTextFile;
	}
	
}
