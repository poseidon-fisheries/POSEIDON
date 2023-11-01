package uk.ac.ox.oxfish.fisher.purseseiner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.regulations.ForbiddenAreasFromShapeFiles;
import uk.ac.ox.oxfish.regulations.ForbiddenIf;
import uk.ac.ox.oxfish.regulations.NamedRegulations;
import uk.ac.ox.oxfish.regulations.conditions.*;
import uk.ac.ox.oxfish.regulations.quantities.LastYearlyFisherValue;
import uk.ac.ox.oxfish.regulations.quantities.NumberOfActiveFads;
import uk.ac.ox.oxfish.regulations.quantities.SecondLastYearlyFisherValue;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Map;
import java.util.Map.Entry;

import static java.time.Month.*;

public class DefaultEpoRegulations {

    public static final Map<Integer, Map<String, Integer>> ACTIVE_FAD_LIMITS = ImmutableMap.of(
        2021, ImmutableMap.of("6A", 300, "6B", 450),
        2022, ImmutableMap.of("6A", 270, "6B", 400),
        2023, ImmutableMap.of("6A", 255, "6B", 340),
        2024, ImmutableMap.of("6A", 210, "6B", 340)
    );
    private static final ImmutableSet<Entry<Integer, Integer>> ADDITIONAL_CLOSURE_DAYS_BY_EXCESS_TONNES_OF_BET = ImmutableMap.of(
        1200, 10,
        1500, 13,
        1800, 16,
        2100, 19,
        2400, 22
    ).entrySet();
    private static final MonthDay CLOSURE_A_START = MonthDay.of(JULY, 29);
    private static final MonthDay CLOSURE_A_END = MonthDay.of(OCTOBER, 8);
    private static final MonthDay CLOSURE_B_START = MonthDay.of(NOVEMBER, 9);
    private static final MonthDay CLOSURE_B_END = MonthDay.of(JANUARY, 19);

    private DefaultEpoRegulations() {
    }

    public static AlgorithmFactory<Regulations> make(final InputPath inputFolder) {

        final InputPath regions = inputFolder.path("regions");

        return new NamedRegulations(
            ImmutableMap.of(
                "DEL licence", new ForbiddenIf(
                    new AllOf(
                        new ActionCodeIs("DEL"),
                        new Not(new AgentHasTag("has_del_license"))
                    )
                ),
                "Active-FAD limits", makeActiveFadLimits(ACTIVE_FAD_LIMITS),
                // Forbid deployments 15 days before closure
                "Closure A", new ForbiddenIf(
                    new AnyOf(
                        new AllOf(
                            // Forbid deployments 15 days before closure
                            new AgentHasTag("closure A"),
                            new ActionCodeIs("DPL"),
                            new BetweenYearlyDates(
                                addDays(CLOSURE_A_START, -15),
                                addDays(CLOSURE_A_START, -1)
                            )
                        ),
                        new AllOf(
                            new AgentHasTag("closure A"),
                            new BetweenYearlyDates(
                                CLOSURE_A_START,
                                CLOSURE_A_END
                            )
                        )
                    )
                ),
                "Closure B", new ForbiddenIf(
                    new AnyOf(
                        // Forbid deployments 15 days before closure
                        new AllOf(
                            new AgentHasTag("closure B"),
                            new ActionCodeIs("DPL"),
                            new BetweenYearlyDates(
                                addDays(CLOSURE_B_START, -15),
                                addDays(CLOSURE_B_START, -1)
                            )
                        ),
                        new AllOf(
                            new AgentHasTag("closure B"),
                            new BetweenYearlyDates(
                                CLOSURE_B_START,
                                CLOSURE_B_END
                            )
                        )
                    )
                ),
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
                        new AgentHasTag("extended_2022_closure"),
                        new AnyOf(
                            new AllOf(
                                new AgentHasTag("closure A"),
                                new BetweenDates(
                                    CLOSURE_A_START.atYear(2022).minusDays(8),
                                    CLOSURE_A_START.atYear(2022).minusDays(1)
                                )
                            ),
                            new AllOf(
                                new AgentHasTag("closure B"),
                                new BetweenDates(
                                    CLOSURE_B_END.atYear(2023).plusDays(1),
                                    CLOSURE_B_END.atYear(2023).plusDays(8)
                                )
                            )
                        )
                    )
                ),
                "BET limits", new ForbiddenIf(
                    new AllOf(
                        new BetweenDates(
                            LocalDate.of(2023, JANUARY, 1),
                            LocalDate.of(2025, DECEMBER, 31)
                        ),
                        new AnyOf(
                            new AllOf(
                                new AgentHasTag("closure A"),
                                new AnyOf(
                                    ADDITIONAL_CLOSURE_DAYS_BY_EXCESS_TONNES_OF_BET.stream().map(entry -> {
                                            final MonthDay newClosureStart = addDays(
                                                CLOSURE_A_START,
                                                entry.getValue() * -1L
                                            );
                                            return new AllOf(
                                                new Above(
                                                    new LastYearlyFisherValue("Bigeye tuna Catches (kg)"),
                                                    entry.getKey() * 1000 // convert from tonnes to kg
                                                ),
                                                new AnyOf(
                                                    // closure extension
                                                    new BetweenYearlyDates(
                                                        newClosureStart,
                                                        addDays(CLOSURE_A_START, -1)
                                                    ),
                                                    // pre-closure DPL ban
                                                    new AllOf(
                                                        new ActionCodeIs("DPL"),
                                                        new BetweenYearlyDates(
                                                            addDays(newClosureStart, -15),
                                                            addDays(newClosureStart, -1)
                                                        )
                                                    )
                                                )
                                            );
                                        }
                                    )
                                )
                            ),
                            new AllOf(
                                new AgentHasTag("closure B"),
                                new AnyOf(
                                    ADDITIONAL_CLOSURE_DAYS_BY_EXCESS_TONNES_OF_BET.stream().map(entry ->
                                        // This gets slightly complicated because we need to check for the catches
                                        // the year before the closure _starts_, and once we get to Jan 1st, that
                                        // not "last year" anymore, but the year before that, hence those different
                                        // conditions depending on where we are in the year. March 30 is an arbitrary
                                        // cutoff for the different checks. At least we do not need to deal with the
                                        // pre-closure DPL ban, since closure B gets extended at the end.
                                        new AllOf(
                                            new AnyOf(
                                                new AllOf(
                                                    new BetweenYearlyDates(JANUARY, 1, MARCH, 30),
                                                    new Above(
                                                        new SecondLastYearlyFisherValue("Bigeye tuna Catches (kg)"),
                                                        entry.getKey() * 1000 // convert from tonnes to kg
                                                    )
                                                ),
                                                new AllOf(
                                                    new BetweenYearlyDates(APRIL, 1, DECEMBER, 31),
                                                    new Above(
                                                        new LastYearlyFisherValue("Bigeye tuna Catches (kg)"),
                                                        entry.getKey() * 1000 // convert from tonnes to kg
                                                    )
                                                )
                                            ),
                                            new BetweenYearlyDates(
                                                addDays(CLOSURE_B_END, 1),
                                                addDays(CLOSURE_B_END, entry.getValue())
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    /**
     * TODO: this should be its own factory class
     */
    public static ForbiddenIf makeActiveFadLimits(
        final Map<Integer, ? extends Map<String, Integer>> activeFadLimits
    ) {
        return new ForbiddenIf(
            new AllOf(
                new ActionCodeIs("DPL"),
                new AnyOf(
                    activeFadLimits.entrySet().stream().map(yearAndLimits ->
                        new AllOf(
                            new InYear(yearAndLimits.getKey()),
                            new AnyOf(
                                yearAndLimits.getValue().entrySet().stream().map(classAndLimit ->
                                    new AllOf(
                                        new AgentHasTag("class " + classAndLimit.getKey()),
                                        new NotBelow(new NumberOfActiveFads(), classAndLimit.getValue())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    @SuppressWarnings("SameParameterValue")
    private static MonthDay addDays(
        final MonthDay monthDay,
        final long daysToAdd
    ) {
        final int REFERENCE_YEAR = 2023;
        final LocalDate baseDate = monthDay.atYear(REFERENCE_YEAR);
        final LocalDate newDate = baseDate.plusDays(daysToAdd);
        return MonthDay.of(newDate.getMonth(), newDate.getDayOfMonth());
    }

}
