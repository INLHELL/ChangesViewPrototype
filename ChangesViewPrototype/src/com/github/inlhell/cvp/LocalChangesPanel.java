package com.github.inlhell.cvp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import com.github.inlhell.cvp.observer.Observable;
import com.github.inlhell.cvp.observer.Observer;

@SuppressWarnings("serial")
public class LocalChangesPanel extends JPanel implements Observable, PropertyChangeListener {
	
	ChangesTableModel changesTableModel;
	TextFile clickedFile;
	Set<Observer> observers = new HashSet<Observer>();
	final JTable table;
	
	public LocalChangesPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		final JToolBar toolbar = new JToolBar(SwingConstants.VERTICAL);
		toolbar.setFloatable(false);
		toolbar.setRollover(false);
		toolbar.setLayout(new GridLayout(6, 1));
		final JButton refreshButton = new JButton("Refresh");
		final JButton revertButton = new JButton("Revert");
		final JButton deleteButton = new JButton("Delete");
		deleteButton.setAlignmentX(100);
		toolbar.add(refreshButton, 0);
		toolbar.add(revertButton, 1);
		toolbar.add(deleteButton, 2);
		toolbar.setMaximumSize(new Dimension(200, 400));
		
		this.changesTableModel = new ChangesTableModel();
		this.table = new JTable(this.changesTableModel);
		this.table.setGridColor(Color.GRAY);
		this.table.setShowVerticalLines(false);
		this.table.setShowHorizontalLines(true);
		this.table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		this.table.setPreferredScrollableViewportSize(this.table.getPreferredSize());
		scrollPane.setViewportView(this.table);
		
		this.add(toolbar);
		this.add(scrollPane);
		
		this.table.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(final MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					final JTable target = (JTable) evt.getSource();
					final int row = target.getSelectedRow();
					LocalChangesPanel.this.clickedFile = AvailableTextFiles.getInstance().getAvailableTextFiles().get(row);
					CurrentlyOpenTextFile.getInstance().setCurrentlyOpenTextFile(LocalChangesPanel.this.clickedFile);
					CurrentlyOpenTextFile.getInstance().incrementNumberOfOpenFiles();
					CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().touch();
					LocalChangesPanel.this.notifyObservers();
				}
			}
		});
		
	}
	
	@Override
	public void notifyObservers() {
		for (final Observer observer : this.observers) {
			observer.update();
		}
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent arg0) {
		this.table.repaint();
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
