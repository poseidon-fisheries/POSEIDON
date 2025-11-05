package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.FishState;
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

/**
 * Created by Brian Powers 5/21/2025 for the Eastern Atlantic Tuna Model
 *
 * Regulations specific to the Eastern Atlantic Ocean Purse Seine tuna fishers.
 * These include the FAD closure at the end of the year, Active FAD limits, EEZs and the rectangular
 * area in the south-west which is off Brazil's coast determined to not be within the scope of the project.
 * A TAC on Bigeye is also included in the regulations
 **/

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
        final List<Integer> yearsActive = ImmutableList.of(2021, 2022, 2023, 2024);
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
                ),
                "BET TAC", new GlobalBetLimit(yearsActive, 16500)/*, // 16400 was roughly the 2022 catch.
                "YFT TAC", new GlobalYftLimit(yearsActive, 110000) // 68428.59 is the portion of the legit 110000 TAC caught by BET in 2023
*/
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
