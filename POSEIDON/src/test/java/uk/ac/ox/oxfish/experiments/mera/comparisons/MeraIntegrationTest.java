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

package uk.ac.ox.oxfish.experiments.mera.comparisons;


import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.DoubleSummaryStatistics;
import java.util.logging.Level;

public class MeraIntegrationTest {

    @Test
    public void scenarioLandsRight() throws IOException {
        //basically this was a yes geography-1 species scenario with low landing error.
        //running it here a bunch of times to make sure that whatever else changes in the code, the landings are still right
        final Path scenarioPath = Paths.get("inputs", "tests", "mera_integration", "slice3_scenario_1.yaml");
        final DoubleSummaryStatistics averageLandings = new DoubleSummaryStatistics();
        for (int i = 0; i < 5; i++) {
            final FishState run = FishStateUtilities.run("test", scenarioPath,
                null,
                System.currentTimeMillis(),
                Level.ALL.getName(), false, null, 1, false, -1,
                null, null, null, null
            );

            //  System.out.println(run.getLatestYearlyObservation("Lutjanus malabaricus Landings"));
            averageLandings.accept(run.getLatestYearlyObservation("Lutjanus malabaricus Landings"));
        }

        final double errorRate = Math.abs(1.018056e+07 - averageLandings.getAverage()) / 1.018056e+07;
        System.out.println(errorRate);
        Assert.assertTrue(errorRate < .05);


    }
}