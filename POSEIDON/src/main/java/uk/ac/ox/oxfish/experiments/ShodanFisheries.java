/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.experiments;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.ExternalOpenCloseSeason;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * An attempt to have the regulator learn its
 * Created by carrknight on 12/14/16.
 */
public class ShodanFisheries {

    private static final int SIMULATION_PER_STEP = 3;

    public static final int VALUE_FUNCTION_DIMENSION = 32;


    public static void main(String[] args) throws IOException {


        //do one lspiRun everything open
        Shodan shodan = new Shodan(0, 0);
        FishState initialRun = oneRun(shodan, 0);
        double initialScore = 0;
        for(Double landing : initialRun.getYearlyDataSet().getColumn("Average Cash-Flow"))
            initialScore+=landing;

        BufferedWriter writer = new BufferedWriter(
                new FileWriter(Paths.get("runs", "shodan", "shodan.csv").toFile(), true));
        writer.newLine();
        writer.write("open," + initialScore);
        writer.close();

        //another lspiRun everything random
        shodan.setErrorRate(1);
        initialRun = oneRun(shodan, 0);
        initialScore = 0;
        for(Double landing : initialRun.getYearlyDataSet().getColumn("Average Cash-Flow"))
            initialScore+=landing;

        writer = new BufferedWriter(
                new FileWriter(Paths.get("runs", "shodan", "shodan.csv").toFile(), true));
        writer.newLine();
        writer.write("random," + initialScore);
        writer.close();

        for(int generation=0; generation<500; generation++)
        {
            shodan.generation = generation;
            writer = new BufferedWriter(
                    new FileWriter(Paths.get("runs", "shodan", "shodan_input_"+
                            generation + ".csv").toFile()));

            for(int i=0; i<VALUE_FUNCTION_DIMENSION; i++)
                writer.write("old_feature_"+i+",");
            for(int i=0; i<VALUE_FUNCTION_DIMENSION; i++)
                writer.write("new_feature_"+i+",");
            writer.write("reward,old_action,new_action");
            writer.close();



            for (int run = 0; run < SIMULATION_PER_STEP; run++) {
                oneRun(shodan, System.currentTimeMillis());
                //less error rate
                shodan.setErrorRate(Math.max(shodan.getErrorRate()*.99,.02));
            }
            shodan.regress();
            //make one lspiRun with error rate 0
            double errorRate = shodan.getErrorRate();
            shodan.setErrorRate(0d);
            FishState referenceRun = oneRun(shodan, 0);
            double score = 0;
            for(Double landing : referenceRun.getYearlyDataSet().getColumn("Average Cash-Flow"))
                score+=landing;

            shodan.setErrorRate(errorRate);
            writer = new BufferedWriter(
                    new FileWriter(Paths.get("runs","shodan","shodan.csv").toFile(),true));
            writer.newLine();
            System.out.println(generation + "," + score);
            writer.write(generation + "," + score);
            writer.close();
        }



    }

    private static FishState oneRun(final Shodan shodan, final long seed) {
        //object we use to control season
        ExternalOpenCloseSeason controller = new ExternalOpenCloseSeason();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(300);

        scenario.setRegulation(new AlgorithmFactory<Regulation>() {
            @Override
            public Regulation apply(FishState state) {
                return controller;
            }
        });

        FishState state = new FishState(seed);
        state.attachAdditionalGatherers();
        state.setScenario(scenario);
        state.start();
        //add counter for tows south
        state.getDailyDataSet().registerGatherer("# of South Tows", model -> {
            double towsSouth = 0;
            for (int x = 0; x < 50; x++)
                for (int y = 25; y < 50; y++)
                    towsSouth += state.getMap().getDailyTrawlsMap().get(x, y);

            return towsSouth;
        }, Double.NaN);


        state.scheduleEveryXDay(new Steppable() {
            @Override
            public void step(SimState simState) {
                if (state.getDay() < 30)
                    return;

                shodan.step(simState);
                controller.setOpen(shodan.action == 0);

                System.out.println("Is controller open? " + controller.isOpen());
            }
        }, StepOrder.AFTER_DATA, 30);

        while (state.getDay() <= 7200)
            state.schedule.step(state);
        return state;
    }


    private static class Shodan implements Steppable
    {


        public static final int INTERCEPT = VALUE_FUNCTION_DIMENSION-1;
        public static final int MONTHS_LEFT_INDEX = 4;
        private  double[] oldFeatures;

        private Integer action;


        private double errorRate = 0d;


        private int generation = 0;




        public Shodan(int action,double errorRate) {
            this.errorRate = errorRate;
            this.action = action;
            Arrays.fill(qParameterOpen,0d);
            Arrays.fill(qParameterClosed,0d);
            qParameterOpen[INTERCEPT] = 5000; //start with a simple intercept
            qParameterClosed[INTERCEPT] = 5000; //start with a simple intercept

        }


        /*
         * store the features of S and S' separately. S will be used as x, S' will be used to compute Q.
         */
        private LinkedList<double[]> openPreDecisionState = new LinkedList<>();
        private LinkedList<double[]> openPostDecisionState = new LinkedList<>();
        /*
         * contains rewards observed
         */
        private LinkedList<Double> openRewards = new LinkedList<>();
        private LinkedList<Double> closedRewards = new LinkedList<>();

        private LinkedList<double[]> closedPreDecisionState = new LinkedList<>();
        private LinkedList<double[]> closedPostDecisionState = new LinkedList<>();


        /**
         * the betas of the linear regression when action is close
         */
        private double[] qParameterClosed = new double[VALUE_FUNCTION_DIMENSION];

        /**
         * the betas of the linear regression when action is open
         */
        private double[] qParameterOpen = new double[VALUE_FUNCTION_DIMENSION];

        public double qValue(double[] features, boolean open)
        {

            if(features[MONTHS_LEFT_INDEX] <= FishStateUtilities.EPSILON)
                return 0;
            double sum =0;
            double[] beta;
            if(open)
                beta=qParameterOpen;
            else
                beta=qParameterClosed;

            assert beta.length == features.length;
            for(int i=0; i<features.length; i++)
                sum += beta[i] * features[i];
            return sum;

        }


        /**
         * called when enough data is accumulated
         */
        public void regress()
        {
            qParameterOpen = computeBeta(openPreDecisionState, openPostDecisionState, openRewards);
            qParameterClosed = computeBeta(closedPreDecisionState, closedPostDecisionState, closedRewards);

            System.out.println("**********************************************************");
            System.out.println(Arrays.toString(qParameterOpen));
            System.out.println(Arrays.toString(qParameterClosed));
            System.out.println("**********************************************************");


        }

        private double[] computeBeta(
                final LinkedList<double[]> preDecision, final LinkedList<double[]> postDecision,
                final LinkedList<Double> rewards) {
            assert postDecision.size() == preDecision.size();
            assert preDecision.size() ==  rewards.size();
            double[][] x = new double[preDecision.size()][VALUE_FUNCTION_DIMENSION];
            double[] y = new double[preDecision.size()];
            Iterator<double[]> pre = preDecision.iterator();
            Iterator<double[]> post = postDecision.iterator();
            Iterator<Double> rewardIterator = rewards.iterator();
            int i=0;
            while(pre.hasNext())
            {
                double[] features = pre.next();
                x[i] = Arrays.copyOf(features, features.length);
                //y is just reward plus max Q
                double reward = rewardIterator.next();
                double[] postFeatures = post.next();
                double maxQ = Math.max(qValue(postFeatures,true),qValue(postFeatures,false));
                y[i] = reward+ maxQ;
                i++;
            }
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.setNoIntercept(true); //we bring our own
            regression.newSampleData(y,x);
            return regression.estimateRegressionParameters();
        }

        @Override
        public void step(SimState simState)
        {

            FishState state = (FishState)simState;
            double[] currentFeatures = factorize(state,oldFeatures == null?
                    new double[VALUE_FUNCTION_DIMENSION] : oldFeatures);
            Iterator<Double> landings = state.getDailyDataSet().getColumn(
                    "Average Cash-Flow").descendingIterator();
            double reward = 0;
            for(int i=0; i<30; i++)
                reward += landings.next();



            int previousAction = action;
            //update your actions
            action = qValue(currentFeatures,true) >=  qValue(currentFeatures,false) ? 0 : 1;
            //random chance
            if(state.getRandom().nextDouble()<errorRate)
                action = state.getRandom().nextInt(2);


            if(oldFeatures != null)
            {
                if(previousAction == 0)
                {
                    openPreDecisionState.add(oldFeatures);
                    openPostDecisionState.add(currentFeatures);
                    openRewards.add(reward);
                }
                else
                {
                    closedPreDecisionState.add(oldFeatures);
                    closedPostDecisionState.add(currentFeatures);
                    closedRewards.add(reward);
                }


                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(
                            new FileWriter(Paths.get("runs", "shodan", "shodan_input_"+
                                    generation + ".csv").toFile(), true));
                    writer.newLine();
                    //old features
                    for(double feature : oldFeatures) {
                        writer.write(Double.toString(feature));
                        writer.write(",");
                    }
                    //new features
                    for(double feature : currentFeatures) {
                        writer.write(Double.toString(feature));
                        writer.write(",");
                    }
                    //reward
                    writer.write(Double.toString(reward));
                    writer.write(",");
                    //old action, new action
                    writer.write(Integer.toString(previousAction));
                    writer.write(",");
                    writer.write(Integer.toString(action));
                    writer.flush();
                    writer.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            oldFeatures = currentFeatures;
            System.out.println(Arrays.toString(currentFeatures));




        }

        private double[] factorize(FishState state,  double[] previousFactors)
        {

            //landings, distance from port, cpue, % of tows south, months left till the end
            //averages daily landings, total distance, total cpue, total tows south
            // squares
            // sqrt
            // inter-products

            double[] factors = new double[VALUE_FUNCTION_DIMENSION];
            //landings
            Iterator<Double> landings = state.getDailyDataSet().getColumn(
                    "Species 0 Landings").descendingIterator();
            for(int i=0; i<30; i++)
                factors[0] += landings.next();
            //distance from port
            Iterator<Double> distance = state.getDailyDataSet().getColumn(
                    "Average Distance From Port").descendingIterator();
            for(int i=0; i<30; i++)
                factors[1] += distance.next();
            factors[1]/=30;
            //cpue
            Iterator<Double> effort = state.getDailyDataSet().getColumn(
                    "Total Effort").descendingIterator();
            for(int i=0; i<30; i++)
                factors[2] += effort.next();
            double totalEffort = factors[2];
            factors[2] = factors[2] > 0 ? factors[0]/factors[2] : 0;
            factors[2]/=30;
            factors[0]/=30;

            //south
            Iterator<Double> south = state.getDailyDataSet().getColumn(
                    "# of South Tows").descendingIterator();
            for(int i=0; i<30; i++)
                factors[3] += south.next();
            factors[3]/=30;

            //month-left
            factors[MONTHS_LEFT_INDEX] = (int)(240-state.getDay()/30);
            double month = state.getDay()/30;

            //total daily landings, total distance, total cpue, total tows south
            factors[5] = previousFactors[5] +  (-previousFactors[5]+factors[0])/(month-1);
            factors[6] = previousFactors[6]  + (-previousFactors[6]+factors[1])/(month-1);
            factors[7] = previousFactors[7]  + (-previousFactors[7]+factors[2])/(month-1);
            factors[8] = previousFactors[8]  + (-previousFactors[8]+factors[3])/(month-1);

            // squares
            factors[9] = factors[0] * factors[0];
            factors[10] = factors[1] * factors[1];
            factors[11] = factors[2] * factors[2];
            factors[12] = factors[3] * factors[3];

            // sqrt
            factors[13] = Math.sqrt(factors[0]);
            factors[14] = Math.sqrt(factors[1]);
            factors[15] = Math.sqrt(factors[2]);
            factors[16] = Math.sqrt(factors[3]);

            //months gone out
            ExternalOpenCloseSeason controller = (ExternalOpenCloseSeason) state.getFishers().get(0).getRegulation();
            if(controller.isOpen())
                factors[17]=0;
            else
                factors[17] = previousFactors[17]+1;


            //interactions
            factors[18] = factors[0] * factors[1];
            factors[19] = factors[0] * factors[2];
            factors[20] = factors[0] * factors[3];
            factors[21] = factors[0] * factors[4];
            factors[22] = factors[1] * factors[2];
            factors[23] = factors[1] * factors[3];
            factors[24] = factors[1] * factors[4];
            factors[25] = factors[2] * factors[3];
            factors[26] = factors[2] * factors[4];
            factors[27] = factors[3] * factors[4];
            factors[28] = factors[0] * factors[17];
            factors[29] = factors[1] * factors[17];
            factors[30] = factors[4] * factors[17];

            //intercept!
            factors[INTERCEPT] = 1;


            return factors;

        }

        /**
         * Getter for property 'nextAction'.
         *
         * @return Value for property 'nextAction'.
         */
        public int getAction() {
            return action;
        }

        /**
         * Getter for property 'errorRate'.
         *
         * @return Value for property 'errorRate'.
         */
        public double getErrorRate() {
            return errorRate;
        }

        /**
         * Setter for property 'errorRate'.
         *
         * @param errorRate Value to set for property 'errorRate'.
         */
        public void setErrorRate(double errorRate) {
            this.errorRate = errorRate;
        }
    }






}
   /*
            Species fishSpecies = state.getBiology().getSpecie(0);
            //5% chance of destroying the south
            state.registerStartable(new BiologicalEvent(
                    state1 -> {
                        return state1.getRandom().nextDouble() <= 0.008174; //5% chance yearly
                    },
                    tile -> tile.getGridY() >= 25,
                    tile -> {
                        //kill off 90% of the biomass
                        BiomassLocalBiology biology = (BiomassLocalBiology) tile.getBiology();
                        biology.setCurrentBiomass(
                                fishSpecies,Math.max(250,biology.getBiomass(fishSpecies)));
                    }

            ));
            //5% of resurging the south
            state.registerStartable(new BiologicalEvent(
                    state12 -> {
                        return state12.getRandom().nextDouble() <= 0.008174; //5% chance yearly
                    },
                    tile -> tile.getGridY() >= 25,
                    tile -> {
                        //go back to carrying capacity
                        BiomassLocalBiology biology = (BiomassLocalBiology) tile.getBiology();
                        biology.setCurrentBiomass(
                                fishSpecies,biology.getCarryingCapacity(fishSpecies));
                    }

            ));
*/