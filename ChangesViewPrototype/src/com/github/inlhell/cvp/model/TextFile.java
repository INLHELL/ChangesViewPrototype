package com.github.inlhell.cvp.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class TextFile {
	
	private String absolutePath = "";
	private boolean changed = false;
	private String initialTextState = "";
	private String name = "Untitled.txt";
	private boolean opened = false;
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private boolean stored = false;
	private String svnStatus;
	private String text = "";
	private final boolean touch = false;
	
	public TextFile() {
	}
	
	public TextFile(final String counetr) {
		String name = this.name.substring(0, this.name.length() - 5);
		name += String.valueOf(counetr);
		name += ".txt";
		this.name = name;
		this.absolutePath = this.name;
	}
	
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		this.propertyChangeSupport.addPropertyChangeListener(listener);
	}
	
	public String getAbsolutePath() {
		return this.absolutePath;
	}
	
	public String getInitialTextState() {
		return this.initialTextState;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getSimpeTitle() {
		final StringBuilder simpleTitle = new StringBuilder();
		simpleTitle.append(this.name);
		if (this.changed) {
			simpleTitle.append("*");
		}
		return simpleTitle.toString();
	}
	
	public String getSvnStatus() {
		return this.svnStatus;
	}
	
	public String getText() {
		return this.text;
	}
	
	public String getTitleWithPath() {
		final StringBuilder titleWithPath = new StringBuilder();
		if (this.stored) {
			titleWithPath.append(this.absolutePath);
		}
		else {
			titleWithPath.append(this.name);
		}
		if (this.changed) {
			titleWithPath.append("*");
		}
		return titleWithPath.toString();
	}
	
	public boolean isChanged() {
		return this.changed;
	}
	
	public boolean isOpened() {
		return this.opened;
	}
	
	public boolean isStored() {
		return this.stored;
	}
	
	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		this.propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	public void setAbsolutePath(final String absolutePath) {
		this.absolutePath = absolutePath;
	}
	
	public void setChanged(final boolean changed) {
		this.changed = changed;
	}
	
	public void setInitialTextState(final String initialTextState) {
		this.initialTextState = initialTextState;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	public void setOpened(final boolean opened) {
		this.opened = opened;
	}
	
	public void setStored(final boolean stored) {
		this.stored = stored;
	}
	
	public void setSvnStatus(final String svnStatus) {
		this.svnStatus = svnStatus;
	}
	
	public void setText(final String text) {
		this.text = text;
	}
	
	public void touch() {
		this.propertyChangeSupport.firePropertyChange("touch", !this.touch, this.touch);
	}
	
}
