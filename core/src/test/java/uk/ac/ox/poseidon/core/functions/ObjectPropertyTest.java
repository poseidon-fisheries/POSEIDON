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

package uk.ac.ox.poseidon.core.functions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ObjectPropertyTest {

    @Test
    void readsASingleProperty() {
        assertThat(new ObjectProperty<Outer, String>(Outer.class, "name").apply(new Outer()))
            .isEqualTo("outer");
    }

    @Test
    void followsANestedPath() {
        assertThat(new ObjectProperty<Outer, String>(Outer.class, "inner.code").apply(new Outer()))
            .isEqualTo("code");
    }

    @Test
    void readsABooleanPropertyThroughItsIsGetter() {
        assertThat(new ObjectProperty<Outer, Boolean>(Outer.class, "active").apply(new Outer()))
            .isTrue();
    }

    @Test
    void readsAGetterInheritedFromAnInterface() {
        assertThat(new ObjectProperty<Coded, String>(Coded.class, "code").apply(new Inner()))
            .isEqualTo("code");
    }

    @Test
    void givesNullWhenTheLastPropertyIsNull() {
        assertThat(new ObjectProperty<Outer, String>(Outer.class, "inner.missingCode")
            .apply(new Outer()))
            .isNull();
    }

    @Test
    void throwsWhenAPropertyPartWayAlongThePathIsNull() {
        final ObjectProperty<Outer, String> property =
            new ObjectProperty<>(Outer.class, "missingInner.code");
        assertThatThrownBy(() -> property.apply(new Outer()))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("missingInner");
    }

    @Test
    void throwsWhenBuiltWithAnUnknownProperty() {
        assertThatThrownBy(() -> new ObjectProperty<Outer, String>(Outer.class, "inner.colour"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("colour");
    }

    @Test
    void doesNotTakeAnIsGetterForANonBooleanProperty() {
        assertThatThrownBy(() -> new ObjectProperty<Outer, String>(Outer.class, "weird"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void passesOnAGetterExceptionUnwrapped() {
        final ObjectProperty<Outer, String> property =
            new ObjectProperty<>(Outer.class, "broken");
        assertThatThrownBy(() -> property.apply(new Outer()))
            .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    public interface Coded {
        String getCode();
    }

    public static class Inner implements Coded {
        @Override
        public String getCode() {
            return "code";
        }

        public String getMissingCode() {
            return null;
        }
    }

    public static class Outer {
        public String getName() {
            return "outer";
        }

        public Inner getInner() {
            return new Inner();
        }

        public Inner getMissingInner() {
            return null;
        }

        public boolean isActive() {
            return true;
        }

        public String isWeird() {
            return "weird";
        }

        public String getBroken() {
            throw new UnsupportedOperationException();
        }
    }
}
