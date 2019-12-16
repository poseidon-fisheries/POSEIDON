package uk.ac.ox.oxfish.biology.complicated;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AbundanceResetterTest {


    @Test
    public void snapshot() {


        FishState fishState = MovingTest.generateSimple4x4Map();
        //zero them all
        for (SeaTile seaTile : fishState.getMap().getAllSeaTilesExcludingLandAsList()) {
            seaTile.setBiology(new EmptyLocalBiology());
        }

        Species species = new Species("test",
                new FromListMeristics(new double[]{1,10},2));
        GlobalBiology biology = new GlobalBiology(species);


        //fill 1x1 at top
        AbundanceLocalBiology zerozero = new AbundanceLocalBiology(
                biology
        );
        zerozero.getAbundance(species).asMatrix()[0][0]=100;
        zerozero.getAbundance(species).asMatrix()[0][1]=10;
        AbundanceLocalBiology oneone = new AbundanceLocalBiology(
                biology
        );
        oneone.getAbundance(species).asMatrix()[0][0]=100;
        oneone.getAbundance(species).asMatrix()[0][1]=10;


        fishState.getMap().getSeaTile(0,0).setBiology(zerozero);
        fishState.getMap().getSeaTile(0,1).setBiology(new AbundanceLocalBiology(biology));
        fishState.getMap().getSeaTile(1,0).setBiology(new AbundanceLocalBiology(biology));
        fishState.getMap().getSeaTile(1,1).setBiology(oneone);

        //biomass allocator wants to reallocate everythin to 0,1 (and triple it too)
        BiomassAllocator biomassAllocator = new BiomassAllocator() {
            @Override
            public double allocate(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {
                if(tile==fishState.getMap().getSeaTile(0,1))
                    return 3d;
                else
                    return 0;

            }
        };

        //record the abundance as it is
        AbundanceResetter resetter = new AbundanceResetter(biomassAllocator,species);
        resetter.recordHowMuchBiomassThereIs(fishState.getMap());


        //reallocate!
        resetter.resetAbundance(fishState.getMap(),new MersenneTwisterFast());


        for(int x=0; x<4;x++) {
            for (int y = 0; y < 4; y++) {

                if(x==0 && y==1)
                {
                    assertEquals(
                            fishState.getMap().getSeaTile(x,y).getBiomass(species),
                            1200d,
                            .0001d
                    );

                    assertArrayEquals(new double[]{600,60},
                            fishState.getMap().getSeaTile(x,y).getAbundance(species).asMatrix()[0],
                            .0001
                    );
                }
                else{
                    assertEquals(
                            fishState.getMap().getSeaTile(x,y).getBiomass(species),
                            0d,
                            .0001d
                    );
                }

            }
        }




    }

}