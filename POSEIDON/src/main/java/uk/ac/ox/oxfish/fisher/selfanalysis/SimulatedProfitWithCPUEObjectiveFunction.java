package uk.ac.ox.oxfish.fisher.selfanalysis;

import com.google.common.annotations.VisibleForTesting;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * rather than looking at previous profits, this function uses a simulator to produce a new trip with new profits
 * taking last CPUE observed on trip record as expected constant CPUE for future trip.
 * From the simulated trip it then extract and return its hourly profits
 *
 */
public class SimulatedProfitWithCPUEObjectiveFunction extends TripBasedObjectiveFunction {

    private final int maxHoursOut;

    private ProfitFunction simulator;

    public SimulatedProfitWithCPUEObjectiveFunction(int maxHoursOut) {
        this.maxHoursOut = maxHoursOut;
        this.simulator = new ProfitFunction(maxHoursOut);
    }

    @VisibleForTesting
    public SimulatedProfitWithCPUEObjectiveFunction(int maxHoursOut, ProfitFunction simulator) {
        this.maxHoursOut = maxHoursOut;
        this.simulator = simulator;
    }

    @Override
    protected double extractUtilityFromTrip(Fisher observer, TripRecord tripRecord, Fisher observed) {
        //if there is no trip record to copy or no tile to look at, just return NaN
        if(tripRecord==null)
            return Double.NaN;
        SeaTile tile = tripRecord.getMostFishedTileInTrip();
        if(tile==null)
            return Double.NaN;

        //get CPUE
        //todo remove this and use the last hour CPUE rather than the average?
        double[] cpue = tripRecord.getTotalCPUE();


        //if it's our own trip, just quickly call predict
        if(observer==observed || observed==null || observed.getGear().isSame(observer.getGear())) {
            return simulator.hourlyProfitFromHypotheticalTripHere(observer,tile,observed.grabState(),
                    cpue,false);
        }

        //if it's not your trip, you need to weigh the efficiency of your gear compared to the one you are observing
        double[] expectedCPUE = new double[cpue.length];
        double[] myEfficiency =
                observer.getGear().expectedHourlyCatch(
                        observer,
                        tile,
                        1,
                        observer.grabState().getBiology());
        double[] theirEfficiency =
                observed.getGear().expectedHourlyCatch(
                        observed,
                        tile,
                        1,
                        observer.grabState().getBiology());

        for(int i=0; i<expectedCPUE.length; i++)
        {
            expectedCPUE[i] = cpue[i]* (theirEfficiency[i] == 0 ? 0 : myEfficiency[i]/theirEfficiency[i]);
        }

        return simulator.hourlyProfitFromHypotheticalTripHere(observer,tile,observed.grabState(),
                expectedCPUE,false);
    }



}
