package uk.ac.ox.poseidon.epo;

import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.regulations.core.ForbiddenIfFactory;
import uk.ac.ox.poseidon.regulations.core.NamedRegulationsFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.*;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.*;

public class ExtendedElCorralitoPolicy extends PolicySupplier {

    private final double newSouthLatitude;
    private final double newNorthLatitude;
    private final double newWestLongitude;
    private final List<Integer> numberOfExtraDays;

    ExtendedElCorralitoPolicy(
        final List<Integer> yearsActive,
        final double newSouthLatitude,
        final double newNorthLatitude,
        final double newWestLongitude,
        final List<Integer> numberOfExtraDays
    ) {
        super(yearsActive);
        this.newSouthLatitude = newSouthLatitude;
        this.newNorthLatitude = newNorthLatitude;
        this.newWestLongitude = newWestLongitude;
        this.numberOfExtraDays = numberOfExtraDays;
    }

    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return numberOfExtraDays.stream().map(extraDays ->
            new Policy<EpoScenario<?>>(
                String.format("Larger El Corralito + %02d days before/after", extraDays),
                scenario -> {
                    final NamedRegulationsFactory namedRegulations =
                        (NamedRegulationsFactory) scenario.getRegulations();
                    namedRegulations.modify(
                        "El Corralito",
                        () -> new ForbiddenIfFactory(
                            new AnyOfFactory(
                                new AllOfFactory(
                                    new NotFactory(yearsActiveCondition()),
                                    ((ForbiddenIfFactory) namedRegulations.getRegulations()
                                        .get("El Corralito"))
                                        .getCondition()
                                ),
                                new AllOfFactory(
                                    yearsActiveCondition(),
                                    new BetweenYearlyDatesFactory(
                                        addDays(EL_CORRALITO_BEGINNING, -extraDays),
                                        addDays(EL_CORRALITO_END, extraDays)
                                    ),
                                    new InRectangularAreaFactory(
                                        newNorthLatitude,
                                        newWestLongitude,
                                        newSouthLatitude,
                                        ((FixedDoubleParameter) EL_CORRALITO_AREA.getEastLongitude()).getDoubleValue()
                                    )
                                )
                            )
                        )
                    );
                }
            )
        ).collect(toImmutableList());
    }
}
