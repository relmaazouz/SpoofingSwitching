/********************************************************** 
ATOM : ArTificial Open Market

Author  : P Mathieu, O Brandouy, Univ Lille1, France
Email   : philippe.mathieu@lifl.fr
Address : Philippe MATHIEU, LIFL, UMR CNRS 8022, 
Lille 1 University
59655 Villeneuve d'Ascq Cedex, france
Date    : 14/12/2008

 ***********************************************************/
package test;

import java.util.ArrayList;

import v13.*;
import v13.agents.*;

/**
 * A MovingAverageAgent is a technical trading agent (a chartist) which have a
 * look to the shape of the prices curve to decide what to do
 * 
 * @author Philippe MATHIEU & Yann SECQ, LIFL, Lille1 University
 **/
public class RsiAgent extends ModerateAgent {

    private int n; // nb de jours d'écart de rsi
    private int seuil; // seuil d'écart minimum acceptable
    private boolean shortselling;
    private boolean shortrestriction;

    /**
     * Constructor with initial amount of cash and initial amount of (different)
     * investments
     *
     * @param name
     *            a simple name to identify each investment. Just for display.
     * @param cash
     *            amount of available currency hold by the agent
     * @param n
     *            the nuber of prices used to compute the moving average
     **/
    public RsiAgent(String name, long cash, int n, int seuil, boolean shortselling, boolean shortrestriction) {
        super(name, cash);
        this.n = n;
        this.seuil = seuil;
        this.shortselling = shortselling;
        this.shortrestriction = shortrestriction;
        
        if (Random.nextDouble() >0.7){
			this.cash = 2000000+Random.nextInt(2000000);
			this.setInvest("ob0", 2000+Random.nextInt(1000));
		}	
		else{ 
			this.cash = 1000000+Random.nextInt(1000000);
			this.setInvest("ob0", 150+Random.nextInt(100));
		}

        if (n < 1 || seuil < 0) {
            throw new RuntimeException("pb with the parameters of " + this);
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
    
    
    /**
     * Compute and execute the agent's decision for this investment. This is the
     * main decision method for the agent. It contains all the actions she wants
     * to make at this time step for this specific investment.
     *
     * @param news
     *            is an information coming from the economic world, directly
     *            related to this investment.
     * @return an Order or null, if the agent does not want to do any action
     *         concerning his holdings.
     **/
    public Order decide(String obName, Day day) {
        OrderBook ob = market.orderBooks.get(obName);
        //OrderBook ob = market.orderBooks.get(obName);
        
        if (ob.numberOfPricesFixed < n) {
            return null;
        }

        // calcul des moyennes des hausses et des baisses sur les n jours
        int nbHausse = 0;
        double moyHausse = 0.0;
        int nbBaisse = 0;
        double moyBaisse = 0.0;
        long price;
        int quty=0;
        char dir;
        int multiple;
        
        int i = (int)ob.numberOfPricesFixed;
        
        
        for (int x=0; x<(n-1); x++)
        {
        	double diff = ob.lastPrices.get(x).price - ob.lastPrices.get(x+1).price;
        	if (diff > 0) {nbHausse++; moyHausse+=diff;}
        	else if(diff < 0) {nbBaisse++; moyBaisse+=(diff*-1);}
        }
         
        // System.out.println(tab[i]+" "+nbHausse+" "+nbBaisse);

        // si l'un des deux est 0 ... on saute la décision
        if (nbHausse == 0 || nbBaisse == 0) {
            return null;
        }
        // A VERIFIER : si sur les n jours il n'y a que des hausses, aucun signal !!
        // du coup il faut obligatoirement des hausses ET des baisses pour avoir un signal !

        moyHausse /= nbHausse;
        moyBaisse /= nbBaisse;

        
        // calcul du RSI
        double rsi = 100 - (100 / (1 + moyHausse / moyBaisse));
        
        if(!shortselling) multiple =1; else multiple = 2;	
        if(shortrestriction && day.currentPeriod().currentTick()>=400) multiple=1;

        // calcul du signal d'achat ou de vente ("A" ou "V")
        if (rsi < 50 - seuil) {
            myId++;
            if (ob.bid.size()==0)
				price = ob.lastFixedPrice.price;
			else
				price = (long)(ob.bid.first().price +Random.nextInt(10));
        	
            if (price<=0) return null;
            
            dir = LimitOrder.BID;
    		
        	int potentialquty = (int) (this.cash/price);
    		if (potentialquty>1) quty = Random.nextInt(potentialquty); else return null;
    		if (quty<1) return null;
   			
   			cancelPengingOrders(ob);
    		return new LimitOrder(obName,""+myId,dir,quty, price);
        }
        if (rsi > 50 + seuil) {
            myId++;
        	if (ob.ask.size()==0)
				price = ob.lastFixedPrice.price;
			else
				price = (long)(ob.ask.first().price - Random.nextInt(10));
        	if (price<=0) return null;
        	
        	
        	dir = LimitOrder.ASK;
        	

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
        	
         	
        	
        	cancelPengingOrders(ob);
        	if(quty == 0) return null;
        	return new LimitOrder(obName,""+myId,dir,quty, price);
        }

        return null;
    }

}
