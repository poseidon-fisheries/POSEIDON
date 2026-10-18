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

package uk.ac.ox.poseidon.fishing.vessels;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What a source states about a vessel that exists: that some of its
 * attributes change, or that it exits. A statement joins the vessel's queue,
 * through its fleet, and takes effect at the vessel's next step boundary, never
 * inside the call that made it.
 */
sealed interface VesselStatement {
  /**
   * Some of a vessel's attributes take new values, others become absent, and
   * those it does not name are untouched.
   *
   * @param values           the attributes set, with their new values
   * @param absentAttributes the attributes that become absent
   */
  record AttributeChange(
    ImmutableMap<String, Object> values,
    ImmutableSet<String> absentAttributes
  ) implements VesselStatement {
    /**
     * @throws IllegalArgumentException if an attribute is both set and made
     *                                  absent
     */
    public AttributeChange {
      checkArgument(
        Sets.intersection(values.keySet(), absentAttributes).isEmpty(),
        "Attributes both set and made absent: %s",
        Sets.intersection(values.keySet(), absentAttributes)
      );
    }

    /**
     * @return {@code attributes} with this change applied, in their order, the
     *         new ones last
     */
    Map<String, Object> applyTo(final Map<String, ?> attributes) {
      final Map<String, Object> changedAttributes = new LinkedHashMap<>(
        attributes
      );
      changedAttributes.putAll(values);
      changedAttributes.keySet().removeAll(absentAttributes);
      return changedAttributes;
    }
  }

  /**
   * The vessel exits: it leaves its fleet for good, and no later statement
   * about it may be made.
   */
  record Exit() implements VesselStatement {}
}
