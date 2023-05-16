/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.behavior.functionapproximation.dense.*;
import burlap.behavior.functionapproximation.dense.fourier.FourierBasis;
import burlap.behavior.functionapproximation.dense.fourier.FourierBasisLearningRateWrapper;
import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.experiencereplay.ExperienceMemory;
import burlap.behavior.singleagent.learning.experiencereplay.FixedSizeMemory;
import burlap.behavior.singleagent.learning.lspi.LSPI;
import burlap.behavior.singleagent.learning.lspi.SARSData;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentQLearning;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.options.EnvironmentOptionOutcome;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import com.beust.jcommander.internal.Nullable;
import com.opencsv.CSVWriter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import joptsimple.internal.Strings;
import org.ejml.simple.SimpleMatrix;
import org.yaml.snakeyaml.Yaml;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by carrknight on 12/19/16.
 */
public class BurlapShodan {


    public static final int STEPS_PER_LEARNING = 10;
    public static final int NUMBER_OF_EPISODES = 2000;

    private final static Steppable DEFAULT_STEPPABLE = new Steppable() {
        @Override
        public void step(final SimState simState) {

            final FishState state = (FishState) simState;
            //change oil price
            for (final Port port : state.getPorts())
                port.setGasPricePerLiter(state.getDayOfTheYear() / 1000d);
        }
    };

    //todo fourier basis require fourier

    public static void main(final String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {

        Paths.get("runs", "burlap").toFile().mkdirs();
        Paths.get("runs", "burlap", "data").toFile().mkdirs();
        Paths.get("runs", "burlap", "saves").toFile().mkdirs();
        Paths.get("runs", "burlap", "results").toFile().mkdirs();


        // polynomialRun(0, "myopic_lspi_simple_gas", 1, ShodanStateOil.GAS_PRICE);
        // polynomialRun(1, "non_myopic_lspi_simple_gas", 1, ShodanStateOil.GAS_PRICE);
        // polynomialRun(0, "myopic_lspi_gas_and_date", 1, ShodanStateOil.GAS_PRICE, ShodanStateOil.MONTHS_LEFT);
        // polynomialRun(1, "non_myopic_lspi_gas_and_date", 1, ShodanStateOil.GAS_PRICE, ShodanStateOil.MONTHS_LEFT);


        //  polynomialRun(0, "myopic_lspi_simple_day", 1, ShodanStateOil.DAY_OF_THE_YEAR);
        // polynomialRun(.99, "99_lspi_simple_day", 1, ShodanStateOil.DAY_OF_THE_YEAR);
        // polynomialRun(1, "non_myopic_lspi_simple_day", 1, ShodanStateOil.DAY_OF_THE_YEAR);
        // polynomialRun(0, "myopic_lspi_simple_day_and_date", 1, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.MONTHS_LEFT);
        //      polynomialRun(.99, "99_lspi_simple_day_and_date", 1, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.MONTHS_LEFT);
        //      polynomialRun(1, "non_myopic_lspi_simple_day_and_date", 1, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.MONTHS_LEFT);


        //full
/*
       polynomialRun(0, "myopic_lspi_full", 1, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                      ShodanStateOil.CUMULATIVE_EFFORT, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.GAS_PRICE,
                      ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);



        polynomialRun(.99, "99_lspi_full", 1, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                      ShodanStateOil.CUMULATIVE_EFFORT, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.GAS_PRICE,
                      ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);



        polynomialRun(1, "non_myopic_lspi_full", 1, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                      ShodanStateOil.CUMULATIVE_EFFORT, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.GAS_PRICE,
                      ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);
                      */


        //polynomialRun(0, "myopic_lspi_full_4", 4, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.GAS_PRICE, ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);
        //polynomialRun(.99, "99_lspi_full_4", 4, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.GAS_PRICE, ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);
        //polynomialRun(1, "non_myopic_lspi_full_4", 4, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.GAS_PRICE, ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);

        /*
         polynomialRun(0, "myopic_lspi_full_2", 2, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.GAS_PRICE, ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);
          polynomialRun(.99, "99_lspi_full_2", 2, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.GAS_PRICE, ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);
         polynomialRun(1, "non_myopic_lspi_full_2", 2, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT, ShodanStateOil.DAY_OF_THE_YEAR, ShodanStateOil.GAS_PRICE, ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);


          polynomialRun(0, "myopic_lspi_target_1", 1, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT,  ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);
           polynomialRun(0, "myopic_lspi_target_2", 2, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT,  ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);
           polynomialRun(0, "myopic_lspi_target_4", 4, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT,  ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);


          polynomialRun(.99, "99_lspi_target_1", 1, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT,  ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);
           polynomialRun(.99, "99_lspi_target_2", 2, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT,  ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);
           polynomialRun(.99, "99_lspi_target_4", 4, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, ShodanStateOil.CUMULATIVE_EFFORT,  ShodanStateOil.LANDINGS, ShodanStateOil.MONTHS_LEFT);
*/
/*
          sarsaRun(0, "myopic_sarsa_simple_gas", 1, .01, .3,ShodanStateOil.GAS_PRICE);
         sarsaRun(.99, "99_sarsa_simple_gas", 1, .01,.3, ShodanStateOil.GAS_PRICE);

        //   sarsaRun(0, "myopic_sarsa_simple_gas_and_date", 1, .0000001, .3, ShodanStateOil.GAS_PRICE,  ShodanStateOil.MONTHS_LEFT);
        // sarsaRun(.99, "99_sarsa_simple_gas_and_date", 1, .005, .3, ShodanStateOil.GAS_PRICE,  ShodanStateOil.MONTHS_LEFT);

           NormalizedVariableFeatures features = new NormalizedVariableFeatures().variableDomain(ShodanStateOil.GAS_PRICE,new VariableDomain(0,0.355)).variableDomain(ShodanStateOil.MONTHS_LEFT,new VariableDomain(0,243));
            sarsaRunNormalized(0, "myopic_sarsa_simple_gas_and_date_normalized", 1, .005, .3, features,ShodanStateOil.GAS_PRICE, ShodanStateOil.MONTHS_LEFT);
            sarsaRunNormalized(.99, "99_sarsa_simple_gas_and_date_normalized", 1, .005, .3, features,ShodanStateOil.GAS_PRICE, ShodanStateOil.MONTHS_LEFT);


        features = new NormalizedVariableFeatures().
                variableDomain(ShodanStateOil.CUMULATIVE_EFFORT,new VariableDomain(0,6000001.0)).
                variableDomain(ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,new VariableDomain(30,500)).
                variableDomain(ShodanStateOil.MONTHS_LEFT,new VariableDomain(0,243))
                .variableDomain(ShodanStateOil.LANDINGS,new VariableDomain(0,100000));

          sarsaRunNormalized(0, "myopic_sarsa_target_normalized", 1, .01, .3, features,ShodanStateOil.CUMULATIVE_EFFORT, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,ShodanStateOil.MONTHS_LEFT,ShodanStateOil.LANDINGS);
               sarsaRunNormalized(.99, "99_sarsa_simple_target_normalized", 1, .01, .3, features,ShodanStateOil.CUMULATIVE_EFFORT, ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,ShodanStateOil.MONTHS_LEFT,ShodanStateOil.LANDINGS);
*/

        //       sarsaRun(1, "non_myopic_sarsa_simple_gas", 1, .001, .3, ShodanStateOil.GAS_PRICE);
        //    sarsaRun(1, "non_myopic_sarsa_simple_gas_and_date", 1, .001, .3, ShodanStateOil.GAS_PRICE,  ShodanStateOil.MONTHS_LEFT);


        //  qRun(0, "myopic_q_noreplay_simple_gas", 1, .01,0,1,0, ShodanStateOil.GAS_PRICE);

        //  qRun(0, "myopic_q_1000replay_simple_gas", 1, .01, 1000, 1, 1, ShodanStateOil.GAS_PRICE);
        //  qRun(0, "myopic_q_1000replay_stale_simple_gas", 1, .01, 1000, 1, 5, ShodanStateOil.GAS_PRICE);
        qRun(0, "myopic_q_1000replay_highintercept_stale_simple_gas", 1, .01, 1000, 4500, 5, new PrototypeScenario(),
            Paths.get("runs", "burlap"), DEFAULT_STEPPABLE, ShodanStateOil.GAS_PRICE
        );

        //     episodesToCSV();


        System.out.println("Finished");


    }

    public static void qRun(
        final double discount, final String name, final int order, final double learningRate, final int replay,
        final double intercept, final int staleDuration, final PrototypeScenario scenario, final Path containerPath,
        final Steppable additionalSteppable,
        final Object... keys
    ) throws IOException, NoSuchFieldException, IllegalAccessException {

        final NumericVariableFeatures inputFeatures = new NumericVariableFeatures(keys);


        //write a YAML for the results
        final HashMap<String, Object> resultObject = new HashMap<>();
        resultObject.put("method", "qlearning");
        resultObject.put("replay", replay);
        resultObject.put("discount", discount);
        resultObject.put("learning_rate", learningRate);
        final String[] stringedKeys = new String[keys.length];
        for (int i = 0; i < stringedKeys.length; i++)
            stringedKeys[i] = keys[i].toString();
        resultObject.put("factors", stringedKeys);
        resultObject.put("name", name);
        resultObject.put("base", "polynomial");
        //stats.stackexchange.com/questions/5747/if-a-and-b-are-correlated-with-c-why-are-a-and-b-not-necessarily-correlated/22522#22522
        resultObject.put("order", order);
        resultObject.put("normalized", true);
        resultObject.put("staleDuration", staleDuration);
        resultObject.put("intercept", intercept);
        //lspiRun sarsa, return last fitness
        final double fitness = runQ(new PolynomialBasis(inputFeatures, order, 1), name, discount, learningRate, replay,
            staleDuration, containerPath,
            new ShodanEnvironment(scenario, additionalSteppable),
            resultObject
        );


        resultObject.put("fitness", fitness);
        resultObject.put("episodes", NUMBER_OF_EPISODES);

        //to file
        final File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        final Yaml yaml = new Yaml();
        yaml.dump(resultObject, new FileWriter(yamlFile));
    }

    public static double runQ(
        final DenseStateFeatures fb, final String directory, final double discount, final double learningRate,
        final int memory, final int staleDuration,
        final Path containerPath, final ShodanEnvironment environment,
        final HashMap<String, Object> metadata
    ) throws IOException, NoSuchFieldException, IllegalAccessException {

        containerPath.resolve(directory).toFile().mkdirs();

        System.out.println("running " + directory);


        final SADomain domain = new SADomain();
        domain.setActionTypes(
            new UniversalActionType(ShodanEnvironment.ACTION_OPEN),
            new UniversalActionType(ShodanEnvironment.ACTION_CLOSE)
        );


        final DenseLinearVFA parametricState;

        GradientDescentQLearning qLearning = (GradientDescentQLearning) readAgent(containerPath, directory);
        if (qLearning == null) {
            parametricState = new DenseLinearVFA(fb, 1);
            parametricState.evaluate(
                new ShodanStateOil(0, 0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0
                ),
                new SimpleAction(ShodanEnvironment.ACTION_OPEN)
            ); //this initalizes the parametrs

            qLearning = new GradientDescentQLearning(domain, discount,
                parametricState,
                learningRate
            );
            if (fb instanceof FourierBasis) {
                System.out.println("fourier learning rate!");
                final ConstantLR delegate = new ConstantLR(learningRate);
                qLearning.setLearningRate(new FourierBasisLearningRateWrapper(
                    delegate,
                    (FourierBasis) fb
                ));
            }

            if (memory > 0) {
                qLearning.setExperienceReplay(readExperienceMemory(discount, containerPath), memory);
                qLearning.useStaleTarget(staleDuration);
            }

            if (fb instanceof FourierBasis) {
                System.out.println("fourier learning rate!");
                qLearning.setLearningRate(new FourierBasisLearningRateWrapper(
                    new ConstantLR(learningRate),
                    (FourierBasis) fb
                ));
            }
        } else {
            final Field vfa = GradientDescentQLearning.class.getDeclaredField("vfa");
            vfa.setAccessible(true);
            parametricState = (DenseLinearVFA) vfa.get(qLearning);
        }
        environment.resetEnvironment();


        final EpsilonGreedy greedy = new EpsilonGreedy(qLearning, .2);
        qLearning.setLearningPolicy(greedy);
        final List<Episode> episodeList = new LinkedList<>();

        double lastEstimation = Double.NaN;
        //lspiRun learning for 100 episodes
        for (int i = 0; i <= NUMBER_OF_EPISODES; i++) {
            greedy.setEpsilon(
                .2 * (NUMBER_OF_EPISODES - i) / (NUMBER_OF_EPISODES));


            episodeList.add(qLearning.runLearningEpisode(environment));
            System.out.println(i + ": " + environment.totalReward() + "epsilon: " + (greedy.getEpsilon()));
            final String[] parameters = new String[parametricState.numParameters()];
            for (int p = 0; p < parameters.length; p++)
                parameters[p] = String.valueOf(parametricState.getParameter(p));
            System.out.println(i + ": " + Strings.join(parameters, ","));

            //reset environment for next learning episode
            environment.resetEnvironment();
            if (i % STEPS_PER_LEARNING == 0) {
                System.out.println("force regression");


                environment.resetEnvironment(0);
                //final
                final GreedyQPolicy policy = new GreedyQPolicy(qLearning);
                PolicyUtils.rollout(policy, environment).write(
                    containerPath.resolve(directory).resolve("q_" + i).toAbsolutePath().toString());
                lastEstimation = environment.totalReward();
                System.out.println("final_" + i + ": " + lastEstimation);
                Files.write(
                    containerPath.resolve(directory).resolve("q_" + i + ".test"),
                    String.valueOf(environment.totalReward()).getBytes()
                );

                //write progression
                Files.write(
                    containerPath.resolve(directory).resolve("progression.csv"),
                    (i + "," + lastEstimation + "\n").getBytes(),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE
                );


                Files.write(
                    containerPath.resolve(directory).resolve("q_" + i + ".csv"),
                    Strings.join(parameters, ",").getBytes()
                );
                environment.resetEnvironment();

                Episode.writeEpisodes(episodeList, containerPath.resolve("data").toAbsolutePath().toString(),
                    directory
                );

                //    saveAgent(containerPath,directory,qLearning);
                double bestFitness = lastEstimation;
                if (metadata.containsKey("fitness"))
                    bestFitness = Math.max(bestFitness, (Double) metadata.get("fitness"));
                metadata.put("fitness", bestFitness);
                metadata.put("episodes", i);
                final File yamlFile = containerPath.resolve("results").resolve(directory + ".yaml").toFile();
                final Yaml yaml = new Yaml();
                yaml.dump(metadata, new FileWriter(yamlFile));

            }
        }


        return lastEstimation;
    }

    public static LearningAgent readAgent(final Path containerPath, final String name) {
        Logger.getGlobal().info("Reading from File");
        final XStream xstream = new XStream(new StaxDriver());
        String xml = null;
        try {
            final byte[] saves = Files.readAllBytes(containerPath.resolve("saves").resolve(name + ".xml"));
            xml = new String(saves);
            Logger.getGlobal().info("Learner read ");

            return (LearningAgent) xstream.fromXML(xml);
        } catch (final NoSuchFileException e) {
            //      e.printStackTrace();
            Logger.getGlobal().info("no saved agent yet");
            return null;
        } catch (final IOException e) {
            e.printStackTrace();
            Logger.getGlobal().severe("Failed to read file " + name);
            return null;
        }
    }

    private static ExperienceMemory readExperienceMemory(final double discountFactor, final Path container) {

        final List<Episode> episodes = Episode.readEpisodes(container.resolve("data").toAbsolutePath().toString());
        final ExperienceMemory memory = new FixedSizeMemory(episodes.size() * 243);
        for (final Episode e : episodes)
            for (int t = 0; t < e.maxTimeStep(); t++)
                memory.addExperience(new EnvironmentOptionOutcome(
                    e.state(t),
                    e.action(t),
                    e.state(t + 1),
                    e.reward(t + 1),
                    t + 1 == e.maxTimeStep(),
                    discountFactor,
                    e
                ));
        return memory;

    }

    public static void sarsaRunNormalized(
        final double discount, final String name, final int order, final double learningRate, final double lambda,
        final NormalizedVariableFeatures inputFeatures, final PrototypeScenario scenario, final Path containerPath,
        final Steppable additionalSteppable,
        final String... featureNames
    ) throws IOException, NoSuchFieldException, IllegalAccessException {


        final ShodanEnvironment environment = new ShodanEnvironment(scenario, additionalSteppable);

        //write a YAML for the results
        final HashMap<String, Object> resultObject = new HashMap<>();
        resultObject.put("method", "sarsa");
        resultObject.put("lambda", lambda);
        resultObject.put("discount", discount);
        resultObject.put("learning_rate", learningRate);
        resultObject.put("factors", featureNames);
        resultObject.put("name", name);
        resultObject.put("base", "polynomial");
        resultObject.put("order", order);
        resultObject.put("normalized", true);
        //run sarsa, return last fitness
        final double fitness = runSarsa(new PolynomialBasis(inputFeatures, order, 1),
            name,
            discount,
            learningRate,
            lambda,
            containerPath,
            environment,
            null,
            resultObject
        );

        resultObject.put("episodes", NUMBER_OF_EPISODES);

        double bestFitness = fitness;
        if (resultObject.containsKey("fitness"))
            bestFitness = Math.max(bestFitness, (Double) resultObject.get("fitness"));
        resultObject.put("fitness", bestFitness);
        //to file
        final File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        final Yaml yaml = new Yaml();
        yaml.dump(resultObject, new FileWriter(yamlFile));
    }

    public static double runSarsa(
        final DenseStateFeatures fb, final String directory, final double discount, final double learningRate,
        final double lambda, final Path containerPath, Environment environment,
        final Pair<ShodanStateOil, Action> baseline,
        final HashMap<String, Object> metadata
    ) throws IOException, IllegalAccessException, NoSuchFieldException {


        //set up domain
        containerPath.resolve(directory).toFile().mkdirs();

        System.out.println("running " + directory);


        final SADomain domain = new SADomain();
        domain.setActionTypes(
            new UniversalActionType(ShodanEnvironment.ACTION_OPEN),
            new UniversalActionType(ShodanEnvironment.ACTION_CLOSE)
        );

        int i = 0; //episode counter


        //try to read saved agent. if it doesn't exist, create it
        final DenseLinearVFA parametricState;
        GradientDescentSarsaLam sarsaLam = (GradientDescentSarsaLam) readAgent(containerPath, directory);
        if (sarsaLam == null) {
            parametricState = new DenseLinearVFA(fb, 1);
            sarsaLam = new GradientDescentSarsaLam(domain, discount,
                parametricState,
                learningRate,
                lambda
            );
            if (fb instanceof FourierBasis) {
                System.out.println("fourier learning rate!");
                sarsaLam.setLearningRate(new FourierBasisLearningRateWrapper(
                    new ConstantLR(learningRate),
                    (FourierBasis) fb
                ));
            }
        } else {
            //read from file, should be safe
            final Field vfa = GradientDescentSarsaLam.class.getDeclaredField("vfa");
            vfa.setAccessible(true);
            parametricState = (DenseLinearVFA) vfa.get(sarsaLam);


            final List<String> previousLines = Files.readAllLines(containerPath.resolve(directory)
                .resolve("progression.csv"));
            if (previousLines.size() > 0) {
                i = Integer.parseInt(previousLines.get(previousLines.size() - 1).split(",")[0]);
                i++;
                System.out.println("start from episode " + i);

            }
        }

        //add epsilon greedy exploration
        final EpsilonGreedy greedy = new EpsilonGreedy(sarsaLam, .2);
        sarsaLam.setLearningPolicy(greedy);


        //add baseline if you need to
        if (baseline != null)
            environment = new RelativeRewardEnvironmentDecorator(
                sarsaLam,
                environment,
                baseline.getFirst(),
                baseline.getSecond()
            );

        environment.resetEnvironment();

        final List<Episode> episodeList = new LinkedList<>();

        double lastEstimation = Double.NaN;
        //lspiRun learning for 100 episodes
        for (; i <= NUMBER_OF_EPISODES; i++) {

            greedy.setEpsilon(
                .2 * (NUMBER_OF_EPISODES - i) / (NUMBER_OF_EPISODES));


            final double runReward = ((ShodanEnvironment) ((RelativeRewardEnvironmentDecorator) environment).getDelegate()).totalReward();
            episodeList.add(sarsaLam.runLearningEpisode(environment));
            System.out.println(i + ": " + runReward + "epsilon: " + (greedy.getEpsilon()));
            final String[] parameters = new String[parametricState.numParameters()];
            for (int p = 0; p < parameters.length; p++)
                parameters[p] = String.valueOf(parametricState.getParameter(p));
            System.out.println(i + ": " + Strings.join(parameters, ","));

            //reset environment for next learning episode
            environment.resetEnvironment();
            if (i % STEPS_PER_LEARNING == 0) {
                System.out.println("force regression");


                ((ShodanEnvironment) ((RelativeRewardEnvironmentDecorator) environment).getDelegate()).resetEnvironment(
                    0);
                //final
                final GreedyQPolicy policy = new GreedyQPolicy(sarsaLam);
                PolicyUtils.rollout(policy, environment).write(
                    containerPath.resolve(directory).resolve("lspi_" + i).toAbsolutePath().toString());
                lastEstimation = ((ShodanEnvironment) ((RelativeRewardEnvironmentDecorator) environment).getDelegate()).totalReward();
                System.out.println("final_" + i + ": " + lastEstimation);
                Files.write(
                    containerPath.resolve(directory).resolve("sarsa_" + i + ".test"),
                    String.valueOf(lastEstimation).getBytes()
                );

                Files.write(
                    containerPath.resolve(directory).resolve("progression.csv"),
                    (i + "," + lastEstimation + "\n").getBytes(),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE
                );


                Files.write(
                    containerPath.resolve(directory).resolve("sarsa_" + i + ".csv"),
                    Strings.join(parameters, ",").getBytes()
                );
                environment.resetEnvironment();

                Episode.writeEpisodes(episodeList, containerPath.resolve("data").toAbsolutePath().toString(),
                    directory
                );

                saveAgent(containerPath, directory, sarsaLam);
                //to file
                double bestFitness = lastEstimation;
                if (metadata.containsKey("fitness"))
                    bestFitness = Math.max(bestFitness, (Double) metadata.get("fitness"));
                metadata.put("fitness", bestFitness);
                metadata.put("episodes", i);
                final File yamlFile = containerPath.resolve("results").resolve(directory + ".yaml").toFile();
                final Yaml yaml = new Yaml();
                yaml.dump(metadata, new FileWriter(yamlFile));

                saveAgentHere(containerPath.resolve(directory).resolve("agent_" + i + ".xml"), sarsaLam);
            }
        }


        return lastEstimation;
    }

    public static void saveAgent(final Path containerPath, final String name, final LearningAgent agent) {
        final XStream xstream = new XStream(new StaxDriver());
        Logger.getGlobal().info("Writing to file!");
        final String xml = xstream.toXML(agent);

        try {
            Files.write(containerPath.resolve("saves").resolve(name + ".xml"), xml.getBytes());
            Logger.getGlobal().info("Learner saved ");
        } catch (final IOException e) {
            e.printStackTrace();
            Logger.getGlobal().severe(e.getMessage());
        }
    }

    public static void saveAgentHere(final Path filePath, final LearningAgent agent) {
        final XStream xstream = new XStream(new StaxDriver());
        Logger.getGlobal().info("Writing to file!");
        final String xml = xstream.toXML(agent);

        try {
            Files.write(filePath, xml.getBytes());
            Logger.getGlobal().info("Learner saved ");
        } catch (final IOException e) {
            e.printStackTrace();
            Logger.getGlobal().severe(e.getMessage());
        }
    }

    public static void sarsaRunFourier(
        final double discount, final String name, final int order, final double learningRate, final double lambda,
        final NormalizedVariableFeatures inputFeatures, final PrototypeScenario scenario, final Path containerPath,
        final Steppable additionalSteppable, @Nullable final Pair<ShodanStateOil, Action> baseline,
        final String... featureNames
    ) throws IOException, NoSuchFieldException, IllegalAccessException {


        final ShodanEnvironment environment = new ShodanEnvironment(scenario, additionalSteppable);

        //write a YAML for the results
        final HashMap<String, Object> resultObject = new HashMap<>();
        resultObject.put("method", "sarsa");
        resultObject.put("lambda", lambda);
        resultObject.put("discount", discount);
        resultObject.put("learning_rate", learningRate);
        resultObject.put("factors", featureNames);
        resultObject.put("name", name);
        resultObject.put("base", "fourier");
        resultObject.put("order", order);
        resultObject.put("normalized", true);
        //run sarsa, return last fitness
        final double fitness = runSarsa(new FourierBasis(inputFeatures, order), name, discount, learningRate, lambda,
            containerPath, environment, baseline, resultObject
        );

        double bestFitness = fitness;
        if (resultObject.containsKey("fitness"))
            bestFitness = Math.max(bestFitness, (Double) resultObject.get("fitness"));
        resultObject.put("fitness", bestFitness);
        resultObject.put("episodes", NUMBER_OF_EPISODES);

        //to file
        final File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        final Yaml yaml = new Yaml();
        yaml.dump(resultObject, new FileWriter(yamlFile));
    }

    public static void qRunFourier(
        final double discount, final String name,
        final int order, final double learningRate,
        final int replay,
        final int staleDuration, final PrototypeScenario scenario, final Path containerPath,
        final NormalizedVariableFeatures inputFeatures,
        final Steppable additionalSteppable,
        final String... keys
    ) throws IOException, NoSuchFieldException, IllegalAccessException {


        //write a YAML for the results
        final HashMap<String, Object> resultObject = new HashMap<>();
        resultObject.put("method", "qlearning");
        resultObject.put("replay", replay);
        resultObject.put("discount", discount);
        resultObject.put("learning_rate", learningRate);
        resultObject.put("factors", keys);
        resultObject.put("name", name);
        resultObject.put("base", "fourier");
        resultObject.put("order", order);
        resultObject.put("normalized", true);
        resultObject.put("staleDuration", staleDuration);
        //lspiRun sarsa, return last fitness
        final double fitness = runQ(new FourierBasis(inputFeatures, order), name, discount, learningRate, replay,
            staleDuration, containerPath,
            new ShodanEnvironment(scenario, additionalSteppable),
            resultObject
        );

        resultObject.put("episodes", NUMBER_OF_EPISODES);
        resultObject.put("fitness", fitness);

        //to file
        final File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        final Yaml yaml = new Yaml();
        yaml.dump(resultObject, new FileWriter(yamlFile));
    }

    public static void lspiFourierRun(
        final double discount, final String name, final int order, final NormalizedVariableFeatures inputFeatures,
        final PrototypeScenario scenario,
        final Path containerPath, final Steppable additionalSteppable,
        final String... featureNames
    ) throws IOException, NoSuchFieldException, IllegalAccessException {

        final int initialNumberOfEpisodes = numberOfEpisodesInMemory();

        final double fitness = lspiRun(new FourierBasis(inputFeatures, order), name, discount,
            containerPath, new ShodanEnvironment(scenario, additionalSteppable)
        );


        //write a YAML for the results
        final HashMap<String, Object> resultObject = new HashMap<>();
        resultObject.put("method", "lspi");
        resultObject.put("discount", discount);

        final String[] names = new String[featureNames.length];
        System.arraycopy(featureNames, 0, names, 0, names.length);

        resultObject.put("factors", names);
        resultObject.put("episodes", NUMBER_OF_EPISODES);
        resultObject.put("name", name);
        resultObject.put("fitness", fitness);
        resultObject.put("base", "fourier");
        resultObject.put("order", order);
        resultObject.put("normalized", true);
        resultObject.put("initial_data_set", initialNumberOfEpisodes);
        //to file
        final File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        final Yaml yaml = new Yaml();
        yaml.dump(resultObject, new FileWriter(yamlFile));
    }

    public static int numberOfEpisodesInMemory() {
        return Episode.readEpisodes(Paths.get("runs", "burlap").resolve("data").toAbsolutePath().toString()).size();
    }

    public static double lspiRun(
        final DenseStateFeatures fb, final String directory, final double discount,
        final Path path, final ShodanEnvironment environment
    ) throws IOException, NoSuchFieldException, IllegalAccessException {

        path.resolve(directory).toFile().mkdirs();

        System.out.println("running " + directory);


        final SADomain domain = new SADomain();
        domain.setActionTypes(
            new UniversalActionType(ShodanEnvironment.ACTION_OPEN),
            new UniversalActionType(ShodanEnvironment.ACTION_CLOSE)
        );
        //create LSPI with discount factor, keep running it until the policy iteration converges
        //densecrossproductfeatures is a way to approximate the Q-Value rather than the Value (like Fourier basis does)
        //3 is the number of actions there are
        LSPI lspi = (LSPI) readAgent(path, directory);
        if (lspi == null)
            lspi = new LSPI(domain, discount, new DenseCrossProductFeatures(fb, 2));

        //are some episodes already available?
        final SARSData data = compileEpisodes(path);
        if (data.size() > 0) {
            lspi.setDataset(data);
            lspi.runPolicyIteration(30, .001);

        }

        double fitness = Double.NaN;
        environment.resetEnvironment();

        lspi.setMaxNumPlanningIterations(5);
        lspi.setMinNewStepsForLearningPI(200000000);
        final List<Episode> episodeList = new LinkedList<>();
        //lspiRun learning for 100 episodes
        for (int i = 0; i <= NUMBER_OF_EPISODES; i++) {
            ((EpsilonGreedy) lspi.getLearningPolicy()).setEpsilon(
                .4 * (STEPS_PER_LEARNING - i % STEPS_PER_LEARNING - 1) / (STEPS_PER_LEARNING - 1));
            episodeList.add(lspi.runLearningEpisode(environment, 20000));


            System.out.println(i + ": " + environment.totalReward() + "epsilon: " + ((EpsilonGreedy) lspi.getLearningPolicy()).getEpsilon());

            //reset environment for next learning episode
            environment.resetEnvironment();
            if (i % STEPS_PER_LEARNING == 0 && i != 0) {
                System.out.println("force regression");
                lspi.runPolicyIteration(30, .001);
                {
                    final Field f = lspi.getClass().getDeclaredField("vfa"); //NoSuchFieldException
                    f.setAccessible(true);
                    final DenseStateActionLinearVFA parametricState = (DenseStateActionLinearVFA) f.get(lspi); //IllegalAccessException
                    final String[] parameters = new String[parametricState.numParameters()];
                    for (int p = 0; p < parameters.length; p++)
                        parameters[p] = String.valueOf(parametricState.getParameter(p));
                    System.out.println(i + ": " + Strings.join(parameters, ","));
                }

                environment.resetEnvironment(0);
                //final
                final GreedyQPolicy policy = new GreedyQPolicy(lspi);
                PolicyUtils.rollout(policy, environment).write(
                    path.resolve(directory).resolve("lspi_" + i).toAbsolutePath().toString());
                fitness = environment.totalReward();
                System.out.println("final_" + i + ": " + fitness);
                Files.write(
                    path.resolve(directory).resolve("lspi_" + i + ".test"),
                    String.valueOf(environment.totalReward()).getBytes()
                );
                //try also doing the boltzmann version, for a comparison
                /*
                environment.resetEnvironment(0);
                BoltzmannQPolicy boltzmannQPolicy = new BoltzmannQPolicy(lspi,1d);
                PolicyUtils.rollout(boltzmannQPolicy, environment).write(
                        path.resolve(directory).resolve("lspi_boltzmann_"+i).toAbsolutePath().toString());
                System.out.println("final_boltzmann_"+i + ": " + fitness );
*/

                final Field f = lspi.getClass().getDeclaredField("lastWeights"); //NoSuchFieldException
                f.setAccessible(true);
                final SimpleMatrix iWantThis = (SimpleMatrix) f.get(lspi); //IllegalAccessException
                iWantThis.saveToFileCSV(path.resolve(directory)
                    .resolve("lspi_" + i + ".csv")
                    .toAbsolutePath()
                    .toString());
                environment.resetEnvironment();

                Episode.writeEpisodes(episodeList, path.resolve("data").toAbsolutePath().toString(),
                    directory
                );
                //      saveAgent(path,directory,lspi);

            }
        }


        return fitness;
    }

    public static SARSData compileEpisodes(final Path containerPath) throws IOException {


        final List<Episode> episodes = Episode.readEpisodes(containerPath.resolve("data").toAbsolutePath().toString());
        final SARSData data = new SARSData();
        for (final Episode e : episodes)
            for (int t = 0; t < e.maxTimeStep(); t++)
                data.add(e.state(t), e.action(t), e.reward(t + 1), e.state(t + 1));
        System.out.println("datasize " + data.size());
        return data;
    }

    public static void lspiPolynomialRunNormalized(
        final double discount, final String name, final int order, final PrototypeScenario scenario,
        final Path containerPath, final Steppable additionalSteppable, final NormalizedVariableFeatures inputFeatures,
        final String... featureNames
    ) throws IOException, NoSuchFieldException, IllegalAccessException {

        final int initialNumberOfEpisodes = numberOfEpisodesInMemory();

        final double fitness = lspiRun(new PolynomialBasis(inputFeatures, order, 1), name, discount,
            containerPath, new ShodanEnvironment(scenario, additionalSteppable)
        );


        //write a YAML for the results
        final HashMap<String, Object> resultObject = new HashMap<>();
        resultObject.put("method", "lspi");
        resultObject.put("discount", discount);


        resultObject.put("factors", featureNames);
        resultObject.put("episodes", NUMBER_OF_EPISODES);
        resultObject.put("name", name);
        resultObject.put("fitness", fitness);
        resultObject.put("base", "polynomial");
        resultObject.put("order", order);
        resultObject.put("normalized", true);
        resultObject.put("initial_data_set", initialNumberOfEpisodes);
        //to file
        final File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        final Yaml yaml = new Yaml();
        yaml.dump(resultObject, new FileWriter(yamlFile));
    }

    public static void polynomialRun(
        final double discount, final String name, final int order, final PrototypeScenario scenario,
        final Path containerPath, final Steppable additionalSteppable,
        final Object... keys
    ) throws IOException, NoSuchFieldException, IllegalAccessException {

        final NumericVariableFeatures inputFeatures = new NumericVariableFeatures(keys);
        final int initialNumberOfEpisodes = numberOfEpisodesInMemory();

        final double fitness = lspiRun(new PolynomialBasis(inputFeatures, order, 1), name, discount,
            containerPath, new ShodanEnvironment(scenario, additionalSteppable)
        );


        //write a YAML for the results
        final HashMap<String, Object> resultObject = new HashMap<>();
        resultObject.put("method", "lspi");
        resultObject.put("discount", discount);

        final String[] names = new String[keys.length];
        for (int i = 0; i < names.length; i++)
            names[i] = keys[i].toString();

        resultObject.put("factors", names);
        resultObject.put("episodes", NUMBER_OF_EPISODES);
        resultObject.put("name", name);
        resultObject.put("fitness", fitness);
        resultObject.put("base", "polynomial");
        resultObject.put("order", order);
        resultObject.put("normalized", false);
        resultObject.put("initial_data_set", initialNumberOfEpisodes);
        //to file
        final File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        final Yaml yaml = new Yaml();
        yaml.dump(resultObject, new FileWriter(yamlFile));
    }

    public static void sarsaRun(
        final double discount, final String name, final int order, final double learningRate, final double lambda,
        final PrototypeScenario scenario, final Path containerPath, final Steppable additionalSteppable,
        final Object... keys
    ) throws IOException, NoSuchFieldException, IllegalAccessException {

        final NumericVariableFeatures inputFeatures = new NumericVariableFeatures(keys);

        //write a YAML for the results
        final HashMap<String, Object> resultObject = new HashMap<>();
        resultObject.put("method", "sarsa");
        resultObject.put("lambda", lambda);
        resultObject.put("discount", discount);
        resultObject.put("learning_rate", learningRate);
        final String[] stringedKeys = new String[keys.length];
        for (int i = 0; i < stringedKeys.length; i++)
            stringedKeys[i] = keys[i].toString();
        resultObject.put("factors", stringedKeys);
        resultObject.put("name", name);
        resultObject.put("base", "polynomial");
        resultObject.put("order", order);
        resultObject.put("normalized", false);

        //lspiRun sarsa, return last fitness
        final double fitness = runSarsa(new PolynomialBasis(inputFeatures, order, 1),
            name,
            discount,
            learningRate,
            lambda,
            containerPath,
            new ShodanEnvironment(scenario, additionalSteppable),
            null,
            resultObject
        );


        //to file
        resultObject.put("fitness", fitness);
        resultObject.put("episodes", NUMBER_OF_EPISODES);
        final File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        final Yaml yaml = new Yaml();
        yaml.dump(resultObject, new FileWriter(yamlFile));

    }

    public static void episodesToCSV(final Path containerPath) throws IOException {

        //read all episodes
        final List<Episode> episodes = Episode.readEpisodes(containerPath.resolve("data").toAbsolutePath().toString());
        final List<String[]> csv = new LinkedList<>();
        //use this object to turn the state into a vector of numbers
        final NumericVariableFeatures features = new NumericVariableFeatures();
        final State state = episodes.get(0).state(0);
        final int featuresLength = features.features(state).length;
        //write the header: old states, reward, action, new states
        final String[] header = new String[featuresLength * 2 + 2];
        for (int i = 0; i < featuresLength; i++)
            header[i] = state.variableKeys().get(i).toString();
        header[featuresLength] = "reward";
        header[featuresLength + 1] = "action";
        for (int i = 0; i < featuresLength; i++)
            header[i + featuresLength + 2] = "new_" + state.variableKeys().get(i).toString();
        csv.add(header);
        //for each episode and for each time step
        for (final Episode e : episodes)
            for (int t = 0; t < e.maxTimeStep(); t++) {

                //write the csv
                final String[] line = new String[featuresLength * 2 + 2];
                final double[] preState = features.features(e.state(t));
                final double[] postState = features.features(e.state(t + 1));
                for (int i = 0; i < featuresLength; i++)
                    line[i] = String.valueOf(preState[i]);
                line[featuresLength] = String.valueOf(e.reward(t + 1));
                line[featuresLength + 1] = String.valueOf(e.action(t));
                for (int i = 0; i < featuresLength; i++)
                    line[i + featuresLength + 2] = String.valueOf(postState[i]);
                csv.add(line);
            }
        //dump
        final CSVWriter writer = new CSVWriter(new FileWriter(containerPath.resolve("data.csv").toFile()));
        writer.writeAll(csv);
    }


}
