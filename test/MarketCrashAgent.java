package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import test.WriteToFileBidAskSpread;

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

public class MarketCrashAgent extends ModerateAgent {
	private int nassets; 
	long [][] PriceList;
	private int CurrentDay;
	boolean LimitsAreDetermined;
	int Frequency;
	boolean shortselling;
	boolean shortrestriction;
	WriteToFileBidAskSpread WriteToFile;
	
	public MarketCrashAgent(String name,long cash, ArrayList<long[]> InitialPrice, boolean shortselling, boolean shortrestriction, WriteToFileBidAskSpread WriteToFile){
		super(name,cash);
		int i;
		nassets = InitialPrice.get(0).length;
		PriceList= new long[2][nassets];
		Frequency = 1+Random.nextInt(100);
		this.shortselling = shortselling;
		this.shortrestriction = shortrestriction; // short restriction after flash crash
		if(this.name.equals("MarketCrash_2")) this.Frequency=1;
		//Frequency =1;
		
		int indx = 1+Random.nextInt(1000);
		for (i=0; i<nassets; i++){
			PriceList[0][i] = InitialPrice.get(0)[0]+indx;
			PriceList[1][i] = InitialPrice.get(0)[0]-indx;
		}
		this.CurrentDay=1;
		this.LimitsAreDetermined = false;
		
		if (Random.nextDouble() >0.7){
			this.cash = 2000000+Random.nextInt(2000000);
			this.setInvest("ob0", 2000+Random.nextInt(1000));
		}	
		else{ 
			this.cash = 1000000+Random.nextInt(1000000);
			this.setInvest("ob0", 150+Random.nextInt(100));
		}
		
		if(this.name.equals("MarketCrash_2")){
			this.WriteToFile=WriteToFile;
			this.cash = 2000000+Random.nextInt(2000000);
			this.setInvest("ob0", 30000);
		}
		
	}	
	
    void setFrozenInvest(String name, int value)
    {
	frozenInvest.put(name,value); // autoboxing java 1.5
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
    	
    
    // public LimitOrder decide(String obName,int day, int tick)
    public Order decide(String obName, Day day)
    {

    Order o = null;
	char dir;
	int quty=0;
	int multiple;
	
	OrderBook ob = market.orderBooks.get(obName);
	
    int IdOrderBook = Integer.parseInt(obName.substring(obName.lastIndexOf("b")+1));
    
  //  if( day.currentPeriod==1 && this.name.equals("MarketCrash_2") && ob.ask.size()>0 && ob.bid.size()>0 && ob.numberOfPricesFixed>0)
  //  	System.out.println(day.currentPeriod().currentTick()+ "\t" + ob.ask.first().price +"\t" + ob.ask.first().quantity +"\t" + ob.bid.first().price +"\t"+ob.bid.first().quantity + "\t" + ob.lastFixedPrice.price + "\t" + ob.lastFixedPrice.dir +"\t" +ob.lastFixedPrice.quantity);
    
    
    if( day.currentPeriod==1 && this.name.equals("MarketCrash_2") && ob.ask.size()>0 && ob.bid.size()>0 && ob.numberOfPricesFixed>0){
    	String str=day.currentPeriod().currentTick()+ "\t" + ob.ask.first().price +"\t" 
    			+ ob.ask.first().quantity +"\t" + ob.bid.first().price +"\t"
    			+ob.bid.first().quantity + "\t" + ob.lastFixedPrice.price + "\t" 
    			+ ob.lastFixedPrice.dir +"\t" +ob.lastFixedPrice.quantity;
    	try {
			WriteToFile.Write(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    }
    
    
	if (day.currentPeriod().currentTick() % Frequency == 0) // on rééquilibre
    {
	if(day.currentPeriod().currentTick()==2000 && this.name.equals("MarketCrash_2")){
		//quty=10000;
		quty = this.getInvest(obName);
		dir = LimitOrder.ASK;
		return new MarketOrder(obName, myId + "", dir, quty);
	}
	else if (this.name.equals("MarketCrash_2")) return null;
	
	dir  = (Math.random()>0.5?LimitOrder.BID:LimitOrder.ASK);
	
	long price;
		if (ob.numberOfPricesFixed == 0){ // there is no fixed prices
					if(dir == LimitOrder.ASK && ob.ask.size()>0) // ASK
							price = (long)(ob.ask.first().price - Random.nextInt(10));
					else
						price = PriceList[1][IdOrderBook] + (int) (Math.random() * (PriceList[0][IdOrderBook] - PriceList[1][IdOrderBook]));

					
					if (dir == LimitOrder.BID && ob.bid.size()>0) //BID
							price = (long)(ob.bid.first().price + Random.nextInt(10));
					else
						price = PriceList[1][IdOrderBook] + (int) (Math.random() * (PriceList[0][IdOrderBook] - PriceList[1][IdOrderBook]));
		}		
		else{
				if (dir == LimitOrder.ASK) // i am a Seller : Vendeur
				{
					if (ob.ask.size()==0)
						price = ob.lastFixedPrice.price;
					else
						price = (long)(ob.ask.first().price - Random.nextInt(10));
				}
				else // i am a Buyer : Acheteur
					{
					if (ob.bid.size()==0)
						price = ob.lastFixedPrice.price;
					else
						price = (long)(ob.bid.first().price +Random.nextInt(10));
					}
	}

	if((price<PriceList[1][IdOrderBook]) || (price>PriceList[0][IdOrderBook]))	
		price = PriceList[1][IdOrderBook] + (int) (Math.random() * (PriceList[0][IdOrderBook] - PriceList[1][IdOrderBook]));	

	if(!shortselling) multiple =1; else multiple = 2;	
    if(shortrestriction && day.currentPeriod().currentTick()>=2000) multiple=1;
    
    
    
if (dir == LimitOrder.ASK){
	if(multiple==1){
		if(this.getInvest(obName)>0)
			quty=1+Random.nextInt(multiple*this.getInvest(obName));
		else return null;
	}
	else{
		long wealth = this.getInvest(obName)*price+this.cash;
		double assetsweight = this.getInvest(obName)*(double)price/(double)wealth;
		if (assetsweight>(1-multiple)){
		int potentialquty  = (int)((assetsweight-(1-multiple))*wealth/(2*price));
		if (potentialquty>1) quty = Random.nextInt(potentialquty); else return null;
		if (quty<1) return null;
		}
	}
}
	else{
		int potentialquty = (int) (this.cash/price);
		if (potentialquty>1) quty = Random.nextInt(potentialquty); else return null;
		if (quty<1) return null;
	}
	

	cancelPengingOrders(ob);
	if (quty==0) return null;
	
	o = new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));
  
	  } // send limit order
	return o;
    }
    
    public static void main(String args[])
    {
	
    	for(int ext=1; ext<=100; ext++){
    	System.out.println(ext);
    	Simulation sim = new MonothreadedSimulation();
      	sim.market.setFixingPeriod (MarketPlace.CONTINUOUS); // ou FIX
        sim.market.logType=MarketPlace.LONG; // ou SHORT
      //  sim.setLogger(new Logger(System.out));
        FilteredLogger fl = new FilteredLogger("Something"+"_"+ext);
        fl.orders = true;
        fl.prices = true;
        fl.agents = false;
        fl.infos = false;
        fl.commands = false; 
        sim.setLogger(fl);        
        int nbOrderBooks = 1;
        ArrayList<long[]> listPrices = new ArrayList();
        long[] price = new long[nbOrderBooks];
        int[] quantity = new int[nbOrderBooks];
        int nbDays=1;

	     
    
        
    for (int i = 0; i < nbOrderBooks; i++){
   		 price[i]=2600;
   		 quantity[i] = Random.nextInt(200);
   	 }
   	 listPrices.add(price);
        
        // create orderbooks
   	int i;
	
   	WriteToFileBidAskSpread WriteToFile=null;
	try {
		WriteToFile = new WriteToFileBidAskSpread(0, 0, ext);
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
   	
   	for (i=0; i<nbOrderBooks; i++)
            sim.addNewOrderBook("ob"+i);
	        // set agents
    for (i=0;i<100; i++){
            sim.addNewAgent(new MarketCrashAgent("MarketCrash_"+i,1000000,listPrices, true, false, WriteToFile));
        	//sim.addNewAgent(new NaiveMarket("naive"+i,1000000,listPrices, quantity, 1+Random.nextInt(20)));
        }      
        
        for (i=0;i<50; i++){
        	sim.addNewAgent(new MomentumAgent("Momentum_"+i,1000000,10, Random.nextInt(40), true, false));
        	sim.addNewAgent(new MovingAverageAgent("MovingAverage_"+i,1000000,10, Random.nextInt(40), true, false));
        	//sim.addNewAgent(new PeriodicAgent("Periodic_"+i,1000000,10, 10));
        	sim.addNewAgent(new RsiAgent("Rsi_"+i,1000000, 14+Random.nextInt(20), Random.nextInt(30), true, false));
        	//sim.addNewAgent(new SimpleTrendFollower("Trend_"+i));
        }
        sim.run(Day.createEuroNEXT(10, 4000, 10), nbDays);
        
	//sim.market.printState();
	sim.market.close();
	
	try {
		WriteToFile.Close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	
	
    }
   } // Extensive analysis
}
