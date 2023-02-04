/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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
