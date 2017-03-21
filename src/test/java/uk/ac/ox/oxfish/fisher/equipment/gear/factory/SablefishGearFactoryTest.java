package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 3/21/17.
 */
public class SablefishGearFactoryTest {


    @Test
    public void correct() throws Exception {



        Meristics sablefish = new Meristics(59, 30 , 0.5, 25.8, 56.2, 0.419, 3.6724E-06, 3.250904,
                                            0.065, 0.5, 25.8, 64, 0.335, 3.4487E-06, 3.26681,
                                            0.08, 58, -0.13, 1, 0, 40741397,
                                            0.6, false);

        Species species = new Species("Sablefish",sablefish);

        SablefishGearFactory factory = new SablefishGearFactory(.1,
                                                                45.5128, 3.12457,0.910947,
                                                                100);
        FishState st = new FishState();
        HomogeneousAbundanceGear gear = factory.apply(st);

        //filtered and rounded if there are 10000 males in this cell, all aged 1, only 9 will actually be caught
        int[][] abundance = new int[2][sablefish.getMaxAge()+1];
        abundance[FishStateUtilities.MALE][1] = 10000;
        assertEquals(gear.filter(species, abundance)[FishStateUtilities.MALE][1],9);


    }
}