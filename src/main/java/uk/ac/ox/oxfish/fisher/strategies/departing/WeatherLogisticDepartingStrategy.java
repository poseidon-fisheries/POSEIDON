package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.LogisticDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A logistic function decides whether to go out or not based on an environment harshness value that is itself
 * caused by windspeed, boat length and the likes
 * Created by carrknight on 9/11/15.
 */
public class WeatherLogisticDepartingStrategy extends LogisticDepartingStrategy {


    /**
     * how much windspeed affects "environment harshness"
     */
    private final double windspeedSensitivity;

    /**
     * how much boat length affects "environment harshness"
     */
    private final double boatLengthSensitivity;

    /**
     * with 0 length and 0 windspeed, what is the harshness level?
     */
    private final double harshnessIntercept;


    public WeatherLogisticDepartingStrategy(
            double l, double k, double x0, double windspeedSensitivity, double boatLengthSensitivity,
            double harshnessIntercept) {
        super(l, k, x0);
        this.windspeedSensitivity = windspeedSensitivity;
        this.boatLengthSensitivity = boatLengthSensitivity;
        this.harshnessIntercept = harshnessIntercept;

    }

    /**
     * abstract method, returns whatever we need to plug in the logistic function
     *
     * @param equipment
     * @param status
     * @param memory
     * @param model
     */
    @Override
    public double computeX(
            FisherEquipment equipment, FisherStatus status, FisherMemory memory, FishState model) {


        return status.getLocation().getWindSpeedInKph() * windspeedSensitivity +
                equipment.getBoat().getLength() * boatLengthSensitivity +
                harshnessIntercept;

    }
}
