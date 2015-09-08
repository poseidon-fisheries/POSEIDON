package uk.ac.ox.oxfish.fisher.strategies.weather;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Given a threshold, if the wind speed is above it then set emergency to true otherwise not
 * Created by carrknight on 9/7/15.
 */
public class WindThresholdStrategy implements WeatherEmergencyStrategy {


    private final double threshold;

    public WindThresholdStrategy(double threshold) {
        this.threshold = threshold;
    }

    /**
     * return what the new value of the weather emergency flag ought to be: this overrides other behaviors of the fisher
     * when true and make it go home (or stay there)
     *
     * @param currentEmergencyFlag is the weather flag on or off currently?
     * @param fisher               the fisher making the decision
     * @param location             where the fisher is currently
     * @return true if the fisher needs to stop what he's doing and go home
     */
    @Override
    public boolean updateWeatherEmergencyFlag(
            boolean currentEmergencyFlag, Fisher fisher, SeaTile location) {

        //if you are already having an emergency and you aren't at port, stay in state of emergency
        if(currentEmergencyFlag && !fisher.isAtPort())
            return true;
        else
            return location.getWindSpeedInKph() > threshold;
    }

    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff() {

    }
}
