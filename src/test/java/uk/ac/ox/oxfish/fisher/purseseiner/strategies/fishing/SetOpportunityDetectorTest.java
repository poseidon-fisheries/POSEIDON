/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.util.Bag;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;
import static uk.ac.ox.oxfish.fisher.purseseiner.caches.FisherValuesByActionFromFileCache.ActionClasses.*;

public class SetOpportunityDetectorTest {

    @Test
    public void test() {

        final MersenneTwisterFast rng = new MersenneTwisterFast();
        final Hold hold = mock(Hold.class);
        final PurseSeineGear gear = mock(PurseSeineGear.class);
        final FadManager fadManager = mock(FadManager.class);
        final Fisher fisher = mock(Fisher.class);
        final FishState fishState = mock(FishState.class);
        final Fad ownFad = mock(Fad.class);
        final Fad otherFad = mock(Fad.class);
        final ActiveActionRegulations actionSpecificRegulations = mock(ActiveActionRegulations.class);
        final Regulation regulation = mock(Regulation.class);
        final SeaTile seaTile = mock(SeaTile.class);
        when(fishState.getStep()).thenReturn(0);
        when(fisher.grabState()).thenReturn(fishState);
        when(fisher.getHold()).thenReturn(hold);
        when(fisher.getGear()).thenReturn(gear);
        when(fisher.grabRandomizer()).thenReturn(rng);
        when(fisher.getLocation()).thenReturn(seaTile);
        when(seaTile.getGridLocation()).thenReturn(new Int2D(0, 0));
        when(seaTile.getBiology()).thenReturn(new EmptyLocalBiology());
        when(gear.getFadManager()).thenReturn(fadManager);
        when(gear.nextSetDuration(any())).thenReturn(getQuantity(1, HOUR));
        when(ownFad.getOwner()).thenReturn(fadManager);
        when(otherFad.getOwner()).thenReturn(mock(FadManager.class));
        when(fadManager.fadsAt(any())).thenAnswer(__ -> new Bag(new Object[]{ownFad, otherFad}));
        when(fadManager.getActionSpecificRegulations()).thenReturn(actionSpecificRegulations);
        when(actionSpecificRegulations.isForbidden(any())).thenReturn(false);
        when(fisher.getRegulation()).thenReturn(regulation);
        when(regulation.canFishHere(any(), any(), any())).thenReturn(true);

        @SuppressWarnings("unchecked") final List<Class<? extends AbstractSetAction>> actionClasses =
            Stream.of(DEL, FAD, NOA, OFS)
                .map(ac -> (Class<? extends AbstractSetAction>) ac.getActionClass())
                .collect(toImmutableList());

        final List<SetOpportunityGenerator> setOpportunityGenerators =
            Stream.of(
                new DolphinSetAction(fisher, mock(VariableBiomassBasedBiology.class)),
                new NonAssociatedSetAction(fisher, mock(VariableBiomassBasedBiology.class))
            ).map(action -> {
                final SetOpportunityGenerator setOpportunityGenerator =
                    mock(SetOpportunityGenerator.class);
                when(setOpportunityGenerator.get(any(), any(), any(), anyInt()))
                    .thenReturn(Optional.of(action));
                return setOpportunityGenerator;
            }).collect(toImmutableList());

        final Map<Class<? extends AbstractSetAction>, Double> basicDetectionProbabilities =
            actionClasses.stream().collect(toMap(identity(), __ -> 0.0));

        final SetOpportunityDetector setOpportunityDetector =
            new SetOpportunityDetector(
                fisher,
                setOpportunityGenerators,
                basicDetectionProbabilities,
                1.0
            );

        // when hold is full there should be no possible action
        when(hold.getPercentageFilled()).thenReturn(1.0);
        assertTrue(setOpportunityDetector.possibleSetActions().isEmpty());

        // before we search, we should only detect our own FAD
        when(hold.getPercentageFilled()).thenReturn(0.0);
        assertEquals(
            ImmutableSet.of(FAD.getActionClass()),
            setOpportunityDetector.possibleSetActions()
                .stream()
                .map(AbstractSetAction::getClass)
                .collect(toImmutableSet())
        );

        setOpportunityDetector.notifyOfSearch();
        assertEquals(
            ImmutableSet.of(
                DEL.getActionClass(), FAD.getActionClass(), NOA.getActionClass(), OFS.getActionClass()
            ),
            setOpportunityDetector.possibleSetActions()
                .stream()
                .map(AbstractSetAction::getClass)
                .collect(toImmutableSet())
        );

    }

}