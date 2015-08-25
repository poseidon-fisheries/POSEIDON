package uk.ac.ox.oxfish.demoes;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.experiments.MarketFirstDemo;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityThrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;


public class ITQCaresAboutMileage {

    @Test
    public void itqNotCaresAboutMileage() throws Exception {

        FishState state=
                MarketFirstDemo.generateAndRunMarketDemo(MarketFirstDemo.MarketDemoPolicy.ITQ,
                                                         new FixedDoubleParameter(.1),
                                                         new UniformDoubleParameter(0, 20),
                                                         Paths.get("runs", "market1", "tacOil.csv").toFile(),
                                                         5, 0);

        //the correlation ought to be very small
        Specie specie = state.getSpecies().get(0);

        double[] mileage = new double[state.getFishers().size()];
        double[] catches =  new double[state.getFishers().size()];

        int i=0;
        for(Fisher fisher : state.getFishers())
        {
            mileage[i] = (((RandomCatchabilityThrawl) fisher.getGear()).getThrawlSpeed());
            catches[i] = fisher.getLatestYearlyObservation(
                    specie + " " + AbstractMarket.LANDINGS_COLUMN_NAME);

            i++;
        }

        assertTrue(FishStateUtilities.computeCorrelation(mileage, catches)<-.6);


    }
}
