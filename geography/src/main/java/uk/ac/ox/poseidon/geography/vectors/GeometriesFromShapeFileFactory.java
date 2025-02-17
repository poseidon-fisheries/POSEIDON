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

import lombok.*;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.shapefile.ShapefileFeatureStore;
import org.geotoolkit.storage.feature.query.Query;
import org.geotoolkit.storage.feature.session.Session;
import org.locationtech.jts.geom.Geometry;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

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

    @SuppressWarnings("deprecation")
    private List<Geometry> readShapeFile(
        final Path path
    ) {
        try (final ShapefileFeatureStore featureStore = new ShapefileFeatureStore(path.toUri())) {
            final Session session = featureStore.createSession(true);
            final var result = featureStore
                .getNames()
                .stream()
                .map(Query::new)
                .flatMap(query -> session.getFeatureCollection(query).stream())
                .map(feature -> feature.getPropertyValue("sis:geometry"))
                .map(Geometry.class::cast)
                .toList();
            featureStore.close();
            return result;
        } catch (final DataStoreException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
