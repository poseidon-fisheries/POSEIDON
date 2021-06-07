package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import sim.util.distribution.Gamma;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class FadInitializerFactory implements AlgorithmFactory<FadInitializer> {

    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);
    private Path fadCarryingCapacitiesGammaParametersFilePath =
        TunaScenario.input("fad_carrying_capacities_gamma_parameters.csv");
    private Map<String, FixedDoubleParameter> attractionRates = new HashMap<>();
    private DoubleParameter dudProbability = new FixedDoubleParameter(0d);

    @SuppressWarnings("unused")
    public Path getFadCarryingCapacitiesGammaParametersFilePath() {
        return fadCarryingCapacitiesGammaParametersFilePath;
    }

    @SuppressWarnings("unused")
    public void setFadCarryingCapacitiesGammaParametersFilePath(final Path fadCarryingCapacitiesGammaParametersFilePath) {
        this.fadCarryingCapacitiesGammaParametersFilePath = fadCarryingCapacitiesGammaParametersFilePath;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getDudProbability() { return dudProbability; }

    @SuppressWarnings("unused")
    public void setDudProbability(final DoubleParameter dudProbability) { this.dudProbability = dudProbability; }

    @SuppressWarnings("unused")
    public Map<String, FixedDoubleParameter> getAttractionRates() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return attractionRates;
    }

    @SuppressWarnings("unused")
    public void setAttractionRates(final Map<String, FixedDoubleParameter> attractionRates) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.attractionRates = attractionRates;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    @SuppressWarnings("unused")
    public void setFishReleaseProbabilityInPercent(final DoubleParameter fishReleaseProbabilityInPercent) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    @Override
    public FadInitializer apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        final SpeciesCodes speciesCodes = TunaScenario.speciesCodesSupplier.get();
        final Map<Species, DoubleSupplier> carryingCapacitySuppliers =
            parseAllRecords(fadCarryingCapacitiesGammaParametersFilePath)
                .stream()
                .collect(toImmutableMap(
                    record -> {
                        final String speciesCode = record.getString("species_code");
                        final String speciesName = speciesCodes.getSpeciesName(speciesCode);
                        return fishState.getBiology().getSpecie(speciesName);
                    },
                    record -> {
                        final Gamma gamma = new Gamma(
                            record.getDouble("shape"),
                            record.getDouble("rate"),
                            rng
                        );
                        return () -> gamma.nextDouble() * 1000; // convert from tonnes to kg
                    }
                ));

        return new FadInitializer(
            fishState.getBiology(),
            carryingCapacitySuppliers,
            attractionRates.entrySet().stream().collect(toImmutableMap(
                entry -> fishState.getBiology().getSpecie(entry.getKey()),
                entry -> entry.getValue().getFixedValue()
            )),
            fishState.getRandom(),
            fishReleaseProbabilityInPercent.apply(rng) / 100d,
            dudProbability.apply(rng),
            fishState::getStep
        );
    }
}
