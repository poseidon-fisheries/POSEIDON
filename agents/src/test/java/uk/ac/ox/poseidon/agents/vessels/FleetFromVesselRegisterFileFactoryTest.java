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

package uk.ac.ox.poseidon.agents.vessels;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.utils.ConstantFactory;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;
import uk.ac.ox.poseidon.io.sources.StringDataSourceFactory;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FleetFromVesselRegisterFileFactoryTest {

    private final String initialData = """
        cfr,name_of_vessel,place_of_registration,event,event_start_date,gear,t1,t2
        V1,Vee One,P1,CEN,2001-01-01,G1,1,a
        V2,Vee Two,P1,CEN,2001-01-01,G2,2,b
        """;
    private Simulation simulation;
    private Fleet fleet;
    private Port p1;
    private Port p2;
    private PortGrid portGrid;
    private Gear<?> g1;
    private Gear<?> g2;
    private Hold<?> h1;

    void initSimulation(final String extraData) {
        p1 = mock(Port.class);
        p2 = mock(Port.class);
        portGrid = mock(PortGrid.class);
        when(portGrid.getObject("P1")).thenReturn(java.util.Optional.of(p1));
        when(portGrid.getObject("P2")).thenReturn(java.util.Optional.of(p2));
        when(p1.getCode()).thenReturn("P1");
        when(p1.toString()).thenReturn("P1");
        when(p2.getCode()).thenReturn("P2");
        when(p2.toString()).thenReturn("P2");

        g1 = mock(Gear.class);
        g2 = mock(Gear.class);
        h1 = mock(Hold.class);

        // noinspection unchecked
        simulation =
            new Scenario(
                LocalDate.of(2000, 1, 1),
                Map.of(
                    "fleet", FleetFromVesselRegisterFileFactory.builder()
                        .vesselField(new ConstantFactory<>(mock(VesselField.class)))
                        .portGrid(new ConstantFactory<>(portGrid))
                        .dataSource(new StringDataSourceFactory(initialData + extraData))
                        .hold(new VesselScopeAdaptor<>(new ConstantFactory<>(h1)))
                        .gear(
                            new VesselScopeFactoriesByCode<>(
                                Map.of(
                                    "G1", new VesselScopeAdaptor<>(new ConstantFactory<>(g1)),
                                    "G2", new VesselScopeAdaptor<>(new ConstantFactory<>(g2))
                                ), null
                            )
                        )
                        .gearFactoryMappings(Map.of("gear", "code"))
                        .engine(new VesselScopeAdaptor<>(new ConstantFactory<>(mock(Engine.class))))
                        .build()
                )
            ).newSimulation();
        simulation.start();
        fleet = simulation.getComponent(Fleet.class);
        assertThat(fleet.getVessels()).isEmpty();
        simulation.step();
    }

    @Test
    void fleetInitialisation() {
        initSimulation("");
        assertThat(fleet.getVessels()).hasSize(2);
        final Vessel v1 = fleet.getVessel("V1").orElseThrow();
        assertThat(v1.getName()).isEqualTo("Vee One");
        assertThat(v1.getHomePort()).isEqualTo(p1);
        assertThat(v1.getTag("t1").orElseThrow()).isEqualTo(1);
        assertThat(v1.getTag("t2").orElseThrow()).isEqualTo("a");
        final Vessel v2 = fleet.getVessel("V2").orElseThrow();
        assertThat(v2.getName()).isEqualTo("Vee Two");
        assertThat(v2.getHomePort()).isEqualTo(p1);
        assertThat(v2.getTag("t1").orElseThrow()).isEqualTo(2);
        assertThat(v2.getTag("t2").orElseThrow()).isEqualTo("b");
    }

    @Test
    void fleetModification() {
        initSimulation("""
            V1,Vee Uno,P1,MOD,2001-01-02,G2,3,c
            V2,Vee Dos,P2,MOD,2001-01-02,G1,4,d
            """
        );
        simulation.step();
        final Vessel v1 = fleet.getVessel("V1").orElseThrow();
        assertThat(v1.getName()).isEqualTo("Vee Uno");
        assertThat(v1.getHomePort()).isEqualTo(p1);
        assertThat(v1.getGear()).isEqualTo(g2);
        assertThat(v1.getTag("t1").orElseThrow()).isEqualTo(3);
        assertThat(v1.getTag("t2").orElseThrow()).isEqualTo("c");
        final Vessel v2 = fleet.getVessel("V2").orElseThrow();
        assertThat(v2.getName()).isEqualTo("Vee Dos");
        assertThat(v2.getHomePort()).isEqualTo(p2);
        assertThat(v2.getGear()).isEqualTo(g1);
        assertThat(v2.getTag("t1").orElseThrow()).isEqualTo(4);
        assertThat(v2.getTag("t2").orElseThrow()).isEqualTo("d");
    }

    @Test
    void deactivationReactivation() {

        initSimulation("""
            V1,Vee One,P1,DES,2001-01-02,G1,1,a
            V2,Vee Two,P1,EXP,2001-01-03,G2,2,b
            V1,Vee One,P1,CST,2001-01-04,G1,1,a
            V2,Vee Two,P1,CHA,2001-01-05,G2,2,b
            V1,Vee One,P1,RET,2001-01-06,G1,1,a
            """
        );

        // Both vessels start off active
        final Vessel v1 = fleet.getVessel("V1").orElseThrow();
        final Vessel v2 = fleet.getVessel("V2").orElseThrow();
        assertThat(v1.isActive()).isTrue();
        assertThat(v2.isActive()).isTrue();

        // Only v1 is deactivated
        simulation.step();
        assertThat(v1.isActive()).isFalse();
        assertThat(v2.isActive()).isTrue();

        // v2 is deactivated as well
        simulation.step();
        assertThat(v1.isActive()).isFalse();
        assertThat(v2.isActive()).isFalse();

        // v1 is reactivated
        simulation.step();
        assertThat(v1.isActive()).isTrue();
        assertThat(v2.isActive()).isFalse();

        // v2 is reactivated
        simulation.step();
        assertThat(v1.isActive()).isTrue();
        assertThat(v2.isActive()).isTrue();

        // v1 is deactivated again
        simulation.step();
        assertThat(v1.isActive()).isFalse();
        assertThat(v2.isActive()).isTrue();

    }

    @Test
    void modifyNonExistingVessel() {
        assertThatThrownBy(
            () -> {
                initSimulation("""
                    V3,Vee Three,P1,MOD,2001-01-02,Gx,0,x
                    """
                );
                simulation.step();
            }
        ).hasMessageContaining("V3");
    }

    @Test
    void assignNonExistingPort() {
        assertThatThrownBy(
            () -> {
                initSimulation("""
                    V1,Vee One,P3,MOD,2001-01-02,Gx,0,x
                    """
                );
                simulation.step();
            }
        ).hasMessageContaining("P3");
    }

    @AfterEach
    void tearDown() {
        if (simulation != null) {
            fleet = null;
            simulation.finish();
        }
    }

}
