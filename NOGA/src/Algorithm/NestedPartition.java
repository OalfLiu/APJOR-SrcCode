package Algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.prioRules.PRDecryptor;

public class NestedPartition {
 
 
	public static NP_Result NP_result;
	public static NP_Result NP_Opt;
	public static String solutionStr = ""; 
 
	public static int n;  
	public static int numWC=72; 
	public static int numHT=9;  
	public static double[] P; 
	public static int numSim=0;
	public static int numSeqRule=24;
	public static int numBatchFormRule=10;
	public static int sampleSize=5;
	public static long seed=9096;
	public static Random rand=new Random(seed);
	public static HashMap hashSolution=new HashMap(30000);
	public static SummaryStat[][] allStats;
	public static double[][] allRatios;
	public static double[][] simpleRatios;
	public static double[][] softmaxRatios;

	public static NP_Result[] initializeWithDefaultNP_ResultInstances(int length)
	{
		NP_Result[] array = new NP_Result[length];
		for (int i = 0; i < length; i++)
		{
			array[i] = new NP_Result(n);
		}
		return array;
	}

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
				res.solution[i]=rouletteSelection(softmaxRatios[i],rand.nextDouble());
				
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
				temp[i]=rouletteSelection(softmaxRatios[i],rand.nextDouble());
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
		
//		if(res.fix<=numHT)
//		{
//			sampleSize=(int)Math.round(1.8*res.fix) ;
//		}
//		else
//		{
//			sampleSize=(int)Math.round(1.2*res.fix) ;
//		}
		 
//		if(res.fix==1)sampleSize=21;
//		else sampleSize=1;
		
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
			
			/* 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷傻姆锟斤拷锟斤拷业锟斤拷锟斤拷薪锟�*/
			NP_Random(samples[s]);
			
			/* 锟斤拷锟斤拷锟斤拷薪锟斤拷应锟斤拷目锟疥函锟斤拷值*/
			samples[s].objvalue = Cal_Obj(samples[s].solution);
			objtemp[s]=samples[s].objvalue;
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
				 
				NP_Surround_Random(temp);
				temp.objvalue = Cal_Obj(temp.solution);
				if (surrOpt.objvalue > temp.objvalue || Math.abs(surrOpt.objvalue + 1) < 0.0000001)
				{
					surrOpt.objvalue = temp.objvalue;
					for (int j = 0; j < n; j++)
					{
						surrOpt.solution[j] = temp.solution[j];
					}
					surrOpt.fix = temp.fix;
				}
			}
		} 
		
		for (int i = 0; i < n; i++)
		{
			res.solution[i] = surrOpt.solution[i];
		}
		res.fix = surrOpt.fix;
		res.objvalue = surrOpt.objvalue;
 
	}
 
	public static void NP_Iter(NP_Result res)
	{
		int i;
		int j;
		int k;
		int h;

		NP_Result[] ResSet;

		double[] objtemp;

		/* 锟斤拷锟斤拷锟斤拷锟斤拷锟街达拷械锟揭伙拷锟斤拷锟斤拷锟斤拷锟斤拷锟侥壳帮拷丫锟斤拷潭锟絩es->fix锟斤拷锟接癸拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷n - res->fix锟斤拷锟斤拷锟接癸拷锟斤拷锟斤拷锟剿筹拷锟轿达拷潭锟斤拷锟�
		   锟斤拷锟皆匡拷锟皆分革拷锟斤拷锟絥 - res->fix锟斤拷锟斤拷锟接空间，锟斤拷锟斤拷锟斤拷锟津，癸拷锟斤拷n - res->fix + 1锟斤拷锟斤拷锟斤拷占锟�*/

		if (res.fix == 0)
		{ 
			ResSet = initializeWithDefaultNP_ResultInstances(numBatchFormRule); 
 
			h=ResSet.length;
			
			objtemp = new double[h];
			//锟斤拷始锟斤拷锟斤拷锟秸间定锟斤拷
			for (i = 0; i < h; i++)
			{
				ResSet[i].solution[0] = i;
				ResSet[i].fix = 1;
				sample(ResSet[i]);
				ResSet[i].objvalue = Cal_Obj(ResSet[i].solution);
				objtemp[i] = ResSet[i].objvalue;
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
				ResSet[i].objvalue = res.objvalue;
				bestOfEachSubRegion[i].objvalue = res.objvalue;
			}

			//锟斤拷most promising锟斤拷占浠拷锟斤拷涌占洳ample
			for (i = 0; i < h - 1; i++)
			{
				ResSet[i].solution[ResSet[i].fix] = i;
				ResSet[i].fix += 1;
				sample(ResSet[i]);
//				ResSet[i].objvalue = Cal_Obj(ResSet[i].solution); 
			}
			
//			int ocbaBudget=-1;
//			if(res.fix<numHT)
//			{
//				ocbaBudget=((int)Math.round(Math.pow(2.0, Math.log(n-res.fix))))*numBatchFormRule;
//			}
//			else
//			{
//				ocbaBudget=((int)Math.round(Math.pow(1.6, Math.log(n-res.fix))))*numSeqRule;
//			}
//			
//			//2019-02-12 ocba sample subpart
//			for (i = 0; i < ocbaBudget; i++)
//			{
//				double maxratio=-1;
//				int sampleIdx=-1;
//				for(int jj=0;jj<allRatios[res.fix].length;jj++)
//				{
//					if(allRatios[res.fix][jj]>maxratio)
//					{
//						maxratio=allRatios[res.fix][jj];
//						sampleIdx=jj;
//					}
//				} 
//				ResSet[sampleIdx].fix = res.fix+1;
//				bestOfEachSubRegion[sampleIdx].fix = res.fix+1;
//				sample(ResSet[sampleIdx]);
//				
//				if(bestOfEachSubRegion[sampleIdx].objvalue>ResSet[sampleIdx].objvalue)
//				{
//					for(int f = 0; f < n; f ++)
//					{
//						bestOfEachSubRegion[sampleIdx].solution[f] = ResSet[sampleIdx].solution[f];					
//					} 
//					bestOfEachSubRegion[sampleIdx].objvalue = ResSet[sampleIdx].objvalue;
//				} 
//			} 
			
			//锟斤拷锟斤拷锟斤拷锟斤拷锟絪ample
			surrounding_sample(ResSet[h-1]);
			
			objtemp = new double[h];

			for (i = 0; i < h; i++)
			{
				ResSet[i].fix = res.fix+1; //some subregion may has no sample
				
//				for(int f = 0; f < n; f ++)
//				{
//					ResSet[i].solution[f] = bestOfEachSubRegion[i].solution[f];					
//				}
//				ResSet[i].objvalue = bestOfEachSubRegion[i].objvalue;	
				
				objtemp[i] = ResSet[i].objvalue; 
			}

			Arrays.sort(objtemp); 
			 
			
			int bestRegionID=-1;
			
			for (i = 0; i < h; i++)
			{
				if (Math.abs(objtemp[0] - ResSet[i].objvalue) < 0.0000001)
				{ 
					if (NP_Opt.objvalue > ResSet[i].objvalue)
					{
						NP_Opt.objvalue = ResSet[i].objvalue;
						NP_Opt.fix = ResSet[i].fix;
						for (j = 0; j < n; j++)
						{
							NP_Opt.solution[j] = ResSet[i].solution[j];
						}
						
						for (j = 0; j < n; j++)
						{
							res.solution[j] = ResSet[i].solution[j];
						}
						res.fix = ResSet[i].fix;
						res.objvalue = ResSet[i].objvalue;
					} 
					
					res.fix = ResSet[i].fix;
					
					bestRegionID=i;
					
					break;
				}
			}

			//锟斤拷锟絤ost promising锟斤拷占锟轿拷锟斤拷锟斤拷锟斤拷锟斤拷
			if (bestRegionID == h - 1 && Math.abs(objtemp[0] - ResSet[h - 1].objvalue) < 0.000001)
			{
				System.out.print("Backtrack! ");
				//System.out.printf("%f ", ResSet[h - 1].objvalue);
				if (NP_Opt.objvalue > ResSet[h - 1].objvalue)
				{
					NP_Opt.objvalue = ResSet[h - 1].objvalue;
					NP_Opt.fix = ResSet[h - 1].fix;
					for (j = 0; j < n; j++)
					{
						NP_Opt.solution[j] = ResSet[i].solution[j];
					}
				}
				for (j = 0; j < n; j++)
				{
					res.solution[j] = ResSet[h - 1].solution[j];
				}
				res.objvalue = ResSet[h - 1].objvalue;
				//res.fix=1;
				if(ResSet[h - 1].fix==1) 
				{
					res.fix = ResSet[h - 1].fix;
				}
				else
				{
					res.fix = ResSet[h - 1].fix-1;
				}
				
			}
 
		}
	}

	
	public static void main(String[] args)
	{
		seed=Long.valueOf(args[0]);
		String fullname = "";

		fullname = "src\\Data\\";
		fullname += "12-1.dat";
		
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
					//LLX 2019-01-17 make a variance
//					stat.value(bf.preTestObj+Math.sqrt(8.0)*bf.preTestStd);
					stat.value(bf.preTestObj+1);
					stat.value(bf.preTestObj-1);
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
					//LLX 2019-01-17 make a variance
//					stat.value(pr.preTestObj+Math.sqrt(8.0)*pr.preTestStd);
					stat.value(pr.preTestObj+1);
					stat.value(pr.preTestObj-1);
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
		while (NP_result.fix < n)
		{			
			System.out.printf("Depth %d: ", NP_result.fix); 
			NP_Iter(NP_result); 
			System.out.print(NP_Opt.objvalue);
			System.out.printf(" Simu: %d", numSim);
			
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
	
	
	
	
	
	
	public static void NP_Random_Original(NP_Result res)
	{
		int[] seq;
		int i;
		int j;
		int k;
		int select;
		//Random rand=new Random(System.currentTimeMillis());
		seq = new int[n];

		for (i = 0; i < n; i++)
		{
			seq[i] = -1;
		}

		for (i = 0; i < res.fix; i++)
		{
			seq[res.solution[i]] = i;
		}

		for (i = 0; i < n - res.fix; i++)
		{
			select = rand.nextInt(Integer.MAX_VALUE) % (n - res.fix - i) + 1;

			k = 0;
			for (j = 0; j < n; j++)
			{
				if (seq[j] == -1)
				{
					k++;
				}

				if (k == select)
				{
					break;
				}
			}

			res.solution[res.fix + i] = j;
			seq[j] = res.fix + i;
		}

		seq = null;
	}
	
	public static void sample_Original(NP_Result res)
	{
		double obj;
		int i;
		int h; 
		
		int[] temp1;
		NP_Result res1=new NP_Result(n);
		NP_Result res2=new NP_Result(n);
		NP_Result res3=new NP_Result(n);
		NP_Result res4=new NP_Result(n);
		NP_Result res5=new NP_Result(n);
		double[] objtemp = new double[5];
		
		for(i = 0; i < n; i ++)
		{
			res1.solution[i] = res.solution[i];
			res2.solution[i] = res.solution[i];
			res3.solution[i] = res.solution[i];
			res4.solution[i] = res.solution[i];
			res5.solution[i] = res.solution[i];
		}
		
		res1.fix = res.fix;
		res2.fix = res.fix;
		res3.fix = res.fix;
		res4.fix = res.fix;
		res5.fix = res.fix;

		res1.objvalue = res.objvalue;
		res2.objvalue = res.objvalue;
		res3.objvalue = res.objvalue;
		res4.objvalue = res.objvalue;
		res5.objvalue = res.objvalue;

	/* 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷傻姆锟斤拷锟斤拷业锟斤拷锟斤拷锟斤拷锟叫斤拷*/

		NP_Random(res1);
		NP_Random(res2);
		NP_Random(res3);
		NP_Random(res4);
		NP_Random(res5);

	/* 锟斤拷锟斤拷锟斤拷锟街匡拷锟叫斤拷锟接︼拷锟侥匡拷旰拷锟街�*/

		res1.objvalue = Cal_Obj(res1.solution);
		res2.objvalue = Cal_Obj(res2.solution);
		res3.objvalue = Cal_Obj(res3.solution);
		res4.objvalue = Cal_Obj(res4.solution);
		res5.objvalue = Cal_Obj(res5.solution);

		objtemp[0] = res1.objvalue;
		objtemp[1] = res2.objvalue;
		objtemp[2] = res3.objvalue;
		objtemp[3] = res4.objvalue;
		objtemp[4] = res5.objvalue;

	/* 锟饺斤拷锟斤拷锟街匡拷锟叫斤拷目锟疥函锟斤拷值锟侥达拷小锟斤拷锟揭筹拷锟斤拷小锟斤拷*/
		Arrays.sort(objtemp);
		
		if(Math.abs(res1.objvalue - objtemp[0]) < 0.0000001)
		{
			for(i = 0; i < n; i ++)
			{
				res.solution[i] = res1.solution[i];
				res.fix = res1.fix;
				res.objvalue = res1.objvalue;
			}
		}
		else if(Math.abs(res2.objvalue - objtemp[0]) < 0.0000001)
		{
			for(i = 0; i < n; i ++)
			{
				res.solution[i] = res2.solution[i];
				res.fix = res2.fix;
				res.objvalue = res2.objvalue;
			}
		}
		else if(Math.abs(res3.objvalue - objtemp[0]) < 0.0000001)
		{
			for(i = 0; i < n; i ++)
			{
				res.solution[i] = res3.solution[i];
				res.fix = res3.fix;
				res.objvalue = res3.objvalue;
			}
		}
		else if(Math.abs(res4.objvalue - objtemp[0]) < 0.0000001)
		{
			for(i = 0; i < n; i ++)
			{
				res.solution[i] = res4.solution[i];
				res.fix = res4.fix;
				res.objvalue = res4.objvalue;
			}
		}
		else if(Math.abs(res5.objvalue - objtemp[0]) < 0.0000001)
		{
			for(i = 0; i < n; i ++)
			{
				res.solution[i] = res5.solution[i];
				res.fix = res5.fix;
				res.objvalue = res5.objvalue;
			}
		}

		temp1 = new int[n];

		for(i = 0; i < n; i ++)
		{
			temp1[i] = res.solution[i];
		}
		
	/* 
		锟斤拷锟斤拷潭锟斤拷募庸锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷为n锟斤拷锟斤拷n-1锟斤拷锟斤拷锟斤拷要锟斤拷锟叫局诧拷锟斤拷锟斤拷锟斤拷锟斤拷为锟斤拷时锟斤拷锟斤拷锟斤拷只锟斤拷一锟斤拷锟斤拷锟叫斤拷1锟斤拷
		锟斤拷锟斤拷执锟叫局诧拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷诰植锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷业锟斤拷锟斤拷玫慕猓拷锟皆斤拷锟斤拷懈锟斤拷隆锟�
	*/

		if(res.fix == n || res.fix == n - 1)
		{
			return;
		}
		else
		{	
			for(i = 0; i < 1*(n - res.fix); i ++)
			{
				Swap(temp1, res.fix);

				obj = Cal_Obj(temp1);

				if(obj < res.objvalue)
				{
					for(h = 0; h < n; h ++)
					{
						res.solution[h] = temp1[h];
					}

					res.objvalue = obj;
				}
			}
		}
		 
		return;
	}
	
	public static void surrounding_sample_Original(NP_Result res)
	{
		int i;
		int j;
		int k;
		int u;
		int v;
		int ctemp;

		NP_Result surrOpt =new NP_Result(n);
		NP_Result temp =new NP_Result(n);
		//Random rand=new Random(System.currentTimeMillis());

		if (surrOpt == null || temp == null)
		{
			System.out.print("Malloc fails!");
			return;
		}

		surrOpt.solution = new int[n];
		temp.solution = new int[n];

		surrOpt.objvalue = -1;
		temp.objvalue = -1;

		k = res.solution[0];
		if (res.fix == 1)
		{
			for (i = 0; i < n; i++)
			{
				if (i != k)
				{
					temp.solution[0] = i;
					temp.fix = 1;
					sample(temp);
					temp.objvalue = Cal_Obj(temp.solution);
					if (surrOpt.objvalue > temp.objvalue || Math.abs(surrOpt.objvalue + 1) < 0.0000001)
					{
						surrOpt.objvalue = temp.objvalue;
						for (j = 0; j < n; j++)
						{
							surrOpt.solution[j] = temp.solution[j];
						}
						surrOpt.fix = temp.fix;
					}
				}
			}
		}
		else
		{
			for (i = 0; i < 10 * res.fix; i++) 
			{
				u = rand.nextInt(Integer.MAX_VALUE) % (res.fix);
				v = rand.nextInt(Integer.MAX_VALUE) % n;
				if (u == v)
				{
					v = v + 1;
				}

				for (j = 0; j < n ; j++)
				{
					temp.solution[j] = res.solution[j];
					temp.fix = res.fix;
				}
				ctemp = temp.solution[v];
				temp.solution[v] = temp.solution[u];
				temp.solution[u] = ctemp;

				sample(temp);
				temp.objvalue = Cal_Obj(temp.solution);
				if (surrOpt.objvalue > temp.objvalue || Math.abs(surrOpt.objvalue + 1) < 0.0000001)
				{
					surrOpt.objvalue = temp.objvalue;
					for (j = 0; j < n; j++)
					{
						surrOpt.solution[j] = temp.solution[j];
					}
					surrOpt.fix = temp.fix;
				}
			}
		}

		res.fix = surrOpt.fix;
		for (i = 0; i < n; i++)
		{
			res.solution[i] = surrOpt.solution[i];
		}
		res.objvalue = surrOpt.objvalue;

		surrOpt.solution = null;

	}
	
	public static double Cal_Obj_Orginal(int[] solution)
	{
		int i;
		double obj;
		double[] c;

		c = new double[n];
 
		for (i = 0; i < n; i++)
		{
			if (i == 0)
			{
				c[i] = P[solution[i]];
			}
			else
			{
				c[i] = c[i - 1] + P[solution[i]];
			}
		}

		obj = 0;

		for (i = 0; i < n; i++)
		{
			obj = obj + c[i];
		}

		c = null;

		return obj;
	}
}
