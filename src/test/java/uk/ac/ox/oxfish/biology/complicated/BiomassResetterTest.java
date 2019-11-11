/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.complicated;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;

public class BiomassResetterTest {




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
        BiomassLocalBiology zerozero = new BiomassLocalBiology(
                1000000,biology.getSize(),new MersenneTwisterFast()
        );
        zerozero.setCurrentBiomass(species,100);
        BiomassLocalBiology oneone = new BiomassLocalBiology(
                1000000,biology.getSize(),new MersenneTwisterFast()
        );
        oneone.setCurrentBiomass(species,100);



        fishState.getMap().getSeaTile(0,0).setBiology(zerozero);
        fishState.getMap().getSeaTile(0,1).setBiology(new BiomassLocalBiology(1000000,biology.getSize(),new MersenneTwisterFast(),0,0));
        fishState.getMap().getSeaTile(1,0).setBiology(new BiomassLocalBiology(1000000,biology.getSize(),new MersenneTwisterFast(),0,0));
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
        BiomassResetter resetter = new BiomassResetter(biomassAllocator,species);
        resetter.recordAbundance(fishState.getMap());


        //reallocate!
        resetter.resetAbundance(fishState.getMap(),new MersenneTwisterFast());


        for(int x=0; x<4;x++) {
            for (int y = 0; y < 4; y++) {

                if(x==0 && y==1)
                {
                    assertEquals(
                            fishState.getMap().getSeaTile(x,y).getBiomass(species),
                            600d,
                            .0001d
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