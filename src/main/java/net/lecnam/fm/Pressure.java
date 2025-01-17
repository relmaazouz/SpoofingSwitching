package net.lecnam.fm;

import java.io.IOException;

import fr.cristal.smac.atom.Day;
import fr.cristal.smac.atom.orders.LimitOrder;
import fr.cristal.smac.atom.orders.MarketOrder;
import fr.cristal.smac.atom.Order;
import fr.cristal.smac.atom.OrderBook;
import fr.cristal.smac.atom.Random;
import fr.cristal.smac.atom.agents.ModerateAgent;

public class Pressure extends ModerateAgent{
    double It;
    double It1;
    double It2;
    int timeInterval;
    int level;
    int Frequency;

    public Pressure(String name,long cash, int continous, WriteToFileBidAskSpread WriteToFileQuality,
                    WriteToFileBidAskSpread WriteToFileHFTActivity) throws IOException{
        super(name,cash);
        timeInterval = 10 + Random.nextInt(60);
        Frequency = 100 + Random.nextInt(200);
    } //function



    public Order decide(String obName, Day day)
    {

        Order o = null;
        long price;
        char dir;
        int quty = 0;
        int askQuantity=0;
        int bidQuantity=0;

        OrderBook ob = market.orderBooks.get(obName);
        for (LimitOrder lo : ob.ask)
            askQuantity += lo.quantity;

        for (LimitOrder lo : ob.bid)
            bidQuantity += lo.quantity;

        if (day.currentPeriod().currentTick() % Frequency == 0 && day.currentPeriod==1) // Continuous trading
            It = (double)(bidQuantity-askQuantity)/(bidQuantity+askQuantity);

        if (day.currentPeriod().currentTick() % (Frequency+ timeInterval)== 0 && day.currentPeriod==1) // Continuous trading
            It1 = (double)(bidQuantity-askQuantity)/(bidQuantity+askQuantity);

        if (day.currentPeriod().currentTick() % (Frequency+ 2*timeInterval) == 0 && day.currentPeriod==1){
            It2 = (double)(bidQuantity-askQuantity)/(bidQuantity+askQuantity);
            if(It<It1 && It1<It2){
                dir = LimitOrder.BID;
                quty = 50;
                if(Random.nextDouble()>0)
                    o=new MarketOrder(obName, myId + "", dir, quty);
                else{
                    price = ob.bid.first().price+Random.nextInt(10);
                    dir = LimitOrder.BID;
                    o = new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));
                }
            }
        }
        if (day.currentPeriod().currentTick() % (Frequency+ 2*timeInterval+1) == 0 && day.currentPeriod==1 && !ob.ask.isEmpty()){
            It2 = (double)(bidQuantity-askQuantity)/(bidQuantity+askQuantity);
            if(It<It1 && It1<It2){
                dir = LimitOrder.ASK;
                quty = 50;
                price = ob.ask.first().price+(50 + Random.nextInt(50));
                o = new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));
            }
        }

        return o;
    } //order function

} //class

