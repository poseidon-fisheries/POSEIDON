package uk.ac.ox.poseidon.epo;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.YearsActive;
import uk.ac.ox.oxfish.model.scenario.EaoScenario;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.regulations.conditions.AnyOf;
import uk.ac.ox.oxfish.regulations.conditions.InYear;

import java.util.List;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Created by Brian Powers 7/2025 for the Eastern Atlantic Tuna Model
 *
 * Basically a copy of the EPO Policy Supplier
 * Should probably be generalized into a class that can be used for any scenario.
 **/

abstract class EAOPolicySupplier implements Supplier<List<Policy<EaoScenario<?>>>> {

    static final Policy<EaoScenario<?>> CURRENT_REGULATIONS = new Policy<>(
        "Current regulations",
        scenario -> {
        }
    );

    private final List<Integer> yearsActive;

    EAOPolicySupplier(final List<Integer> yearsActive) {
        this.yearsActive = yearsActive;
    }

    public List<Integer> getYearsActive() {
        return yearsActive;
    }

    AnyOf yearsActiveCondition() {
        return new AnyOf(yearsActive.stream().map(InYear::new));
    }

    protected void deactivateForYearsActive(final YearsActive regulation) {
        regulation.setYearsActive(
            regulation.getYearsActive()
                .stream()
                .filter(year -> !yearsActive.contains(year))
                .collect(toImmutableList())
        );
    }

    List<Policy<EaoScenario<?>>> getWithDefault() {
        return ImmutableList.<Policy<EaoScenario<?>>>builder()
            .add(CURRENT_REGULATIONS)
            .addAll(get())
            .build();
    }
}
