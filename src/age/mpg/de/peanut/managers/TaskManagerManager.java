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
package age.mpg.de.peanut.managers;

import java.util.ArrayList;
import java.util.List;

import age.mpg.de.peanut.model.PeanutModel;

import cytoscape.Cytoscape;
import cytoscape.logger.CyLogger;
import cytoscape.task.Task;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;

// TaskManagerManager --> singleton because cytoscape taksmanager can only handle one task at a time.
public class TaskManagerManager {
	
	private static TaskManagerManager instance = new TaskManagerManager();
	private CyLogger logger = CyLogger.getLogger(this.getClass());
	private List<Task>taskList = new ArrayList<Task>();

	public static TaskManagerManager getInstance() {
		return instance;
	}


	private TaskManagerManager(){
		
	}
	
	
	public void setTasks(List<Task> taskList){
		this.taskList = taskList;
		logger.info("Setting tasks: " + taskList.size());

	}
	
	
	public void queueTask(Task myTask){
		logger.info("Queueing task: " + myTask.getTitle());

		taskList.add(myTask);
	}
	
	
	public void removeTask(Task myTask){
		logger.info("Removing task: " + myTask.getTitle());
		taskList.remove(myTask);
	}
	
	
	public void clearQueuedTasks(){
		taskList = new ArrayList<Task>();
		logger.info("taskList cleared");
	}
	
	
	// loops over all qued tasks and executes them. when execution fails, an error is thrown
	public void manageQueuedTasks(){
		for (Task task : taskList){
			//checks if something has been canceled. if something has been canceled, don't invoke the other tasks;
			if (!PeanutModel.getInstance().isExit()){
				logger.info("Executing task: " + task.getTitle());
				boolean success = invokeTask(task);
				if (!success){
					logger.warn("Task " + task.getTitle() + "failed");
					throw new Error(task.getTitle()  + " clould not be completed");
				}
			}
		}
		
		// clear the task list
		taskList = new ArrayList<Task>();
	}
	
	
	public boolean invokeTask(Task myTask){	
		JTaskConfig jTaskConfig = new JTaskConfig();
		jTaskConfig.setOwner(Cytoscape.getDesktop());
		jTaskConfig.displayCloseButton(true);
		jTaskConfig.displayCancelButton(true);
		jTaskConfig.displayStatus(true);
		jTaskConfig.setAutoDispose(true);
		return TaskManager.executeTask(myTask, jTaskConfig);
	}
	
	//function that returns round up percentage of a fraction --> for setting the progress bar
  	public Integer getPercentage(int a, int b){
  		int percentage = (int) Math.round((a*100.0)/b);
  		return percentage;			
  	}

}
