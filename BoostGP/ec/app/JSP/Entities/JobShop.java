package ec.app.JSP.Entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import ec.gp.GPNode;

public class JobShop  {

	public static final int MAKESPAN = 0;

	public static final int TARDINESS = 1;

	public static final int DUENUM = 2;

	public static int objective = MAKESPAN;

	int numMachines;
	int numJobs;

	List<Machine> machines = new ArrayList<>();
	List<Job> jobs = new ArrayList<>();

	public void  Initialize(int numM, List<List<Integer>> opM, List<List<Double>> opP, List<Double> weights, List<Double> dues
			) 
	{

		numMachines = numM;
		for (int i = 0; i < numM; i++) {
			machines.add(new Machine(i));

		}

		numJobs = opM.size();

		for (int i = 0; i < numJobs; i++) {

			jobs.add(new Job(i, weights.get(i), dues.get(i)));
		}

		for (int i = 0; i < numJobs; i++) {
			for (int j = 0; j < opM.get(i).size(); j++) {
				Operation operation = new Operation(jobs.get(i), machines.get(opM.get(i).get(j)),
						opP.get(i).get(j), j);

				          jobs.get(i).addOperation(operation);
			}

			jobs.get(i).computeRemainingTime();
		}
	}

	public void  Initialize(int numM, List<List<Integer>> opM, List<List<Double>> opP, List<Double> weights, List<Double> dues,
			List<Double> releases) {

		numMachines = numM;
		for (int i = 0; i < numM; i++) {
			machines.add(new Machine(i));

		}

		numJobs = opM.size();

		for (int i = 0; i < numJobs; i++) {

			jobs.add(new Job(i, weights.get(i), dues.get(i), releases.get(i)));
		}

		for (int i = 0; i < numJobs; i++) {
			for (int j = 0; j < opM.get(i).size(); j++) {
				Operation operation = new Operation(jobs.get(i), machines.get(opM.get(i).get(j)),
						opP.get(i).get(j), j);

				          jobs.get(i).addOperation(operation);
			}

			jobs.get(i).computeRemainingTime();
		}

	}

	/**
	 * clear schedule
	*/
	public void clearSchedule() {
		for (Machine machine : machines) {
			machine.clearSchedule();
		}

		for (Job job : jobs) {

			job.clearSchedule();
		}
	}
	
	List<Operation> getOperationWithMachine(Machine machine) {
        List<Operation> sameMachineOps = new ArrayList<>();

        for (int i = 0; i < jobs.size(); i++) {

            Operation operation = jobs.get(i).nextOp();

            if (operation != null && operation.getMachine() == machine) {
                sameMachineOps.add(operation);
            }
        }

        return sameMachineOps;
    }
	
	  public double getMinReady(List<Operation> sameMachineOps) {
	        double minReady = Double.MAX_VALUE;
	        for (Operation operation : sameMachineOps) {
	            if (operation.getReadyTime() < minReady) {
	                minReady = operation.getReadyTime();

	            }
	        }

	        return minReady;
	    }
	
	public void RunSchedule(){
		
		 while (true) {

	            // find the operation from ready OPs that can be finished earliest
	            List<Operation> candidates = new ArrayList<Operation>();
	            for (int i = 0; i < jobs.size(); i++) {
	                Operation availableOperation = jobs.get(i).nextOp();

	                if (availableOperation != null) {
	                  candidates.add(availableOperation);
	                }
	            }
	            
	            if(candidates.isEmpty())
	            	break;
	            
	            Operation selectOP = null;
	            
	            selectOP = SPT(candidates);
	            
	            if(selectOP != null)
	            {
	            	selectOP.putToSchedule();
	            }
	          
		 }
		 
		 System.out.println(getMakeSpan());
	
	}
	
	public double getMakeSpan() {
		return jobs.stream().mapToDouble(Job::getCompletionTime).max().getAsDouble();

	}
	
	public List<Operation> getReadyTimeBefore(List<Operation> sameMachineOps, double thre) {
        List<Operation> candidates = new ArrayList<>();

        for (Operation operation : sameMachineOps) {


            if (operation.getReadyTime() <= thre) {
                candidates.add(operation);
            }
        }

        return candidates;
    }
	
	  static Operation SPT(List<Operation> operations) {
	        return Collections.min(operations, new Comparator<Operation>() {
	            @Override
	            public int compare(Operation o1, Operation o2) {

	                if (o1.getProcessingTime() < o2.getProcessingTime()) {
	                    return -1;
	                }

	                return 1;
	            }
	        });
	    }
	
	public void Read()
	{
		Scanner in = null;
		try {
			System.out.println(new File("1.txt").getAbsolutePath());
			in = new Scanner(new File("1.txt"));


			String temp[] = in.nextLine().trim().split("\\s+");

			System.out.println(Arrays.toString(temp));

			int numJob = Integer.parseInt(temp[0]);

			int numM = Integer.parseInt(temp[1]);



			List<List<Integer>> opM = new ArrayList<>();

			List<List<Double>> opP = new ArrayList<>();

			List<Double> weights = new ArrayList<>();

			List<Double> dues = new ArrayList<>();


			for (int i = 0; i < numJob; i++) {

				weights.add(1.0);

				dues.add(10000.0);

				List<Integer> l = new ArrayList<>();

				List<Double> p = new ArrayList<>();

				String line = in.nextLine().trim();

				String t[] = line.split("\\s+");

				//                  System.out.println(Arrays.toString(t));

				for (int k = 0; k < t.length; k += 2) {

					l.add(Integer.parseInt(t[k]));

					p.add(Double.parseDouble(t[k + 1]));
				}


				opM.add(l);

				opP.add(p);


			}

			System.out.println(numM);
			System.out.println(opM);

			System.out.println(opP);

			this.Initialize(numM, opM, opP, weights, dues);


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
