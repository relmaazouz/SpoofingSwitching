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
public class MovingAverageAgent extends ModerateAgent {
	
	private int day;
    private int n; // pb of prices needed to compute the average.
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
     *            the number of prices used to compute the moving average
     **/
    public MovingAverageAgent(String name, long cash, int n, int nassets, boolean shortselling, boolean shortrestriction) {
        super(name, cash);
        this.n = n;
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

        /*
        if (n > OrderBook.LASTPRICESSIZE) {
            throw new RuntimeException("size to large for the orderbook ! " + OrderBook.LASTPRICESSIZE);
        }
         */
    }
    
	 void setDay(int d){
			int i,j;
			this.day = d;
		//	System.out.println("DAY \t "+ d);

			//System.out.println(this.name + "\t" + this.wealth());
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
	 
   // public Order decide(String invest, int day, int tick) {
    public Order decide(String obName, Day day){	
  	
    	long price;
    	int quty=0;
    	char dir;
    	int multiple;
        OrderBook ob = market.orderBooks.get(obName);

        
        // On ne fait rien tant qu'il n'y a pas eu au moins n+1 jours.
        if (ob.numberOfPricesFixed < n + 1) {
            return null;
        }

        long somme = 0;
        for (int x = 0; x < n; x++) {
            somme += ob.lastPrices.get(x).price;
        }
        long sommeAnc = somme - ob.lastFixedPrice.price + ob.lastPrices.get(n).price;

        double moy = somme / (double) n;
        double moyAnc = sommeAnc / (double) n;

        // System.out.println(" -> "+i+" "+tab[i-n+1]+" "+tab[i]+" "+sommeAnc+" "+somme);

        myId++;
        
        // le cours coupe la moyenne mobile du haut vers le bas : signal de vente.
        long actual = ob.lastFixedPrice.price;
        long previous = ob.lastPrices.get(1).price;
        
        
        if(!shortselling) multiple =1; else multiple = 2;
        if(shortrestriction && day.currentPeriod().currentTick()>=400) multiple=1;
        
        if(day.currentPeriod().currentTick()>=400) multiple=1;

        if (previous > actual && moyAnc < moy && previous > moyAnc && actual < moy) {
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
        	if(quty==0) return null;
        	return new LimitOrder(obName,""+myId,dir,quty, price);
        }

        // Si le cours coupe la moyenne mobile du bas vers le haut : signal d'achat.
        if (previous < actual && moyAnc > moy && previous < moyAnc && actual > moy) {
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

        return null;
    }
}
