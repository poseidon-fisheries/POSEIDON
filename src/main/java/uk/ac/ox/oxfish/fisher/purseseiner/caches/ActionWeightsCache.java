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

package uk.ac.ox.oxfish.fisher.purseseiner.caches;

import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;

import java.nio.file.Path;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class ActionWeightsCache extends FisherValuesByActionFromFileCache<Double> {

    public static final ActionWeightsCache INSTANCE = new ActionWeightsCache();

    ActionWeightsCache() { super(() -> 0.0); }

    @Override
    protected Map<Integer, Map<String, Map<Class<? extends PurseSeinerAction>, Double>>> readValues(final Path valuesFile) {
        return parseAllRecords(valuesFile)
            .stream()
            .collect(
                groupingBy(
                    record -> record.getInt("year"),
                    groupingBy(
                        record -> record.getString("ves_no"),
                        toMap(
                            record -> ActionClass.valueOf(record.getString("event")).getActionClass(),
                            record -> record.getDouble("w")
                        )
                    )
                ));
    }

}
