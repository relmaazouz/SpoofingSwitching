package manipulations;

import java.io.IOException;
import java.util.ArrayList;

import v13.Day;
import v13.LimitOrder;
import v13.MarketOrder;
import v13.Order;
import v13.OrderBook;
import v13.Random;
import v13.agents.ModerateAgent;


public class PhysicaAECOMOD extends ModerateAgent{
	//long fundamentalValue = 4400 + (Random.nextDouble()>0.5?Random.nextInt(40):-Random.nextInt(40));
	
	int Frequency;
	long InitialPrice;
	int rounds;
	boolean shortselling;
	long[] fundamentalValue;
	int memorySpan;
	double alpha_fundamental;
	double beta_trend;
	double[] alpha_Fundamental_Vector;
	double[] beta_Trend_Vector;
	double delta;
	long Expectation;
	ArrayList Wealth;
	long priceBuy;
	double NVAT;
	WriteToFileBidAskSpread WriteToFileQuality;
	WriteToFileBidAskSpread WriteToFileSwitching;
	WriteToFileBidAskSpread WriteToFileOrderBook;
	int rejectToTrade;
	int[][] mimicMatrix;
	double[] profitVector;
	boolean[] isFundamentalist;
	int nbAgents;
	double It;
	double It1;
	double It2;
	int timeInterval =  10 + Random.nextInt(60);
	int level;

	public PhysicaAECOMOD(String name,long cash, long InitialPrice, 
			int initQuty, long[] fundamentalValue, int continous, 
			WriteToFileBidAskSpread WriteToFileQuality, WriteToFileBidAskSpread WriteToFileSwitching, 
			int[][] mimicMatrix, double[] profitVector, boolean[] isFundamentalist, 
			double[] alpha_Fundamental_Vector, double[] beta_Trend_Vector){
		super(name,cash);
		this.fundamentalValue = fundamentalValue;
		this.InitialPrice = InitialPrice;
		this.mimicMatrix = mimicMatrix;
		this.profitVector = profitVector;
		this.isFundamentalist = isFundamentalist;
		this.timeInterval =  10 + Random.nextInt(60);
		nbAgents = this.isFundamentalist.length;
		this.setInvest("ob", initQuty);


			this.alpha_Fundamental_Vector = alpha_Fundamental_Vector;
			this.beta_Trend_Vector = beta_Trend_Vector;
		
		
		//beta =0.5;
		//this.memorySpan = 1+Random.nextInt(5);
		this.memorySpan = 1+Random.nextInt(180);
		
		if(this.name.split("_")[0].equals("Fundamentalist")) 
			this.Frequency=1+Random.nextInt(360);
		else this.Frequency=1+Random.nextInt(18);
		
		if(this.name.equals("Fundamentalist_2")) this.Frequency=1;
		
		if(this.name.equals("Fundamentalist_2")) {
			
		}
	
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
						"avgReturnHFT" + "\t" + 
						"avg_Alpha_Fundamental" + "\t" + "avg_Beta_Trend" + "\t"+ "isFundAfter" + "\t" + "nbFundamentalist" + "\t" + "nbHFT");

				WriteToFileQuality.Write("Tick" + "\t" + "bestAskPrice" + "\t" + "bestAskQuty" + "\t" +  "bestBidPrice" + 
				"\t" +  "bestBidQuty" + "\t" + "lastFixedPrice" + "\t" + "lastFixedDir" + "\t" + "lastFixedQuty" + 
						"\t" + "fundamentalValue" + "\t" + "nbFixedPrices" + "\t" + "nbOrdersReceived" );
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
		char dir = ' ';
		int quty = 0;
		int i;
		this.Expectation = 0;
		
		rounds++;
		long price=fundamentalValue[rounds];
		this.Wealth.add(this.getWealth());
		OrderBook ob = market.orderBooks.get(obName);

		/*-----Write data to quality file--------*/
		if(this.name.equals("Fundamentalist_2")){   
			if( day.currentPeriod==1 && this.name.equals("Fundamentalist_2") && ob.ask.size()>0 && ob.bid.size()>0 && ob.numberOfPricesFixed>0){
		    	String str=day.currentPeriod().currentTick()+ "\t" + ob.ask.last().price +"\t" 
		    			+ ob.ask.last().quantity +"\t" + ob.bid.last().price +"\t"
		    			+ob.bid.last().quantity + "\t" + ob.lastFixedPrice.price + "\t" 
		    			+ ob.lastFixedPrice.dir +"\t" +ob.lastFixedPrice.quantity+ "\t" + fundamentalValue[rounds] + "\t" + ob.numberOfPricesFixed + "\t" +ob.numberOfOrdersReceived;
		    	try {
					WriteToFileQuality.Write(str);
				} catch (IOException e) {
					e.printStackTrace();
				}
		    
		    }
			
	
		}
	
		
		
		
		
		
		//-----At opening trade like fundamentalist
		if (day.currentPeriod().currentTick() % Frequency == 0 && day.currentPeriod==0 && this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])]){ //Opening: Send only limit orders
			    dir  = (Math.random()>0.5?LimitOrder.BID:LimitOrder.ASK);
	  			if(Math.random()>0.5){
	  				dir = LimitOrder.ASK;
  					quty = 1 + Random.nextInt(this.getInvest("ob"));
	  				price = this.fundamentalValue[rounds] - Random.nextInt(20) + Random.nextInt(20);
	  			}
	  			else{
	  				dir = LimitOrder.BID;
	  				price = this.fundamentalValue[rounds] - Random.nextInt(20) + Random.nextInt(20);
	  				quty = 1 + Random.nextInt(100);
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
	
			/*------------------------------------------------*/
			/*-----SWITCHING----------------------------------*/	
			/*------------------------------------------------*/
			if(day.currentPeriod().currentTick()>11) {
				//Compute the average profit of neighbors-fundamentalist of a given agent
				double avgReturnFundamentalist = 0;
				double avgReturnHFT = 0;
				int nbNeighborsFundamentalist = 0;
				int nbNeighborsHFT= 0;
				int nbFundamentalist= 0;
				double avg_Alpha_Fundamental = 0;
				double avg_Beta_Trend = 0;
				double currentAlpha = 0;
				double currentBeta = 0;
				int nbHFT=0;
				for (i=0; i<nbAgents; i++) {
					if(this.isFundamentalist[i]) nbFundamentalist+=1; else nbHFT+=1;
					if(this.mimicMatrix[Integer.parseInt(this.name.split("_")[1])][i]==1)
						if(isFundamentalist[i]) { //Neighbor is fundamentalist
							avgReturnFundamentalist+= profitVector[i];
							nbNeighborsFundamentalist+=1;
							avg_Alpha_Fundamental += this.alpha_Fundamental_Vector[i];
							avg_Beta_Trend += this.beta_Trend_Vector[i];
						}
						else { //Neighbor is HFT
							avgReturnHFT+= profitVector[i];
							nbNeighborsHFT+=1;
						}
				}//Nb of agents
					//If fundamentalist on average is more profitable
				if(nbNeighborsFundamentalist!=0) {
					avgReturnFundamentalist = avgReturnFundamentalist/nbNeighborsFundamentalist; 
					avg_Alpha_Fundamental = avg_Alpha_Fundamental/nbNeighborsFundamentalist;
					avg_Beta_Trend = avg_Beta_Trend/nbNeighborsFundamentalist;
				}	
				else avgReturnFundamentalist = 0;
				if(nbNeighborsHFT!=0) avgReturnHFT = avgReturnHFT/nbNeighborsHFT; else avgReturnHFT = 0 ;
				//System.out.print(day.currentPeriod().currentTick() + "\t" + this.name + "\t" + this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])] + "\t");
				String str = day.currentPeriod().currentTick() + "\t" + this.name + "\t" + this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])] + "\t";
				
				
				
				
				if (avgReturnFundamentalist > avgReturnHFT && avgReturnHFT!=0) 
					if(Random.nextDouble() < Math.exp(avgReturnFundamentalist)/(Math.exp(avgReturnFundamentalist)+Math.exp(avgReturnHFT))) {
						this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])]=true;
						currentAlpha = this.alpha_Fundamental_Vector[Integer.parseInt(this.name.split("_")[1])];
							this.alpha_Fundamental_Vector[Integer.parseInt(this.name.split("_")[1])] = 
										currentAlpha + (avg_Alpha_Fundamental - currentAlpha)*Random.nextDouble();
							
							currentBeta = this.beta_Trend_Vector[Integer.parseInt(this.name.split("_")[1])]+
										+ currentBeta + (avg_Beta_Trend - currentBeta)*Random.nextDouble();
							
						
					}//switch to 
					else this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])]=false;
				else if (avgReturnFundamentalist < avgReturnHFT && avgReturnFundamentalist!=0){
					if(Random.nextDouble() < Math.exp(avgReturnHFT)/(Math.exp(avgReturnFundamentalist)+Math.exp(avgReturnHFT)))
						this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])]=false;
					else {
						
						this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])]=true;
						currentAlpha = this.alpha_Fundamental_Vector[Integer.parseInt(this.name.split("_")[1])];
						this.alpha_Fundamental_Vector[Integer.parseInt(this.name.split("_")[1])] = 
									currentAlpha + (avg_Alpha_Fundamental - currentAlpha)*Random.nextDouble();
						
						currentBeta = this.beta_Trend_Vector[Integer.parseInt(this.name.split("_")[1])];
						this.beta_Trend_Vector[Integer.parseInt(this.name.split("_")[1])] = 
									currentBeta + (avg_Beta_Trend - currentBeta)*Random.nextDouble();
					}//switch to fundamentalist and revise your sensivities
				}
				
				if(this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])]) 
					this.Frequency =1+Random.nextInt(360);
				else 
					this.Frequency = 1 + Random.nextInt(18);
				

			     str = str + "" +  avgReturnFundamentalist + "\t" +  avgReturnHFT + "\t" +  
			    		 				+ avg_Alpha_Fundamental + "\t" + avg_Beta_Trend + "\t" + 
							               this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])] +
							               "\t" + nbFundamentalist + "\t" + nbHFT;
				
				
			     //str = str + "" + String.format("%.5g", avgReturnFundamentalist) + "\t" + String.format("%.5g", avgReturnHFT) + "\t" +  
				//			               this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])] +
				//			               "\t" + nbFundamentalist + "\t" + nbHFT;
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
				this.alpha_fundamental = this.alpha_Fundamental_Vector[Integer.parseInt(this.name.split("_")[1])] ;
				this.beta_trend = this.beta_Trend_Vector[Integer.parseInt(this.name.split("_")[1])] ;
				//this.Expectation =  ob.lastPrices.get(0).price + (long)Random.nextInt(10)*(this.fundamentalValue[rounds] - ob.lastPrices.get(0).price);
				if(ob.numberOfPricesFixed>1) {
					this.Expectation = ob.lastFixedPrice.price + (long)(this.alpha_fundamental*(this.fundamentalValue[rounds]- this.fundamentalValue[rounds-1])) +
					+ (long)(this.beta_trend*(ob.lastPrices.get(0).price - ob.lastPrices.get(1).price));
				
					dir=(this.Expectation > ob.lastPrices.get(0).price?LimitOrder.BID:LimitOrder.ASK);
				}
				else if(ob.numberOfPricesFixed==1) {
					this.Expectation = ob.lastFixedPrice.price + (long)(this.alpha_fundamental*(this.fundamentalValue[rounds]- this.fundamentalValue[rounds-1])) +
							+ (long)(this.beta_trend*(ob.lastPrices.get(0).price - 4400));
						
							dir=(this.Expectation > ob.lastPrices.get(0).price?LimitOrder.BID:LimitOrder.ASK);
				}
				else {
					 dir  = (Math.random()>0.5?LimitOrder.BID:LimitOrder.ASK);
			  			if(Math.random()>0.5){
			  				dir = LimitOrder.ASK;
		  					quty = 1 + Random.nextInt(this.getInvest("ob"));
			  				price = this.fundamentalValue[rounds] - Random.nextInt(20) + Random.nextInt(20);
			  			}
			  			else{
			  				dir = LimitOrder.BID;
			  				price = this.fundamentalValue[rounds] - Random.nextInt(20) + Random.nextInt(20);
			  				quty = 1 + Random.nextInt(100);
			  			} 
				}//there is no fixed prices
			}//Fundamentalist
			else { //if Pressure
				
				int askQuantity=0;
			    int bidQuantity=0;
			          
			   ob = market.orderBooks.get(obName);
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
		   				this.Expectation = ob.bid.first().price+Random.nextInt(10);
		    				
		        			
		    		}
		    }
		    if (day.currentPeriod().currentTick() % (Frequency+ 2*timeInterval+1) == 0 && day.currentPeriod==1){
		        		It2 = (double)(bidQuantity-askQuantity)/(bidQuantity+askQuantity);
		        		if(It>It1 && It1>It2){
		        			dir = LimitOrder.ASK;
		        			this.Expectation = ob.ask.first().price - Random.nextInt(10);
		        		}		
		    }		
				
	    
		}//Pressure
	//--------------------Expectations for both categories of traders	
			
			
	//Do not submit at expectations, check the current state of the order book before
	if(this.Expectation!=0) {
		if(dir==LimitOrder.BID) {//Traitons d'abord le cas d'achat
			if(ob.bid.isEmpty()) 
				price = this.Expectation;
			else if (ob.bid.first().price<this.Expectation) 
				price = ob.bid.first().price + Random.nextInt((int)(this.Expectation-ob.bid.first().price)) ;
			else 
				price = this.Expectation;
			this.priceBuy = price; 
		}
		else {//Traitons maintenant le cas de vente
			if(ob.ask.isEmpty()) 
				price = this.Expectation;
			else if (ob.ask.first().price>this.Expectation) 
				price = this.Expectation + Random.nextInt((int)(ob.ask.first().price - this.Expectation)) ;
			else 
				price = this.Expectation;
		} 
	}
	else return null;
		
	
	
	/*Determine volume of the order*/
		if(dir==LimitOrder.ASK)
			if(this.getInvest(obName)>0) quty=1+Random.nextInt(this.getInvest(obName)); else return null;
			//quty = Random.nextInt(50);
		else {
			if(price!=0) quty = (int)(this.cash/price); else return null;
			if (quty<1) return null;
		}	
		
		
		
		cancelPengingOrders(ob);
		if(ob.numberOfPricesFixed>0) 
			if(!this.isFundamentalist[Integer.parseInt(this.name.split("_")[1])] ) 
				profitVector[Integer.parseInt(this.name.split("_")[1])] = Math.abs((double)((ob.lastPrices.get(0).price - price)/(double)price));
			else 
				profitVector[Integer.parseInt(this.name.split("_")[1])] = Math.abs((double)((fundamentalValue[rounds] - price)/(double)price));
//if(day.currentPeriod().currentTick()>550 && !isFundamentalist[Integer.parseInt(this.name.split("_")[1])])
//	System.out.println(day.currentPeriod().currentTick() + "\t" + this.name + "\t" + dir + "\t" + quty);
		
		//this.Wealth.add(price*(this.getInvest(obName)+quty)+this.cash-price*quty);
		o = new LimitOrder(obName,""+myId,dir,Math.abs(quty),Math.abs(price));	
		return o;
	    } // frequency
		else return null;
	}
}
	


