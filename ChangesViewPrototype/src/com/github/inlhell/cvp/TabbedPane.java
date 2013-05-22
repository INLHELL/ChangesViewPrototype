package com.github.inlhell.cvp;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.inlhell.cvp.model.AvailableTextFiles;
import com.github.inlhell.cvp.model.CurrentlyOpenTextFile;
import com.github.inlhell.cvp.model.TextFile;

@SuppressWarnings("serial")
public class TabbedPane extends JTabbedPane implements PropertyChangeListener, TextModificationIgnorable {
	
	private final Map<Integer, TextFile> tabFileMap = new HashMap<Integer, TextFile>();
	private boolean textModificationIgnored = false;
	private boolean ignoreTabSelection = false;
	
	public TabbedPane() {
		this.setTabPlacement(SwingConstants.TOP);
		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}
	
	@Override
	public boolean isTextModificationIgnored() {
		return this.textModificationIgnored;
	}
	
	@Override
	public void setTextModificationIgnored(final boolean textModificationIgnored) {
		this.textModificationIgnored = textModificationIgnored;
	}
	
	public void addNewTab(final PropertyChangeListener propertyChangeListener) {
		// If we create new tab first time, we have to copy text from the textArea
		// of the TextEditorPane first, create a new tab for this text and only
		// afterwards create a new tab
		if (CurrentlyOpenTextFile.getInstance().getNumberOfAvailableFiles() == 2) {
			// we have to figure out which file is opened too, because
			// CurrentlyOpenFile singleton contains another, immediately opened file
			for (final TextFile currentyOpenTextFile : AvailableTextFiles.getInstance().getAvailableTextFiles()) {
				// We found opened file
				if (currentyOpenTextFile.isOpened()) {
					final JScrollPane preparedScrollPane = this.prepareNewTab(currentyOpenTextFile);
					this.addTab(currentyOpenTextFile.getSimpeTitle(), preparedScrollPane);
					this.tabFileMap.put(Integer.valueOf(this.getTabCount() - 1), currentyOpenTextFile);
				}
			}
		}
		
		// Creating a new tab
		// Set all necessary properties of CurrentlyOpenTextFile
		CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setOpened(true);
		CurrentlyOpenTextFile.getInstance().incrementNumberOfOpenFiles();
		CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().addPropertyChangeListener(propertyChangeListener);
		
		// Prepare new tab for the opened file
		final JScrollPane preparedScrollPane =
			this.prepareNewTab(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile());
		// Add newly created tab
		this.addTab(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getName(), preparedScrollPane);
		// Put Tab->TextFile pair to the tabFile map
		this.tabFileMap.put(Integer.valueOf(this.getTabCount() - 1), CurrentlyOpenTextFile.getInstance()
			.getCurrentlyOpenTextFile());
		// Go to the newly created tab
		this.setSelectedIndex(this.getTabCount() - 1);
		
		// This listener performed when we switch to another tab
		this.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(final ChangeEvent e) {
				// We might ignore tab changing, in case we revert content of the text
				// text file and to update its content, we delete tab which contains its
				// content and re-added it with new content
				if (!TabbedPane.this.ignoreTabSelection) {
					// Here we change our CurrentlyOpenTextFile, because we switched to
					// another tab
					final int currentComponentIndex = TabbedPane.this.getSelectedIndex();
					CurrentlyOpenTextFile.getInstance().setCurrentlyOpenTextFile(
						TabbedPane.this.tabFileMap.get(Integer.valueOf(currentComponentIndex)));
					// The title of BorderFrame must be updated as well, so we call
					// touch() method of the currently open TextFile, BorderFrame listens
					// this kind of changes and will update its title (it will add the
					// name of currently open file to the title)
					CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().touch();
				}
			}
		});
	}
	
	/*
	 * This method prepares a tab for adding to the tabbed pane
	 */
	private JScrollPane prepareNewTab(final TextFile currentlyOpenTextFile) {
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
		currentlyOpenTextFile.addPropertyChangeListener(this);
		textArea.getDocument().addDocumentListener(new TextAreaChangesListener(textArea, this));
		return scrollPane;
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		// If text file was changed we need its name with an asterisk sign
		if (evt.getPropertyName().equals("changed")) {
			this.setTitleAt(TabbedPane.this.getSelectedIndex(), CurrentlyOpenTextFile.getInstance()
				.getCurrentlyOpenTextFile().getSimpeTitle());
		}
		// When we revert text file state, the text which this file contains was
		// changed as well and this file is shown at the tabbed pane, to update text
		// content shown at the tabbed pane we have to:
		// 1) Find the tab which shows the reverted text file
		// 2) Remove this tab
		// 3) Create a new tab and insert it to the particular place
		else if (evt.getPropertyName().equals("text") && !this.textModificationIgnored
							&& ((TextFile) evt.getSource()).isOpened()) {
			int tabIndex = -1;
			// First we are looking for the tab index
			final Iterator<Entry<Integer, TextFile>> tabFileMapIterator = this.tabFileMap.entrySet().iterator();
			while ((tabIndex == -1) && tabFileMapIterator.hasNext()) {
				final Entry<Integer, TextFile> entry = tabFileMapIterator.next();
				if (entry.getValue().equals(evt.getSource())) {
					tabIndex = entry.getKey().intValue();
				}
			}
			// If we found tab which presents reverted text file
			if (tabIndex != -1) {
				// We have to ignore tab selection listener execution
				this.ignoreTabSelection = true;
				// We remove tab with particular index and create a new one
				this.removeTabAt(tabIndex);
				final JScrollPane preparedJScrollPane = this.prepareNewTab((TextFile) evt.getSource());
				this.insertTab(((TextFile) evt.getSource()).getName(), null, preparedJScrollPane, null, tabIndex);
				// If reverted file is selected and open at the tabbed pane at the
				// moment we have to switch to this tab
				final boolean isRevertedTextFileOpen =
					CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().equals(evt.getSource());
				if (isRevertedTextFileOpen) {
					this.setSelectedIndex(tabIndex);
				}
				this.ignoreTabSelection = false;
			}
		}
	}
}
