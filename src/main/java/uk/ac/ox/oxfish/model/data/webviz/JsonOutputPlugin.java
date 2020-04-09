/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.data.webviz;

import com.google.gson.Gson;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.OutputPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public final class JsonOutputPlugin<T> implements OutputPlugin, AdditionalStartable {

    private final Gson gson;
    private final JsonBuilder<? extends T> builder;
    private final String fileName;
    private T outputObject;

    JsonOutputPlugin(final Gson gson, final JsonBuilder<? extends T> builder, final String fileName) {
        this.gson = gson;
        this.builder = builder;
        this.fileName = fileName;
    }

    /**
     * Does the same job as {@code FishStateUtilities.writeAdditionalOutputsToFolder}
     * but writes the content directly to a buffered file writer instead of having to
     * build the output strings first.
     */
    public static void writeOutputsToFolder(FishState fishState, Path outputFolder) {
        fishState
            .getOutputPlugins()
            .stream()
            .filter(plugin -> plugin instanceof JsonOutputPlugin)
            .forEach(plugin ->
                ((JsonOutputPlugin<?>) plugin).writeOutputToFile(fishState, outputFolder)
            );
    }

    private void writeOutputToFile(FishState fishState, Path outputFolder) {
        reactToEndOfSimulation(fishState);
        final File outputFile = outputFolder.resolve(getFileName()).toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            gson.toJson(outputObject, writer);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write JSON output to " + outputFile, e);
        }
    }

    @Override public void reactToEndOfSimulation(final FishState fishState) {
        outputObject = builder.buildJsonObject(fishState);
    }

    @Override public String getFileName() { return fileName; }

    @Override public String composeFileContents() { return gson.toJson(outputObject); }

    @Override public void start(final FishState fishState) {
        if (builder instanceof Startable) ((Startable) builder).start(fishState);
        fishState.getOutputPlugins().add(this);
    }

}
