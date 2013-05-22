package com.github.inlhell.cvp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.github.inlhell.cvp.model.AvailableTextFiles;
import com.github.inlhell.cvp.model.CurrentlyOpenTextFile;
import com.github.inlhell.cvp.model.TextFile;
import com.github.inlhell.cvp.model.TextFileUtil;
import com.github.inlhell.cvp.observer.Observable;
import com.github.inlhell.cvp.observer.Observer;
import com.github.inlhell.cvp.svn.SVNClient;
import com.github.inlhell.cvp.svn.SVNStatusChecker;

@SuppressWarnings("serial")
public class LocalChangesPanel extends JPanel implements Observable, PropertyChangeListener {
	
	private final ChangesTableModel changesTableModel;
	private final Set<Observer> observers = new HashSet<Observer>();
	private final JTable fileStateTable;
	
	public LocalChangesPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		// Toolbar creation
		final JToolBar toolbar = new JToolBar(SwingConstants.VERTICAL);
		toolbar.setFloatable(false);
		toolbar.setRollover(false);
		toolbar.setLayout(new GridLayout(6, 1));
		final JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				LocalChangesPanel.this.refreshButtonActionPerformed(e);
			}
		});
		final JButton revertButton = new JButton("Revert");
		revertButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				LocalChangesPanel.this.revertButtonActionPerformed(e);
			}
		});
		final JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				LocalChangesPanel.this.deleteButtonActionPerformed(e);
			}
		});
		deleteButton.setAlignmentX(100);
		toolbar.add(refreshButton, 0);
		toolbar.add(revertButton, 1);
		toolbar.add(deleteButton, 2);
		toolbar.setMaximumSize(new Dimension(200, 400));
		
		// Table with filenames, full path and svn status
		this.changesTableModel = new ChangesTableModel();
		this.fileStateTable = new JTable(this.changesTableModel);
		this.fileStateTable.setGridColor(Color.GRAY);
		this.fileStateTable.setShowVerticalLines(false);
		this.fileStateTable.setShowHorizontalLines(true);
		this.fileStateTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		this.fileStateTable.setPreferredScrollableViewportSize(this.fileStateTable.getPreferredSize());
		scrollPane.setViewportView(this.fileStateTable);
		
		this.add(toolbar);
		this.add(scrollPane);
		
		// What happens if clicked twice on the specific row
		this.fileStateTable.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(final MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					final JTable target = (JTable) evt.getSource();
					final int row = target.getSelectedRow();
					final TextFile clickedFile = AvailableTextFiles.getInstance().getAvailableTextFiles().get(row);
					if (!clickedFile.isOpened()) {
						CurrentlyOpenTextFile.getInstance().setCurrentlyOpenTextFile(clickedFile);
						CurrentlyOpenTextFile.getInstance().incrementNumberOfOpenFiles();
						CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().touch();
						// We made double click, we have to notify BorderFrame about this
						// action because BorderFrame is responsible for new document
						// opening, BorderFrame will delegate this action to the
						// TextEditorPane
						LocalChangesPanel.this.notifyObservers();
					}
				}
			}
		});
		
	}
	
	protected void refreshButtonActionPerformed(@SuppressWarnings("unused") final ActionEvent arg0) {
		SVNStatusChecker.getInstance().checkStatus();
		this.fileStateTable.repaint();
	}
	
	protected void revertButtonActionPerformed(@SuppressWarnings("unused") final ActionEvent arg0) {
		for (int i = 0; i < this.fileStateTable.getSelectedRows().length; i++) {
			final int rowIndex = this.fileStateTable.getSelectedRows()[i];
			// We have to know the absolute path of the selected file
			// Indexes of files in the AvailableTextFiles are correspond with the
			// indexes of rows in our fileStateTable
			final TextFile selectedFile = AvailableTextFiles.getInstance().getAvailableTextFiles().get(rowIndex);
			final boolean isRevertDoneSuccessfully = SVNClient.getInstance().revert(new File(selectedFile.getAbsolutePath()));
			if (isRevertDoneSuccessfully) {
				try {
					selectedFile.setText(TextFileUtil.getInstance().readFileAsString(selectedFile.getAbsolutePath()));
					selectedFile.setInitialTextState(selectedFile.getText());
					selectedFile.setChanged(false);
					selectedFile.setStored(true);
					selectedFile.setSvnStatus("NORMAL");
				}
				catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.fileStateTable.repaint();
	}
	
	protected void deleteButtonActionPerformed(@SuppressWarnings("unused") final ActionEvent arg0) {
		for (int rowIndex : this.fileStateTable.getSelectedRows()) {
			rowIndex = this.fileStateTable.getSelectedRow();
			final TextFile selectedFile = AvailableTextFiles.getInstance().getAvailableTextFiles().get(rowIndex);
			SVNClient.getInstance().delete(new File(selectedFile.getAbsolutePath()));
		}
		this.fileStateTable.repaint();
	}
	
	@Override
	public void notifyObservers() {
		for (final Observer observer : this.observers) {
			observer.update();
		}
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals("svnStatus")) {
			this.fileStateTable.repaint();
		}
	}
	
	@Override
	public void registerObserver(final Observer observer) {
		this.observers.add(observer);
	}
	
	@Override
	public void removeObserver(final Observer observer) {
		this.observers.remove(observer);
	}
}
