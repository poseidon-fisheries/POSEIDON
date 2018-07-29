package uk.ac.ox.oxfish.maximization;

import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.GarbageGearFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomTrawlStringFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.scenario.DerisoCaliforniaScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

public class CaliforniaDerisoOptimization extends SimpleProblemDouble {

    private String scenarioFile =
            Paths.get("docs","paper3_dts","mark2",
                    "exploratory","calibration","histograms","deriso_new_manual3.yaml").toString();


    private String summaryDirectory =
            Paths.get("docs","paper3_dts","mark2",
                    "exploratory","eva").toString();

    private long seed = 0;


    // basically how much a +1 in input means in terms of % change in parameter
    private double multiplier =  .1;


    private int yearsToRun = 5;

    private int yearsToIgnore = 1;


    //TARGETS
    private static final double YELLOW_QUOTA = 800;
    private static final double DOVER_QUOTA = 21190006.5696815;
    private static final double LONGSPINE_QUOTA = 1788870.1;
    private static final double SABLEFISH_QUOTA = 1418397.36611325;
    private static final double SHORTSPINE_QUOTA = 1319456.10095308;
    private static final double[] YELLOW_ATTAINMENT = new double[]{6.6, 2};
    private static final double[] DOVER_ATTAINMENT = new double[]{29.98 , 3.75};
    private static final double[] LONGSPINE_ATTAINMENT = new double[]{48.425 , 4.61 };
    private static final double[] SHORTSPINE_ATTAINMENT = new double[]{49.714 , 4.71 };
    private static final double[] SABLEFISH_ATTAINMENT = new double[]{91.8, 7.51264925998236};
    private static final double[] HOURS_AT_SEA = new double[]{1024.32, 94.3844054915853};
    private static final double[] PROFITS = new double[]{116019.255, 13962};
    private static final double[] DISTANCE = new double[]{103.484794189846, 51.7606763378253};
    private static final double[] DURATION = new double[]{60.70, 31.95};


    private double startingDoverCatchability = 6e-05;
    private double startingLongspineCatchability = 2e-04;
    private double startingSablefishCatchability = 3.17986760953e-05;
    private double startingShortspineCatchability = 4.32282202495e-05;
    private double startingYelloweyeCatchability = 4.11440673662e-07;


    private int runsPerSetting = 1;

    @Override
    public double[] evaluate(double[] x) {

        FishYAML yaml = new FishYAML();
        try {
            double error = 0;
            Path scenarioPath = Paths.get(scenarioFile);


            for(int i=0; i<runsPerSetting; i++)
            {
                DerisoCaliforniaScenario derisoScenario = yaml.loadAs(
                        new FileReader(scenarioPath.toFile()),
                        DerisoCaliforniaScenario.class);


                modifyScenarioFile(x, derisoScenario);


                FishState model = new FishState(getSeed()+i);
                model.setScenario(derisoScenario);
                model.start();
                System.out.println("starting run");
                while (model.getYear() < yearsToRun) {
                    model.schedule.step(model);
                }
                model.schedule.step(model);


                //catches errors
                error +=
                        deviationAttainment(
                                model.getYearlyDataSet().getColumn("Dover Sole Landings"),
                                DOVER_QUOTA,
                                DOVER_ATTAINMENT[0],
                                DOVER_ATTAINMENT[1],
                                1);
                error +=
                        deviationAttainment(
                                model.getYearlyDataSet().getColumn("Longspine Thornyhead Landings"),
                                LONGSPINE_QUOTA,
                                LONGSPINE_ATTAINMENT[0],
                                LONGSPINE_ATTAINMENT[1],
                                1);
                error +=
                        deviationAttainment(
                                model.getYearlyDataSet().getColumn("Shortspine Thornyhead Landings"),
                                SHORTSPINE_QUOTA,
                                SHORTSPINE_ATTAINMENT[0],
                                SHORTSPINE_ATTAINMENT[1],
                                1);

                error +=
                        deviationAttainment(
                                model.getYearlyDataSet().getColumn("Yelloweye Rockfish Landings"),
                                YELLOW_QUOTA,
                                YELLOW_ATTAINMENT[0],
                                YELLOW_ATTAINMENT[1],
                                1);

                error +=
                        deviationAttainment(
                                model.getYearlyDataSet().getColumn("Sablefish Landings"),
                                SABLEFISH_QUOTA,
                                SABLEFISH_ATTAINMENT[0],
                                SABLEFISH_ATTAINMENT[1],
                                1);


                error +=
                        deviation(
                                model.getYearlyDataSet().getColumn("Actual Average Hours Out"),
                                HOURS_AT_SEA[0],
                                HOURS_AT_SEA[1],
                                1
                        );

                error +=
                        deviation(
                                model.getYearlyDataSet().getColumn("Actual Average Cash-Flow"),
                                PROFITS[0],
                                PROFITS[1],
                                1
                        );

                error +=
                        deviation(
                                model.getYearlyDataSet().getColumn("Average Trip Duration"),
                                DURATION[0],
                                DURATION[1],
                                1
                        );

                error +=
                        deviation(
                                model.getYearlyDataSet().getColumn("Average Distance From Port"),
                                DISTANCE[0],
                                DISTANCE[1],
                                1
                        );

            }

            error /= (double) runsPerSetting;
            //write summary file
            Files.write(
                    Paths.get(summaryDirectory).resolve(
                            scenarioPath.getFileName() + "_"+seed+".csv"
                    ),
                    (error +"," + Arrays.toString(x).
                            replace("[","").
                            replace("]","") +"\n").getBytes(),
                    StandardOpenOption.WRITE,StandardOpenOption.CREATE,StandardOpenOption.APPEND
            );


            return new double[]{error};


        } catch (IOException  e) {
            e.printStackTrace();
            throw new RuntimeException("failed to read input file!");
        }


    }


    //saves best option so far
    public static void main(String[] args) throws IOException {
        FishYAML yaml = new FishYAML();
        CaliforniaDerisoOptimization optimization = new CaliforniaDerisoOptimization();

        //produce local best
        Path scenarioPath = Paths.get(optimization.scenarioFile);


        DerisoCaliforniaScenario derisoScenario = yaml.loadAs(
                new FileReader(scenarioPath.toFile()),
                DerisoCaliforniaScenario.class);


        optimization.modifyScenarioFile(
                //new double[]{ 0.610,-1.203,-1.558,-1.730,-0.601, 0.528}
                new double[]{ 0.054,-0.482,-1.303,-0.947, 0.342,-1.823}
                ,derisoScenario);

        yaml.dump(derisoScenario,
                new FileWriter(
                        Paths.get("docs/paper3_dts/mark2/exploratory/calibration/histograms/",
                                "deriso_hillclimber2.yaml").toFile()
                ));


    }

    public void modifyScenarioFile(double[] x, DerisoCaliforniaScenario derisoScenario) {
        //change inputs
        String dover = "0:"+String.valueOf(startingDoverCatchability * (1d+x[0]*multiplier));
        String longspine = "1:"+String.valueOf(startingLongspineCatchability * (1d+x[1]*multiplier));
        String sablefish = "2:"+String.valueOf(startingSablefishCatchability * (1d+x[2]*multiplier));
        String shortspine ="3:"+ String.valueOf(startingShortspineCatchability * (1d+x[3]*multiplier));
        String yelloweye = "4:"+String.valueOf(startingYelloweyeCatchability * (1d+x[4]*multiplier));


        ((RandomTrawlStringFactory) ((GarbageGearFactory) derisoScenario.getGear()).getDelegate()).setCatchabilityMap(
                dover+","+longspine+","+sablefish+","+shortspine+","+yelloweye
        );

        FixedDoubleParameter hold = (FixedDoubleParameter) derisoScenario.getHoldSizePerBoat();
        hold.setFixedValue(hold.getFixedValue() * (1+x[5]*multiplier));
    }

    //computes abs(x-mu)/sigma
    public static double deviation(DataColumn data,
                                   double target,
                                   double standardDeviation,
                                   int yearsToSkip)
    {

        return Math.abs(
                FishStateUtilities.getAverage(data,yearsToSkip) - target
        )/standardDeviation;

    }

    // abs(100*data/quota-attainment)/standardDeviation
    public static double deviationAttainment(DataColumn data,
                                             double quota,
                                             double attainment,
                                             double standardDeviation,
                                             int yearsToSkip)
    {

        return Math.abs(
                100*FishStateUtilities.getAverage(data,yearsToSkip)/quota - attainment
        )/standardDeviation;

    }

    @Override
    public int getProblemDimension() {
        //5 catchabilities, 1 hold size
        return 6;
    }

    public String getScenarioFile() {
        return scenarioFile;
    }

    public void setScenarioFile(String scenarioFile) {
        this.scenarioFile = scenarioFile;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public String getSummaryDirectory() {
        return summaryDirectory;
    }

    public void setSummaryDirectory(String summaryDirectory) {
        this.summaryDirectory = summaryDirectory;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public int getYearsToRun() {
        return yearsToRun;
    }

    public void setYearsToRun(int yearsToRun) {
        this.yearsToRun = yearsToRun;
    }

    public int getYearsToIgnore() {
        return yearsToIgnore;
    }

    public void setYearsToIgnore(int yearsToIgnore) {
        this.yearsToIgnore = yearsToIgnore;
    }

    public double getStartingDoverCatchability() {
        return startingDoverCatchability;
    }

    public void setStartingDoverCatchability(double startingDoverCatchability) {
        this.startingDoverCatchability = startingDoverCatchability;
    }

    public double getStartingLongspineCatchability() {
        return startingLongspineCatchability;
    }

    public void setStartingLongspineCatchability(double startingLongspineCatchability) {
        this.startingLongspineCatchability = startingLongspineCatchability;
    }

    public double getStartingSablefishCatchability() {
        return startingSablefishCatchability;
    }

    public void setStartingSablefishCatchability(double startingSablefishCatchability) {
        this.startingSablefishCatchability = startingSablefishCatchability;
    }

    public double getStartingShortspineCatchability() {
        return startingShortspineCatchability;
    }

    public void setStartingShortspineCatchability(double startingShortspineCatchability) {
        this.startingShortspineCatchability = startingShortspineCatchability;
    }

    public double getStartingYelloweyeCatchability() {
        return startingYelloweyeCatchability;
    }

    public void setStartingYelloweyeCatchability(double startingYelloweyeCatchability) {
        this.startingYelloweyeCatchability = startingYelloweyeCatchability;
    }

    public int getRunsPerSetting() {
        return runsPerSetting;
    }

    public void setRunsPerSetting(int runsPerSetting) {
        this.runsPerSetting = runsPerSetting;
    }
}
