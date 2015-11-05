package uk.ac.ox.oxfish.model.scenario;

import org.jenetics.DoubleGene;
import org.jenetics.Genotype;
import org.jenetics.util.Factory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.ga.FisherEvolution;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The prototype scenario with made up biomass and on top of that a GA optimization
 * Created by carrknight on 5/5/15.
 */
public abstract class PrototypeGeneticScenario implements Scenario {


    private PrototypeScenario delegate = new PrototypeScenario();



    private ScenarioEssentials result;

    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioEssentials start(FishState model) {

        //modify the scenario before starting, if needed
        modifyPrototypeScenario(delegate, model);

        result = delegate.start(model);
        //modify results, if needed
        result = modifyScenarioResult(result);

        return result;
    }

    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(FishState model) {
        List<Fisher> fishers = delegate.populateModel(model).getPopulation();



        //get the required functions
        Factory<Genotype<DoubleGene>> factory = generateGenotypeFactory(result);
        Function<Fisher,Genotype<DoubleGene>> transformer = generateFisherToGenotypeTransformer(result);
        Consumer<Pair<Fisher,Genotype<DoubleGene>>> adapter = generateFisherAdapterToNewGenotype(result);


        //now add the evolution object
        FisherEvolution<DoubleGene> evolution = new FisherEvolution<>(factory,
                                                                      transformer,
                                                                      adapter);

        //set it up
        model.registerStartable(evolution);
        return new ScenarioPopulation(fishers,new SocialNetwork(new EmptyNetworkBuilder()));
    }

    /**
     * if we need to change anything of the prototype scenario, do it here
     * @param scenario the original scenario
     * @param model
     */
    protected abstract PrototypeScenario modifyPrototypeScenario(PrototypeScenario scenario, FishState model);

    /**
     * after the scenario has started, use this if you want to change something
     * @param result the result of starting the scenario
     * @return the final result
     */
    protected abstract ScenarioEssentials modifyScenarioResult(ScenarioEssentials result);

    /**
     * the factory that generates new random genotypes
     * @param result the scenario result, if you need to focus on it
     * @return the factory
     */
    protected abstract Factory<Genotype<DoubleGene>> generateGenotypeFactory(ScenarioEssentials result);

    /**
     * the function that transforms fishers to genotypes ready to be optimized
     * @param result the scenario result
     * @return the transformer
     */
    protected abstract Function<Fisher,Genotype<DoubleGene>> generateFisherToGenotypeTransformer(ScenarioEssentials result);

    /**
     * the function that modifies fishers with their new genotype
     * @param result the scenario result
     * @return the adapter
     */
    protected abstract Consumer<Pair<Fisher,Genotype<DoubleGene>>> generateFisherAdapterToNewGenotype(ScenarioEssentials result);



    public void setRegulation(
            AlgorithmFactory<? extends Regulation> regulation) {
        delegate.setRegulation(regulation);
    }

    public AlgorithmFactory<? extends Regulation> getRegulation() {
        return delegate.getRegulation();
    }







    public int getFishers() {
        return delegate.getFishers();
    }


    public void setPorts(int ports) {
        delegate.setPorts(ports);
    }






    public void setSpeedInKmh(DoubleParameter speedInKmh) {
        delegate.setSpeedInKmh(speedInKmh);
    }



    public void setFishers(int fishers) {
        delegate.setFishers(fishers);
    }


    public AlgorithmFactory<? extends DepartingStrategy> getDepartingStrategy() {
        return delegate.getDepartingStrategy();
    }




    public DoubleParameter getHoldSize() {
        return delegate.getHoldSize();
    }

    public void setFishingStrategy(
            AlgorithmFactory<? extends FishingStrategy> fishingStrategy) {
        delegate.setFishingStrategy(fishingStrategy);
    }


    public void setHoldSize(DoubleParameter holdSize) {
        delegate.setHoldSize(holdSize);
    }


    public AlgorithmFactory<? extends FishingStrategy> getFishingStrategy() {
        return delegate.getFishingStrategy();
    }


    public int getPorts() {
        return delegate.getPorts();
    }


    public void setDepartingStrategy(
            AlgorithmFactory<? extends DepartingStrategy> departingStrategy) {
        delegate.setDepartingStrategy(departingStrategy);
    }




    public DoubleParameter getSpeedInKmh() {
        return delegate.getSpeedInKmh();
    }
}
