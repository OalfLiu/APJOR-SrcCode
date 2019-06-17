package ec.app.JSP.Entities;
import java.util.*;

public class Operation {

	//  int jodId;
	private final int opid;
	private double prepareTime;	
	private double proTime;
	private final Job job;	
	private final Machine machine;

	public Operation(Job job, Machine machine, double processingTime, int opid) {
		this.machine = machine;
		this.job = job;
		this.processingTime = processingTime;
		this.opid = opid;

		job.addOperation(this);

		machine.addOperation(this);
	}

	public double getPrepareTime() {
		return prepareTime;
	}

	public void setPrepareTime(double prepareTime) {
		this.prepareTime = prepareTime;
	}

	public double getProTime() {
		return proTime;
	}

	public void setProTime(double proTime) {
		this.proTime = proTime;
	}

	public void clearSchedule()
	{
		completionTime = 0;
	}

	public int getOPId() {
		return opid;
	}

	public Job getJob() {
		return job;
	}

	private final double processingTime;

	private double completionTime;

	public double getCompletionTime() {
		return completionTime;
	}

	public double getProcessingTime() {
		return processingTime;
	}

	public Machine getMachine() {
		return machine;
	}

	public double getOPReadyTime(){

		if (opid == 0)
		{
			return job.getReleaseTime();			  
		}
		else
		{
			return job.getOperations().get(opid-1).getCompletionTime();

		}		 
	}

	public void setCompletionTime(int completionTime) {
		this.completionTime = completionTime;
	}

	public double getReadyTime()
	{
		if (this.opid == 0)
		{
			return job.getReleaseTime();			
		}
		else
		{
			return job.getOperations().get(this.opid-1).getCompletionTime();
		}
	}

	Operation getNextOperation()
	{
		if (this.opid + 1 < getJob().getOperations().size()) {
			return getJob().getOperations().get(opid + 1);
		}else{

			return null;
		}
	}

	/**
	 * add to schedule
	 */
	void AddToSchedule()
	{
		// compute completion time
		completionTime = Math.max(getMachine().getReadyTime(), getReadyTime()) + getProcessingTime();

		getMachine().addToSchedule(this);

		getJob().increaseSchedule();
	}
	
    public double getRemainingProcessingTimeIncludingThis() {


        return getJob().getRemaining(this.opid);
    }


    public int getNumberOfRemainOperation()
    {
        return getJob().getOperations().size() - this.opid;
    }

    double getWeightedProcessingTime()
    {
        return getJob().getWeight() / processingTime;
    }

    public void putToSchedule()
    {
        // compute completion time
        completionTime = Math.max(getMachine().getReadyTime(), getReadyTime()) + getProcessingTime();

        getMachine().addToSchedule(this);

        getJob().increaseSchedule();
    }

}
