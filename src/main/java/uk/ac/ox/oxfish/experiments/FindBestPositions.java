package uk.ac.ox.oxfish.experiments;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializers;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.FixedFavoriteDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.RandomFavoriteDestinationFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.runs.BeanFisherModification;
import uk.ac.ox.oxfish.model.runs.Experiment;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A simple experiment where I modify the position of one fisher keeping everything constant and see how simple hill-climbing
 * works in practice. It turns out to be extremely slow and wasteful. Better optimization, like PSO, ought to be used.
 * Created by carrknight on 7/27/15.
 */
public class FindBestPositions {


    private final static int YEARS_TO_SIMULATE = 10;

    private final static long SEED = 0;

    private final static int NUMBER_OF_FISHERS = 20;

    private final static LinkedList<Consumer<FishState>> ACCEPTABLE_CHANGES = new LinkedList<>();

    private static PrototypeScenario generateScenario()
    {

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(BiologyInitializers.CONSTRUCTORS.get("From Left To Right Fixed").get());
        scenario.setMapInitializer(new SimpleMapInitializerFactory(50, 50, 4, 1000000, 10));
        scenario.setDestinationStrategy(new RandomFavoriteDestinationFactory());
        scenario.setFishers(NUMBER_OF_FISHERS);

        return  scenario;
    }

    private static Consumer<FishState> generateExperimentCondition(MersenneTwisterFast randomizer, int fisherId, int width, int height)
    {

        int x = randomizer.nextInt(width);
        int y = randomizer.nextInt(height);

        FixedFavoriteDestinationFactory destinationStrategy = new FixedFavoriteDestinationFactory();
        destinationStrategy.setX(x);
        destinationStrategy.setY(y);
        return new BeanFisherModification(fisherId,"destinationStrategy",
                                                                      destinationStrategy);


    }
    

    private static Consumer<FishState> getAllPreviouslyAcceptedConditions()
    {
        Consumer<FishState> consumer = state -> {
        };

        for(Consumer<FishState> c : ACCEPTABLE_CHANGES)
            consumer = consumer.andThen(c);

        return consumer;
    }
    
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {

        MersenneTwisterFast randomizer = new MersenneTwisterFast();




        ExecutorService runner = Executors.newFixedThreadPool(2);
        FishYAML yaml = new FishYAML();

        while(true)
        {
            //choose a fisher to change at random
            int fisherToChange = randomizer.nextInt(NUMBER_OF_FISHERS);
            //grab the scenario
            PrototypeScenario scenario = generateScenario();
            Consumer<PrototypeScenario> scenarioMod = prototypeScenario -> {}; //i won't modify the scenario
            //create fitness function
            Function<FishState,Double> fitness = state -> {
                List<Fisher> fishers = state.getFishers();
                Optional<Fisher> firstFound = fishers.stream().filter(fisher -> fisher.getID() == fisherToChange).findFirst();
                return firstFound.get().getBankBalance();
            };
            //this will proceed by A-B trial
            Consumer<FishState> aCondition = getAllPreviouslyAcceptedConditions();
            Consumer<FishState> newCondition = generateExperimentCondition(randomizer, fisherToChange,
                                                                           50,
                                                                           50);
            Consumer<FishState> bCondition = getAllPreviouslyAcceptedConditions().andThen(newCondition);

            //run the two experiments

            Experiment<PrototypeScenario> aExperiment= new Experiment<>(SEED, YEARS_TO_SIMULATE, fitness,
                                                                        scenario, scenarioMod,aCondition);
            Experiment<PrototypeScenario> bExperiment = new Experiment<>(SEED, YEARS_TO_SIMULATE, fitness,
                                                                         scenario, scenarioMod,bCondition);
            Future<Double> aResult = runner.submit(aExperiment);
            Future<Double> bResult = runner.submit(bExperiment);

            double aFitness = aResult.get();
            double bFitness = -1;
            try {
                bFitness = bResult.get();
            }catch (ExecutionException e)
            {
                assert e.getCause() instanceof IllegalArgumentException;
                //this can happen if we randomly send the poor guy to fish on land. If it happens, ignore it!
            }


            System.out.println(aFitness + " ----- " + bFitness);
            if(bFitness>aFitness) {
                ACCEPTABLE_CHANGES.add(newCondition);
                FileWriter writer = new FileWriter(Paths.get("optimal.yaml").toFile(),false);
                writer.write(yaml.dump(ACCEPTABLE_CHANGES));
                writer.close();
            }
        }
        
    }


    

}
