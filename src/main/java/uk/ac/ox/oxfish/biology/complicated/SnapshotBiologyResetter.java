package uk.ac.ox.oxfish.biology.complicated;

import static uk.ac.ox.oxfish.model.StepOrder.DATA_RESET;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.allocator.SnapshotBiomassAllocator;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.BiomassLogger;

/**
 * the plugin/additionalStartable version of AbundanceResetter + BiomassSnapshot
 */
public class SnapshotBiologyResetter implements AdditionalStartable {



    private final HashMap<BiologyResetter,SnapshotBiomassAllocator> resetters;



    private final int yearsBeforeReset;


    /**
     * when this is true, the biomass is reset not just in total numbers but also geographically in the same way it started.
     * When this is false, the biomass is reset to original numbers but the locations to which it is distributed are the current ones
     */
    private final boolean restoreOriginalLocations;


    private SnapshotBiologyResetter(int yearsBeforeReset,
                                    HashMap<BiologyResetter,SnapshotBiomassAllocator> resetters,
                                    boolean restoreOriginalLocations)
    {
        this.yearsBeforeReset = yearsBeforeReset;
        this.resetters = resetters;
        this.restoreOriginalLocations=restoreOriginalLocations;
    }


    public static SnapshotBiologyResetter abundanceResetter(
            GlobalBiology biology,
            int yearsBeforeReset,
            boolean restoreOriginalLocations,
            boolean restoreOriginalLengthDistribution){


        LinkedHashMap<BiologyResetter,SnapshotBiomassAllocator> resetters = new LinkedHashMap<>();
        for (Species species : biology.getSpecies()) {
            if(species.isImaginary())
                continue;
            SnapshotBiomassAllocator snapper = new SnapshotBiomassAllocator();
            resetters.put(

                    restoreOriginalLengthDistribution ?
                        new AbundanceResetter(snapper,species) :
                             new AbundanceScalingResetter(snapper,species)
                    ,
                    snapper);
        }
        return new SnapshotBiologyResetter(yearsBeforeReset, resetters, restoreOriginalLocations);
    }

    public static SnapshotBiologyResetter biomassResetter(GlobalBiology biology,
                                                            int yearsBeforeReset,
                                                          boolean restoreOriginalLocations){


        LinkedHashMap<BiologyResetter,SnapshotBiomassAllocator> resetters = new LinkedHashMap<>();
        for (Species species : biology.getSpecies()) {
            if(species.isImaginary())
                continue;
            SnapshotBiomassAllocator snapper = new SnapshotBiomassAllocator();
            resetters.put(new BiomassResetter(snapper,species),snapper);
        }
        return new SnapshotBiologyResetter(yearsBeforeReset,resetters,restoreOriginalLocations);
    }

    

    @Override
    public void start(FishState model) {
        //record biology on day 1
        model.scheduleOnce(new Steppable() {
            @Override
            public void step(SimState simState) {
                for (Map.Entry<BiologyResetter, SnapshotBiomassAllocator> resetter : resetters.entrySet()) {
                    resetter.getKey().recordHowMuchBiomassThereIs(model);

                    if(restoreOriginalLocations)
                        resetter.getValue().takeSnapshort(
                                model.getMap(),
                                resetter.getKey().getSpecies());

                }

            }
        },StepOrder.DAWN);



        //reset it at year X
        model.scheduleOnceAtTheBeginningOfYear(
                new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        System.out.println("Resetted biomass at day " + model.getDay());

                        for (Map.Entry<BiologyResetter, SnapshotBiomassAllocator>
                                resetter : resetters.entrySet()) {
                            if (!restoreOriginalLocations) {
                                resetter.getValue().takeSnapshort(
                                    model.getMap(),
                                    resetter.getKey().getSpecies());
                            }

                            final double biomassBefore = ((FishState) simState)
                                .getTotalBiomass(resetter.getKey().getSpecies());

                            resetter.getKey().resetAbundance(model.getMap(), model.getRandom());

                            BiomassLogger.INSTANCE.add(
                                ((FishState) simState).getStep(),
                                DATA_RESET,
                                "RESET",
                                resetter.getKey().getSpecies(),
                                biomassBefore,
                                (((FishState) simState).getTotalBiomass(resetter.getKey().getSpecies()))
                            );

                        }


                    }
                },
                StepOrder.
                DATA_RESET,
                yearsBeforeReset
        );

//        //reset it twice: you want to make sure it gets captured by data
//        model.scheduleOnceInXDays(
//                new Steppable() {
//                    @Override
//                    public void step(SimState simState) {
//                        System.out.println("Resetted biomass at day " + model.getDay());
//                        for (Map.Entry<BiologyResetter, SnapshotBiomassAllocator>
//                                resetter : resetters.entrySet()) {
//                            resetter.getValue().takeSnapshort(
//                                    model.getMap(),
//                                    resetter.getKey().getSpecies());
//                            resetter.getKey().resetAbundance(model.getMap(),model.getRandom());
//
//
//                        }
//
//
//                    }
//                },
//                StepOrder.DAWN,
//                yearsBeforeReset*365
//        );
    }


    @Override
    public void turnOff() {

    }
}
