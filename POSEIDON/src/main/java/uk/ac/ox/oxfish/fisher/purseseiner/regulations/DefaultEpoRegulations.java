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
import java.util.Map;

import static java.time.Month.*;

public class DefaultEpoRegulations {

    public static final Map<Integer, Map<String, Integer>> ACTIVE_FAD_LIMITS = ImmutableMap.of(
        2021, ImmutableMap.of("6A", 300, "6B", 450),
        2022, ImmutableMap.of("6A", 270, "6B", 400),
        2023, ImmutableMap.of("6A", 255, "6B", 340),
        2024, ImmutableMap.of("6A", 210, "6B", 340)
    );
    private static final Map<Integer, Integer> ADDITIONAL_CLOSURE_DAYS_BY_EXCESS_TONNES_OF_BET = ImmutableMap.of(
        1200, 10,
        1500, 13,
        1800, 16,
        2100, 19,
        2400, 22
    );
    private static final MonthDay CLOSURE_A_START = MonthDay.of(JULY, 29);
    private static final MonthDay CLOSURE_A_END = MonthDay.of(OCTOBER, 8);
    private static final MonthDay CLOSURE_B_START = MonthDay.of(NOVEMBER, 9);
    private static final MonthDay CLOSURE_B_END = MonthDay.of(JANUARY, 19);

    private DefaultEpoRegulations() {
    }

    public static AlgorithmFactory<Regulations> make(final InputPath inputFolder) {

        final InputPath regions = inputFolder.path("regions");

        final Closure closureA = new Closure("closure A", CLOSURE_A_START, CLOSURE_A_END, 15);
        final Closure closureB = new Closure("closure B", CLOSURE_B_START, CLOSURE_B_END, 15);
        return new NamedRegulations(
            ImmutableMap.of(
                "DEL licence", new ForbiddenIf(
                    new AllOf(
                        new ActionCodeIs("DEL"),
                        new Not(new AgentHasTag("has_del_license"))
                    )
                ),
                "Active-FAD limits", new ActiveFadLimits(ACTIVE_FAD_LIMITS),
                "Closure A", closureA,
                "Closure B", closureB,
                "El Corralito", new ForbiddenIf(
                    new AllOf(
                        new BetweenYearlyDates(
                            OCTOBER, 9,
                            NOVEMBER, 8
                        ),
                        new InRectangularArea(
                            4.0, -110.0, -3.0, -96.0
                        )
                    )
                ),
                "EEZs", new ForbiddenAreasFromShapeFiles(
                    regions,
                    regions.path("region_tags.csv")
                ),
                "Extended 2022 closure", new ForbiddenIf(
                    new AllOf(
                        new InYear(2022),
                        new AgentHasTag("extended_2022_closure"),
                        new AnyOf(
                            new ClosureExtensionBefore(closureA, 8),
                            new ClosureExtensionAfter(closureB, 8)
                        )
                    )
                ),
                "BET limits", new IndividualBetLimits(
                    closureA,
                    closureB,
                    ADDITIONAL_CLOSURE_DAYS_BY_EXCESS_TONNES_OF_BET,
                    ImmutableList.of(2023, 2024, 2025)
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
