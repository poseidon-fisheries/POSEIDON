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

import uk.ac.ox.oxfish.model.regs.MaxHoursOutRegulation;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Just a map with a link to all the constructors
 * Created by carrknight on 6/14/15.
 */
public class Regulations
{


    /**
     * I am forcing the TAC factory itself to be a singleton so that i force all the TACs to remain connected
     */
    private static final TACMonoFactory TAC_MONO_FACTORY = new TACMonoFactory();

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends Regulation>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();

    static
    {
        CONSTRUCTORS.put("Anarchy", AnarchyFactory::new);
        NAMES.put(AnarchyFactory.class,"Anarchy");

        CONSTRUCTORS.put("Fishing Season", FishingSeasonFactory::new);
        NAMES.put(FishingSeasonFactory.class,"Fishing Season");

        CONSTRUCTORS.put("MPA Only", ProtectedAreasOnlyFactory::new);
        NAMES.put(ProtectedAreasOnlyFactory.class,"MPA Only");

        CONSTRUCTORS.put("MPA Chromosome", ProtectedAreaChromosomeFactory::new);
        NAMES.put(ProtectedAreaChromosomeFactory.class,"MPA Chromosome");

        CONSTRUCTORS.put("MPA with fine", FinedProtectedAreasFactory::new);
        NAMES.put(FinedProtectedAreasFactory.class,"MPA with fine");

        CONSTRUCTORS.put("MPA by depth", DepthMPAFactory::new);
        NAMES.put(DepthMPAFactory.class,"MPA by depth");



        CONSTRUCTORS.put("Mono-TAC", () -> TAC_MONO_FACTORY);
        NAMES.put(TACMonoFactory.class,"Mono-TAC");

        CONSTRUCTORS.put("Mono-IQ", IQMonoFactory::new);
        NAMES.put(IQMonoFactory.class,"Mono-IQ");

        CONSTRUCTORS.put("Mono-ITQ", ITQMonoFactory::new);
        NAMES.put(ITQMonoFactory.class,"Mono-ITQ");

        CONSTRUCTORS.put("Multi-ITQ", MultiITQFactory::new);
        NAMES.put(MultiITQFactory.class, "Multi-ITQ");

        CONSTRUCTORS.put("Multi-ITQ by List", MultiITQStringFactory::new);
        NAMES.put(MultiITQStringFactory.class, "Multi-ITQ by List");

        CONSTRUCTORS.put("Partial-ITQ", ITQSpecificFactory::new);
        NAMES.put(ITQSpecificFactory.class,"Partial-ITQ");

        CONSTRUCTORS.put("Multi-TAC", TACMultiFactory::new);
        NAMES.put(TACMultiFactory.class,"Multi-TAC");

        CONSTRUCTORS.put("Multi-TAC by List", MultiTACStringFactory::new);
        NAMES.put(MultiTACStringFactory.class,"Multi-TAC by List");


        CONSTRUCTORS.put("Kitchen Sink", KitchenSinkFactory::new);
        NAMES.put(KitchenSinkFactory.class,"Kitchen Sink");

        CONSTRUCTORS.put("Multi-Quotas from Map", MultiQuotaMapFactory::new);
        NAMES.put(MultiQuotaMapFactory.class, "Multi-Quotas from Map");


        CONSTRUCTORS.put("Single Species Threshold Taxation", ThresholdSingleSpeciesTaxation::new);
        NAMES.put(ThresholdSingleSpeciesTaxation.class, "Single Species Threshold Taxation");

        CONSTRUCTORS.put("Single Species PID Taxation", SingleSpeciesPIDTaxationOnLandingsFactory::new);
        NAMES.put(SingleSpeciesPIDTaxationOnLandingsFactory.class, "Single Species PID Taxation");

        CONSTRUCTORS.put("Open/Close Shodan from File", ShodanFromFileFactory::new);
        NAMES.put(ShodanFromFileFactory.class, "Open/Close Shodan from File");


        CONSTRUCTORS.put("Temporary MPA", TemporaryProtectedAreasFactory::new);
        NAMES.put(TemporaryProtectedAreasFactory.class, "Temporary MPA");

        CONSTRUCTORS.put("Multiple Regulations", MultipleRegulationsFactory::new);
        NAMES.put(MultipleRegulationsFactory.class, "Multiple Regulations");


        CONSTRUCTORS.put("Weak Multi-TAC by List", WeakMultiTACStringFactory::new);
        NAMES.put(WeakMultiTACStringFactory.class,"Weak Multi-TAC by List");

        CONSTRUCTORS.put("Port Based Wait Times", PortBasedWaitTimesFactory::new);
        NAMES.put(PortBasedWaitTimesFactory.class,"Port Based Wait Times");


        CONSTRUCTORS.put("Max Hours Out", MaxHoursOutFactory::new);
        NAMES.put(MaxHoursOutFactory.class,"Port Based Wait Times");

        CONSTRUCTORS.put("Trigger Regulation", TriggerRegulationFactory::new);
        NAMES.put(TriggerRegulationFactory.class,"Trigger Regulation");


    }




}
