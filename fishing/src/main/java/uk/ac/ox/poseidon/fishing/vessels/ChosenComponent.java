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
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * A vessel component chosen by the vessel's attributes: a
 * table of exact values of the attributes it reads, each combination giving
 * what to build, or no component, with an optional default for the
 * combinations it does not list. The rows have no order, and a combination
 * appears in one row at most. A vessel is given its component when it is
 * created, and given it anew whenever one of the attributes the choice reads
 * changes, even if the same row is picked. A choice reads attributes only,
 * never another component. Built via {@link Factories#choice}.
 *
 * @param <T> the type of the component
 */
@Getter
public final class ChosenComponent<T>
  extends GlobalScopeFactory<Function<Vessel, T>>
  implements VesselComponent<T>
{

  /** The attributes the choice reads, in the order each row gives values. */
  @NonNull
  private final ImmutableList<String> attributes;

  /** The rows, each a combination of values and what it gives. */
  @NonNull
  private final ImmutableList<ChoiceRow<? extends T>> rows;

  /**
   * What a combination that no row lists gives, or {@code null} if there is
   * no default, so that such a combination is an error.
   */
  private final ChoiceOutcome<? extends T> defaultOutcome;

  /**
   * @throws IllegalArgumentException if a row does not give one value for each
   *                                  attribute, or two rows give the same
   *                                  values
   */
  @Builder(toBuilder = true)
  private ChosenComponent(
    @NonNull @Singular final ImmutableList<String> attributes,
    @NonNull @Singular final ImmutableList<ChoiceRow<? extends T>> rows,
    final ChoiceOutcome<? extends T> defaultOutcome
  ) {
    final Set<ImmutableList<Object>> combinations = new HashSet<>();
    rows.forEach(row -> {
      checkArgument(
        row.getValues().size() == attributes.size(),
        "The choice by %s has a row with the values %s",
        attributes,
        row.getValues()
      );
      checkArgument(
        combinations.add(row.getValues()),
        "The choice by %s has two rows with the values %s",
        attributes,
        row.getValues()
      );
    });
    this.attributes = attributes;
    this.rows = rows;
    this.defaultOutcome = defaultOutcome;
  }

  @Override
  protected Function<Vessel, T> newInstance(final Scope scope) {
    return this::of;
  }

  /** @return whether {@code other} is this very component */
  @Override
  public boolean equals(final Object other) {
    return this == other;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  /**
   * @param vessel the vessel to choose for
   * @return what the row of {@code vessel}'s values gives, or the default if
   *         no row lists them
   * @throws IllegalStateException if {@code vessel} lacks an attribute the
   *                               choice reads, or no row lists its values and
   *                               there is no default
   */
  ChoiceOutcome<? extends T> outcomeFor(final Vessel vessel) {
    final Map<String, Object> vesselAttributes = vessel.getAttributes();
    final ImmutableList<Object> values = attributes
      .stream()
      .map(attribute -> {
        final Object value = vesselAttributes.get(attribute);
        checkState(
          value != null,
          "Vessel %s has no attribute %s",
          vessel.getId(),
          attribute
        );
        return value;
      })
      .collect(toImmutableList());
    return rows
      .stream()
      .filter(row -> row.getValues().equals(values))
      .findFirst()
      .<ChoiceOutcome<? extends T>>map(ChoiceRow::getOutcome)
      .or(() -> Optional.ofNullable(defaultOutcome))
      .orElseThrow(() ->
        new IllegalStateException(
          "Vessel %s has the values %s for %s, which no row of the choice lists, and the choice has no default".formatted(
            vessel.getId(),
            values,
            attributes
          )
        )
      );
  }
}
