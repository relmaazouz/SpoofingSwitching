package net.lecnam.fm;

import java.util.ArrayList;

import fr.cristal.smac.atom.Day;
import fr.cristal.smac.atom.FilteredLogger;
import fr.cristal.smac.atom.orders.LimitOrder;
import fr.cristal.smac.atom.orders.MarketOrder;
import fr.cristal.smac.atom.MarketPlace;
import fr.cristal.smac.atom.MonothreadedSimulation;
import fr.cristal.smac.atom.Order;
import fr.cristal.smac.atom.OrderBook;
import fr.cristal.smac.atom.Random;
import fr.cristal.smac.atom.Simulation;
import fr.cristal.smac.atom.agents.ModerateAgent;

public class Fundamentalist extends ModerateAgent {
    private int nassets;
    long[][] PriceList;
    int[] InitQuanty;
    //boolean LimitsAreDetermined;
    int Frequency;
    long[] FundamentalValue;
    double epsilon;
    long encadrementBAS;
    long encadrementHAUT;
    int rounds;


    public Fundamentalist(String name, long cash, ArrayList<long[]> InitialPrice, int[] HoldAssets, long[] FundamentalValue, int continous) {
        super(name, cash);
        int i;
        epsilon = 0.01 + Random.nextInt(10) * 0.01;
        this.FundamentalValue = FundamentalValue;
        encadrementBAS = (long) (FundamentalValue[0] * (1 - epsilon));
        encadrementHAUT = (long) (FundamentalValue[0] * (1 + epsilon));
        nassets = InitialPrice.get(0).length;
        PriceList = new long[2][nassets];
        InitQuanty = new int[nassets];
        InitQuanty = HoldAssets;
        Frequency = 1 + Random.nextInt(20);
        rounds = -1;
        //Frequency = 1;
        if (this.name.equals("Fundamentalist_2")) Frequency = 2;

        for (i = 0; i < nassets; i++) {
            PriceList[0][i] = InitialPrice.get(0)[0];
            PriceList[1][i] = InitialPrice.get(0)[0];
        }
        //this.LimitsAreDetermined = false;

    }



    public void FundamentalValueUpdate(int tick) {
        //FundamentalValue = FundamentalValue + (long)(Random.nextGaussian(0,0.5)*10);
        //encadrementBAS = (long)(FundamentalValue - epsilon*FundamentalValue + Random.nextInt((int)(epsilon*FundamentalValue)));
        //encadrementHAUT = (long)(FundamentalValue+Random.nextInt((int)(epsilon*FundamentalValue)));
        //encadrementBAS = (long)(FundamentalValue[tick]*(1-epsilon));
        //encadrementHAUT = (long)(FundamentalValue[tick]*(1+epsilon));
        encadrementBAS = (long) (FundamentalValue[tick] - Random.nextInt(20));
        encadrementHAUT = (long) (FundamentalValue[tick] + Random.nextInt(20));
    }


    public Order decide(String obName, Day day) {

        Order o = null;
        char dir;
        int quty;
        rounds++;
        OrderBook ob = market.orderBooks.get(obName);


        int IdOrderBook = Integer.parseInt(obName.substring(obName.lastIndexOf("b") + 1));

        if (day.currentPeriod().currentTick() == 1 && day.currentPeriod == 0)
            this.setInvest(obName, InitQuanty[IdOrderBook]);


        if (cash < 0 || this.getInvest(obName) < 0) {
            System.out.println(this.name + " " + " " + cash + " " + getInvest(obName) + " " + pendings.size() + " " + day.currentPeriod().currentTick());
            // System.exit(1);
        }


        if (day.currentPeriod().currentTick() % Frequency == 0) // on rééquilibre
        {


            ArrayList<LimitOrder> toDestroy = new ArrayList<LimitOrder>();
            for (LimitOrder lo : ob.ask)
                if (lo.sender == this)
                    toDestroy.add(lo);
            for (LimitOrder lo2 : toDestroy)
                ob.ask.remove(lo2);

            toDestroy = new ArrayList<LimitOrder>();
            for (LimitOrder lo : ob.bid)
                if (lo.sender == this)
                    toDestroy.add(lo);
            for (LimitOrder lo2 : toDestroy)
                ob.bid.remove(lo2);

            pendings.clear();


            FundamentalValueUpdate(rounds);
            // if (this.name.equals("Fundamentalist_2"))


            long price;
            long ReferencePrice;


            dir = (Math.random() > 0.5 ? LimitOrder.BID : LimitOrder.ASK); // act randomly
            if (dir == LimitOrder.ASK) { // i am a Seller : Vendeur
                if (this.getInvest(obName) > 0) {
                    price = encadrementHAUT;
                    quty = 1 + Random.nextInt(this.getInvest(obName));
                } else return null;
            } else {
                price = encadrementBAS;
                quty = (int) (this.cash / price);
            }


            if (ob.numberOfPricesFixed != 0) {
                ReferencePrice = ob.lastFixedPrice.price;
                //System.out.println(this.name + "\t" + this.getInvest(obName) + "\t" +FundamentalValue[rounds] + "\t" + ReferencePrice);
                if (ReferencePrice > encadrementHAUT && this.getInvest(obName) > 0) { //Asset is overpriced so sell it
                    dir = LimitOrder.ASK;
                    quty = 1 + Random.nextInt(this.getInvest(obName));
                    price = ReferencePrice - Random.nextInt(5);
                    //price = encadrementHAUT;
                } else if (ReferencePrice < encadrementBAS) { //Asset is underpriced so buy it
                    dir = LimitOrder.BID;
                    price = ReferencePrice + Random.nextInt(5);
                    //price = encadrementBAS;
                    quty = (int) (this.cash / price);
                } else {
                    dir = (Math.random() > 0.5 ? LimitOrder.BID : LimitOrder.ASK); // act randomly
                    if (dir == LimitOrder.ASK && this.getInvest(obName) > 0) { // i am a Seller : Vendeur
                        price = ReferencePrice - Random.nextInt(5);
                        //price = encadrementHAUT;
                        quty = 1 + Random.nextInt(this.getInvest(obName));
                    } else {
                        price = ReferencePrice + Random.nextInt(5);
                        //price = encadrementBAS;
                        quty = (int) (this.cash / price);
                    }
                }
            } else if (ob.ask.size() > 0) { // check if there is something in the Ask order book
                if (ob.ask.first().price > encadrementHAUT) { //assets is overpriced so sell it
                    if (this.getInvest(obName) > 0) {
                        dir = LimitOrder.ASK;
                        quty = 1 + Random.nextInt(this.getInvest(obName));
                        price = ob.ask.first().price - Random.nextInt(5);
                    } else return null;
                }
            } else if (ob.bid.size() > 0) { // check if there is something in the Bid order book
                if (ob.bid.first().price < encadrementBAS) { // buy it
                    dir = LimitOrder.BID;
                    //price = encadrementBAS;
                    price = ob.bid.first().price + Random.nextInt(5);
                    quty = (int) (this.cash / price);
                }
            } else {
                ReferencePrice = PriceList[0][0];


                if (ReferencePrice > encadrementHAUT) { //Asset is overpriced so sell it
                    if (this.getInvest(obName) > 0) {
                        dir = LimitOrder.ASK;
                        quty = 1 + Random.nextInt(this.getInvest(obName));
                        //price = encadrementHAUT;
                        price = ReferencePrice - Random.nextInt(5);
                    } else return null;
                } else if (ReferencePrice < encadrementBAS) { //Asset is underpriced so buy it
                    dir = LimitOrder.BID;
                    //price = encadrementBAS;
                    price = ReferencePrice + Random.nextInt(5);
                    quty = (int) (this.cash / price);
                } else {
                    dir = (Math.random() > 0.5 ? LimitOrder.BID : LimitOrder.ASK); // act randomly
                    if (dir == LimitOrder.ASK) { // i am a Seller : Vendeur
                        if (this.getInvest(obName) > 0) {
                            price = ReferencePrice - Random.nextInt(5);
                            //price = encadrementHAUT;
                            quty = 1 + Random.nextInt(this.getInvest(obName));
                        } else return null;
                    } else {
                        price = ReferencePrice + Random.nextInt(5);
                        //price = encadrementBAS;
                        quty = (int) (this.cash / price);
                    }
                }
            }

            o = new LimitOrder(obName, "" + myId, dir, Math.abs(quty), Math.abs(price));
            //System.out.println(this.Day + "\t" + IdOrderBook + "\t" + this.name + "\t" + dir + "\t" + quty + "\t" + price);

        } // send limit order


        if (day.currentPeriod().currentTick() == 250 && this.name.equals("Fundamentalist_2")) {
            quty = 10000;
            dir = LimitOrder.ASK;
            return new MarketOrder(obName, myId + "", dir, quty);
        }

        return o;
    }

    public static void main(String args[]) {

        int i;
        int openning = 10;
        int continous = 200;
        int closing = 10;
        long[] FundamentalValue = new long[openning + continous + closing];
        FundamentalValue[0] = 8250;
        for (i = 1; i < openning + continous + closing; i++)
            FundamentalValue[i] = FundamentalValue[i - 1] + (long) (Random.nextGaussian(0, 0.5) * 10);

        Simulation sim = new MonothreadedSimulation();
        sim.market.setFixingPeriod(MarketPlace.CONTINUOUS); // ou FIX
        sim.market.logType = MarketPlace.LONG; // ou SHORT
        FilteredLogger fl = new FilteredLogger("Something");
        fl.orders = true;
        fl.prices = true;
        fl.agents = true;
        fl.infos = true;
        fl.commands = true;
        sim.setLogger(fl);
        int nbOrderBooks = 1;
        int nbDays = 1;

        for (i = 0; i < nbOrderBooks; i++)
            sim.addNewOrderBook("ob" + i);

        ArrayList<long[]> listPrices = new ArrayList<long[]>();
        long[] price = new long[nbOrderBooks];
        int[] quantity = new int[nbOrderBooks];

        for (i = 0; i < nbOrderBooks; i++) {
            // price[i] = (long)(1000+ Random.nextInt(999));
            price[i] = 8200;
            quantity[i] = Random.nextInt(200);
        }
        listPrices.add(price);


        for (i = 0; i < 1000; i++) {
            sim.addNewAgent(new Fundamentalist("Fundamentalist_" + i, 1000000, listPrices, quantity, FundamentalValue, continous));
        }

        sim.run(Day.createEuroNEXT(openning, continous, closing), nbDays);

        //sim.market.printState();
        sim.market.close();
    }

}
