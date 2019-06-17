package ec.app.JSP.Entities;

import java.util.ArrayList;
import java.util.List;

public class Job {

	//Unique Identifier
	private int jobID;	
	//Due Date
	private double duedate;	
	//Job Release Time
	private double releaseTime;
	//Priority
	private double weight;
	//Operations of this job
	private List<Operation> operations = new ArrayList<>();
	//Remaining time of each operation
	double remainTime[];
	
	public int CountofScheduledOPs = 0;
	
    public void setDueDate(double due) {
        this.duedate = due;
    }

    public double getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(double releaseTime) {
        this.releaseTime = releaseTime;
    }

    public double getWeight() {
        return weight;
    }

    public double getDueDate() {
        return duedate;
    }

    public int getJobID() {
        return jobID;
    }
    
    public List<Operation> getOperations() {
        return operations;
    }

    public void addOperation(Operation operation) {
        operations.add(operation);
    }
    
    public void computeRemainingTime() {

        remainTime = new double[operations.size()];

        for (int i = remainTime.length - 2; i >= 0; i--) {
            remainTime[i] = remainTime[i + 1] + operations.get(i).getProcessingTime();
        }
    }
    
    //Initialization
    public Job(int jobId, double weight, double due) {
    	
        this.jobID = jobId;
        this.weight = weight;
        this.duedate = due;
        this.releaseTime = 0;
        this.CountofScheduledOPs = 0;
    }

    public Job(int jobId, double weight, double due, double releaseTime) {
    	
        this.jobID = jobId;
        this.weight = weight;
        this.duedate = due;
        this.releaseTime = releaseTime;
        this.CountofScheduledOPs = 0;
    }
    
    //For scheduling
    public void clearSchedule() {
    	CountofScheduledOPs = 0;

        for (Operation op : operations) {

            op.clearSchedule();
        }
    }

    /**
     * another operation scheduled
     */
    public void increaseSchedule() {
    	CountofScheduledOPs++;
    }
    
    public Operation nextOp() {
        if (CountofScheduledOPs >= operations.size()) {
            return null;
        } else {

            return operations.get(CountofScheduledOPs);
        }
    }
    
    /**
    *
    * @param machine
    * @return whether there is remaining operation need to be run on machine 'machine'
    */
    public boolean hasRemainOperationRunOn(Machine machine) {
       for (int i = CountofScheduledOPs; i < operations.size(); i++) {
           if (operations.get(i).getMachine() == machine) {
               return true;
           }
       }

       return false;
   }

   /**
    *
    * @return the job's completion time
    */
   public double getCompletionTime() {
       return operations.get(operations.size() - 1).getCompletionTime();
   }
   
   /**
   *
   * @return tariness
   */
   public double getTardiness() {

      return Math.max(operations.get(operations.size() - 1).getCompletionTime() - getDueDate(), 0);
  }

  public double getStartTime()
  {
      return getOperations().get(0).getCompletionTime() - getOperations().get(0).getProcessingTime();
  }
  
  public double getRemaining(int from) {

      if (from < operations.size()) {
          return remainTime[from];
      } else {

          return 0;
      }
  }

  public double getTotalWork() {

      return remainTime[0];
  }

  public int getTotalOperations()
  {
      return operations.size();
  }

}
