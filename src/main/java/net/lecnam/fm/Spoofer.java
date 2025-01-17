package net.lecnam.fm;

import java.io.IOException;
import java.util.ArrayList;

import fr.cristal.smac.atom.Day;
import fr.cristal.smac.atom.orders.LimitOrder;
import fr.cristal.smac.atom.orders.MarketOrder;
import fr.cristal.smac.atom.Order;
import fr.cristal.smac.atom.OrderBook;
import fr.cristal.smac.atom.Random;
import fr.cristal.smac.atom.agents.ModerateAgent;

public class Spoofer extends ModerateAgent{
    int round;
    int buyTick;
    int spoofingTick;
    int sellTick;
    int cancelTick;
    int continous;
    long currentBuyPrice;

    public Spoofer(String name,long cash, int continous, WriteToFileBidAskSpread WriteToFileQuality,
                   WriteToFileBidAskSpread WriteToFileHFTActivity) throws IOException{
        super(name,cash);

        this.continous = continous;
        //this.buyTick=10+Random.nextInt(continous/2);
        //this.spoofingTick = this.buyTick + Random.nextInt(50);
        //this.sellTick = this.spoofingTick + Random.nextInt(50);
        //this.cancelTick = this.sellTick + Random.nextInt(50);

        this.buyTick=500;
        this.spoofingTick = this.buyTick + 50;
        this.sellTick = this.spoofingTick + Random.nextInt(50);
        this.cancelTick = this.sellTick + Random.nextInt(50);
        this.setInvest("ob", 100);


        currentBuyPrice=Integer.MAX_VALUE;
    } //function



    void cancelPengingOrders(OrderBook ob){

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
        for (LimitOrder lo2 : toDestroy){
            ob.bid.remove(lo2);

        }
    }






    public Order decide(String obName, Day day)
    {

        Order o = null;
        long price;
        char dir;
        int quty = 0;



        OrderBook ob = market.orderBooks.get(obName);
        round++;



        if (day.currentPeriod().currentTick() == buyTick && day.currentPeriod==1 && !ob.bid.isEmpty()){
            currentBuyPrice = ob.bid.first().price;
            dir = LimitOrder.BID;
            quty = 50;
            o = new MarketOrder(obName, myId + "", dir, quty);
            return o;
        }

        if (day.currentPeriod().currentTick() == spoofingTick && day.currentPeriod==1 && !ob.bid.isEmpty()){ //spoofing episode
            ob = market.orderBooks.get(obName);
            int bidQuantity = 0;
            for (LimitOrder lo : ob.bid)
                bidQuantity += lo.quantity;


            dir = LimitOrder.BID;
            //quty = 1000;
            quty = bidQuantity*5/ob.bid.size();
            price =  ob.bid.first().price - Random.nextInt(400);
            return new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));
        }

        if (day.currentPeriod().currentTick() > sellTick && day.currentPeriod==1 && !ob.bid.isEmpty()){
            quty = this.getInvest(obName);
            dir = LimitOrder.ASK;
            if(quty>0 && ob.bid.first().price > currentBuyPrice){
                buyTick = day.currentPeriod().currentTick() + Random.nextInt(50);
                spoofingTick = buyTick + Random.nextInt(50);
                sellTick = buyTick + Random.nextInt(50);
                return new MarketOrder(obName, myId + "", dir, quty);
            }
            else return null;
        }


        if(day.currentPeriod==1 && !ob.bid.isEmpty())
            if (ob.bid.first().sender==this)
                cancelPengingOrders(ob);

        if (day.currentPeriod().currentTick() == cancelTick)
            cancelPengingOrders(ob);

        return o;
    }

}//class
