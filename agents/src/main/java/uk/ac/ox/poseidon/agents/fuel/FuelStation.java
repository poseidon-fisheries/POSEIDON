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

package uk.ac.ox.poseidon.agents.fuel;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.NonNull;
import lombok.Value;
import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.vessels.engines.FuelTank;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.math.RoundingMode.CEILING;

@Data
public class FuelStation {

    private final @NonNull Port port;
    private final @NonNull String code;
    private final @NonNull EventManager eventManager;
    private @NonNull Money pricePerLitre;
    private double pumpRateInLitresPerMinute;

    public FuelStation(
        final @NonNull Port port,
        final @NonNull String code,
        final @NonNull EventManager eventManager,
        final @NonNull Money pricePerLitre,
        final double pumpRateInLitresPerMinute
    ) {
        this.port = checkNotNull(port, "port must not be null");
        this.code = checkNotNull(code, "code must not be null");
        this.eventManager = checkNotNull(eventManager, "eventManager must not be null");
        this.pricePerLitre = checkNotNull(pricePerLitre, "pricePerLitre must not be null");
        Preconditions.checkArgument(
            !pricePerLitre.isNegative(),
            "pricePerLitre must be non-negative"
        );
        Preconditions.checkArgument(
            pumpRateInLitresPerMinute > 0.0,
            "pumpRateInLitresPerMinute must be positive"
        );
        this.pumpRateInLitresPerMinute = pumpRateInLitresPerMinute;
    }

    public Purchase refill(final FuelTank fuelTank) {
        final double litresToAdd =
            fuelTank.getCapacityInLitres() - fuelTank.getCurrentFuelInLitres();
        fuelTank.addFuel(litresToAdd);
        return new Purchase(
            litresToAdd,
            priceFor(litresToAdd),
            durationFor(litresToAdd)
        );
    }

    @Value
    public static class Purchase {
        double litres;
        Money price;
        Duration duration;
    }

    public Duration durationFor(final double litres) {
        final double seconds = litres * 60.0 / pumpRateInLitresPerMinute;
        final long durationInSeconds = (long) Math.ceil(seconds);
        return Duration.ofSeconds(durationInSeconds);
    }

    public Money priceFor(final double litres) {
        return pricePerLitre.multipliedBy(litres, CEILING);
    }

}
