package uk.ac.ox.oxfish.biology.boxcars;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;


import java.util.LinkedList;
import java.util.List;

/**
 * collects the "real" biological count of how many fish for each bin are there in the model
 */
public class AbundanceGatherers implements AdditionalStartable {

    private final int dayOfMeasurement;

    private double[][][] abundancePerSpecies;

    /**
     * hanndy list of all the columns we create
     */
    private final List<String> columnsCreated;

    private Stoppable stoppable;


    public AbundanceGatherers(int dayOfMeasurement) {
        this.dayOfMeasurement = dayOfMeasurement;
        columnsCreated = new LinkedList<>();
    }


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {


        abundancePerSpecies = new double[model.getBiology().getSize()][][];
        for(Species species: model.getSpecies())
        {
            abundancePerSpecies[species.getIndex()]=new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

            for(int subdivision =0; subdivision<species.getNumberOfSubdivisions(); subdivision++) {
                for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                    int finalSubdivision = subdivision;
                    int finalBin = bin;
                    String columnName = species + " " + "Abundance " + subdivision + "." + bin + " at day " + dayOfMeasurement;
                    model.getYearlyDataSet().registerGatherer(
                            columnName,
                            new Gatherer<FishState>() {
                                @Override
                                public Double apply(FishState fishState) {

                                    return abundancePerSpecies[species.getIndex()][finalSubdivision][finalBin];
                                }
                            }, Double.NaN
                    );
                    columnsCreated.add(columnName);
                }

            }



        }


        //first step
        model.scheduleOnceInXDays((Steppable) simState -> updateStep(model),StepOrder.YEARLY_DATA_GATHERING,dayOfMeasurement);

        //other steps
        stoppable = model.scheduleEveryYear(new Steppable() {
            @Override
            public void step(SimState simState) {
                model.scheduleOnceInXDays(new Steppable() {
                    @Override
                    public void step(SimState simState) {

                        updateStep(model);


                    }
                }, StepOrder.YEARLY_DATA_GATHERING,dayOfMeasurement);
            }
        }, StepOrder.DAWN);



    }

    public void updateStep(FishState model) {
        for(Species species: model.getSpecies()) {
            for (int subdivision = 0; subdivision < species.getNumberOfSubdivisions(); subdivision++) {
                for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                    abundancePerSpecies[species.getIndex()][subdivision][bin] =
                            model.getTotalAbundance(species,subdivision,bin);
                }
            }
        }
    }


    public List<String> getColumnsCreated() {
        return columnsCreated;
    }


    @Override
    public void turnOff() {
        abundancePerSpecies = null;
        columnsCreated.clear();
        if(stoppable!=null)
            stoppable.stop();
    }
}
