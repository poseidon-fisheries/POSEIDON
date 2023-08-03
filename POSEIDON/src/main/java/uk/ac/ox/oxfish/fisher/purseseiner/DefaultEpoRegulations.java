package uk.ac.ox.oxfish.fisher.purseseiner;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.regulation.ForbiddenAreasFromShapeFiles;
import uk.ac.ox.oxfish.regulation.ForbiddenIf;
import uk.ac.ox.oxfish.regulation.NamedRegulations;
import uk.ac.ox.oxfish.regulation.conditions.*;
import uk.ac.ox.oxfish.regulation.quantities.NumberOfActiveFads;
import uk.ac.ox.oxfish.regulation.quantities.SumOf;
import uk.ac.ox.oxfish.regulation.quantities.YearlyActionCount;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import static java.time.Month.*;

public class DefaultEpoRegulations {

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
                "Active-FAD limits", new ForbiddenIf(
                    new AllOf(
                        new ActionCodeIs("DPL"),
                        new AnyOf(
                            new AllOf(
                                new AgentHasTag("class 6A"),
                                new NotBelow(new NumberOfActiveFads(), 300)
                            ),
                            new AllOf(
                                new AgentHasTag("class 6B"),
                                new NotBelow(new NumberOfActiveFads(), 450)
                            )
                        )
                    )
                ),
                // Yearly set limits, set to 99999 as a placeholder
                "Object-set limits", new ForbiddenIf(
                    new AllOf(
                        new AnyOf(
                            new ActionCodeIs("FAD"),
                            new ActionCodeIs("OFS")
                        ),
                        new NotBelow(
                            new SumOf(
                                new YearlyActionCount("FAD"),
                                new YearlyActionCount("OFS")
                            ),
                            999999
                        )
                    )
                ),
                // Forbid deployments 15 days before closure
                "Closure A", new ForbiddenIf(
                    new AnyOf(
                        new AllOf(
                            // Forbid deployments 15 days before closure
                            new AgentHasTag("closure A"),
                            new ActionCodeIs("DPL"),
                            new BetweenYearlyDates(
                                JULY, 14,
                                OCTOBER, 28
                            )
                        ),
                        new AllOf(
                            new AgentHasTag("closure A"),
                            new BetweenYearlyDates(
                                JULY, 29,
                                OCTOBER, 8
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
                                OCTOBER, 25,
                                NOVEMBER, 8
                            )
                        ),
                        new AllOf(
                            new AgentHasTag("closure B"),
                            new BetweenYearlyDates(
                                NOVEMBER, 9,
                                JANUARY, 19
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
                )
            )
        );
    }
}
