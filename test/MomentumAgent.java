/********************************************************** 
ATOM : ArTificial Open Market

Author  : P Mathieu, Y. Secq, O Brandouy, Univ Lille1, France
Email   : philippe.mathieu@lifl.fr
Address : Philippe MATHIEU, LIFL, UMR CNRS 8022, 
Lille 1 University
59655 Villeneuve d'Ascq Cedex, france
Date    : 14/12/2008

 ***********************************************************/
package test;

import java.util.ArrayList;

import v13.*;
import v13.agents.ModerateAgent;



/**
 * A MovingAverageAgent is a technical trading agent (a chartist) which have a
 * look to the shape of the prices curve to decide what to do
 * 
 * @author Philippe MATHIEU, LIFL, Lille1 University
 **/
public class MomentumAgent extends ModerateAgent {

    private int n; // nb de jours d'écrat de momentum
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
    public MomentumAgent(String name, long cash, int n, int seuil, boolean shortselling, boolean shortrestriction) {
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

        // we verify parameters
        if (n < 1 || seuil < 0) {
            throw new RuntimeException("pb with the parameters of " + this);
        }
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
    	
    
    public Order decide(String obName, Day day) {
        OrderBook ob = market.orderBooks.get(obName);
        char dir;
        long price;
        int quty = 0 ;
        int multiple;

        
        // On ne fait rien tant qu'il n'y a pas eu au moins n+1 jours.
        if (ob.numberOfPricesFixed < n + 1) {
            return null;
        }
        myId++;
        // calcul du momentum
        
        if(!shortselling) multiple =1; else multiple = 2;	
        if(shortrestriction && day.currentPeriod().currentTick()>=2000) multiple=1;	
    	
        long momentum = ob.lastFixedPrice.price - ob.lastPrices.get(n-1).price; //  getNthLastPrice(n);
        long ancMomentum = ob.lastPrices.get(1).price - ob.lastPrices.get(n).price; // getNthLastPrice(n - 1);
        long ecart = Math.abs(momentum - ancMomentum);
        // calcul du signal d'achat ou de vente ("A" ou "V")
        if (ancMomentum < 0 && momentum > 0 && ecart > (ob.lastFixedPrice.price * seuil / 100.0)) {
        	if (ob.bid.size()==0) price = ob.lastFixedPrice.price; 
        	else price = (long)(ob.bid.first().price +Random.nextInt(10));
        	
        	if (price<=0) return null;
        	
       			dir = LimitOrder.BID;
        		
       			int potentialquty = (int) (this.cash/price);
       			if (potentialquty>1) quty = Random.nextInt(potentialquty); else return null;
       			if (quty<1) return null;
       			
       			cancelPengingOrders(ob);
       			if (quty==0) return null;
        		return new LimitOrder(obName,""+myId,dir,quty, price);
        }
        if (ancMomentum > 0 && momentum < 0 && ecart > (ob.lastFixedPrice.price * seuil / 100.0)) {
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
        	if (quty==0) return null;
        	return new LimitOrder(obName,""+myId,dir,quty, price);
        }
        return null;
        
	
        
    }
}
