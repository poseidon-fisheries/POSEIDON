/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FixedMap;

import java.util.Collection;
import java.util.Map;

/**
 * Returns 1.0 to everything
 * Created by carrknight on 5/26/16.
 */
public class EverywhereTrueExtractor implements SafetyFeatureExtractor<SeaTile>, LegalityFeatureExtractor<SeaTile>,
        SocialAcceptabilityFeatureExtractor<SeaTile>
{

    /**
     * Returns a map where everything is assigned value 1 (safe)
     */
    @Override
    public Map<SeaTile, Double> extractFeature(
            Collection<SeaTile> toRepresent,
            FishState model, Fisher fisher)
    {



        return new FixedMap<>(1.0, toRepresent);

    }
}