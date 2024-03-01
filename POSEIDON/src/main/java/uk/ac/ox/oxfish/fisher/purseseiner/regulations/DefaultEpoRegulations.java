package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.ForbiddenAreasFromShapeFiles;
import uk.ac.ox.poseidon.regulations.core.ForbiddenIfFactory;
import uk.ac.ox.poseidon.regulations.core.NamedRegulationsFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.*;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;

import static java.time.Month.*;

public class DefaultEpoRegulations {

    public static final Map<Integer, Map<String, Integer>> ACTIVE_FAD_LIMITS = ImmutableMap.of(
        2021, ImmutableMap.of("6.b", 300, "6.a", 450),
        2022, ImmutableMap.of("6.b", 270, "6.a", 400),
        2023, ImmutableMap.of("6.b", 255, "6.a", 340),
        2024, ImmutableMap.of("6.b", 210, "6.a", 340)
    );
    public static final MonthDay EL_CORRALITO_BEGINNING = MonthDay.of(OCTOBER, 9);
    public static final MonthDay EL_CORRALITO_END = MonthDay.of(NOVEMBER, 8);
    public static final InRectangularAreaFactory EL_CORRALITO_AREA = new InRectangularAreaFactory(
        4.0, -110.0, -3.0, -96.0
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

    public static ComponentFactory<Regulations> make(final InputPath inputFolder) {
        final InputPath regions = inputFolder.path("regions");
        final List<Integer> yearsActive = ImmutableList.of(2021, 2022, 2023);
        final TemporalClosure closureA = new TemporalClosure(
            yearsActive,
            "closure A",
            CLOSURE_A_START,
            CLOSURE_A_END,
            15
        );
        final TemporalClosure closureB = new TemporalClosure(
            yearsActive,
            "closure B",
            CLOSURE_B_START,
            CLOSURE_B_END,
            15
        );
        return new NamedRegulationsFactory(
            ImmutableMap.of(
                "DEL licence", new ForbiddenIfFactory(
                    new AllOfFactory(
                        new ActionCodeIsFactory("DEL"),
                        new NotFactory(new AgentHasTagFactory("has_del_license"))
                    )
                ),
                "Active-FAD limits", new ActiveFadLimits(ACTIVE_FAD_LIMITS),
                "Closure A", closureA,
                "Closure B", closureB,
                "El Corralito", new ForbiddenIfFactory(
                    new AllOfFactory(
                        new BetweenYearlyDatesFactory(
                            EL_CORRALITO_BEGINNING,
                            EL_CORRALITO_END
                        ),
                        EL_CORRALITO_AREA
                    )
                ),
                "EEZs", new ForbiddenAreasFromShapeFiles(
                    regions,
                    regions.path("region_tags.csv")
                ),
                "Extended 2022 closure", new ForbiddenIfFactory(
                    new AllOfFactory(
                        new InYearFactory(2022),
                        new AgentHasTagFactory("extended_2022_closure"),
                        new AnyOfFactory(
                            new TemporalClosureExtensionBeforeFactory(closureA, 8),
                            new TemporalClosureExtensionAfterFactory(closureB, 8)
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
