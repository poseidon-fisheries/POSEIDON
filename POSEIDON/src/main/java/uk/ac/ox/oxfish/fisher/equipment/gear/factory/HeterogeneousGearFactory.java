/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.fisher.equipment.gear.HeterogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NullParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by carrknight on 5/17/16.
 */
public class HeterogeneousGearFactory implements AlgorithmFactory<HeterogeneousAbundanceGear> {


    public HashMap<String, HomogeneousGearFactory> gears = new HashMap<>();


    private DoubleParameter hourlyGasPriceOverride = new NullParameter();


    public HeterogeneousGearFactory() {
        gears.clear();
    }

    @SuppressWarnings("unchecked")
    public HeterogeneousGearFactory(
        final Entry<String, HomogeneousGearFactory>... given
    ) {
        gears.clear();
        for (final Entry<String, HomogeneousGearFactory> pair : given) {
            gears.put(pair.getKey(), pair.getValue());
        }
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HeterogeneousAbundanceGear apply(final FishState state) {

        final HashMap<Species, HomogeneousAbundanceGear> gearsPerSpecies = new HashMap<>();

        for (final Entry<String, HomogeneousGearFactory>
            entry : getGears().entrySet()) {
            gearsPerSpecies.put(
                state.getBiology().getSpeciesByCaseInsensitiveName(entry.getKey()),
                entry.getValue().apply(state)
            );
        }

        Preconditions.checkState(
            gears.size() == state.getSpecies().size() ||
                (gears.size() + 1 == state.getSpecies().size() && state.getBiology().getSpeciesByCaseInsensitiveName(
                    MultipleSpeciesAbundanceInitializer.FAKE_SPECIES_NAME) != null)
            ,
            "Not all species have a gear assigned");
        final HeterogeneousAbundanceGear heterogeneousAbundanceGear = new HeterogeneousAbundanceGear(gearsPerSpecies);
        heterogeneousAbundanceGear.setHourlyGasPriceOverride(hourlyGasPriceOverride.applyAsDouble(state.getRandom()));
        return heterogeneousAbundanceGear;

    }

    /**
     * Getter for property 'gears'.
     *
     * @return Value for property 'gears'.
     */
    @SuppressWarnings({"unchecked", "rawtypes", "DataFlowIssue"})
    public HashMap<String, HomogeneousGearFactory> getGears() {
        if (!(gears.values().iterator().next() instanceof HomogeneousGearFactory)) {
            //there is an annoying bug with yaml that doesn't really read maps correctly
            //so we'll have to force it here
            // (NP 2023-07-04): the aforementioned bug means that the runtime map content
            // violates its type parameters, which explains the need to rely on raw types
            // and unchecked casts. It is truly a thing of horror.
            final FishYAML yaml = new FishYAML();
            final HashMap<String, HomogeneousGearFactory> cleaned = new LinkedHashMap<>();
            for (final Map.Entry entry : gears.entrySet()) {
                final String key = (String) entry.getKey();
                final Map<String, LinkedHashMap<String, String>> container =
                    (HashMap<String, LinkedHashMap<String, String>>) entry.getValue();
                assert container.size() == 1;
                final Entry<String, LinkedHashMap<String, String>> constructor = container.entrySet()
                    .iterator()
                    .next();
                final StringBuilder cleanedYaml = new StringBuilder();
                cleanedYaml.append(constructor.getKey()).append(":").append("\n");
                for (final Entry<String, String> parameter : constructor.getValue().entrySet()) {
                    if (parameter.getValue() != null)
                        cleanedYaml.append("  ")
                            .append(parameter.getKey())
                            .append(": '")
                            .append(parameter.getValue())
                            .append("'")
                            .append("\n");
                }
                cleaned.put(key, yaml.loadAs(cleanedYaml.toString(), HomogeneousGearFactory.class));
            }
            gears = cleaned;
        }
        return gears;
    }

    /**
     * Setter for property 'gears'.
     *
     * @param gears Value to set for property 'gears'.
     */
    public void setGears(
        final HashMap<String, HomogeneousGearFactory> gears
    ) {
        this.gears = gears;
    }

    /**
     * Getter for property 'hourlyGasPriceOverride'.
     *
     * @return Value for property 'hourlyGasPriceOverride'.
     */
    public DoubleParameter getHourlyGasPriceOverride() {
        return hourlyGasPriceOverride;
    }

    /**
     * Setter for property 'hourlyGasPriceOverride'.
     *
     * @param hourlyGasPriceOverride Value to set for property 'hourlyGasPriceOverride'.
     */
    public void setHourlyGasPriceOverride(final DoubleParameter hourlyGasPriceOverride) {
        this.hourlyGasPriceOverride = hourlyGasPriceOverride;
    }
}
