/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.maximization;

import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.biology.complicated.factory.HockeyStickRecruitmentFactory;
import uk.ac.ox.oxfish.biology.complicated.factory.RecruitmentBySpawningJackKnifeMaturity;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.MultipleIndependentSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.maximization.generic.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GenericOptimization extends SimpleProblemDouble implements Serializable {

    public static final double MINIMUM_CATCHABILITY = 1.0E-5;

    public static final double MAXIMUM_CATCHABILITY = 1.0E-3;

    private static final Path DEFAULT_PATH = Paths.get("docs",
            "indonesia_hub",
            "runs", "712", "slice3", "calibration");

    private String scenarioFile =   DEFAULT_PATH.resolve("pessimistic_recruits_spinup.yaml").toString();


    private double translateNANto = 10000000;

    private boolean maximization = false;

    //todo have a summary outputting a CSV: parameter1,parameter2,...,parameterN,target1,...,targetN for logging purposes and also maybe IITP

    /**
     * list of all parameters that can be changed
     */
    private List<OptimizationParameter> parameters = new LinkedList<>();

    {

        parameters.add(new SimpleOptimizationParameter(
                "gear.delegate.gears~Dover Sole.averageCatchability",
                MINIMUM_CATCHABILITY,
                MAXIMUM_CATCHABILITY
        ));

        parameters.add(new SimpleOptimizationParameter(
                "gear.delegate.gears~Longspine Thornyhead.averageCatchability",
                MINIMUM_CATCHABILITY,
                MAXIMUM_CATCHABILITY
        ));

        parameters.add(new SimpleOptimizationParameter(
                "gear.delegate.gears~Sablefish.averageCatchability",
                MINIMUM_CATCHABILITY,
                MAXIMUM_CATCHABILITY
        ));

        parameters.add(new SimpleOptimizationParameter(
                "gear.delegate.gears~Shortspine Thornyhead.averageCatchability",
                MINIMUM_CATCHABILITY,
                MAXIMUM_CATCHABILITY
        ));

        parameters.add(new SimpleOptimizationParameter(
                "gear.delegate.gears~Yelloweye Rockfish.averageCatchability",
                MINIMUM_CATCHABILITY,
                MAXIMUM_CATCHABILITY
        ));

        parameters.add(new SimpleOptimizationParameter(
                "gear.proportionSimulatedToGarbage.averageCatchability",
                MINIMUM_CATCHABILITY,
                MAXIMUM_CATCHABILITY
        ));


        parameters.add(new SimpleOptimizationParameter(
                "holdSizePerBoat",
                2500,
                15000
        ));
//
//
//        for(int populations=0; populations<3; populations++) {
//
//            parameters.add(new SimpleOptimizationParameter(
//                    "fisherDefinitions$"+populations+".gear.delegate.delegate.gears~Pristipomoides multidens.averageCatchability",
//                    MINIMUM_CATCHABILITY,
//                    MAXIMUM_CATCHABILITY
//            ));
//
//            parameters.add(new SimpleOptimizationParameter(
//                    "fisherDefinitions$"+populations+".gear.delegate.delegate.gears~Lutjanus malabaricus.averageCatchability",
//                    MINIMUM_CATCHABILITY,
//                    MAXIMUM_CATCHABILITY
//            ));
//            parameters.add(new SimpleOptimizationParameter(
//                    "fisherDefinitions$"+populations+".gear.delegate.delegate.gears~Epinephelus areolatus.averageCatchability",
//                    MINIMUM_CATCHABILITY,
//                    MAXIMUM_CATCHABILITY
//            ));
//            parameters.add(new SimpleOptimizationParameter(
//                    "fisherDefinitions$"+populations+".gear.delegate.delegate.gears~Lutjanus erythropterus.averageCatchability",
//                    MINIMUM_CATCHABILITY,
//                    MAXIMUM_CATCHABILITY
//            ));
//
//            //garbage collectors
//            parameters.add(new SimpleOptimizationParameter(
//                    "fisherDefinitions$"+populations+".gear.delegate.proportionSimulatedToGarbage",
//                    0,
//                    .80
//            ));
//        }

        /*
        for(int species=0; species<4; species++)
        {
            parameters.add(
                    new SimpleOptimizationParameter(
                            "biologyInitializer.factories$"+species+".grower.distributionalWeight",
                            .5,
                            10

                    )
            );
        }
        */

    }

    /**
     * map linking the name of the YearlyDataSet in the model with the path to file containing the real time series
     */
    private List<DataTarget> targets = new LinkedList<>();

    public static final boolean CUMULATIVE = false;

    {

        Map<String,Integer> populations = new HashMap<>();
        populations.put("Small",0);
        populations.put("Medium",1);
        populations.put("Big",2);

        for (Map.Entry<String, Integer> population : populations.entrySet()) {
            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(
                                    population.getKey() + "_LL021 Lutjanus malabaricus.csv").toString(),
                            "Lutjanus malabaricus Landings of population" + population.getValue(), true, -1d, 1,
                            CUMULATIVE));

            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(
                                    population.getKey() + "_LP012 Pristipomoides multidens.csv").toString(),
                            "Pristipomoides multidens Landings of population" + population.getValue(), true, -1d, 1,
                            CUMULATIVE));


            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(
                                    population.getKey() + "_SE002 Epinephelus areolatus.csv").toString(),
                            "Epinephelus areolatus Landings of population" + population.getValue(), true, -1d, 1,
                            CUMULATIVE));
            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(
                                    population.getKey() + "_LL017 Lutjanus erythropterus.csv").toString(),
                            "Lutjanus erythropterus Landings of population" + population.getValue(), true, -1d, 1,
                            CUMULATIVE));

            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey() + "_Others.csv").toString(),
                            "Others Landings of population" + population.getValue(), true, -1d, 1,
                            CUMULATIVE));

            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey() + "_total.csv").toString(),
                            "Total Landings of population" + population.getValue(), true, -1d, 1,
                            CUMULATIVE));

        }


            //landings per species

            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve("all_landings_LL021 Lutjanus malabaricus.csv").toString(),
                            "Lutjanus malabaricus Landings", true, -1d, 1,
                            CUMULATIVE));

            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve("all_landings"+"_LP012 Pristipomoides multidens.csv").toString(),
                            "Pristipomoides multidens Landings", true, -1d, 1,
                            CUMULATIVE));


            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve("all_landings"+"_SE002 Epinephelus areolatus.csv").toString(),
                            "Epinephelus areolatus Landings", true, -1d, 1,
                            CUMULATIVE));
            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve("all_landings"+"_LL017 Lutjanus erythropterus.csv").toString(),
                            "Lutjanus erythropterus Landings", true, -1d, 1,
                            CUMULATIVE));






    }










    private int runsPerSetting = 1;

    private int simulatedYears = 4;

    /**
     * create smaller optimization problem trying to climb within a small range of previously found optimal parameters
     * this assumes however all parameters are simple
     */
    public static void buildLocalCalibrationProblem(
            final Path optimizationFile, double[] originalParameters,
            String newCalibrationName,
            double range) throws IOException {
        FishYAML yaml = new FishYAML();
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);
        for (int i = 0; i < optimization.getParameters().size(); i++) {
            final SimpleOptimizationParameter parameter = ((SimpleOptimizationParameter) optimization.getParameters().get(i));
            double optimalValue = parameter.computeNumericValue(originalParameters[i]);
            parameter.setMaximum(optimalValue* (1d+range));
            parameter.setMinimum(optimalValue* (1d-range));
            if(parameter.getMaximum()==parameter.getMinimum())
            {
                assert parameter.getMinimum()==0;
                parameter.setMaximum(0.001);
            }

        }
        yaml.dump(optimization, new FileWriter(optimizationFile.getParent().resolve(newCalibrationName).toFile()));


    }

    /**
     * Return the problem dimension.
     *
     * @return the problem dimension
     */
    @Override
    public int getProblemDimension() {
        int sum = 0;
        for(OptimizationParameter parameter : parameters)
            sum+=parameter.size();
        return sum;
    }

    /**
     * Evaluate a double vector representing a possible problem solution as
     * part of an individual in the EvA framework. This makes up the
     * target function to be evaluated.
     *
     * @param x a double vector to be evaluated
     * @return the fitness vector assigned to x as to the target function
     */
    @Override
    public double[] evaluate(double[] x) {

        try {
            double error = 0;

            for (int i = 0; i < runsPerSetting; i++) {
                //read in and modify parameters
                Scenario scenario = buildScenario(x, Paths.get(scenarioFile).toFile(), parameters);

                //run the model
                error += computeErrorGivenScenario(scenario, simulatedYears);

            }

            double finalError = error / (double) runsPerSetting;
            if(maximization)
                finalError = finalError * (-1);
            if(!Double.isFinite(finalError)) {
                System.out.println("was NAN!");
                finalError = translateNANto;
            }

            System.out.println(Arrays.toString(x) + " ---> " + finalError);
            return new double[]{finalError};

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public double computeErrorGivenScenario(Scenario scenario,
                                            int simulatedYears) {
        FishState model = new FishState(System.currentTimeMillis());

        double error = 0;
        model.setScenario(scenario);
        model.start();
        System.out.println("starting run");
        while (model.getYear() < simulatedYears) {
            model.schedule.step(model);
        }
        model.schedule.step(model);

        //collect error
        for (DataTarget target : targets) {
            error+=target.computeError(model);
        }
        return error;
    }

    public static Scenario buildScenario(double[] x, File scenarioFile,
                                         List<OptimizationParameter> parameterList)
            throws FileNotFoundException {
        FishYAML yaml = new FishYAML();
        Scenario scenario = yaml.loadAs(new 
                FileReader(scenarioFile),Scenario.class);
        int parameter=0;
       for (OptimizationParameter optimizationParameter : parameterList)
        {
            optimizationParameter.parametrize(scenario,
                    Arrays.copyOfRange(x,parameter,
                            parameter+optimizationParameter.size()));
            parameter+=optimizationParameter.size();
        }

        return scenario;
    }




    public static void main(String[] args) throws IOException {

        FishYAML yaml = new FishYAML();
        Path optimizationFile =
        Paths.get("docs", "indonesia_hub", "runs", "712","slice2019","calibration",
                              "optimizationproblem_historical20_vpar4years_cumulative.yaml");
        final String scenarioName = "historical20_vpa4years_cumulative_2";

        GenericOptimization optimization =
                yaml.loadAs(new FileReader(optimizationFile.toFile()),GenericOptimization.class);
        System.out.println(optimization.scenarioFile);
        Scenario scenario = GenericOptimization.buildScenario(new double[]{
                -8.207,-4.757,-9.443,-1.924,-4.246,-4.158,-9.681,-9.751, 2.864,-0.253, 4.651, 8.520,-6.758,-7.782, 4.285,-5.825,-0.477,-9.664, 1.917,-1.249                   }, Paths.get(optimization.scenarioFile).toFile(), optimization.parameters);
        Path outputFile = optimizationFile.getParent().resolve("slicesweep").resolve(scenarioName + "_8h.yaml");
        yaml.dump(scenario,new FileWriter(outputFile.toFile()));

        //  FishYAML yaml = new FishYAML();
        FlexibleScenario modified = (FlexibleScenario) scenario;
        MultipleIndependentSpeciesAbundanceFactory bio = (MultipleIndependentSpeciesAbundanceFactory) modified.getBiologyInitializer();
        for (AlgorithmFactory<? extends SingleSpeciesAbundanceInitializer> factory : bio.getFactories()) {
            ((RecruitmentBySpawningJackKnifeMaturity) ((SingleSpeciesAbundanceFactory) factory).getRecruitment()).
                    setSteepness(new FixedDoubleParameter(.7));
        }
        outputFile = optimizationFile.getParent().resolve("slicesweep").resolve(scenarioName + "_7h.yaml");
        yaml.dump(modified,new FileWriter(outputFile.toFile()));


        //  FishYAML yaml = new FishYAML();
        for (AlgorithmFactory<? extends SingleSpeciesAbundanceInitializer> factory : bio.getFactories()) {
            ((RecruitmentBySpawningJackKnifeMaturity) ((SingleSpeciesAbundanceFactory) factory).getRecruitment()).
                    setSteepness(new FixedDoubleParameter(.6));
        }
        outputFile = optimizationFile.getParent().resolve("slicesweep").resolve(scenarioName + "_6h.yaml");
        yaml.dump(modified,new FileWriter(outputFile.toFile()));

        for (AlgorithmFactory<? extends SingleSpeciesAbundanceInitializer> factory : bio.getFactories()) {
            final RecruitmentBySpawningJackKnifeMaturity oldRecruitment = (RecruitmentBySpawningJackKnifeMaturity) ((SingleSpeciesAbundanceFactory) factory).getRecruitment();


            HockeyStickRecruitmentFactory newRecruitment = new HockeyStickRecruitmentFactory();
            newRecruitment.setHinge(new FixedDoubleParameter(.2));
            newRecruitment.setLengthAtMaturity(new FixedDoubleParameter(oldRecruitment.getLengthAtMaturity()));
            final double r0 = ((FixedDoubleParameter) oldRecruitment.getVirginRecruits()).getFixedValue();
            final double ssb0 = r0* ((FixedDoubleParameter) oldRecruitment.getCumulativePhi()).getFixedValue();
            newRecruitment.setVirginRecruits(new FixedDoubleParameter(r0));

            newRecruitment.setVirginSpawningBiomass(
                    new FixedDoubleParameter(
                            ssb0
                    )
            );

            ((SingleSpeciesAbundanceFactory) factory).setRecruitment(newRecruitment);
        }
        outputFile = optimizationFile.getParent().resolve("slicesweep").resolve(scenarioName + "_hs.yaml");
        yaml.dump(modified,new FileWriter(outputFile.toFile()));

    }


    public List<OptimizationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<OptimizationParameter> parameters) {
        this.parameters = parameters;
    }

    public List<DataTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<DataTarget> targets) {
        this.targets = targets;
    }

    public String getScenarioFile() {
        return scenarioFile;
    }

    public void setScenarioFile(String scenarioFile) {
        this.scenarioFile = scenarioFile;
    }

    public int getRunsPerSetting() {
        return runsPerSetting;
    }

    public void setRunsPerSetting(int runsPerSetting) {
        this.runsPerSetting = runsPerSetting;
    }

    /**
     * Getter for property 'simulatedYears'.
     *
     * @return Value for property 'simulatedYears'.
     */
    public int getSimulatedYears() {
        return simulatedYears;
    }

    /**
     * Setter for property 'simulatedYears'.
     *
     * @param simulatedYears Value to set for property 'simulatedYears'.
     */
    public void setSimulatedYears(int simulatedYears) {
        this.simulatedYears = simulatedYears;
    }


    /**
     * Getter for property 'maximization'.
     *
     * @return Value for property 'maximization'.
     */
    public boolean isMaximization() {
        return maximization;
    }

    /**
     * Setter for property 'maximization'.
     *
     * @param maximization Value to set for property 'maximization'.
     */
    public void setMaximization(boolean maximization) {
        this.maximization = maximization;
    }

    /**
     * Getter for property 'translateNANto'.
     *
     * @return Value for property 'translateNANto'.
     */
    public double getTranslateNANto() {
        return translateNANto;
    }

    /**
     * Setter for property 'translateNANto'.
     *
     * @param translateNANto Value to set for property 'translateNANto'.
     */
    public void setTranslateNANto(double translateNANto) {
        this.translateNANto = translateNANto;
    }
}
