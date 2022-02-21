package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;

import java.util.*;
import java.util.function.Function;

public class ExogenousFadSetterFromData extends ExogenousFadSetter {


    private final Map<Integer,List<FadSetObservation>> fadSetsPerDayInData;

    /**
     * how much should the error be when the data wants us to set out of the simulation map
     */
    public final static double OUT_OF_BOUNDS_FAD_ERROR = 0;

    /**
     * how much should the error be when there is a fad set in the data but we didn't find any FAD
     * in the simulation to match it to
     */
    public final static double MISSING_FAD_ERROR = 10000;


    private final Counter counter = new Counter(IntervalPolicy.EVERY_YEAR);



    /**
     * This is called to take simulated fad biomass and transform it to whatever is appropriate for comparison with data
     * by defa
     */
    private Function<Double,Double> simulatedToDataScaler = simulatedBiomass -> simulatedBiomass;

    public ExogenousFadSetterFromData(Map<Integer, List<FadSetObservation>> fadSetsPerDayInData) {
        this.fadSetsPerDayInData = fadSetsPerDayInData;
    }


    @Override
    public void start(FishState model) {
        super.start(model);
        counter.start(model);
        counter.addColumn("Error");
        counter.addColumn("Failed Matches");
        counter.addColumn("Matches");
        counter.addColumn("Out of Bounds");
        for (String column : counter.getValidCounters()) {
            model.getYearlyDataSet().registerGatherer(
                    "Exogenous Fad Setter "+column,
                    (Gatherer<FishState>) state -> counter.getColumn(column),
                    Double.NaN
            );
        }


    }

    /**
     * this method does a bunch of stuff
     * (i) translates data coordinates to simulation's seatile
     * (ii) match within the seatile data with simulated fad, choosing the ones that minimize error
     * (iii) accounts for errors
     * (iv) returns list of valid matches for fishing out within the model
     */
    @Override
    protected List<Fad> chooseWhichFadsToSetOnToday(FadMap fadMap, FishState model, int day) {
        //only bother if there is anything to set in the data
        List<FadSetObservation> fadSetObservationsToday = fadSetsPerDayInData.get(day);
        if(fadSetObservationsToday == null || fadSetObservationsToday.isEmpty())
            return new LinkedList<>();


        //ready containers
        List<FadSetObservation> outOfBoundsObservations = new LinkedList<>();
        List<FadSetObservation> observationsThatCouldNotBeMatched = new LinkedList<>();
        List<Fad> matchedFadsToFishOut = new LinkedList<>();
        //for now, and for simplicity, let's focus on just matching FADs with observations that share the same seatile
        //first step, we need to take all the observation coordinates that were fished out today, and turn them into seatiles
        HashMap<SeaTile,List<FadSetObservation>> fadObservations = new HashMap<>();
        for (FadSetObservation observedSet : fadSetObservationsToday) {
            SeaTile seaTile = model.getMap().getSeaTile(observedSet.getLocationInData());
            //if it is out of bounds, record it as out of bounds
            if(seaTile == null)
                outOfBoundsObservations.add(observedSet);
            //otherwise record it in the right spot
            else
                fadObservations.computeIfAbsent(seaTile, tile -> new LinkedList<>()).add(observedSet);

        }
        //now that we have arranged observations by area, match observations with simulated fads
        for (Map.Entry<SeaTile, List<FadSetObservation>> setsPerTile : fadObservations.entrySet()) {
            //get all observable matches (i.e. all fads in the same tile)
            ArrayList<Fad> matchableFads = new ArrayList<>(getFadMap().fadsAt(setsPerTile.getKey()));
            //sort them by size (to get consistent errors)
            Collections.sort(matchableFads, (o1, o2) -> -Double.compare(
                    Arrays.stream(o1.getBiomass()).sum(),
                    Arrays.stream(o2.getBiomass()).sum()
            ));
            //for each observed set
            for (FadSetObservation observedSet : setsPerTile.getValue()) {
                //get the closest simulated fad (in terms of error)
                Optional<Fad> bestMatch = matchableFads.stream().min(Comparator.
                        comparingDouble(simulatedFad -> computeError(
                        observedSet,
                        simulatedFad
                )));
                //if there is such thing:
                if (bestMatch.isPresent()) {
                    //count the error
                    matchedFadsToFishOut.add(bestMatch.get());
                    counter.count("Error",computeError(observedSet, bestMatch.get()));
                    //remove from matchables
                    matchableFads.remove(bestMatch.get());

                }else{
                    //otherwise count it as a miss
                    assert  matchableFads.isEmpty();
                    observationsThatCouldNotBeMatched.add(observedSet);
                }
            }

        }


        counter.count("Matches",matchedFadsToFishOut.size());
        counter.count("Failed Matches",observationsThatCouldNotBeMatched.size());
        counter.count("Error",observationsThatCouldNotBeMatched.size()*MISSING_FAD_ERROR);
        counter.count("Out of Bounds",outOfBoundsObservations.size());
        counter.count("Error",outOfBoundsObservations.size()*OUT_OF_BOUNDS_FAD_ERROR);

        return matchedFadsToFishOut;
    }




    //might want to make this a pluggable strategy

    /**
     * sqrt of squared error per species
     * @param observation
     * @param fad
     * @return
     */
    public double computeError(FadSetObservation observation,
                               Fad fad){
        double[] data = observation.getBiomassCaughtInData();
        double[] simulated = fad.getBiomass();
        assert data.length == simulated.length;
        double totalError = 0;
        for (int i = 0; i < data.length; i++)
            totalError += Math.pow(simulatedToDataScaler.apply(simulated[i]) - data[i], 2);
        return Math.sqrt(totalError);

    }

    @Override
    public void turnOff() {
        super.turnOff();
        counter.turnOff();

    }


    public Function<Double, Double> getSimulatedToDataScaler() {
        return simulatedToDataScaler;
    }

    public void setSimulatedToDataScaler(Function<Double, Double> simulatedToDataScaler) {
        this.simulatedToDataScaler = simulatedToDataScaler;
    }

    public Counter getCounter() {
        return counter;
    }
}
