package uk.ac.ox.oxfish.utility.bandit;

import ec.util.MersenneTwisterFast;

/**
 * Created by carrknight on 11/9/16.
 */
public interface BanditAlgorithm {


    public int chooseArm(MersenneTwisterFast random);

    public void observeReward(double reward, int armPlayed);
}
