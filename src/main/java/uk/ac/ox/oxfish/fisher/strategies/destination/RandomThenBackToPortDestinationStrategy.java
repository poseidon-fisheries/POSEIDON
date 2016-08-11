package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Random-walk like decision. Not very useful but the easiest to make
 * Created by carrknight on 4/19/15.
 */
public class RandomThenBackToPortDestinationStrategy implements DestinationStrategy {
    /**
     *  if the fisher is at port, picks a sea-location at random. If the fisher is at sea, it chooses the same destination until it arrives.
     * Once it has arrived, it chooses to go back to port.
     * @param fisher
     * @param random        the randomizer
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination   */
    @Override
    public SeaTile chooseDestination(
            Fisher fisher, MersenneTwisterFast random,
            FishState model,
            Action currentAction) {

        //if the fisher is at port
        if(fisher.isAtPort())
        {
            //they are probably docked
            assert currentAction instanceof AtPort;
            assert fisher.isGoingToPort(); //I assume at port your destination is still the port

            //that's where we are headed!
            return model.getMap().getRandomBelowWaterLineSeaTile(random);
        }
        else
        {
            //we are not at port
            assert ! (currentAction instanceof AtPort);
            //are we there yet?
            if(fisher.getLocation() == fisher.getDestination())
                return fisher.getHomePort().getLocation(); //return home
            else
                return fisher.getDestination(); //stay the course!
        }



    }


    /**
     * ignored
     *
     * @param model the model
     */
    @Override
    public void start(FishState model, Fisher fisher) {

    }

    /**
     * ignored
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {

    }
}



