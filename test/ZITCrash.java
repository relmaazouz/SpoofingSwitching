package test;



import java.io.IOException;
import java.util.ArrayList;

import v13.*;
import v13.agents.*;


public class ZITCrash extends ModerateAgent
{


    long initPrice;
	WriteToFileBidAskSpread WriteToFileQuality;
	WriteToFileBidAskSpread WriteToFileOrderBook;
	int Frequency;

   public ZITCrash(String name, long cash, long InitialPrice, WriteToFileBidAskSpread WriteToFileQuality, WriteToFileBidAskSpread WriteToFileOrderBook)
    {        super(name, cash);
        this.initPrice = InitialPrice;
		this.Frequency=1+Random.nextInt(20);
		if(this.name.equals("ZIT_2")){
			this.WriteToFileQuality=WriteToFileQuality;
			this.WriteToFileOrderBook=WriteToFileOrderBook;
			Frequency=1;
			
		}

		 
    }
    
    
    
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
       for (LimitOrder lo2 : toDestroy)
     	  ob.bid.remove(lo2);

       pendings.clear(); 
     }
   

    public Order decide(String invest, Day day)
    {

        Order o = null;
        int quty;
        long price;
        OrderBook ob = market.orderBooks.get(invest);
        int Spread = 50;
        int crashTick = 50;
        int IdOrderBook = Integer.parseInt(invest.substring(invest.lastIndexOf("b")+1));

       
        if(this.name.equals("ZIT_2")){   
			if( day.currentPeriod==1 && this.name.equals("ZIT_2") && ob.ask.size()>0 && ob.bid.size()>0 && ob.numberOfPricesFixed>0){
		    	String str=day.currentPeriod().currentTick()+ "\t" + ob.ask.last().price +"\t" 
		    			+ ob.ask.last().quantity +"\t" + ob.bid.last().price +"\t"
		    			+ob.bid.last().quantity + "\t" + ob.lastFixedPrice.price + "\t" 
		    			+ ob.lastFixedPrice.dir +"\t" +ob.lastFixedPrice.quantity+ "\t" 
		    			+ ob.numberOfPricesFixed + "\t" + ob.numberOfOrdersReceived;
		    	try {
					WriteToFileQuality.Write(str);
				} catch (IOException e) {
					e.printStackTrace();
				}
		    
		    }
			
			if( day.currentPeriod==1 && this.name.equals("ZIT_2")){
		    	String strob= "Tick;"+ day.currentPeriod().currentTick() +";======" + "\n" +  ob.bid +"\t" + ob.ask;
		    	try {
					WriteToFileOrderBook.Write(strob);
				} catch (IOException e) {
					e.printStackTrace();
				}
		    
		    }
		
			
		}
        
        
    if (day.currentPeriod().currentTick() % Frequency == 0) // Continuous trading
	{
       char dir  = (Math.random()>0.5?LimitOrder.BID:LimitOrder.ASK);
    			dir  = (Math.random()>0.5?LimitOrder.BID:LimitOrder.ASK);
    			if(Random.nextDouble()>0.3)
    				quty=Random.nextInt(100);
    			else
    				quty=Random.nextInt(700);

    	
       
       
    			if(this.name.equals("ZIT_2")){
    				if(day.currentPeriod().currentTick()==crashTick){
    					quty=50000;
    					dir = LimitOrder.ASK;
    					cancelPengingOrders(ob);
    					return new MarketOrder(invest, myId + "", dir, quty);
    				}
    				else if (day.currentPeriod().currentTick()==(crashTick+1)) 
    					cancelPengingOrders(ob);
    			
    			}
    		
   			

    		price = initPrice - Spread + Random.nextInt(Spread*2);
        	o = new LimitOrder(invest,""+myId,dir,Math.abs(quty),Math.abs(price)); 
        
        cancelPengingOrders(ob);	
    	return o;
    }
   
    else return null;
    }
    
}
