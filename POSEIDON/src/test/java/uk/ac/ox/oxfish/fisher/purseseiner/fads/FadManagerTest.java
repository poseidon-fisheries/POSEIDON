/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.regulation.ForbiddenIf;
import uk.ac.ox.oxfish.regulation.conditions.*;
import uk.ac.ox.oxfish.regulation.quantities.NumberOfActiveFads;
import uk.ac.ox.oxfish.regulation.quantities.YearlyActionCount;
import uk.ac.ox.poseidon.agents.api.YearlyActionCounter;
import uk.ac.ox.poseidon.agents.core.AtomicLongMapYearlyActionCounter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;

import static java.util.stream.IntStream.range;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.DPL;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.numberOfPermissibleActions;
import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class FadManagerTest {

    @Test
    public void testFadsGoBackInStockAfterSet() {

        final FadInitializer<BiomassLocalBiology, BiomassAggregatingFad> fadInitializer =
            (fadManager, owner, initialLocation, rng) -> {
                final BiomassAggregatingFad fad = mock(BiomassAggregatingFad.class);
                when(fad.getOwner()).thenReturn(fadManager);
                when(fad.getBiology()).thenReturn(new BiomassLocalBiology(new double[]{0}));
                when(fad.getLocation()).thenReturn(initialLocation);
                return fad;
            };

        final FadMap fadMap = mock(FadMap.class);

        final FadManager fadManager =
            new FadManager(fadMap, fadInitializer, null, null);

        final PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);

        final GlobalBiology globalBiology = GlobalBiology.genericListOfSpecies(1);
        final FishState fishState = mock(FishState.class);
        when(fishState.getStep()).thenReturn(1);
        when(fishState.getFadMap()).thenReturn(fadMap);
        when(fishState.getBiology()).thenReturn(globalBiology);
        when(fishState.getRegulations()).thenReturn(PERMITTED);

        final MersenneTwisterFast rng = mock(MersenneTwisterFast.class);

        final SeaTile seaTile = mock(SeaTile.class);
        final Regulation anarchy = new Anarchy();
        final Fisher fisher = mock(Fisher.class);
        when(fisher.grabState()).thenReturn(fishState);
        when(fisher.getGear()).thenReturn(purseSeineGear);
        when(fisher.grabRandomizer()).thenReturn(rng);
        when(fisher.getRegulation()).thenReturn(new Anarchy());
        when(fisher.getLocation()).thenReturn(seaTile);

        fadManager.setFisher(fisher);
        fadManager.setNumFadsInStock(10);
        final BiomassAggregatingFad fad1 = (BiomassAggregatingFad) fadManager.deployFadInCenterOfTile(seaTile, rng);

        Assertions.assertEquals(9, fadManager.getNumFadsInStock());
        Assertions.assertEquals(1, fadManager.getNumberOfActiveFads());

        // try a successful set
        when(rng.nextDouble()).thenReturn(1.0);
        new FadSetAction(fad1, fisher, 1.0)
            .act(fishState, fadManager.getFisher(), anarchy, 24);
        Assertions.assertEquals(10, fadManager.getNumFadsInStock());

        final BiomassAggregatingFad fad2 = (BiomassAggregatingFad) fadManager.deployFadInCenterOfTile(seaTile, rng);
        Assertions.assertEquals(9, fadManager.getNumFadsInStock());
        Assertions.assertEquals(2, fadManager.getNumberOfActiveFads());

        // try with a failed set
        when(rng.nextDouble()).thenReturn(1.0);
        new FadSetAction(fad2, fisher, 1.0)
            .act(fishState, fadManager.getFisher(), anarchy, 24);
        Assertions.assertEquals(10, fadManager.getNumFadsInStock());

    }

    @Test
    public void testNumberOfRemainingActions() {
        final FishState fishState = mock(FishState.class);
        final MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
        when(fishState.getRandom()).thenReturn(rng);
        when(fishState.getDate()).thenReturn(LocalDate.now());
        final Fisher fisher = mock(Fisher.class);
        when(fisher.grabState()).thenReturn(fishState);

        final AllOf fadLimit = new AllOf(
            new ActionCodeIs(DPL.name()),
            new Not(new Below(new NumberOfActiveFads(), 30))
        );
        final AllOf actionLimit = new AllOf(
            new ActionCodeIs(DPL.name()),
            new Not(new Below(new YearlyActionCount(DPL.name()), 20))
        );

        final YearlyActionCounter yearlyActionCounter = AtomicLongMapYearlyActionCounter.create();
        final AtomicLong numberOfActiveFads = new AtomicLong(5);

        final IntConsumer deploy = i -> {
            yearlyActionCounter.observe(new FadManager.DummyAction(
                DPL.name(),
                fisher,
                LocalDateTime.now(),
                yearlyActionCounter,
                numberOfActiveFads
            ));
            numberOfActiveFads.incrementAndGet();
        };

        range(0, 10).forEach(deploy);

        Assertions.assertEquals(15, numberOfPermissibleActions(
            fisher,
            new ForbiddenIf(fadLimit).apply(fishState),
            yearlyActionCounter,
            numberOfActiveFads.get(),
            DPL,
            50
        ));

        range(0, 5).forEach(deploy);

        Assertions.assertEquals(5, numberOfPermissibleActions(
            fisher,
            new ForbiddenIf(new AnyOf(fadLimit, actionLimit)).apply(fishState),
            yearlyActionCounter,
            numberOfActiveFads.get(),
            DPL,
            50
        ));

    }
}