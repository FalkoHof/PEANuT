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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import age.mpg.de.peanut.managers.TaskManagerManager;
import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.utilityobjects.PathwayCommonsProtein;
import age.mpg.de.peanut.utilityobjects.PluginProperties;

import cytoscape.logger.CyLogger;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

public class PathwayCommonsWebClient implements Task {
	
	private CyLogger logger = CyLogger.getLogger(this.getClass());
	private cytoscape.task.TaskMonitor taskMonitor;	
	private boolean interrupted = false;
	private List<List<String>> queryBatchList;
	private List<PathwayCommonsProtein> proteinPathwayList;
	private final int MAX_THREADS_PATHWAYS = PluginProperties.getInstance().getMaxThreadsPathways();
	
	//private final int MAX_THREADS_INTERACTIONS = PluginProperties.getInstance().getMaxThreadsInteractions();
	//private List<PathwayCommonsInteraction>interactionList;

	
	public PathwayCommonsWebClient(){
		this.queryBatchList = new ArrayList<List<String>>();
	}
	
	@Override 
	public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
		taskMonitor = monitor;
	}
	
	@Override 
	public void halt() {
		logger.warning("canceled");
		this.interrupted = true;
	}
	
	@Override 
	public String getTitle() {
		return "PathwayCommons Web Service";
	}

	
	// needed for task monitor
	public void run(){

		findPathways();		
	}
	
	
	public void findPathways(){
		try {
			if (!interrupted)
				generateQuerys();
			if (!interrupted)
				runOnlineQueryPathways();
			/*
			 * Commented out as the pathway commons server can crash when using this command.
			 * 
			if (!interrupted)
				runOnlineQueryInteractions();
			*/
			
		} catch (InterruptedException e) {
			logger.error("PathwayCommonsWebClient interrupted", e);
			e.printStackTrace();
		}	
	}
	

	// query manager for finding the pathways proteins are involved in --> basically creates an array of threads holding objects requesting pathway information from pc
	public void runOnlineQueryPathways() throws InterruptedException{
		
		taskMonitor.setStatus("Fetching pathway information...");
		taskMonitor.setPercentCompleted(-1);			

		List<PCWebServicePathways>threadList = new ArrayList<PCWebServicePathways>();
		proteinPathwayList = new ArrayList<PathwayCommonsProtein>();
		
		for (int i = 0; i< queryBatchList.size(); i++){
			
			if (interrupted){
				PeanutModel.getInstance().setExit(true);
				break;
			}
			
			//sets the percentage of queries send in the task monitor
			int percentage = TaskManagerManager.getInstance().getPercentage(i,queryBatchList.size());
			taskMonitor.setPercentCompleted(percentage);
			
			System.out.println("#Iterations: " + i + " Percentage completed: " + percentage);
			
			if (threadList.size() < MAX_THREADS_PATHWAYS){
				PCWebServicePathways pathwaysThread = new PCWebServicePathways(queryBatchList.get(i));
				pathwaysThread.setName("PC Webservice Thread #" + i);
				pathwaysThread.start();
				threadList.add(pathwaysThread);	
			}
			else{
				for (int t = 0; t < threadList.size(); t++){
					PCWebServicePathways thread = threadList.get(t);
					thread.join();
					proteinPathwayList.addAll(thread.getResultList());
				}
				threadList = new ArrayList<PCWebServicePathways>();
				PCWebServicePathways pathwaysTread = new PCWebServicePathways(queryBatchList.get(i));	
				threadList.add(pathwaysTread);	
			}
		}
		//needed to add the last results
		for (int t = 0; t < threadList.size(); t++){
			PCWebServicePathways thread = threadList.get(t);
			thread.join();
			proteinPathwayList.addAll(thread.getResultList());
		}
		
		PeanutModel.getInstance().setProteinPathwayList(proteinPathwayList);
		
	taskMonitor.setPercentCompleted(100);			
	}
	
		
	/*
	 * Commented out as the pathway commons server can crash when using this command.
	 * 
	// query manager for finding the interactions proteins are involved in --> basically creates an array of threads holding objects requesting pathway information from pc
	public void runOnlineQueryInteractions() throws InterruptedException{
		taskMonitor.setStatus("Fetching interactions...");
		taskMonitor.setPercentCompleted(-1);			

		List<PCWebServiceNeighbors>threadList = new ArrayList<PCWebServiceNeighbors>();
		interactionList = new ArrayList<PathwayCommonsInteraction>();
		
		//get whole network id list as query and removes possible duplicates
		List<String> queryList = PathwayFinderModel.getInstance().getWholeNetworkIdList();
		Set<String> querySet = new HashSet<String>(queryList);
		queryList.clear();
		queryList.addAll(querySet);
	
		
		// loop that starts a new thread and adds it to a list of threads
		for (int i = 0; i< queryList.size(); i++){
			
			if (interrupted){
				PathwayFinderModel.getInstance().setExit(true);
				break;
			}
			
			//sets the percentage of queries send in the task monitor
			int percentage = TaskManagerManager.getInstance().getPercentage(i,queryList.size());
			taskMonitor.setPercentCompleted(percentage);	
			// if the list is smaller than the max number of new threads, start a new thread
			if (threadList.size() < MAX_THREADS_INTERACTIONS){
				PCWebServiceNeighbors pathwaysTread = new PCWebServiceNeighbors(datasource, queryList.get(i),querySet);	
				pathwaysTread.start();
				threadList.add(pathwaysTread);	
			}
			//else wait till the threads are finished running and get the results
			else{
				for (int t = 0; t < threadList.size(); t++){
					PCWebServiceNeighbors thread = threadList.get(t);
					thread.join();
					if(thread.getResultList() != null)
						interactionList.addAll(thread.getResultList());
				}
				//clear all the temporary stuff, create a new thread and add it to the thread list
				threadList = new ArrayList<PCWebServiceNeighbors>();
				PCWebServiceNeighbors pathwaysTread = new PCWebServiceNeighbors(datasource, queryList.get(i),querySet);	
				threadList.add(pathwaysTread);	
			}
		}
		for (int t = 0; t < threadList.size(); t++){
			PCWebServiceNeighbors thread = threadList.get(t);
			thread.join();
			interactionList.addAll(thread.getResultList());
		}
		taskMonitor.setPercentCompleted(100);			
	}
*/
	
	
	// function that generates a list of query batches a 25 --> needed for runOnlineQueryPathways() as this function of PC takes batches of 25
	public void generateQuerys(){
		taskMonitor.setStatus("Generating online queries...");
		taskMonitor.setPercentCompleted(-1);			

		//get whole network id list as query and removes possible duplicates
		List<String> queryList = PeanutModel.getInstance().getWholeNetworkIdList();
		Set<String> querySet = new HashSet<String>(queryList);
		queryList.clear();
		queryList.addAll(querySet);
		
		List<String>oneQuery = new ArrayList<String>();
		
		for (int i = 0; i < queryList.size(); i++){	
			
			//sets the percentage of queries send in the task monitor
			int percentage = TaskManagerManager.getInstance().getPercentage(i,queryList.size());
			taskMonitor.setPercentCompleted(percentage);	
			
			//creates query batches out of 25 and adds them to an array list
			oneQuery.add(queryList.get(i));			
			if (i % 25 == 0){
				queryBatchList.add(oneQuery);
				oneQuery = new ArrayList<String>();
			}
		}
		queryBatchList.add(oneQuery);
		System.out.println("Querys complete.... Number of batches: " + queryBatchList.size());
		taskMonitor.setPercentCompleted(100);			
	}
}
