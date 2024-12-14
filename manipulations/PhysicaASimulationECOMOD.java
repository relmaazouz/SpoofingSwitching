package manipulations;

import java.io.*;
import v13.Day;
import v13.FilteredLogger;
import v13.MarketPlace;
import v13.MonothreadedSimulation;
import v13.Random;
import v13.Simulation;

public class PhysicaASimulationECOMOD  {
	static long[][] FundamentalValue;
	
	private static double generateTriangularValue(double minValue, double maxValue, double centerValue) {
		 double u = Random.nextDouble();
       double f = (centerValue - minValue) / (maxValue - minValue);

       if (u <= f) {
           return minValue + Math.sqrt(u * (maxValue - minValue) * (centerValue - minValue));
       } else {
           return maxValue - Math.sqrt((1 - u) * (maxValue - minValue) * (maxValue - centerValue));
       }
   }
	
	
	
   	 public static void main(String args[]) throws IOException {
	    
		
		 	int i, j, seriesLength;
		 	int openning = 1, continous=3060, closing=0; //1 tick 10 seconds
			int nbFundamentalist=1000;
			int nbHFT = 100;
			int nbSim;
		 	
		 	FundValueReader data;
		 	data = new FundValueReader("fundamentalValue");
			nbSim = data.FundValue.size();
			seriesLength = data.FundValue.get(0).length;
			nbSim = 100;
			
			double[] beta_trend_Vect;
			double[] alpha_Fundamental_Vector = new double[nbFundamentalist + nbHFT];
			double[] beta_Trend_Vector = new double[nbFundamentalist + nbHFT];
			
			for(i=0;i<nbFundamentalist; i++) {
				//alpha_Fundamental_Vector[i] = generateTriangularValue(0.25, 0.75, 0.5);
				//alpha_Fundamental_Vector[i] =  Random.nextDouble();
				//beta_Trend_Vector[i] = Random.nextDouble();
				alpha_Fundamental_Vector[i] =  Random.nextGaussian(0.5, 1);
				beta_Trend_Vector[i] = Random.nextGaussian(0.5, 0.05);
			}
			

			

			//int nbNeighbors =10;
			int nbNeighbors = (nbFundamentalist+nbHFT)/2;
for (j=0; j<nbSim; j++) {	
		long[] fundamentalValue = new long[openning+continous];
		fundamentalValue[0]=4400;
			for(i=1; i<(openning+continous);i++) 
				fundamentalValue[i]=fundamentalValue[i-1] + Random.nextInt(20) - Random.nextInt(20);
			
		int[][] mimicMatix = new int [nbFundamentalist+nbHFT][nbFundamentalist+nbHFT];
		double[] profitVector = new double[nbFundamentalist+nbHFT];
		boolean[] isFundamentalist = new boolean[nbFundamentalist+nbHFT];
		//for (int k=0; k<(nbFundamentalist+nbHFT); k++)
		//	wealthVector = (long)2000000, (long) 20000, 100
		for(i=0; i<(nbFundamentalist+nbHFT); i++) {
			for(int k=0; k<nbNeighbors/2; k++) {
				int inxNB = Random.nextInt(nbFundamentalist);
				//mimicMatix[i][inxNB] = mimicMatix[inxNB][i]=1;
				mimicMatix[i][inxNB] =1;
			}
			
			for(int k=0; k<nbNeighbors/2; k++) {
				int inxNB = nbFundamentalist + Random.nextInt(nbHFT);
				//mimicMatix[i][inxNB] = mimicMatix[inxNB][i]=1;
				mimicMatix[i][inxNB] =1;
			}
			
		}
	//for (j=0; j<1; j++) {
		 	Simulation sim = new MonothreadedSimulation();
	      	sim.market.setFixingPeriod (MarketPlace.CONTINUOUS); // ou FIX
	        sim.market.logType=MarketPlace.LONG; // ou SHORT
	        FilteredLogger fl = new FilteredLogger("Something-" +j);
	        fl.orders = true;
	        fl.prices = true;
	        fl.agents = false;
	        fl.infos = false;
	        fl.commands = false; 
	        sim.setLogger(fl);        
	        int nbOrderBooks = 1;
	        int nbDays=1;

            sim.addNewOrderBook("ob");
            
            
            WriteToFileBidAskSpread WriteToFileQuality=null;
            WriteToFileBidAskSpread WriteToFileSwitching=null;
    	    WriteToFileBidAskSpread WriteToFileHFTActivity=null;
            try {
        		String str = "Wealth-" + j;
        		WriteToFileQuality = new WriteToFileBidAskSpread(str);
        		
        		str = "Switching-" + j;
        		WriteToFileSwitching = new WriteToFileBidAskSpread(str);
        		
        		str = "OrderBook-"  + j;
        		WriteToFileQuality = new WriteToFileBidAskSpread(str);
        	} catch (IOException e1) {
        		    		// TODO Auto-generated catch block
        		e1.printStackTrace();
        	}
            
            
            

	        for (i=0;i<nbFundamentalist; i++){
	        	isFundamentalist[i] = true;
	        	//sim.addNewAgent(new PhysicaAECOMOD("Fundamentalist_"+i, (long)440000, (long) 4400, 1000, 
	        	//		data.FundValue.get(j), continous, WriteToFileQuality,  WriteToFileSwitching, mimicMatix, profitVector, isFundamentalist));
	        	sim.addNewAgent(new PhysicaAECOMOD("Fundamentalist_"+i, (long)440000, (long) 4400, 100, 
	        			fundamentalValue, continous, WriteToFileQuality,  WriteToFileSwitching, mimicMatix, 
	        			profitVector, isFundamentalist, alpha_Fundamental_Vector, beta_Trend_Vector));
	       }      
	        
	        for (i=nbFundamentalist;i<nbFundamentalist+nbHFT; i++){
	        	//sim.addNewAgent(new PhysicaAECOMOD("Pressure_"+i, (long)440000, (long) 4400, 1000, 
	        	//		data.FundValue.get(j), continous, WriteToFileQuality, WriteToFileSwitching, mimicMatix, profitVector, isFundamentalist));
	        	sim.addNewAgent(new PhysicaAECOMOD("Pressure_"+i, (long)440000, (long) 4400, 100, 
	        			fundamentalValue, continous, WriteToFileQuality, WriteToFileSwitching, mimicMatix, 
	        			profitVector, isFundamentalist, alpha_Fundamental_Vector, beta_Trend_Vector));
	       }
	        
	    	/*Spoofer block*/	
			for (i=0;i<1; i++){
			        	try {
							sim.addNewAgent(new Spoofer("Spoofer_"+i,10000000 + Random.nextInt(10000000),
									continous, WriteToFileQuality, WriteToFileHFTActivity));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
	        
	        
	        sim.run(Day.createEuroNEXT(openning, continous, closing), nbDays);
	        WriteToFileSwitching.Close();
	        WriteToFileQuality.Close();
		//sim.market.printState();
		sim.market.close();
	    }
   	 } //for simNumber 
}
