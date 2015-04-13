/*
 * Copyright (C) 2012-2013 Falko Hofmann Max Planck Institute for Biology
 * of Ageing, Cologne (MPI-age)
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package age.mpg.de.peanut.gui.tablemodel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

import age.mpg.de.peanut.statistics.StatisticResults;

//JTableModel to manage the Pathway Statistic Results
public class PeanutTableModelPathways extends AbstractTableModel {

	private List<StatisticResults> resultList;
	
	private	final String[] columns = {"Pathway","# Nodes (focus)", "Coverage", "Coverage (%)", "p-Value", "Datasource", "Select Nodes"};
	
	
	public static final int PATHWAYNAME_COLUMN = 0;
	public static final int NUMBER_OF_NODES_COLUMN = 1;
	public static final int COVERAGE_COLUMN = 2;
	public static final int COVERAGE_PERCENT_COLUMN = 3;
	public static final int P_VALUE_COLUMN = 4;
	public static final int SOURCE_COLUMN = 5;
	public static final int SELECT_COLUMN = 6;
	public static final int NUMBER_COLUMNS = 7;
		
	public PeanutTableModelPathways(List<StatisticResults> list){
		this.resultList = list;
	}
	
	@Override
	public int getColumnCount() {
		return NUMBER_COLUMNS;
	}

	@Override
	public String getColumnName(int columnIndex) {
		  return columns[columnIndex];
	}
	
	/*
	public boolean isVisualizable(int rowIndex){
		return resultList.get(rowIndex).isVisualizable();
	}
	*/
	@Override
	public int getRowCount() {
		return resultList.size();
	}
	
	@Override
	public Class getColumnClass(int columnIndex) {
		return getValueAt(0,columnIndex).getClass();
    }
		
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if((columnIndex == SELECT_COLUMN))
        	return true;
		else
    	   return false;
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		StatisticResults result = resultList.get(rowIndex);	
		
		if(columnIndex == SELECT_COLUMN)
			result.setSelected((Boolean) value);
		
		fireTableCellUpdated(rowIndex, columnIndex);		
	}
		
	public void setModel(List<StatisticResults> list){
		this.resultList = list;
		fireTableDataChanged();
	}
	
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		StatisticResults result = resultList.get(rowIndex);	
		
		DecimalFormat dfPvalue = new DecimalFormat("0.###E0");	
		DecimalFormat dfCoverage = new DecimalFormat("##.00");	
		
		switch(columnIndex){
			case PATHWAYNAME_COLUMN:
				return result.getDisplayName();
		    case NUMBER_OF_NODES_COLUMN:
		    	return result.getNumberOfNodes();
		    case COVERAGE_COLUMN:
		    	return result.getProportion();
		    case COVERAGE_PERCENT_COLUMN:
		    	return dfCoverage.format(result.getCoverage());
		    case P_VALUE_COLUMN:
		    	return dfPvalue.format(result.getOneTailed());
		    case SOURCE_COLUMN:
		    	return result.getDatabaseType();
		    case SELECT_COLUMN:
		    	return result.isSelected();
		    default:
		    	return null;
		}
	}
		
}
