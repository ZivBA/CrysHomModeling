package ModellingTool;

import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;

/**
 * Created by zivben on 26/03/16.
 */
public class ChainListTableModel extends DefaultTableModel {


	public ChainListTableModel() {
		super(new String[]{"Chain ID", "Length","Process", "Strip", "Homologue"}, 0);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		Class clazz = String.class;
		switch (columnIndex) {
			case 0:
				clazz = String.class;
				break;
			case 1:
				clazz = Integer.class;
				break;
			case 2:
				clazz = Boolean.class;
				break;
			case 3:
				clazz = Boolean.class;
				break;
			case 4:
				clazz = Boolean.class;
				break;

		}
		return clazz;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return (column == 2 || column == 3 || column == 4);
	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if (aValue instanceof Boolean && (column == 2) ) {
			System.err.println("chain: " + this.getValueAt(row, 0) + " processing set to:" + aValue);
			Vector rowData = (Vector) getDataVector().get(row);
			rowData.set(2, (boolean) aValue);
			if ((Boolean)aValue) {
				for (int i = 0; i < this.getRowCount(); i++) {
					if (i != row) {
						this.setValueAt(false, i, column);
					}
				}
			}
			fireTableCellUpdated(row, column);
		} else if (aValue instanceof Boolean && (column == 3) ) {
			System.err.println("column: "+column+ "set to:" + aValue);
			Vector rowData = (Vector) getDataVector().get(row);
			rowData.set(3, (boolean) aValue);
			fireTableCellUpdated(row, column);
		} else if (aValue instanceof Boolean && (column == 4) ) {
			System.err.println("column: "+column+ "set to:" + aValue);
			Vector rowData = (Vector) getDataVector().get(row);
			rowData.set(4, (boolean) aValue);
			fireTableCellUpdated(row, column);
		}
	}

}

