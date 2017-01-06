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
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import com.esotericsoftware.minlog.Log;
import com.opencsv.CSVWriter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import joptsimple.internal.Strings;
import org.ejml.simple.SimpleMatrix;
import org.yaml.snakeyaml.Yaml;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by carrknight on 12/19/16.
 */
public class BurlapShodan {


    public static final int STEPS_PER_LEARNING = 10;
    public static final int NUMBER_OF_EPISODES = 500;

    private final static Steppable DEFAULT_STEPPABLE =  new Steppable() {
        @Override
        public void step(SimState simState) {

            FishState state = (FishState) simState;
            //change oil price
            for (Port port : state.getPorts())
                port.setGasPricePerLiter(state.getDayOfTheYear() / 1000d);
        }
    };

    //todo fourier basis require fourier

    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {

        Paths.get("runs","burlap").toFile().mkdirs();
        Paths.get("runs","burlap","data").toFile().mkdirs();
        Paths.get("runs","burlap","saves").toFile().mkdirs();
        Paths.get("runs","burlap","results").toFile().mkdirs();



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
             Paths.get("runs", "burlap"), DEFAULT_STEPPABLE, ShodanStateOil.GAS_PRICE);

        //     episodesToCSV();


        System.out.println("Finished");


    }

    public static void sarsaRunNormalized(
            final double discount, final String name, final int order, final double learningRate, final double lambda,
            NormalizedVariableFeatures inputFeatures, final PrototypeScenario scenario, final Path containerPath,
            final Steppable additionalSteppable,
            String... featureNames) throws IOException, NoSuchFieldException, IllegalAccessException {


        ShodanEnvironment environment = new ShodanEnvironment(scenario, additionalSteppable);

        //write a YAML for the results
        HashMap<String,Object> resultObject = new HashMap<>();
        resultObject.put("method","sarsa");
        resultObject.put("lambda",lambda);
        resultObject.put("discount",discount);
        resultObject.put("learning_rate",learningRate);
        resultObject.put("factors",featureNames);
        resultObject.put("name",name);
        resultObject.put("base","polynomial");
        resultObject.put("order",order);
        resultObject.put("normalized",true);
        //run sarsa, return last fitness
        double fitness = runSarsa(new PolynomialBasis(inputFeatures, order, 1), name, discount, learningRate, lambda,
                                  containerPath, environment,resultObject );

        resultObject.put("episodes",NUMBER_OF_EPISODES);

        double bestFitness = fitness;
        if(resultObject.containsKey("fitness"))
            bestFitness = Math.max(bestFitness, (Double) resultObject.get("fitness"));
        resultObject.put("fitness",bestFitness);
        //to file
        File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        Yaml yaml = new Yaml();
        yaml.dump(resultObject,new FileWriter(yamlFile));
    }

    public static void sarsaRunFourier(
            final double discount, final String name, final int order, final double learningRate, final double lambda,
            NormalizedVariableFeatures inputFeatures, final PrototypeScenario scenario, final Path containerPath,
            final Steppable additionalSteppable,
            String... featureNames) throws IOException, NoSuchFieldException, IllegalAccessException {


        ShodanEnvironment environment = new ShodanEnvironment(scenario, additionalSteppable);

        //write a YAML for the results
        HashMap<String,Object> resultObject = new HashMap<>();
        resultObject.put("method","sarsa");
        resultObject.put("lambda",lambda);
        resultObject.put("discount",discount);
        resultObject.put("learning_rate",learningRate);
        resultObject.put("factors",featureNames);
        resultObject.put("name",name);
        resultObject.put("base","fourier");
        resultObject.put("order",order);
        resultObject.put("normalized",true);
        //run sarsa, return last fitness
        double fitness = runSarsa(new FourierBasis(inputFeatures, order), name, discount, learningRate, lambda,
                                  containerPath, environment,resultObject );

        double bestFitness = fitness;
        if(resultObject.containsKey("fitness"))
            bestFitness = Math.max(bestFitness, (Double) resultObject.get("fitness"));
        resultObject.put("fitness",bestFitness);
        resultObject.put("episodes",NUMBER_OF_EPISODES);

        //to file
        File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        Yaml yaml = new Yaml();
        yaml.dump(resultObject,new FileWriter(yamlFile));
    }

    public static void qRunFourier(
            final double discount, final String name,
            final int order, final double learningRate,
            final int replay,
            final int staleDuration, final PrototypeScenario scenario, final Path containerPath,
            NormalizedVariableFeatures inputFeatures,
            final Steppable additionalSteppable,
            String... keys) throws IOException, NoSuchFieldException, IllegalAccessException {



        //write a YAML for the results
        HashMap<String,Object> resultObject = new HashMap<>();
        resultObject.put("method","qlearning");
        resultObject.put("replay",replay);
        resultObject.put("discount",discount);
        resultObject.put("learning_rate",learningRate);
        resultObject.put("factors",keys);
        resultObject.put("name",name);
        resultObject.put("base","fourier");
        resultObject.put("order",order);
        resultObject.put("normalized",true);
        resultObject.put("staleDuration",staleDuration);
        //lspiRun sarsa, return last fitness
        double fitness = runQ(new FourierBasis(inputFeatures, order), name, discount, learningRate, replay,
                              staleDuration, containerPath,
                              new ShodanEnvironment(scenario, additionalSteppable),
                              resultObject);

        resultObject.put("episodes",NUMBER_OF_EPISODES);
        resultObject.put("fitness",fitness);

        //to file
        File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        Yaml yaml = new Yaml();
        yaml.dump(resultObject,new FileWriter(yamlFile));
    }

    public static void qRun(
            final double discount, final String name, final int order, final double learningRate, final int replay,
            final double intercept, final int staleDuration, final PrototypeScenario scenario, final Path containerPath,
            final Steppable additionalSteppable,
            Object... keys) throws IOException, NoSuchFieldException, IllegalAccessException {

        NumericVariableFeatures inputFeatures = new NumericVariableFeatures(keys);


        //write a YAML for the results
        HashMap<String,Object> resultObject = new HashMap<>();
        resultObject.put("method","qlearning");
        resultObject.put("replay",replay);
        resultObject.put("discount",discount);
        resultObject.put("learning_rate",learningRate);
        String[] stringedKeys = new String[keys.length];
        for(int i=0; i<stringedKeys.length; i++)
            stringedKeys[i] = keys[i].toString();
        resultObject.put("factors",stringedKeys);
        resultObject.put("name",name);
        resultObject.put("base","polynomial");http://stats.stackexchange.com/questions/5747/if-a-and-b-are-correlated-with-c-why-are-a-and-b-not-necessarily-correlated/22522#22522
        resultObject.put("order",order);
        resultObject.put("normalized",true);
        resultObject.put("staleDuration",staleDuration);
        resultObject.put("intercept",intercept);
        //lspiRun sarsa, return last fitness
        double fitness = runQ(new PolynomialBasis(inputFeatures, order, 1), name, discount, learningRate, replay,
                              staleDuration, containerPath,
                              new ShodanEnvironment(scenario, additionalSteppable),
                              resultObject);


        resultObject.put("fitness",fitness);
        resultObject.put("episodes",NUMBER_OF_EPISODES);

        //to file
        File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        Yaml yaml = new Yaml();
        yaml.dump(resultObject,new FileWriter(yamlFile));
    }

    public static void lspiFourierRun(
            final double discount, final String name, final int order, NormalizedVariableFeatures inputFeatures,
            final PrototypeScenario scenario,
            final Path containerPath, final Steppable additionalSteppable,
            String... featureNames) throws IOException, NoSuchFieldException, IllegalAccessException {

        int initialNumberOfEpisodes = numberOfEpisodesInMemory();

        double fitness = lspiRun(new FourierBasis(inputFeatures, order), name, discount,
                                 containerPath, new ShodanEnvironment(scenario, additionalSteppable));


        //write a YAML for the results
        HashMap<String,Object> resultObject = new HashMap<>();
        resultObject.put("method","lspi");
        resultObject.put("discount",discount);

        String[] names = new String[featureNames.length];
        for(int i=0; i<names.length; i++)
            names[i] = featureNames[i];

        resultObject.put("factors",names);
        resultObject.put("episodes",NUMBER_OF_EPISODES);
        resultObject.put("name",name);
        resultObject.put("fitness",fitness);
        resultObject.put("base","fourier");
        resultObject.put("order",order);
        resultObject.put("normalized",true);
        resultObject.put("initial_data_set", initialNumberOfEpisodes);
        //to file
        File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        Yaml yaml = new Yaml();
        yaml.dump(resultObject,new FileWriter(yamlFile));
    }

    public static void lspiPolynomialRunNormalized(
            final double discount, final String name, final int order, final PrototypeScenario scenario,
            final Path containerPath, final Steppable additionalSteppable, NormalizedVariableFeatures inputFeatures,
            String... featureNames) throws IOException, NoSuchFieldException, IllegalAccessException {

        int initialNumberOfEpisodes = numberOfEpisodesInMemory();

        double fitness = lspiRun(new PolynomialBasis(inputFeatures, order, 1), name, discount,
                                 containerPath, new ShodanEnvironment(scenario, additionalSteppable));


        //write a YAML for the results
        HashMap<String,Object> resultObject = new HashMap<>();
        resultObject.put("method","lspi");
        resultObject.put("discount",discount);



        resultObject.put("factors",featureNames);
        resultObject.put("episodes",NUMBER_OF_EPISODES);
        resultObject.put("name",name);
        resultObject.put("fitness",fitness);
        resultObject.put("base","polynomial");
        resultObject.put("order",order);
        resultObject.put("normalized",true);
        resultObject.put("initial_data_set", initialNumberOfEpisodes);
        //to file
        File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        Yaml yaml = new Yaml();
        yaml.dump(resultObject,new FileWriter(yamlFile));
    }

    public static void polynomialRun(
            final double discount, final String name, final int order, final PrototypeScenario scenario,
            final Path containerPath, final Steppable additionalSteppable,
            Object... keys) throws IOException, NoSuchFieldException, IllegalAccessException {

        NumericVariableFeatures inputFeatures = new NumericVariableFeatures(keys);
        int initialNumberOfEpisodes = numberOfEpisodesInMemory();

        double fitness = lspiRun(new PolynomialBasis(inputFeatures, order, 1), name, discount,
                                 containerPath, new ShodanEnvironment(scenario, additionalSteppable));


        //write a YAML for the results
        HashMap<String,Object> resultObject = new HashMap<>();
        resultObject.put("method","lspi");
        resultObject.put("discount",discount);

        String[] names = new String[keys.length];
        for(int i=0; i<names.length; i++)
            names[i] = keys[i].toString();

        resultObject.put("factors",names);
        resultObject.put("episodes",NUMBER_OF_EPISODES);
        resultObject.put("name",name);
        resultObject.put("fitness",fitness);
        resultObject.put("base","polynomial");
        resultObject.put("order",order);
        resultObject.put("normalized",false);
        resultObject.put("initial_data_set", initialNumberOfEpisodes);
        //to file
        File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        Yaml yaml = new Yaml();
        yaml.dump(resultObject,new FileWriter(yamlFile));
    }

    public static void sarsaRun(
            final double discount, final String name, final int order, final double learningRate, final double lambda,
            final PrototypeScenario scenario, final Path containerPath, final Steppable additionalSteppable,
            Object... keys) throws IOException, NoSuchFieldException, IllegalAccessException {

        NumericVariableFeatures inputFeatures = new NumericVariableFeatures(keys);

        //write a YAML for the results
        HashMap<String,Object> resultObject = new HashMap<>();
        resultObject.put("method","sarsa");
        resultObject.put("lambda",lambda);
        resultObject.put("discount",discount);
        resultObject.put("learning_rate",learningRate);
        String[] stringedKeys = new String[keys.length];
        for(int i=0; i<stringedKeys.length; i++)
            stringedKeys[i] = keys[i].toString();
        resultObject.put("factors",stringedKeys);
        resultObject.put("name",name);
        resultObject.put("base","polynomial");
        resultObject.put("order",order);
        resultObject.put("normalized",false);

        //lspiRun sarsa, return last fitness
        double fitness = runSarsa(new PolynomialBasis(inputFeatures, order, 1), name, discount, learningRate, lambda,
                                  containerPath, new ShodanEnvironment(scenario, additionalSteppable),resultObject );



        //to file
        resultObject.put("fitness",fitness);
        resultObject.put("episodes",NUMBER_OF_EPISODES);
        File yamlFile = containerPath.resolve("results").resolve(name + ".yaml").toFile();
        Yaml yaml = new Yaml();
        yaml.dump(resultObject,new FileWriter(yamlFile));

    }

    public static double runSarsa(
            DenseStateFeatures fb, final String directory, final double discount, final double learningRate,
            final double lambda, final Path containerPath, final ShodanEnvironment environment,
            HashMap<String, Object> metadata) throws IOException, IllegalAccessException, NoSuchFieldException {


        containerPath.resolve(directory).toFile().mkdirs();

        System.out.println("running " + directory   );


        SADomain domain = new SADomain();
        domain.setActionTypes(new UniversalActionType(ShodanEnvironment.ACTION_OPEN),
                              new UniversalActionType(ShodanEnvironment.ACTION_CLOSE));

        int i = 0; //episode counter

        DenseLinearVFA parametricState;
        GradientDescentSarsaLam sarsaLam = (GradientDescentSarsaLam) readAgent(containerPath, directory);
        if(sarsaLam==null) {
            parametricState = new DenseLinearVFA(fb, 1);
            sarsaLam = new GradientDescentSarsaLam(domain, discount,
                                                   parametricState,
                                                   learningRate,
                                                   lambda);
            if(fb instanceof  FourierBasis) {
                System.out.println("fourier learning rate!");
                sarsaLam.setLearningRate(new FourierBasisLearningRateWrapper(new ConstantLR(learningRate),
                                                                             (FourierBasis) fb));
            }
        }
        else {
            //read from file, should be safe
            Field vfa = GradientDescentSarsaLam.class.getDeclaredField("vfa");
            vfa.setAccessible(true);
            parametricState = (DenseLinearVFA) vfa.get(sarsaLam);


            List<String> previousLines = Files.readAllLines(containerPath.resolve(directory).resolve("progression.csv"));
            if(previousLines.size() > 0) {
                i = Integer.parseInt(previousLines.get(previousLines.size() - 1).split(",")[0]);
                i++;
                System.out.println("start from episode " + i );

            }
        }
        EpsilonGreedy greedy = new EpsilonGreedy(sarsaLam,.2);
        sarsaLam.setLearningPolicy(greedy);

        environment.resetEnvironment();

        List<Episode> episodeList = new LinkedList<>();

        double lastEstimation = Double.NaN;
       //lspiRun learning for 100 episodes
        for(; i <= NUMBER_OF_EPISODES; i++){

            greedy.setEpsilon(
                    .2 * (NUMBER_OF_EPISODES-i)/(NUMBER_OF_EPISODES));



            episodeList.add(sarsaLam.runLearningEpisode(environment));
            System.out.println(i + ": " + environment.totalReward() + "epsilon: " + (greedy.getEpsilon()));
            String parameters[] = new String[parametricState.numParameters()];
            for(int p=0; p<parameters.length; p++)
                parameters[p] = String.valueOf(parametricState.getParameter(p));
            System.out.println(i + ": " + Strings.join(parameters,","));

            //reset environment for next learning episode
            environment.resetEnvironment();
            if(i% STEPS_PER_LEARNING ==0 )
            {
                System.out.println("force regression");


                environment.resetEnvironment(0);
                //final
                GreedyQPolicy policy = new GreedyQPolicy(sarsaLam);
                PolicyUtils.rollout(policy, environment).write(
                        containerPath.resolve(directory).resolve("lspi_"+i).toAbsolutePath().toString());
                lastEstimation= environment.totalReward();
                System.out.println("final_"+i + ": " +lastEstimation );
                Files.write(containerPath.resolve(directory).resolve("sarsa_"+i+".test"), String.valueOf(environment.totalReward()).getBytes());

                Files.write(containerPath.resolve(directory).resolve("progression.csv"), (i + "," + lastEstimation +"\n").getBytes(),
                            StandardOpenOption.APPEND,StandardOpenOption.CREATE  );


                Files.write(containerPath.resolve(directory).resolve("sarsa_"+i+".csv"), Strings.join(parameters, ",").getBytes());
                environment.resetEnvironment();

                Episode.writeEpisodes(episodeList, containerPath.resolve("data").toAbsolutePath().toString(),
                                      directory);

                saveAgent(containerPath,directory,sarsaLam);
                //to file
                double bestFitness = lastEstimation;
                if(metadata.containsKey("fitness"))
                    bestFitness = Math.max(bestFitness, (Double) metadata.get("fitness"));
                metadata.put("fitness",bestFitness);
                metadata.put("episodes",i);
                File yamlFile = containerPath.resolve("results").resolve(directory + ".yaml").toFile();
                Yaml yaml = new Yaml();
                yaml.dump(metadata,new FileWriter(yamlFile));

                saveAgentHere(containerPath.resolve(directory).resolve("agent_"+i+".xml"),sarsaLam);
            }
        }


        return lastEstimation;
    }



    public static double runQ(
            DenseStateFeatures fb, final String directory, final double discount, final double learningRate,
            final int memory, final int staleDuration,
            final Path containerPath, final ShodanEnvironment environment,
            HashMap<String,Object> metadata) throws IOException, NoSuchFieldException, IllegalAccessException {

        containerPath.resolve(directory).toFile().mkdirs();

        System.out.println("running " + directory   );


        SADomain domain = new SADomain();
        domain.setActionTypes(new UniversalActionType(ShodanEnvironment.ACTION_OPEN),
                              new UniversalActionType(ShodanEnvironment.ACTION_CLOSE));



        DenseLinearVFA parametricState;

        GradientDescentQLearning qLearning = (GradientDescentQLearning) readAgent(containerPath, directory);
        if(qLearning== null) {
            parametricState = new DenseLinearVFA(fb, 1);
            parametricState.evaluate(
                    new ShodanStateOil(0, 0, 0, 0, 0,
                                       0, 0, 0, 0,
                                       0, 0, 0, 0, 0, 0),
                    new SimpleAction(ShodanEnvironment.ACTION_OPEN)); //this initalizes the parametrs

            qLearning = new GradientDescentQLearning(domain, discount,
                                                     parametricState,
                                                     learningRate);
            if(fb instanceof  FourierBasis) {
                System.out.println("fourier learning rate!");
                ConstantLR delegate = new ConstantLR(learningRate);
                qLearning.setLearningRate(new FourierBasisLearningRateWrapper(delegate,
                                                                             (FourierBasis) fb));
            }

            if (memory > 0) {
                qLearning.setExperienceReplay(readExperienceMemory(discount, containerPath), memory);
                qLearning.useStaleTarget(staleDuration);
            }

            if(fb instanceof  FourierBasis) {
                System.out.println("fourier learning rate!");
                qLearning.setLearningRate(new FourierBasisLearningRateWrapper(new ConstantLR(learningRate),
                                                                             (FourierBasis) fb));
            }
        }
        else{
            Field vfa = GradientDescentQLearning.class.getDeclaredField("vfa");
            vfa.setAccessible(true);
            parametricState = (DenseLinearVFA) vfa.get(qLearning);
        }
        environment.resetEnvironment();




        EpsilonGreedy greedy = new EpsilonGreedy(qLearning,.2);
        qLearning.setLearningPolicy(greedy);
        List<Episode> episodeList = new LinkedList<>();

        double lastEstimation = Double.NaN;
        //lspiRun learning for 100 episodes
        for(int i = 0; i <= NUMBER_OF_EPISODES; i++){
            greedy.setEpsilon(
                    .2 * (NUMBER_OF_EPISODES-i)/(NUMBER_OF_EPISODES));



            episodeList.add(qLearning.runLearningEpisode(environment));
            System.out.println(i + ": " + environment.totalReward() + "epsilon: " + (greedy.getEpsilon()));
            String parameters[] = new String[parametricState.numParameters()];
            for(int p=0; p<parameters.length; p++)
                parameters[p] = String.valueOf(parametricState.getParameter(p));
            System.out.println(i + ": " + Strings.join(parameters,","));

            //reset environment for next learning episode
            environment.resetEnvironment();
            if(i% STEPS_PER_LEARNING ==0 )
            {
                System.out.println("force regression");


                environment.resetEnvironment(0);
                //final
                GreedyQPolicy policy = new GreedyQPolicy(qLearning);
                PolicyUtils.rollout(policy, environment).write(
                        containerPath.resolve(directory).resolve("q_"+i).toAbsolutePath().toString());
                lastEstimation= environment.totalReward();
                System.out.println("final_"+i + ": " +lastEstimation );
                Files.write(containerPath.resolve(directory).resolve("q_"+i+".test"), String.valueOf(environment.totalReward()).getBytes());

                //write progression
                Files.write(containerPath.resolve(directory).resolve("progression.csv"), (i + "," + lastEstimation +"\n").getBytes(),
                            StandardOpenOption.APPEND,StandardOpenOption.CREATE  );


                Files.write(containerPath.resolve(directory).resolve("q_"+i+".csv"), Strings.join(parameters, ",").getBytes());
                environment.resetEnvironment();

                Episode.writeEpisodes(episodeList, containerPath.resolve("data").toAbsolutePath().toString(),
                                      directory);

            //    saveAgent(containerPath,directory,qLearning);
                double bestFitness = lastEstimation;
                if(metadata.containsKey("fitness"))
                    bestFitness = Math.max(bestFitness, (Double) metadata.get("fitness"));
                metadata.put("fitness",bestFitness);                metadata.put("episodes",i);
                File yamlFile = containerPath.resolve("results").resolve(directory + ".yaml").toFile();
                Yaml yaml = new Yaml();
                yaml.dump(metadata,new FileWriter(yamlFile));

            }
        }



        return lastEstimation;
    }


    public static double lspiRun(
            DenseStateFeatures fb, final String directory, final double discount,
            final Path path, final ShodanEnvironment environment) throws IOException, NoSuchFieldException, IllegalAccessException {

        path.resolve(directory).toFile().mkdirs();

        System.out.println("running " + directory   );


        SADomain domain = new SADomain();
        domain.setActionTypes(new UniversalActionType(ShodanEnvironment.ACTION_OPEN),
                              new UniversalActionType(ShodanEnvironment.ACTION_CLOSE));
        //create LSPI with discount factor, keep running it until the policy iteration converges
        //densecrossproductfeatures is a way to approximate the Q-Value rather than the Value (like Fourier basis does)
        //3 is the number of actions there are
        LSPI lspi = (LSPI) readAgent(path, directory);
        if(lspi==null)
            lspi = new LSPI(domain, discount, new DenseCrossProductFeatures(fb, 2));

        //are some episodes already available?
        SARSData data = compileEpisodes(path);
        if(data.size()>0) {
            lspi.setDataset(data);
            lspi.runPolicyIteration(30,.001);

        }

        double fitness = Double.NaN;
        environment.resetEnvironment();

        lspi.setMaxNumPlanningIterations(5);
        lspi.setMinNewStepsForLearningPI(200000000);
        List<Episode> episodeList = new LinkedList<>();
        //lspiRun learning for 100 episodes
        for(int i = 0; i <= NUMBER_OF_EPISODES; i++){
            ((EpsilonGreedy) lspi.getLearningPolicy()).setEpsilon(
                    .4 * (STEPS_PER_LEARNING-i% STEPS_PER_LEARNING-1)/(STEPS_PER_LEARNING-1));
            episodeList.add(lspi.runLearningEpisode(environment, 20000));


            System.out.println(i + ": " + environment.totalReward() + "epsilon: " + ((EpsilonGreedy) lspi.getLearningPolicy()).getEpsilon());

            //reset environment for next learning episode
            environment.resetEnvironment();
            if(i% STEPS_PER_LEARNING ==0 && i!=0)
            {
                System.out.println("force regression");
                lspi.runPolicyIteration(30,.001);
                {
                    Field f = lspi.getClass().getDeclaredField("vfa"); //NoSuchFieldException
                    f.setAccessible(true);
                    DenseStateActionLinearVFA parametricState = (DenseStateActionLinearVFA) f.get(lspi); //IllegalAccessException
                    String parameters[] = new String[parametricState.numParameters()];
                    for(int p=0; p<parameters.length; p++)
                        parameters[p] = String.valueOf(parametricState.getParameter(p));
                    System.out.println(i + ": " + Strings.join(parameters,","));
                }

                environment.resetEnvironment(0);
                //final
                GreedyQPolicy policy = new GreedyQPolicy(lspi);
                PolicyUtils.rollout(policy, environment).write(
                        path.resolve(directory).resolve("lspi_"+i).toAbsolutePath().toString());
                fitness = environment.totalReward();
                System.out.println("final_"+i + ": " + fitness );
                Files.write(path.resolve(directory).resolve("lspi_"+i+".test"), String.valueOf(environment.totalReward()).getBytes());
                //try also doing the boltzmann version, for a comparison
                /*
                environment.resetEnvironment(0);
                BoltzmannQPolicy boltzmannQPolicy = new BoltzmannQPolicy(lspi,1d);
                PolicyUtils.rollout(boltzmannQPolicy, environment).write(
                        path.resolve(directory).resolve("lspi_boltzmann_"+i).toAbsolutePath().toString());
                System.out.println("final_boltzmann_"+i + ": " + fitness );
*/

                Field f = lspi.getClass().getDeclaredField("lastWeights"); //NoSuchFieldException
                f.setAccessible(true);
                SimpleMatrix iWantThis = (SimpleMatrix) f.get(lspi); //IllegalAccessException
                iWantThis.saveToFileCSV(path.resolve(directory).resolve("lspi_"+i+".csv").toAbsolutePath().toString());
                environment.resetEnvironment();

                Episode.writeEpisodes(episodeList, path.resolve("data").toAbsolutePath().toString(),
                                      directory);
              //      saveAgent(path,directory,lspi);

            }
        }


        return fitness;
    }


    public static SARSData compileEpisodes(final Path containerPath) throws IOException {


        List<Episode> episodes = Episode.readEpisodes(containerPath.resolve("data").toAbsolutePath().toString());
        SARSData data = new SARSData();
        for(Episode e : episodes)
            for(int t=0; t<e.maxTimeStep(); t++)
                data.add(e.state(t),e.action(t),e.reward(t+1),e.state(t+1));
        System.out.println("datasize " + data.size());
        return data;
    }



    private static ExperienceMemory readExperienceMemory(double discountFactor, final Path container) {

        List<Episode> episodes = Episode.readEpisodes(container.resolve("data").toAbsolutePath().toString());
        ExperienceMemory memory = new FixedSizeMemory(episodes.size()*243);
        for(Episode e : episodes)
            for(int t=0; t<e.maxTimeStep(); t++)
                memory.addExperience(new EnvironmentOptionOutcome(e.state(t),
                                                                  e.action(t),
                                                                  e.state(t+1),
                                                                  e.reward(t+1),
                                                                  t+1==e.maxTimeStep(),
                                                                  discountFactor,
                                                                  e
                ));
        return memory;

    }


    public static void episodesToCSV(final Path containerPath) throws IOException {

        //read all episodes
        List<Episode> episodes = Episode.readEpisodes(containerPath.resolve("data").toAbsolutePath().toString());
        List<String[]> csv = new LinkedList<>();
        //use this object to turn the state into a vector of numbers
        NumericVariableFeatures features = new NumericVariableFeatures();
        State state = episodes.get(0).state(0);
        int featuresLength =  features.features(state).length;
        //write the header: old states, reward, action, new states
        String[] header = new String[featuresLength*2+2];
        for(int i=0; i<featuresLength; i++)
            header[i] = state.variableKeys().get(i).toString();
        header[featuresLength] = "reward";
        header[featuresLength+1] = "action";
        for(int i=0; i<featuresLength; i++)
            header[i+featuresLength+2] = "new_"+state.variableKeys().get(i).toString();
        csv.add(header);
        //for each episode and for each time step
        for(Episode e : episodes)
            for(int t=0; t<e.maxTimeStep(); t++) {

                //write the csv
                String[] line = new String[featuresLength*2+2];
                double[] preState = features.features(e.state(t));
                double[] postState = features.features(e.state(t+1));
                for(int i=0; i<featuresLength; i++)
                    line[i] = String.valueOf(preState[i]);
                line[featuresLength] = String.valueOf(e.reward(t+1));
                line[featuresLength+1] = String.valueOf(e.action(t));
                for(int i=0; i<featuresLength; i++)
                    line[i+featuresLength+2] = String.valueOf(postState[i]);
                csv.add(line);
            }
        //dump
        CSVWriter writer = new CSVWriter(new FileWriter(containerPath.resolve("data.csv").toFile()));
        writer.writeAll(csv);
    }

    public static int numberOfEpisodesInMemory(){
        return Episode.readEpisodes(Paths.get("runs", "burlap").resolve(    "data").toAbsolutePath().toString()).size();
    }


    public static void saveAgent(Path containerPath, String name, LearningAgent agent){
        XStream xstream = new XStream(new StaxDriver());
        Log.info("Writing to file!");
        String xml = xstream.toXML(agent);

        try {
            Files.write(containerPath.resolve("saves").resolve(name+".xml"),xml.getBytes());
            Log.info("Learner saved ");
        } catch (IOException e) {
            e.printStackTrace();
            Log.error(e.getMessage());
        }
    }

    public static void saveAgentHere(Path filePath, LearningAgent agent){
        XStream xstream = new XStream(new StaxDriver());
        Log.info("Writing to file!");
        String xml = xstream.toXML(agent);

        try {
            Files.write(filePath,xml.getBytes());
            Log.info("Learner saved ");
        } catch (IOException e) {
            e.printStackTrace();
            Log.error(e.getMessage());
        }
    }

    public static LearningAgent readAgent(Path containerPath, String name)
    {
        Log.info("Reading from File");
        XStream xstream = new XStream(new StaxDriver());
        String xml = null;
        try {
            byte[] saves = Files.readAllBytes(containerPath.resolve("saves").resolve(name + ".xml"));
            xml = new String(saves);
            Log.info("Learner read ");

            return  (LearningAgent) xstream.fromXML(xml);
        } catch (NoSuchFileException e ) {
      //      e.printStackTrace();
            Log.info("no saved agent yet");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.error("Failed to read file " + name);
            return null;        }
    }


}
