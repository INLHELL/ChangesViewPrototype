package com.github.inlhell.cvp;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.github.inlhell.cvp.model.TextFile;
import com.github.inlhell.observer.Observable;
import com.github.inlhell.observer.Observer;

@SuppressWarnings("serial")
public class TextEditorPanel extends JScrollPane implements Observable,
    Observer {
    
    private final JTextArea textArea = new JTextArea();
    private TabbedPane tabbedPane;
    private TextFile currentlyOpenFile = null;
    private final List<Observer> observers = new ArrayList<Observer>();
    
    public TextEditorPanel() {
        this.textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        this.textArea.setForeground(new Color(87, 109, 117));
        this.textArea.setCaretPosition(0);
        this.textArea.setMargin(new Insets(5, 5, 5, 5));
        this.textArea.setLineWrap(true);
        this.textArea.setBackground(new Color(253, 246, 227));
        this.textArea.setWrapStyleWord(true);
        
        this.textArea.getDocument().addDocumentListener(
            new TextAreaChangesListener());
        
        this.setViewportView(this.textArea);
        this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.setBackground(Color.GRAY);
        this.setBorder(BorderFactory.createLineBorder(Color.black));
        
        final TextFile firstTextFile = new TextFile();
        this.currentlyOpenFile = firstTextFile;
    }
    
    public TextFile getCurrentlyOpenFile() {
        return this.currentlyOpenFile;
    }
    
    public void setCurrentlyOpenFile(final TextFile currentlyOpenFile) {
        this.currentlyOpenFile = currentlyOpenFile;
    }
    
    @Override
    public void notifyObservers() {
        for (final Observer observer : this.observers) {
            observer.update(this.currentlyOpenFile);
        }
    }
    
    public void createNewFile() {
        // If we already have one open file, we have to create the TabbedPane
        // copy an existing open file to one tab, create a new tab and switch to
        // it. Also we have to remove textArea from the TextEditorPanel.
        if (this.tabbedPane == null) {
            this.getViewport().remove(this.textArea);
            this.tabbedPane = new TabbedPane();
            this.tabbedPane.registerObserver(this);
            this.tabbedPane.createNewTab(this.currentlyOpenFile);
            this.getViewport().add(this.tabbedPane);
        }
        else {
            this.tabbedPane.createNewTab();
        }
    }
    
    public String getTitleForEditorWindow() {
        final StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(this.currentlyOpenFile.getName());
        if (this.currentlyOpenFile.isChanged()) {
            titleBuilder.append("*");
        }
        return titleBuilder.toString();
    }
    
    @Override
    public void registerObserver(final Observer observer) {
        this.observers.add(observer);
    }
    
    @Override
    public void removeObserver(final Observer observer) {
        this.observers.remove(observer);
    }
    
    @Override
    public void update(final TextFile textFile) {
        this.currentlyOpenFile = textFile;
        this.notifyObservers();
    }
    
    private final class TextAreaChangesListener implements DocumentListener {
        @Override
        public void removeUpdate(final DocumentEvent e) {
            this.analyzeTextAreaModification();
        }
        
        @Override
        public void insertUpdate(final DocumentEvent e) {
            this.analyzeTextAreaModification();
        }
        
        @Override
        public void changedUpdate(final DocumentEvent e) {
            this.analyzeTextAreaModification();
        }
        
        /**
         * 
         */
        private void analyzeTextAreaModification() {
            final boolean wasTextModifiedComparedWithInitState =
                !(TextEditorPanel.this.textArea.getText()
                    .equals(TextEditorPanel.this.currentlyOpenFile
                        .getInitialTextState()));
            
            if (wasTextModifiedComparedWithInitState) {
                TextEditorPanel.this.currentlyOpenFile.setChanged(true);
                TextEditorPanel.this.currentlyOpenFile
                    .setText(TextEditorPanel.this.textArea.getText());
            }
            else {
                TextEditorPanel.this.currentlyOpenFile.setChanged(false);
            }
            TextEditorPanel.this.notifyObservers();
        }
    }
}
