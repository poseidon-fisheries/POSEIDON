/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

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

    private Deque<SeaTile> path;

    public Moving(double timeAlreadyTravelling, Deque<SeaTile> currentPath) {
        this.accruedHours = timeAlreadyTravelling;
        this.path = currentPath;
    }

    public Moving() {
        this(-1,null);
    }

    public Moving(Deque<SeaTile> currentPath)
    {
        this(-1,currentPath);
    }

    /**
     * Move on a previous osmoseWFSPath or ask the map for a new osmoseWFSPath and follow it from agent location to agent destination
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
        if(oldDestination != agent.getDestination()) {
            accruedHours = -1;
            path = null;
        }

        /**
         * If we have arrived, don't bother moving.
         */
        if(agent.getDestination().equals(agent.getLocation()))
            return new ActionResult(new Arriving(),hoursLeft);


        NauticalMap map = model.getMap();
        //if you don't have a path, you have to find it
        if(path==null) {
            //get the pathfinder to help
            path = map.getRoute(agent.getLocation(), agent.getDestination());
            if(path == null)
            {
                //there is no osmoseWFSPath available
                agent.setDestinationForPort();
                return new ActionResult(new Arriving(),0);
            }

            assert path.peek().equals(agent.getLocation()); //starts at the right location
            assert path.peekLast().equals(agent.getDestination()); //ends where we are
            path.poll(); //remove start, it's useless
        }
        //the first step should not be where we currently are
        assert path != null;
        assert !path.isEmpty();
        assert !path.peekFirst().equals(agent.getLocation());


        //if you have been moving from the previous step, count those hours
        if(accruedHours > 0)
            hoursLeft+= accruedHours;

        //while there are still places to go




        //moving actually happens in more than one "step" at a time; this is because agent.move(*) is slow (since we have to update the MASON map)
        //so what we do is that we check what's the farthest we can go in one period and go there
        double timeSpentTravelling=0;
        double totalDistance = 0;
        SeaTile next = agent.getLocation();
        //go through the osmoseWFSPath until it's empty
        while(!path.isEmpty())
        {
            //check distance and time to travel one more node during this period
            SeaTile step = path.peekFirst();
            double distance = map.distance(next, step);
            final double hoursForThisNode = agent.hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(distance);

            //if you can make this step within the time given, do it
            if(timeSpentTravelling + hoursForThisNode<=hoursLeft)
            {
                next = path.poll();
                assert next == step;
                timeSpentTravelling+=hoursForThisNode;
                totalDistance+=distance;
            }
            else
             break;

        }

        if(!next.equals(agent.getLocation())) //if you have time to make at least one step
        {

            assert  Math.abs(totalDistance/agent.getBoat().getSpeedInKph()- timeSpentTravelling)<
                    FishStateUtilities.EPSILON;
            assert hoursLeft >= timeSpentTravelling;
            hoursLeft = hoursLeft - timeSpentTravelling;
            hoursLeft = (Math.abs(hoursLeft) < FishStateUtilities.EPSILON) ? 0 : hoursLeft;

            agent.move(next, map,model, totalDistance);

            assert agent.getLocation().equals(next); //check that I moved to the right spot
            if(next.equals(agent.getDestination()))
                return new ActionResult(new Arriving(),hoursLeft);
            else
            {
                assert !path.isEmpty();
                return new ActionResult(new Moving(path), hoursLeft);
            }
        }
        //didn't make it to there
        return new ActionResult(new Moving(hoursLeft,path),0);



    }


}
