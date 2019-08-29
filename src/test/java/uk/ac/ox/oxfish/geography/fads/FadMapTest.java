package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentMaps;
import uk.ac.ox.oxfish.geography.currents.VectorGrid2D;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.assertEmptyBiology;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.assertFullBiology;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.fillBiology;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.makeBiology;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class FadMapTest {

    @Test
    public void fadBeaching() {

        // Make a 3x3 map, with a one tile island in the middle
        NauticalMap nauticalMap = makeMap(new int[][]{
            {-1, -1, -1},
            {-1, 10, -1},
            {-1, -1, -1}
        });

        final GlobalBiology globalBiology = new GlobalBiology(new Species("A"), new Species("B"));

        final Quantity<Mass> k = getQuantity(1, TONNE);
        // Make a current map that moves FADs west
        final Double2D currentVector = new Double2D(-0.3, 0);
        final Map<SeaTile, Double2D> vectors = nauticalMap
            .getAllSeaTilesExcludingLandAsList().stream()
            .collect(toMap(identity(), seaTile -> currentVector));
        for (SeaTile tile : nauticalMap.getAllSeaTilesAsList()) {
            tile.setBiology(tile.isWater() ? makeBiology(globalBiology, k) : new EmptyLocalBiology());
        }

        final VectorGrid2D vectorGrid2D = new VectorGrid2D(nauticalMap.getWidth(), nauticalMap.getHeight(), vectors);
        final CurrentMaps currentMaps = new CurrentMaps(ImmutableList.of(vectorGrid2D), t -> 0);
        final FadInitializer fadInitializer = new FadInitializer(k, 0);
        final FadMap fadMap = new FadMap(nauticalMap, currentMaps, globalBiology);
        final FadManager fadManager = new FadManager(fadMap, fadInitializer, 1);

        final Schedule schedule = mock(Schedule.class);
        final SimState simState = mock(SimState.class);
        simState.schedule = schedule;

        // Put a FAD at the East edge of the central row
        final MersenneTwisterFast random = new MersenneTwisterFast();
        final SeaTile startTile = nauticalMap.getSeaTile(2, 1);
        final Fad fad = fadManager.deployFad(startTile);
        fillBiology(fad.getBiology());
        assertEquals(Optional.of(startTile), fadMap.getFadTile(fad));
        assertFullBiology(fad.getBiology());
        assertEmptyBiology((VariableBiomassBasedBiology) startTile.getBiology());

        // If we step once, the FAD should still be in its starting tile
        // and the biologies should not have changed
        fadMap.step(simState);
        assertEquals(Optional.of(startTile), fadMap.getFadTile(fad));
        assertFullBiology(fad.getBiology());
        assertEmptyBiology((VariableBiomassBasedBiology) startTile.getBiology());

        // Let it drift to the island
        fadMap.step(simState);
        // The FAD should have been removed from the map
        assertEquals(Optional.empty(), fadMap.getFadTile(fad));
        // And the fish should be released in the starting cell
        assertEmptyBiology(fad.getBiology());
        assertFullBiology((VariableBiomassBasedBiology) startTile.getBiology());
    }

}