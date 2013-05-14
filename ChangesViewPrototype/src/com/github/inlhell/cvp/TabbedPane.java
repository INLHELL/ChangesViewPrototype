package com.github.inlhell.cvp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
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

import com.github.inlhell.cvp.model.AvailableTextFiles;
import com.github.inlhell.cvp.model.CurrentlyOpenTextFile;
import com.github.inlhell.cvp.model.TextFile;

@SuppressWarnings("serial")
public class TabbedPane extends JTabbedPane implements PropertyChangeListener {
	
	private final Map<Integer, TextFile> tabFileMap = new HashMap<Integer, TextFile>();
	
	public TabbedPane() {
		this.setTabPlacement(SwingConstants.TOP);
		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}
	
	public void addNewTab(final PropertyChangeListener propertyChangeListener) {
		if (CurrentlyOpenTextFile.getInstance().getNumberOfAvailableFiles() == 2) {
			for (final TextFile currentyOpenTextFile : AvailableTextFiles.getInstance().getAvailableTextFiles()) {
				if (currentyOpenTextFile.isOpened()) {
					this.prepareNewTab(currentyOpenTextFile);
					this.tabFileMap.put(Integer.valueOf(this.getTabCount() - 1), currentyOpenTextFile);
				}
			}
		}
		CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setOpened(true);
		CurrentlyOpenTextFile.getInstance().incrementNumberOfOpenFiles();
		CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().addPropertyChangeListener(propertyChangeListener);
		this.prepareNewTab(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile());
		this.tabFileMap.put(Integer.valueOf(this.getTabCount() - 1), CurrentlyOpenTextFile.getInstance()
			.getCurrentlyOpenTextFile());
		this.setSelectedIndex(this.getTabCount() - 1);
		CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().touch();
		
		this.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(final ChangeEvent e) {
				
				final int currentComponentIndex = TabbedPane.this.getCurrentComponentIndex();
				
				CurrentlyOpenTextFile.getInstance().setCurrentlyOpenTextFile(
					TabbedPane.this.tabFileMap.get(Integer.valueOf(currentComponentIndex)));
				CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().touch();
			}
		});
	}
	
	private void prepareNewTab(final TextFile currentlyOpenTextFile) {
		final JScrollPane scrollPane = new JScrollPane();
		final JTextArea textArea = new JTextArea();
		textArea.setForeground(new Color(87, 109, 117));
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
		textArea.setCaretPosition(0);
		textArea.setMargin(new Insets(5, 5, 5, 5));
		textArea.setLineWrap(true);
		textArea.setBackground(new Color(253, 246, 227));
		textArea.setWrapStyleWord(true);
		textArea.setText(currentlyOpenTextFile.getText());
		scrollPane.setViewportView(textArea);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.addTab(currentlyOpenTextFile.getName(), scrollPane);
		currentlyOpenTextFile.addPropertyChangeListener(this);
		textArea.getDocument().addDocumentListener(new TextAreaChangesListener(textArea));
	}
	
	public int getCurrentComponentIndex() {
		boolean isComponentFound = false;
		int componnetIndex = 0;
		final Component[] tabbedPaneComponents = TabbedPane.this.getComponents();
		for (; (componnetIndex < tabbedPaneComponents.length) && !isComponentFound; componnetIndex++) {
			if (tabbedPaneComponents[componnetIndex].equals(TabbedPane.this.getSelectedComponent())) {
				isComponentFound = true;
			}
		}
		return componnetIndex - 1;
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		this.setTitleAt(TabbedPane.this.getCurrentComponentIndex(), CurrentlyOpenTextFile.getInstance()
			.getCurrentlyOpenTextFile().getSimpeTitle());
	}
	
	private final class TextAreaChangesListener implements DocumentListener {
		
		private final JTextArea textArea;
		
		private TextAreaChangesListener(final JTextArea textArea) {
			this.textArea = textArea;
		}
		
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
				!(this.textArea.getText().equals(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile()
					.getInitialTextState()));
			
			if (wasTextModifiedComparedWithInitState) {
				CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setChanged(true);
				CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setText(this.textArea.getText());
			}
			else {
				CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setChanged(false);
			}
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().touch();
		}
	}
	
}
