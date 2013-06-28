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
package age.mpg.de.peanut.statistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import age.mpg.de.peanut.managers.TaskManagerManager;
import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.utilityobjects.cytoscapeparsing.pathway.PathwayObjectStatistics;

import cytoscape.logger.CyLogger;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;



//class that performes the pathway statistics (tests for enrichment via fischer's exact test)
public class PathwayStatistics implements Task {
	
	private cytoscape.task.TaskMonitor taskMonitor;
	private CyLogger logger = CyLogger.getLogger(this.getClass());
	
	private List<StatisticResults> resultList;
	private Map<String,PathwayObjectStatistics> pathwayDistributionMap;

    private boolean interrupted = false;
    private File outputFile;
    
    //standard constructor for constructing the class and running it via the cyotscape taskmanager
	public PathwayStatistics(){
	}

	
	// alternative constructor for saving the statistics as file
	public PathwayStatistics(File outputFile){
		logger.info("constructor for saving file called");
		resultList = new ArrayList<StatisticResults>(PeanutModel.getInstance().getStatisticsResultList());
		this.outputFile = outputFile;
		writeOutputFile();
	}
	
	// run needed for task monitor/execution as a separate task
	public void run(){
		try{
			logger.info("tyring to do statistics");
			doStatistics();
		}
		catch (Exception e) {
			logger.error("try method doStatistics() failed");
			e.printStackTrace();
		}	
		
	}
	
	//method for calculating p-values
	private void doStatistics(){
		taskMonitor.setStatus("Loading pathway data...");
		taskMonitor.setPercentCompleted(-1);
		
		int parentNetworkSize = PeanutModel.getInstance().getParentSize();
		int childNetworkSize = PeanutModel.getInstance().getChildSize();
		double threshold = PeanutModel.getInstance().getpValue();

		pathwayDistributionMap = PeanutModel.getInstance().getPathwayDistributionMap();
		
		List<String>pathwayNames = new ArrayList<String>(pathwayDistributionMap.keySet());
		
		taskMonitor.setStatus("Calculating results...");

		//List for holding the results
		resultList = new ArrayList<StatisticResults>();

		int counter = 0;

		//loop through all the pathways found in the child network
		for (String s : pathwayNames){
			//stuff for setting the percentage of the progress bar in the taks
			taskMonitor.setStatus("Creating contingency Tables...");
			counter++;
			int percentage = TaskManagerManager.getInstance().getPercentage(counter, pathwayNames.size());
			taskMonitor.setPercentCompleted(percentage);

			if (s == null || s.length() <= 1){
				logger.warn("childMap contains unexpected pathway name: " + s);
				continue;
			}
			
			// breaks the loop if task gets canceled
			if(interrupted){
				PeanutModel.getInstance().setExit(true);
				break;
			}
			
			//calculate significance via fischer's exact test
			PathwayObjectStatistics obj = pathwayDistributionMap.get(s);
			
			int numberOfFoundMembers = obj.getNumberOfFoundMembers();
			int pathwaySize = obj.getPathwaySize();
			
			//skips to the next pathway if 1 or less members are found
			if (numberOfFoundMembers <= 1)
				continue;
			
			HypergeometricDist hd = new HypergeometricDist(pathwaySize, parentNetworkSize,numberOfFoundMembers, childNetworkSize);
			
			
			double pValueOneTailed = hd.computeOneTailedFisher();
			int compare = Double.compare(pValueOneTailed, threshold);
			
			//check if p-Value is below the set threshold
			if (compare == 0 || compare < 0){
				StatisticResults res = new StatisticResults(s, obj.getBioIdsFoundMembers(), obj.getCytoscapeIdsFoundMembers(), obj.getPathwaySize(),pValueOneTailed);
				resultList.add(res);
			}
		}
		PeanutModel.getInstance().setStatisticsResultList(resultList);
		taskMonitor.setPercentCompleted(100);
	}
	
	
	// method for saving the results as file
	public void writeOutputFile(){

		//header of the output string displaying basic network properties
		String outputStr = "name parent network:\t"+ PeanutModel.getInstance().getParentNetwork() +"\tsize parent network:\t" + PeanutModel.getInstance().getParentSize() + "\n";
		outputStr = outputStr + "name child network:" + "\t"+ PeanutModel.getInstance().getChildNetwork() +  "\t" + "size child network:"+ "\t" + PeanutModel.getInstance().getChildSize() + "\t# pathways child network:\t" + PeanutModel.getInstance().getPathwayDistributionMap().size() + "\n"; 
		outputStr = outputStr + "P-Value cutoff:\t" + PeanutModel.getInstance().getpValue() + "\n\n";
		
		//header of the output string explaining the different variables of the contingency table
		outputStr = outputStr + "Pathway name\t" + "Datasource\t"+ "# nodes (child network)\t" + "# different biological identifiers (child network)\t"+ "Coverage of the pathway\t";
		outputStr = outputStr + "Coverage of the pathway (%)\t" + "p-value (one-tailed Fisher's exact test)\t" + "Entrez/UniprotIds of the members\t";
		outputStr = outputStr + "Cytoscape node IDs of the present proteins" +  "\n";  
		
		//loop that adds the statistic results to the output String
		for (int i = 0; i< resultList.size(); i++)
			outputStr = outputStr + resultList.get(i).getDisplayName() + "\t" + resultList.get(i).getDatabaseType()  + "\t" + resultList.get(i).getNumberOfNodes() + "\t" + resultList.get(i).getBioIdSet().size() + "\t" + resultList.get(i).getProportion() + "\t" + resultList.get(i).getCoverage()  + "\t" + resultList.get(i).getOneTailed() + "\t" + resultList.get(i).getIdSet() +"\t" + resultList.get(i).getCyIdSet() + "\n";
		
	
		//write the file
		 FileWriter fileWriter;
		try {
			logger.info("writing output file");
			fileWriter = new FileWriter(outputFile);
			fileWriter.write(outputStr);
			fileWriter.close();

		} catch (IOException e) {
			logger.error("writing ouput file failed");
			e.printStackTrace();
		}
	}
	
	
	@Override 
	public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
		if (this.taskMonitor != null) {
            throw new IllegalStateException("Task Monitor is already set.");
        }		
		taskMonitor = monitor;
	}
	
	@Override 
	public void halt() {
		logger.warning("canceled");
		this.interrupted = true;
	}
	
	@Override 
	public String getTitle() {
		return "PathwayFinder - Statistics";
	}

	//check if getter is needed
	public List<StatisticResults> getResultList() {
		return resultList;
	}
}
	
