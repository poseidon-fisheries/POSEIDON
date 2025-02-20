/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.geography.vectors;

import com.google.common.collect.ImmutableList;
import lombok.*;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeometriesFromShapeFileFactory extends GlobalScopeFactory<Collection<Geometry>> {

    @NonNull
    private Factory<? extends Path> path;

    @Override
    protected List<Geometry> newInstance(final Simulation simulation) {
        final Path filePath = this.path.get(simulation);
        return readShapeFile(filePath);
    }

    private List<Geometry> readShapeFile(
        final Path path
    ) {
        try {
            final Map<String, URL> params = Map.of("url", path.toFile().toURI().toURL());
            final DataStore dataStore = DataStoreFinder.getDataStore(params);
            final SimpleFeatureCollection featureCollection =
                dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getFeatures();
            final ImmutableList.Builder<Geometry> geometries = ImmutableList.builder();
            try (final SimpleFeatureIterator iterator = featureCollection.features()) {
                while (iterator.hasNext())
                    if (iterator.next().getDefaultGeometry() instanceof final Geometry geometry)
                        geometries.add(geometry);
            }
            dataStore.dispose();
            return geometries.build();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
