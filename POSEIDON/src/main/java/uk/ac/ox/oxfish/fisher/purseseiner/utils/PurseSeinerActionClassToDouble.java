/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class PurseSeinerActionClassToDouble implements ToDoubleFunction<Class<? extends PurseSeinerAction>> {
    private final Map<Class<? extends PurseSeinerAction>, Double> values;

    public PurseSeinerActionClassToDouble(final Map<Class<? extends PurseSeinerAction>, Double> values) {
        this.values = ImmutableMap.copyOf(values);
    }

    public static PurseSeinerActionClassToDouble fromFile(
        final Path path,
        final int year,
        final String actionColumn,
        final String valueColumn
    ) {
        return new PurseSeinerActionClassToDouble(
            recordStream(path)
                .filter(r -> r.getInt("year") == year)
                .collect(toImmutableMap(
                    r -> ActionClass.valueOf(r.getString(actionColumn)).getActionClass(),
                    r -> r.getDouble(valueColumn)
                ))
        );
    }

    @Override
    public String toString() {
        return values.toString();
    }

    public PurseSeinerActionClassToDouble mapValues(final DoubleUnaryOperator doubleUnaryOperator) {
        return new PurseSeinerActionClassToDouble(
            values.entrySet().stream()
                .collect(toImmutableMap(
                    Map.Entry::getKey,
                    entry -> doubleUnaryOperator.applyAsDouble(entry.getValue())
                ))
        );
    }

    @Override
    public double applyAsDouble(final Class<? extends PurseSeinerAction> purseSeinerActionClass) {
        return values.get(purseSeinerActionClass);
    }

}
