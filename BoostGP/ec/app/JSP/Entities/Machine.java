package ec.app.JSP.Entities;

import java.util.*;

public class Machine {

	private int machID;
	
	private double scheduledTime = 0;
	private double totalTime = 0;
	/**
     * all operations which need to run this machine
     */
    private final Set<Operation> candidateOPs = new HashSet<>();
    /**
     * already scheduled operations
     */
    private List<Operation> scheduledOPs = new ArrayList<>();
    
    private List<Operation> queue;

    public List<Operation> getQueue() {
        return queue;
    }

    public Machine(int id){
    	
    	this.machID = id;
    	scheduledTime = 0;
    	totalTime = 0;
    }
    
	 
    //Clean current schedule
    public void clearSchedule()
    {
        scheduledTime = 0;
        scheduledOPs.clear();
    }
    
    //Add a new operation
    public void addOperation(Operation operation) {
    	candidateOPs.add(operation);
        totalTime += operation.getProcessingTime();
    }
    
    public void addToSchedule(Operation operation) {

    	scheduledOPs.add(operation);

        scheduledTime += operation.getProcessingTime();
    }
    
    /*
     * Get latest scheduled op's completion time as ready time.
     * If no scheduled operation, return 0
     */    
    public double getReadyTime()
    {
        if (scheduledOPs.isEmpty())
        {
            return 0;

        }else{

        	//Get latest scheduled op's completion time as ready time
            return scheduledOPs.get(scheduledOPs.size() - 1).getCompletionTime();
        }
    }
    
    //Get total processing time of scheduled operations
    public double getScheduledTime() {
        return scheduledTime;
    }
    
    //Get total processing time of remaining operations
    public double getRemainingTime()
    {
        return totalTime - scheduledTime;
    }
    
    public void outputSchedule()
    {

        System.out.println("machine " + this.machID);
        for (int i=0; i< scheduledOPs.size(); i++)
        {
            System.out.printf("(%d %d %f %f) ", scheduledOPs.get(i).getJob().getJobID(),
            		scheduledOPs.get(i).getOPId(), scheduledOPs.get(i).getProcessingTime(), scheduledOPs.get(i).getCompletionTime());
        }

        System.out.println("\n");
    }
}
