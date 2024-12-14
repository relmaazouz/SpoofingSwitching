package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import test.WriteToFileBidAskSpread;

import v13.CancelOrder;
import v13.Day;
import v13.LimitOrder;
import v13.MarketOrder;
import v13.Order;
import v13.OrderBook;
import v13.Random;
import v13.agents.ModerateAgent;

public class QuoteStuffing extends ModerateAgent{
	private int nassets; 
	long[] PriceList;
	private int CurrentDay;
	boolean LimitsAreDetermined;
	int Frequency;
	double Variance;
	int memorySpan;
	boolean shortselling;
	ArrayList<Integer> prevRound;
	int round;
	double cancelTaxe;
	int spread;
	long[] fundamentalValue;
	WriteToFileBidAskSpread WriteToFileQuality;
	WriteToFileBidAskSpread WriteToFileHFTActivity;
	int cancelTime;
	boolean trendFollower; // 1 is a trend follower, 0 is a contrarian
	int continous;
	long wealth;
	int nbCancelled;
	
	
	public QuoteStuffing(String name,long cash, long InitialPrice, boolean shortselling, double cancelTaxe, int cancelTime,
			int continous, long[] fundamentalValue, WriteToFileBidAskSpread WriteToFileQuality, 
			WriteToFileBidAskSpread WriteToFileHFTActivity) throws IOException{
		super(name,cash);
		nassets =1;
		PriceList= new long[2];
		this.shortselling = shortselling;
		this.prevRound = new ArrayList();
		this.round=0;
		this.cancelTaxe=cancelTaxe;
		this.cancelTime = cancelTime;
		this.continous = continous;
		this.nbCancelled = 0;
		
		//int n = Random.nextInt((int) Math.floor(18000.0*continous/306000000.0));
				int n = Random.nextInt(20);
				if(n>1)Frequency = 1+n; else Frequency =1; //Median time to cancellation is 18 seconds
				if(this.name.equals("HFTLiquidity_2")) this.Frequency = 1;
				
				//this.trendFollower = (Random.nextDouble()>0.5?true:false);
				if(this.name.split("_")[0].equals("HFTTrendFollower")) this.trendFollower = true; else this.trendFollower = false;
			

	this.WriteToFileHFTActivity=WriteToFileHFTActivity;	
		
		
		this.spread = 5;
		this.fundamentalValue = fundamentalValue;
		round=-1;
		this.cash = 500000+Random.nextInt(500000);
		this.setInvest("ob0", 50+Random.nextInt(150));
		
		int indx = 10+Random.nextInt(9)*10;
		PriceList[0] = fundamentalValue[0] + indx;
		PriceList[1] = fundamentalValue[0] - indx;
		
		this.CurrentDay=1;
		this.LimitsAreDetermined = false;
		this.Variance = Random.nextInt(100)*0.0001;
		this.memorySpan = 1+ Random.nextInt(180);
		
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
    	
	
	public Order decide(String obName, Day day)
    {

    Order o = null;
    long price;
    char dir;
    int quty = 0;
    int multiple;
    int crashTick = 50;
   // if(round>=(crashTick-1)) shortselling = false;
    
    OrderBook ob = market.orderBooks.get(obName);
    round++;
   

	//Track a wealth evolution
	
	if( day.currentPeriod==1 && ob.numberOfPricesFixed>0){
	try {
		this.wealth = this.getInvest(obName)*ob.lastFixedPrice.price +this.cash;
		WriteToFileHFTActivity.Write(round + "\t" + this.name + "\t" + this.wealth  + "\t" +  this.getInvest(obName) + "\t" + 
		ob.lastFixedPrice.price  + "\t" + this.cash + "\t" + this.nbCancelled);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	
	
	

	//if (day.currentPeriod().currentTick() % Frequency == 0 && day.currentPeriod==1)
	if (day.currentPeriod==1 && round> (continous/2))
	{
			
	
	//LimitOrder o;
		 dir = (Random.nextDouble()>0.5?LimitOrder.ASK:LimitOrder.BID);
			
			if (ob.numberOfPricesFixed == 0){
					if(dir == LimitOrder.ASK && ob.ask.size()>0)
						price = (long)(ob.ask.first().price + Random.nextInt(spread));
					else if (dir == LimitOrder.BID && ob.bid.size()>0)
						price = (long)(ob.bid.first().price - Random.nextInt(spread));
					else
						price = PriceList[1] + Random.nextInt(10)*(Random.nextDouble()>0.5?1:-1);
						//price = PriceList[1] + (int) (Math.random() * (PriceList[0] - PriceList[1]));
			}		
			else{
					if (dir == LimitOrder.ASK) // i am a Seller : Vendeur
					{
						if (ob.ask.size()==0)
							price = ob.lastFixedPrice.price + Random.nextInt(spread);
						else
							price = (long)(ob.ask.first().price + Random.nextInt(spread));
					}
					else // i am a Buyer : Acheteur
					{
						if (ob.bid.size()==0)
							price = ob.lastFixedPrice.price - Random.nextInt(spread);
						else
							price = (long)(ob.bid.first().price - Random.nextInt(spread));
					}
			}

			if((price<PriceList[1]) || (price>PriceList[0]))	
				price = PriceList[1] + Random.nextInt(10)*(Random.nextDouble()>0.5?1:-1);
				//price = PriceList[1] + (int) (Math.random() * (PriceList[0] - PriceList[1]));	
	
			
			if(Random.nextDouble()>0.3)
				quty=300+Random.nextInt(100);
			else
				quty=300+Random.nextInt(700);

			cancelPengingOrders(ob);
			o = new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));
			return o;
    	}
	else return null;
    }
	
   // }	
}
