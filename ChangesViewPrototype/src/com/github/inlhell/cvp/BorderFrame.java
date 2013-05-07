package com.github.inlhell.cvp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import com.github.inlhell.cvp.model.TextFile;
import com.github.inlhell.observer.Observer;

@SuppressWarnings("serial")
public class BorderFrame extends SimpleFrame implements Observer {
    private final TextEditorPanel textEditorPanel;
    private final LocalChangesPanel localChangesPanel;
    
    public BorderFrame() {
        this.setExtendedState(this.getExtendedState() | Frame.MAXIMIZED_BOTH);
        this.setMinimumSize(new Dimension(800, 640));
        
        // This panel contains text editor or tabs with editors (if we have
        // more then one open file)
        this.textEditorPanel = new TextEditorPanel();
        this.textEditorPanel.registerObserver(this);
        
        // Panel contains changes view
        this.localChangesPanel = new LocalChangesPanel();
        this.getContentPane().add(this.textEditorPanel, BorderLayout.CENTER);
        this.getContentPane().add(this.localChangesPanel, BorderLayout.SOUTH);
        
        // Menu bar and menu items (File->New|Save|Save As...|Close)
        final JMenuBar menuBar = new JMenuBar();
        
        final JMenu fileMenu = new JMenu("File");
        final JMenuItem newMenuItem = new JMenuItem("New", null);
        newMenuItem.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                BorderFrame.this.newMenuItemActionPerformed(arg0);
            }
        });
        fileMenu.add(newMenuItem);
        
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);
    }
    
    protected void newMenuItemActionPerformed(
        @SuppressWarnings("unused") final ActionEvent arg0) {
        this.textEditorPanel.createNewFile();
    }
    
    public String getCurrentFileName() {
        return this.textEditorPanel.getTitleForEditorWindow();
    }
    
    /*
     * This method is a part of Observer interface and was called from the
     * underlying UI element.
     * This method asks textEditorPanel for the title which must be shown
     * as title of the main window. Title looks like this: "FileName", if file
     * was modified and the changes wasn't stored, the title in addition has a
     * "*" (asterisk) sign after the file name, like this "MyFileName*". If more
     * then one file opened the the title contains a name of currently open tab.
     */
    @Override
    public void update(final TextFile textFile) {
        this.setTitle("Changes View Prototype - "
                      + this.textEditorPanel.getTitleForEditorWindow());
    }
    
}
