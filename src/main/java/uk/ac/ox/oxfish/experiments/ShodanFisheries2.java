package uk.ac.ox.oxfish.experiments;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Port;
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
 * Created by carrknight on 12/15/16.
 */
public class ShodanFisheries2 {


    private static final int SIMULATION_PER_STEP = 2;

    public static final int VALUE_FUNCTION_DIMENSION = 6;

    public static final String directory = "shodan4";


    public static void main(String[] args) throws IOException {

        ShodanFisheries2.Shodan shodan = new ShodanFisheries2.Shodan(0, 0);

        //do one run everything open
        FishState initialRun = oneRun(shodan, 0);
        double initialScore = 0;
        for(Double landing : initialRun.getYearlyDataSet().getColumn("Average Cash-Flow"))
            initialScore+=landing;

        BufferedWriter writer = new BufferedWriter(
                new FileWriter(Paths.get("runs", directory, "shodan.csv").toFile(), true));
        writer.newLine();
        writer.write("open," + initialScore);
        writer.close();

        //another run everything random
        shodan.setErrorRate(1);
        initialRun = oneRun(shodan, 0);
        initialScore = 0;
        for(Double landing : initialRun.getYearlyDataSet().getColumn("Average Cash-Flow"))
            initialScore+=landing;

        writer = new BufferedWriter(
                new FileWriter(Paths.get("runs", directory, "shodan.csv").toFile(), true));
        writer.newLine();
        writer.write("random," + initialScore);
        writer.close();

        //now start for real
        shodan.setErrorRate(.9);
        for(int generation=0; generation<500; generation++)
        {
            shodan.generation = generation;
            writer = new BufferedWriter(
                    new FileWriter(Paths.get("runs", directory, "shodan_input_"+
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
                shodan.setErrorRate(Math.max(shodan.getErrorRate()*.99,.05));
            }
            shodan.regress();
            //make one run with error rate 0
            double errorRate = shodan.getErrorRate();
            shodan.setErrorRate(0d);
            FishState referenceRun = oneRun(shodan, 0);
            double score = 0;
            for(Double landing : referenceRun.getYearlyDataSet().getColumn("Average Cash-Flow"))
                score+=landing;

            shodan.setErrorRate(errorRate);
            writer = new BufferedWriter(
                    new FileWriter(Paths.get("runs", directory, "shodan.csv").toFile(), true));
            writer.newLine();
            System.out.println((generation+1) + "," + score);
            writer.write(generation + "," + score);
            writer.close();
        }



    }

    private static FishState oneRun(final ShodanFisheries2.Shodan shodan, final long seed) {
        //object we use to control season
        ExternalOpenCloseSeason controller = new ExternalOpenCloseSeason();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(100);

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


        state.scheduleEveryXDay(new Steppable() {
            @Override
            public void step(SimState simState) {
                if (state.getDay() < 30)
                    return;

                //change oil price
                for(Port port : state.getPorts())
                    port.setGasPricePerLiter(state.getDayOfTheYear()/1000d);

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


        public static final int INTERCEPT = 5;
        public static final int MONTHS_LEFT_INDEX = 4;
        private  double[] oldFeatures;

        private Integer action;


        private double errorRate = 0d;


        private int generation = 0;




        public Shodan(int action,double errorRate) {
            this.errorRate = errorRate;
            this.action = action;
            Arrays.fill(qParameterOpen, 0d);
            Arrays.fill(qParameterClosed,0d);

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

            System.out.println("reward: " + reward);


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
                            new FileWriter(Paths.get("runs", directory, "shodan_input_"+
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
            factors[2]= factors[2] > 0 ? factors[0]/factors[2] : 0;

            factors[0]/=30;

            //day of the year
        //    factors[3]= state.getDayOfTheYear();
          //  factors[3]= state.getDayOfTheYear();
            //months gone out
            ExternalOpenCloseSeason controller = (ExternalOpenCloseSeason) state.getFishers().get(0).getRegulation();
            if(controller.isOpen())
                factors[3]=0;
            else
                factors[3] = previousFactors[3]+1;


            //month-left
            factors[MONTHS_LEFT_INDEX] = (int)(240-state.getDay()/30);

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
            System.out.println("error rate:"+errorRate);
        }
    }






}
