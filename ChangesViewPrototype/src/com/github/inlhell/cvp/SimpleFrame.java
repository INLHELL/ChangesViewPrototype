package com.github.inlhell.cvp;

import java.awt.HeadlessException;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class SimpleFrame extends JFrame {
    
    public SimpleFrame() throws HeadlessException {
        this.setSize(200, 200);
        this.setLocation(50, 50);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void showIt() {
        this.setVisible(true);
    }
    
    public void showIt(final String title) {
        this.setTitle(title);
        this.setVisible(true);
    }
    
    public void showIt(final String title, final int x, final int y) {
        this.setTitle(title);
        this.setLocation(x, y);
        this.setVisible(true);
    }
    
    public void hideIt() {
        this.setVisible(false);
    }
    
}
