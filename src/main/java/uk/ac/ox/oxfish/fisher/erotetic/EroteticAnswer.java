package uk.ac.ox.oxfish.fisher.erotetic;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;

import java.util.List;

/**
 * A "filter" that given a list of options only returns the ones that are deemed "acceptable" or null
 * if there is no way to give preference to any strategy
 * Created by carrknight on 4/10/16.
 */
public interface EroteticAnswer<T> extends Startable
{




    /**
     * Grabs the list of current options and returns the list of all options that are acceptable
     * @param currentOptions list of options, possibly already filtered by others. It is <b>unmodifiable</b>
     * @param representation the set of all feature extractors available
     * @param state the model   @return a list of acceptable options or null if there is pure indifference among them
     * @param fisher   */
    List<T> answer(
            List<T> currentOptions,
            FeatureExtractors<T> representation,
            FishState state,
            Fisher fisher);






}
