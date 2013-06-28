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

import java.util.ArrayList;
import java.util.List;


import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.statistics.StatisticResults;

import cytoscape.CyNode;
import cytoscape.Cytoscape;


//class to create a network from a statistic result pathway object and view it
public class PathwayViewer {

	private StatisticResults pathway;
	
	public PathwayViewer(StatisticResults pathway){
		this.pathway = pathway;	
	}
	
	
	
	
	public void createNetworkFromPathway(){
		
		List<Integer> nodeRootGraphList = new ArrayList<Integer>();
		List<String>nodeIdList = new ArrayList<String>(pathway.getIdSet());
		
		// loop to get the root graph index of the nodes of one pathway
		for (String id : nodeIdList){
			CyNode node = Cytoscape.getCyNode(id);
			if (node != null)
				nodeRootGraphList.add(node.getRootGraphIndex());	
		}
		
		// convert the arrayList to an array;
		int counter = 0;
		int[]nodeRootGraphArray = new int[nodeRootGraphList.size()];
		for (Integer i : nodeRootGraphList){
			nodeRootGraphArray[counter] = i;
			counter++;
		}
		
		
		// get the edges connecting the nodes of one pathway
		
		int[]edgesRootGraphArray = Cytoscape.getNetwork(PeanutModel.getInstance().getChildNetwork()).getConnectingEdgeIndicesArray(nodeRootGraphArray);
			
		// create a new network from the edges and nodes
		Cytoscape.createNetwork(nodeRootGraphArray, edgesRootGraphArray, pathway.getPathwayName());	
	}
	
}


