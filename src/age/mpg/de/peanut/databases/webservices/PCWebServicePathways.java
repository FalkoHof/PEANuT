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


import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.utilityobjects.PathwayCommonsProtein;

import cytoscape.logger.CyLogger;

public class PCWebServicePathways extends Thread {
	
	private CyLogger logger = CyLogger.getLogger(this.getClass());
	private List<String>queryList;
	private List<PathwayCommonsProtein>resultList;
	
	public PCWebServicePathways(List<String>queryList){
		this.queryList = queryList;
		resultList = new ArrayList<PathwayCommonsProtein>();
	}
	
	public void run(){
		try {
			retrieveNodePathwayInformation();
		}catch (IOException e){
			try {
				logger.warn("Pathway Commons Webservice" + this.getName() + "failed\nretrying...", e);
				this.sleep(5000);
				run();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	
	private void retrieveNodePathwayInformation() throws IOException{
		URL query = getPathwaysQuery();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(query.openStream()));
  
        
        String inputURL = null;
	    String proteinId = null;
	    String pathwayURI = null;
	    String pathwayName = null;
        String idCheck = queryList.get(0); //gets the first query of the list
    	List<String>pathwayURIList = new ArrayList<String>();
    	List<String>pathwayNameList = new ArrayList<String>();


        while ((inputURL = in.readLine()) != null){
        	String[] input = inputURL.split("UNIPROT:"); //splits the return string into processable tokens
        	for (String s : input){	        		
        		if (s.contains("Database:ID") || s.contains("UNIPROT:")) //used to skip header and ID of the return string
        			continue;
        		else{
        			String [] temp = s.split("\t"); // splits the string into uniprotID,Pathway_Name,Pathway_Database_Name,CPATH_ID
        			if (temp.length == 4){	
        				proteinId = temp[0];
        				pathwayName = temp[1];
        				pathwayURI = pathwayName + PeanutModel.GENERAL_DELIMITER + PeanutModel.PATHWAYCOMMONS_DELIMITER + PeanutModel.GENERAL_DELIMITER + proteinId;
        				//statements to collect all pathway names for one query/uniprotID
        				if (proteinId.equals(idCheck)){ 
        					pathwayURIList.add(pathwayURI);
        					pathwayNameList.add(pathwayName);
        				}
        				else if (pathwayURIList.size() > 0 && proteinId != null){
        						PathwayCommonsProtein pathwaysPerProtein = new PathwayCommonsProtein(idCheck,pathwayURIList,pathwayNameList);
        						resultList.add(pathwaysPerProtein);
        						pathwayURIList = new ArrayList<String>();
        						pathwayNameList = new ArrayList<String>();
        						idCheck = proteinId;
        						pathwayURI = null;
        						proteinId = null;
        				}
        			}
        		}
        	}
            //add the last proteinQuery/pathways set
        	if (pathwayURI != null && proteinId != null){
        		pathwayURIList.add(pathwayURI);
        		PathwayCommonsProtein pathwaysPerProtein = new PathwayCommonsProtein(proteinId,pathwayURIList,pathwayNameList);
        		resultList.add(pathwaysPerProtein);
				pathwayURIList = new ArrayList<String>();
        	}
    	}
        in.close();		
	}	
	
	
	private URL getPathwaysQuery() throws MalformedURLException{
		String ids = "&q=";
		String source = PeanutModel.getInstance().getPathwayCommonsDataSourceParameters();
		
		for (String q : queryList)
			ids = ids + q + ",";		
		
		String query = "http://www.pathwaycommons.org/pc/webservice.do?cmd=get_pathways&version=2.0&input_id_type=UNIPROT" + ids + source;
		System.out.println(query);
		URL pcQuery = new URL(query);
			
		return pcQuery;			
	}

	public List<PathwayCommonsProtein> getResultList() {
		return resultList;
	}

}
