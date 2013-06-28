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
package age.mpg.de.peanut.databases.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.utilityobjects.PathwayCommonsInteraction;
import age.mpg.de.peanut.utilityobjects.PluginProperties;

public class PCWebServiceNeighbors extends Thread {
	String datasource;
	String query;
	Set<String>completeQuerySet;
	List<PathwayCommonsInteraction>resultList;
	
	public PCWebServiceNeighbors(String datasource, String query, Set<String>completeQuerySet){
		this.datasource = datasource;
		this.query = query;
		this.completeQuerySet = completeQuerySet;
		resultList = new ArrayList<PathwayCommonsInteraction>();
		System.out.println("Neighbor query: " + query);
	}
	
	@Override
	public void run(){
		try {
			retrieveNeighbourInformation();
		}catch (Exception e){
			try {
				System.out.println(e);
				this.sleep(5000);
				System.out.println("Retrying...");
				//run();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
		
	private void retrieveNeighbourInformation() throws IOException{
		URL query = getNeighborQuery();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(query.openStream()));
	    String inputURL;
	    
	    while ((inputURL = in.readLine()) != null){
	    	String[] input = inputURL.split(" ");
	        for(String s :input){
	        	if (s.contains("NOT_SPECIFIED"))
	        		continue;
	        	else{
	        		String [] temp = s.split("\t");
	        		if (temp.length == 3){
	        			String interactorA = temp[0];
	        			String interactionType = temp[1];
	        			String interactorB = temp[2]; 
	        			if (interactorA.contains("-"))
	        				interactorA = interactorA.split("-")[0];
	        			if (interactorB.contains("-"))
	        				interactorB = interactorB.split("-")[0];
	        			if (completeQuerySet.contains(interactorA) && completeQuerySet.contains(interactorB)){
	        				PathwayCommonsInteraction edge = new PathwayCommonsInteraction(interactorA, interactionType, interactorB);
	        				edge.print();
	        				resultList.add(edge);
	        			}	
	        		}
	        	}
	        }
	       }
	    	in.close();		
		}
	
	
	private URL getNeighborQuery() throws MalformedURLException{
		String id = "&q=" + query;
		String source = PeanutModel.getInstance().getPathwayCommonsDataSourceParameters();
		String interactionTypes = "&binary_interaction_rule=" + PluginProperties.getInstance().getPathwayCommonsSifInteractionParameters();
		String query = "http://www.pathwaycommons.org/pc/webservice.do?version=3.0&cmd=get_neighbors&input_id_type=UNIPROT&output_id_type=UNIPROT&output=binary_sif" + id + interactionTypes + source;
		
		URL pcQuery = new URL(query);
		
		return pcQuery;
	}


	public List<PathwayCommonsInteraction> getResultList() {
		return resultList;
	}
			
	
}
