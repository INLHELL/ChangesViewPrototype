package com.github.inlhell.cvp.model;

public class TextFile {
    private String name = "Untitled";
    private boolean changed = false;
    private String text = "";
    private String initialTextState = "";
    
    public TextFile() {
    }
    
    public TextFile(final String counetr) {
        this.name += counetr;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public boolean isChanged() {
        return this.changed;
    }
    
    public void setChanged(final boolean changed) {
        this.changed = changed;
    }
    
    public String getText() {
        return this.text;
    }
    
    public void setText(final String text) {
        this.text = text;
    }
    
    public String getInitialTextState() {
        return this.initialTextState;
    }
    
    public void setInitialTextState(final String initialTextState) {
        this.initialTextState = initialTextState;
    }
    
}
