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

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.regs.ConjunctiveRegulationsFactory;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.TaggedRegulationFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Just a map with a link to all the constructors
 * Created by carrknight on 6/14/15.
 */
public class Regulations {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String, Supplier<AlgorithmFactory<? extends Regulation>>> CONSTRUCTORS;

    public static final Map<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(AnarchyFactory.class, "Anarchy");
        NAMES.put(FishingSeasonFactory.class, "Fishing Season");
        NAMES.put(ProtectedAreasOnlyFactory.class, "MPA Only");
        NAMES.put(SpecificProtectedAreaFromShapeFileFactory.class, "Specific MPA from Shape File");
        NAMES.put(SpecificProtectedAreaFromCoordinatesFactory.class, "Specific MPA from Coordinates");
        NAMES.put(ProtectedAreaChromosomeFactory.class, "MPA Chromosome");
        NAMES.put(FinedProtectedAreasFactory.class, "MPA with fine");
        NAMES.put(DepthMPAFactory.class, "MPA by depth");
        NAMES.put(TACMonoFactory.class, "Mono-TAC");
        NAMES.put(IQMonoFactory.class, "Mono-IQ");
        NAMES.put(ITQMonoFactory.class, "Mono-ITQ");
        NAMES.put(MultiITQFactory.class, "Multi-ITQ");
        NAMES.put(MultiITQStringFactory.class, "Multi-ITQ by List");
        NAMES.put(ITQSpecificFactory.class, "Partial-ITQ");
        NAMES.put(TACMultiFactory.class, "Multi-TAC");
        NAMES.put(MultiTACStringFactory.class, "Multi-TAC by List");
        NAMES.put(KitchenSinkFactory.class, "Kitchen Sink");
        NAMES.put(MultiQuotaMapFactory.class, "Multi-Quotas from Map");
        NAMES.put(ThresholdSingleSpeciesTaxation.class, "Single Species Threshold Taxation");
        NAMES.put(SingleSpeciesPIDTaxationOnLandingsFactory.class, "Single Species PID Taxation");
        NAMES.put(TemporaryProtectedAreasFactory.class, "Temporary MPA");
        NAMES.put(TemporaryRegulationFactory.class, "Temporary Regulation");
        NAMES.put(TaggedRegulationFactory.class, "Tagged Regulation");
        NAMES.put(MultipleRegulationsFactory.class, "Multiple Regulations");
        NAMES.put(ConjunctiveRegulationsFactory.class, "Conjunctive Regulations");
        NAMES.put(WeakMultiTACStringFactory.class, "Weak Multi-TAC by List");
        NAMES.put(PortBasedWaitTimesFactory.class, "Port Based Wait Times");
        NAMES.put(MaxHoursOutFactory.class, "Max Hours Out");
        NAMES.put(TriggerRegulationFactory.class, "Trigger Regulation");
        NAMES.put(OffSwitchFactory.class, "Off Switch Decorator");
        NAMES.put(NoFishingFactory.class, "No Fishing");
        NAMES.put(ProtectedAreasFromFolderFactory.class, "Protected Areas from Folder");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }
}
