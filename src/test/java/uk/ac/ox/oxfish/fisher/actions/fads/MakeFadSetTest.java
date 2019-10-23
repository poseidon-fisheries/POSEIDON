package uk.ac.ox.oxfish.fisher.actions.fads;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.*;

public class MakeFadSetTest {

    private final GlobalBiology globalBiology = new GlobalBiology(new Species("A"), new Species("B"));

    @Test
    public void act() {

        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        FishState model = mock(FishState.class);
        SeaTile seaTile = mock(SeaTile.class);
        FadMap fadMap = mock(FadMap.class);
        FadManager fadManager = mock(FadManager.class);
        PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
        Fisher fisher = mock(Fisher.class);
        Regulation regulation = mock(Regulation.class);
        final Hold hold = mock(Hold.class);

        // Make a full FAD and an empty tile biology
        final double carryingCapacity = 0.0;
        final BiomassLocalBiology fadBiology = makeBiology(globalBiology, carryingCapacity);
        fillBiology(fadBiology);
        final Fad fad = new Fad(fadManager, fadBiology, 0, 0);
        final MakeFadSet makeFadSet = new MakeFadSet(fad);
        VariableBiomassBasedBiology tileBiology = makeBiology(globalBiology, carryingCapacity);

        // wire everything together...
        when(seaTile.getBiology()).thenReturn(tileBiology);
        when(model.getBiology()).thenReturn(globalBiology);
        when(model.getRandom()).thenReturn(random);
        when(fadMap.getFadTile(fad)).thenReturn(Optional.of(seaTile));
        when(fadManager.getFadMap()).thenReturn(fadMap);
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);
        when(fisher.getLocation()).thenReturn(seaTile);
        when(fisher.getHold()).thenReturn(hold);
        when(fisher.getRegulation()).thenReturn(regulation);
        when(fisher.isCheater()).thenReturn(false);
        when(fisher.getGear()).thenReturn(purseSeineGear);
        when(regulation.canFishHere(any(), any(), any())).thenReturn(true);

        // Before the set, FAD biology should be full and tile biology should be empty
        assertFullBiology(fadBiology);
        assertEmptyBiology(tileBiology);

        // After a successful set, FAD biology should be empty and tile biology should also be empty
        when(random.nextDouble()).thenReturn(1.0);
        makeFadSet.act(model, fisher, regulation, 0);
        assertEmptyBiology(fadBiology);
        assertEmptyBiology(tileBiology);

        // Now we refill the FAD biology and make an unsuccessfull set
        fillBiology(fadBiology);
        when(random.nextDouble()).thenReturn(0.0);
        makeFadSet.act(model, fisher, regulation, 0);

        // After that, the FAD biology should be empty and the tile biology should be full
        assertEmptyBiology(fadBiology);
        assertFullBiology(tileBiology);
    }
}