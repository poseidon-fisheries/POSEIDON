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
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.vardomain.VariableDomain;
import burlap.mdp.singleagent.SADomain;
import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import joptsimple.internal.Strings;
import org.yaml.snakeyaml.Yaml;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.initializer.DiffusingLogisticInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
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
        biologyInitializer.setSteepness(new FixedDoubleParameter(.7));
        scenario.setBiologyInitializer(biologyInitializer);

        //just look at biomass
        NormalizedVariableFeatures features =
                new NormalizedVariableFeatures().
                        variableDomain(ShodanStateOil.BIOMASS, new VariableDomain(0, 10500000)).
                        variableDomain(ShodanStateOil.DAY_OF_THE_YEAR, new VariableDomain(0, 243));

        /*
        sarsaRunFourier(.99,
                                     "99_sarsa_biomass_7lambda_fourier",
                                     4,
                                     .005,
                                     .7,
                                     features,
                                     scenario,
                                     containerPath, (Steppable) simState -> {},
                                     ShodanStateOil.BIOMASS,
                                     ShodanStateOil.DAY_OF_THE_YEAR);
*/
        //look at yearly landings only (basically discover quotas)
        features =
                new NormalizedVariableFeatures().
                        variableDomain(ShodanStateOil.AVERAGE_YEARLY_LANDINGS, new VariableDomain(0, 5500)).
                        variableDomain(ShodanStateOil.DAY_OF_THE_YEAR, new VariableDomain(0, 243));

        sarsaRunFourier(.99,
                        "99_sarsa_landings_7lambda_fourier",
                        4,
                        .005,
                        .7,
                        features,
                        scenario,
                        containerPath, (Steppable) simState -> {},
                        ShodanStateOil.BIOMASS,
                        ShodanStateOil.DAY_OF_THE_YEAR);

    }



    public static void sarsaRunFourier(
            final double discount, final String name, final int order, final double learningRate, final double lambda,
            NormalizedVariableFeatures inputFeatures, final PrototypeScenario scenario, final Path containerPath,
            final Steppable additionalSteppable,
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
        resultObject.put("normalized",true);
        //run sarsa, return last fitness
        double fitness = runSarsa(new FourierBasis(inputFeatures, order), name, discount, learningRate, lambda,
                                  containerPath,scenario, resultObject );

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
            HashMap<String, Object> metadata) throws IOException, IllegalAccessException, NoSuchFieldException {

        MersenneTwisterFast random = new MersenneTwisterFast();

        //((DiffusingLogisticFactory) scenario.getBiologyInitializer()).setMaxCapacity(randomnumber)

        //never ending scenario
        ShodanEnvironment environment = new ShodanEnvironment(scenario, (Steppable) simState -> {
        },-1);
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
        EpsilonGreedy greedy = new EpsilonGreedy(sarsaLam, .2);
        sarsaLam.setLearningPolicy(greedy);

        //start model
        environment.resetEnvironment();

        List<Episode> episodeList = new LinkedList<>();

        double lastEstimation = Double.NaN;
        //lspiRun learning for 100 episodes
        for(; i <= NUMBER_OF_EPISODES; i++){

            greedy.setEpsilon(
                    .2 * (NUMBER_OF_EPISODES-i)/(NUMBER_OF_EPISODES));


            int maxYears = 5 + random.nextInt(40);
            int maxDays = maxYears *365; //5 to 45 years of simulation
            int maxMonths = (int) Math.ceil(maxDays / 30.42);
            System.out.println("years to run: " +  maxYears);
            //run!

            episodeList.add(sarsaLam.runLearningEpisode(environment,maxMonths));
            System.out.println(i + ": " + environment.totalReward()/maxYears + "epsilon: " + (greedy.getEpsilon()));
            String parameters[] = new String[parametricState.numParameters()];
            for(int p=0; p<parameters.length; p++)
                parameters[p] = String.valueOf(parametricState.getParameter(p));
            System.out.println(i + ": " + Strings.join(parameters, ","));

            //reset environment for next learning episode
            environment.resetEnvironment();
            if(i% STEPS_PER_LEARNING ==0 )
            {
                System.out.println("20 year check");


                environment.resetEnvironment(0);
                //final
                GreedyQPolicy policy = new GreedyQPolicy(sarsaLam);
                PolicyUtils.rollout(policy, environment,244).write(
                        containerPath.resolve(directory).resolve("lspi_"+i).toAbsolutePath().toString());
                lastEstimation= environment.totalReward();
                System.out.println("final_"+i + ": " +lastEstimation );
                Files.write(containerPath.resolve(directory).resolve("sarsa_"+i+".test"), String.valueOf(environment.totalReward()).getBytes());

                Files.write(containerPath.resolve(directory).resolve("progression.csv"), (i + "," + lastEstimation +"\n").getBytes(),
                            StandardOpenOption.APPEND, StandardOpenOption.CREATE  );


                Files.write(containerPath.resolve(directory).resolve("sarsa_"+i+".csv"), Strings.join(parameters, ",").getBytes());
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
