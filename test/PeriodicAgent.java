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

import v13.Day;
import v13.LimitOrder;
import v13.Order;
import v13.OrderBook;
import v13.Random;
import v13.agents.ModerateAgent;

/**
 * A MovingAverageAgent is a technical trading agent (a chartist) which have a
 * look to the shape of the prices curve to decide what to do
 * 
 * @author Philippe MATHIEU, LIFL, Lille1 University
 **/
public class PeriodicAgent extends ModerateAgent {

    private int pachat;
    private int pvente;
    private boolean toggle;
    private int cpt;

    /**
     * Constructor with initial amount of cash and initial amount of (different)
     * investments
     *
     * @param name
     *            a simple name to identify each investment. Just for display.
     * @param cash
     *            amount of available currency hold by the agent
     * @param pachat the number of rounds before a new Bid/Buy
     * @param pvente the number of rounds before a new Ask/Sell
     **/
    public PeriodicAgent(String name, long cash, int pachat, int pvente) {
        super(name, cash);
        this.pachat = pachat;
        this.pvente = pvente;
        cpt = 0;
        toggle = false;
        
        if (Random.nextDouble() >0.7){
			this.cash = 2000000+Random.nextInt(2000000);
			this.setInvest("ob0", 2000+Random.nextInt(1000));
		}	
		else{ 
			this.cash = 1000000+Random.nextInt(1000000);
			this.setInvest("ob0", 150+Random.nextInt(100));
		}
        // we verify parameters
        if (pachat < 1 || pvente < 1) {
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
        // Try/catch because getBestAsk or getBestBid is not always available !
        try {
    //        OrderBook ob = market.orderBooks.get(invest);
       long price;
       int quty;
       int nothing;
       
       if(this.cash<0)
       	nothing=0;
        	OrderBook ob = market.orderBooks.get(obName);
            cpt++;
            myId++;
            // pour donner un signal d'achat il faut avoir donné un
            // signal de vente et avoir passé la periode d'achat
            if (!toggle && cpt == pachat) {
                cpt = 0;
                toggle = true;
            	if (ob.bid.size()==0)
    				price = ob.lastFixedPrice.price + Random.nextInt(40);
    			else
    				price = (long)(ob.bid.first().price + Random.nextInt(40));
            	
            	quty = (int)(this.cash/price);
        		if (quty<1) return null;
        		
        		if(price<0)
                 	nothing=0;
        		cancelPengingOrders(ob);
                return new LimitOrder(obName,""+myId,LimitOrder.BID,quty, price);
            }
            // pour donner un signal de vente il faut avoir donné
            // un signal d'achat et avoir passé la periode de
            // vente
            if (toggle && cpt == pvente) {
                cpt = 0;
                toggle = false;
            	if (ob.ask.size()==0)
    				price = ob.lastFixedPrice.price - Random.nextInt(40);
    			else
    				price = (long)(ob.ask.first().price - Random.nextInt(40));
            	
            	if(this.getInvest(obName)>0)
        			quty=1+Random.nextInt(this.getInvest(obName));
        		else return null; 
            	
            	if(price<0)
                 	nothing=0;
            	cancelPengingOrders(ob);
                return new LimitOrder(obName,""+myId,LimitOrder.ASK,quty,price);
            }
        } catch (RuntimeException re) {
        }
        return null;
    }
}
