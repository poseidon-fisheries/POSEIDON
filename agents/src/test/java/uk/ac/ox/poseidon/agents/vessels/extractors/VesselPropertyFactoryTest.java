/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.vessels.extractors;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.core.scopes.Scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.vessels.extractors.Factories.vesselProperty;

class VesselPropertyFactoryTest {

    private static Vessel vesselWithGearCode(final String gearCode) {
        final Gear gear = mock(Gear.class);
        when(gear.getCode()).thenReturn(gearCode);
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getGear()).thenReturn(gear);
        return vessel;
    }

    @Test
    void survivesAYamlRoundTripWithOnlyItsPath() {
        final LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setTagInspector(tag -> tag.getClassName().startsWith("uk.ac.ox.poseidon"));
        final Yaml yaml = new Yaml(loaderOptions);
        final String dumped = yaml.dump(vesselProperty("gear.code"));
        assertThat(dumped).contains("propertyPath: gear.code").doesNotContain("rootClass");
        assertThat(yaml.<Object>load(dumped)).isEqualTo(vesselProperty("gear.code"));
    }

    @Test
    void readsTheCodeOfTheVesselsCurrentGear() {
        assertThat(
            vesselProperty("gear.code").get(Scope.GLOBAL_SCOPE).apply(vesselWithGearCode("OTB"))
        ).isEqualTo("OTB");
    }

    @Test
    void readsNullForAGearWithoutCode() {
        assertThat(
            vesselProperty("gear.code").get(Scope.GLOBAL_SCOPE).apply(vesselWithGearCode(null))
        ).isNull();
    }
}
