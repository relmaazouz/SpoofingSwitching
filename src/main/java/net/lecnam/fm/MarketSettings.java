package net.lecnam.fm;

import java.io.IOException;
import java.util.ArrayList;

import fr.cristal.smac.atom.Day;
import fr.cristal.smac.atom.FilteredLogger;
import fr.cristal.smac.atom.MarketPlace;
import fr.cristal.smac.atom.MonothreadedSimulation;
import fr.cristal.smac.atom.Random;
import fr.cristal.smac.atom.Simulation;

public class MarketSettings {

    /**
     * @param args
     */
    public static void main(String args[]) {

        int i;
        int openning = 100;
        // int continous=6120000; //1 tick is 5 milliseconds
        // int continous=306000; //1 tick is 100 milliseconds
        // int continous=30600; //1 tick is 1 second
        int continous = 1000;
        int closing = 0;
        int taxei = 0;
        double taxe = (double) taxei / 10000;

        int cancelTime = 5;

        for (int extSim = 1; extSim <= 1; extSim += 1) {
            for (taxei = 0; taxei <= 0; taxei += 1) {
                // for(taxei=1; taxei<=1; taxei+=1){
                taxe = (double) taxei / 10000;
                // for(cancelTime=100; cancelTime<=100; cancelTime+=1){

                Simulation sim = new MonothreadedSimulation(); // !!! Attension, shafle is commented
                sim.market.setFixingPeriod(MarketPlace.CONTINUOUS); // ou FIX
                sim.market.logType = MarketPlace.LONG; // ou SHORT
                // sim.market.cost = 0.002;
                FilteredLogger fl = new FilteredLogger("Something_" + taxe * 100 + "_" + cancelTime + "_" + extSim);
                fl.orders = true;
                fl.prices = true;
                fl.agents = false;
                fl.infos = false;
                fl.commands = false;
                sim.setLogger(fl);
                int nbOrderBooks = 1;
                int nbDays = 1;
                int quantity = Random.nextInt(10000);
                long initPrice = 4400;
                long[] fundamentalValue = new long[openning + continous + closing];

                for (i = 0; i < nbOrderBooks; i++)
                    sim.addNewOrderBook("ob" + i);

                fundamentalValue[0] = initPrice;
                int modif;
                for (i = 1; i < openning + continous + closing; i++) {
                    modif = (Random.nextDouble() > 0.3 ? Random.nextInt(6) : Random.nextInt(10));
                    // if(i%60==0)
                    fundamentalValue[i] = fundamentalValue[i - 1] + (Random.nextDouble() > 0.5 ? +1 : -1) * modif;
                    // else
                    // fundamentalValue[i]= fundamentalValue[i-1];
                    // System.out.println(modif+ "\t"+ fundamentalValue[i]);
                }

                WriteToFileBidAskSpread WriteToFileQuality = null;
                WriteToFileBidAskSpread WriteToFileHFTActivity = null;
                WriteToFileBidAskSpread WriteToFileOrderBook = null;
                try {
                    String str = "BidAskSpread" + "_" + taxe * 100 + "_" + cancelTime + "_" + extSim;
                    WriteToFileQuality = new WriteToFileBidAskSpread(str);

                    str = "HFTActivity" + "_" + taxe * 100 + "_" + cancelTime + "_" + extSim;
                    WriteToFileHFTActivity = new WriteToFileBidAskSpread(str);

                    str = "OrderBook" + "_" + taxe * 100 + "_" + cancelTime + "_" + extSim;
                    WriteToFileOrderBook = new WriteToFileBidAskSpread(str);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    System.out.println("##################@");
                    e1.printStackTrace();
                }

                /* Fundamentalists bolc */
                /*
                 * for (i=0;i<1000; i++){
                 * quantity= Random.nextInt(300);
                 * sim.addNewAgent(new FundamentalistsExp("FundamentalistExp_"+i,10000000 +
                 * Random.nextInt(10000000),initPrice,
                 * quantity, fundamentalValue, continous, true, WriteToFileQuality,
                 * WriteToFileOrderBook, WriteToFileHFTActivity));
                 * }
                 */

                /* Fundamentalists bolc */
                for (i = 0; i < 1000; i++) {
                    quantity = 100 + Random.nextInt(300);

                    int[] randomQuantity = new int[1000];

                    long[] randomInitPriceArray = new long[1000];

                    ArrayList<long[]> initPriceArrayList = new ArrayList<>();


                    for (int j = 0; j < 1000; j++) {
                        // Generate random variations from initPrice
                        randomInitPriceArray[j] = initPrice + Random.nextInt(100) - 50; // ±50 random variation
                        randomQuantity[j] = quantity + Random.nextInt(100);
                    }
                    initPriceArrayList.add(randomInitPriceArray);

                    sim.addNewAgent(
                            new Fundamentalist("Fundamentalist_" + i, 1000 + Random.nextInt(1000), initPriceArrayList,
                                    randomQuantity, fundamentalValue, continous)
                    );
                }

                /* Spoofer bolc */
                for (i = 0; i < 10; i++) {
                    try {
                        sim.addNewAgent(new Spoofer("Spoofer_" + i, 1000 + Random.nextInt(1000),
                                continous, WriteToFileQuality, WriteToFileHFTActivity));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

                for (i = 0; i < 100; i++) {
                    try {
                        sim.addNewAgent(new Pressure("Pressure_" + i, 1000 + Random.nextInt(1000),
                                continous, WriteToFileQuality, WriteToFileHFTActivity));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

                sim.run(Day.createEuroNEXT(openning, continous, closing), nbDays);

                // sim.market.printState();
                sim.market.close();

                try {
                    WriteToFileQuality.Close();
                    WriteToFileHFTActivity.Close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } // taxe
    }

}
