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

package uk.ac.ox.poseidon.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReadablePropertiesTest {

  @Test
  void discoversReadablePropertiesIncludingInheritedOnes() {
    assertThat(ReadableProperties.of(Bean.class))
      .extracting(PropertyDescriptor::getName)
      .containsExactlyInAnyOrder(
        "plain",
        "map",
        "collection",
        "inherited",
        "absent"
      );
  }

  @Test
  void readsValuesWithoutUnrollingContainers() {
    final Bean bean = new Bean();

    for (final PropertyDescriptor property : ReadableProperties.of(
      Bean.class
    )) {
      final Object expected = switch (property.getName()) {
        case "plain" -> bean.getPlain();
        case "map" -> bean.getMap();
        case "collection" -> bean.getCollection();
        case "inherited" -> bean.getInherited();
        case "absent" -> null;
        default -> throw new AssertionError(property.getName());
      };
      assertThat(ReadableProperties.read(bean, property)).isSameAs(expected);
    }
  }

  @Test
  void discoveringPropertiesDoesNotInvokeGetters() {
    assertThat(ReadableProperties.of(FailingBean.class))
      .extracting(PropertyDescriptor::getName)
      .containsExactly("failure");
  }

  @Test
  void failedReadNamesThePropertyAndBeanClassAndKeepsTheCause() {
    final PropertyDescriptor property = ReadableProperties.of(
      FailingBean.class
    ).getFirst();

    assertThatThrownBy(() ->
      ReadableProperties.read(new FailingBean(), property)
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Cannot read failure on " + FailingBean.class)
      .hasCauseInstanceOf(InvocationTargetException.class)
      .hasRootCauseMessage("getter failed");
  }

  public static class Parent {

    public String getInherited() {
      return "inherited";
    }
  }

  public static final class Bean extends Parent {

    private final Object plain = new Object();
    private final Map<String, Object> map = Map.of("key", plain);
    private final Collection<Object> collection = List.of(plain);

    public Object getPlain() {
      return plain;
    }

    public Map<String, Object> getMap() {
      return map;
    }

    public Collection<Object> getCollection() {
      return collection;
    }

    public Object getAbsent() {
      return null;
    }

    public void setWriteOnly(final Object value) {}
  }

  public static final class FailingBean {

    public Object getFailure() {
      throw new IllegalArgumentException("getter failed");
    }
  }
}
