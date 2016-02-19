package uk.ac.ox.oxfish.biology;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 2/19/16.
 */
public class MeristicsTest {


    @Test
    public void yelloweye() throws Exception {


        Meristics yellowEye = new Meristics(70,1,18.717,64.594,0.047,0.000017,3.03,0.045,
                                            70,1,18.717,62.265,0.047,0.00000977,3.17,0.046,
                                            38.78,-0.437,137900,36500);

        //see if age 5 was computed correctly
        assertEquals(yellowEye.getLengthFemaleInCm()[5],26.4837518217,.001);
        //todo ask steve about these numbers:
       // assertEquals(yellowEye.getLengthMaleInCm()[5],28.9073864644,.001);

        assertEquals(yellowEye.getWeightFemaleInKg()[5],0.3167667645,.001);
        //todo ask steve about these numbers
        //assertEquals(yellowEye.getWeightMaleInKg()[5],0.4542612295,.001);

        assertEquals(yellowEye.getMaturity()[5],0.0046166415,.0001);
        //todo ask steve about these numbers:
//        assertEquals(yellowEye.getRelativeFecundity()[5],1104556.94149208,.001);
        //        assertEquals(yellowEye.getPhi()[5],4051.5996992325,.001);

        assertEquals(yellowEye.getCumulativeSurvivalFemale()[5],0.7945336025,.001);
    }


    @Test
    public void shortspine() throws Exception {


        Meristics shortspine = new Meristics(100,2,7,75,0.018,4.77E-06,3.263,0.0505,
                                             100,2,7,75,0.018,4.77E-06,3.263,0.0505,
                                             18.2,-2.3,1,0);

        //see if age 5 was computed correctly
        assertEquals(shortspine.getLengthFemaleInCm()[5],11.3138255265,.001);
        assertEquals(shortspine.getLengthMaleInCm()[5],11.3138255265,.001);

        assertEquals(shortspine.getWeightFemaleInKg()[5],0.0130770514,.001);
        assertEquals(shortspine.getWeightMaleInKg()[5],0.0130770514,.001);

        assertEquals(shortspine.getMaturity()[5],1.32298987179355E-07,.0001);
        assertEquals(shortspine.getRelativeFecundity()[5], 1, .001);
        assertEquals(shortspine.getPhi()[5],1.08100831396146E-07,.001);

    }

    @Test
    public void longspine() throws Exception {


        Meristics longspine = new Meristics(40,3,8.573,27.8282,0.108505,4.30E-06,3.352,0.111313,
                                             40,3,8.573,27.8282,0.108505,4.30E-06,3.352,0.111313,
                                             17.826,-1.79,1,0);

        //see if age 5 was computed correctly
        assertEquals(longspine.getLengthFemaleInCm()[5],12.3983090675,.001);
        assertEquals(longspine.getLengthMaleInCm()[5],12.3983090675,.001);

        assertEquals(longspine.getWeightFemaleInKg()[5],0.019880139,.001);
        assertEquals(longspine.getWeightMaleInKg()[5],0.019880139,.001);

        assertEquals(longspine.getMaturity()[5],6.03332555676691E-05,.0001);
        assertEquals(longspine.getRelativeFecundity()[5], 1, .001);
        assertEquals(longspine.getCumulativeSurvivalMale()[5], 0.5731745408, .001);
        assertEquals(longspine.getPhi()[5],3.45814860523815E-05,.001);

    }

    @Test
    public void sablefish() throws Exception {


        Meristics sablefish = new Meristics(30,0.5,25.8,56.2,0.419,3.6724E-06,3.250904,0.065,
                                            30,0.5,25.8,64,0.335,3.4487E-06,3.26681,0.08,
                                            58,-0.13,1,0);

        //see if age 5 was computed correctly
        assertEquals(sablefish.getLengthFemaleInCm()[5],55.5416341677,.001);
        assertEquals(sablefish.getLengthMaleInCm()[5],51.5868143025,.001);

        assertEquals(sablefish.getWeightFemaleInKg()[5],1.7258103959,.001);
        assertEquals(sablefish.getWeightMaleInKg()[5],1.3559663707,.001);

        assertEquals(sablefish.getMaturity()[5],0.4207762664,.0001);
        assertEquals(sablefish.getRelativeFecundity()[5], 1, .001);
        //todo ask steve since sablefish excel sheet starts at age 1 rather than 0
      //  assertEquals(sablefish.getCumulativeSurvivalFemale()[5],0.7261490371,.001);
       // assertEquals(sablefish.getPhi()[5],0.3055462806,.001);

    }


    @Test
    public void doverSole() throws Exception {


        Meristics sole = new Meristics(50,1,9.04,39.91,0.1713,0.000002231,3.412,0.1417,
                                       50,1,5.4,47.81,0.1496,0.000002805,3.345,0.1165,
                                       35,-0.775,1,0);

        //see if age 5 was computed correctly
        assertEquals(sole.getLengthFemaleInCm()[5],24.5101516433,.001);
        assertEquals(sole.getLengthMaleInCm()[5],24.3553122333,.001);

        assertEquals(sole.getWeightFemaleInKg()[5],0.124536091,.001);
        assertEquals(sole.getWeightMaleInKg()[5],0.120103947,.001);

        assertEquals(sole.getMaturity()[5],0.0002945897,.0001);
        //todo check this  since sole excel sheet starts at age 1 rather than 0
        //assertEquals(sole.getRelativeFecundity()[5], 1, .001);
        //assertEquals(sole.getCumulativeSurvivalMale()[5], 0.627507284, .001);
        //assertEquals(sole.getPhi()[5],2.30213887774956E-05,.001);

    }


    @Test
    public void canaryRockfish() throws Exception {


        Meristics canary = new Meristics(20,1,52.53,8.04,0.16,1.55E-05,3.03,0.06,
                                         20,1,8.04,60.36,0.125,1.55E-05,3.03,0.06,
                                         40.5,-0.25,1,0);

        //see if age 5 was computed correctly
        assertEquals(canary.getLengthFemaleInCm()[5],30.7375135093,.001);
        assertEquals(canary.getWeightFemaleInKg()[5],0.4988476814,.001);

        //todo ask to steve for discrepancy
        //assertEquals(canary.getLengthMaleInCm()[5],24.3479116018,.001);
        //assertEquals(canary.getWeightMaleInKg()[5],0.2462128406,.001);

        assertEquals(canary.getMaturity()[5],0.0801270824,.0001);
        assertEquals(canary.getRelativeFecundity()[5], 1, .001);
        assertEquals(canary.getCumulativeSurvivalFemale()[5], 0.7408182207, .001);
        assertEquals(canary.getPhi()[5],0.0593596026
                ,.001);

    }
}