package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        Double spawners = empiricalYearlyBiomasses.get(empiricalYearlyBiomasses.size()-recruitmentLag-1);
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
                                8883000d,8879174d,8873953d,8868155d,8864445d,8860530d,8856710d,8853086d,8849360d,
                                8845047d,8838688d,8831406d,8823224d,8813870d,8800921d,8787570d,8776476d,8765928d,
                                8757796d,8747619d,8735145d,8718657d,8702657d,8687139d,8676014d,8657811d,8635120d,
                                8606655d,8534448d,8463244d,8344813d,8248938d,8216305d,8177412d,8154640d,8129782d,
                                8097658d,8071355d,8053628d,8028939d,7995605d,7953450d,7903732d,7849827d,7802497d,
                                7760358d,7727444d,7686846d,7657372d,7639984d,7543438d,7510307d,7470365d,7428501d,
                                7320928d,7252466d,7156663d,7031742d,6914782d,6781853d,6661574d,6517739d,6371633d,
                                6108281d,5850281d,5652339d,5359377d,4978877d,4735490d,4550998d,4318958d,4199800d,
                                4051556d,3878241d,3641386d,3516316d,3264065d,3009226d,2809955d,2706916d,2572423d,
                                2472791d,2345994d,2355185d,2313621d,2385708d
//last is 2011
                        ),
                        Lists.newArrayList(
                                0.938156881691037,
                                0.932582202042579
                        ),
                        0.981194230283006d,
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
                                  "biology","Yelloweye Rockfish","deriso_2001.yaml")
                                .toFile()
                )
        );
        //add years 2002 to 2011
        yellowEye.getEmpiricalYearlyBiomasses().addAll(
                Lists.newArrayList(2440588d,2535301d, 2626801d,
                                   2713269d, 2791586d, 2865974d,
                                   2925349d, 2989877d, 3052190d, 3109600d)
        );
        yellowEye.getHistoricalYearlySurvival().clear();
        yellowEye.getHistoricalYearlySurvival().addAll(
                Lists.newArrayList(0.9515583,
                                   0.9514729)
        );
        yellowEye.updateLastRecruits();
        yaml.dump(
                yellowEye,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Yelloweye Rockfish","deriso_2011.yaml")
                                .toFile()
                )
        );

        DerisoParameters sablefish =
                new DerisoParameters(
                        Lists.newArrayList(
                                527154000d,527105779d,527033675d,526937145d,526819309d,526679414d,526563028d,526467416d,526392560d,526337557d,526243592d,526110372d,525941820d,525738694d,525502749d,
                                525235725d,524940279d,523750897d,521807115d,519028892d,518183379d,517707156d,516809380d,516662376d,515626553d,514582427d,513397054d,512674141d,511423759d,510525199d,
                                509507192d,508278705d,508102873d,507775981d,507491398d,506569660d,505125433d,504494266d,503975133d,503628931d,502963312d,502927457d,502782061d,501560771d,499724770d,
                                497712445d,495422136d,493526237d,493495066d,492918991d,492646142d,492560761d,491204268d,491212648d,491585614d,491276018d,491098354d,489641827d,489181485d,489750949d,
                                489615770d,488813834d,489120929d,488538202d,488842424d,488890144d,489039154d,489251476d,487090459d,486562540d,483366313d,481078276d,479618827d,475399328d,472254781d,
                                466454189d,455081889d,433774184d,429505494d,421974007d,402971360d,399900476d,394650983d,381593314d,373460329d,366503129d,359845028d,354395185d,349711490d,347253956d,
                                345399509d,344891059d,343993698d,343317882d,344025143d,345403773d,346390724d,346833531d,347657176d,352224351d,354280712d,
                                356442219d
                        ),
                        Lists.newArrayList(0.906926410977892,0.90533206481522),
                        0.813181970802262,
                        0.92311,
                        0.6,
                        3,
                        1.03313,
                        0.63456,
                        527154d*1000
                );
        yaml.dump(
                sablefish,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Sablefish","deriso_2001.yaml")
                                .toFile()
                )
        );
        //2011
        sablefish.getEmpiricalYearlyBiomasses().addAll(
                Lists.newArrayList(359093535d,
                                   363693876d,
                                   366614309d,
                                   368991925d,
                                   370826873d,
                                   372606667d,
                                   375295416d,
                                   377220074d,
                                   377528382d,
                                   378249946d)
        );
        sablefish.getHistoricalYearlySurvival().clear();
        sablefish.getHistoricalYearlySurvival().addAll(
                Lists.newArrayList(0.9053192,
                                   0.9066929)
        );
        sablefish.updateLastRecruits();
        yaml.dump(
                sablefish,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Sablefish","deriso_2011.yaml")
                                .toFile()
                )
        );



        //longspine


        DerisoParameters longspineThornyheads =
                new DerisoParameters(
                        Lists.newArrayList(
                                103038000d,103025584.771664d,102997500.336291d,102979298.372349d,
                                102972452.834924d,102965948.20946d,102941624.053253d,102906023.177085d,
                                102870174.83624d,102799724.464293d,102722048.52885d,102663292.650363d,102586334.368527d,
                                102556007.421254d,102481393.603707d,102319622.072949d,102216986.556186d,101504168.41372d,
                                101468541.463655d,100790779.214925d,100641607.271138d,99991240.7974544d,99361283.1687786d,
                                98756437.1272555d,97392517.569876d,94525669.3252501d,91098236.7938047d,84242827.4620659d,
                                81000032.1357237d,76049213.3654202d,71096739.3621415d,67362333.8782557d,62995082.3649151d,
                                59274580.6947781d,56553909.4842188d,55993442.10361d,55921810.4872823d,56288602.3587431d//,

                        ),
                        Lists.newArrayList(
                                0.8721173,
                                0.8647872

                        ),
                        1.05569340594933d,
                        0.895834135296528d,
                        0.6,
                        16,
                        0.16707731172202,
                        0.154309182775882,
                        103038000
                );



        yaml.dump(
                longspineThornyheads,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Longspine Thornyhead","deriso_2001.yaml")
                                .toFile()
                )
        );
        longspineThornyheads.getEmpiricalYearlyBiomasses().addAll(
                Lists.newArrayList(57072598.3896481d,
                                   56792094.3517341d,
                                   56966962.450067d,
                                   58093119.984295d,
                                   59633108.6541825d,
                                   60447252.8924193d,
                                   61142013.4012949d,
                                   61704712.854162d,
                                   62150218.0834741d,
                                   62499241.7096534)
        );
        longspineThornyheads.getHistoricalYearlySurvival().clear();
        longspineThornyheads.getHistoricalYearlySurvival().addAll(
                Lists.newArrayList(0.8765318,
                                   0.8861736)
        );
        longspineThornyheads.updateLastRecruits();
        yaml.dump(
                longspineThornyheads,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Longspine Thornyhead","deriso_2011.yaml")
                                .toFile()
                )
        );




        //shortspine
        DerisoParameters shortspineThornyheads =
                new DerisoParameters(
                        Lists.newArrayList(
                                331900000d,331897991d,331895970d,331891929d,331886859d,331880752d,
                                331872596d,331863381d,331853097d,331841733d,331828273d,331813705d,331798016d,
                                331779185d,331760202d,331739050d,331716713d,331693176d,331667421d,331640432d,
                                331612191d,331582682d,331550885d,331517780d,331483351d,331446575d,331409438d,
                                331369916d,331328989d,331286639d,331241839d,331195569d,331147809d,331090497d,
                                331032613d,330974135d,330912027d,330854290d,330788871d,330712701d,330622690d,
                                330497640d,330357429d,330064285d,329653507d,329164517d,328917180d,328841189d,328656053d,
                                328451621d,328260894d,327910086d,327689657d,327579078d,327403070d,327224714d,326367971d,
                                326016804d,325650872d,325174420d,324615964d,324088431d,323772578d,323394186d,323115525d,
                                322584337d,321184561d,319737270d,317514816d,316784367d,315891028d,314832885d,312878709d,
                                309426043d,307695458d,305091588d,303769689d,302010782d,300478979d,298293310d,296652997d,
                                294372968d,291681947d,287992167d,283641341d,279016051d,275503397d,272316303d,268696372d,
                                262890326d,257689814d,253079617d,248647553d,243613297d,239002151d,235988455d,233275789d,
                                230749319d,228372784d,226400107d,224392307d
                        ),
                        Lists.newArrayList(
                                0.9468694,
                                0.9482032

                        ),
                        1.069d,
                        0.950753929d,
                        0.6,
                        12,
                        0.091218379,
                        0.074366689,
                        331900000
                );



        yaml.dump(
                shortspineThornyheads,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Shortspine Thornyhead","deriso_2001.yaml")
                                .toFile()
                )
        );
        shortspineThornyheads.getEmpiricalYearlyBiomasses().addAll(
                Lists.newArrayList(222673008d,220666342d,218577444d,216511889d,
                                   214521270d,212445888d,210071132d,207213527d,204209583d,201358815d)
        );
        shortspineThornyheads.getHistoricalYearlySurvival().clear();
        shortspineThornyheads.getHistoricalYearlySurvival().addAll(
                Lists.newArrayList(0.9468694,
                                   0.9482032)
        );
        shortspineThornyheads.updateLastRecruits();
        yaml.dump(
                shortspineThornyheads,
                new FileWriter(
                        Paths.get("inputs","california",
                                  "biology","Shortspine Thornyhead","deriso_2011.yaml")
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
