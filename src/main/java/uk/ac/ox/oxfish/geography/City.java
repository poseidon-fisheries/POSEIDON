package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Geometry;
import sim.util.geo.MasonGeometry;

/**
 * Unless it becomes important to model price dispersion the city is just a landmark. It extends MasonGeometry. I'd like
 * it not to, but GeoMason gui is pretty choosy about what it wants and so here we are.
 * Created by carrknight on 4/3/15.
 */
public class City  extends MasonGeometry{


    private final String name;

    private final int population;

    public City(Geometry g, String name, int population)
    {
        super(g);
        this.name = name;
        this.population = population;
    }


    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public int getPopulation() {
        return population;
    }



}
