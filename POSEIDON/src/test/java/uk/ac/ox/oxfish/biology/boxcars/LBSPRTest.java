package uk.ac.ox.oxfish.biology.boxcars;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.GrowthBinByList;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyCounter;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class LBSPRTest {


    @Test
    public void probMatrix() {

        final LbSprEstimation.AgeToLength ageToLength = LbSprEstimation.buildAgeToLengthKey(
            new double[]{2.5, 7.5, 12.5, 17.5, 22.5, 27.5, 32.5, 37.5, 42.5, 47.5, 52.5, 57.5, 62.5, 67.5, 72.5, 77.5, 82.5, 87.5, 92.5, 97.5, 102.5},
            1.5,
            100,
            0.1,
            100
        );

        //computed the numbers in R for comparison

        Assertions.assertArrayEquals(ageToLength.getRelativeLengthAtAge(), new double[]{0, 0.0302346408917507, 0.0595551482736483, 0.0879891606440902, 0.1155634808614, 0.142304101409106,
            0.168236228897329, 0.193384307823387, 0.217772043613997, 0.241422424970816, 0.264357745540359, 0.286599624928744,
            0.308169029081063, 0.329086290044589, 0.349371125134443, 0.369042655519807, 0.388119424248178, 0.406619413724672,
            0.424560062662843, 0.441958282523004, 0.458830473453536, 0.475192539750227, 0.491059904848191, 0.506447525860519,
            0.521369907677362, 0.535841116638722, 0.549874793793866, 0.563484167759834, 0.576682067191179, 0.589480932872692,
            0.601892829446503, 0.613929456784619, 0.625602161017619, 0.636921945229899, 0.647899479831548, 0.65854511261664,
            0.668868878517409, 0.678880509063518, 0.688589441555324, 0.698004827959798, 0.707135543537476, 0.715990195208579,
            0.724577129666183, 0.732904441244101, 0.740979979546868, 0.748811356849042, 0.756405955270797, 0.763770933736554,
            0.770913234723223, 0.777839590804419, 0.784556530996812, 0.791070386914596, 0.797387298737886, 0.803513221000653,
            0.809453928203675, 0.815215020257771, 0.820801927762467, 0.826219917125062, 0.831474095524925, 0.836569415727686,
            0.841510680753889, 0.846302548406473, 0.85094953566138, 0.855456022925407, 0.859826258165323, 0.864064360912147,
            0.868174326144359, 0.872160028053698, 0.876025223697106, 0.879773556538259, 0.883408559882017, 0.886933660205036,
            0.890352180385682, 0.893667342836284, 0.896882272540695, 0.9, 0.903023464089175, 0.905955514827365, 0.908798916064409,
            0.91155634808614, 0.914230410140911, 0.916823622889733, 0.919338430782339, 0.9217772043614, 0.924142242497082,
            0.926435774554036, 0.928659962492874, 0.930816902908106, 0.932908629004459, 0.934937112513444, 0.936904265551981,
            0.938811942424818, 0.940661941372467, 0.942456006266284, 0.9441958282523, 0.945883047345354, 0.947519253975023,
            0.949105990484819, 0.950644752586052, 0.952136990767736, 0.953584111663872}, .0001);


        Assertions.assertArrayEquals(ageToLength.getAgeToLengthKey()[0],
            new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            .001);

        Assertions.assertArrayEquals(ageToLength.getAgeToLengthKey()[9],
            new double[]{0, 0, 0, 0, 0.676083996633745, 0.323916003366255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            .001);
        Assertions.assertArrayEquals(ageToLength.getAgeToLengthKey()[49],
            new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0380479211891531, 0.109432932410976, 0.208216346994095, 0.262078437645448, 0.218221452273814, 0.120202437543582, 0.0438004719429317, 0, 0},
            .001);
        Assertions.assertArrayEquals(ageToLength.getAgeToLengthKey()[98],
            new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0147408071584518, 0.0447332732716233, 0.102943763385379, 0.179650882000543, 0.237748954698158, 0.238598565526688, 0.181583753959157},
            .001);


    }


    @Test
    public void formulaTest() {

        final LbSprEstimation.TheoreticalSPR theoreticalSPR = LbSprEstimation.sprFormula(
            30,
            50,
            1.2,
            100,
            100,
            0.1,
            new double[]{2.5, 5, 7.5, 10, 12.5, 15, 17.5, 20, 22.5, 25, 27.5, 30, 32.5, 35, 37.5, 40, 42.5, 45, 47.5, 50, 52.5, 55, 57.5, 60, 62.5, 65, 67.5, 70, 72.5, 75, 77.5, 80, 82.5, 85, 87.5, 90, 92.5, 95, 97.5, 100, 102.5, 105, 107.5},
            1.5,
            new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            .01


        );
        System.out.println(theoreticalSPR.getSpr());
        Assertions.assertEquals(theoreticalSPR.getSpr(), 0.2692, .0001);
        System.out.println(Arrays.toString(theoreticalSPR.getCatchesAtLength()));
        Assertions.assertArrayEquals(theoreticalSPR.getCatchesAtLength(),
            new double[]{0.002728, 0.003731, 0.002341, 0.006383, 0.009351, 0.012567, 0.016806, 0.02228, 0.028641, 0.035723, 0.042553, 0.050446, 0.056415, 0.060525, 0.062533, 0.062506, 0.060939, 0.05794, 0.054172, 0.049845, 0.045279, 0.040631, 0.035691, 0.031429, 0.027384, 0.023597, 0.019933, 0.016807, 0.013852, 0.011375, 0.009111, 0.007172, 0.005542, 0.004195, 0.003106, 0.002242, 0.001576, 0.001058, 0.000687, 0.000429, 0.000258, 0.000145, 7.4e-05},
            .0001);


    }


    @Test
    public void lbsprOptTest() {


        double[] catchAtLengthObserved = new double[]{2.5, 5, 7.5, 10, 12.5, 15, 17.5, 20,
            22.5, 25, 27.5, 30, 32.5, 35, 37.5, 40, 42.5, 45,
            47.5, 50, 52.5, 55, 57.5, 60, 62.5, 65, 67.5, 70,
            72.5, 75, 77.5, 80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        double logSelectivityCmAt50PercentAsPercentageOfLinf = .3;
        double logSelectivitySlope = .1;
        double logRatioFishingToNaturalMortality = Math.log((1.5 / 1));
        int maximumAge = 100;
        double Linf = 100;
        double coefficientVariationLinf = .1;
        double[] binMids =
            new double[]{2.5, 5, 7.5, 10, 12.5, 15, 17.5, 20, 22.5, 25, 27.5, 30, 32.5, 35, 37.5, 40, 42.5, 45, 47.5, 50, 52.5, 55, 57.5, 60, 62.5, 65, 67.5, 70, 72.5, 75, 77.5, 80, 82.5, 85, 87.5, 90, 92.5, 95, 97.5, 100, 102.5, 105, 107.5};


        double mkRatio = 1.5;
        double[] maturityPerBin = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        double bVariableLengthToWeightConversion = .01;


        final double likelihood = LbSprEstimation.lbsprDistance(
            catchAtLengthObserved,
            logSelectivityCmAt50PercentAsPercentageOfLinf,
            logSelectivitySlope,
            logRatioFishingToNaturalMortality,
            maximumAge,
            Linf,
            coefficientVariationLinf,
            binMids,
            mkRatio,
            maturityPerBin,
            bVariableLengthToWeightConversion
        );

        System.out.println(likelihood);
        Assertions.assertEquals(861.6453, likelihood, .0001);


        //R script:
//        catchAtLengthObserved =
//                c(2.5, 5, 7.5, 10, 12.5, 15, 17.5, 20,
//                        22.5, 25, 27.5, 30, 32.5, 35, 37.5, 40, 42.5, 45,
//                        47.5, 50, 52.5, 55, 57.5, 60, 62.5, 65, 67.5, 70,
//                        72.5, 75, 77.5, 80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
//        logSelectivityCmAt50PercentAsPercentageOfLinf = .3;
//        logSelectivitySlope = .1;
//        logRatioFishingToNaturalMortality = log((1.5/1));
//        nage = 101;
//        Linf = 100;
//        coefficientVariationLinf = .1;
//        binMids =   c(2.5, 5, 7.5, 10, 12.5, 15, 17.5, 20, 22.5, 25, 27.5, 30, 32.5, 35, 37.5, 40, 42.5, 45, 47.5, 50, 52.5, 55, 57.5, 60, 62.5, 65, 67.5, 70, 72.5, 75, 77.5, 80, 82.5, 85, 87.5, 90, 92.5, 95, 97.5, 100, 102.5, 105, 107.5)
//
//        mkRatio = 1.5;
//        maturityPerBin  =c(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
//        bVariableLengthToWeightConversion = .01;
//
//
//
//
//        nage <- 101
//        P <- 0.01
//        xs <- seq(0, to=1, length.out = nage)
//        rLens <- 1-P^(xs/MK)
//        EL <- rLens * Linf
//        SDL <- EL * CVLinf
//
//        Prob <- matrix(0, nrow=nage, ncol=length(binMids))
//        for (aa in 1:nage) {
//            d1 <- dnorm(binMids, EL[aa], SDL[aa])
//            t1 <- dnorm(EL[aa] + SDL[aa]*2.5, EL[aa], SDL[aa]) # truncate at 2.5 sd
//            d1[d1<t1] <- 0
//            if (!all(d1==0)) Prob[aa,] <- d1/sum(d1)
//        }
//
//
//
//
//
//        DLMtool:::LBSPRopt(
//                pars = c(logSelectivityCmAt50PercentAsPercentageOfLinf,
//                        logSelectivitySlope,
//                        logRatioFishingToNaturalMortality),
//                CAL = catchAtLengthObserved,
//                nage = nage,
//                nlen = length(binMids),
//                CVLinf = coefficientVariationLinf,
//                LenBins = rep.int(-1,times=length(binMids)), ### not used!
//                L50 =  -1, ### not used!
//                L95 = -1, ### not used!
//                LenMids = binMids,
//                MK = mkRatio,
//                Linf = Linf,
//                Ml = maturityPerBin,
//                Beta = bVariableLengthToWeightConversion,
//                Prob = Prob,
//                rLens = rLens
//)

    }


    @Test
    public void findingLBSPR() {

        double[] catchAtLengthObserved = new double[]{2.5, 5, 7.5, 10, 12.5, 15, 17.5, 20,
            22.5, 25, 27.5, 30, 32.5, 35, 37.5, 40, 42.5, 45,
            47.5, 50, 52.5, 55, 57.5, 60, 62.5, 65, 67.5, 70,
            72.5, 75, 77.5, 80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        double Linf = 100;
        double coefficientVariationLinf = .1;
        double[] binMids =
            new double[]{2.5, 5, 7.5, 10, 12.5, 15, 17.5, 20, 22.5, 25,
                27.5, 30, 32.5, 35, 37.5, 40, 42.5, 45, 47.5, 50,
                52.5, 55, 57.5, 60, 62.5, 65, 67.5, 70, 72.5, 75,
                77.5, 80, 82.5, 85, 87.5, 90, 92.5, 95, 97.5, 100,
                102.5, 105, 107.5};


        double mkRatio = 1.5;
        double[] maturityPerBin = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        double bVariableLengthToWeightConversion = .01;

        final LbSprEstimation.LBSPREstimate estimate = LbSprEstimation.computeSPR(
            catchAtLengthObserved,
            Linf,
            coefficientVariationLinf,
            binMids,
            mkRatio,
            maturityPerBin,
            bVariableLengthToWeightConversion
        );
        System.out.println(estimate);


        Assertions.assertEquals(estimate.getFishingToNaturalMortalityRatio(), 2.599041, .01);

        Assertions.assertEquals(estimate.getLengthAt50PercentSelectivity(), 69.7203, .01);

        Assertions.assertEquals(estimate.getLengthAt95PercentSelectivity(), 112.0104, .01);

        Assertions.assertEquals(estimate.getSpr(), 0.4260315, .01);
        Assertions.assertEquals(estimate.getLikelihood(), 167.1362, .01);

    }

    @Test
    public void findingLBSPRInTheAgent() {


        EquallySpacedBertalanffyFactory factory = new EquallySpacedBertalanffyFactory();
        factory.setAllometricAlpha(new FixedDoubleParameter(1));
        factory.setAllometricBeta(new FixedDoubleParameter(3));
        factory.setMaxLengthInCm(new FixedDoubleParameter(100));
        factory.setRecruitLengthInCm(new FixedDoubleParameter(0d));
        factory.setkYearlyParameter(new FixedDoubleParameter(1));
        factory.setNumberOfBins(101);


        GrowthBinByList meristics = factory.apply(mock(FishState.class));

        Species fish = new Species("test", meristics);


        SPRAgent agent =
            new SPRAgent("tag",
                fish,
                fisher -> fisher.getID() == 1,
                100d,
                1, 1.5, 100, 1000, 5,
                1, 3,
                52, new LbSPRFormula()
            );


        //there are two fishers, but you should only sample fisher 1
        int[] lengthsCaught = new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 75, 81};
        int[] correctLandings = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};


        double reOrderedLandings[] = new double[101];
        for (int i = 0; i < lengthsCaught.length; i++)
            reOrderedLandings[lengthsCaught[i]] = correctLandings[i];

        for (int i = 0; i < reOrderedLandings.length; i++)
            reOrderedLandings[i] = reOrderedLandings[i] * fish.getWeight(0, i);
        Fisher fisher1 = mock(Fisher.class, RETURNS_DEEP_STUBS);
        // when(fisher1.getDailyCounter()).thenReturn(mock(FisherDailyCounter.class,RETURNS_DEEP_STUBS));
        when(fisher1.getID()).thenReturn(1);
        FisherDailyCounter dailyCounter = fisher1.getDailyCounter();
        doAnswer(invocation -> {
            int bin = (Integer) invocation.getArguments()[2];
            return reOrderedLandings[bin];
        }).when(dailyCounter).getSpecificLandings(any(Species.class), anyInt(), anyInt());
        //fisher 2 returns garbage
        Fisher fisher2 = mock(Fisher.class, RETURNS_DEEP_STUBS);
        when(fisher2.getID()).thenReturn(2);
        when(fisher2.getDailyCounter().getSpecificLandings(any(Species.class), anyInt(), anyInt())).thenReturn(100d);

//        SPRAgent agent = new SPRAgent(
//                "testtag",
//                fish,
//                new Predicate<Fisher>() {
//                    @Override
//                    public boolean test(Fisher fisher) {
//                        return fisher.getID()==1;
//                    }
//                },
//                81,
//                0.4946723,
//                0.394192,
//                100,
//                1000,
//                5,
//                0.02d,
//                2.94,
//                48,
//                new LbSPRFormula()
//        );
        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);

        when(model.getFishers()).thenReturn(
            ObservableList.observableList(
                fisher1, fisher2
            )
        );

        agent.start(model);
        agent.step(model);
        double spr = agent.computeSPR();
        //problem here fundamentally is that we are using the LBSPR formula from DLMtoolkit rather than the package itself
        Assertions.assertEquals(0.3315833, spr, .05);


    }

}
