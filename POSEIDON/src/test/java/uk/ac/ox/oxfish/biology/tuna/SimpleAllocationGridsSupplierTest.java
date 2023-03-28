/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.tuna;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoGravityAbundanceScenario;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class SimpleAllocationGridsSupplierTest {


    @Test
    public void clorophillMap() {


        final EpoGravityAbundanceScenario scenarioPathfinding = new EpoGravityAbundanceScenario();
        final FishState model = new FishState(0);
        model.setScenario(scenarioPathfinding);
        model.start();
        final SimpleAllocationGridsSupplier supplier = new SimpleAllocationGridsSupplier(
                Paths.get("inputs/epo_inputs/environmental_maps/chlorophyll.csv"),
                model.getMap().getMapExtent(),
                "Chlorophyll"
        );

        final AllocationGrids<String> lame = supplier.get();
        final SeaTile seaTile = model.getMap().getSeaTile(new Coordinate(-142, 40));
        double chlorophyll = lame.atOrBeforeStep(1).get("Chlorophyll").get(seaTile.getGridX(), seaTile.getGridY());
        assertEquals(chlorophyll,0.258503,.0001);
        chlorophyll = lame.atOrBeforeStep(360).get("Chlorophyll").get(seaTile.getGridX(), seaTile.getGridY());
        assertEquals(chlorophyll,0.136956,.0001);
    }
}