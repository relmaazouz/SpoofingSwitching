package test;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import v13.CancelOrder;
import v13.Day;
import v13.FilteredLogger;
import v13.LimitOrder;
import v13.MarketOrder;
import v13.MarketPlace;
import v13.MonothreadedSimulation;
import v13.Order;
import v13.OrderBook;
import v13.Random;
import v13.Simulation;
import v13.agents.ModerateAgent;

public class PhysicaASimulationECOMOD  {
	static long[][] FundamentalValue;
			
   	 public static void main(String args[]) throws IOException {
	    
		
		 	int i, j, seriesLength, nbSim;
		 	int openning = 1, continous=999, closing=0;
		 	
		 	FundValueReader data;
		 	data = new FundValueReader("fundamentalValue");
			nbSim = data.FundValue.size();
			seriesLength = data.FundValue.get(0).length;
			nbSim = 1;
			
			int nbFundamentalist=500;
			int nbHFT = 500;
			//int nbNeighbors =10;
			int nbNeighbors = (nbFundamentalist+nbHFT)/2;
	for (j=0; j<nbSim; j++) {	
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
	        fl.orders = false;
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
            try {
        		String str = "Wealth-" + j;
        		WriteToFileQuality = new WriteToFileBidAskSpread(str);
        		
        		str = "Switching-" + j;
        		WriteToFileSwitching = new WriteToFileBidAskSpread(str);
        	} catch (IOException e1) {
        		    		// TODO Auto-generated catch block
        		e1.printStackTrace();
        	}
            
            
            

	        for (i=0;i<nbFundamentalist; i++){
	        	isFundamentalist[i] = true;
	        	sim.addNewAgent(new PhysicaAECOMOD("Fundamentalist_"+i, (long)2000000, (long) 20000, 1, 
	        			data.FundValue.get(j), continous, WriteToFileQuality,  WriteToFileSwitching, mimicMatix, profitVector, isFundamentalist));
	       }      
	        
	        for (i=nbFundamentalist;i<nbFundamentalist+nbHFT; i++){
	        	sim.addNewAgent(new PhysicaAECOMOD("HFT_"+i, (long)2000000, (long) 20000, 1, 
	        			data.FundValue.get(j), continous, WriteToFileQuality, WriteToFileSwitching, mimicMatix, profitVector, isFundamentalist));
	       }
	        
	        sim.run(Day.createEuroNEXT(openning, continous, closing), nbDays);
	        WriteToFileSwitching.Close();
	        WriteToFileQuality.Close();
		//sim.market.printState();
		sim.market.close();
	    }
   	 } //for simNumber 
}
