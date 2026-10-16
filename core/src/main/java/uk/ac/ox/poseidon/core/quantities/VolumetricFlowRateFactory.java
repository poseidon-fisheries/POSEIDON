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

package uk.ac.ox.poseidon.core.quantities;

import static tech.units.indriya.unit.Units.HOUR;
import static tech.units.indriya.unit.Units.LITRE;

import javax.measure.Unit;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import si.uom.quantity.VolumetricFlowRate;

/**
 * A {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for
 * {@link VolumetricFlowRate} quantities, built via {@link Factories
 * Factories.volumetricFlowRateOf(...)}.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class VolumetricFlowRateFactory
  extends AbstractQuantityFactory<VolumetricFlowRate>
{

  /** The unit of one litre per hour. */
  public static final Unit<VolumetricFlowRate> LITRE_PER_HOUR = LITRE.divide(
    HOUR
  ).asType(VolumetricFlowRate.class);

  /**
   * @param value      the numeric flow rate value
   * @param unitString the unit, in the format produced by
   *                   {@code Unit.toString()}
   */
  public VolumetricFlowRateFactory(
    final double value,
    final String unitString
  ) {
    super(value, unitString);
  }

  /**
   * @param value the numeric flow rate value
   * @param unit  the unit the value is expressed in
   */
  public VolumetricFlowRateFactory(
    final double value,
    final Unit<VolumetricFlowRate> unit
  ) {
    super(value, unit.toString());
  }

  @Override
  protected Class<VolumetricFlowRate> quantityClass() {
    return VolumetricFlowRate.class;
  }
}
