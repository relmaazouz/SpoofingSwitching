package test;

import java.io.IOException;
import java.util.ArrayList;

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

public class FundamentalistsExp extends ModerateAgent{
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

	public FundamentalistsExp(String name,long cash, long InitialPrice, int HoldAssets, long[] fundamentalValue, 
			int continous, boolean shortselling, WriteToFileBidAskSpread WriteToFileQuality, WriteToFileBidAskSpread WriteToFileOrderBook, 
			WriteToFileBidAskSpread WriteToFileHFTActivity){
		super(name,cash);
		this.fundamentalValue = fundamentalValue;
		this.InitialPrice = InitialPrice;
		rounds=-1;
		this.shortselling = shortselling;

		
		if(this.name.equals("FundamentalistExp_2")){
			this.WriteToFileQuality=WriteToFileQuality;
			this.WriteToFileOrderBook=WriteToFileOrderBook;
			
		}
		this.WriteToFileHFTActivity = WriteToFileHFTActivity;
		
		if (Random.nextDouble() >0.7){
			this.cash = 1000000+Random.nextInt(1000000);
			this.setInvest("ob0", 2000+Random.nextInt(6000));
		}	
		else{ 
			this.cash = 50000+Random.nextInt(50000);
			this.setInvest("ob0", 150+Random.nextInt(100));
		}
		
		//Frequency = 1+Random.nextInt(continous); //number of intraday ticks determines the trading frequency
		Frequency = 1 + Random.nextInt(100); 
		if(this.name.equals("FundamentalistExp_2")) this.Frequency=1; //he is information writer, so he trades every signe tick
		this.setInvest("ob0", HoldAssets);
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
    

	public Order decide(String obName, Day day){
	    Order o = null;
		char dir = LimitOrder.ASK;
		int quty = 0;
		rounds++;
		int spread=20;
		long price=fundamentalValue[rounds];
		int crashTick = 50;
		int multiple;
		if(!shortselling) multiple =1; else multiple = 2;
		
		//fundamentalValue[rounds]  = fundamentalValue[rounds]+(Random.nextDouble()>0.5?1:-1)*Random.nextInt(5); //Biaised fundamental value
		//fundamentalValue[rounds]  = fundamentalValue[rounds]+(Random.nextDouble()>0.5?1:-1); //Biaised fundamental value
		
		OrderBook ob = market.orderBooks.get(obName);
		
		
		
		if( day.currentPeriod==1 && ob.numberOfPricesFixed>0){
			try {
				this.wealth = this.getInvest(obName)*ob.lastFixedPrice.price +this.cash;
				WriteToFileHFTActivity.Write(rounds + "\t" + this.name + "\t" + this.wealth  + "\t" +  this.getInvest(obName) + "\t" + 
				ob.lastFixedPrice.price  + "\t" + this.cash + "\t" + 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		if(this.name.equals("FundamentalistExp_2")){   
				if( day.currentPeriod==1 && this.name.equals("FundamentalistExp_2") && ob.ask.size()>0 && ob.bid.size()>0 && ob.numberOfPricesFixed>0){
			    	String str=day.currentPeriod().currentTick()+ "\t" + ob.ask.last().price +"\t" 
			    			+ ob.ask.last().quantity +"\t" + ob.bid.last().price +"\t"
			    			+ob.bid.last().quantity + "\t" + ob.lastFixedPrice.price + "\t" 
			    			+ ob.lastFixedPrice.dir +"\t" +ob.lastFixedPrice.quantity+ "\t" 
			    			+ fundamentalValue[rounds] + "\t" + ob.numberOfPricesFixed + "\t" + ob.numberOfOrdersReceived;
			    	try {
						WriteToFileQuality.Write(str);
					} catch (IOException e) {
						e.printStackTrace();
					}
			    
			    }
				ob.toString();
				if( day.currentPeriod==1 && this.name.equals("FundamentalistExp_2")){
			    	//String strob= "Tick;"+ day.currentPeriod().currentTick() +";======" + "\n" +  ob.bid.toString() +"\t" + ob.ask.toString();
					String strob= "Tick;"+ day.currentPeriod().currentTick() +";======" + "\n" +  ob.toString();
			    	try {
						WriteToFileOrderBook.Write(strob);
					} catch (IOException e) {
						e.printStackTrace();
					}
			    
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
	  			cancelPengingOrders(ob);
	  			return new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));	          
	          
		}//Opening: Send only limit orders
		
		
		if (day.currentPeriod().currentTick() % Frequency == 0 && day.currentPeriod==1) // Continuous trading
	    {
			 
			//FundamentalValueUpdate(rounds);
			
			//FLASH CRASH HERE
			/*if(day.currentPeriod().currentTick()==crashTick && this.name.equals("FundamentalistExp_2")){
	        	quty=5000;
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
			if(fundamentalValue[rounds]<=ob.bid.first().price){ // Fund value < best bid ==> Sell
				quty = this.getInvest(obName);
				dir = LimitOrder.ASK;
        		if(!shortselling)
					if(this.getInvest(obName)>0) quty=1+Random.nextInt(this.getInvest(obName)); else return null;
				else quty = 50+ Random.nextInt(150);
        		
        		cancelPengingOrders(ob);
        		return new MarketOrder(obName, myId + "", dir, quty);

			} // Fund value < best bid
			
			if(fundamentalValue[rounds]>=ob.ask.first().price){ // Fund value > best ask ==> Buy
				dir = LimitOrder.BID;
				quty = (int)(this.cash/ob.ask.first().price);
				if (quty<1)
  					if(!shortselling) return null; else quty = 50+ Random.nextInt(150);
				
					cancelPengingOrders(ob);
					return new MarketOrder(obName, myId + "", dir, quty);

			} // Fund value > best ask ==> Buy
			
			if((fundamentalValue[rounds]<ob.ask.first().price) && (fundamentalValue[rounds]>ob.bid.first().price) && ((ob.ask.first().price - fundamentalValue[rounds])> (fundamentalValue[rounds] - ob.bid.first().price))){
				dir = LimitOrder.ASK;
				price = fundamentalValue[rounds] + Random.nextInt(spread);
				if(!shortselling)
					if(this.getInvest(obName)>0) quty=1+Random.nextInt(this.getInvest(obName)); else return null;
				else quty = 50+ Random.nextInt(150);
					
			}
			
			if((fundamentalValue[rounds]<ob.ask.first().price) && (fundamentalValue[rounds]>ob.bid.first().price) && ((ob.ask.first().price - fundamentalValue[rounds]) < (fundamentalValue[rounds] - ob.bid.first().price))){
				dir = LimitOrder.BID;
				price = fundamentalValue[rounds] - Random.nextInt(spread);
				if(price==0) return null;
				quty = (int)(this.cash/ob.ask.first().price);
				
				if (quty<1)
  					if(!shortselling) return null; else quty = 50+ Random.nextInt(150);
			}
			
			if((fundamentalValue[rounds]<ob.ask.first().price) && (fundamentalValue[rounds]>ob.bid.first().price) && ((ob.ask.first().price - fundamentalValue[rounds])== (fundamentalValue[rounds] - ob.bid.first().price))){
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
			if(fundamentalValue[rounds]<ob.bid.first().price){
        		dir = LimitOrder.ASK;
        		if(!shortselling)
					if(this.getInvest(obName)>0) quty=1+Random.nextInt(this.getInvest(obName)); else return null;
				else quty = 50+ Random.nextInt(150);
        		
        		cancelPengingOrders(ob);
        		return new MarketOrder(obName, myId + "", dir, quty); 
        		
        		
			}
			
			if(fundamentalValue[rounds]>=ob.bid.first().price){
				dir = LimitOrder.ASK;
				price = fundamentalValue[rounds] + Random.nextInt(spread);
				
        		if(!shortselling)
					if(this.getInvest(obName)>0) quty=1+Random.nextInt(this.getInvest(obName)); else return null;
				else quty = 50+ Random.nextInt(150);
			}
		}//existing bid, no ask
		
		if(!ob.ask.isEmpty() && ob.bid.isEmpty()){ //no bid, existing ask
		
			if(fundamentalValue[rounds]>ob.ask.first().price){
				dir = LimitOrder.BID;
				if(ob.ask.first().type =='L') price = ob.ask.first().price;  else price = fundamentalValue[rounds] - Random.nextInt(spread);;
				quty = (int)(this.cash/price);
				if (quty>ob.ask.first().quantity) 
					quty=ob.ask.first().quantity;
				else if (shortselling) quty = 50+ Random.nextInt(150);
				else return null;
				if (quty>1) return new MarketOrder(obName, myId + "", dir, quty); 
			}
			
			if(fundamentalValue[rounds]<=ob.ask.first().price){
				dir = LimitOrder.BID;
				price = fundamentalValue[rounds] - Random.nextInt(spread);
				if (price==0) return null;
				quty = (int)(this.cash/price);
				if (quty<1)
  					if(!shortselling) return null; else quty = 50+ Random.nextInt(150);
			}
			
		//} 
		}	//no bid, existing ask
		//quty = Random.nextInt(50);
		if (price<=0) price =1;
		
	

		cancelPengingOrders(ob);
		o = new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));	
		return o;
		
	    } // frequency
		else return null;
}

	
	
	/*public void FundamentalValueUpdate(int tick){
			int modif = (Random.nextDouble()>0.3? Random.nextInt(20): Random.nextInt(50));
				this.fundamentalValue = this.fundamentalValue + (Random.nextDouble()>0.5?+1:-1)*modif;
				
	}*/
	
	
	
	public static void main(String args[])
    {
	
	 	int i;
	 	int openning = 0;
	 	//int continous=6120000; //1 tick is 5 milliseconds
	 	//int continous=306000; //1 tick is 100 milliseconds
	 	//int continous=30600; //1 tick is 1 second
	 	int continous = 3000;
	 	int closing =0;
	 	int taxei=0;
	 	double taxe=(double)taxei/10000;
	 	
	 	//convert to millisecond grain
	 	//int cancelTime=(int)Math.floor(50.0*continous/306000000.0);
	 	
	 	//int cancelTime=(int)Math.floor(500.0*continous/306000000.0);
	 	int cancelTime=5;
	 	
	 for(int extSim = 1; extSim<=1; extSim+=1){ 
	 for(taxei=0; taxei<=0; taxei+=1){
		 //for(taxei=1; taxei<=1; taxei+=1){
	 	taxe=(double)taxei/10000;
	 	//for(cancelTime=100; cancelTime<=100; cancelTime+=1){	
	 		
	    Simulation sim = new MonothreadedSimulationIAV(); //!!! Attension, shafle is commented
      	sim.market.setFixingPeriod (MarketPlace.CONTINUOUS); // ou FIX
        sim.market.logType=MarketPlace.LONG; // ou SHORT
        //sim.market.cost = 0.002;
        FilteredLogger fl = new FilteredLogger("Something_"+ taxe*100 +"_"+cancelTime+"_"+extSim);
        fl.orders = false;
        fl.prices = true;
        fl.agents = false;
        fl.infos = true;
        fl.commands = true; 
        sim.setLogger(fl);        
        int nbOrderBooks = 1;
        int nbDays=1;
        int quantity= Random.nextInt(10000);
        long initPrice  = 4400;
        long[] fundamentalValue = new long[openning+continous+closing];
        
        
	for (i=0; i<nbOrderBooks; i++)
            sim.addNewOrderBook("ob"+i);
		
	fundamentalValue[0]= initPrice; 
	int modif;
    for (i=1; i<openning+continous+closing; i++){
    	modif = (Random.nextDouble()>0.3? Random.nextInt(6): Random.nextInt(10));
	   	//if(i%60==0)
	   		fundamentalValue[i]= fundamentalValue[i-1]+(Random.nextDouble()>0.5?+1:-1)*modif;
	   	//else 
	   	//	fundamentalValue[i]= fundamentalValue[i-1];
	   	//System.out.println(modif+ "\t"+ fundamentalValue[i]);
    }
    
    
    WriteToFileBidAskSpread WriteToFileQuality=null;
    WriteToFileBidAskSpread WriteToFileHFTActivity=null;
    WriteToFileBidAskSpread WriteToFileOrderBook=null;
    try {
		String str = "BidAskSpread" + "_" + taxe*100 + "_" + cancelTime + "_" + extSim;
		WriteToFileQuality = new WriteToFileBidAskSpread(str);
		
		str = "HFTActivity" + "_" + taxe*100 + "_" + cancelTime + "_" + extSim;
		WriteToFileHFTActivity = new WriteToFileBidAskSpread(str);
		
		str = "OrderBook" + "_" + taxe*100 + "_" + cancelTime + "_" + extSim;
		WriteToFileOrderBook = new WriteToFileBidAskSpread(str);
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}

/* HFT bolc*/	
	for (i=0;i<0; i++){
				quantity= Random.nextInt(300);
	        	sim.addNewAgent(new NewsHFT("NewsHFT_"+i,10000000 + Random.nextInt(10000000),initPrice, 
	        			quantity, fundamentalValue, continous, true, WriteToFileQuality, WriteToFileOrderBook, WriteToFileHFTActivity));

			}
	  
	int nbHFT=0;
	for (i=0;i<nbHFT; i++)
		try {
			sim.addNewAgent(new HFT((i>nbHFT/2?"HFTTrendFollower_" + i:"HFTLiquidity_" + i) ,50 + Random.nextInt(150),initPrice, true, taxe, cancelTime, 
					continous, fundamentalValue, WriteToFileQuality, WriteToFileHFTActivity));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
	/* Chartists bolc*/	
	for (i=0;i<0; i++){
    	sim.addNewAgent(new MomentumAgent("Momentum_"+i,1000000,10, Random.nextInt(40), true, false));
    	sim.addNewAgent(new MovingAverageAgent("MovingAverage_"+i,1000000,10, Random.nextInt(40), true, false));
    	//sim.addNewAgent(new PeriodicAgent("Periodic_"+i,1000000,10, 10));
    	sim.addNewAgent(new RsiAgent("Rsi_"+i,1000000, 14+Random.nextInt(20), Random.nextInt(30), true, false));
    	//sim.addNewAgent(new SimpleTrendFollower("Trend_"+i));
    }
	
	/* Fundamentalists bolc*/
    for (i=0;i<1000; i++){
    		quantity= Random.nextInt(300);
        	sim.addNewAgent(new FundamentalistsExp("FundamentalistExp_"+i,10000000 + Random.nextInt(10000000),initPrice, 
        			quantity, fundamentalValue, continous, true, WriteToFileQuality, WriteToFileOrderBook, WriteToFileHFTActivity));
    }
    
    
    for (i=0;i<1; i++)
		try {
			sim.addNewAgent(new QuoteStuffing((i>nbHFT/2?"HFTTrendFollower_" + i:"HFTLiquidity_" + i) ,50 + Random.nextInt(150),initPrice, true, taxe, cancelTime, 
					continous, fundamentalValue, WriteToFileQuality, WriteToFileHFTActivity));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    
    ArrayList<long[]> listPrices = new ArrayList();
    long[] price = new long[nbOrderBooks];
    int[] quantityFund = new int[nbOrderBooks];
    for (i = 0; i < nbOrderBooks; i++){
    	price[i]=initPrice;
    	quantityFund[i] = 200+Random.nextInt(600);
    }
    listPrices.add(price);
    
    for (i=0;i<0; i++)
    	sim.addNewAgent(new Fundamentalist("Fundamentalist_"+i,500000 + Random.nextInt(500000),listPrices, quantityFund, fundamentalValue, continous));
   

    
    sim.run(Day.createEuroNEXT(openning, continous, closing), nbDays);
        
	//sim.market.printState();
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
