package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.OutputPlugin;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.utility.MasonUtils;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class ExogenousFadSetterFromData extends ExogenousFadSetter implements OutputPlugin {


    /**
     * how much should the error be when the data wants us to set out of the simulation map
     */
    public final static double OUT_OF_BOUNDS_FAD_ERROR = 0;
    /**
     * how much should the error be when there is a fad set in the data but we didn't find any FAD
     * in the simulation to match it to
     */
    public final static double DEFAULT_MISSING_FAD_ERROR = 10000;
    private static final long serialVersionUID = -6460566846566873720L;
    private final Map<Integer, List<FadSetObservation>> fadSetsPerDayInData;
    private final Counter counter = new Counter(IntervalPolicy.EVERY_YEAR);
    /**
     * how much should the error be when there is a fad set in the data but we didn't find any FAD
     * in the simulation to match it to
     */
    private double missingFadError = DEFAULT_MISSING_FAD_ERROR;
    /**
     * The size of the area (in tiles) we want to search for a matching FAD.
     * Zero means only in the correct cell size
     */
    private int neighborhoodSearchSize = 0;

    /**
     * This is called to take simulated fad biomass and transform it to whatever is appropriate for comparison with data
     */
    private Function<Double, Double> simulatedToDataScaler = simulatedBiomass -> simulatedBiomass;
    private StringBuilder setLog;

    public ExogenousFadSetterFromData(final Map<Integer, List<FadSetObservation>> fadSetsPerDayInData) {
        this.fadSetsPerDayInData = fadSetsPerDayInData;
    }

    @Override
    public void start(final FishState model) {
        super.start(model);
        counter.start(model);
        counter.addColumn("Error");
        counter.addColumn("Failed Matches");
        counter.addColumn("Matches");
        counter.addColumn("Out of Bounds");
        for (final String column : counter.getValidCounters()) {
            model.getYearlyDataSet().registerGatherer(
                "Exogenous Fad Setter " + column,
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
    protected List<Fad> chooseWhichFadsToSetOnToday(final FadMap fadMap, final FishState model, final int day) {
        //only bother if there is anything to set in the data
        final List<FadSetObservation> fadSetObservationsToday = fadSetsPerDayInData.get(day);
        if (fadSetObservationsToday == null || fadSetObservationsToday.isEmpty())
            return new LinkedList<>();


        //ready containers
        final List<FadSetObservation> outOfBoundsObservations = new LinkedList<>();
        final List<FadSetObservation> observationsThatCouldNotBeMatched = new LinkedList<>();
        final List<Fad> matchedFadsToFishOut = new LinkedList<>();
        //for now, and for simplicity, let's focus on just matching FADs with observations that share the same seatile
        //first step, we need to take all the observation coordinates that were fished out today, and turn them into seatiles
        final HashMap<SeaTile, List<FadSetObservation>> fadObservations = new HashMap<>();
        for (final FadSetObservation observedSet : fadSetObservationsToday) {
            final SeaTile seaTile = model.getMap().getSeaTile(observedSet.getLocationInData());
            //if it is out of bounds, record it as out of bounds
            if (seaTile == null)
                outOfBoundsObservations.add(observedSet);
                //otherwise record it in the right spot
            else
                fadObservations.computeIfAbsent(seaTile, tile -> new LinkedList<>()).add(observedSet);

        }
        //now that we have arranged observations by area, match observations with simulated fads
        for (final Map.Entry<SeaTile, List<FadSetObservation>> setsPerTile : fadObservations.entrySet()) {
            //get all observable matches (i.e. all fads in the same tile)
            final List<Fad> matchableFads =
                MasonUtils.<Fad>bagToStream(getFadMap().fadsAt(setsPerTile.getKey()))
                    .collect(toList());
            //if you are looking in the neighborhood size...
            if (neighborhoodSearchSize > 0) {
                //get all seatile neighbors and add their fads to the matchable list
                for (final Object mooreNeighbor : model.getMap()
                    .getMooreNeighbors(setsPerTile.getKey(), neighborhoodSearchSize)) {
                    if (mooreNeighbor == setsPerTile.getKey()) //don't add yourself
                        continue;
                    matchableFads.addAll(
                        MasonUtils.<Fad>bagToStream(getFadMap().fadsAt(((SeaTile) mooreNeighbor))).collect(toList())
                    );
                }

            }
            //you have to remove already matched fads though
            matchableFads.removeAll(matchedFadsToFishOut);
            assert (matchableFads.size() == (new HashSet<>(matchableFads)).size()) : "some fads seem to appear in multiple spots";
            //sort them by size (to get consistent errors)
            matchableFads.sort((o1, o2) -> -Double.compare(
                o1.getBiology().getTotalBiomass(model.getSpecies()),
                o2.getBiology().getTotalBiomass(model.getSpecies())
            ));
            //for each observed set
            for (final FadSetObservation observedSet : setsPerTile.getValue()) {
                //get the closest simulated fad (in terms of error)
                final Optional<Fad> bestMatch = matchableFads.stream().min(Comparator.
                    comparingDouble(simulatedFad -> computeError(
                        observedSet,
                        simulatedFad,
                        model
                    )));
                //if there is such thing:
                if (bestMatch.isPresent()) {
                    //count the error
                    matchedFadsToFishOut.add(bestMatch.get());
                    final double error = computeError(observedSet, bestMatch.get(), model);
                    counter.count("Error", error);
                    //log:
                    if (setLog != null) {
                        setLog.append(day).append(",")
                            .append(setsPerTile.getKey().getGridX()).append(",")
                            .append(setsPerTile.getKey().getGridY()).append(",")
                            .append("MATCH,")
                            .append(error);
                        for (int i = 0; i < observedSet.getBiomassCaughtInData().length; i++) {
                            setLog.append(",").append(observedSet.getBiomassCaughtInData()[i]).append(",").
                                append(
                                    simulatedToDataScaler.apply(
                                        bestMatch.get().getBiology().getBiomass(model.getSpecies().get(i))));
                        }

                        setLog.append("\n");
                    }
                    //remove from matchables
                    matchableFads.remove(bestMatch.get());

                } else {
                    //otherwise count it as a miss
                    assert matchableFads.isEmpty();
                    observationsThatCouldNotBeMatched.add(observedSet);
                    if (setLog != null)
                        setLog.append(day).append(",")
                            .append(setsPerTile.getKey().getGridX()).append(",")
                            .append(setsPerTile.getKey().getGridY()).append(",")
                            .append("FAILED,")
                            .append("NaN").append("\n");
                }
            }

        }


        counter.count("Matches", matchedFadsToFishOut.size());
        counter.count("Failed Matches", observationsThatCouldNotBeMatched.size());
        counter.count("Error", observationsThatCouldNotBeMatched.size() * missingFadError);
        counter.count("Out of Bounds", outOfBoundsObservations.size());
        counter.count("Error", outOfBoundsObservations.size() * OUT_OF_BOUNDS_FAD_ERROR);

        return matchedFadsToFishOut;
    }


    //might want to make this a pluggable strategy

    /**
     * sqrt of squared error per species
     *
     * @param observation
     * @param fad
     * @return
     */
    public double computeError(
        final FadSetObservation observation,
        final Fad fad,
        final FishState state
    ) {
        final double[] data = observation.getBiomassCaughtInData();
        final double[] simulated = new double[state.getSpecies().size()];
        final LocalBiology biology = fad.getBiology();
        for (final Species species : state.getSpecies()) {
            simulated[species.getIndex()] = biology.getBiomass(species);
        }
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

    public void setSimulatedToDataScaler(final Function<Double, Double> simulatedToDataScaler) {
        this.simulatedToDataScaler = simulatedToDataScaler;
    }

    public Counter getCounter() {
        return counter;
    }

    public int getNeighborhoodSearchSize() {
        return neighborhoodSearchSize;
    }

    public void setNeighborhoodSearchSize(final int neighborhoodSearchSize) {
        this.neighborhoodSearchSize = neighborhoodSearchSize;
    }

    public void startOrResetLogger(final FishState state) {
        setLog = new StringBuilder();
        setLog.append("day,x,y,result,error");
        for (final Species species : state.getSpecies()) {
            setLog.append(",").
                append(species).append(",").append(species).append("_simulated");
        }
        setLog.append("\n");
        state.getOutputPlugins().add(this);
    }

    public double getMissingFadError() {
        return missingFadError;
    }

    public void setMissingFadError(final double missingFadError) {
        this.missingFadError = missingFadError;
    }

    @Override
    public void reactToEndOfSimulation(final FishState state) {
        //nothing happens here
    }

    @Override
    public String getFileName() {
        return "exogenous_fad_setter_log";
    }

    @Override
    public String composeFileContents() {
        return printLog();
    }

    public String printLog() {
        if (setLog == null)
            return "";
        else
            return setLog.toString();
    }
}
