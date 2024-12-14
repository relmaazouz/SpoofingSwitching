package test;

import java.io.IOException;
import java.util.ArrayList;
import v13.Day;
import v13.LimitOrder;
import v13.MarketOrder;
import v13.Order;
import v13.OrderBook;
import v13.Random;
import v13.agents.ModerateAgent;

public class NewsHFT extends ModerateAgent{
	//long fundamentalValue = 4400 + (Random.nextDouble()>0.5?Random.nextInt(40):-Random.nextInt(40));
	
	int Frequency;
	long InitialPrice;
	int rounds;
	boolean shortselling;
	long[] fundamentalValue;
	WriteToFileBidAskSpread WriteToFileQuality;
	WriteToFileBidAskSpread WriteToFileOrderBook;
	WriteToFileBidAskSpread WriteToFileHFTActivity;
	long wealth;
	int continous;
	int nbCancelled;

	public NewsHFT(String name,long cash, long InitialPrice, int HoldAssets, long[] fundamentalValue, 
			int continous, boolean shortselling, WriteToFileBidAskSpread WriteToFileQuality, WriteToFileBidAskSpread WriteToFileOrderBook, 
			WriteToFileBidAskSpread WriteToFileHFTActivity){
		super(name,cash);
		this.fundamentalValue = fundamentalValue;
		this.InitialPrice = InitialPrice;
		rounds=0;
		this.shortselling = shortselling;
		this.continous= continous;
		this.nbCancelled = 0;
		
		this.WriteToFileHFTActivity = WriteToFileHFTActivity;
		
		if (Random.nextDouble() >0.7){
			this.cash = 1000000+Random.nextInt(1000000);
			this.setInvest("ob0", 2000+Random.nextInt(6000));
		}	
		else{ 
			this.cash = 500000+Random.nextInt(500000);
			this.setInvest("ob0", 150+Random.nextInt(100));
		}
		
		
		Frequency = 1 +Random.nextInt(20); 
		
		this.setInvest("ob0", HoldAssets);
	}
	
    void cancelPengingOrders(OrderBook ob, int round){
    	  
      	ArrayList<LimitOrder> toDestroy = new ArrayList<LimitOrder>();
        for (LimitOrder lo : ob.ask)
      	  	if (lo.sender == this)
      	  		toDestroy.add(lo);
        for (LimitOrder lo2 : toDestroy){
      	  ob.ask.remove(lo2);
      	this.nbCancelled++;
        //System.out.println(round + "\t" + "cancel" + "\t" + -1  +"\t"+ this.name + "\t" + lo2.direction + "\t" + lo2.quantity + "\t" + lo2.price);
        }
        
        toDestroy = new ArrayList<LimitOrder>();
        for (LimitOrder lo : ob.bid)
      	  	if (lo.sender == this)
     	  		toDestroy.add(lo);
        for (LimitOrder lo2 : toDestroy){
      	  ob.bid.remove(lo2);
      	this.nbCancelled++;
        //System.out.println(round + "\t" + "cancel" + "\t" + -1  +"\t"+ this.name + "\t" + lo2.direction + "\t" + lo2.quantity + "\t" + lo2.price);
        }
        
        pendings.clear(); 
      }
    

	public Order decide(String obName, Day day){
	    Order o = null;
		char dir = LimitOrder.ASK;
		int quty = 0;
		rounds++;
		if(rounds>=continous) rounds=continous-1;
		int spread=20;
		long price=fundamentalValue[rounds];
		int crashTick = 50;
		int multiple;
		if(rounds>=(crashTick-1)) shortselling = false;
		//if(!shortselling) multiple =1; else multiple = 2;
		
		
		OrderBook ob = market.orderBooks.get(obName);
		
		
		
		if( day.currentPeriod==1 && ob.numberOfPricesFixed>0){
			try {
				this.wealth = this.getInvest(obName)*ob.lastFixedPrice.price +this.cash;
				WriteToFileHFTActivity.Write(rounds + "\t" + this.name + "\t" + this.wealth  + "\t" +  this.getInvest(obName) + "\t" + 
				ob.lastFixedPrice.price  + "\t" + this.cash + "\t" + this.nbCancelled);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		

		
		if (day.currentPeriod().currentTick() % Frequency == 0 && day.currentPeriod==0){ //Opening: Send only limit orders
			
	          
	  			dir  = (Math.random()>0.5?LimitOrder.BID:LimitOrder.ASK);
	  				  			
	  			
	  			if(Math.random()>0.5){
	  				dir = LimitOrder.ASK;
	  				if(!shortselling) 
	  					if(this.getInvest(obName)>1)
	  						quty=1+Random.nextInt(this.getInvest(obName));
	  					else 
	  						return null;
	  				else 
	  					quty = 50+ Random.nextInt(150);
	  				price = this.fundamentalValue[rounds] - Random.nextInt(20) + Random.nextInt(20);
	  			}
	  			else{
	  				dir = LimitOrder.BID;
	  				price = this.fundamentalValue[rounds] - Random.nextInt(20) + Random.nextInt(20);
	  				quty = (int)(this.cash/price);
	  				if (quty<1)
	  					if(!shortselling) return null; else quty = 50+ Random.nextInt(150);
	  				} 
	  			//cancelPengingOrders(ob);
	  			cancelPengingOrders(ob, rounds);
	  			return new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));	          
	          
		}//Opening: Send only limit orders
		
		
		if (day.currentPeriod().currentTick() % Frequency == 0 && day.currentPeriod==1) // Continuous trading
	    {
			 
			//FundamentalValueUpdate(rounds);
			
			/*if(day.currentPeriod().currentTick()==15300 && this.name.equals("FundamentalistExp_2")){
	        	quty=50000;
	        	dir = LimitOrder.ASK;
	        	return new MarketOrder(obName, myId + "", dir, quty);
			}*/
			
			
		//cancelPengingOrders(ob);
		
		
		if(ob.ask.isEmpty() && ob.bid.isEmpty()){ //no bid, no ask
			if(Math.random()>0.5){
				dir = LimitOrder.ASK;
				price = fundamentalValue[rounds] + Random.nextInt(spread);
				if(!shortselling)
					if(this.getInvest(obName)>0) quty=1+Random.nextInt(this.getInvest(obName)); else return null;
				else quty = 50+ Random.nextInt(150);
			}
			else{
				dir = LimitOrder.BID;
				price = fundamentalValue[rounds] - Random.nextInt(spread);
				quty = (int)(this.cash/price);
				if (quty<1)
  					if(!shortselling) return null; else quty = 50+ Random.nextInt(150);
				} 
	    } // no bid, no ask
		
	          
		if(!ob.ask.isEmpty() && !ob.bid.isEmpty()){ // existing bid, existing ask
			if(fundamentalValue[rounds+1]<=ob.bid.first().price){ // Fund value < best bid ==> Sell
				quty = this.getInvest(obName);
				dir = LimitOrder.ASK;
        		if(!shortselling)
					if(this.getInvest(obName)>0) quty=1+Random.nextInt(this.getInvest(obName)); else return null;
				else quty = 50+ Random.nextInt(150);
        		
        		cancelPengingOrders(ob, rounds);
        		return new MarketOrder(obName, myId + "", dir, quty);

			} // Fund value < best bid
			
			if(fundamentalValue[rounds+1]>=ob.ask.first().price){ // Fund value > best ask ==> Buy
				dir = LimitOrder.BID;
				quty = (int)(this.cash/ob.ask.first().price);
				if (quty<1)
  					if(!shortselling) return null; else quty = 50+ Random.nextInt(150);
				
					cancelPengingOrders(ob, rounds);
					return new MarketOrder(obName, myId + "", dir, quty);

			} // Fund value > best ask ==> Buy
			
			if((fundamentalValue[rounds+1]<ob.ask.first().price) && (fundamentalValue[rounds+1]>ob.bid.first().price) && ((ob.ask.first().price - fundamentalValue[rounds+1])> (fundamentalValue[rounds+1] - ob.bid.first().price))){
				dir = LimitOrder.ASK;
				price = fundamentalValue[rounds] + Random.nextInt(spread);
				if(!shortselling)
					if(this.getInvest(obName)>0) quty=1+Random.nextInt(this.getInvest(obName)); else return null;
				else quty = 50+ Random.nextInt(150);
					
			}
			
			if((fundamentalValue[rounds+1]<ob.ask.first().price) && (fundamentalValue[rounds+1]>ob.bid.first().price) && ((ob.ask.first().price - fundamentalValue[rounds+1]) < (fundamentalValue[rounds+1] - ob.bid.first().price))){
				dir = LimitOrder.BID;
				price = fundamentalValue[rounds] - Random.nextInt(spread);
				if(price==0) return null;
				quty = (int)(this.cash/ob.ask.first().price);
				
				if (quty<1)
  					if(!shortselling) return null; else quty = 50+ Random.nextInt(150);
			}
			
			if((fundamentalValue[rounds+1]<ob.ask.first().price) && (fundamentalValue[rounds+1]>ob.bid.first().price) && ((ob.ask.first().price - fundamentalValue[rounds+1])== (fundamentalValue[rounds+1] - ob.bid.first().price))){
				if(Math.random()>0.5){
					dir = LimitOrder.ASK;
					price = fundamentalValue[rounds] + Random.nextInt(spread);
					if(!shortselling)
						if(this.getInvest(obName)>0) quty=1+Random.nextInt(this.getInvest(obName)); else return null;
					else quty = 50+ Random.nextInt(150);
				}
				else{
					dir = LimitOrder.BID;
					price = fundamentalValue[rounds] - Random.nextInt(spread);
					if(price==0) return null;
					quty = (int)(this.cash/ob.ask.first().price);
					if (quty<1)
	  					if(!shortselling) return null; else quty = 50+ Random.nextInt(150);
					} 	
			}
			
		//}
	    } // existing bid, existing ask

		
		if(ob.ask.isEmpty() && !ob.bid.isEmpty()){ //existing bid, no ask
			if(fundamentalValue[rounds+1]<ob.bid.first().price){
        		dir = LimitOrder.ASK;
        		if(!shortselling)
					if(this.getInvest(obName)>0) quty=1+Random.nextInt(this.getInvest(obName)); else return null;
				else quty = 50+ Random.nextInt(150);
        		
        		cancelPengingOrders(ob, rounds);
        		return new MarketOrder(obName, myId + "", dir, quty); 
        		
        		
			}
			
			if(fundamentalValue[rounds+1]>=ob.bid.first().price){
				dir = LimitOrder.ASK;
				price = fundamentalValue[rounds] + Random.nextInt(spread);
				
        		if(!shortselling)
					if(this.getInvest(obName)>0) quty=1+Random.nextInt(this.getInvest(obName)); else return null;
				else quty = 50+ Random.nextInt(150);
			}
		}
		
		if(!ob.ask.isEmpty() && ob.bid.isEmpty()){ //no bid, existing ask
		
			if(fundamentalValue[rounds+1]>ob.ask.first().price){
				dir = LimitOrder.BID;
				if(ob.ask.first().type =='L') price = ob.ask.first().price;  else price = fundamentalValue[rounds] - Random.nextInt(spread);
				quty = (int)(this.cash/price);
				
				if (quty>ob.ask.first().quantity) 
					quty=ob.ask.first().quantity;
				else if (shortselling) quty = 50+ Random.nextInt(150);
				else return null;
				if (quty>1) return new MarketOrder(obName, myId + "", dir, quty); 
			}
			
			if(fundamentalValue[rounds+1]<=ob.ask.first().price){
				dir = LimitOrder.BID;
				price = fundamentalValue[rounds] - Random.nextInt(spread);
				if (price==0) return null;
				quty = (int)(this.cash/price);
				if (quty<1)
  					if(!shortselling) return null; else quty = 50+ Random.nextInt(150);
			}
			
		//} 
		}	 // continous
		//quty = Random.nextInt(500);
		if (price<=0) price =1;
		
	

		cancelPengingOrders(ob, rounds);
		o = new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));	
		return o;
		
	    } // frequency
		else return null;
}

		
}
