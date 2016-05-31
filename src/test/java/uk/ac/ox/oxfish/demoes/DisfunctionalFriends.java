package uk.ac.ox.oxfish.demoes;

import org.junit.Assert;
import org.junit.Test;

import static uk.ac.ox.oxfish.demoes.FunctionalFriendsDemo.stepsItTook;


public class DisfunctionalFriends {



    //another result I have is that exploration-imitation works poorly if everybody spends too much time
    //imitating and too little time exploring
    @Test
    public void disfunctionalFriends() throws Exception {


        //there is about 1:50 chance that this test fails if run only once (sometimes some pockets of fish are just ignored for too long)
        //so I run it twice and sum up the steps. Failure in this case is not an option
        int totalStepsAlone = 0;
        int totalStepsManyFriends = 0;

        for(int i=0; i<2; i++) {
            long seed = System.currentTimeMillis();
            int stepsAlone = stepsItTook(.2, 1, 5000, seed, true, .1);
            int stepsWithManyFriends = stepsItTook(.2, 20, 5000, seed, true, .1);

            System.out.println(stepsAlone + " ---- " + stepsWithManyFriends);

            totalStepsAlone += stepsAlone;
            totalStepsManyFriends +=stepsWithManyFriends;
        }
        Assert.assertTrue(totalStepsAlone < totalStepsManyFriends);




    }

}
