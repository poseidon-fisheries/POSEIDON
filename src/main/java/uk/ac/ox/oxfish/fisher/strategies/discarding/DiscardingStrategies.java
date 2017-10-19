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

package uk.ac.ox.oxfish.fisher.strategies.discarding;

import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.RandomFavoriteDestinationFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Created by carrknight on 5/3/17.
 */
public class DiscardingStrategies {


    public static final LinkedHashMap<String,Supplier<AlgorithmFactory<? extends DiscardingStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();


    static {
        CONSTRUCTORS.put("No Discarding",
                         NoDiscardingFactory::new
        );
        NAMES.put(NoDiscardingFactory.class,
                  "No Discarding");

        CONSTRUCTORS.put("Discarding All Unsellable",
                         DiscardingAllUnsellableFactory::new
        );
        NAMES.put(DiscardingAllUnsellableFactory.class,
                  "Discarding All Unsellable");


        CONSTRUCTORS.put("Specific Discarding",
                         AlwaysDiscardTheseSpeciesFactory::new
        );
        NAMES.put(AlwaysDiscardTheseSpeciesFactory.class,
                  "Specific Discarding");


        CONSTRUCTORS.put("Age Discarding",
                         DiscardUnderagedFactory::new
        );
        NAMES.put(DiscardUnderagedFactory.class,
                  "Age Discarding");

    }

}
