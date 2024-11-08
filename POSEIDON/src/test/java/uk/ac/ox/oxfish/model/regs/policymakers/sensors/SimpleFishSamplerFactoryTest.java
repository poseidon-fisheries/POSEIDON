package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class SimpleFishSamplerFactoryTest {


    @Test
    public void onehundredpercentSamplingMakesItEqual() {

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(20);

        final SimpleFishSamplerFactory sampler = new SimpleFishSamplerFactory();
        sampler.setPercentageSampled(new FixedDoubleParameter(1.0));
        scenario.getPlugins().add(sampler);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        while (state.getYear() < 2)
            state.schedule.step(state);

        state.schedule.step(state);

        Assertions.assertEquals(state.getYearlyDataSet().getColumn("Species 0 CPUE").getLatest(),
            state.getYearlyDataSet().getColumn("Species 0 CPUE Scaled Sample").getLatest(),
            .001);

        Assertions.assertEquals(state.getYearlyDataSet().getColumn("Species 0 CPHO").getLatest(),
            state.getYearlyDataSet().getColumn("Species 0 CPHO Scaled Sample").getLatest(),
            .001);

        Assertions.assertEquals(state.getYearlyDataSet().getColumn("Species 0 Landings").getLatest(),
            state.getYearlyDataSet().getColumn("Species 0 Landings Scaled Sample").getLatest(),
            .001);


    }
}
