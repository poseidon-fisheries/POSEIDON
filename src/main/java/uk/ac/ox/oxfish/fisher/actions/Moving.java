package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Deque;
import java.util.LinkedList;

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

        Deque<SeaTile> route = getRoute(model.getMap(),agent.getLocation(),agent.getDestination());
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


    /**
     * builds a path from start to end. No weird pathfinding here, simply move diagonally then horizontally-vertically when that's not possible anymore
     * @param map the nautical map
     * @param start the start point
     * @param end the end point
     * @return a queue of tiles to pass through (not including the starting point but including the end point). Empty if starting point is the ending point
     */
    public Deque<SeaTile> getRoute(NauticalMap map, SeaTile start, SeaTile end)
    {


        int x = start.getGridX(); int endX = end.getGridX();
        int y = start.getGridY(); int endY =end.getGridY();

        LinkedList<SeaTile> path = new LinkedList<>();

        while( x != endX || y!= endY)
        {

            int candidateX =  x + Integer.signum( endX - x );
            int candidateY =  y + Integer.signum( endY - y );

            //can you move your preferred way?
            SeaTile bestSeaTile = map.getSeaTile(candidateX, candidateY);
            if(bestSeaTile.getAltitude() <= 0 || bestSeaTile.isPortHere())
            {
                path.add(bestSeaTile);
                x = candidateX;
                y=candidateY;
            }
            //try to move on the x axis only then
            else if(candidateX != x && map.getSeaTile(candidateX,y).getAltitude() <= 0)
            {
                x= candidateX;
                path.add(map.getSeaTile(candidateX,y));
            }
            else if(candidateY != y && map.getSeaTile(x,candidateY).getAltitude() <= 0)
            {
                y=candidateY;
                path.add(map.getSeaTile(x,candidateY));

            }
            //otherwise just go over land!
            else
            {
                path.add(map.getSeaTile(candidateX,candidateY));
                x= candidateX;
                y=candidateY;
            }
        }


        assert path.isEmpty() || path.peekLast().equals(end);

        return path;



    }
}
