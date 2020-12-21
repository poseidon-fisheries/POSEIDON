package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static uk.ac.ox.oxfish.model.FishStateDailyTimeSeries.getAllMarketColumns;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.EFFORT;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.HOURS_OUT;

/**
 * not convoluted like SPR agent. This simply replicates some of the gatherers
 * of fishState, but only a (always randomm) proportion of agents
 */
public class SimpleFisherSampler implements AdditionalStartable {


    private final double percentageOfFishersToSample;


    public SimpleFisherSampler(double percentageOfFishersToSample) {
        this.percentageOfFishersToSample = percentageOfFishersToSample;
    }

    /**
     * samples at random, making sure at least one fisher is sampled each time
     */
    private List<Fisher> sampleFishers(List<Fisher> original,
                                       MersenneTwisterFast random){

        Preconditions.checkArgument(original.size()>0,"There are no fishers to sample!!");
        List<Fisher> toReturn = new LinkedList<>();

        for (Fisher fisher : original) {
            if(random.nextDouble()<percentageOfFishersToSample)
                toReturn.add(fisher);
        }

        //if you collected at least one fisher, you are good to go
        if(toReturn.size()>0)
            return toReturn;
        else
            //retry!
            return sampleFishers(original, random);


    }



    @Override
    public void start(FishState model) {





        for (Species species : model.getSpecies()) {


            final String catchesColumn = species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME;

            //CPUE
            model.getYearlyDataSet().registerGatherer(species + " CPUE" + " Scaled Sample",
                    new Gatherer<FishState>() {
                        @Override
                        public Double apply(FishState fishState) {
                            final String catches = catchesColumn;


                            final List<Fisher> fishers = sampleFishers(model.getFishers(), model.getRandom());
                            double numerator = 0;
                            double denominator = 0;
                            for (Fisher fisher : fishers) {
                                denominator+=fisher.getYearlyCounterColumn(EFFORT);
                                //this data set should have been filled before we call (individual time series happen in order before the aggregate ones)
                                assert fisher.getYearlyData().getColumn(species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME).size() ==
                                        fishState.getYear()+1;
                                numerator+=fisher.getYearlyData().getColumn(species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME).getLatest();
                            }

                            double cpue = (numerator / denominator);
                            return cpue/percentageOfFishersToSample;

                        }
                    }, Double.NaN);

            //CPHO
            model.getYearlyDataSet().registerGatherer(species + " CPHO" + " Scaled Sample",
                    new Gatherer<FishState>() {
                        @Override
                        public Double apply(FishState fishState) {


                            final List<Fisher> fishers = sampleFishers(model.getFishers(), model.getRandom());
                            double numerator = 0;
                            double denominator = 0;
                            for (Fisher fisher : fishers) {
                                denominator+=fisher.getYearlyCounterColumn(HOURS_OUT);
                                //this data set should have been filled before we call (individual time series happen in order before the aggregate ones)
                                assert fisher.getYearlyData().getColumn(species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME).size() ==
                                        fishState.getYear()+1;
                                numerator+=fisher.getYearlyData().getColumn(species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME).getLatest();
                            }

                            return (numerator / denominator)/percentageOfFishersToSample;

                        }
                    }, Double.NaN);

            //Landings
            model.getYearlyDataSet().registerGatherer(species + " Landings"+ " Scaled Sample",
                    new Gatherer<FishState>() {
                        @Override
                        public Double apply(FishState fishState) {


                            final List<Fisher> fishers = sampleFishers(model.getFishers(), model.getRandom());
                            double numerator = 0;
                            for (Fisher fisher : fishers) {
                                //this data set should have been filled before we call (individual time series happen in order before the aggregate ones)
                                assert fisher.getYearlyData().getColumn(species + " " + AbstractMarket.LANDINGS_COLUMN_NAME).size() ==
                                        fishState.getYear()+1;
                                numerator+=fisher.getYearlyData().getColumn(species + " " + AbstractMarket.LANDINGS_COLUMN_NAME).getLatest();
                            }

                            return (numerator)/percentageOfFishersToSample;

                        }
                    }, Double.NaN);


        }
    }
}
