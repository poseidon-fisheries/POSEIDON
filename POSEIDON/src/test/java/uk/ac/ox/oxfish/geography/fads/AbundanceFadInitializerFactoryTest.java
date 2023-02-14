package uk.ac.ox.oxfish.geography.fads;

import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.INPUT_PATH;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputFile;
import uk.ac.ox.oxfish.model.scenario.InputFolder;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.nio.file.Paths;

public class AbundanceFadInitializerFactoryTest {

    private long generateAndCountDuds(final double fadDudRate, final int numFadsToGenerate) {
        final AbstractFadInitializer<?, ?> initializer =
            (AbstractFadInitializer<?, ?>)generateFakeInitializer(fadDudRate);
        return range(0, numFadsToGenerate)
            .filter(__ -> initializer.generateCarryingCapacity() == 0)
            .count();
    }

    @Test
    public void generatesDudsWhenNeeded() {
        final long counterOfTimesItIsZero = generateAndCountDuds(0.5, 100);
        System.out.println("Times it was zero: " + counterOfTimesItIsZero);
        assertTrue(counterOfTimesItIsZero > 20);
        assertTrue(counterOfTimesItIsZero < 80);
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
                new SpeciesCodesFromFileFactory(new InputFile(new InputFolder(INPUT_PATH), Paths.get("species_codes.csv")));
        final SpeciesCodes speciesCodes = speciesCodesFactory.get();
        factory.setSpeciesCodes(speciesCodes);
        factory.setFadDudRate(new FixedDoubleParameter(fadDudRate));
        FishState fakeModel = mock(FishState.class,RETURNS_DEEP_STUBS);
        when(fakeModel.getRandom()).thenReturn(new MersenneTwisterFast());
        when(fakeModel.getBiology()).thenReturn(
            GlobalBiology.fromNames(speciesCodes.getSpeciesNames())
        );

        FadInitializer<AbundanceLocalBiology, AbundanceFad> initializer = factory.apply(fakeModel);
        return initializer;
    }


    @Test
    public void generatesNoDudsWhenNeeded() {
        final long counterOfTimesItIsZero = generateAndCountDuds(0, 100);
        System.out.println("Times it was zero: " + counterOfTimesItIsZero);
        assertEquals(counterOfTimesItIsZero, 0);
    }
}