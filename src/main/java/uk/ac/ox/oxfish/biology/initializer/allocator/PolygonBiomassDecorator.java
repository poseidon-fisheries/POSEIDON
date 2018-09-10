package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.vividsolutions.jts.geom.GeometryFactory;
import ec.util.MersenneTwisterFast;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

public class PolygonBiomassDecorator implements BiomassAllocator {

    /**
     * polygons where biomass is allowed
     */
    private final GeomVectorField boundingPolyongs;

    /**
     * if this is true, biomass is within the polygons. If this is false, biomass is allowed only outside of it.
     */
    private final boolean inside;

    /**
     * the delegate choosing biomass.
     */
    private final BiomassAllocator delegate;


    public PolygonBiomassDecorator(GeomVectorField boundingPolyongs, boolean inside, BiomassAllocator delegate) {
        this.boundingPolyongs = boundingPolyongs;
        this.inside = inside;
        this.delegate = delegate;
        boundingPolyongs.computeUnion();
    }

    @Override
    public double allocate(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {


        if(!boundingPolyongs.isInsideUnion(map.getCoordinates(tile)))
        {
            if(inside)
                return 0;
            else
                return delegate.allocate(tile,map,random);
        }
        else
        {
            if(inside)
                return delegate.allocate(tile,map,random);
            else
                return 0;
        }

    }
}
