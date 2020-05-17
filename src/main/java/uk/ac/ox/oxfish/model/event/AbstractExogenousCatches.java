package uk.ac.ox.oxfish.model.event;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * an abstract class that deals with data gatherrs and other basic utilities of the exogenous catch
 * but doesn't actually do any "catching" except scheduling itself every year
 */
public abstract class AbstractExogenousCatches implements  ExogenousCatches {



    protected final LinkedHashMap<Species,Double> lastExogenousCatchesMade = new LinkedHashMap<>();
    private final String columnName;
    private Stoppable stoppable;

    public AbstractExogenousCatches(
            final String dataColumnName) {
        columnName = dataColumnName;
    }



    protected List<? extends LocalBiology> getAllCatchableBiologies(FishState model) {
        return model.getMap().getAllSeaTilesExcludingLandAsList();
    }

    protected Double getFishableBiomass(Species target, LocalBiology seaTile) {

        return seaTile.getBiomass( target);
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

        //schedule yourself at the end of the year!
        model.scheduleOnceInXDays(new Steppable() {
            @Override
            public void step(SimState simState) {
                AbstractExogenousCatches.this.step(model);
                stoppable = model.scheduleEveryYear(AbstractExogenousCatches.this,
                        StepOrder.BIOLOGY_PHASE);
            }
        },StepOrder.BIOLOGY_PHASE,364);


        for(Species species : model.getSpecies())
        {
            model.getYearlyDataSet().registerGatherer(
                    columnName + species,
                    new Gatherer<FishState>() {
                        @Override
                        public Double apply(FishState state) {
                            return lastExogenousCatchesMade.get(species);
                        }
                    },
                    0
            );
        }
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

        if(stoppable!= null)
            stoppable.stop();
    }


}
