package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.LinkedList;
import java.util.List;

/**
 * A bunch of distances one on top of the other
 * Created by carrknight on 7/7/16.
 */
public class CompositeRegressionDistance implements RegressionDistance
{


    private final List<RegressionDistance> components;


    public CompositeRegressionDistance() {
        components = new LinkedList<>();
    }


    public CompositeRegressionDistance(RegressionDistance... initialComponents) {

        components =  new LinkedList<>();
        for(RegressionDistance distance : initialComponents)
            components.add(distance);
    }

    @Override
    public double distance(
            SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {


        double sum =0;
        for(RegressionDistance distance : components)
            sum+= distance.distance(tile,currentTimeInHours,observation);

        return sum;



    }


    /**
     * Getter for property 'components'.
     *
     * @return Value for property 'components'.
     */
    public List<RegressionDistance> getComponents() {
        return components;
    }
}
