package uk.ac.ox.oxfish.demoes;

import junit.framework.Assert;
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
import java.util.LinkedList;

import static org.junit.Assert.assertTrue;



public class TACNotCaresAboutMileage {


    @Test
    public void tacNotCaresAboutMileage() throws Exception {

        FishState state=
                MarketFirstDemo.generateAndRunMarketDemo(MarketFirstDemo.MarketDemoPolicy.TAC,
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

        double correlation = FishStateUtilities.computeCorrelation(mileage, catches);
        assertTrue(correlation <.3);
        assertTrue(correlation >-.3);


    }
}
