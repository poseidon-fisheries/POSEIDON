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

package uk.ac.ox.poseidon.core;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.time.DateFactory;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static uk.ac.ox.poseidon.core.scopes.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.utils.Factories.listOf;

class MappedFactoryTest {

    @Test
    void canUseZippedFactoryToGenerateDates() {
        assertEquals(
            List.of(
                LocalDate.of(2000, 1, 21),
                LocalDate.of(2000, 2, 22),
                LocalDate.of(2000, 3, 23)
            ),
            new MappedFactory<>(
                new DateFactory(2000, null, null),
                Map.of(
                    "month", listOf(1, 2, 3),
                    "day", listOf(21, 22, 23)
                )
            ).get(GLOBAL_SCOPE)
        );
    }

    @Test
    void appliesMappedPropertiesInEncounterOrderAndRestoresInReverseOrder() {
        final DateFactory originalDateFactory = new DateFactory(2000, 1, 1);
        final DateHolderFactory dateHolderFactory =
            new DateHolderFactory(originalDateFactory);
        final Map<String, Factory<? super Scope, ? extends List<?>>> mappedProperties =
            new LinkedHashMap<>();
        mappedProperties.put(
            "dateFactory",
            listOf(
                new DateFactory(1999, 1, 1),
                new DateFactory(1998, 2, 2)
            )
        );
        mappedProperties.put("dateFactory.year", listOf(2001, 2002));

        assertEquals(
            List.of(
                LocalDate.of(2001, 1, 1),
                LocalDate.of(2002, 2, 2)
            ),
            new MappedFactory<>(dateHolderFactory, mappedProperties).get(GLOBAL_SCOPE)
        );
        assertSame(originalDateFactory, dateHolderFactory.getDateFactory());
        assertEquals(2000, originalDateFactory.getYear());
    }

    public static class DateHolderFactory extends GlobalScopeFactory<LocalDate> {

        private DateFactory dateFactory;

        @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "This test fixture intentionally stores a nested mutable bean property."
        )
        public DateHolderFactory(final DateFactory dateFactory) {
            this.dateFactory = dateFactory;
        }

        @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "This test fixture intentionally exposes a nested mutable bean property."
        )
        public DateFactory getDateFactory() {
            return dateFactory;
        }

        @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "This test fixture intentionally stores a nested mutable bean property."
        )
        public void setDateFactory(final DateFactory dateFactory) {
            this.dateFactory = dateFactory;
        }

        @Override
        public int hashCode() {
            return dateFactory.hashCode();
        }

        @Override
        protected LocalDate newInstance(final Scope scope) {
            return dateFactory.get(scope);
        }
    }
}
