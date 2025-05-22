package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.regulations.ForbiddenAreasFromShapeFiles;
import uk.ac.ox.oxfish.regulations.ForbiddenIf;
import uk.ac.ox.oxfish.regulations.NamedRegulations;
import uk.ac.ox.oxfish.regulations.conditions.*;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;

import static java.time.Month.*;

public class DefaultEaoRegulations {

    public static final Map<Integer, Map<String, Integer>> ACTIVE_FAD_LIMITS = ImmutableMap.of(
        2021, ImmutableMap.of("PS", 300),
        2022, ImmutableMap.of("PS", 300),
        2023, ImmutableMap.of("PS", 300),
        2024, ImmutableMap.of("PS", 300)
    );
    private static final MonthDay CLOSURE_A_START = MonthDay.of(JANUARY, 1);
    private static final MonthDay CLOSURE_A_END = MonthDay.of(MARCH, 13);

    private DefaultEaoRegulations() {
    }

    public static AlgorithmFactory<Regulations> make(final InputPath inputFolder) {
        final InputPath regions = inputFolder.path("regions");
        final List<Integer> yearsActive = ImmutableList.of(2021, 2022, 2023);
        final TemporalClosure closureA = new TemporalClosure(
            yearsActive,
            "closure A",
            CLOSURE_A_START,
            CLOSURE_A_END,
            15
        );
        return new NamedRegulations(
            ImmutableMap.of(
                "Active-FAD limits", new ActiveFadLimits(ACTIVE_FAD_LIMITS),
                "Closure A", closureA,
               "EEZs", new ForbiddenAreasFromShapeFiles(
                    regions,
                    regions.path("region_tags.csv")
                )
            )
        );
    }

    @SuppressWarnings("SameParameterValue")
    public static MonthDay addDays(
        final MonthDay monthDay,
        final long daysToAdd
    ) {
        final int REFERENCE_YEAR = 2023;
        final LocalDate baseDate = monthDay.atYear(REFERENCE_YEAR);
        final LocalDate newDate = baseDate.plusDays(daysToAdd);
        return MonthDay.of(newDate.getMonth(), newDate.getDayOfMonth());
    }

}
