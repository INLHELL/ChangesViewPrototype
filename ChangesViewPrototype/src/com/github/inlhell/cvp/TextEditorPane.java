package com.github.inlhell.cvp;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import com.github.inlhell.cvp.model.CurrentlyOpenTextFile;
import com.github.inlhell.cvp.model.TextFile;

@SuppressWarnings("serial")
public class TextEditorPane extends JScrollPane implements PropertyChangeListener, TextModificationIgnorable {
	
	private TabbedPane tabbedPane;
	private JTextArea textArea;
	private TextFile openedTextFile;
	private boolean textModificationIgnored = false;
	
	public TextEditorPane() {
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.setBackground(Color.GRAY);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
	}
	
	@Override
	public boolean isTextModificationIgnored() {
		return this.textModificationIgnored;
	}
	
	@Override
	public void setTextModificationIgnored(final boolean textModificationIgnored) {
		this.textModificationIgnored = textModificationIgnored;
	}
	
	/*
	 * This method prepares text area for the opened text file, if more then one
	 * text file is opened, in this case this method creates a tabbed pane and
	 * adds all the files to it
	 */
	public void createNewFile(final PropertyChangeListener propertyChangeListener) {
		// If the number of opened files equals one, we don't need tabbed pane
		if (CurrentlyOpenTextFile.getInstance().getNumberOfAvailableFiles() == 1) {
			this.textArea = new JTextArea();
			this.textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
			this.textArea.setForeground(new Color(87, 109, 117));
			this.textArea.setCaretPosition(0);
			this.textArea.setMargin(new Insets(5, 5, 5, 5));
			this.textArea.setLineWrap(true);
			this.textArea.setBackground(new Color(253, 246, 227));
			this.textArea.setWrapStyleWord(true);
			this.textArea.setText(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getText());
			this.textArea.getDocument().addDocumentListener(new TextAreaChangesListener(this.textArea, this));
			
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().addPropertyChangeListener(this);
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setOpened(true);
			// We have store reference to the file which is shown in text area of
			// TextEditPane class, next time when we will decide to open an additional
			// file we will use this reference, we just send it to the tabbed pane
			this.openedTextFile = CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile();
			this.setViewportView(this.textArea);
		}
		else {
			this.openedTextFile.removePropertyChangeListener(this);
			// If we already have one open file, we have to create the TabbedPane
			// copy an existing open file to one tab (this was the reason to keep
			// opendedTextFile reference), create a new tab and switch to
			// it. Also we have to remove textArea from the TextEditorPanel
			
			// Generally speaking we have three cases:
			// 1) one file - text area of TextEditorPane class
			// 2) two files - we have create TabbedPane class and add existing file
			// and the file which was opened
			// 3) more then two files - TabbedPane exists, we just add all files to it
			if (this.tabbedPane == null) {
				this.getViewport().remove(this.textArea);
				this.tabbedPane = new TabbedPane();
				this.tabbedPane.addNewTab(propertyChangeListener);
				this.getViewport().add(this.tabbedPane);
			}
			else {
				this.tabbedPane.addNewTab(propertyChangeListener);
			}
		}
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("text") && !this.textModificationIgnored) {
			this.getViewport().remove(this.textArea);
			this.createNewFile(null);
		}
	}
}
