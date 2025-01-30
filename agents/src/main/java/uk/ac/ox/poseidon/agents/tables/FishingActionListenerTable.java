/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.tables;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import uk.ac.ox.poseidon.agents.behaviours.fishing.FishingAction;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

public class FishingActionListenerTable extends GridActionListenerTable<FishingAction> {

    private final StringColumn speciesCode = StringColumn.create("species_code");
    private final DoubleColumn biomassCaught = DoubleColumn.create("biomass_caught");

    public FishingActionListenerTable(final GridExtent gridExtent) {
        super(FishingAction.class, gridExtent);
        get().addColumns(speciesCode, biomassCaught);
    }

    @Override
    public void receive(final FishingAction fishingAction) {
        super.receive(fishingAction);
        fishingAction.getFishCaught().getMap().forEach((species, content) -> {
            speciesCode.append(species.getCode());
            biomassCaught.append(content.asBiomass().asKg());
        });
    }

}
