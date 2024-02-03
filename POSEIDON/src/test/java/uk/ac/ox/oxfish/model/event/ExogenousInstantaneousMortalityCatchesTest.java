package uk.ac.ox.oxfish.model.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesBoxcarAbstractFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesRegularBoxcarFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class ExogenousInstantaneousMortalityCatchesTest {


    @Test
    public void biomassTest() {

        final FlexibleScenario scenario = new FlexibleScenario();
        scenario.getFisherDefinitions().clear();

        ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).
            setCarryingCapacity(new FixedDoubleParameter(1000));
        ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).setMinInitialCapacity(new FixedDoubleParameter(1));
        ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).setMaxInitialCapacity(new FixedDoubleParameter(1));
        ((SimpleLogisticGrowerFactory) ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).getGrower()).setSteepness(
            new FixedDoubleParameter(0d));
        // work!
        final FishState state = new FishState();
        state.setScenario(scenario);

        final ExogenousInstantaneousMortalityCatchesFactory factory = new ExogenousInstantaneousMortalityCatchesFactory();
        factory.getExogenousMortalities().put("Species 0", .5);
        factory.setAbundanceBased(false);

        scenario.setExogenousCatches(factory);

        state.start();


        for (int i = 0; i < 363; i++) {
            state.schedule.step(state);
            Assertions.assertEquals(
                state.getMap().getSeaTile(2, 2).getBiomass(state.getSpecies("Species 0")),
                1000,
                .0001
            );
        }

        for (int i = 0; i < 30; i++) {
            state.schedule.step(state);

        }
        Assertions.assertEquals(state.getMap().getSeaTile(2, 2).getBiomass(state.getSpecies("Species 0")),
            606.5307,
            .0001);


    }


    @Test
    public void abundanceTest() {

        final FlexibleScenario scenario = new FlexibleScenario();
        scenario.getFisherDefinitions().clear();


        final SingleSpeciesBoxcarAbstractFactory boxy = new SingleSpeciesRegularBoxcarFactory();
        scenario.setBiologyInitializer(boxy);

        // Feed an empty ExogenousCatches factory to the scenario, as we'll build our own later
        scenario.setExogenousCatches(__ -> new ExogenousCatches() {
            private static final long serialVersionUID = 3903468230543739523L;

            @Override
            public void start(final FishState model) {
            }

            @Override
            public void step(final SimState simState) {
            }
        });

        boxy.setInitialBtOverK(new FixedDoubleParameter(1));
        //   boxy.setYearlyMortality(new FixedDoubleParameter(0d));
        //   boxy.setK(new FixedDoubleParameter(0d));
        //  boxy.setSteepness(new FixedDoubleParameter(0.0001d));
        // work!
        final FishState state = new FishState(0);
        state.setScenario(scenario);

        final ExogenousInstantaneousMortalityCatchesFactory factory = new ExogenousInstantaneousMortalityCatchesFactory();
        factory.getExogenousMortalities().put("Red Fish", .5);
        factory.setAbundanceBased(true);


        state.start();


        //manual to get more control
        final ExogenousInstantaneousMortalityCatches catches = factory.apply(state);


        state.schedule.step(state);
        Assertions.assertEquals(state.getMap().getSeaTile(2, 2).getBiomass(state.getSpecies("Red Fish")), 2266886, 1);

        //    catches.start(state);
        catches.step(state);
        Assertions.assertEquals(state.getMap().getSeaTile(2, 2).getBiomass(state.getSpecies("Red Fish")), 1374936, 1);


    }
}
