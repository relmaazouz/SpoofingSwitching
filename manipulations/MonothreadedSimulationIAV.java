/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manipulations;

import v13.Logger;
import v13.Order;
import v13.OrderBook;
import v13.Simulation;
import v13.agents.Agent;

import java.util.*;

/**
 * This simulation is monothreaded. It means that one simulation time step is
 * organized around a talk turn tfor all agents. Agents list is shuffled and
 * each agent is asked to provide an order that is sent directly to the market.
 *
 * Thus, equity in information is not guaranteed: the last agent to talk can
 * take a decision based on all consequences from previous agents action (even
 * if we are still within the same time step).
 *
 * @author yann.secq@univ-lille1.fr
 */
public class MonothreadedSimulationIAV extends Simulation {

    public MonothreadedSimulationIAV() {
        super();
    }

    public MonothreadedSimulationIAV(Logger log) {
        super(log);
    }

    protected void queryAllAgents() {
        //System.out.println("MonoThreadedSimulation/queryAllAgents(Day): " + day);
        // Iterating on orderbooks to decrease validity of orders of one round
        for (OrderBook ob : market.orderBooks.values()) {
            // ob.decreaseAndDeleteUnvalid(); // Comment to SpeedUp if validity = -1
        }
        // "Tour de parole"
        List<Agent> al = new ArrayList<Agent>(agentList.values());
        //Collections.shuffle(al);
        for (Agent agent : al) {
            int currentTick = day.currentPeriod().currentTick();
            if (agent.speed == 0 || (currentTick % agent.speed == 0)) {
                for (OrderBook ob : market.orderBooks.values()) {
                    agent.beforeDecide(ob.obName, day); // changement v10
                    Order lo = agent.decide(ob.obName, day);
                    agent.afterDecide(ob.obName, day, lo); // changement v10

                    if (lo != null) {
                        market.send(agent, lo);
                    }
                }
            }
        }
        try {
            // Waiting tempo milliseconds before going to next tick
            Thread.sleep(tempo);
        } catch (InterruptedException ex) {
            log.error(ex);
        }
    }
}
