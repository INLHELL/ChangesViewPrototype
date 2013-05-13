package com.github.inlhell.cvp;

import javax.swing.table.AbstractTableModel;

import com.github.inlhell.cvp.model.AvailableTextFiles;

@SuppressWarnings("serial")
public class ChangesTableModel extends AbstractTableModel {
	
	private static final int COLUMN_COUNT = 3;
	
	private final String[] columnNames = {
		"File Name",
		"Path",
		"State"
	};
	
	public ChangesTableModel() {
	}
	
	@Override
	public int getColumnCount() {
		return ChangesTableModel.COLUMN_COUNT;
	}
	
	@Override
	public String getColumnName(final int column) {
		return this.columnNames[column];
	}
	
	@Override
	public int getRowCount() {
		return AvailableTextFiles.getInstance().getAvailableTextFiles().size();
	}
	
	@Override
	public Object getValueAt(final int row, final int column) {
		if (column == 0) {
			return AvailableTextFiles.getInstance().getAvailableTextFiles().get(row).getName();
		}
		else if (column == 1) {
			return AvailableTextFiles.getInstance().getAvailableTextFiles().get(row).getAbsolutePath();
		}
		else if (column == 2) {
			return AvailableTextFiles.getInstance().getAvailableTextFiles().get(row).getSvnStatus();
		}
		return null;
	}
	
}
