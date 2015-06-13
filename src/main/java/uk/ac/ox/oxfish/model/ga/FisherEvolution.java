package uk.ac.ox.oxfish.model.ga;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.util.Factory;
import org.jenetics.util.RandomRegistry;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An attempt at a generic way to evolve Fishers every year.
 * Basically it is a yearly steppable that does the following:
 * <ol>
 *     <li>Maps each fisher to a Genotype (a collection of chromosomes)</li>
 *     <li>Run a GA algorithm on the genotypes</li>
 *     <li>Uses the surviving genotypes to change fishers </li>
 * </ol>
 * The main thing to keep in mind is that genotypes are final and unique in jenetics. this means
 * that you need to provide at least one random gene/chromosome so that the mapping fisher< --- > genotype is unique
 * Created by carrknight on 5/4/15.
 */
public class FisherEvolution<G extends Gene<?,G>> implements Steppable, Startable {


    /**
     * I need a function to each worker into a genotype so I can improve it genetically
     */
    final private Function<Fisher,Genotype<G>> fisherToGenotypeTransformer;

    /**
     * a function to move back from genotype to fisher
     */
    final private Consumer<Pair<Fisher,Genotype<G>>> genotypeToFisherTransformer;


    /**
     * a way to keep the fisher and genotype connected
     */
    final private BiMap<Fisher,Genotype<G>> fisherToGenotypeMap =  HashBiMap.create();

    /**
     * a way to create new genotypes.
     */
    final private Factory<Genotype<G>> genotypeFactory;

    public FisherEvolution(
            Factory<Genotype<G>> genotypeFactory,
            Function<Fisher, Genotype<G>> fisherToGenotypeTransformer,
            Consumer<Pair<Fisher, Genotype<G>>> genotypeToFisherTransformer) {
        this.fisherToGenotypeTransformer = fisherToGenotypeTransformer;
        this.genotypeToFisherTransformer = genotypeToFisherTransformer;
        this.genotypeFactory = genotypeFactory;
    }


    private Engine<G,Double> engine;

    private int generation = 1;

    private Stoppable receipt = null;

    public void start(FishState model)
    {

        Preconditions.checkArgument(receipt==null,"Already started");


        //weird setting up of the randomizer
        RandomRegistry.setRandom(new Random(model.seed()));
        //create the engine!
        engine = Engine.builder(genotypeFitness, genotypeFactory)
                .populationSize(model.getFishers().size()+1)
                .survivorsSelector(new StochasticUniversalSelector<>())
                .offspringSelector(new TournamentSelector<>(5))
                .alterers(
                        new Mutator<>(0.005),
                        new GaussianMutator(0.1),
                        new SinglePointCrossover<>(0.5))
                .build();




        //schedule yourself every year!
        receipt = model.scheduleEveryYear(this, StepOrder.AFTER_DATA);

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        receipt.stop();
    }

    @Override
    public void step(SimState simState) {

        //create population from fisher list
        FishState model = (FishState) simState;
        List<Fisher> fishers = model.getFishers();
        Population<G, Double> population = new Population<>();
        double averageFitness = 0.0;
        for (Fisher fisher : fishers) {
            averageFitness += fitness.apply(fisher);
            Genotype<G> genotype = fisherToGenotypeTransformer.apply(fisher);
            fisherToGenotypeMap.put(fisher, genotype);
            population.add(Phenotype.of(genotype, generation, genotypeFitness));
        }
        averageFitness /= fishers.size();
        System.out.println("average fitness: " + averageFitness);

        //now that the population is ready, evolve it one step
        EvolutionResult<G, Double> evolved = engine.evolve(population, generation);
        population = evolved.getPopulation();
        generation++;



        //now go for each fisher and apply changes as you see fit
        for (int i=0; i<fishers.size(); i++) {
            genotypeToFisherTransformer.accept(
                    new Pair<>(fishers.get(i),
                               population.get(i).getGenotype()));


        }
        fisherToGenotypeMap.clear();
    }
    /**
     * the fitness function. By default it's just the net-cash-flow of the fisher
     */
    public Function<Fisher,Double> fitness =
            fisher -> fisher.getYearlyData().getColumn("NET_CASH_FLOW").getLatest();

    private Function<Genotype<G>,Double> genotypeFitness =
            chromosomes -> {
                Fisher fisher = fisherToGenotypeMap.inverse().get(chromosomes);
                if(fisher == null)
                    return  -1d; //the fitness of new values (that aren't associated to any fisher) is -1
                //this means that we cannot use this for multiple steps at once, we need to re-read the fisher list
                //all the time
                return fitness.apply(fisher);
            };


    public Function<Fisher, Double> getFitness() {
        return fitness;
    }

    public void setFitness(Function<Fisher, Double> fitness) {
        this.fitness = fitness;
    }
}
