/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;

import java.util.LinkedList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TripLaggedExtractorTest {


    @Test
    public void threeTrips() {

        //tile 1 observes 2 trips: 10, 100 ---> 50
        //tile 2 observes 2 trips (but one is too old) 10, 100 (old) ---> 10
        // tile 3 observes no trips: ---> 0

        SeaTile tile1 = mock(SeaTile.class);
        SeaTile tile2 = mock(SeaTile.class);
        SeaTile tile3 = mock(SeaTile.class);

        MapDiscretization discretizer = mock(MapDiscretization.class);
        when(discretizer.getGroup(tile1)).thenReturn(0);
        when(discretizer.getGroup(tile2)).thenReturn(1);
        when(discretizer.getGroup(tile3)).thenReturn(2);
//        when(discretizer.getGroup(0)).thenReturn(Lists.newArrayList(tile1));
//        when(discretizer.getGroup(1)).thenReturn(Lists.newArrayList(tile2));
//        when(discretizer.getGroup(2)).thenReturn(Lists.newArrayList(tile3));
        when(discretizer.getNumberOfGroups()).thenReturn(3);

        //trips!
        LinkedList<TripRecord> records = new LinkedList<>();
        //trip 1: tile 1, 10$, day 500
        TripRecord record1 = mock(TripRecord.class);
        when(record1.getTripDay()).thenReturn(500);
        when(record1.getProfitPerHour(true)).thenReturn(10d);
        when(record1.getMostFishedTileInTrip()).thenReturn(tile1);
        records.add(record1);
        //trip 2: tile 1, 100$, day 500
        TripRecord record2 = mock(TripRecord.class);
        when(record2.getTripDay()).thenReturn(500);
        when(record2.getProfitPerHour(true)).thenReturn(100d);
        when(record2.getMostFishedTileInTrip()).thenReturn(tile1);
        records.add(record2);
        //trip 3: tile 2, 100$, day 500
        TripRecord record3 = mock(TripRecord.class);
        when(record3.getTripDay()).thenReturn(500);
        when(record3.getProfitPerHour(true)).thenReturn(10d);
        when(record3.getMostFishedTileInTrip()).thenReturn(tile2);
        records.add(record3);
        //trip 4: tile 2, 100$, day 5
        TripRecord record4 = mock(TripRecord.class);
        when(record4.getTripDay()).thenReturn(500);
        when(record4.getProfitPerHour(true)).thenReturn(10d);
        when(record4.getMostFishedTileInTrip()).thenReturn(tile2);
        records.add(record4);

        Fisher fakeFisher = mock(Fisher.class);
        when(fakeFisher.getFinishedTrips()).thenReturn(records);


        TripLaggedExtractor extractor = new TripLaggedExtractor(
            tripRecord -> tripRecord.getProfitPerHour(true),
            discretizer
        );
        extractor.setFisherTracked(fakeFisher);

        FishState model = mock(FishState.class);
        when(model.getDay()).thenReturn(600);

        //before starting, everything should be 0
        Assertions.assertEquals(extractor.extract(tile1, Double.NaN, fakeFisher, model), 0d, .0001);
        Assertions.assertEquals(extractor.extract(tile2, Double.NaN, fakeFisher, model), 0d, .0001);
        Assertions.assertEquals(extractor.extract(tile3, Double.NaN, fakeFisher, model), 0d, .0001);

        extractor.start(model);
        extractor.step(model);

        Assertions.assertEquals(extractor.extract(tile1, Double.NaN, fakeFisher, model), 55d, .0001);
        Assertions.assertEquals(extractor.extract(tile2, Double.NaN, fakeFisher, model), 10d, .0001);
        Assertions.assertEquals(extractor.extract(tile3, Double.NaN, fakeFisher, model), 0d, .0001);

    }


    @Test
    public void allFishersAreRecordedWhenYouPassNull() {

        //like above, but now tile 1 is visited by one fisher and tile 2 by another. TripLaggedExtractor should act fleet wide if I don't pass a value


        //tile 1 observes 2 trips: 10, 100 ---> 50
        //tile 2 observes 2 trips (but one is too old) 10, 100 (old) ---> 10
        // tile 3 observes no trips: ---> 0

        SeaTile tile1 = mock(SeaTile.class);
        SeaTile tile2 = mock(SeaTile.class);
        SeaTile tile3 = mock(SeaTile.class);

        MapDiscretization discretizer = mock(MapDiscretization.class);
        when(discretizer.getGroup(tile1)).thenReturn(0);
        when(discretizer.getGroup(tile2)).thenReturn(1);
        when(discretizer.getGroup(tile3)).thenReturn(2);
//        when(discretizer.getGroup(0)).thenReturn(Lists.newArrayList(tile1));
//        when(discretizer.getGroup(1)).thenReturn(Lists.newArrayList(tile2));
//        when(discretizer.getGroup(2)).thenReturn(Lists.newArrayList(tile3));
        when(discretizer.getNumberOfGroups()).thenReturn(3);

        //trips!
        LinkedList<TripRecord> records = new LinkedList<>();
        //trip 1: tile 1, 10$, day 500
        TripRecord record1 = mock(TripRecord.class);
        when(record1.getTripDay()).thenReturn(500);
        when(record1.getProfitPerHour(true)).thenReturn(10d);
        when(record1.getMostFishedTileInTrip()).thenReturn(tile1);
        records.add(record1);
        //trip 2: tile 1, 100$, day 500
        TripRecord record2 = mock(TripRecord.class);
        when(record2.getTripDay()).thenReturn(500);
        when(record2.getProfitPerHour(true)).thenReturn(100d);
        when(record2.getMostFishedTileInTrip()).thenReturn(tile1);
        records.add(record2);
        LinkedList<TripRecord> records2 = new LinkedList<>();
        //trip 3: tile 2, 100$, day 500
        TripRecord record3 = mock(TripRecord.class);
        when(record3.getTripDay()).thenReturn(500);
        when(record3.getProfitPerHour(true)).thenReturn(10d);
        when(record3.getMostFishedTileInTrip()).thenReturn(tile2);
        records2.add(record3);
        //trip 4: tile 2, 100$, day 5
        TripRecord record4 = mock(TripRecord.class);
        when(record4.getTripDay()).thenReturn(500);
        when(record4.getProfitPerHour(true)).thenReturn(10d);
        when(record4.getMostFishedTileInTrip()).thenReturn(tile2);
        records2.add(record4);

        Fisher fakeFisher1 = mock(Fisher.class);
        when(fakeFisher1.getFinishedTrips()).thenReturn(records);
        Fisher fakeFisher2 = mock(Fisher.class);
        when(fakeFisher2.getFinishedTrips()).thenReturn(records2);


        TripLaggedExtractor extractorFisher1 = new TripLaggedExtractor(
            tripRecord -> tripRecord.getProfitPerHour(true),
            discretizer
        );
        extractorFisher1.setFisherTracked(fakeFisher1);
        TripLaggedExtractor fleetwideExtractor = new TripLaggedExtractor(
            tripRecord -> tripRecord.getProfitPerHour(true),
            discretizer
        );

        FishState model = mock(FishState.class);
        when(model.getDay()).thenReturn(600);
        when(model.getFishers()).thenReturn(ObservableList.observableList(
            Lists.newArrayList(fakeFisher1, fakeFisher2)));

        extractorFisher1.start(model);
        extractorFisher1.step(model);
        fleetwideExtractor.start(model);
        fleetwideExtractor.step(model);


        Assertions.assertEquals(extractorFisher1.extract(tile1, Double.NaN, null, model), 55d, .0001);
        Assertions.assertEquals(extractorFisher1.extract(tile2, Double.NaN, null, model), 0d, .0001);
        Assertions.assertEquals(extractorFisher1.extract(tile3, Double.NaN, null, model), 0d, .0001);
        Assertions.assertEquals(fleetwideExtractor.extract(tile1, Double.NaN, null, model), 55d, .0001);
        Assertions.assertEquals(fleetwideExtractor.extract(tile2, Double.NaN, null, model), 10d, .0001);
        Assertions.assertEquals(fleetwideExtractor.extract(tile3, Double.NaN, null, model), 0d, .0001);

    }
}
