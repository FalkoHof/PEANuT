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
package age.mpg.de.peanut.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.utilityobjects.PluginProperties;
import age.mpg.de.peanut.utilityobjects.databaseparsing.pathway.ConsensusPathDBObject;
import age.mpg.de.peanut.utilityobjects.databaseparsing.pathway.PathwayObject;
import age.mpg.de.peanut.utilityobjects.databaseparsing.pathway.WikiPathwaysObject;

import cytoscape.logger.CyLogger;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

public class Parser implements Task {
	
	private cytoscape.task.TaskMonitor taskMonitor;
	private CyLogger logger = CyLogger.getLogger(this.getClass());

	public static final byte WIKIPATHWAYS = 0, CONSENSUSPATHDB = 1;
	
	private byte database;
	private boolean wp = false;
	private boolean cpdb = false;
	
	private boolean interrupted = false;
	private List<PathwayObject> cpdbPathways;
	private List<PathwayObject> wpPathways;
		
	public Parser(byte database){
		this.database = database;
		
	}
	
	
	private void parseData(){
		List<PathwayObject> pathwayList = new ArrayList<PathwayObject>();		
		try{
			switch (database){
				case WIKIPATHWAYS:		pathwayList = parseWikiPathways();
					break;
				case CONSENSUSPATHDB:	pathwayList = parseConsensusPathDB();
					break;
				default:				throw new InvalidParameterException("Invalid parameter for Parser");
			}
			PeanutModel.getInstance().setPathwayList(pathwayList);
		}
		catch (IOException e){
			logger.error("Parsing error", e);
			e.printStackTrace();
		}
	}
	
	
	private List<PathwayObject> parseWikiPathways() throws IOException{
		logger.info("parsing wikipathways");
		wp = true;
		wpPathways = new ArrayList<PathwayObject>();
		File folder = new File(PluginProperties.getInstance().getWikiPathwaysDirectory());
		parseFile(folder);
		
		return wpPathways;
	}
	
	
	private List<PathwayObject> parseConsensusPathDB() throws IOException{
		cpdb = true;
		cpdbPathways = new ArrayList<PathwayObject>();
		File folder = new File(PluginProperties.getInstance().getConsensusPathDBDirectory());
		parseFile(folder);
		
		return cpdbPathways;		
	}	
	
	
	private void parseFile(File folder) throws IOException{
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles){
			if (interrupted){
				PeanutModel.getInstance().setExit(true);
				break;
			}
			
			//substitute the space in the organism name with the html character in the downloaded files.
			String organism = PeanutModel.getInstance().getOrganism().replace(" ", "%20");
			//parses the file if the file belongs to the selected organism
			if (file.toString().contains(organism)){
				logger.info("parsing: " + file);
				BufferedReader reader = new BufferedReader(new FileReader(file));
				
				String line = reader.readLine(); // used to skip the first line headers
				while((line = reader.readLine()) != null){
					if (interrupted){
						PeanutModel.getInstance().setExit(true);
						break;
					}
						
					if (cpdb)
						processCPDBLine(line);
					if (wp)
						processWPLine(line);	
				}
			}
		}
	}


	//method to process one line of the wiki pathways flatfile
	private void processWPLine(String line){
		String[] temp = line.split("\t");
		if (temp.length > 9){
			String pathwayName = temp[0].trim();
			String pathwayURL = temp[3].trim();
			String pathwayID = pathwayURL.split("Pathway:")[1];
			String ids = temp[10].trim();
			List<String>uniprotIdList = Arrays.asList(ids.split("[,]"));
			WikiPathwaysObject pathway =  new WikiPathwaysObject(pathwayName,pathwayID, uniprotIdList);
			wpPathways.add(pathway);
		}
	}

	//method to process one line of the consensus path db flatfile
	public void processCPDBLine(String line){
		String[] temp = line.split("\t");
		if (temp.length > 2){
			String pathwayName = temp[0];
			String databaseName = temp[1];
			String ids = temp[2];
			List<String>idList =  Arrays.asList(ids.split("[,]"));
			ConsensusPathDBObject pathway = new ConsensusPathDBObject(pathwayName, databaseName, idList);
			cpdbPathways.add(pathway);
		}
	}

	
	@Override
	public String getTitle() {
		return "Pathway Finder - Database Parser";
	}

	@Override
	public void halt() {
		logger.warning("canceled");
		this.interrupted = true;
	}
	
	@Override
	public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
		taskMonitor = monitor;
	}


	@Override
	public void run() {
		parseData();
	}

}
