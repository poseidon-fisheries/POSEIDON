package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Season;

/**
 * Seasonal dummy
 * Created by carrknight on 4/12/17.
 */
public class SeasonExtractor implements ObservationExtractor
{


    private final Season correctSeason;

    public SeasonExtractor(Season correctSeason) {
        this.correctSeason = correctSeason;
    }

    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        return Season.season(
                getSimulationDayFromHoursSinceStart(timeOfObservation,model)
        ).equals(correctSeason) ? 1d : 0d;
    }


    /**
     * this is useful for observations that store the observation time as hours since start but sometimes need to get back
     * what day of the year it actually was!
     * @param hoursSinceStart
     * @return
     */
    public static int getDaySinceStartFromHoursSinceStart(double hoursSinceStart,
                                                    FishState model)
    {

        /*
        //get the steps
        double steps = hoursSinceStart / model.getHoursPerStep();
        //get days passed
        int days = (int) (steps * model.getStepsPerDay());
*/

//simplifies to this
        return  (int) (hoursSinceStart * model.getStepsPerDay() * model.getStepsPerDay() / 24d);

    }

    /**
     *
     * @param hoursSinceStart
     * @param model
     * @return
     */
    public static int getSimulationDayFromHoursSinceStart(double hoursSinceStart,
                                                          FishState model)
    {


        return  (getDaySinceStartFromHoursSinceStart(hoursSinceStart,model) % 365) + 1;

    }
}
