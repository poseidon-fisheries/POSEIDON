package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PeriodHabitBooleanExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PortDistanceExtractor;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The simplest possible regression we fit to WFS data
 * Created by carrknight on 4/18/17.
 */
public class BarebonesFloridaDestinationFactory extends BarebonesLogitDestinationFactory
{


    //variables here are the handliner default
    {
        ((CentroidMapFileFactory) discretizer).setFilePath(Paths.get("temp_wfs", "areas.txt").toString());
        ((CentroidMapFileFactory) discretizer).setxColumnName("eastings");
        ((CentroidMapFileFactory) discretizer).setyColumnName("northings");
    }


    @Override
    @NotNull
    public ObservationExtractor buildHabitExtractor(MapDiscretization discretization, final int period) {
        return new PeriodHabitBooleanExtractor(discretization, period);
    }


}
