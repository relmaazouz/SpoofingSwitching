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

public class PhysicaA extends ModerateAgent{
	//long fundamentalValue = 4400 + (Random.nextDouble()>0.5?Random.nextInt(40):-Random.nextInt(40));
	
	int Frequency;
	long InitialPrice;
	int rounds;
	boolean shortselling;
	long[] fundamentalValue;
	int memorySpan;
	double alpha;
	double beta;
	double delta;
	long Expectation;
	ArrayList Wealth;
	long priceBuy;
	double NVAT;
	WriteToFileBidAskSpread WriteToFileQuality;
	int rejectToTrade;

	public PhysicaA(String name,long cash, long InitialPrice, 
			int initQuty, long[] fundamentalValue, int continous, WriteToFileBidAskSpread WriteToFileQuality){
		super(name,cash);
		this.fundamentalValue = fundamentalValue;
		this.InitialPrice = InitialPrice;
		this.setInvest("ob", initQuty);
		alpha = (double)Random.nextInt(50)/100;
		delta = Random.nextInt(100)*0.0001;
		//alpha = 0.5;
		beta = 0.5 + (double)Random.nextInt(50)/100;
		//beta =0.5;
		//this.memorySpan = 1+Random.nextInt(5);
		this.memorySpan = 1+Random.nextInt(180);
		if(this.name.split("_")[0].equals("Fundamentalist")) 
			this.Frequency=1+Random.nextInt(100);
		else this.Frequency=1+Random.nextInt(10);
		if(this.name.equals("Fundamentalist_2")) this.Frequency=1;
		//this.Frequency=1;
		this.Wealth = new ArrayList();
		this.Wealth.add(cash+InitialPrice*initQuty);
		this.priceBuy = InitialPrice;
		rounds = -1;
		this.rejectToTrade = 0;
		
		this.WriteToFileQuality=WriteToFileQuality;
		if(this.name.equals("Fundamentalist_0")){
			
			try { // write profitable sell
				//WriteToFileQuality.Write("rounds" + "\t" + "name" + "\t" + "Wealth" + "\t" +  "Invest" + "\t" + 
				//		"cash" + "\t" + "nbOfOrdersSent" + "\t" + "nbOfPricesFixed");
				
				WriteToFileQuality.Write("rounds" + "\t" + "name" + "\t" + "Wealth" + "\t" +  "Invest" + "\t" + 
						"cash" + "\t" + "NVAT" + "\t" + "TaxPayment" + "\t" + "Reject");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//this.Frequency=1;
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
		long price=fundamentalValue[rounds];
		this.Wealth.add(this.getWealth());
		
		
		OrderBook ob = market.orderBooks.get(obName);
		
	/*	if(day.currentPeriod().currentTick() == 999 && day.currentPeriod==1) { //if((price- this.priceBuy)*maxTax>0) {
			try { // write profitable sell
				WriteToFileQuality.Write(rounds + "\t" + this.name + "\t" + this.Wealth.get(this.Wealth.size()-1) + "\t" +  this.getInvest(obName) + "\t" + 
						this.cash + "\t" + this.numberOfOrdersSent + "\t" + ob.numberOfPricesFixed);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			}//write to file if potTaxPayement >0
		
		*/
		
		if (day.currentPeriod().currentTick() % Frequency == 0 && day.currentPeriod==0){ //Opening: Send only limit orders
			    dir  = (Math.random()>0.5?LimitOrder.BID:LimitOrder.ASK);
	  			if(Math.random()>0.5){
	  				dir = LimitOrder.ASK;
  					quty = 1;
	  				price = this.fundamentalValue[rounds] - Random.nextInt(200) + Random.nextInt(200);
	  			}
	  			else{
	  				dir = LimitOrder.BID;
	  				price = this.fundamentalValue[rounds] - Random.nextInt(200) + Random.nextInt(200);
	  				quty = 1;
	  			} 
	  			//if(price < this.fundamentalValue[rounds]*0.8 ||  price > this.fundamentalValue[rounds]*1.2) return null;
	  			cancelPengingOrders(ob);
	  			return new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));	          
	          
		}//Opening: Send only limit orders
		
		
		if (day.currentPeriod().currentTick() % Frequency == 0 && day.currentPeriod==1) // Continuous trading
	    {
		//if(ob.numberOfPricesFixed>this.memorySpan) {	 
		cancelPengingOrders(ob);
		//this.Expectation = ob.lastFixedPrice.price - (long)(beta*(this.Expectation - ob.lastPrices.get(0).price))+ (long)(alpha*(ob.lastPrices.get(0).price - ob.lastPrices.get(memorySpan).price))+
		//				+ (long)(beta*(this.fundamentalValue[rounds]-this.fundamentalValue[rounds-1]));
		//System.out.println(this.name + "\t" + this.Expectation + "\t" + this.alpha + "\t" + this.beta + "\t" + this.memorySpan);
		
//===Flash Crash==============================		
		if(this.name.equals("Fundamentalist_2")){
			//if(day.currentPeriod().currentTick()%100==0){
			if(day.currentPeriod().currentTick()==100){
				quty=200;
				dir = LimitOrder.BID;
				cancelPengingOrders(ob);
				return new MarketOrder(obName,""+myId, dir, quty);
			}
			//else if (day.currentPeriod().currentTick()==(crashTick+1)) 
			//	cancelPengingOrders(ob);
		
		}
		
		
		if(this.name.split("_")[0].equals("Fundamentalist")) {//Fundamentalist
			this.Expectation = this.fundamentalValue[rounds] - Random.nextInt(200) + Random.nextInt(200);
			if(this.Expectation > this.fundamentalValue[rounds]*1.5 || this.Expectation < this.fundamentalValue[rounds]*0.5) 
					this.Expectation = this.fundamentalValue[rounds] - Random.nextInt(200) + Random.nextInt(200);
			if(ob.numberOfPricesFixed>0) 
				dir=(this.Expectation > ob.lastPrices.get(0).price?LimitOrder.BID:LimitOrder.ASK);
			else return null;
			
		}//Fundamentalist
		else { //if HFT
			double diff = 0;
			if (ob.numberOfPricesFixed > memorySpan){
				diff= 100*(ob.lastPrices.get(0).price - ob.lastPrices.get(memorySpan).price)/(double)ob.lastPrices.get(memorySpan).price;
				//System.out.println(ob.lastFixedPrice.price +"\t" + ob.lastPrices.get(0).price+"\t" + ob.lastPrices.get(1).price);
			}
			else 
				diff = (Random.nextDouble()>0.5?1:-1)*delta;
			
			
			if (diff > 0 &&  Math.abs(diff)>= delta) dir = LimitOrder.BID;
			else if(diff < 0 && Math.abs(diff)>= delta) dir = LimitOrder.ASK;
			else return null;
			
			if(ob.numberOfPricesFixed>0)
				this.Expectation = (long)(ob.lastPrices.get(0).price*(1+((dir == LimitOrder.ASK?1:-1))*delta));
			else return null;
		
		}//HFT
		
			
			
	
		
		if(dir==LimitOrder.BID) {//Traitons d'abord le cas d'achat
			if(ob.bid.isEmpty()) price = this.Expectation;
			else if (ob.bid.first().price<this.Expectation) price = ob.bid.first().price + Random.nextInt((int)(this.Expectation-ob.bid.first().price)) ;
			else price = this.Expectation;
			this.Wealth.add(price*(this.getInvest(obName)+1)+this.cash-price*1);
			//if(this.cash/price < 1) return null; 
			this.priceBuy = price; 
			
			
		}
		
		if(dir==LimitOrder.ASK) {//Traitons d'abord le cas de vente
			if(ob.ask.isEmpty()) price = this.Expectation;
			else if (ob.ask.first().price>this.Expectation) price = this.Expectation + Random.nextInt((int)(ob.ask.first().price - this.Expectation)) ;
			else price = this.Expectation;
			
			double var = 1;
			if(price!=ob.lastPrices.get(0).price) var =  Math.abs((double)(this.fundamentalValue[rounds] - this.fundamentalValue[rounds-1]) / (double)(price - ob.lastPrices.get(0).price));
			
			
			//System.out.println(this.fundamentalValue[rounds] + ";" + this.fundamentalValue[rounds-1]+ ";" + price + ";" + ob.lastPrices.get(0).price + ";" + ob.lastFixedPrice.price + ";" +var);
			/*if(var<0.1) NVAT = 0.75;
			else if (var>=0.11 && var <=0.66) NVAT = 0.25;
			else if (var>=0.67 && var <1) NVAT = 0.15;
			else NVAT = 0.0;
			NVAT = 0;
			double maxTax = NVAT;
			long taxPayment = (long)((price - this.priceBuy)*maxTax);
			price+=taxPayment; */
			
			long[] p = new long [4];
			double[] realizedProfit = new double [4]; 
			double[] NVAT = new double[4];
			NVAT[0] = 0.75;
			NVAT[1] = 0.25;
			NVAT[2] = 0.15;
			NVAT[3] = 0.0;
			
			 p[0] = (long)(((this.fundamentalValue[rounds] - this.fundamentalValue[rounds-1])) /0.1)  + ob.lastPrices.get(0).price;
			if(p[0] > this.priceBuy) realizedProfit[0] = (p[0] - this.priceBuy)*(1-NVAT[0]);
			 p[1] = (long)(((this.fundamentalValue[rounds] - this.fundamentalValue[rounds-1])) /0.66)  + ob.lastPrices.get(0).price;
			if(p[1] > this.priceBuy) realizedProfit[1] = (p[1] - this.priceBuy)*(1-NVAT[1]);
			 p[2] = (long)(((this.fundamentalValue[rounds] - this.fundamentalValue[rounds-1])) /1)  + ob.lastPrices.get(0).price;
			if(p[2] > this.priceBuy) realizedProfit[2] = (p[2] - this.priceBuy)*(1-NVAT[2]);
			 p[3] = (long)(((this.fundamentalValue[rounds] - this.fundamentalValue[rounds-1])) /1.01)  + ob.lastPrices.get(0).price;
			if(p[3] > this.priceBuy) realizedProfit[3] = (p[3] - this.priceBuy)*(1-NVAT[3]);
			
			double maxProfit = 0;
			double maxTax = 0;
			for (int i=0; i<4; i++)
				if(realizedProfit[i]>maxProfit) {
					maxProfit = realizedProfit[i];
					price = p[i];
					maxTax = NVAT[i];
				}
			long taxPayment = (long)((price - this.priceBuy)*maxTax); 
					
			if (price < this.Expectation || price<this.priceBuy) {
				this.rejectToTrade++;
				return null;
			}
			
	//==Print Tax Payment===========
			if((price- this.priceBuy)*maxTax>0) {
				try { // write profitable sell
					WriteToFileQuality.Write(rounds + "\t" + this.name + "\t" + this.getWealth() + "\t" +  this.getInvest(obName) + "\t" + 
							this.cash + "\t" +  maxTax + "\t" + (price- this.priceBuy)*maxTax*0.01 + "\t" + this.rejectToTrade);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				}//write to file if potTaxPayement >0
			
			
			
			/*double var = 1;
			if(price!=ob.lastPrices.get(0).price) var = Math.abs((this.fundamentalValue[rounds] - this.fundamentalValue[rounds-1]) / (price - ob.lastPrices.get(0).price));
			if(var<0.1) NVAT = 0.75;
			else if (var>=0.11 && var <=0.66) NVAT = 0.25;
			else if (var>=0.67 && var <1) NVAT = 0.15;
			else NVAT = 0;
			
			long potentialTaxPayment = (long)((price-priceBuy)*NVAT); //potential profit from trade
			if(this.Expectation - this.priceBuy < potentialTaxPayment) { 
				this.rejectToTrade++;
				return null;
			}
			
			if(potentialTaxPayment>0) {
			try { // write profitable sell
				WriteToFileQuality.Write(rounds + "\t" + this.name + "\t" + this.getWealth() + "\t" +  this.getInvest(obName) + "\t" + 
						this.cash + "\t" + this.Expectation  + "\t" + this.priceBuy + "\t" + NVAT + "\t" + potentialTaxPayment + "\t" + this.rejectToTrade);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			}//write to file if potTaxPayement >0
		
		} */ //enough memory
			
		
		
		//} //enough memory
		//else return null;
		
		//if(price < this.fundamentalValue[rounds]*0.8 && price > this.fundamentalValue[rounds]*1.2) return null;
		this.Wealth.add(price*(this.getInvest(obName)-1)+this.cash+price*1-taxPayment);	
		quty = 1;
		cancelPengingOrders(ob);
		o = new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));	
		
		return o;
		} //there is enough for memory span
		else {
			dir  = (Math.random()>0.5?LimitOrder.BID:LimitOrder.ASK);
  			if(Math.random()>0.5){
  				dir = LimitOrder.ASK;
			    quty = 1;
  				price = this.fundamentalValue[rounds] - Random.nextInt(200) + Random.nextInt(200);
  				this.Wealth.add(price*(this.getInvest(obName)-1)+this.cash+price*1);	
  			}
  			else{
  				dir = LimitOrder.BID;
  				price = this.fundamentalValue[rounds] - Random.nextInt(200) + Random.nextInt(200);
  				quty = 1;
  			} 
  			//if(price < this.fundamentalValue[rounds]*0.8 || price > this.fundamentalValue[rounds]*1.2) return null;
  			this.Wealth.add(price*(this.getInvest(obName)+1)+this.cash-price*1);
  			cancelPengingOrders(ob);
  			return new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));	          
          
			}//there is no enough memory
	    } // frequency
		else return null;
	}
}
	


