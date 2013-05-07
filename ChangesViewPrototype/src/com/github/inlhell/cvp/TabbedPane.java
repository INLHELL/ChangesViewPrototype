package com.github.inlhell.cvp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.github.inlhell.cvp.model.TextFile;
import com.github.inlhell.observer.Observable;
import com.github.inlhell.observer.Observer;

@SuppressWarnings("serial")
public class TabbedPane extends JTabbedPane implements Observable {
    
    private final List<Observer> observers = new ArrayList<Observer>();
    private int numberOfAvailableFiles = 0;
    private final List<TextFile> textFiles = new ArrayList<TextFile>();
    private TextFile currentlyOpenTextFile = null;
    private final Map<Integer, TextFile> tabFileMap =
        new HashMap<Integer, TextFile>();
    
    public TabbedPane() {
        this.setTabPlacement(SwingConstants.TOP);
        this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }
    
    /*
     * This method called from the TextEditorPanel in the case when the user
     * have one open file and he decides to create a new one. This method
     * executed always before createNewTab(). We have to create
     * two tabs, one for previously created file and another one for the new
     * file. We also have to take the content of available text file and copy it
     * to the newly created tab. Finally we have to switch to the second tab
     * which represents an editor for the new file.
     */
    public void createNewTab(
        final TextFile currentlyOpenTextFileInTextEditorPanel) {
        final JScrollPane scrollPane = new JScrollPane();
        final JTextArea textArea = new JTextArea();
        textArea.setForeground(new Color(87, 109, 117));
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setCaretPosition(0);
        textArea.setMargin(new Insets(5, 5, 5, 5));
        textArea.setLineWrap(true);
        textArea.setBackground(new Color(253, 246, 227));
        textArea.setWrapStyleWord(true);
        
        // New tab for existing file was created, we copy the content of this
        // file
        textArea.setText(currentlyOpenTextFileInTextEditorPanel.getText());
        
        this.numberOfAvailableFiles++;
        this.textFiles.add(currentlyOpenTextFileInTextEditorPanel);
        
        scrollPane.setViewportView(textArea);
        scrollPane
            .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane
            .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.addTab(currentlyOpenTextFileInTextEditorPanel.getName(),
            scrollPane);
        this.tabFileMap.put(Integer.valueOf(this.getTabCount() - 1),
            currentlyOpenTextFileInTextEditorPanel);
        
        textArea.getDocument().addDocumentListener(
            new TextAreaChangesListener(textArea));
        
        // We've finished with the existing file, now we call createNewTab()
        // without parameters to create a tab and a new file for it
        this.createNewTab();
        
        // This listener is called when we switch to another tab. First of all
        // we have to find out the index of open tab, then we take corresponding
        // text file from tabFileMap and specify a proper tab title.
        this.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                
                final int currentComponentIndex =
                    TabbedPane.this.getCurrentComponentIndex();
                
                TabbedPane.this.currentlyOpenTextFile =
                    TabbedPane.this.tabFileMap.get(Integer
                        .valueOf(currentComponentIndex));
                
                if (TabbedPane.this.currentlyOpenTextFile.isChanged()) {
                    TabbedPane.this.setTitleAt(currentComponentIndex,
                        TabbedPane.this.currentlyOpenTextFile.getName() + "*");
                }
                else {
                    TabbedPane.this.setTitleAt(currentComponentIndex,
                        TabbedPane.this.currentlyOpenTextFile.getName());
                }
                
                TabbedPane.this.notifyObservers();
            }
        });
    }
    
    public void createNewTab() {
        final JScrollPane scrollPane = new JScrollPane();
        final JTextArea textArea = new JTextArea();
        textArea.setForeground(new Color(87, 109, 117));
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setCaretPosition(0);
        textArea.setMargin(new Insets(5, 5, 5, 5));
        textArea.setLineWrap(true);
        textArea.setBackground(new Color(253, 246, 227));
        textArea.setWrapStyleWord(true);
        
        // currentlyOpenTextFile variable will refer to the newly created text
        // file
        this.currentlyOpenTextFile =
            new TextFile(String.valueOf(this.numberOfAvailableFiles));
        this.numberOfAvailableFiles++;
        this.textFiles.add(this.currentlyOpenTextFile);
        
        scrollPane.setViewportView(textArea);
        scrollPane
            .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane
            .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.addTab(this.currentlyOpenTextFile.getName(), scrollPane);
        this.tabFileMap.put(Integer.valueOf(this.getTabCount() - 1),
            this.currentlyOpenTextFile);
        this.setSelectedIndex(this.getTabCount() - 1);
        
        textArea.getDocument().addDocumentListener(
            new TextAreaChangesListener(textArea));
        
        // We have to notify the BorderFrame to change its title.
        this.notifyObservers();
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
    public void notifyObservers() {
        for (final Observer observer : this.observers) {
            observer.update(this.currentlyOpenTextFile);
        }
    }
    
    public int getCurrentComponentIndex() {
        boolean isComponentFound = false;
        int componnetIndex = 0;
        final Component[] tabbedPaneComponents =
            TabbedPane.this.getComponents();
        for (; (componnetIndex < tabbedPaneComponents.length)
               && !isComponentFound; componnetIndex++) {
            if (tabbedPaneComponents[componnetIndex].equals(TabbedPane.this
                .getSelectedComponent())) {
                isComponentFound = true;
            }
        }
        return componnetIndex - 1;
    }
    
    private final class TextAreaChangesListener implements DocumentListener {
        private final JTextArea textArea;
        
        private TextAreaChangesListener(final JTextArea textArea) {
            this.textArea = textArea;
        }
        
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
                !(this.textArea.getText()
                    .equals(TabbedPane.this.currentlyOpenTextFile
                        .getInitialTextState()));
            
            if (wasTextModifiedComparedWithInitState) {
                TabbedPane.this.currentlyOpenTextFile.setChanged(true);
                TabbedPane.this.currentlyOpenTextFile.setText(this.textArea
                    .getText());
                
                TabbedPane.this.setTitleAt(
                    TabbedPane.this.getCurrentComponentIndex(),
                    TabbedPane.this.currentlyOpenTextFile.getName() + "*");
            }
            else {
                TabbedPane.this.currentlyOpenTextFile.setChanged(false);
                TabbedPane.this.setTitleAt(
                    TabbedPane.this.getCurrentComponentIndex(),
                    TabbedPane.this.currentlyOpenTextFile.getName());
            }
            TabbedPane.this.notifyObservers();
        }
    }
    
}
