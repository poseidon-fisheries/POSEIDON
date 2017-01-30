package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.behavior.functionapproximation.dense.DenseLinearVFA;
import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.functionapproximation.dense.NormalizedVariableFeatures;
import burlap.behavior.functionapproximation.dense.fourier.FourierBasis;
import burlap.behavior.functionapproximation.dense.fourier.FourierBasisLearningRateWrapper;
import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.vardomain.VariableDomain;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import com.beust.jcommander.internal.Nullable;
import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import joptsimple.internal.Strings;
import org.yaml.snakeyaml.Yaml;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static uk.ac.ox.oxfish.experiments.burlapspike.BurlapShodan.NUMBER_OF_EPISODES;
import static uk.ac.ox.oxfish.experiments.burlapspike.BurlapShodan.STEPS_PER_LEARNING;

/**
 * Created by carrknight on 1/4/17.
 */
public class BurlapQuotaInfinity {


    public static final double INITIAL_EPSILON = 1;

    public static void main(String[] args) throws IllegalAccessException, NoSuchFieldException, IOException {


        Log.set(Log.LEVEL_INFO);

        Path containerPath = Paths.get("runs", "burlap_infinity");
        containerPath.toFile().mkdirs();
        containerPath.resolve("data").toFile().mkdirs();
        containerPath.resolve("results").toFile().mkdirs();
        containerPath.resolve("saves").toFile().mkdirs();

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(300);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setCoastalRoughness(new FixedDoubleParameter(0d));
        scenario.setPortPositionX(40);
        scenario.setPortPositionY(25);
        scenario.setMapInitializer(mapInitializer);
        DiffusingLogisticFactory biologyInitializer = new DiffusingLogisticFactory();
        biologyInitializer.setGrower(new SimpleLogisticGrowerFactory(.7));
        scenario.setBiologyInitializer(biologyInitializer);

        //just look at biomass
        NormalizedVariableFeatures features =
                new NormalizedVariableFeatures().
                        variableDomain(ShodanStateOil.BIOMASS, new VariableDomain(0, 10500000)).
                        variableDomain(ShodanStateOil.DAY_OF_THE_YEAR, new VariableDomain(0, 243));

/*
        sarsaRunFourier(.999,
                        "999_sarsa_biomass_9lambda_fourier2",
                        4,
                        .0025,
                        .9,
                        features,
                        scenario,
                        containerPath, (Steppable) simState -> {},
                        .2,
                        null,
                        ShodanStateOil.BIOMASS,
                        ShodanStateOil.DAY_OF_THE_YEAR);
*/

/*
        sarsaRunFourier(.999,
                        "999_sarsa_biomass_9lambda_fourier_baseline",
                        4,
                        .0025,
                        .9,
                        features,
                        scenario,
                        containerPath, (Steppable) simState -> {},
                        .2,
                        new Pair<>(new ShodanStateOil(
                                0,0,0,0,0,1,
                                0,0,0,0,
                                0,0,5000000,0,0,0
                        ), new SimpleAction(ShodanEnvironment.ACTION_OPEN)),
                        ShodanStateOil.BIOMASS,
                        ShodanStateOil.DAY_OF_THE_YEAR);
*/
        //look at yearly landings only (basically discover quotas)
        features =
                new NormalizedVariableFeatures().
                        variableDomain(ShodanStateOil.AVERAGE_YEARLY_LANDINGS, new VariableDomain(0, 5500)).
                        variableDomain(ShodanStateOil.DAY_OF_THE_YEAR, new VariableDomain(0, 243));

        /*
        sarsaRunFourier(.999,
                        "99_sarsa_landings_7lambda_fourier",
                        4,
                        .005,
                        .9,
                        features,
                        scenario,
                        containerPath, (Steppable) simState -> {},
                        ShodanStateOil.AVERAGE_YEARLY_LANDINGS,
                        ShodanStateOil.DAY_OF_THE_YEAR);
                        */



        features =
                new NormalizedVariableFeatures().
                        variableDomain(ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, new VariableDomain(0, 440)).
                        variableDomain(ShodanStateOil.AVERAGE_YEARLY_CASHFLOW, new VariableDomain(-5, 300)).
                        variableDomain(ShodanStateOil.DAY_OF_THE_YEAR, new VariableDomain(0, 365));

        /*
        sarsaRunFourier(.999,
                        "999_sarsa_cashdistance_9lambda_fourier",
                        4,
                        .0025,
                        .9,
                        features,
                        scenario,
                        containerPath, (Steppable) simState -> {},
                        .2,
                        ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                        ShodanStateOil.AVERAGE_YEARLY_CASHFLOW,
                        ShodanStateOil.DAY_OF_THE_YEAR);

*/

        /*
        sarsaRunFourier(.999,
                        "999_sarsa_cashdistance_9lambda_fourier_highepsilon",
                        4,
                        .0025,
                        .9,
                        features,
                        scenario,
                        containerPath, (Steppable) simState -> {},
                        1,null,
                        ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                        ShodanStateOil.AVERAGE_YEARLY_CASHFLOW,
                        ShodanStateOil.DAY_OF_THE_YEAR);

*/
        features =
                new NormalizedVariableFeatures().
                        variableDomain(ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, new VariableDomain(0, 440)).
                        variableDomain(ShodanStateOil.AVERAGE_YEARLY_CASHFLOW, new VariableDomain(-5, 300)).
                        variableDomain(ShodanStateOil.MONTHS_CLOSED, new VariableDomain(0, 12*5)); //cutoff at 5 years



/*
        sarsaRunFourier(.999,
                        "999_sarsa_cashdistanceclosed_9lambda_fourier",
                        4,
                        .0025,
                        .9,
                        features,
                        scenario,
                        containerPath, (Steppable) simState -> {},
                        .2,
                        null,
                        ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                        ShodanStateOil.AVERAGE_YEARLY_CASHFLOW,
                        ShodanStateOil.MONTHS_CLOSED);
*/
/*
        sarsaRunFourier(.999,
                        "999_sarsa_cashdistanceclosed_9lambda_fourier_highepsilon",
                        4,
                        .0025,
                        .9,
                        features,
                        scenario,
                        containerPath, (Steppable) simState -> {},
                        1,
                        null,
                        ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                        ShodanStateOil.AVERAGE_YEARLY_CASHFLOW,
                        ShodanStateOil.MONTHS_CLOSED);
*/
        /*
        sarsaRunFourier(.999,
                        "999_sarsa_cashdistanceclosed_9lambda_fourier_baseline",
                        4,
                        .0025,
                        .9,
                        features,
                        scenario,
                        containerPath, (Steppable) simState -> {},
                        .2,
                        new Pair<>(new ShodanStateOil(
                                0,0,0,0,268.5,1,
                                0,0,0,0,
                                0,0,0,25.220,0,0
                        ),new SimpleAction(ShodanEnvironment.ACTION_OPEN)),
                        ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                        ShodanStateOil.AVERAGE_YEARLY_CASHFLOW,
                        ShodanStateOil.MONTHS_CLOSED);
                        */
        sarsaRunFourier(.999,
                        "999_sarsa_cashdistanceclosed_9lambda_fourier_baseline_highepsilon2",
                        4,
                        .0025,
                        .9,
                        features,
                        scenario,
                        containerPath, (Steppable) simState -> {},
                        1,
                        new Pair<>(new ShodanStateOil(
                                0,0,0,0,268.5,1,
                                0,0,0,0,
                                0,0,0,25.220,0,0
                        ),new SimpleAction(ShodanEnvironment.ACTION_OPEN)),
                        ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                        ShodanStateOil.AVERAGE_YEARLY_CASHFLOW,
                        ShodanStateOil.MONTHS_CLOSED);


    }



    public static void sarsaRunFourier(
            final double discount, final String name, final int order, final double learningRate, final double lambda,
            NormalizedVariableFeatures inputFeatures, final PrototypeScenario scenario, final Path containerPath,
            final Steppable additionalSteppable, final double initialEpsilon, @Nullable Pair<ShodanStateOil,Action> baseline,
            String... featureNames) throws IOException, NoSuchFieldException, IllegalAccessException {




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
        resultObject.put("initial_epsilon", initialEpsilon);
        resultObject.put("normalized",true);
        resultObject.put("baseline", baseline != null);
        //run sarsa, return last fitness
        double fitness = runSarsa(new FourierBasis(inputFeatures, order), name, discount, learningRate, lambda,
                                  containerPath, scenario,baseline, resultObject, initialEpsilon);

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


    public static double runSarsa(
            DenseStateFeatures fb, final String directory, final double discount, final double learningRate,
            final double lambda, final Path containerPath, final PrototypeScenario scenario,
            @Nullable Pair<ShodanStateOil,Action> baseline,
            HashMap<String, Object> metadata, final double initialEpsilon) throws IOException, IllegalAccessException, NoSuchFieldException {

        MersenneTwisterFast random = new MersenneTwisterFast();

        //

        //never ending scenario
        ShodanEnvironment delegate = new ShodanEnvironment(scenario, (Steppable) simState -> {
        }, -1);
        Environment environment = delegate;

        containerPath.resolve(directory).toFile().mkdirs();

        System.out.println("running " + directory   );


        SADomain domain = new SADomain();
        domain.setActionTypes(new UniversalActionType(ShodanEnvironment.ACTION_OPEN),
                              new UniversalActionType(ShodanEnvironment.ACTION_CLOSE));

        int i = 0; //episode counter


        //initialize sarsalam
        DenseLinearVFA parametricState;
        GradientDescentSarsaLam sarsaLam = (GradientDescentSarsaLam) BurlapShodan.readAgent(containerPath, directory);
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
        //poliy
        EpsilonGreedy greedy = new EpsilonGreedy(sarsaLam, initialEpsilon);
        sarsaLam.setLearningPolicy(greedy);


        //add baseline if you need to
        if(baseline != null)
            environment = new RelativeRewardEnvironmentDecorator(sarsaLam,
                                                                 environment,
                                                                 baseline.getFirst(),
                                                                 baseline.getSecond());
        //start model
        ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).setMaxInitialCapacity(new FixedDoubleParameter(
                random.nextDouble()
        ));
        environment.resetEnvironment();

        List<Episode> episodeList = new LinkedList<>();

        double lastEstimation = Double.NaN;
        //lspiRun learning for NUMBER_OF_EPISODES
        for(; i <= NUMBER_OF_EPISODES; i++){

            greedy.setEpsilon(
                    initialEpsilon * (NUMBER_OF_EPISODES-i)/(NUMBER_OF_EPISODES));


            int maxYears = 5 + random.nextInt(40);
            int maxDays = maxYears *365; //5 to 45 years of simulation
            int maxMonths = (int) Math.ceil(maxDays / 30.42);
            System.out.println("years to run: " +  maxYears);
            //run!

            episodeList.add(sarsaLam.runLearningEpisode(environment,maxMonths));
            double runReward;


            System.out.println(i + ": " + delegate.totalReward()/maxYears + "epsilon: " + (greedy.getEpsilon()));
            String parameters[] = new String[parametricState.numParameters()];
            for(int p=0; p<parameters.length; p++)
                parameters[p] = String.valueOf(parametricState.getParameter(p));
            System.out.println(i + ": " + Strings.join(parameters, ","));

            //reset environment for next learning episode
            ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).setMaxInitialCapacity(new FixedDoubleParameter(
                    random.nextDouble()
            ));
            environment.resetEnvironment();
            if(i% STEPS_PER_LEARNING ==0 )
            {
                System.out.println("20 year check");

                ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).setMaxInitialCapacity(new FixedDoubleParameter(
                        1d
                ));
                delegate.resetEnvironment(0);
                //final
                GreedyQPolicy policy = new GreedyQPolicy(sarsaLam);
                PolicyUtils.rollout(policy, environment,244).write(
                        containerPath.resolve(directory).resolve("lspi_"+i).toAbsolutePath().toString());
                lastEstimation= delegate.totalReward();
                System.out.println("final_"+i + ": " +lastEstimation );
                Files.write(containerPath.resolve(directory).resolve("sarsa_"+i+".test"), String.valueOf(lastEstimation).getBytes());

                Files.write(containerPath.resolve(directory).resolve("progression.csv"), (i + "," + lastEstimation +"\n").getBytes(),
                            StandardOpenOption.APPEND, StandardOpenOption.CREATE  );


                Files.write(containerPath.resolve(directory).resolve("sarsa_"+i+".csv"), Strings.join(parameters, ",").getBytes());

                ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).setMaxInitialCapacity(new FixedDoubleParameter(
                        random.nextDouble()
                ));
                environment.resetEnvironment();

                Episode.writeEpisodes(episodeList, containerPath.resolve("data").toAbsolutePath().toString(),
                                      directory);

                BurlapShodan.saveAgent(containerPath,directory,sarsaLam);
                //to file
                double bestFitness = lastEstimation;
                if(metadata.containsKey("fitness"))
                    bestFitness = Math.max(bestFitness, (Double) metadata.get("fitness"));
                metadata.put("fitness",bestFitness);
                metadata.put("episodes",i);
                File yamlFile = containerPath.resolve("results").resolve(directory + ".yaml").toFile();
                Yaml yaml = new Yaml();
                yaml.dump(metadata,new FileWriter(yamlFile));

                BurlapShodan.saveAgentHere(containerPath.resolve(directory).resolve("agent_"+i+".xml"),sarsaLam);
            }
        }


        return lastEstimation;
    }



}
