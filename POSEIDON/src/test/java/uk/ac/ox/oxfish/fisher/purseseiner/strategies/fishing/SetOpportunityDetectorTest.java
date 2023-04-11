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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.util.Bag;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.*;

public class SetOpportunityDetectorTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void test() {

        final MersenneTwisterFast rng = new MersenneTwisterFast();
        final Hold hold = mock(Hold.class);
        final PurseSeineGear<BiomassLocalBiology, BiomassAggregatingFad> gear = mock(PurseSeineGear.class);
        final FadManager<BiomassLocalBiology, BiomassAggregatingFad> fadManager = mock(FadManager.class);
        final Fisher fisher = mock(Fisher.class);
        final FishState fishState = mock(FishState.class);
        final BiomassAggregatingFad ownFad = mock(BiomassAggregatingFad.class);
        final BiomassAggregatingFad otherFad = mock(BiomassAggregatingFad.class);
        final ActiveActionRegulations actionSpecificRegulations =
            mock(ActiveActionRegulations.class);
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
        when(ownFad.getOwner()).thenReturn(fadManager);
        when(otherFad.getOwner()).thenReturn(mock(FadManager.class));
        when(fadManager.fadsAt(any())).thenAnswer(__ -> new Bag(new Object[]{ownFad, otherFad}));
        when(fadManager.getActionSpecificRegulations()).thenReturn(actionSpecificRegulations);
        when(actionSpecificRegulations.isForbidden(any(), any())).thenReturn(false);
        when(fisher.getRegulation()).thenReturn(regulation);
        when(regulation.canFishHere(any(), any(), any())).thenReturn(true);

        final Map<ActionClass, ? extends PurseSeinerAction> mockActions = Stream
            .of(FAD, OFS, NOA, DEL)
            .collect(toImmutableMap(
                Function.identity(),
                actionClass -> mock(actionClass.getActionClass())
            ));

        @SuppressWarnings("unchecked") final Map<SetOpportunityGenerator, Double>
            detectionProbabilities =
            ImmutableMap.of(
                    FAD, 1.0,
                    OFS, 0.0,
                    NOA, 0.0,
                    DEL, 0.0
                )
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    entry -> {
                        final PurseSeinerAction action = mockActions.get(entry.getKey());
                        final SetOpportunityGenerator setOpportunityGenerator =
                            mock(SetOpportunityGenerator.class);
                        when(setOpportunityGenerator.apply(fisher))
                            .thenReturn(ImmutableList.of(action));
                        return setOpportunityGenerator;
                    },
                    Entry::getValue
                ));

        final SetOpportunityDetector setOpportunityDetector =
            new SetOpportunityDetector(
                fisher,
                detectionProbabilities,
                1.0
            );

        // when hold is full there should be no possible action
        when(hold.getPercentageFilled()).thenReturn(1.0);
        assertFalse(setOpportunityDetector.possibleSetActions().findAny().isPresent());

        // before we search, we should only detect our own FAD
        when(hold.getPercentageFilled()).thenReturn(0.0);
        assertEquals(
            ImmutableList.of(mockActions.get(FAD)),
            setOpportunityDetector.possibleSetActions().collect(toList())
        );

        setOpportunityDetector.notifyOfSearch();
        assertEquals(
            mockActions.values(),
            setOpportunityDetector.possibleSetActions().collect(toList())
        );

    }

}