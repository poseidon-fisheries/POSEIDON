package uk.ac.ox.oxfish.model.scenario;

import org.jenetics.DoubleGene;
import org.jenetics.Genotype;
import org.jenetics.util.Factory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.ga.FisherEvolution;
import uk.ac.ox.oxfish.model.regs.Regulations;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The prototype scenario with made up biomass and on top of that a GA optimization
 * Created by carrknight on 5/5/15.
 */
public abstract class PrototypeGeneticScenario implements Scenario {


    private PrototypeScenario delegate = new PrototypeScenario();




    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioResult start(FishState model) {

        //modify the scenario before starting, if needed
        modifyPrototypeScenario(delegate, model);

        ScenarioResult result = delegate.start(model);
        //modify results, if needed
        result = modifyScenarioResult(result);

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

        return result;
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
    protected abstract ScenarioResult modifyScenarioResult(ScenarioResult result);

    /**
     * the factory that generates new random genotypes
     * @param result the scenario result, if you need to focus on it
     * @return the factory
     */
    protected abstract Factory<Genotype<DoubleGene>> generateGenotypeFactory(ScenarioResult result);

    /**
     * the function that transforms fishers to genotypes ready to be optimized
     * @param result the scenario result
     * @return the transformer
     */
    protected abstract Function<Fisher,Genotype<DoubleGene>> generateFisherToGenotypeTransformer(ScenarioResult result);

    /**
     * the function that modifies fishers with their new genotype
     * @param result the scenario result
     * @return the adapter
     */
    protected abstract Consumer<Pair<Fisher,Genotype<DoubleGene>>> generateFisherAdapterToNewGenotype(ScenarioResult result);

    public void setDepthSmoothing(int depthSmoothing) {
        delegate.setDepthSmoothing(depthSmoothing);
    }

    public double getMaxDepartingProbability() {
        return delegate.getMaxDepartingProbability();
    }

    public double getMinFishingEfficiency() {
        return delegate.getMinFishingEfficiency();
    }

    public Regulations getRegulation() {
        return delegate.getRegulation();
    }

    public void setMaxDepartingProbability(double maxDepartingProbability) {
        delegate.setMaxDepartingProbability(maxDepartingProbability);
    }

    public int getCoastalRoughness() {
        return delegate.getCoastalRoughness();
    }

    public int getMaxBiomass() {
        return delegate.getMaxBiomass();
    }

    public double getMinSpeedInKmh() {
        return delegate.getMinSpeedInKmh();
    }

    public void setMinSpeedInKmh(double minSpeedInKmh) {
        delegate.setMinSpeedInKmh(minSpeedInKmh);
    }

    public void setRegulation(Regulations regulation) {
        delegate.setRegulation(regulation);
    }

    public int getHeight() {
        return delegate.getHeight();
    }

    public int getDepthSmoothing() {
        return delegate.getDepthSmoothing();
    }

    public double getMaxSpeedInKmh() {
        return delegate.getMaxSpeedInKmh();
    }

    public int getMinBiomass() {
        return delegate.getMinBiomass();
    }

    public void setFishers(int fishers) {
        delegate.setFishers(fishers);
    }

    public double getMinDepartingProbability() {
        return delegate.getMinDepartingProbability();
    }

    public double getMaxHoldSize() {
        return delegate.getMaxHoldSize();
    }

    public int getBiologySmoothing() {
        return delegate.getBiologySmoothing();
    }

    public void setMaxBiomass(int maxBiomass) {
        delegate.setMaxBiomass(maxBiomass);
    }

    public double getMinHoldSize() {
        return delegate.getMinHoldSize();
    }

    public void setBiologySmoothing(int biologySmoothing) {
        delegate.setBiologySmoothing(biologySmoothing);
    }

    public double getMaxFishingEfficiency() {
        return delegate.getMaxFishingEfficiency();
    }

    public void setCoastalRoughness(int coastalRoughness) {
        delegate.setCoastalRoughness(coastalRoughness);
    }

    public double getGridSizeInKm() {
        return delegate.getGridSizeInKm();
    }

    public void setWidth(int width) {
        delegate.setWidth(width);
    }

    public int getPorts() {
        return delegate.getPorts();
    }

    public void setGridSizeInKm(double gridSizeInKm) {
        delegate.setGridSizeInKm(gridSizeInKm);
    }

    public void setMaxFishingEfficiency(double maxFishingEfficiency) {
        delegate.setMaxFishingEfficiency(maxFishingEfficiency);
    }

    public void setMaxHoldSize(double maxHoldSize) {
        delegate.setMaxHoldSize(maxHoldSize);
    }

    public void setMinBiomass(int minBiomass) {
        delegate.setMinBiomass(minBiomass);
    }

    public void setMinHoldSize(double minHoldSize) {
        delegate.setMinHoldSize(minHoldSize);
    }

    public void setMinFishingEfficiency(double minFishingEfficiency) {
        delegate.setMinFishingEfficiency(minFishingEfficiency);
    }

    public int getFishers() {
        return delegate.getFishers();
    }

    public void setMaxSpeedInKmh(double maxSpeedInKmh) {
        delegate.setMaxSpeedInKmh(maxSpeedInKmh);
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

    public void setMinDepartingProbability(double minDepartingProbability) {
        delegate.setMinDepartingProbability(minDepartingProbability);
    }
}
