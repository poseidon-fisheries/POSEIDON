package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Function;

/**
 * Takes a bunch of extractors and when the regression need it, turns them into the input matrix (x) of the
 * regression
 * Created by carrknight on 12/9/16.
 */
public class LogisticInputMaker {



    /**
     * the observation extractors (the functions that return the "x" associated with each group). One array for each possible Y
     */
    private final ObservationExtractor[][] extractors;


    /**
     * function (that can return null) that extracts from a bandit arm a seatile associated with it.
     * This is then fed to the observtion extractor
     */
    private final Function<Integer,SeaTile> armToTileExtractor;


    public LogisticInputMaker(
            ObservationExtractor[][] extractors,
            Function<Integer, SeaTile> armToTileExtractor) {
        this.extractors = extractors;
        this.armToTileExtractor = armToTileExtractor;
    }

    public LogisticInputMaker(
            ObservationExtractor[][] extractors) {
        this(extractors, integer -> null);
    }

    /**
     * extract the design matrix to feed into a regression
     * @param fisher the fisher
     * @param state the state
     * @return a matrix of inputs
     */
    public double[][] getRegressionInput(Fisher fisher, FishState state)
    {
        //compute all the x ahead of time
        final double[][] x = new double[extractors.length][];
        for(int i=0; i<extractors.length; i++)
        {
            x[i] = new double[extractors[0].length];
            for(int j=0; j<extractors[0].length; j++)
                x[i][j] = extractors[i][j].extract(armToTileExtractor.apply(i),
                                                   state.getHoursSinceStart(),fisher,state);

        }

        return x;

    }


    /**
     * Getter for property 'extractors'.
     *
     * @return Value for property 'extractors'.
     */
    public ObservationExtractor[][] getExtractors() {
        return extractors;
    }

    /**
     * Getter for property 'armToTileExtractor'.
     *
     * @return Value for property 'armToTileExtractor'.
     */
    public Function<Integer, SeaTile> getArmToTileExtractor() {
        return armToTileExtractor;
    }
}
