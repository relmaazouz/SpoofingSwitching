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

public class PhysicaAECOMOD extends ModerateAgent{
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
	WriteToFileBidAskSpread WriteToFileSwitching;
	int rejectToTrade;
	int[][] mimicMatrix;
	double[] profitVector;
	boolean[] isFundamentalist;
	int nbAgents;

	public PhysicaAECOMOD(String name,long cash, long InitialPrice, 
			int initQuty, long[] fundamentalValue, int continous, 
			WriteToFileBidAskSpread WriteToFileQuality, WriteToFileBidAskSpread WriteToFileSwitching, 
			int[][] mimicMatrix, double[] profitVector, boolean[] isFundamentalist){
		super(name,cash);
		this.fundamentalValue = fundamentalValue;
		this.InitialPrice = InitialPrice;
		this.mimicMatrix = mimicMatrix;
		this.profitVector = profitVector;
		this.isFundamentalist = isFundamentalist;
		nbAgents = this.isFundamentalist.length;
		this.setInvest("ob", initQuty);
		alpha = (double)Random.nextInt(50)/100;
		delta = Random.nextInt(10)*0.0001;
		//alpha = 0.5;
		beta = 0.5 + (double)Random.nextInt(50)/100;
		//beta =0.5;
		//this.memorySpan = 1+Random.nextInt(5);
		this.memorySpan = 1+Random.nextInt(180);
		
		if(this.name.split("_")[0].equals("Fundamentalist")) 
			this.Frequency=1+Random.nextInt(100);
		else this.Frequency=1+Random.nextInt(100);
		if(this.name.equals("Fundamentalist_2")) this.Frequency=1;
	
		//this.Frequency=1;
		this.Wealth = new ArrayList();
		this.Wealth.add(cash+InitialPrice*initQuty);
		this.priceBuy = InitialPrice;
		rounds = -1;
		this.rejectToTrade = 0;
		
		this.WriteToFileQuality=WriteToFileQuality;
		this.WriteToFileSwitching = WriteToFileSwitching;
		if(this.name.equals("Fundamentalist_0")){
			
			try { // write profitable sell
				WriteToFileSwitching.Write("Tick" + "\t" + "name" + "\t" + "isFund" + "\t" +  "avgReturnFundamentalist" + "\t" + 
						"avgReturnHFT" + "\t" + "isFundAfter" + "\t" + "nbFundamentalist" + "\t" + "nbHFT");

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
		int i;
		long taxPayment = 0;
		
		rounds++;
		long price=fundamentalValue[rounds];
		this.Wealth.add(this.getWealth());
		
		OrderBook ob = market.orderBooks.get(obName);

		//-----At opening trade like fundamentalist
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
		
		
		else if (day.currentPeriod().currentTick() % Frequency == 0 && day.currentPeriod==1) // Continuous trading
	    {
			if(ob.numberOfPricesFixed>1) 
				if(!this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])] ) 
					profitVector[Integer.parseInt(this.name.split("_")[1])] = Math.abs((double)((ob.lastPrices.get(0).price - price)/(double)price));
				else 
					profitVector[Integer.parseInt(this.name.split("_")[1])] = Math.abs((double)((fundamentalValue[rounds] - price)/(double)price));
	
			//-----SWITCHING----------------------------------	
			if(day.currentPeriod().currentTick()>11) {
				//Compute the average profit of neighbors-fundamentalist of a given agent
				double avgReturnFundamentalist = 0;
				double avgReturnHFT = 0;
				int nbNeighborsFundamentalist = 0;
				int nbNeighborsHFT= 0;
				int nbFundamentalist= 0;
				int nbHFT=0;
				for (i=0; i<nbAgents; i++) {
					if(this.isFundamentalist[i]) nbFundamentalist+=1; else nbHFT+=1;
					if(this.mimicMatrix[Integer.parseInt(this.name.split("_")[1])][i]==1)
						if(isFundamentalist[i]) { //Neighbor is fundamentalist
							avgReturnFundamentalist+= profitVector[i];
							nbNeighborsFundamentalist+=1;
						}
						else { //Neighbor is HFT
							avgReturnHFT+= profitVector[i];
							nbNeighborsHFT+=1;
						}
				}//Nb of agents
					//If fundamentalist on average is more profitable
				if(nbNeighborsFundamentalist!=0) avgReturnFundamentalist = avgReturnFundamentalist/nbNeighborsFundamentalist; else avgReturnFundamentalist = 0;
				if(nbNeighborsHFT!=0) avgReturnHFT = avgReturnHFT/nbNeighborsHFT; else avgReturnHFT = 0 ;
				//System.out.print(day.currentPeriod().currentTick() + "\t" + this.name + "\t" + this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])] + "\t");
				String str = day.currentPeriod().currentTick() + "\t" + this.name + "\t" + this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])] + "\t";
				if (avgReturnFundamentalist > avgReturnHFT && avgReturnHFT!=0) {
					if(Random.nextDouble() < Math.exp(avgReturnFundamentalist)/(Math.exp(avgReturnFundamentalist)+Math.exp(avgReturnHFT)))
					//if(Random.nextDouble() < 0.7)
						this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])]=true;
					else this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])]=false;
				}
				else if (avgReturnFundamentalist < avgReturnHFT && avgReturnFundamentalist!=0){
					if(Random.nextDouble() < Math.exp(avgReturnHFT)/(Math.exp(avgReturnFundamentalist)+Math.exp(avgReturnHFT)))
					//if(Random.nextDouble() < 0.7)	
						this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])]=false;
					else this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])]=true;
				}
				
				if(this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])]) 
					this.Frequency = 1 + Random.nextInt(100);
				else 
					this.Frequency = 1 + Random.nextInt(100);
				
				//System.out.println(avgReturnFundamentalist + "\t" + avgReturnHFT + "\t" +  
				//               this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])] +
				//               "\t" + nbFundamentalist + "\t" + nbHFT);
			  
			     str = str + "" +avgReturnFundamentalist + "\t" + avgReturnHFT + "\t" +  
							               this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])] +
							               "\t" + nbFundamentalist + "\t" + nbHFT;
				try { // write profitable sell
					WriteToFileSwitching.Write(str);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			
			
			} //SWITCHING
			//---------------------------------------
		
			
			
			//Fundamentalist
			if(isFundamentalist[Integer.parseInt(this.name.split("_")[1])]) {
				//this.Expectation =  ob.lastPrices.get(0).price + (long)Random.nextInt(10)*(this.fundamentalValue[rounds] - ob.lastPrices.get(0).price);
				this.Expectation = this.fundamentalValue[rounds] + (Random.nextDouble() > 0.5?1:-1)*Random.nextInt(500);
				if(ob.numberOfPricesFixed>0) 
					dir=(this.Expectation > ob.lastPrices.get(0).price?LimitOrder.BID:LimitOrder.ASK);
				else return null;
			}//Fundamentalist
			else { //if HFT
				if(ob.numberOfPricesFixed > 0)
					//this.Expectation =  ob.lastPrices.get(0).price + (long)Random.nextInt(2)*(ob.lastPrices.get(0).price - ob.lastPrices.get(1).price);
					//this.Expectation =  ob.lastPrices.get(0).price + (long)(Random.nextInt(2)*(ob.lastPrices.get(0).price - ob.lastPrices.get(memorySpan).price));
					//this.Expectation = this.fundamentalValue[rounds]- Random.nextInt(1000) + Random.nextInt(1000);
					this.Expectation =  ob.lastPrices.get(0).price + (Random.nextDouble() > 0.5?1:-1)*Random.nextInt(500);	
				//this.Expectation = (long)(ob.lastPrices.get(0).price*(1+((dir == LimitOrder.ASK?1:-1))*delta));
				//else return null;
				if(ob.numberOfPricesFixed>0) 
					dir=(this.Expectation > ob.lastPrices.get(0).price?LimitOrder.BID:LimitOrder.ASK);
				else return null;
			}//HFT
	//--------------------Expectations for both categories of traders	
			
			
	//Do not submit at expectations, check the current state of the order book before
		
		if(dir==LimitOrder.BID) {//Traitons d'abord le cas d'achat
			if(ob.bid.isEmpty()) price = this.Expectation;
			else if (ob.bid.first().price<this.Expectation) price = ob.bid.first().price + Random.nextInt((int)(this.Expectation-ob.bid.first().price)) ;
			else price = this.Expectation;
			if(this.cash/price < 1) return null; 
			//price = this.Expectation;
			
			this.Wealth.add(price*(this.getInvest(obName)+1)+this.cash-price*1);
			this.priceBuy = price; 
		}
		
		else {//Traitons maintenant le cas de vente
			if(ob.ask.isEmpty()) 
				price = this.Expectation;
			else if (ob.ask.first().price>this.Expectation) 
				price = this.Expectation + Random.nextInt((int)(ob.ask.first().price - this.Expectation)) ;
			else 
				price = this.Expectation;
			
			//price = this.Expectation;
			double var = 1;
			if(price!=ob.lastPrices.get(0).price) var =  Math.abs((double)(this.fundamentalValue[rounds] - this.fundamentalValue[rounds-1]) / (double)(price - ob.lastPrices.get(0).price));
			
			
			//System.out.println(this.fundamentalValue[rounds] + ";" + this.fundamentalValue[rounds-1]+ ";" + price + ";" + ob.lastPrices.get(0).price + ";" + ob.lastFixedPrice.price + ";" +var);
			if(var<0.1) NVAT = 0.75;
			else if (var>=0.11 && var <=0.66) NVAT = 0.25;
			else if (var>=0.67 && var <1) NVAT = 0.15;
			else NVAT = 0.0;
			//NVAT = 0;
			double maxTax = NVAT;
			
			
			//taxPayment = (long)((price - this.priceBuy)*maxTax);
			//price+=taxPayment; 
			
			/*long[] p = new long [4];
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
			for (i=0; i<4; i++)
				if(realizedProfit[i]>maxProfit) {
					maxProfit = realizedProfit[i];
					//price = p[i];
					maxTax = NVAT[i];
			}
			
			
		//	maxTax = 0; //If the simulation is untaxed
		*/	
					
			if (price<this.priceBuy || this.getInvest(ob.obName)==0) {
				this.rejectToTrade++;
				return null;
			}
			else 
				taxPayment = (long)((price - this.priceBuy)*maxTax); 
			
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

		
			
			
		
		} 
		quty = 1;
		//price = this.fundamentalValue[rounds]- Random.nextInt(500) + Random.nextInt(500);
		cancelPengingOrders(ob);
		if(ob.numberOfPricesFixed>1) 
			if(!this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])] ) 
				profitVector[Integer.parseInt(this.name.split("_")[1])] = Math.abs((double)((ob.lastPrices.get(0).price - price-taxPayment)/(double)price));
			else 
				profitVector[Integer.parseInt(this.name.split("_")[1])] = Math.abs((double)((fundamentalValue[rounds] - price-taxPayment)/(double)price));

		o = new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));	
		return o;
	    } // frequency
		else return null;
	}
}
	


