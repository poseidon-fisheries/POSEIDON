package uk.ac.ox.oxfish.utility.imitation;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.LocationMemory;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.maximization.IterativeMovement;

import java.util.Comparator;

/**
 * The basis of the PSO movement: you have a velocity at start, you modify it by trying to go back to your best memory
 * and where your best friend is. Whenever you are told to iterate you shock your velocity at random.
 *
 * Created by carrknight on 7/27/15.
 */
public class ParticleSwarmMovement implements ImitativeMovement, IterativeMovement
{

    private final float memoryWeight;
    private final float friendWeight;

    private final int explorationShockVelocity;
    /**
     *  movement along the x axis
     */
    private float xVelocity;

    /**
     * movement along the y axis
     */
    private float yVelocity;

    /**
     * slowing down of speed over time
     */
    private final float inertia;

    private final Comparator<LocationMemory<TripRecord>> memoryComparator = (o1, o2) -> Double.compare(o1.getInformation().getProfitPerStep(),
                                                                                                       o2.getInformation().getProfitPerStep());;


    public ParticleSwarmMovement(
            float memoryWeight, float friendWeight, int explorationShockVelocity, float xVelocity, float yVelocity,
            float inertia) {
        this.memoryWeight = memoryWeight;
        this.friendWeight = friendWeight;
        this.explorationShockVelocity = explorationShockVelocity;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        this.inertia = inertia;
    }

    /**
     * during imitation you follow standard PSO rules
     * @param fisher  the fisher
     * @param map the nautical map
     * @param previous the previous position
     * @param current the current position
     * @param previousFitness the previous fitness
     * @param newFitness the new fitness
     * @param bestFriend your best friend
     * @param friendFitness your best friend fitness
     * @param friendSeaTile your best friend position
     * @return the new position
     */
    @Override
    public SeaTile adapt(
            Fisher fisher, NauticalMap map, SeaTile previous, SeaTile current, double previousFitness,
            double newFitness,
            Fisher bestFriend, double friendFitness, SeaTile friendSeaTile)
    {

        MersenneTwisterFast random = fisher.grabRandomizer();
        SeaTile bestMemory = fisher.getBestSpotForTripsRemembered(memoryComparator) ;
        //if you have no memory or you are doing better than your last memory
        if(bestMemory == null || newFitness > previousFitness)
            bestMemory = current;
        //if you have no friend
        if(friendSeaTile == null || bestFriend == null || !Double.isFinite(friendFitness) ||
                friendFitness < newFitness)
            friendSeaTile = current;

        //adjust X velocity
        float currentX = current.getGridX();
        float memoryX = bestMemory.getGridX();
        float friendX = friendSeaTile.getGridX();
        xVelocity = inertia * xVelocity +
                memoryWeight * random.nextFloat() * (memoryX - currentX) +
                friendWeight * random.nextFloat() * (friendX - currentX);

        //adjust Y velocity
        float currentY = current.getGridY();
        float memoryY = bestMemory.getGridY();
        float friendY = friendSeaTile.getGridY();
        yVelocity = inertia * yVelocity +
                memoryWeight * random.nextFloat() * (memoryY - currentY) +
                friendWeight * random.nextFloat() * (friendY - currentY);


        return move(map, current);
    }




    private SeaTile move(NauticalMap map, SeaTile current) {
        int newX = Math.min(Math.max(Math.round(current.getGridX() + xVelocity),0),map.getWidth()-1);
        int newY = Math.min(Math.max(Math.round(current.getGridY() + yVelocity), 0),map.getHeight()-1);

        SeaTile newSeatile = map.getSeaTile(newX, newY);
        if(newSeatile.getAltitude() <0)
            return newSeatile;
        else
            return current;
    }


    /**
     * decide a new tile to move to given the current and previous step and their fitness
     *
     * @param fisher          the fisher doing the maximization
     * @param map             the map over which the fishers move
     * @param previous        the sea-tile tried before this one. Could be null
     * @param current         the sea-tile just tried
     * @param previousFitness the fitness value associated with the old sea-tile, could be NaN
     * @param newFitness      the fitness value associated with the current tile     @return a new sea-tile to try
     */
    @Override
    public SeaTile adapt(
            Fisher fisher, NauticalMap map, SeaTile previous, SeaTile current, double previousFitness,
            double newFitness) {
        xVelocity = fisher.grabRandomizer().nextFloat()*2*explorationShockVelocity-explorationShockVelocity;
        yVelocity = fisher.grabRandomizer().nextFloat()*2*explorationShockVelocity-explorationShockVelocity;
        return move(map,current);
    }
}
