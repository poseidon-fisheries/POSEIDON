package uk.ac.ox.oxfish.model.scenario;

import ec.util.MersenneTwisterFast;
import org.jenetics.DoubleGene;
import org.jenetics.Genotype;
import org.jenetics.util.Factory;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.ga.FisherEvolution;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.StrategyFactory;
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
    public List<Fisher> populateModel(FishState model) {
        List<Fisher> fishers = delegate.populateModel(model);



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
        return fishers;

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

    public void setDepthSmoothing(int depthSmoothing) {
        delegate.setDepthSmoothing(depthSmoothing);
    }


    public void setRegulation(
            StrategyFactory<? extends Regulation> regulation) {
        delegate.setRegulation(regulation);
    }

    public StrategyFactory<? extends Regulation> getRegulation() {
        return delegate.getRegulation();
    }

    public int getHeight() {
        return delegate.getHeight();
    }






    public int getFishers() {
        return delegate.getFishers();
    }


    public void setPorts(int ports) {
        delegate.setPorts(ports);
    }

    public int getWidth() {
        return delegate.getWidth();
    }

    public void setHeight(int height) {
        delegate.setHeight(height);
    }


    public void setGridSizeInKm(double gridSizeInKm) {
        delegate.setGridSizeInKm(gridSizeInKm);
    }



    public DoubleParameter getFishingEfficiency() {
        return delegate.getFishingEfficiency();
    }



    public void setSpeedInKmh(DoubleParameter speedInKmh) {
        delegate.setSpeedInKmh(speedInKmh);
    }

    public int getNumberOfSpecies() {
        return delegate.getNumberOfSpecies();
    }

    public void setFishers(int fishers) {
        delegate.setFishers(fishers);
    }


    public StrategyFactory<? extends DepartingStrategy> getDepartingStrategy() {
        return delegate.getDepartingStrategy();
    }

    public void setFishingEfficiency(DoubleParameter fishingEfficiency) {
        delegate.setFishingEfficiency(fishingEfficiency);
    }

    public void setWidth(int width) {
        delegate.setWidth(width);
    }

    public DoubleParameter getHoldSize() {
        return delegate.getHoldSize();
    }

    public void setFishingStrategy(
            StrategyFactory<? extends FishingStrategy> fishingStrategy) {
        delegate.setFishingStrategy(fishingStrategy);
    }

    public int getCoastalRoughness() {
        return delegate.getCoastalRoughness();
    }

    public void setHoldSize(DoubleParameter holdSize) {
        delegate.setHoldSize(holdSize);
    }

    public void setCoastalRoughness(int coastalRoughness) {
        delegate.setCoastalRoughness(coastalRoughness);
    }

    public StrategyFactory<? extends FishingStrategy> getFishingStrategy() {
        return delegate.getFishingStrategy();
    }

    public int getDepthSmoothing() {
        return delegate.getDepthSmoothing();
    }

    public int getPorts() {
        return delegate.getPorts();
    }


    public void setDepartingStrategy(
            StrategyFactory<? extends DepartingStrategy> departingStrategy) {
        delegate.setDepartingStrategy(departingStrategy);
    }



    public double getGridSizeInKm() {
        return delegate.getGridSizeInKm();
    }

    public DoubleParameter getSpeedInKmh() {
        return delegate.getSpeedInKmh();
    }
}
