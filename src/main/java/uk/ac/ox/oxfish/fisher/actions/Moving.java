package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Deque;

/**
 * The action of moving
 */
public class Moving implements Action
{


    /**
     * if you were moving halfway through 2 cells, here you keep record, otherwise it stays at -1
     */
    private double accruedHours = -1;

    public Moving(double timeAlreadyTravelling) {
        this.accruedHours = timeAlreadyTravelling;
    }

    public Moving() {
    }

    /**
     * Do something and returns a result which is the next state and whether or not it should be run on the same turn
     *
     * @param model a link to the model, in case you need to grab global objects
     * @param agent a link to the fisher in case you need to get or set agent's variables
     * @param regulation the regulation object that tells us whether we can be out at all
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(FishState model, Fisher agent, Regulation regulation, double hoursLeft) {

        //it would be very weird to accumulate a full day!
        assert accruedHours < 24;

        //adapt if needed
        SeaTile oldDestination = agent.getDestination();
        agent.updateDestination(model,this);
        //if you changed your direction, you lose your accrued hours
        if(oldDestination != agent.getDestination())
            accruedHours = -1;

        /**
         * we have arrived!
         */
        if(agent.getDestination().equals(agent.getLocation()))
            return new ActionResult(new Arriving(),hoursLeft);

        Deque<SeaTile> route = model.getMap().getRoute(agent.getLocation(),agent.getDestination());
        assert  route.peek().equals(agent.getLocation()); //starts at the right location
        assert route.peekLast().equals(agent.getDestination()); //ends where we are
        route.poll(); //remove start, it's useless

        NauticalMap map = model.getMap();

        if(accruedHours > 0)
            hoursLeft+= accruedHours;

        //while there are still places to go
        while(!route.isEmpty())
        {
            SeaTile step = route.pollLast();

            double distance = map.distance(agent.getLocation(),step);
            //if you have time move
            final double hoursForThisStep = agent.hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(distance);
            assert  hoursForThisStep >= accruedHours : agent;

            if(hoursForThisStep <= hoursLeft)
            {
                agent.move(step, map,model);

                assert agent.getLocation().equals(step);
                if(step.equals(agent.getDestination()))
                    return new ActionResult(new Arriving(),hoursLeft-hoursForThisStep);
                else
                    return new ActionResult(new Moving(),hoursLeft-hoursForThisStep);
            }
            //too far, try closer
        }
        return new ActionResult(new Moving(hoursLeft),0);



    }


}
