/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.sampling.GeographicalSample;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

public abstract class FileBiomassAllocator implements BiomassAllocator {


    /**
     * true if when reading data we should skip the first line
     */
    private final boolean inputFileHasHeader;


    /**
     * the path to the file to read
     */
    private final Path csvFile;


    private boolean initialized = false;

    public FileBiomassAllocator(final Path csvFile, final boolean inputFileHasHeader) {
        this.inputFileHasHeader = inputFileHasHeader;
        this.csvFile = csvFile;
    }

    public double allocate(final SeaTile tile, final NauticalMap map, final MersenneTwisterFast random) {

        //initialize if needed
        if (!isInitialized())
            try {
                lazyInitialization(map);
            } catch (final IOException e) {
                throw new RuntimeException("failed to read allocator csv file!", e);
            }

        return allocateNumerically(tile, map, random);


    }

    /**
     * Getter for property 'initialized'.
     *
     * @return Value for property 'initialized'.
     */
    public boolean isInitialized() {
        return initialized;
    }

    private void lazyInitialization(final NauticalMap map) throws IOException {

        //make sure we initialize only once!
        Preconditions.checkArgument(!initialized);
        initialized = true;

        //otherwise read from data
        final GeographicalSample biologicalSample = new
            GeographicalSample(
            csvFile,
            inputFileHasHeader
        );

        //check it was read correctly
        Preconditions.checkArgument(
            biologicalSample.getNumberOfObservations() > 0,
            "The CSV provided" + csvFile + " had no data!"
        );

        //get read to iterate!
        final Iterator<Double> x = biologicalSample.getFirstCoordinate().iterator();
        final Iterator<Double> y = biologicalSample.getSecondCoordinate().iterator();
        final Iterator<Double> value = biologicalSample.getObservations().iterator();

        for (int i = 0; i < biologicalSample.getNumberOfObservations(); i++) {
            //this is quite slow. We have to do it only once though!
            final Double currentX = x.next();
            final Double currentY = y.next();
            final Double nextValue = value.next();

            observePoint(map, currentX, currentY, nextValue);

        }
        //should be done, now!
        Preconditions.checkState(!x.hasNext(), "failed to iterate all x columns; mismatch column sizes?");
        Preconditions.checkState(!y.hasNext(), "failed to iterate all y columns; mismatch column sizes?");
        Preconditions.checkState(!value.hasNext(), "failed to iterate all value columns; mismatch column sizes?");

        //initialization complete!


    }

    protected abstract double allocateNumerically(SeaTile tile, NauticalMap map, MersenneTwisterFast random);

    protected abstract void observePoint(NauticalMap map, Double currentX, Double currentY, Double nextValue);
}
