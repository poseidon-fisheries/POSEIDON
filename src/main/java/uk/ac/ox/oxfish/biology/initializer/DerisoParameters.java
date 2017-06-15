package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by carrknight on 6/14/17.
 */
public class DerisoParameters {


    private List<Double> empiricalYearlyBiomasses;

    private List<Double> historicalYearlySurvival;

    private double rho;

    private double naturalSurvivalRate;

    private double recruitmentSteepness;

    private int recruitmentLag;

    private double weightAtRecruitment;

    private double  weightAtRecruitmentMinus1;

    private double virginBiomass;

    private double lastRecruits;


    public DerisoParameters(
            List<Double> empiricalYearlyBiomasses, List<Double> historicalYearlySurvival, double rho,
            double naturalSurvivalRate, double recruitmentSteepness, int recruitmentLag, double weightAtRecruitment,
            double weightAtRecruitmentMinus1,
            double virginBiomass) {
        this.empiricalYearlyBiomasses = empiricalYearlyBiomasses;
        this.historicalYearlySurvival = historicalYearlySurvival;
        this.rho = rho;
        this.naturalSurvivalRate = naturalSurvivalRate;
        this.recruitmentSteepness = recruitmentSteepness;
        this.recruitmentLag = recruitmentLag;
        this.weightAtRecruitment = weightAtRecruitment;
        this.weightAtRecruitmentMinus1 = weightAtRecruitmentMinus1;
        this.virginBiomass = virginBiomass;

        double virginRecruits =
                virginBiomass * (1d-(1+rho)*naturalSurvivalRate + rho * naturalSurvivalRate * naturalSurvivalRate)
                        /
                        (weightAtRecruitment - rho * naturalSurvivalRate * weightAtRecruitmentMinus1);

        double alpha = (1d-recruitmentSteepness)/(4d*recruitmentSteepness*virginRecruits);
        double beta = (5*recruitmentSteepness-1d)/(4d*recruitmentSteepness*virginRecruits);

        //deriso schnute formula
        Double spawners = empiricalYearlyBiomasses.get(empiricalYearlyBiomasses.size()-recruitmentLag);
        lastRecruits =  spawners /virginBiomass/ (alpha+beta* spawners /virginBiomass);
    }

    /**
     * Getter for property 'empiricalYearlyBiomasses'.
     *
     * @return Value for property 'empiricalYearlyBiomasses'.
     */
    public List<Double> getEmpiricalYearlyBiomasses() {
        return empiricalYearlyBiomasses;
    }

    /**
     * Getter for property 'historicalYearlySurvival'.
     *
     * @return Value for property 'historicalYearlySurvival'.
     */
    public List<Double> getHistoricalYearlySurvival() {
        return historicalYearlySurvival;
    }

    /**
     * Getter for property 'rho'.
     *
     * @return Value for property 'rho'.
     */
    public double getRho() {
        return rho;
    }

    /**
     * Getter for property 'naturalSurvivalRate'.
     *
     * @return Value for property 'naturalSurvivalRate'.
     */
    public double getNaturalSurvivalRate() {
        return naturalSurvivalRate;
    }

    /**
     * Getter for property 'recruitmentSteepness'.
     *
     * @return Value for property 'recruitmentSteepness'.
     */
    public double getRecruitmentSteepness() {
        return recruitmentSteepness;
    }

    /**
     * Getter for property 'recruitmentLag'.
     *
     * @return Value for property 'recruitmentLag'.
     */
    public int getRecruitmentLag() {
        return recruitmentLag;
    }

    /**
     * Getter for property 'weightAtRecruitment'.
     *
     * @return Value for property 'weightAtRecruitment'.
     */
    public double getWeightAtRecruitment() {
        return weightAtRecruitment;
    }

    /**
     * Getter for property 'weightAtRecruitmentMinus1'.
     *
     * @return Value for property 'weightAtRecruitmentMinus1'.
     */
    public double getWeightAtRecruitmentMinus1() {
        return weightAtRecruitmentMinus1;
    }

    /**
     * Getter for property 'virginBiomass'.
     *
     * @return Value for property 'virginBiomass'.
     */
    public double getVirginBiomass() {
        return virginBiomass;
    }

    /**
     * Getter for property 'lastRecruits'.
     *
     * @return Value for property 'lastRecruits'.
     */
    public double getLastRecruits() {
        return lastRecruits;
    }

    //writes deriso parameters down
    public static void main(String[] args) throws IOException {

        FishYAML yaml = new FishYAML();
        DerisoParameters yellowEye =
                new DerisoParameters(
                        Lists.newArrayList(
                                5884.75390821775d* 1000,
                                5684.81400583123d* 1000,
                                5388.82436349019d* 1000,
                                5003.39439583719* 1000,
                                4755.7256703365* 1000,
                                4568.16526592639* 1000,
                                4333.32840621945* 1000,
                                4213.46491242035* 1000,
                                4065.55744324838* 1000,
                                3892.9123125654* 1000,
                                3655.63133792404* 1000,
                                3531.3892303894* 1000,
                                3278.22685605126* 1000,
                                3021.32461787125* 1000,
                                2820.02768880825* 1000,
                                2716.72810126806* 1000,
                                2582.87302878858* 1000,
                                2485.78587079967* 1000,
                                2361.38936282188* 1000,
                                2376.07942409806* 1000,
                                2341.24361654434* 1000,
                                2423.36370656817* 1000

                        ),
                        Lists.newArrayList(
                                0.938356073678119,
                                0.932931166788219
                        ),
                        1.03d,
                        0.95504d,
                        0.44056,
                        14,
                        1.11909797520236,
                        1.01603952895487,
                        8883*1000d);
        yaml.dump(
                yellowEye,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Yelloweye Rockfish","deriso.yaml")
                        .toFile()
                )
        );


        DerisoParameters sablefish =
                new DerisoParameters(
                        Lists.newArrayList(
                                338562.722077059 * 1000,
                                338705.324234203 * 1000,
                                342472.859205429* 1000,
                                343751.711943503* 1000,
                                345160.671539353* 1000
                        ),
                        Lists.newArrayList(0.904787532616812,0.906397451712637),
                        0.92267402483245,
                        0.923116346386636,
                        0.6,
                        3,
                        1.03312585773941,
                        0.634560212266768,
                        527154d*1000
                );
        yaml.dump(
                sablefish,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Sablefish","deriso.yaml")
                                .toFile()
                )
        );


        DerisoParameters longspineThornyheads =
                new DerisoParameters(
                        Lists.newArrayList(
                                //from spawners (multiplied by 2 since that's only female, that's the only mature biomass data I have)
                                29028*2d*1000,
                                26944d*2d*1000d,
                                24887*2d*1000,
                                23302*2d*1000,
                                21285*2d*1000,
                                19673*2d*1000,
                                18465*2d*1000,
                                18184*2d*1000,
                                18189*2d*1000,
                                18484*2d*1000,
                                19064*2d*1000,
                                19378*2d*1000,
                                19958*2d*1000,
                                21060*2d*1000,
                                22244*2d*1000,
                                23440*2d*1000,
                                24674*2d*1000,
                                25705*2d*1000,
                                26771*2d*1000,
                                27689*2d*1000
                        ),
                        Lists.newArrayList(
                                (26771d-1588-26771*(1-0.895834135296528))/26771, //natural mortality + fishing mortality in 2010
                                (27689-972-27689*(1d-0.895834135296528))/27689 //natural mortality + fishing mortality in 2011

                        ),
                        1.05569340594933d,
                        0.895834135296528d,
                        0.6,
                        16,
                        0.16707731172202,
                        0.154309182775882,
                        39134d*2d*1000
                );

        yaml.dump(
                longspineThornyheads,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Longspine Thornyhead","deriso.yaml")
                                .toFile()
                )
        );



        yaml.dump(
                longspineThornyheads,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Dover Sole","deriso.yaml")
                                .toFile()
                )
        );
        yaml.dump(
                longspineThornyheads,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Shortspine Thornyhead","deriso.yaml")
                                .toFile()
                )
        );

    }


    /**
     * Setter for property 'empiricalYearlyBiomasses'.
     *
     * @param empiricalYearlyBiomasses Value to set for property 'empiricalYearlyBiomasses'.
     */
    public void setEmpiricalYearlyBiomasses(List<Double> empiricalYearlyBiomasses) {
        this.empiricalYearlyBiomasses = empiricalYearlyBiomasses;
    }

    /**
     * Setter for property 'historicalYearlySurvival'.
     *
     * @param historicalYearlySurvival Value to set for property 'historicalYearlySurvival'.
     */
    public void setHistoricalYearlySurvival(List<Double> historicalYearlySurvival) {
        this.historicalYearlySurvival = historicalYearlySurvival;
    }

    /**
     * Setter for property 'rho'.
     *
     * @param rho Value to set for property 'rho'.
     */
    public void setRho(double rho) {
        this.rho = rho;
    }

    /**
     * Setter for property 'naturalSurvivalRate'.
     *
     * @param naturalSurvivalRate Value to set for property 'naturalSurvivalRate'.
     */
    public void setNaturalSurvivalRate(double naturalSurvivalRate) {
        this.naturalSurvivalRate = naturalSurvivalRate;
    }

    /**
     * Setter for property 'recruitmentSteepness'.
     *
     * @param recruitmentSteepness Value to set for property 'recruitmentSteepness'.
     */
    public void setRecruitmentSteepness(double recruitmentSteepness) {
        this.recruitmentSteepness = recruitmentSteepness;
    }

    /**
     * Setter for property 'recruitmentLag'.
     *
     * @param recruitmentLag Value to set for property 'recruitmentLag'.
     */
    public void setRecruitmentLag(int recruitmentLag) {
        this.recruitmentLag = recruitmentLag;
    }

    /**
     * Setter for property 'weightAtRecruitment'.
     *
     * @param weightAtRecruitment Value to set for property 'weightAtRecruitment'.
     */
    public void setWeightAtRecruitment(double weightAtRecruitment) {
        this.weightAtRecruitment = weightAtRecruitment;
    }

    /**
     * Setter for property 'weightAtRecruitmentMinus1'.
     *
     * @param weightAtRecruitmentMinus1 Value to set for property 'weightAtRecruitmentMinus1'.
     */
    public void setWeightAtRecruitmentMinus1(double weightAtRecruitmentMinus1) {
        this.weightAtRecruitmentMinus1 = weightAtRecruitmentMinus1;
    }

    /**
     * Setter for property 'virginBiomass'.
     *
     * @param virginBiomass Value to set for property 'virginBiomass'.
     */
    public void setVirginBiomass(double virginBiomass) {
        this.virginBiomass = virginBiomass;
    }

    /**
     * Setter for property 'lastRecruits'.
     *
     * @param lastRecruits Value to set for property 'lastRecruits'.
     */
    public void setLastRecruits(double lastRecruits) {
        this.lastRecruits = lastRecruits;
    }
}
