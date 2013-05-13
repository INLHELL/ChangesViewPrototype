package com.github.inlhell.cvp;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import com.github.inlhell.cvp.model.AvailableTextFiles;
import com.github.inlhell.cvp.model.CurrentlyOpenTextFile;
import com.github.inlhell.cvp.model.TextFile;
import com.github.inlhell.cvp.observer.Observable;
import com.github.inlhell.cvp.observer.Observer;

@SuppressWarnings("serial")
public class LocalChangesPane extends JScrollPane implements Observable, PropertyChangeListener {
	
	ChangesTableModel changesTableModel;
	TextFile clickedFile;
	Set<Observer> observers = new HashSet<Observer>();
	final JTable table;
	
	public LocalChangesPane() {
		this.changesTableModel = new ChangesTableModel();
		this.table = new JTable(this.changesTableModel);
		
		final TableColumnModel tableColumnModel = this.table.getColumnModel();
		tableColumnModel.getColumn(0).setPreferredWidth(300);
		tableColumnModel.getColumn(0).setMinWidth(300);
		tableColumnModel.getColumn(1).setPreferredWidth(300);
		tableColumnModel.getColumn(1).setMinWidth(300);
		tableColumnModel.getColumn(2).setPreferredWidth(300);
		tableColumnModel.getColumn(2).setMinWidth(300);
		this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.table.setPreferredScrollableViewportSize(this.table.getPreferredSize());
		
		this.setViewportView(this.table);
		
		this.table.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(final MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					final JTable target = (JTable) evt.getSource();
					final int row = target.getSelectedRow();
					LocalChangesPane.this.clickedFile = AvailableTextFiles.getInstance().getAvailableTextFiles().get(row);
					CurrentlyOpenTextFile.getInstance().setCurrentlyOpenTextFile(LocalChangesPane.this.clickedFile);
					CurrentlyOpenTextFile.getInstance().incrementNumberOfOpenFiles();
					CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().touch();
					LocalChangesPane.this.notifyObservers();
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
		// this.changesTableModel.fireTableDataChanged();
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
