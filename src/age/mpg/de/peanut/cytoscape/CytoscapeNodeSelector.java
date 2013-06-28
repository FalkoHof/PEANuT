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
package age.mpg.de.peanut.cytoscape;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.statistics.StatisticResults;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;

public class CytoscapeNodeSelector {
	
	private List<StatisticResults> resultList;
	private Set<CyNode>nodesSet;
	
	public CytoscapeNodeSelector(){
		resultList = PeanutModel.getInstance().getStatisticsResultList();
		selectNodes();
	}

	public void selectNodes(){
		getNodesToSelect();
		setSelectedNodes();
	}
	
	public void getNodesToSelect(){
		nodesSet = new HashSet<CyNode>();
		for (StatisticResults result : resultList){
			if (result.isSelected())
				for (String id : result.getCyIdSet())			
					nodesSet.add(Cytoscape.getCyNode(id));
		}
	}
	
	public void setSelectedNodes(){
		CyNetwork network = Cytoscape.getNetwork(PeanutModel.getInstance().getChildNetwork());
		Set<CyNode> selectedNodes = (Set<CyNode>) network.getSelectedNodes();
		if (selectedNodes.size() > 0 && selectedNodes!= null)
			network.unselectAllNodes();
		network.setSelectedNodeState(nodesSet, true);
		CyNetworkView view = Cytoscape.getNetworkView(PeanutModel.getInstance().getChildNetwork());
		view.updateView();
	}
	
}
