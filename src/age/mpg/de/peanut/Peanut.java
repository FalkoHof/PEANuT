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
package age.mpg.de.peanut;

import java.io.FileNotFoundException;


import age.mpg.de.peanut.cytoscape.CytoscapeAttributeGetter;
import age.mpg.de.peanut.cytoscape.CytoscapeAttributeSetter;
import age.mpg.de.peanut.databases.webservices.DependenciesDownloader;
import age.mpg.de.peanut.databases.webservices.KeggService;
import age.mpg.de.peanut.databases.webservices.PathwayCommonsWebClient;
import age.mpg.de.peanut.managers.TaskManagerManager;
import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.parser.Parser;
import age.mpg.de.peanut.statistics.PathwayStatistics;

import cytoscape.logger.CyLogger;

// class for decision making -- > determines which classes are called and in which sequence
public class Peanut {

	private CyLogger logger = CyLogger.getLogger(this.getClass());
	
	public Peanut(){

	}

	
	//method called for annotating the network with pathwaydata
	public void findPathways() throws FileNotFoundException {
		
		//check if files are there
		new DependenciesDownloader().checkDependenciesWithDialog();
		
		// queue tasks
		//tasks to be performed when the option "wikipathways" is selected
		if (PeanutModel.getInstance().wikiPathways()){
			logger.info("findPathways(): queueing wikipathways tasks...");
			TaskManagerManager.getInstance().queueTask(new Parser(Parser.WIKIPATHWAYS));
			TaskManagerManager.getInstance().queueTask(new CytoscapeAttributeGetter(CytoscapeAttributeGetter.WIKIPATHWAYS, PeanutModel.getInstance().getColumnID()));
			TaskManagerManager.getInstance().queueTask(new CytoscapeAttributeSetter(CytoscapeAttributeSetter.WIKIPATHWAYS));
			logger.info("findPathways(): queueing wikipathways tasks - Done");

		}
		//tasks to be performed when the option "consensusPathDB" is selected
		if (PeanutModel.getInstance().consensusPathDB())	{
			logger.info("findPathways(): queueing consensusPathDB tasks...");
			TaskManagerManager.getInstance().queueTask(new Parser(Parser.CONSENSUSPATHDB));
			TaskManagerManager.getInstance().queueTask(new CytoscapeAttributeGetter(CytoscapeAttributeGetter.CONSENSUSPATHDB, PeanutModel.getInstance().getColumnID()));
			if (PeanutModel.getInstance().isImportKeggInteractions())
				TaskManagerManager.getInstance().queueTask(new KeggService());
			TaskManagerManager.getInstance().queueTask(new CytoscapeAttributeSetter(CytoscapeAttributeSetter.CONSENSUSPATHDB));
			logger.info("findPathways(): queueing consensusPathDB tasks - Done");
		}
		//tasks to be performed when the option "pcCommons" is selected
		if (PeanutModel.getInstance().pcCommons()){
			logger.info("findPathways(): queueing pathwayCommons tasks...");
			TaskManagerManager.getInstance().queueTask(new CytoscapeAttributeGetter(CytoscapeAttributeGetter.PATHWAYCOMMONS, PeanutModel.getInstance().getColumnID()));
			TaskManagerManager.getInstance().queueTask(new PathwayCommonsWebClient());
			TaskManagerManager.getInstance().queueTask(new CytoscapeAttributeSetter(CytoscapeAttributeSetter.PATHWAYCOMMONS));
			logger.info("findPathways(): queueing pathwayCommons tasks - Done");
		}
		
		// Execute Tasks in New Thread; pops open JTask Dialog Box.
		logger.info("findPathways(): start managing of queued tasks..");
		TaskManagerManager.getInstance().manageQueuedTasks();
		PeanutModel.getInstance().setFinished(true);
	}

	
	
	//method called for analyzing already annotated networks
	public boolean getResults(){
		
		logger.info("getResults(): queueing statistics tasks...");
		TaskManagerManager.getInstance().queueTask(new CytoscapeAttributeGetter(CytoscapeAttributeGetter.STATISTICS, PeanutModel.COLUMN_TITLE_PATHWAY_IDENTIFIER_LIST));
		TaskManagerManager.getInstance().queueTask(new PathwayStatistics());
		
		logger.info("getResults(): queueing statistics tasks - Done");
		logger.info("getResults(): start managing of queued tasks..");
		TaskManagerManager.getInstance().manageQueuedTasks();		
		
		return true;
		
	}
	
	
	
}
