/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.biology.initializer.allocator.SnapshotBiomassAllocator;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.ConstantAttractionProbabilityFunction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.GlobalCarryingCapacityInitializer;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LinearFishBiomassAttractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.geography.currents.CurrentVectorsEPO;
import uk.ac.ox.oxfish.geography.fads.BiomassFadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BiomassResetterTest {

    @Test
    public void snapshot() {

        final FishState fishState = MovingTest.generateSimple4x4Map();
        //zero them all
        for (final SeaTile seaTile : fishState.getMap().getAllSeaTilesExcludingLandAsList()) {
            seaTile.setBiology(new EmptyLocalBiology());
        }

        final Species species = new Species(
            "test",
            new FromListMeristics(new double[]{1, 10}, 2)
        );
        final GlobalBiology biology = new GlobalBiology(species);

        //fill 1x1 at top
        final VariableBiomassBasedBiology zerozero = new BiomassLocalBiology(
            1000000, biology.getSize(), new MersenneTwisterFast()
        );
        zerozero.setCurrentBiomass(species, 100);
        final VariableBiomassBasedBiology oneone = new BiomassLocalBiology(
            1000000, biology.getSize(), new MersenneTwisterFast()
        );
        oneone.setCurrentBiomass(species, 100);

        fishState.getMap().getSeaTile(0, 0).setBiology(zerozero);
        fishState.getMap()
            .getSeaTile(0, 1)
            .setBiology(new BiomassLocalBiology(
                1000000,
                biology.getSize(),
                new MersenneTwisterFast(),
                0,
                0
            ));
        fishState.getMap()
            .getSeaTile(1, 0)
            .setBiology(new BiomassLocalBiology(
                1000000,
                biology.getSize(),
                new MersenneTwisterFast(),
                0,
                0
            ));
        fishState.getMap().getSeaTile(1, 1).setBiology(oneone);

        //biomass allocator wants to reallocate everythin to 0,1 (and triple it too)
        final BiomassAllocator biomassAllocator = (tile, map, random) ->
            tile == fishState.getMap().getSeaTile(0, 1) ? 3d : 0;

        //record the abundance as it is
        final BiologyResetter resetter = new BiomassResetter(biomassAllocator, species);
        resetter.recordHowMuchBiomassThereIs(fishState);

        //reallocate!
        resetter.resetAbundance(fishState.getMap(), new MersenneTwisterFast());

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {

                if (x == 0 && y == 1) {
                    assertEquals(
                        fishState.getMap().getSeaTile(x, y).getBiomass(species),
                        600d,
                        .0001d
                    );

                } else {
                    assertEquals(
                        fishState.getMap().getSeaTile(x, y).getBiomass(species),
                        0d,
                        .0001d
                    );
                }

            }
        }

    }

    @Test
    public void snapshotWithFADs() {

        final FishState fishState = MovingTest.generateSimple4x4Map();
        final MersenneTwisterFast rng = new MersenneTwisterFast();
        when(fishState.getRandom()).thenReturn(rng);

        final List<SeaTile> seaTiles = fishState.getMap().getAllSeaTilesExcludingLandAsList();

        final GlobalBiology globalBiology = new GlobalBiology(new Species("A"), new Species("B"));
        final List<Species> species = globalBiology.getSpecies();

        // assign some random biomass to sea tiles
        final Quantity<Mass> oneTonne = getQuantity(1, TONNE);
        final double k = asDouble(oneTonne, KILOGRAM);
        seaTiles.forEach(seaTile -> seaTile.setBiology(new BiomassLocalBiology(
            k,
            globalBiology.getSize(),
            rng
        )));

        final CurrentVectors currentVectors = new CurrentVectorsEPO(
            new TreeMap<>(),
            1,
            fishState.getMap().getWidth(),
            fishState.getMap().getHeight()
        );
        final FadMap fadMap = new FadMap(
            fishState.getMap(),
            currentVectors,
            globalBiology,
            BiomassLocalBiology.class
        );
        when(fishState.getFadMap()).thenReturn(fadMap);

        // deploy one FAD in the center of each tile
        final double carryingCapacity = 1000;
        final double fadAttractionRate = 0.5;

        final double[] attractionRates = new double[species.size()];
        Arrays.fill(attractionRates, fadAttractionRate);

        final LinearFishBiomassAttractor fishBiomassAttractor =
            new LinearFishBiomassAttractor(
                species,
                new ConstantAttractionProbabilityFunction(1.0),
                attractionRates,
                new MersenneTwisterFast()
            );

        final BiomassFadInitializer fadInitializer = new BiomassFadInitializer(
            globalBiology,
            fishBiomassAttractor,
            0,
            () -> 0,
            new GlobalCarryingCapacityInitializer(new FixedDoubleParameter(carryingCapacity))
        );

        when(fishState.getBiology()).thenReturn(globalBiology);
        final Fisher fisher = mock(Fisher.class);
        when(fisher.grabState()).thenReturn(fishState);
        when(fisher.grabRandomizer()).thenReturn(rng);
        final FadManager fadManager = mock(FadManager.class, RETURNS_DEEP_STUBS);
        when(fadManager.getFisher()).thenReturn(fisher);
        seaTiles.forEach(seaTile -> {
            when(fisher.getLocation()).thenReturn(seaTile);
            fadMap.deployFad(
                fadInitializer.makeFad(fadManager, fisher, seaTile, rng),
                seaTile
            );
        });

        // record total biomass
        final ImmutableList<LocalBiology> seaTileBiologies =
            seaTiles.stream().map(SeaTile::getBiology).collect(toImmutableList());
        final ImmutableMap<Species, Double> initialSeaTileBiomasses =
            totalBiomasses(globalBiology, seaTileBiologies);

        // record the abundance as it is
        final ImmutableMap<Species, SnapshotBiomassAllocator> allocators =
            species.stream()
                .collect(toImmutableMap(identity(), __ -> new SnapshotBiomassAllocator()));
        final ImmutableList<BiomassResetter> resetters =
            species.stream()
                .map(s -> new BiomassResetter(allocators.get(s), s))
                .collect(toImmutableList());
        resetters.forEach(resetter -> resetter.recordHowMuchBiomassThereIs(fishState));
        allocators.forEach((s, allocator) -> allocator.takeSnapshort(fishState.getMap(), s));

        // transfer some biomass to FADs
        fadMap.step(fishState);

        final ImmutableList<LocalBiology> fadBiologies =
            fadMap.allFads().map(fad -> fad.getBiology()).collect(toImmutableList());
        final ImmutableMap<Species, Double> initialFadBiomasses =
            totalBiomasses(globalBiology, fadBiologies);

        // Check that FADs have attracted the right biomass
        species.forEach(s -> assertEquals(
            initialSeaTileBiomasses.get(s) * fadAttractionRate,
            initialFadBiomasses.get(s),
            EPSILON
        ));

        // Check that the tile biomasses have changed
        assertNotEquals(totalBiomasses(globalBiology, seaTileBiologies), initialSeaTileBiomasses);

        // Wipe the cell biomasses
        species.forEach(s ->
            seaTileBiologies.forEach(localBiology ->
                ((VariableBiomassBasedBiology) localBiology).setCurrentBiomass(s, 0)
            )
        );

        // reallocate!
        resetters.forEach(resetter -> resetter.resetAbundance(fishState.getMap(), rng));

        // Check that the FAD biomasses are unaffected
        final ImmutableMap<Species, Double> finalFadBiomasses =
            totalBiomasses(globalBiology, fadBiologies);
        assertEquals(finalFadBiomasses, initialFadBiomasses);

        // Check that the sum of current tile and FAD biomasses is equal to the initial biomass
        final ImmutableMap<Species, Double> finalSeaTileBiomasses =
            totalBiomasses(globalBiology, seaTileBiologies);
        species.forEach(s -> assertEquals(
            finalFadBiomasses.get(s) + finalSeaTileBiomasses.get(s),
            initialSeaTileBiomasses.get(s),
            EPSILON
        ));

    }

    private static ImmutableMap<Species, Double> totalBiomasses(
        final GlobalBiology globalBiology,
        final Collection<LocalBiology> localBiologies
    ) {
        return globalBiology.getSpecies().stream().collect(toImmutableMap(
            identity(),
            species -> localBiologies.stream()
                .mapToDouble(localBiology -> localBiology.getBiomass(species))
                .sum()
        ));
    }

}