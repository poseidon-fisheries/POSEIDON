/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TwoPopulationsScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.model.regs.MultipleRegulations.TAG_FOR_ALL;

/**
 * Created by carrknight on 4/4/17.
 */
public class MultipleRegulationsTest {


    @Test
    public void readFromYaml() throws Exception {

        final FishYAML yaml = new FishYAML();
        final TwoPopulationsScenario scenario = yaml.loadAs(
            new FileReader(
                Paths.get("inputs", "tests", "multiregulations.yaml").toFile()
            ),
            TwoPopulationsScenario.class
        );
        final FishState state = new FishState();
        state.setScenario(scenario);

        //there is a law that applies to both and one that only applies to large fish
        state.start();
        Assertions.assertEquals(state.getFishers().size(), 2);
        int small = 0;
        int large = 0;
        for (final Fisher fisher : state.getFishers()) {
            if (fisher.getTagsList().contains("small")) {
                Assertions.assertEquals(((MultipleRegulations) fisher.getRegulation()).getRegulations().size(), 1);
                Assertions.assertTrue(((MultipleRegulations) fisher.getRegulation()).getRegulations()
                    .get(0) instanceof MonoQuotaRegulation);
                small++;
            } else {
                Assertions.assertEquals(((MultipleRegulations) fisher.getRegulation()).getRegulations().size(), 2);
                for (final Regulation regulation : ((MultipleRegulations) fisher.getRegulation()).getRegulations()) {
                    Assertions.assertTrue(regulation instanceof MonoQuotaRegulation || regulation instanceof ProtectedAreasOnly);
                    System.out.println(regulation.getClass().getSimpleName());
                }
                large++;
            }
        }
        Assertions.assertEquals(large, 1);
        Assertions.assertEquals(small, 1);


    }

    //adapted from kitchen sink regulation
    @Test
    public void simpleChecks() throws Exception {

        final TemporaryProtectedArea mpa = mock(TemporaryProtectedArea.class);
        final FishingSeason season = mock(FishingSeason.class);
        final QuotaPerSpecieRegulation quota = mock(QuotaPerSpecieRegulation.class);

        final MultipleRegulations regs = new MultipleRegulations(
            ImmutableMap.of(
                TAG_FOR_ALL,
                ImmutableList.of(fishState -> mpa, fishState -> season, fishState -> quota)
            )
        );

        regs.start(mock(FishState.class), mock(Fisher.class));

        //same exact process for "can I be out?"
        when(mpa.allowedAtSea(any(), any(), anyInt())).thenReturn(true);
        when(season.allowedAtSea(any(), any(), anyInt())).thenReturn(true);
        when(quota.allowedAtSea(any(), any(), anyInt())).thenReturn(true);
        Assertions.assertTrue(regs.allowedAtSea(mock(Fisher.class), mock(FishState.class)));
        when(mpa.allowedAtSea(any(), any(), anyInt())).thenReturn(false);
        Assertions.assertFalse(regs.allowedAtSea(mock(Fisher.class), mock(FishState.class)));

        //all true, return true
        when(mpa.canFishHere(any(), any(), any(), anyInt())).thenReturn(true);
        when(season.canFishHere(any(), any(), any(), anyInt())).thenReturn(true);
        when(quota.canFishHere(any(), any(), any(), anyInt())).thenReturn(true);
        Assertions.assertTrue(regs.canFishHere(mock(Fisher.class), mock(SeaTile.class), mock(FishState.class)));
        //one false, return false
        when(mpa.canFishHere(any(), any(), any(), anyInt())).thenReturn(false);
        Assertions.assertFalse(regs.canFishHere(mock(Fisher.class), mock(SeaTile.class), mock(FishState.class)));
        //two/three still false
        when(season.canFishHere(any(), any(), any(), anyInt())).thenReturn(true);
        Assertions.assertFalse(regs.canFishHere(mock(Fisher.class), mock(SeaTile.class), mock(FishState.class)));
        when(quota.canFishHere(any(), any(), any(), anyInt())).thenReturn(true);
        Assertions.assertFalse(regs.canFishHere(mock(Fisher.class), mock(SeaTile.class), mock(FishState.class)));

        when(season.allowedAtSea(any(), any())).thenReturn(false);
        Assertions.assertFalse(regs.allowedAtSea(mock(Fisher.class), mock(FishState.class)));

        when(mpa.allowedAtSea(any(), any())).thenReturn(false);
        Assertions.assertFalse(regs.allowedAtSea(mock(Fisher.class), mock(FishState.class)));


        //check that calls get propagated
        final SeaTile tile = mock(SeaTile.class);
        final Fisher who = mock(Fisher.class);
        final Catch haul = mock(Catch.class);
        final FishState state = mock(FishState.class);
        regs.reactToFishing(tile, who, haul, haul, 10, state, 0);
        verify(mpa).reactToFishing(tile, who, haul, haul, 10, state, 0);
        verify(season).reactToFishing(tile, who, haul, haul, 10, state, 0);
        verify(quota).reactToFishing(tile, who, haul, haul, 10, state, 0);
        //react to sale
        final Species species = mock(Species.class);
        regs.reactToSale(species, who, 100d, 100d, state, 0);
        verify(mpa).reactToSale(species, who, 100d, 100d, state, 0);
        verify(season).reactToSale(species, who, 100d, 100d, state, 0);
        verify(quota).reactToSale(species, who, 100d, 100d, state, 0);


        //take the minimum of the two
        when(mpa.maximumBiomassSellable(any(), any(), any(), anyInt())).thenReturn(125d);
        when(season.maximumBiomassSellable(any(), any(), any(), anyInt())).thenReturn(100d);
        when(quota.maximumBiomassSellable(any(), any(), any(), anyInt())).thenReturn(200d);
        Assertions.assertEquals(100, regs.maximumBiomassSellable(who, species, state), .0001);
        when(quota.maximumBiomassSellable(any(), any(), any(), anyInt())).thenReturn(20d);
        Assertions.assertEquals(20, regs.maximumBiomassSellable(who, species, state), .0001);


    }

}
