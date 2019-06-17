package Algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.prioRules.PRDecryptor;

public class NPOCBA_GA {
 
 
	public static NP_Result NP_result;
	public static NP_Result NP_Opt;
	public static String solutionStr = ""; 
 
	public static int n;  
	public static int numWC=72; 
	public static int numHT=9;  
	public static double[] P; 
	public static int numSim=0;
	public static int numSeqRule=25;
	public static int numBatchFormRule=10;
	public static int sampleSize=5;
	public static long seed=9096;
	public static Random rand=new Random(seed);
	public static HashMap hashSolution=new HashMap(30000);
	public static SummaryStat[][] allStats;
	public static double[][] allRatios;
	public static double[][] simpleRatios;
	public static double[][] softmaxRatios;
	public static int totalBudget=25000;
	public static String logFileName="";

	public static NP_Result[] initializeWithDefaultNP_ResultInstances(int length)
	{
		NP_Result[] array = new NP_Result[length];
		for (int i = 0; i < length; i++)
		{
			array[i] = new NP_Result(n);
		}
		return array;
	}
	public static List<NP_Result> evaluationBatch = new ArrayList<NP_Result>();
	
	public static String buildSolutionStr(int[] solution)
	{ 
		//strcpy_s(solutionStr, sizeof(solutionStr), "Best So Far: ");
		solutionStr = " ";

		for (int i = 0;i < n;i++)
		{ 
			solutionStr += String.valueOf(solution[i]);
			solutionStr += ",";
		}
		solutionStr = solutionStr.substring(0,solutionStr.length()-1);

		//System.out.print(solutionStr);
		return solutionStr;
	}

	public static int readData(String filename)
	{
		int status;
		File fp = new File(filename);
		 
		try 
		{ 
			BufferedReader reader =new BufferedReader(new FileReader(fp));
			status = fp==null?0:1;
			
			if (status == 0)
			{
				System.out.print("Error in reading the data file.\r\n");
				return status;
			}
			
			n=Integer.parseInt(reader.readLine());
			if (n == 0) return 0;

			P = new double[n];

			P=parseDblList(reader.readLine());
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 1;
	}
	
	/**
	 * Converts a list of comma-separated double values (with dot as decimal
	 * separator) to a double-array. Example: parseDblList("1.23,4.56") -&gt;
	 * {1.23,4.56}
	 */
	public static double[] parseDblList(String s) {
		ArrayList<Double> ll = new ArrayList<Double>();
		StringTokenizer st = new StringTokenizer(s, ",");
		while (st.hasMoreElements()) {
			double v = Double.parseDouble(st.nextToken().trim());
			ll.add(v);
		}

		double[] res = new double[ll.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = ll.get(i);
		}
		return res;
	}
	
	public static int[] parseSolution(String s) {
		ArrayList<Integer> ll = new ArrayList<Integer>();
		StringTokenizer st = new StringTokenizer(s, " ");
		while (st.hasMoreElements()) {
			int v = Integer.parseInt(st.nextToken().trim());
			ll.add(v);
		}

		int[] res = new int[ll.size()-1];
		for (int i = 0; i < res.length; i++) {
			res[i] = ll.get(i+1);
		}
		return res;
	}
	
	
 
	private static int currbest(SummaryStat[] t_s_mean) {
		double best =Double.POSITIVE_INFINITY;
		int currBestIdx=-1;
		for (int i = 0; i < t_s_mean.length; i++) {
			if (t_s_mean[i].mean() < best) {
				best = t_s_mean[i].mean();
				currBestIdx=i;
			}
		}

		return currBestIdx;
	}
	
	public static double Cal_Obj(int[] solution)
	{ 
		double obj;
		String[] args=new String[4];
		 
		solutionStr ="";

		for (int i = 0;i < n;i++)
		{ 
			solutionStr += String.valueOf(solution[i]);
			solutionStr += ",";
		}
		solutionStr = solutionStr.substring(0,solutionStr.length()-1);
		 
		args[0]=String.valueOf(n-numHT);
		args[1]=String.valueOf(numHT);
		args[2]=solutionStr;
		args[3]=String.valueOf(seed);
		
		//double rtn= jasima.shopSim.SimSH.main(args);
		double rtn= 0;
		String [][] arglist =new String[1][];
		arglist[0]=args;
//		jasima.shopSim.ParallelSimSH.runParallelExps(arglist);
		
		
		hashSolution.put(solution, rtn);
		
		numSim++;
		
		//LLX 2019-01-17 for each simulation, update the objective value to stat of every decision point
		//Then, update the sample ratios of each point
		for(int i=0;i<n;i++)
		{
			allStats[i][solution[i]].value(rtn);
//			allRatios[i]=new OCBA().CalRatios(allStats[i], currbest(allStats[i]), 1);
//			simpleRatios[i]=new FitnessDepRatioAllocate().CalRatios(allStats[i]);
			softmaxRatios[i]= new Softmax().CalRatios(allStats[i]);
		}
		
		return rtn;
	}
	
	public static String buildSolutionForECJ(int[] solution)
	{
		String str ="";
		for(int j=0;j<solution.length;j++)
		{
			str+="i"+solution[j]+"|";
		}
		return str;
	}
	
	public static void evalAllSamples()
	{ 
		
		if(evaluationBatch.size()<1) return;
		
		String strBuilder="Number of Individuals: i"+evaluationBatch.size()+"|\r\n";
		 
		for (int i = 0;i < evaluationBatch.size();i++)
		{  
			strBuilder+="Individual Number: i"+i+"|\r\n";
			strBuilder+="Evaluated: F\r\n";
			strBuilder+="Fitness: d0|0.0|d0|0.0|i1|d0|1.0|\r\n";
			strBuilder+="i"+n+"|"+buildSolutionForECJ(evaluationBatch.get(i).solution)+"\r\n";
		}
		
		
		File writeName = new File("ParallelEvaluator\\subpopulation.txt"); // 相对路径，如果没有则要建立一个新的output.txt文件
		try {            
            writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                out.write(strBuilder); // \r\n即为换行 
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		
		//GA Local Search
		int fixedNodeNum=0;
		if(evaluationBatch.get(0).regionIndex!=-1 )fixedNodeNum=evaluationBatch.get(0).fix;
		String paramsBuilder ="pop.subpop.0.fix="+fixedNodeNum+"\r\n\r\n";
//		for(int i=0;i<fixedNodeNum;i++)
//		{
//			paramsBuilder+="pop.subpop.0.species.min-gene."+i+"="+evaluationBatch.get(0).solution[i]+"\r\n";
//			paramsBuilder+="pop.subpop.0.species.max-gene."+i+"="+evaluationBatch.get(0).solution[i]+"\r\n";
//		}
		File paramsFile = new File("ParallelEvaluator\\NPsubregion.params"); // 相对路径，如果没有则要建立一个新的output.txt文件
		try {            
			paramsFile.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(paramsFile);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                out.write(paramsBuilder); // \r\n即为换行 
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		
		//begin call ECJ
		String[] params=new String[3];
		params[0]="-Xmx1024m";
		params[1]="-file";
		params[2]="ParallelEvaluator\\CalExistingSuppop.params";
		
  	
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "ParallelEvaluator.jar","-Xmx1024m","-file","CalExistingSuppop.params");
		pb.directory(new File("ParallelEvaluator"));
		
		Process p =null;
		try {
			p = pb.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			p.waitFor();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//resolve results file
		int status;
		try 
		{ 
			BufferedReader reader =new BufferedReader(new FileReader("ParallelEvaluator\\log\\allRules.log"));
			status = writeName==null?0:1;
			
			if (status == 0)
			{
				System.out.print("Error in reading the data file.\r\n");
			}
			
			reader.readLine();reader.readLine();reader.readLine();
			String line;
			int iterator=0, indIdx=0;
			double obj=20000.0;
			while((line = reader.readLine())!=null)
			{
				iterator++;
				if(iterator==3) // get fitness
				{ 
					obj=Double.parseDouble((line.split("\\|"))[1]);
					if(obj==20000) obj+=rand.nextDouble();
					evaluationBatch.get(indIdx).objvalue=obj;
				}
				else if(iterator==4) // get soluiton
				{
					String str=line.replace("|", " ").replace("i", "");
					int[] solution =parseSolution(str);
					hashSolution.put(solution, obj);
					//LLX 2019-01-17 for each simulation, update the objective value to stat of every decision point
					//Then, update the sample ratios of each point
//					for(int i=0;i<n;i++)
//					{
//						allStats[i][solution[i]].value(obj);
//						
////						simpleRatios[i]=new FitnessDepRatioAllocate().CalRatios(allStats[i]);
////						softmaxRatios[i]= new Softmax().CalRatios(allStats[i]);
//					}
					
					iterator=0;
					indIdx++;
				}
			}
			
			reader.close();
			
			//record all feasible solutions in log file
			writeInLog(evaluationBatch);		
			
//			for(int i=0;i<n;i++)
//			{
//				allRatios[i]=new OCBA().CalRatios(allStats[i], currbest(allStats[i]), 1);
//			}
			
		} 
		catch (IOException e) 
		{ 
			e.printStackTrace();
		}
		
		numSim+=evaluationBatch.size()*3; 
	}

	public static void writeInLog(List<NP_Result> evaluationBatch) {
		String strBuilder="";
		for (int i = 0;i < evaluationBatch.size();i++)
		{  

			if(evaluationBatch.get(i).objvalue<20000) {
				strBuilder+="Fitness: "+evaluationBatch.get(i).objvalue+"\r\n";
				strBuilder+=buildSolutionStr(evaluationBatch.get(i).solution)+"\r\n\r\n";
			}
			
		}
		
		
		File writeName = new File(logFileName); // 相对路径，如果没有则要建立一个新的output.txt文件
		try {            

            try (FileWriter writer = new FileWriter(writeName,true); //增量写入
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                out.write(strBuilder); // \r\n即为换行 
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static int rouletteSelection(double[] ratios,double randnum)
	{
		double cumulateRatio=0;
		for(int i=0;i<ratios.length;i++)
		{
			cumulateRatio+=ratios[i];
			if(randnum<=cumulateRatio)
			{
				return i;
			}			
		}
		return 0;
	}
	
	public static int tournamentSelection(SummaryStat[] indviduals,int popSize)
	{		
		int ind1=rand.nextInt(Integer.MAX_VALUE) % popSize;
		int ind2=rand.nextInt(Integer.MAX_VALUE) % popSize;
		
		while(ind1==ind2)
		{
			ind2=rand.nextInt(Integer.MAX_VALUE) % popSize;
		}
		
		if(indviduals[ind1].mean()<indviduals[ind2].mean())
			return ind1;
		else
			return ind2; 
	}
	
	public static int tournamentDoubleSelection(double[] indviduals,int popSize)
	{		
		int ind1=rand.nextInt(Integer.MAX_VALUE) % popSize;
		int ind2=rand.nextInt(Integer.MAX_VALUE) % popSize;
		
		while(ind1==ind2)
		{
			ind2=rand.nextInt(Integer.MAX_VALUE) % popSize;
		}
		
		if(indviduals[ind1]>indviduals[ind2])
			return ind1;
		else
			return ind2; 
	}
	
	public static void NP_Random(NP_Result res)
	{ 
		boolean flag=false;
		
		while(!flag)
		{ 
			for (int i = res.fix; i < n; i++)
			{
				res.solution[i]=rouletteSelection(simpleRatios[i],rand.nextDouble());
				
//				if(i<numHT)
//				{  
////					res.solution[i]=rand.nextInt(Integer.MAX_VALUE) % numBatchFormRule;  
////					res.solution[i]=rouletteSelection(allRatios[i],rand.nextDouble());
//					res.solution[i]=tournamentDoubleSelection(allRatios[i],numBatchFormRule);
//				} 
//				else
//				{
////					res.solution[i]=rand.nextInt(Integer.MAX_VALUE) % numSeqRule;
////					res.solution[i]=rouletteSelection(allRatios[i],rand.nextDouble());
//					res.solution[i]=tournamentDoubleSelection(allRatios[i],numSeqRule);
//				}
			} 
			if(hashSolution.get(res.solution)==null) flag=true;
		}
		
	}
	
	public static void NP_Surround_Random(NP_Result res)
	{
		//Random rand=new Random(System.currentTimeMillis());
		boolean flag =false;
		
		while(!flag)
		{
			int[] temp = new int[n];
			
			for (int i = 0; i < n; i++)
			{
				temp[i]=rouletteSelection(simpleRatios[i],rand.nextDouble());
//				if(i<numHT)
//				{  
////					res.solution[i]=rand.nextInt(Integer.MAX_VALUE) % numBatchFormRule;  
////					res.solution[i]=rouletteSelection(allRatios[i],rand.nextDouble());
////					res.solution[i]=tournamentDoubleSelection(allRatios[i],numBatchFormRule);
//				} 
//				else
//				{
////					res.solution[i]=rand.nextInt(Integer.MAX_VALUE) % numSeqRule;
////					res.solution[i]=rouletteSelection(allRatios[i],rand.nextDouble());
////					res.solution[i]=tournamentDoubleSelection(allRatios[i],numSeqRule);
//				}				
			} 
			
			for(int i=0;i<=res.fix-1;i++)
			{
				if(temp[i]!=res.solution[i] && hashSolution.get(temp)==null) 
				{
					flag=true;
					for (int j = 0; j < n; j++)
					{
						res.solution[j]=temp[j];
					}					
					break;
				}					
			}
		}
		
	}
	
	public static int[] NP_Single_Random(NP_Result res, boolean isSurroundingRegion)
	{
		int[] sol=new int[n];
		
		boolean flag=false; 
		while(!flag)
		{  
			int startPoint=isSurroundingRegion?0:res.fix;
			for (int i=0; i < n; i++)
			{
				if(i>=startPoint) 
					sol[i]=rouletteSelection(simpleRatios[i],rand.nextDouble());
				else 
					sol[i]=res.solution[i];
			} 
			
			if(!isSurroundingRegion && hashSolution.get(sol)==null) 
				flag=true;
			else if(isSurroundingRegion)
			{
				for(int i=0;i<=res.fix-1;i++)
				{
					if(sol[i]!=res.solution[i] && hashSolution.get(sol)==null) 
						flag=true;		
				}
			}			
		}
		return sol;
	} 
	
	public static void Swap(int[] solution, int fix)
	{
		int i;
		int j;
		int temp;

		//Random rand=new Random(System.currentTimeMillis());

		i = rand.nextInt(Integer.MAX_VALUE) % (n - fix);
		j = rand.nextInt(Integer.MAX_VALUE) % (n - fix);

		temp = solution[fix + i];
		solution[fix + i] = solution[fix + j];
		solution[fix + j] = temp;
	}
 
	public static void sample(NP_Result res)
	{
		if(res.fix<=numHT)
		{
			sampleSize=(int)Math.round(Math.pow(2.0, Math.log(n-res.fix))) ;
		}
		else
		{
			sampleSize=(int)Math.round(Math.pow(1.6, Math.log(n-res.fix))) ;
		}
		
//		if(res.fix==1)sampleSize=21;
//		else sampleSize=1;
		sampleSize=15;
		
		NP_Result[] samples=initializeWithDefaultNP_ResultInstances(sampleSize); 
		double[] objtemp = new double[sampleSize];
		
		for(int s=0;s<sampleSize;s++)
		{
			for(int i = 0; i < n; i ++)
			{
				samples[s].solution[i] = res.solution[i]; 
			}
			samples[s].fix = res.fix;
			samples[s].objvalue = res.objvalue;
			samples[s].regionIndex=res.regionIndex;
			/* 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷傻姆锟斤拷锟斤拷业锟斤拷锟斤拷薪锟�*/
			NP_Random(samples[s]);
			
			evaluationBatch.add(samples[s]);
			/* 锟斤拷锟斤拷锟斤拷薪锟斤拷应锟斤拷目锟疥函锟斤拷值*/
//			samples[s].objvalue = Cal_Obj(samples[s].solution);
//			objtemp[s]=samples[s].objvalue;
		} 
		
		/* 锟饺斤拷锟斤拷锟叫匡拷锟叫斤拷目锟疥函锟斤拷值锟侥达拷小锟斤拷锟揭筹拷锟斤拷小锟斤拷*/
		Arrays.sort(objtemp);
		
		for(int s=0;s<sampleSize;s++)
		{
			if(Math.abs(samples[s].objvalue - objtemp[0]) < 0.0000001)
			{
				for(int i = 0; i < n; i ++)
				{
					res.solution[i] = samples[s].solution[i];					
				}
				res.fix = samples[s].fix;
				res.objvalue = samples[s].objvalue;
				
				break;
			}
		} 
		  
		return;
	}
	 
	public static void surrounding_sample(NP_Result res)
	{   
		//sampleSize=(int)Math.round(Math.pow(4.0, Math.log(res.fix))) ;
		sampleSize=60;
		
		NP_Result surrOpt =new NP_Result(n);
		NP_Result temp =new NP_Result(n); 
		
		int k = res.solution[0];
//		if (res.fix == 1)
		if (false)
		{
			for (int i = 0; i < numBatchFormRule ; i++)
			{
				if (i != k)
				{
					temp.solution[0] = i;
					temp.fix = 1;
					sample(temp);
					temp.objvalue = Cal_Obj(temp.solution);
					if (surrOpt.objvalue > temp.objvalue || Math.abs(surrOpt.objvalue + 1) < 0.0000001)
					{						
						for (int j = 0; j < n; j++)
						{
							surrOpt.solution[j] = temp.solution[j];
						}
						surrOpt.fix = temp.fix;
						surrOpt.objvalue = temp.objvalue;
					}
				}
			}
		}
		else
		{
			 
			for (int i = 0; i < sampleSize; i++) 
			{
				for (int j = 0; j < n ; j++)
				{
					temp.solution[j] = res.solution[j];					
				}
				temp.fix = res.fix;
//				temp.fix = 0;
				temp.regionIndex=res.regionIndex;
				NP_Surround_Random(temp);
				evaluationBatch.add(temp);
//				temp.objvalue = Cal_Obj(temp.solution);
			}
		} 
 
	}
 
	public static void NP_Iter(NP_Result res)
	{
		int i;
		int j;
		int k;
		int h;

		NP_Result[] ResSet;

		double[] objtemp;
 
		if (res.fix == 0)
		{ 
			ResSet = initializeWithDefaultNP_ResultInstances(numBatchFormRule); 
 
			h=ResSet.length;
			
			objtemp = new double[h];

			for (i = 0; i < h; i++)
			{
				ResSet[i].solution[0] = i;
				ResSet[i].fix = 1;
				ResSet[i].regionIndex=i;
				ResSet[i].objvalue = Double.MAX_VALUE;
				
				evaluationBatch.clear();
				sample(ResSet[i]);

				evalAllSamples();
				
				for(int b=0;b<evaluationBatch.size();b++)
				{
					NP_Result nr=evaluationBatch.get(b);
					if(nr.objvalue<ResSet[nr.regionIndex].objvalue) 
					{
						ResSet[nr.regionIndex].solution=nr.solution;
						ResSet[nr.regionIndex].objvalue=nr.objvalue;
						objtemp[nr.regionIndex] = ResSet[nr.regionIndex].objvalue;
					}
				}
			}

			
			
			
			k = 0; 
			
			Arrays.sort(objtemp); 
			
			if (NP_Opt.objvalue > objtemp[0] || Math.abs(NP_Opt.objvalue + 1) < 0.0000001)
			{
				NP_Opt.objvalue = objtemp[0];

				for (i = 0; i < h; i++)
				{
					if (Math.abs(ResSet[i].objvalue - objtemp[0]) < 0.0000001)
					{
						for (j = 0; j < n; j++)
						{
							NP_Opt.solution[j] = ResSet[i].solution[j];
						}
						k = i;
						break;
					}
				}
			}
			else
			{
				for (i = 0; i < h; i++)
				{
					if (Math.abs(ResSet[i].objvalue - objtemp[0]) < 0.0000001)
					{
						k = i;
						break;
					}
				}
			}
 
			for (i = 0; i < n; i++)
			{
				res.solution[i] = ResSet[k].solution[i];
			}

			res.fix = ResSet[k].fix;
			res.objvalue = ResSet[k].objvalue;
 
		}
 
		else
		{
			if(res.fix>=10 && res.fix<=18 && res.solution[res.fix-9-1]==9) {
				//if batch form rule is IJAMT, then skip
				res.fix++;
				return;
			}
			
			//h = n - res.fix + 1;
			NP_Result[] bestOfEachSubRegion;
			if(res.fix<numHT)
			{
				ResSet = initializeWithDefaultNP_ResultInstances(numBatchFormRule+1); 
				bestOfEachSubRegion = initializeWithDefaultNP_ResultInstances(numBatchFormRule+1); 
			} 
			else 
			{
				ResSet = initializeWithDefaultNP_ResultInstances(numSeqRule+1); 
				bestOfEachSubRegion = initializeWithDefaultNP_ResultInstances(numSeqRule+1); 
			}
				
			
			h=ResSet.length;
			
			for (i = 0; i < h; i++)
			{
				for (j = 0; j < n; j++)
				{
					ResSet[i].solution[j] = res.solution[j];
					bestOfEachSubRegion[i].solution[j] = res.solution[j];
				}

				ResSet[i].fix = res.fix;
				ResSet[i].objvalue = Double.MAX_VALUE;
				bestOfEachSubRegion[i].objvalue = res.objvalue;
			}


			for (i = 0; i < h - 1; i++)
			{
				ResSet[i].solution[ResSet[i].fix] = i;
				ResSet[i].fix += 1;
				ResSet[i].regionIndex=i; 
			} 
			
			
			int bestRegionID = OCBAEvaluate(ResSet);
			
			objtemp = new double[h];
			for(int b=0;b<h;b++)
			{ 
				objtemp[b]=ResSet[b].objvalue;
			}		
			Arrays.sort(objtemp);
  
			for (i = 0; i < h; i++)
			{
				if (Math.abs(objtemp[0] - ResSet[i].objvalue) < 0.0000001)
				{ 
					if (NP_Opt.objvalue > ResSet[i].objvalue)
					{
						NP_Opt.objvalue = ResSet[i].objvalue;
						NP_Opt.fix = ResSet[i].fix;
						NP_Opt.solution = ResSet[i].solution;
						
						res.solution = ResSet[i].solution;
						res.objvalue = ResSet[i].objvalue;
					} 
//					res.solution = ResSet[i].solution;
//					res.objvalue = res.objvalue;
					
					res.fix = ResSet[i].fix; 
					
					bestRegionID=i;
					
					break;
				}
			}

			// Backrack
			if (bestRegionID == h - 1 && Math.abs(objtemp[0] - ResSet[h - 1].objvalue) < 0.000001)
			{
				System.out.print("Backtrack! ");
				//System.out.printf("%f ", ResSet[h - 1].objvalue);
				if (NP_Opt.objvalue > ResSet[h - 1].objvalue)
				{
					NP_Opt.objvalue = ResSet[h - 1].objvalue;
					NP_Opt.fix = ResSet[h - 1].fix;
					NP_Opt.solution = ResSet[i].solution;
					res.solution = ResSet[i].solution;
					res.objvalue = res.objvalue;
				}
//				res.solution = ResSet[i].solution;
//				res.objvalue = res.objvalue;
				
				res.fix--;
				
			}
 
		}
	}

	public static int OCBAEvaluate(NP_Result[] ResSet)
	{
		evaluationBatch.clear();
		
		int h=ResSet.length, subregionNum=0;
		NP_Result res=ResSet[0];
		int ocbaBudget=-1;
		if(res.fix<=numHT)
		{
//			ocbaBudget=((int)Math.round(Math.pow(2.0, Math.log(n-res.fix))))*(numBatchFormRule);
			ocbaBudget=10*(numBatchFormRule);
			subregionNum=numBatchFormRule;
		}
		else
		{
//			ocbaBudget=((int)Math.round(Math.pow(1.6, Math.log(n-res.fix))))*numSeqRule;
			ocbaBudget=10*numSeqRule;
			subregionNum=numSeqRule;
		}
		
		SummaryStat[] stats = new SummaryStat[h];
		SummaryStat[] statsMin = new SummaryStat[h];
		for(int i=0;i<h;i++)
		{
			stats[i]=new SummaryStat(String.valueOf(i));
			statsMin[i]=new SummaryStat(String.valueOf(i));
		}
		
		for(int j=0;j<h;j++)
		{
			evaluationBatch.clear();
			for(int i=0;i<5;i++)
			{
				NP_Result newSample= new NP_Result(n);
				if(j==h-1) {
					// surrounding region
					newSample.solution=NP_Single_Random(ResSet[j],true);
					newSample.fix=0;
				}
				else {
					newSample.solution=NP_Single_Random(ResSet[j],false);
					newSample.fix=ResSet[j].fix;
				}
				
				newSample.regionIndex=j;
				evaluationBatch.add(newSample);
			}
			
			evalAllSamples();

				 
			for(int b=0;b<evaluationBatch.size();b++)
			{
				NP_Result nr=evaluationBatch.get(b);
				stats[nr.regionIndex].value(nr.objvalue);
				if(nr.objvalue==-1) {
					System.out.println("Who!!!!!");
				}
				statsMin[nr.regionIndex].value(nr.objvalue);
				if(nr.objvalue<ResSet[nr.regionIndex].objvalue) 
				{
					ResSet[nr.regionIndex].solution=nr.solution;
					ResSet[nr.regionIndex].objvalue=nr.objvalue; 
				}
			}
			
		} 
		

		for(int i=0;i<ocbaBudget/subregionNum;i++)
		{		
			
			int curBest=0;
			double min=Double.MAX_VALUE;
			for(int j=0;j<stats.length;j++)
			{
				if(stats[j].mean()<min)
				{
					curBest=j;
					min=stats[j].mean();
				}
			}
			int[] addtionSampleNum = OCBA.CalAllocation(stats,curBest,subregionNum);
			
			for(int j=0;j<addtionSampleNum.length;j++)
			{
				evaluationBatch.clear();
				for(int k=0;k<addtionSampleNum[j];k++)
				{
					NP_Result newSample= new NP_Result(n);
					if(j==h-1) // surrounding region
						newSample.solution=NP_Single_Random(ResSet[j],true);
					else
						newSample.solution=NP_Single_Random(ResSet[j],false);
					newSample.fix=ResSet[j].fix;
					newSample.regionIndex=j;
					evaluationBatch.add(newSample);
				}
				
				evalAllSamples();
				Double[] Xi=new Double[h];
				for(int x=0;x<h;x++)
				{
					Xi[x]=Double.MAX_VALUE;
				}
				
				for(int b=0;b<evaluationBatch.size();b++)
				{
					NP_Result nr=evaluationBatch.get(b);
					stats[nr.regionIndex].value(nr.objvalue);	
					if(nr.objvalue==-1) {
						System.out.println("Who!!!!!");
					}
					if(nr.objvalue<ResSet[nr.regionIndex].objvalue) 
					{
						ResSet[nr.regionIndex].solution=nr.solution;
						ResSet[nr.regionIndex].objvalue=nr.objvalue; 
					}
					
					if(nr.objvalue<Xi[nr.regionIndex]) Xi[nr.regionIndex]=nr.objvalue;
				}
				
				for(int x=0;x<h;x++)
				{ 
					if(addtionSampleNum[x]>0)
						statsMin[x].value(Xi[x]);
				}
			}		
			
		} 
		
		//output
		int curBest=0;
		double min=Double.MAX_VALUE;
		for(int j=0;j<statsMin.length;j++)
		{
			if(stats[j].min()<min)
			{
				curBest=j;
				min=stats[j].min();
			}
		}
 
		return curBest;
	}
	
	public static void main(String[] args)
	{
		seed=Long.valueOf(args[0]);
		logFileName = "ParallelEvaluator\\log\\allFesible-";

		logFileName += System.currentTimeMillis()+".txt";
		
		//if(readData(fullname)==0) return;
		 
		n=numHT+numWC;
		
		//LLX 2019-01-17 Construct initial stats matrix for OCBA
		PRDecryptor prd =new PRDecryptor();
		allStats=new SummaryStat[n][];
		allRatios=new double[n][];
		simpleRatios=new double[n][];
		softmaxRatios=new double[n][];
 
		for(int i=0;i<n;i++)
		{
			
			if(i<numHT)
			{
				SummaryStat[] ruleStats=new SummaryStat[numBatchFormRule]; 
				double[] specRatios=new double[numBatchFormRule];
				for(int j =0;j<numBatchFormRule;j++)
				{  
					BatchForming bf=prd.decryptBatchFormPR(j);
					SummaryStat stat=new SummaryStat(i+"-"+bf.getName());
					stat.value(bf.preTestObj);
//					//LLX 2019-01-17 make a variance
////					stat.value(bf.preTestObj+Math.sqrt(8.0)*bf.preTestStd);
//					stat.value(bf.preTestObj+1);
//					stat.value(bf.preTestObj-1);
					ruleStats[j]=stat;  
				}
				allStats[i]=ruleStats;
				allRatios[i]=new OCBA().CalRatios(ruleStats, 0, 1);
				simpleRatios[i]=new FitnessDepRatioAllocate().CalRatios(ruleStats);
				softmaxRatios[i]= new Softmax().CalRatios(ruleStats);
			}
			else
			{
				SummaryStat[] ruleStats=new SummaryStat[numSeqRule]; 
				double[] specRatios=new double[numSeqRule];
				for(int j =0;j<numSeqRule;j++)
				{  
					PR pr=prd.decryptSeqPR(j);
					SummaryStat stat=new SummaryStat(i+"-"+pr.getName());
					stat.value(pr.preTestObj);
//					//LLX 2019-01-17 make a variance
////					stat.value(pr.preTestObj+Math.sqrt(8.0)*pr.preTestStd);
//					stat.value(pr.preTestObj+1);
//					stat.value(pr.preTestObj-1);
					ruleStats[j]=stat;  
				}
				allStats[i]=ruleStats;
				allRatios[i]=new OCBA().CalRatios(ruleStats, 0, 1);
				simpleRatios[i]=new FitnessDepRatioAllocate().CalRatios(ruleStats);
				softmaxRatios[i]= new Softmax().CalRatios(ruleStats);
			} 
		}
		
		
		 
 
		NP_result=new NP_Result(n);
		 
		NP_Opt=new NP_Result(n);
		
//		for(int i=0;i<n;i++)
//		{
//			NP_Opt.solution[i]=0;
//		}
//		
//		Cal_Obj(NP_Opt.solution);

		long start = System.currentTimeMillis();
		long laststamp = start;
		String prevOptStr="";
		while (NP_result.fix <= n && totalBudget>0)
		{	
			// 2019-03-03
			evaluationBatch.clear();
			
			System.out.printf("Depth %d: ", NP_result.fix); 
			NP_Iter(NP_result); 
			System.out.print(NP_Opt.objvalue);
			System.out.printf(" Simu: %d", numSim);
			System.out.printf(" totalSimu: %d", 25000-totalBudget);
			
			String latestOptStr=buildSolutionStr(NP_Opt.solution);
			if(prevOptStr.isEmpty()||!prevOptStr.equals(latestOptStr))
			{
				System.out.print(" Opt:"+latestOptStr+'\n');
				prevOptStr=latestOptStr;
			}
			else
				System.out.print("\n");
			
			long pause = System.currentTimeMillis();
			
			System.out.print("Iter Time Cost:"+(long)((pause - laststamp)/1000.0/60.0)+"mins\t");
			
			laststamp=pause;
			System.out.print("So Far Time Cost:"+(long)((pause - start)/1000.0/60.0)+"mins\n");
			
//			printAllRatios();
			totalBudget-=numSim;
			numSim=0;
		}

		long end = System.currentTimeMillis();

		NP_Opt.time = (long)((end - start)/1000.0/60.0);
 

		System.out.print("The optimal solution of NP is:"+buildSolutionStr(NP_Opt.solution)+"\n"); 
		System.out.printf("The objective function value of NP is: %f\n", NP_Opt.objvalue);
		System.out.printf("The computational time of NP is: %d mins\n\n", NP_Opt.time);

		//writeRes("result.txt", NP_Opt);
  
	}
	
	public static void printAllRatios()
	{
		String str="\n";
		for(int i=0;i<allRatios.length;i++)
		{
			for(int j=0;j<allRatios[i].length;j++)
			{
				str+=allRatios[i][j]+"\t";
			}
			str+="\n";
		}
		System.out.print(str);
	}
	 
}
