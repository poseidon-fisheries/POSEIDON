package uk.ac.ox.poseidon.epo;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.ActiveFadLimits;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.regulations.NamedRegulations;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.ACTIVE_FAD_LIMITS;

public class ActiveFadLimitsPolicies implements PolicySupplier {

    private final int referenceYear;
    private final int yearToModify;
    private final List<Double> proportionsOfCurrentLimits;

    ActiveFadLimitsPolicies(
        final int referenceYear,
        final int yearToModify,
        final List<Double> proportionsOfCurrentLimits
    ) {
        this.referenceYear = referenceYear;
        this.yearToModify = yearToModify;
        this.proportionsOfCurrentLimits = proportionsOfCurrentLimits;
    }

    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return proportionsOfCurrentLimits
            .stream()
            .map(proportion ->
                new Policy<EpoScenario<?>>(
                    String.format(
                        "%d%% of regular active FAD limits",
                        (int) (proportion * 100)
                    ),
                    scenario ->
                        ((NamedRegulations) scenario.getRegulations()).modify(
                            "Active-FAD limits",
                            ignored -> new ActiveFadLimits(
                                ImmutableMap.<Integer, Map<String, Integer>>builder()
                                    .putAll(ACTIVE_FAD_LIMITS)
                                    .put(
                                        yearToModify,
                                        ACTIVE_FAD_LIMITS
                                            .get(referenceYear)
                                            .entrySet()
                                            .stream()
                                            .collect(toImmutableMap(
                                                Entry::getKey,
                                                entry -> (int) (entry.getValue() * proportion)
                                            ))
                                    )
                                    .buildKeepingLast()
                            )
                        )
                )
            )
            .collect(toImmutableList());
    }
}
