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
public class SnapshotBiologyResetter implements AdditionalStartable {



    private final HashMap<BiologyResetter,SnapshotBiomassAllocator> resetters;



    private final int yearsBeforeReset;


    private SnapshotBiologyResetter(int yearsBeforeReset,
                                    HashMap<BiologyResetter,SnapshotBiomassAllocator> resetters)
    {
        this.yearsBeforeReset = yearsBeforeReset;
        this.resetters = resetters;
    }


    public static SnapshotBiologyResetter abundanceResetter(GlobalBiology biology,
                                                            int yearsBeforeReset){


        LinkedHashMap<BiologyResetter,SnapshotBiomassAllocator> resetters = new LinkedHashMap<>();
        for (Species species : biology.getSpecies()) {
            SnapshotBiomassAllocator snapper = new SnapshotBiomassAllocator();
            resetters.put(new AbundanceResetter(snapper,species),snapper);
        }
        return new SnapshotBiologyResetter(yearsBeforeReset,resetters);
    }

    public static SnapshotBiologyResetter biomassResetter(GlobalBiology biology,
                                                            int yearsBeforeReset){


        LinkedHashMap<BiologyResetter,SnapshotBiomassAllocator> resetters = new LinkedHashMap<>();
        for (Species species : biology.getSpecies()) {
            SnapshotBiomassAllocator snapper = new SnapshotBiomassAllocator();
            resetters.put(new BiomassResetter(snapper,species),snapper);
        }
        return new SnapshotBiologyResetter(yearsBeforeReset,resetters);
    }

    public SnapshotBiologyResetter(GlobalBiology biology, int yearsBeforeReset) {

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
                for (BiologyResetter resetter : resetters.keySet()) {
                    resetter.recordAbundance(model.getMap());
                }
            }
        },StepOrder.DAWN);

        //reset it at year X
        model.scheduleOnceAtTheBeginningOfYear(
                new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        for (Map.Entry<BiologyResetter, SnapshotBiomassAllocator>
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
