/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.log;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.mockito.Mockito.mock;

public class FishingRecordTest {


    @Test
    public void biomassOnlySum() {

        SeaTile commonTile = mock(SeaTile.class);

        FishingRecord record1 = new FishingRecord(
            1,
            commonTile,
            new Catch(new double[]{100, 200})
        );
        FishingRecord record2 = new FishingRecord(
            1,
            commonTile,
            new Catch(new double[]{100, 200})
        );

        FishingRecord record3 = new FishingRecord(
            1,
            commonTile,
            new Catch(new double[]{10, 20})
        );


        FishingRecord sum = FishingRecord.sumRecords(FishingRecord.sumRecords(record1, record2), record3);
        Assertions.assertArrayEquals(sum.getFishCaught().getBiomassArray(), new double[]{210, 420}, .001);

    }

    @Test
    public void abundanceOnlySum() {

        //set up copied from the holdsize test
        Meristics first = new FromListMeristics(new double[]{100, 100, 100}, 2);
        Meristics second = new FromListMeristics(new double[]{100, 100}, 2);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second", second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);
        SeaTile commonTile = mock(SeaTile.class);

        FishingRecord record1 = new FishingRecord(
            1,
            commonTile,
            new Catch(
                new double[]{0, 10, 0},
                new double[]{10, 0, 0},
                firstSpecies,
                bio

            )
        );
        FishingRecord record2 = new FishingRecord(
            1,
            commonTile,
            new Catch(
                new double[]{0, 2},
                new double[]{0, 0},
                secondSpecies,
                bio

            )
        );
        FishingRecord record3 = new FishingRecord(
            1,
            commonTile,
            new Catch(
                new double[]{0, 2},
                new double[]{0, 0},
                secondSpecies,
                bio

            )
        );
        FishingRecord sum = FishingRecord.sumRecords(FishingRecord.sumRecords(record1, record2), record3);
        Assertions.assertEquals(sum.getFishCaught().getAbundance(0).getAbundance(0, 1), 10, .001);
        Assertions.assertEquals(sum.getFishCaught().getAbundance(1).getAbundance(0, 1), 4, .001);
    }
}
