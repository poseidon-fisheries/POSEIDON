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
 *
 */

package uk.ac.ox.poseidon.geography.grids;

import lombok.*;
import org.apache.sis.coverage.grid.GridCoverage;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.DataStores;
import org.apache.sis.storage.GridCoverageResource;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.Envelope;

import java.nio.file.Path;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GridExtentFromEsriAsciiGridFactory
    extends GlobalScopeFactory<uk.ac.ox.poseidon.geography.grids.GridExtent> {
    @NonNull private Factory<? extends Path> path;

    @Override
    protected uk.ac.ox.poseidon.geography.grids.GridExtent newInstance(final Simulation simulation) {
        try (final DataStore store = DataStores.open(this.path.get(simulation))) {
            final GridCoverageResource r = (GridCoverageResource) store;
            final GridCoverage coverage = r.read(null, null);
            final org.apache.sis.coverage.grid.GridExtent sisExtent =
                coverage.getGridGeometry().getExtent();
            final int width = (int) (sisExtent.getHigh(0) - sisExtent.getLow(0)) + 1;
            final int height = (int) (sisExtent.getHigh(1) - sisExtent.getLow(1)) + 1;
            final Envelope envelope = coverage
                .getEnvelope()
                .map(sisEnvelope -> new Envelope(
                    sisEnvelope.getMinimum(0),
                    sisEnvelope.getMaximum(0),
                    sisEnvelope.getMinimum(1),
                    sisEnvelope.getMaximum(1)
                ))
                .orElseThrow(
                    // TODO: write proper exception message
                );
            return new uk.ac.ox.poseidon.geography.grids.GridExtent(
                width,
                height,
                envelope
            );
        } catch (final DataStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
