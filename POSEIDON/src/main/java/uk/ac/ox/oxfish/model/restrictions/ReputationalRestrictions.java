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

package uk.ac.ox.oxfish.model.restrictions;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.Territory;
import uk.ac.ox.oxfish.fisher.strategies.destination.GeneralizedCognitiveStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;
import java.util.List;

public class ReputationalRestrictions implements Restriction {

    List<Territory> territory = new ArrayList<Territory>();

    public List<Territory> getTerritory() {
        return territory;
    }

    public int countTerritory() {
        return territory.size();
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        // TODO Auto-generated method stub

    }

    @Override
    public void turnOff(Fisher fisher) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model) {
        boolean canFishHere = true;
        //If it is NOT my territory and it IS someone else's then return false
        if (agent.getDestinationStrategy() instanceof GeneralizedCognitiveStrategy) {
            GeneralizedCognitiveStrategy destStrat = (GeneralizedCognitiveStrategy) agent.getDestinationStrategy();
            if (!isTerritory(tile)) {
                for (Fisher fisher : model.getFishers()) {
                    if (fisher != agent) {
                        if (fisher.isTerritory(tile)) {
                            canFishHere = false;
                            break;
                        }
                    }
                }
            }
        }
        return canFishHere;
    }

    public boolean isTerritory(SeaTile site) {
        boolean isTerritory = false;
        for (Territory territorySite : territory) {
            SeaTile territorySeaTile = territorySite.getLocation();
            if (territorySeaTile == site) {
                isTerritory = true;
                break;
            }
        }
        return isTerritory;
    }

    public void addTerritories(
        NauticalMap map,
        MersenneTwisterFast random,
        int nSites,
        int ulX,
        int ulY,
        int brX,
        int brY
    ) {
        for (int i = 0; i < nSites; ) {
            int seaTileX = random.nextInt(brX - ulX) + ulX;
            int seaTileY = random.nextInt(brY - ulY) + ulY;
            SeaTile potentialSite = map.getSeaTile(seaTileX, seaTileY);
            if (potentialSite.isWater() && !isSeaTileAlreadyTerritory(potentialSite)) {
                territory.add(new Territory(potentialSite));
                i++;
            }
        }
    }

    private boolean isSeaTileAlreadyTerritory(SeaTile site) {
        if (territory.isEmpty()) {
            return false;
        } else {
            for (Territory territorySite : territory) {
                SeaTile territorySeaTile = territorySite.getLocation();
                if (territorySeaTile == site) {
                    return true;
                }
            }
        }
        return false;
    }


    public void addTerritories(NauticalMap map, MersenneTwisterFast random, int nSites) {
        for (int i = 0; i < nSites; i++) {
            SeaTile potentialSite = map.getRandomBelowWaterLineSeaTile(random);
            boolean repeat = false;
            if (!territory.isEmpty()) {
                for (Territory territorySite : territory) {
                    SeaTile territorySeaTile = territorySite.getLocation();
                    if (territorySeaTile == potentialSite) {
                        repeat = true;
                        break;
                    }
                }
                if (!repeat) territory.add(new Territory(potentialSite));
            }
        }
    }
}
