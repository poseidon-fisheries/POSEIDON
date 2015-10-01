package uk.ac.ox.oxfish.experiments.dedicated.habitat;


import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.RockyLogisticFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HabitatAwareGearFactory;
import uk.ac.ox.oxfish.geography.habitat.AllSandyHabitatFactory;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.ScenarioEssentials;
import uk.ac.ox.oxfish.model.scenario.ScenarioPopulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class HabitatDeploymentScenario implements Scenario{


    private PrototypeScenario delegate = new PrototypeScenario();


    private AlgorithmFactory<? extends BiologyInitializer> biology = new RockyLogisticFactory();

    private AlgorithmFactory<? extends HabitatInitializer> habitat = new AllSandyHabitatFactory();

    private AlgorithmFactory<? extends Gear> gear = new HabitatAwareGearFactory();

    public HabitatDeploymentScenario() {

        delegate.setGridCellSizeInKm(2);

    }


    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioEssentials start(FishState model) {
        delegate.setGear(gear);
        delegate.setHabitatInitializer(habitat);
        delegate.setBiologyInitializer(biology);
        return delegate.start(model);
    }

    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(FishState model) {
        return delegate.populateModel(model);
    }

    public AlgorithmFactory<? extends HabitatInitializer> getHabitat() {
        return habitat;
    }

    public void setHabitat(
            AlgorithmFactory<? extends HabitatInitializer> habitat) {
        this.habitat = habitat;
    }


    public AlgorithmFactory<? extends Gear> getGear() {
        return gear;
    }


    public int getFishers() {
        return delegate.getFishers();
    }

    public void setFishers(int fishers) {
        delegate.setFishers(fishers);
    }

    public double getGridCellSizeInKm() {
        return delegate.getGridCellSizeInKm();
    }

    public void setGridCellSizeInKm(double gridCellSizeInKm) {
        delegate.setGridCellSizeInKm(gridCellSizeInKm);
    }

    public void setLiterPerKilometer(DoubleParameter literPerKilometer) {
        delegate.setLiterPerKilometer(literPerKilometer);
    }

    public DoubleParameter getLiterPerKilometer() {
        return delegate.getLiterPerKilometer();
    }

    public AlgorithmFactory<? extends BiologyInitializer> getBiology() {
        return biology;
    }

    public Long getMapRandomSeed() {
        return delegate.getMapMakerDedicatedRandomSeed();
    }

    public void setMapRandomSeed(Long mapMakerDedicatedRandomSeed) {
        delegate.setMapMakerDedicatedRandomSeed(mapMakerDedicatedRandomSeed);
    }
}
