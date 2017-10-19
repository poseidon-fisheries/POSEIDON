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

package uk.ac.ox.oxfish.fisher.strategies.gear.factory;

import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Created by carrknight on 6/14/16.
 */
public class GearStrategies {

    public static final LinkedHashMap<String,
            Supplier<AlgorithmFactory<? extends GearStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();

    static {


        CONSTRUCTORS.put("Never Change Gear", FixedGearStrategyFactory::new);
        NAMES.put(FixedGearStrategyFactory.class,"Never Change Gear");


        CONSTRUCTORS.put("Periodic Gear Update from List", PeriodicUpdateFromListFactory::new);
        NAMES.put(PeriodicUpdateFromListFactory.class,"Periodic Gear Update from List");



        CONSTRUCTORS.put("Periodic Gear Update Mileage", PeriodicUpdateMileageFactory::new);
        NAMES.put(PeriodicUpdateMileageFactory.class,"Periodic Gear Update Mileage");



        CONSTRUCTORS.put("Periodic Gear Update Catchability", PeriodicUpdateCatchabilityFactory::new);
        NAMES.put(PeriodicUpdateCatchabilityFactory.class,"Periodic Gear Update Catchability");





    }

}
