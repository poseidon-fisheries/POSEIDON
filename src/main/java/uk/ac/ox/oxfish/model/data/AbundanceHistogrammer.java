package uk.ac.ox.oxfish.model.data;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

public class AbundanceHistogrammer implements OutputPlugin, AdditionalStartable, Steppable {

    private StringBuilder builder = new StringBuilder().append("species,subdivision,bin,abundance,day").append("\n");


    @Override
    public void step(SimState simState) {
        FishState model = (FishState) simState;
        for (Species species : model.getSpecies())
        {
            double[][] totalAbundance = model.getTotalAbundance(species);
            for (int i = 0; i < species.getNumberOfSubdivisions(); i++) {
                for (int j = 0; j < species.getNumberOfBins(); j++) {
                    builder.append(species.getName()).
                            append(",").
                            append(i).
                            append(",").
                            append(j).
                            append(",").
                            append(totalAbundance[i][j]).
                            append(",").
                            append(model.getDay()).
                            append("\n");
                }

            }


        }
    }

    @Override
    public void start(FishState model) {
        model.getOutputPlugins().add(this);
        model.scheduleEveryDay(this, StepOrder.AGGREGATE_DATA_GATHERING);
    }

    @Override
    public void reactToEndOfSimulation(FishState state) {
        //ignored
    }

    @Override
    public String getFileName() {
        return "abundance_ts.csv";
    }

    @Override
    public String composeFileContents() {
        return builder.toString();
    }
}
