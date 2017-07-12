package uk.ac.ox.oxfish.gui.drawing;

import sim.util.gui.SimpleColorMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.function.Function;

/**
 * Created by carrknight on 7/12/17.
 */
public class SelfAdjustingColorEncoding extends ColorEncoding {

    private double maxValue;

    private double minValue;

    private final Function<SeaTile,Double> adjustingEncoding;

    public SelfAdjustingColorEncoding(
            SimpleColorMap map,
            Function<SeaTile, Double> encoding, boolean immutable, double maxValue, double minValue) {
        super(map, encoding, immutable);
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.adjustingEncoding = encoding.andThen(
                new Function<Double, Double>() {
                    @Override
                    public Double apply(Double value) {

                        if(value>SelfAdjustingColorEncoding.this.maxValue)
                        {
                            SelfAdjustingColorEncoding.this.maxValue = value;
                            map.setLevels(
                                    SelfAdjustingColorEncoding.this.minValue,
                                    SelfAdjustingColorEncoding.this.maxValue,
                                    map.getColor(SelfAdjustingColorEncoding.this.minValue ),
                                    map.getColor(SelfAdjustingColorEncoding.this.maxValue )
                            );
                        }
                        return value;

                    }
                }
        );
    }

    @Override
    public Function<SeaTile, Double> getEncoding() {
        return adjustingEncoding;
    }
}
