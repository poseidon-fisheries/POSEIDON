package uk.ac.ox.oxfish.maximization;

import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OneGearExampleMaximization extends SimpleProblemDouble {



    private String scenarioFile =
            Paths.get("coe_new.yaml").toString();


    private String summaryFile =
            Paths.get(".").toString();

    private String landingData =
            Paths.get("landings_10.csv").toString();

    private long seed = 0;


    // an input of +10 means this is the catchability
    private double maxCatchability =  0.002;

    //an input of -10 means this catchability
    private double minCatchability =  0.00001;

    //
    // target: −3.768844221

    private int yearsToRun = 9;

    private int yearsToIgnore = 0;


    private int runsPerSetting = 1;
    private String speciesName = "Peter Snapper";

    @Override
    public double[] evaluate(double[] x) {
        FishYAML yaml = new FishYAML();
        Path scenarioPath = Paths.get(scenarioFile);

        try {
            double error = 0;
            for (int i = 0; i < runsPerSetting; i++) {
                FlexibleScenario scenario = yaml.loadAs(
                        new FileReader(scenarioPath.toFile()),
                        FlexibleScenario.class);

                double catchability = ((x[0]+10)/20)*(maxCatchability-minCatchability);

                for (FisherDefinition definition : scenario.getFisherDefinitions()) {
                    ((RandomCatchabilityTrawlFactory) definition.getGear()).setMeanCatchabilityFirstSpecies(
                            new FixedDoubleParameter(catchability)
                    );
                }


                FishState model = new FishState(System.currentTimeMillis());
                model.setScenario(scenario);
                model.start();
                System.out.println("starting run");
                while (model.getYear() < yearsToRun) {
                    model.schedule.step(model);
                }
                model.schedule.step(model);

                error+=FishStateUtilities.timeSeriesDistance(
                        model.getYearlyDataSet().getColumn(
                                speciesName + " Landings"
                        ),
                        Paths.get(landingData), 1
                );

            }

            return new double[]{
                    error/(double)runsPerSetting};
        }
        catch (IOException exception){
            throw new RuntimeException("failed to read or deal with files!");
        }
    }


    @Override
    public int getProblemDimension() {
        return 1;
    }


    public String getScenarioFile() {
        return scenarioFile;
    }

    public void setScenarioFile(String scenarioFile) {
        this.scenarioFile = scenarioFile;
    }

    public String getSummaryFile() {
        return summaryFile;
    }

    public void setSummaryFile(String summaryFile) {
        this.summaryFile = summaryFile;
    }

    public String getLandingData() {
        return landingData;
    }

    public void setLandingData(String landingData) {
        this.landingData = landingData;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public double getMaxCatchability() {
        return maxCatchability;
    }

    public void setMaxCatchability(double maxCatchability) {
        this.maxCatchability = maxCatchability;
    }

    public double getMinCatchability() {
        return minCatchability;
    }

    public void setMinCatchability(double minCatchability) {
        this.minCatchability = minCatchability;
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

    public int getRunsPerSetting() {
        return runsPerSetting;
    }

    public void setRunsPerSetting(int runsPerSetting) {
        this.runsPerSetting = runsPerSetting;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }
}
