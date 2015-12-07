package uk.ac.ox.oxfish.demoes;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.experiments.MarketFirstDemo;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;



public class TACNotCaresAboutMileage {


    @Test
    public void tacNotCaresAboutMileage() throws Exception {

        FishState state=
                MarketFirstDemo.generateAndRunMarketDemo(MarketFirstDemo.MarketDemoPolicy.TAC,
                                                         new FixedDoubleParameter(.1),
                                                         new UniformDoubleParameter(0, 20),
                                                         Paths.get("runs", "market1", "tacOil.csv").toFile(),
                                                         5, System.currentTimeMillis());

        //the correlation ought to be very small
        Species species = state.getSpecies().get(0);

        double[] mileage = new double[state.getFishers().size()];
        double[] catches =  new double[state.getFishers().size()];

        int i=0;
        for(Fisher fisher : state.getFishers())
        {
            mileage[i] = (((RandomCatchabilityTrawl) fisher.getGear()).getTrawlSpeed());
            catches[i] = fisher.getLatestYearlyObservation(
                    species + " " + AbstractMarket.LANDINGS_COLUMN_NAME);

            i++;
        }

        double correlation = FishStateUtilities.computeCorrelation(mileage, catches);
        System.out.println("the correlation between mileage and TAC is: " + correlation);
        System.out.println("Ideally it should be, in absolute value, less than .3");
        assertTrue(correlation <.3);
        assertTrue(correlation >-.3);


    }
}
