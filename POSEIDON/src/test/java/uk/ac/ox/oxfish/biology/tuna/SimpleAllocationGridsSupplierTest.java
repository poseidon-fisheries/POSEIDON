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


        EpoGravityAbundanceScenario scenarioPathfinding = new EpoGravityAbundanceScenario();
        FishState model = new FishState(0);
        model.setScenario(scenarioPathfinding);
        model.start();
        SimpleAllocationGridsSupplier supplier = new SimpleAllocationGridsSupplier(
                Paths.get("inputs/tests/clorophill.csv"),
                model.getMap().getMapExtent(),
                "Clorophill"
        );

        AllocationGrids<String> lame = supplier.get();
        SeaTile seaTile = model.getMap().getSeaTile(new Coordinate(-142, 40));
        double clorophill = lame.atOrBeforeStep(1).get("Clorophill").get(seaTile.getGridX(), seaTile.getGridY());
        assertEquals(clorophill,0.258503,.0001);
        clorophill = lame.atOrBeforeStep(360).get("Clorophill").get(seaTile.getGridX(), seaTile.getGridY());
        assertEquals(clorophill,0.136956,.0001);
    }
}