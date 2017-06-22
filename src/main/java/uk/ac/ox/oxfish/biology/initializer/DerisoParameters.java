package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
        updateLastRecruits();
    }

    public DerisoParameters() {
    }

    public void updateLastRecruits(){
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


        ArrayList<Double> doverSoleBiomassTo2004 = Lists.newArrayList(
                454464.737022236,
                454455.142355372,
                454436.377120975,
                454408.876243331,
                454373.078957402,
                454329.423511648,
                454282.372675649,
                454145.340678537,
                453984.473276064,
                453822.837315264,
                453694.58541101,
                453488.861051895,
                453125.841149793,
                452719.035543256,
                452142.008495977,
                451525.850519319,
                450950.637539372,
                450253.298381689,
                449609.550822611,
                448878.545424562,
                448255.185338069,
                447793.86961712,
                447397.543437443,
                447077.960004407,
                446739.86536601,
                446402.748504238,
                446134.091812606,
                445793.126972236,
                445610.893536505,
                444968.077986714,
                444410.687519543,
                443918.803878762,
                443163.181321141,
                440423.069546259,
                439826.799006746,
                438702.980467699,
                437031.044365195,
                435949.483333867,
                432472.802541433,
                428903.709179953,
                423310.217443121,
                417535.860537664,
                411113.001350118,
                408223.395679606,
                404755.29283876,
                401929.684310981,
                399119.011449303,
                396742.302766497,
                394817.911244725,
                392392.719261325,
                388753.974890726,
                386636.617058695,
                383857.942095367,
                380596.158596699,
                378017.251547112,
                375861.695162321,
                373998.798854156,
                373729.288471648,
                372383.302890545,
                368519.905311815,
                363878.659273728,
                359735.901233916,
                352413.228656262,
                345944.188758797,
                340779.619773305,
                334633.081784908,
                327618.182576658,
                321754.562233162,
                315303.921831028,
                305427.62577773,
                299718.716939682,
                292007.262213911,
                280361.944707722,
                270124.80803812,
                261117.775921805,
                251249.614537102,
                244921.819809234,
                237880.689724215,
                231466.172631371,
                224741.741260429,
                221291.34226459,
                215532.635261772,
                212149.129362457,
                210546.998093771,
                213865.500684878,
                215908.178258886,
                216290.842875025,
                218632.180749449,
                222943.697476257,
                225965.127696632,
                229202.642755269,
                234113.947619391,
                239438.493035321,
                243521.0191812);
        //that's in metric tonnes, we want them in kg
        for(int i=0; i<doverSoleBiomassTo2004.size(); i++)
            doverSoleBiomassTo2004.set(i,doverSoleBiomassTo2004.get(i)*1000d);

        DerisoParameters sole = new DerisoParameters(
                (List<Double>) doverSoleBiomassTo2004,
                (List<Double>) Lists.newArrayList(
                        0.885791668526681,
        0.888626296064829

                ),
                0.965,
                .914,
                0.8,
                8,
                0.314,
                0.2674,
                454464.737022236d
        );



        yaml.dump(
                sole,
                new FileWriter(
                        Paths.get("inputs","simple_california",
                                  "biology","Dover Sole","deriso.yaml")
                                .toFile()
                )
        );
        //todo add numbers till 2011


        yaml.dump(
                sole,
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


    @Deprecated
    public void setLastRecruits(double lastRecruits) {
        this.lastRecruits = lastRecruits;
    }
}
