package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.INPUT_PATH;

public class AbundanceFadInitializerFactoryTest {


    @Test
    public void generatesDudsWhenNeeded() {


        FadInitializer<AbundanceLocalBiology, AbundanceFad> initializer = generateFakeInitializer(0.5);

        int counterOfTimesItIsZero = 0;
        for (int i = 0; i < 100; i++) {
            if(((AbstractFadInitializer) initializer).generateCarryingCapacity()==0)
                counterOfTimesItIsZero++;
        }
        System.out.println("Times it was zero: " + counterOfTimesItIsZero);
        assertTrue(counterOfTimesItIsZero>20);
        assertTrue(counterOfTimesItIsZero<80);



    }

    private FadInitializer<AbundanceLocalBiology, AbundanceFad> generateFakeInitializer(double fadDudRate) {
        String defaultConstructor =
                "Abundance FAD Initializer:\n" +
                        "  attractableBiomassCoefficients:\n" +
                        "    Bigeye tuna: '0.5719764480766927'\n" +
                        "    Yellowfin tuna: '0.6277910307768089'\n" +
                        "    Skipjack tuna: '0.5410119863916563'\n" +
                        "  biomassInteractionsCoefficients:\n" +
                        "    Bigeye tuna: '0.2874000423979856'\n" +
                        "    Yellowfin tuna: '0.3503701995411316'\n" +
                        "    Skipjack tuna: '0.5035558212092365'\n" +
                        "  compressionExponents:\n" +
                        "    Bigeye tuna: '11.55188456367076'\n" +
                        "    Yellowfin tuna: '10.996558480131561'\n" +
                        "    Skipjack tuna: '8.09163666986751'\n" +
                        "  fishReleaseProbabilityInPercent: '1.1157206090837157'\n" +
                        "  growthRates:\n" +
                        "    Bigeye tuna: '1.1542113992177854'\n" +
                        "    Yellowfin tuna: '1.0487948686421702'\n" +
                        "    Skipjack tuna: '1.2084595864666523'\n" +
                        "  selectivityFilters: {\n" +
                        "    }\n" +
                        "  speciesCodes: null\n" +
                        "  totalCarryingCapacity: '445000.0'";
        FishYAML yaml = new FishYAML();
        AbundanceFadInitializerFactory factory = yaml.loadAs(defaultConstructor,AbundanceFadInitializerFactory.class);
        SpeciesCodesFromFileFactory speciesCodesFactory =
                new SpeciesCodesFromFileFactory(INPUT_PATH.resolve("species_codes.csv"));
        factory.setSpeciesCodes(speciesCodesFactory.get());
        factory.setFadDudRate(new FixedDoubleParameter(fadDudRate));
        FishState fakeModel = mock(FishState.class,RETURNS_DEEP_STUBS);
        when(fakeModel.getRandom()).thenReturn(new MersenneTwisterFast());

        FadInitializer<AbundanceLocalBiology, AbundanceFad> initializer = factory.apply(fakeModel);
        return initializer;
    }


    @Test
    public void generatesNoDudsWhenNeeded() {


        FadInitializer<AbundanceLocalBiology, AbundanceFad> initializer = generateFakeInitializer(0);

        int counterOfTimesItIsZero = 0;
        for (int i = 0; i < 100; i++) {
            if(((AbstractFadInitializer) initializer).generateCarryingCapacity()==0)
                counterOfTimesItIsZero++;
        }
        System.out.println("Times it was zero: " + counterOfTimesItIsZero);
        assertEquals(counterOfTimesItIsZero,0);



    }
}