package uk.ac.ox.poseidon.epo;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.ActiveFadLimits;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.poseidon.regulations.core.NamedRegulationsFactory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.ACTIVE_FAD_LIMITS;

public class ActiveFadLimitsPolicies extends PolicySupplier {

    private final int referenceYear;
    private final List<Double> proportionsOfCurrentLimits;

    ActiveFadLimitsPolicies(
        final List<Integer> yearsActive,
        final int referenceYear,
        final List<Double> proportionsOfCurrentLimits
    ) {
        super(yearsActive);
        this.referenceYear = referenceYear;
        this.proportionsOfCurrentLimits = proportionsOfCurrentLimits;
    }

    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return proportionsOfCurrentLimits
            .stream()
            .map(proportion ->
                new Policy<EpoScenario<?>>(
                    String.format(
                        "%02d%% of regular active FAD limits",
                        (int) (proportion * 100)
                    ),
                    scenario ->
                        ((NamedRegulationsFactory) scenario.getRegulations()).modify(
                            "Active-FAD limits",
                            () -> {
                                final ImmutableMap<String, Integer> newLimits = ACTIVE_FAD_LIMITS
                                    .get(referenceYear)
                                    .entrySet()
                                    .stream()
                                    .collect(toImmutableMap(
                                        Entry::getKey,
                                        entry -> (int) (entry.getValue() * proportion)
                                    ));
                                final ImmutableMap.Builder<Integer, Map<String, Integer>> builder =
                                    ImmutableMap.<Integer, Map<String, Integer>>builder()
                                        .putAll(ACTIVE_FAD_LIMITS);
                                getYearsActive().forEach(year -> builder.put(year, newLimits));
                                return new ActiveFadLimits(builder.buildKeepingLast());
                            }
                        )
                )
            )
            .collect(toImmutableList());
    }
}
