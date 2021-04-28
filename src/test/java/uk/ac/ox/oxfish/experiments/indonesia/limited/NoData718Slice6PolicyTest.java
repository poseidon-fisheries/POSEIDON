package uk.ac.ox.oxfish.experiments.indonesia.limited;

import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoData718Slice6PolicyTest {

    static public LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> testPolicies = new LinkedHashMap();

    static {
        testPolicies.put(
                "BAU",
                shockYear -> scenario -> {
                }

        );
    }

  //  @Test
    public void seedStaysTheSame() throws IOException {

        final Path scenarioFile = Paths.get("inputs", "tests", "scenario0.yaml");

        BatchRunner runner =  NoData718Slice2PriceIncrease.setupRunner(scenarioFile,
                3, null, 0, null);
        StringBuffer tidy = new StringBuffer();
        runner.run(tidy);
        System.out.println(tidy);
        String oldTidy = tidy.toString();
        System.out.println(oldTidy);


        runner =  NoData718Slice2PriceIncrease.setupRunner(scenarioFile,
                3, null, 0, null);
        tidy = new StringBuffer();
        runner.run(tidy);
        System.out.println(tidy);

    }

  //  @Test
    public void sameInputSameOutput() throws IOException {


        LinkedList<String> otherColumnsToPrint = new LinkedList<>();
        otherColumnsToPrint.add("SPR Atrobucca brevis spr_agent3_small");
        otherColumnsToPrint.add("SPR Lutjanus malabaricus spr_agent2_small");
        otherColumnsToPrint.add("SPR Lethrinus laticaudis spr_agent1_small");


        NoData718Slice6PriceIncrease.runDirectoryPriceIncrease(
                Paths.get("inputs", "tests", "slice718","output"),
                Paths.get("inputs", "tests", "slice718","candidates.csv"),
                NoData718Slice6PriceIncrease.slice6PriceJump,
                otherColumnsToPrint
        );

    }

  //  @Test
    public void samePolicySameOutput() throws IOException {


        LinkedList<String> otherColumnsToPrint = new LinkedList<>();
        otherColumnsToPrint.add("SPR Atrobucca brevis spr_agent3_small");
        otherColumnsToPrint.add("SPR Lutjanus malabaricus spr_agent2_small");
        otherColumnsToPrint.add("SPR Lethrinus laticaudis spr_agent1_small");


        NoData718Slice6Policy.runPolicyDirectory(
                Paths.get("inputs", "tests", "slice718","candidates.csv").toFile(),
                Paths.get("inputs", "tests", "slice718","output_policy"),
                NoData718Utilities.policies, new LinkedList<>(), new LinkedList<Pair<Integer, AlgorithmFactory<? extends AdditionalStartable>>>(), null
        );

    }
}