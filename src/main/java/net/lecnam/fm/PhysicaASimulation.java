package net.lecnam.fm;

import java.io.*;

import fr.cristal.smac.atom.Day;
import fr.cristal.smac.atom.FilteredLogger;
import fr.cristal.smac.atom.MarketPlace;
import fr.cristal.smac.atom.MonothreadedSimulation;
import fr.cristal.smac.atom.Simulation;

public class PhysicaASimulation  {
    static long[][] FundamentalValue;

    public static void main(String args[]) throws IOException {


        int i, j, seriesLength, nbSim;
        int openning = 1, continous=999, closing=0;

        FundValueReader data;
        data = new FundValueReader("fundamentalValue");
        nbSim = data.FundValue.size();
        seriesLength = data.FundValue.get(0).length;
        nbSim = 1;
        for (j=0; j<nbSim; j++) {
            //for (j=0; j<1; j++) {
            Simulation sim = new MonothreadedSimulation();
            sim.market.setFixingPeriod (MarketPlace.CONTINUOUS); // ou FIX
            sim.market.logType=MarketPlace.LONG; // ou SHORT
            FilteredLogger fl = new FilteredLogger("Something-" +j);
            fl.orders = true;
            fl.prices = true;
            fl.agents = true;
            fl.infos = true;
            fl.commands = false;
            sim.setLogger(fl);
            int nbOrderBooks = 1;
            int nbDays=1;

            sim.addNewOrderBook("ob");


            WriteToFileBidAskSpread WriteToFileQuality=null;
            try {
                String str = "Wealth-" + j;
                WriteToFileQuality = new WriteToFileBidAskSpread(str);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }




            for (i=0;i<1000; i++){
                sim.addNewAgent(new PhysicaA("Fundamentalist_"+i, (long)2000000, (long) 20000, 100,
                        data.FundValue.get(j), continous, WriteToFileQuality));
            }

            for (i=0;i<200; i++){
                sim.addNewAgent(new PhysicaA("HFT_"+i, (long)2000000, (long) 20000, 100,
                        data.FundValue.get(j), continous, WriteToFileQuality));
            }

            sim.run(Day.createEuroNEXT(openning, continous, closing), nbDays);

            //sim.market.printState();
            sim.market.close();
        }
    } //for simNumber
}
