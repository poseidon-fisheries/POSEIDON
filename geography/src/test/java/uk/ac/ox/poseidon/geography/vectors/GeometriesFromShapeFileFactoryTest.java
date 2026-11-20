/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.geography.vectors.Factories.geometriesFromShapeFile;

import java.nio.file.Path;
import java.util.Map;
import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

final class GeometriesFromShapeFileFactoryTest {

  private final GeometryFactory geometryFactory = new GeometryFactory();

  @TempDir
  private Path directory;

  @Test
  void readsEveryFeatureGeometryOfTheShapeFile() throws Exception {
    final Path shapeFile = writeShapeFile(box(0, 0, 2, 2), box(10, 0, 12, 4));

    final var geometries = geometriesFromShapeFile(ignored -> shapeFile).get(
      GLOBAL_SCOPE
    );

    assertThat(geometries)
      .hasSize(2)
      .allSatisfy(geometry -> assertThat(geometry.isValid()).isTrue());
    assertThat(
      geometries.stream().map(Geometry::getEnvelopeInternal).toList()
    ).containsExactlyInAnyOrder(
      new Envelope(0, 2, 0, 2),
      new Envelope(10, 12, 0, 4)
    );
  }

  @Test
  void anEmptyShapeFileGivesNoGeometries() throws Exception {
    final Path shapeFile = writeShapeFile();

    assertThat(
      geometriesFromShapeFile(ignored -> shapeFile).get(GLOBAL_SCOPE)
    ).isEmpty();
  }

  @Test
  void aMissingShapeFileFailsWithoutReturningAnything() {
    final Path missing = directory.resolve("missing.shp");

    assertThatThrownBy(() ->
      geometriesFromShapeFile(ignored -> missing).get(GLOBAL_SCOPE)
    ).isInstanceOf(RuntimeException.class);
  }

  private Geometry box(
    final double minX,
    final double minY,
    final double maxX,
    final double maxY
  ) {
    return geometryFactory.toGeometry(new Envelope(minX, maxX, minY, maxY));
  }

  private Path writeShapeFile(final Geometry... geometries) throws Exception {
    final Path shapeFile = directory.resolve("areas.shp");
    final SimpleFeatureType type = DataUtilities.createType(
      "areas",
      "the_geom:Polygon"
    );
    final var features = new ListFeatureCollection(type);
    final var builder = new SimpleFeatureBuilder(type);
    for (final Geometry geometry : geometries) {
      builder.add(geometry);
      features.add(builder.buildFeature(null));
    }
    final var factory = FileDataStoreFinder.getDataStoreFactory("shp");
    final FileDataStore dataStore = (FileDataStore) factory.createNewDataStore(
      Map.of("url", shapeFile.toUri().toURL())
    );
    try {
      dataStore.createSchema(type);
      final var store = (SimpleFeatureStore) dataStore.getFeatureSource();
      store.addFeatures(features);
    } finally {
      dataStore.dispose();
    }
    return shapeFile;
  }
}
