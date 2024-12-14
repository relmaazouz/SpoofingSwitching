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

public class HFT extends ModerateAgent{
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
	
	
	public HFT(String name,long cash, long InitialPrice, boolean shortselling, double cancelTaxe, int cancelTime,
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
		if (Random.nextDouble() >0.7){
			this.cash = 1000000+Random.nextInt(1000000);
			this.setInvest("ob0", 500+Random.nextInt(2000));
		}	
		else{ 
			this.cash = 500000+Random.nextInt(500000);
			this.setInvest("ob0", 50+Random.nextInt(150));
		}	

		int indx = 10+Random.nextInt(9)*10;
		PriceList[0] = fundamentalValue[0] + indx;
		PriceList[1] = fundamentalValue[0] - indx;
		
		this.CurrentDay=1;
		this.LimitsAreDetermined = false;
		this.Variance = Random.nextInt(100)*0.0001;
		this.memorySpan = 1+ Random.nextInt(180);
		
	}	

	int cancelPengingOrders(OrderBook ob, int round, double taxe, long newprice){
		//return 0 if cancellation can't be realized and 1 it's interesting to cancel
		int time=100;
		//prevRound.add(round);
		this.cancelTaxe=taxe;
		int n = prevRound.size();
		if(n>1) 
			time = round-prevRound.get(n-1);
		else return 0;
		
	  	ArrayList<LimitOrder> toDestroy = new ArrayList<LimitOrder>();
	    for (LimitOrder lo : ob.ask)
	  	  	if (lo.sender == this)
	  	  		toDestroy.add(lo);
	    for (LimitOrder lo2 : toDestroy){
	    	if(time<cancelTime){
	    		//Check the advantage of reballancing compared to transaction tax
	    		if(Math.floor((lo2.price*lo2.quantity)*cancelTaxe) > (newprice-lo2.price)*lo2.quantity) return 0;
	    		this.cash =(long) (this.cash-Math.floor((lo2.price*lo2.quantity)*cancelTaxe));
	    	}
	    	
    	
	    	ob.ask.remove(lo2);
	    	//System.out.println(round + "\t" + "cancel" + "\t" + -1  +"\t"+ this.name + "\t" + lo2.direction + "\t" + lo2.quantity + "\t" + lo2.price);
	    	this.nbCancelled++;
	    	return 1;
	    }
	       
	    toDestroy = new ArrayList<LimitOrder>();
	    for (LimitOrder lo : ob.bid)
	  	  	if (lo.sender == this)
	  	  		toDestroy.add(lo);
	    for (LimitOrder lo2 : toDestroy){
	    	if(time<cancelTime)
	    		//Check the advantage of rebalancing compared to transaction taxe
	    		if(Math.floor((lo2.price*lo2.quantity)*cancelTaxe) > (lo2.price-newprice)*lo2.quantity) return 0;
	    		this.cash =(long) (this.cash-Math.floor((lo2.price*lo2.quantity)*cancelTaxe));

	    		ob.bid.remove(lo2);
	    		//System.out.println(round + "\t" + "cancel" + "\t" + -1  +"\t"+ this.name + "\t" + lo2.direction + "\t" + lo2.quantity + "\t" + lo2.price);
	    		this.nbCancelled++;
	  	  	return 1;
	    }

	    if(!this.shortselling) this.cash = 0;
	    
	    pendings.clear();
	    return 1;
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
    //int indx = 1+Random.nextInt(100);
    int indx = 10+Random.nextInt(9)*10;
	PriceList[0] = fundamentalValue[round] + indx;
	PriceList[1] = fundamentalValue[round] - indx;
    

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
	
	
	
	double diff = 0;
	if (ob.numberOfPricesFixed > memorySpan){
		diff= 100*(ob.lastPrices.get(0).price - ob.lastPrices.get(memorySpan).price)/(double)ob.lastPrices.get(memorySpan).price;
		//System.out.println(ob.lastFixedPrice.price +"\t" + ob.lastPrices.get(0).price+"\t" + ob.lastPrices.get(1).price);
	}
	else 
		diff = (Random.nextDouble()>0.5?1:-1)*Variance;
	
	
	if (diff > 0 &&  Math.abs(diff)>= Variance) dir = (this.trendFollower?LimitOrder.BID:LimitOrder.ASK);
	else if(diff < 0 && Math.abs(diff)>= Variance) dir = (this.trendFollower?LimitOrder.ASK:LimitOrder.BID);
	else return null;
	
	
	//if (day.currentPeriod().currentTick() % Frequency == 0 && day.currentPeriod==1)
	if (day.currentPeriod==1)
	{
			
	
	//LimitOrder o;
			
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
	
			
			//if(Random.nextDouble()>0.3)
			//	quty=300+Random.nextInt(100);
			//else
			//	quty=300+Random.nextInt(700);
			
			
			if(!shortselling) multiple =1; else multiple = 2;
			if (dir == LimitOrder.ASK){//ASK
				if(multiple == 1){
					if(this.getInvest(obName)>0)
						quty=1+Random.nextInt(multiple*this.getInvest(obName));
					else return null;
				}
				else{
					this.wealth = this.getInvest(obName)*price+this.cash;
					if(this.wealth==0) return null;
					double assetsweight = this.getInvest(obName)*price/this.wealth;
					if (assetsweight>(1-multiple)){
						if(price==0) return null;
						quty = (int)((assetsweight-(1-multiple))*this.wealth/price);
						if (quty<1) return null;
					}
				}
			}//ASK
			else{ // BID
				if(price==0) return null;
				int potentialquty = (int) (multiple*this.cash/price);
				if (potentialquty>1) quty = Random.nextInt(potentialquty); else return null;
				if (quty<1)
  					if(!shortselling) return null; else quty = 50+ Random.nextInt(150);
				}
			
			double taxe;
			if(round<(continous/2)) taxe = 0; else taxe = this.cancelTaxe;
			//taxe = this.cancelTaxe;
			if(cancelPengingOrders(ob, round, taxe, price)==0 && prevRound.size()>1){ 
				return null;
			}	
			
			if(quty==0) 
				return null;
			if (price<=0) price =1;
			

			//System.out.println(round + "\t" + "send" + "\t" + 1 +"\t" + taxe +"\t"+ this.name + "\t" + dir + "\t" + Math.abs(quty) + "\t" + Math.abs(price));
			if(Random.nextDouble() >0.9 && round>11)
				o = new MarketOrder(obName, myId + "", dir, Math.abs(quty));
			else{
				this.prevRound.add(round);
				o = new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));
					
				//System.out.println(o);
			}
   // } // send limit order
			return o;
    	}
	else return null;
    }
	
   // }	
}
