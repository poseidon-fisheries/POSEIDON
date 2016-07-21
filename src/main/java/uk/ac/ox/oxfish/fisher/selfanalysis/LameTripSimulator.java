package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.log.FishingRecord;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;

/**
 * A "trip simulator" that is really just a collection of equations and not a proper simulation. It assumes you only
 * go to a single spot, you have a maximum amount of time you spend fishing and then you come back
 * Created by carrknight on 7/13/16.
 */
public class LameTripSimulator {


    public TripRecord simulateRecord(Fisher fisher, SeaTile fishingSpot, FishState state,
                                     double maxHoursOut,
                                     double[] expectedHourlyCatches)
    {

        int numberOfSpecies = state.getSpecies().size();
        TripRecord record = new TripRecord(numberOfSpecies, fisher.getHoursAtPort());

        double timeSpentAtSea = 0;
        double gasConsumed = 0;

        //get path from port to fishing spot
        Port homePort = fisher.getHomePort();
        Deque<SeaTile> routeToAndFrom = state.getMap().getRoute(homePort.getLocation(), fishingSpot);
        //you need to go there
        double tripDistance = pathDistance(routeToAndFrom, state.getMap());
        double distanceTravelled = tripDistance;

        timeSpentAtSea+= fisher.hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(distanceTravelled);
        gasConsumed+= fisher.getBoat().expectedFuelConsumption(distanceTravelled);
        //while there is still time, fish
        double maxWeight = fisher.getMaximumHold();
        double expectedTotalCatchesPerHour = Arrays.stream(expectedHourlyCatches).sum();
        int hoursNeededToFillBoat = expectedTotalCatchesPerHour>0? (int)Math.ceil(maxWeight/expectedTotalCatchesPerHour) :
                (int)maxHoursOut;
        double fishingHours = Math.min(maxHoursOut-timeSpentAtSea,hoursNeededToFillBoat );

        if(fishingHours>0)
        {
            double[] catches = new double[numberOfSpecies];
            for(int i=0; i<numberOfSpecies; i++)
            {
                catches[i] = expectedHourlyCatches[i] * fishingHours;
            }
            Hold.throwOverboard(catches,maxWeight);
            assert Arrays.stream(catches).sum() <= maxWeight;
            gasConsumed+=
                    fisher.getGear().getFuelConsumptionPerHourOfFishing(
                            fisher,fisher.getBoat(),fishingSpot
                    ) * fishingHours;

            record.recordFishing(new FishingRecord((int)fishingHours,fisher.getGear(),
                                                   fishingSpot,new Catch(catches),
                                                   fisher,
                                                   state.getStep()));
            //now check what can actually be sold
            for(int i=0; i<numberOfSpecies; i++)
            {
                Species species = state.getSpecies().get(i);
                catches[i] = Math.min(fisher.getRegulation().maximumBiomassSellable(fisher, species, state),
                                      catches[i]);
                record.recordEarnings(i,catches[i],
                                      homePort.getMarginalPrice(species)*catches[i]);

            }
        }


        //you need to come back
        distanceTravelled+= tripDistance;
        timeSpentAtSea+= fisher.hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(tripDistance);
        gasConsumed+= fisher.getBoat().expectedFuelConsumption(tripDistance);

        record.addToDistanceTravelled(distanceTravelled);
        record.recordGasConsumption(gasConsumed);
        record.completeTrip(timeSpentAtSea + fishingHours, homePort);

        return record;


    }


    /**
     * turns path into distance
     * @param path
     * @return
     */
    public static double  pathDistance(Deque<SeaTile> path, NauticalMap map)
    {
        Iterator<SeaTile> iterator = path.iterator();
        SeaTile current = iterator.next();
        double totalDistance = 0;
        while(iterator.hasNext())
        {
            SeaTile next = iterator.next();
            totalDistance+=map.distance(current,next);
            current=next;
        }
        return totalDistance;
    }



}
