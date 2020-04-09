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

import com.google.common.net.PercentEscaper;

@FunctionalInterface
public interface FileNameMaker {

    /**
     * This is a slightly roundabout way of creating safe file names:
     * The name is made from a prefix (usually the scenario title),
     * the base name and the .json extension. Is is then passed through
     * a PercentEscaper to make sure that all unsafe characters are turned
     * into their percent-encoded unicode points, and the % signs are finally
     * replaced by underscores to avoid the web server chocking on them.
     * The reason we go through this dance instead of just replacing problem
     * chars with underscores directly is to preserve uniqueness. Otherwise,
     * "Marine Protected Area - <75m" and "Marine Protected Area - >75m" would
     * both encode to "Marine Protected Area - _75m".
     */
    @SuppressWarnings("UnstableApiUsage")
    default String makeFileName(String fileNamePrefix) {
        return new PercentEscaper("-+.,_() ", false)
            .escape(String.format("%s - %s.json", fileNamePrefix, getBaseName()))
            .replaceAll("%", "_");
    }

    /**
     * This is the main part of the filename to be constructed,
     * to which a prefix and an extension will be added by {@code makeFilename}.
     */
    String getBaseName();

}