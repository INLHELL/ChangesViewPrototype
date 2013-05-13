package com.github.inlhell.cvp;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.github.inlhell.cvp.model.CurrentlyOpenTextFile;

@SuppressWarnings("serial")
public class TextEditorPane extends JScrollPane {
	
	private final class TextAreaChangesListener implements DocumentListener {
		
		@Override
		public void changedUpdate(final DocumentEvent e) {
			this.analyzeTextAreaModification();
		}
		
		@Override
		public void insertUpdate(final DocumentEvent e) {
			this.analyzeTextAreaModification();
		}
		
		@Override
		public void removeUpdate(final DocumentEvent e) {
			this.analyzeTextAreaModification();
		}
		
		private void analyzeTextAreaModification() {
			final boolean wasTextModifiedComparedWithInitState =
				!(TextEditorPane.this.textArea.getText().equals(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile()
					.getInitialTextState()));
			
			if (wasTextModifiedComparedWithInitState) {
				CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setChanged(true);
				CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setText(TextEditorPane.this.textArea.getText());
			}
			else {
				CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setChanged(false);
			}
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().touch();
		}
	}
	
	private TabbedPane tabbedPane;
	
	private final JTextArea textArea = new JTextArea();
	
	public TextEditorPane() {
		
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.setBackground(Color.GRAY);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
	}
	
	public void createNewFile(final PropertyChangeListener propertyChangeListener) {
		if (CurrentlyOpenTextFile.getInstance().getNumberOfAvailableFiles() == 1) {
			this.textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
			this.textArea.setForeground(new Color(87, 109, 117));
			this.textArea.setCaretPosition(0);
			this.textArea.setMargin(new Insets(5, 5, 5, 5));
			this.textArea.setLineWrap(true);
			this.textArea.setBackground(new Color(253, 246, 227));
			this.textArea.setWrapStyleWord(true);
			this.textArea.getDocument().addDocumentListener(new TextAreaChangesListener());
			
			this.setViewportView(this.textArea);
			
			this.textArea.setText(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getText());
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setOpened(true);
		}
		else {
			
			// If we already have one open file, we have to create the TabbedPane
			// copy an existing open file to one tab, create a new tab and switch to
			// it. Also we have to remove textArea from the TextEditorPanel.
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
}
