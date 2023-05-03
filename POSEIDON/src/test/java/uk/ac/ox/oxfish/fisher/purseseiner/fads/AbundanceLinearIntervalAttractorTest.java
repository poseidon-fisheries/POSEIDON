package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AbundanceLinearIntervalAttractorTest {


    @Test
    public void computesDailyAttractionCorrectly() {

        final Species species = new Species(
            "Species 0",
            //age 0: 1kg; age 1:2kg
            new FromListMeristics(
                new double[]{1, 2},
                1
            )
        );

        //fake selectivity: [.1,.5]
        final HashMap<Species, NonMutatingArrayFilter> selectivityFilters = new HashMap<>();
        selectivityFilters.put(species, new NonMutatingArrayFilter(new double[]{.1, .5}));


        final FishState state = mock(FishState.class);
        when(state.getSpecies()).thenReturn(ImmutableList.of(species));
        final AbundanceLinearIntervalAttractor attractor = new AbundanceLinearIntervalAttractor(
            5, 10,
            new double[]{800}, //80kg a day
            1,
            selectivityFilters,
            state

        );


        //100 age 0, 2 age 1 fish
        final double[][] totalAbundance = new double[1][2];
        totalAbundance[0][0] = 100;
        totalAbundance[0][1] = 2;
        when(state.getTotalAbundance(species)).thenReturn(totalAbundance);

        //each day you want to attract 80kg!
        attractor.step(state);
        assertEquals(attractor.getDailyAttractionStep().getTotalWeight(),
            80.0, .0001
        );

        assertEquals(attractor.getDailyAttractionStep()
                .getObjectBeingWeighted()
                .getAbundance(species)
                .getAbundance(0, 0),
            80 * 10d / 12d, .0001
        );
        assertEquals(attractor.getDailyAttractionStep()
                .getObjectBeingWeighted()
                .getAbundance(species)
                .getAbundance(0, 1),
            80d / 12d, .0001
        );

    }


    @Test
    public void onlyAttractsWhenNeeded() {

        final Species species = new Species(
            "Species 0",
            //age 0: 1kg; age 1:2kg
            new FromListMeristics(
                new double[]{1, 2},
                1
            )
        );

        //fake selectivity: [.1,.5]
        final HashMap<Species, NonMutatingArrayFilter> selectivityFilters = new HashMap<>();
        selectivityFilters.put(species, new NonMutatingArrayFilter(new double[]{.1, .5}));


        final FishState state = mock(FishState.class);
        when(state.getSpecies()).thenReturn(ImmutableList.of(species));
        final AbundanceLinearIntervalAttractor attractor = new AbundanceLinearIntervalAttractor(
            5, 10,
            new double[]{800}, //80kg a day
            1,
            selectivityFilters,
            state

        );


        //100 age 0, 2 age 1 fish
        final double[][] totalAbundance = new double[1][2];
        totalAbundance[0][0] = 100;
        totalAbundance[0][1] = 2;
        when(state.getTotalAbundance(species)).thenReturn(totalAbundance);

        //each day you want to attract 80kg!
        attractor.step(state);
        assertEquals(attractor.getDailyAttractionStep().getTotalWeight(),
            80.0, .0001
        );

        assertEquals(attractor.getDailyAttractionStep()
                .getObjectBeingWeighted()
                .getAbundance(species)
                .getAbundance(0, 0),
            80 * 10d / 12d, .0001
        );
        assertEquals(attractor.getDailyAttractionStep()
                .getObjectBeingWeighted()
                .getAbundance(species)
                .getAbundance(0, 1),
            80d / 12d, .0001
        );

        //too early!
        final AbundanceAggregatingFad fad = mock(AbundanceAggregatingFad.class);
        when(fad.getCarryingCapacity()).thenReturn(new PerSpeciesCarryingCapacity(new double[]{800}));
        final AbundanceLocalBiology fadBiology = mock(AbundanceLocalBiology.class, RETURNS_DEEP_STUBS);
        when(fad.getBiology()).thenReturn(fadBiology);
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{0, 0})
        ));
        final AbundanceLocalBiology localBiology = mock(AbundanceLocalBiology.class, RETURNS_DEEP_STUBS);
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{999999, 999999})
        ));
        when(fad.getStepDeployed()).thenReturn(1);
        when(state.getDay()).thenReturn(3);

        assertNull(
            attractor.attractImplementation(localBiology, fad)
        );

        //too full
        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{999999});
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{999999, 999999})
        ));
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{999999, 999999})
        ));
        when(fad.getStepDeployed()).thenReturn(1);
        when(state.getDay()).thenReturn(99999999);

        assertNull(
            attractor.attractImplementation(localBiology, fad)
        );

        //empty local biology
        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{0});
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{0, 0})
        ));
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{1, 1})
        ));
        when(fad.getStepDeployed()).thenReturn(1);
        when(state.getDay()).thenReturn(99999999);

        assertNull(
            attractor.attractImplementation(localBiology, fad)
        );

        //valid
        when(fadBiology.getCurrentBiomass()).thenReturn(new double[]{0});
        when(fadBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{0, 0})
        ));
        when(localBiology.getStructuredAbundance()).thenReturn(ImmutableMap.of(
            species,
            new StructuredAbundance(new double[]{999999, 99999})
        ));
        when(fad.getStepDeployed()).thenReturn(1);
        when(fad.isActive()).thenReturn(true);
        when(state.getDay()).thenReturn(99999999);

        assertNotNull(
            attractor.attractImplementation(localBiology, fad)
        );
    }
}