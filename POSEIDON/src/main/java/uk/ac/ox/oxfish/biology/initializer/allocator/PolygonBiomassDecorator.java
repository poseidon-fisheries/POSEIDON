package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import sim.field.geo.GeomVectorField;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.HashMap;
import java.util.function.Function;

public class PolygonBiomassDecorator implements BiomassAllocator {

    /**
     * polygons where biomass is allowed
     */
    private final GeomVectorField boundingPolygons;

    /**
     * if this is true, biomass is within the polygons. If this is false, biomass is allowed only outside of it.
     */
    private final boolean inside;

    /**
     * the delegate choosing biomass.
     */
    private final BiomassAllocator delegate;

    /**
     * pre-compute all the tiles so that you call "isInsideUnion" only once
     */
    private final HashMap<SeaTile, Boolean> insideUnion;


    public PolygonBiomassDecorator(GeomVectorField boundingPolygons, boolean inside, BiomassAllocator delegate) {
        this.boundingPolygons = boundingPolygons;
        this.inside = inside;
        this.delegate = delegate;
        boundingPolygons.computeUnion();
        insideUnion = new HashMap<>();
    }

    @Override
    public double allocate(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {


        if (!checkIfInside(tile, map)) {
            if (inside)
                return 0;
            else
                return delegate.allocate(tile, map, random);
        } else {
            if (inside)
                return delegate.allocate(tile, map, random);
            else
                return 0;
        }

    }

    private boolean checkIfInside(SeaTile tile, NauticalMap map) {
        return
            insideUnion.computeIfAbsent(
                tile, new Function<SeaTile, Boolean>() {
                    @Override
                    public Boolean apply(SeaTile seaTile) {
                        return boundingPolygons.isInsideUnion(map.getCoordinates(tile));
                    }
                }
            );

    }
}
