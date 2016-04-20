package uk.ac.ox.oxfish.fisher.strategies.weather;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * The strategy used by the fisher to fish
 * Created by carrknight on 9/7/15.
 */
public interface WeatherEmergencyStrategy extends FisherStartable{


    /**
     * return what the new value of the weather emergency flag ought to be: this overrides other behaviors of the fisher
     * when true and make it go home (or stay there)
     * @param currentEmergencyFlag is the weather flag on or off currently?
     * @param fisher the fisher making the decision
     * @param location where the fisher is currently
     * @return true if the fisher needs to stop what he's doing and go home
     */
    boolean updateWeatherEmergencyFlag(
            boolean currentEmergencyFlag, Fisher fisher,
            SeaTile location);



}
