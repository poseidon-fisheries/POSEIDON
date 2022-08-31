package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.DiscretizedOwnFadCentroidPlanningModule;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;

public class DiscretizedOwnFadPlanningFactory implements AlgorithmFactory<DiscretizedOwnFadCentroidPlanningModule> {


    /**
     * discretizes map so that when it is time to target FADs you just
     * go through a few relevant ones
     */
    private AlgorithmFactory<? extends MapDiscretizer> discretization =
            new SquaresMapDiscretizerFactory(6,3);

    private DoubleParameter minimumValueFadSets = new FixedDoubleParameter(0d);

    private DoubleParameter distancePenalty = new FixedDoubleParameter(1d);


    private String bannedXCoordinateBounds = "";
    private String bannedYCoordinateBounds = "";

    private DoubleParameter badReadingsProbability = new FixedDoubleParameter(0d);

    @Override
    public DiscretizedOwnFadCentroidPlanningModule apply(FishState state) {

        OwnFadSetDiscretizedActionGenerator optionsGenerator = new OwnFadSetDiscretizedActionGenerator(
                new MapDiscretization(
                        discretization.apply(state)
                ),
                minimumValueFadSets.apply(state.getRandom())
        );
        if(!bannedXCoordinateBounds.isEmpty())
        {
            String[] coordinate = bannedXCoordinateBounds.split(",");
            if(coordinate.length==2)
            {
                double[] bannedX = Arrays.stream(bannedXCoordinateBounds.split(","))
                        .mapToDouble(Double::parseDouble)
                        .toArray();
                double[] bannedY = Arrays.stream(bannedYCoordinateBounds.split(","))
                        .mapToDouble(Double::parseDouble)
                        .toArray();
                optionsGenerator.setBannedGridBounds(bannedY,bannedX);
            }
        }
        optionsGenerator.setBadReadingsProbability(badReadingsProbability.apply(state.getRandom()));
        return new DiscretizedOwnFadCentroidPlanningModule(
                optionsGenerator,
                distancePenalty.apply(state.getRandom())

                );

    }

    public AlgorithmFactory<? extends MapDiscretizer> getDiscretization() {
        return discretization;
    }

    public void setDiscretization(AlgorithmFactory<? extends MapDiscretizer> discretization) {
        this.discretization = discretization;
    }

    public DoubleParameter getMinimumValueFadSets() {
        return minimumValueFadSets;
    }

    public void setMinimumValueFadSets(DoubleParameter minimumValueFadSets) {
        this.minimumValueFadSets = minimumValueFadSets;
    }

    public DoubleParameter getDistancePenalty() {
        return distancePenalty;
    }

    public void setDistancePenalty(DoubleParameter distancePenalty) {
        this.distancePenalty = distancePenalty;
    }

    public String getBannedXCoordinateBounds() {
        return bannedXCoordinateBounds;
    }

    public void setBannedXCoordinateBounds(String bannedXCoordinateBounds) {
        this.bannedXCoordinateBounds = bannedXCoordinateBounds;
    }

    public String getBannedYCoordinateBounds() {
        return bannedYCoordinateBounds;
    }

    public void setBannedYCoordinateBounds(String bannedYCoordinateBounds) {
        this.bannedYCoordinateBounds = bannedYCoordinateBounds;
    }

    public DoubleParameter getBadReadingsProbability() {
        return badReadingsProbability;
    }

    public void setBadReadingsProbability(DoubleParameter badReadingsProbability) {
        this.badReadingsProbability = badReadingsProbability;
    }
}
