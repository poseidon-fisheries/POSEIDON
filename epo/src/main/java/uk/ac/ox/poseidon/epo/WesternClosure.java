package uk.ac.ox.poseidon.epo;

import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.regulations.ForbiddenIf;
import uk.ac.ox.oxfish.regulations.NamedRegulations;
import uk.ac.ox.oxfish.regulations.conditions.AllOf;
import uk.ac.ox.oxfish.regulations.conditions.BetweenYearlyDates;
import uk.ac.ox.oxfish.regulations.conditions.InRectangularArea;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.*;

public class WesternClosure extends PolicySupplier {

    private final double eastLongitude;
    private final List<Integer> numberOfExtraDays;

    public WesternClosure(
        final List<Integer> yearsActive,
        final double eastLongitude,
        final List<Integer> numberOfExtraDays
    ) {
        super(yearsActive);
        this.eastLongitude = eastLongitude;
        this.numberOfExtraDays = numberOfExtraDays;
    }

    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return numberOfExtraDays.stream().map(extraDays ->
            new Policy<EpoScenario<?>>(
                String.format(
                    "West of %.0f, %02d days before/after El Corralito",
                    eastLongitude,
                    extraDays
                ),
                epoScenario -> {
                    final NamedRegulations namedRegulations = (NamedRegulations) epoScenario.getRegulations();
                    namedRegulations.modify(
                        "Western closure",
                        () -> new ForbiddenIf(
                            new AllOf(
                                yearsActiveCondition(),
                                new BetweenYearlyDates(
                                    addDays(EL_CORRALITO_BEGINNING, -extraDays),
                                    addDays(EL_CORRALITO_END, extraDays)
                                ),
                                new InRectangularArea(
                                    50,
                                    -150,
                                    -50,
                                    eastLongitude
                                )
                            )
                        )
                    );
                }
            )
        ).collect(toImmutableList());
    }
}
