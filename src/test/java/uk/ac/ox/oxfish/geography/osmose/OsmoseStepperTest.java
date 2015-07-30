package uk.ac.ox.oxfish.geography.osmose;

import ec.util.MersenneTwisterFast;
import fr.ird.osmose.OsmoseSimulation;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.*;


public class OsmoseStepperTest {


    @Test
    public void stepsCorrectly() throws Exception {

        OsmoseSimulation osmose = mock(OsmoseSimulation.class);
        when(osmose.stepsPerYear()).thenReturn(365);
        OsmoseStepper stepper = new OsmoseStepper(365*2,osmose,new MersenneTwisterFast());
        final LocalOsmoseBiology localBiology = mock(LocalOsmoseBiology.class);
        stepper.getToReset().add(localBiology);
        stepper.start(mock(FishState.class));


        for(int i=0; i<10;i++)
            stepper.step(mock(FishState.class));

        verify(osmose,times(5)).oneStep();
        verify(localBiology,times(5)).osmoseStep();


    }
}