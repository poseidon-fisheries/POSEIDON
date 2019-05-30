package uk.ac.ox.oxfish.biology.complicated;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.allocator.SnapshotBiomassAllocator;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * the plugin/additionalStartable version of AbundanceResetter + BiomassSnapshot
 */
public class SnapshotAbundanceResetter implements AdditionalStartable {



    private final HashMap<AbundanceResetter,SnapshotBiomassAllocator> resetters;



    private final int yearsBeforeReset;

    public SnapshotAbundanceResetter(GlobalBiology biology, int yearsBeforeReset) {

        this.yearsBeforeReset=yearsBeforeReset;
        resetters = new LinkedHashMap<>();
        for (Species species : biology.getSpecies()) {
            SnapshotBiomassAllocator snapper = new SnapshotBiomassAllocator();
            resetters.put(new AbundanceResetter(snapper,species),snapper);
        }

    }


    @Override
    public void start(FishState model) {
        //record biology on day 1
        model.scheduleOnce(new Steppable() {
            @Override
            public void step(SimState simState) {
                for (AbundanceResetter resetter : resetters.keySet()) {
                    resetter.recordAbundance(model.getMap());
                }
            }
        },StepOrder.DAWN);

        //reset it at year X
        model.scheduleOnceAtTheBeginningOfYear(
                new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        for (Map.Entry<AbundanceResetter, SnapshotBiomassAllocator>
                                resetter : resetters.entrySet()) {
                            resetter.getValue().takeSnapshort(
                                    model.getMap(),
                                    resetter.getKey().getSpecies());
                            resetter.getKey().resetAbundance(model.getMap(),model.getRandom());


                        }


                    }
                },
                StepOrder.DAWN,
                yearsBeforeReset
        );
    }


    @Override
    public void turnOff() {

    }
}
