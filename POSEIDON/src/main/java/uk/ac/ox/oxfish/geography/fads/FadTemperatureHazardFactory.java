/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * it creates its own map to produce hazard from
 */
public class FadTemperatureHazardFactory implements AlgorithmFactory<FadTemperatureHazard> {


    private DoubleParameter minimumDaysBeforeHazardCanTakePlace = new FixedDoubleParameter(10);

    private DoubleParameter valueBelowWhichHazardHappens = new FixedDoubleParameter(25) ;

    private DoubleParameter hazardProbability = new FixedDoubleParameter(.2);


    @Override
    public FadTemperatureHazard apply(FishState fishState) {

        fishState.registerStartable( additionalMapFactory.apply(fishState));

        return new FadTemperatureHazard(
                minimumDaysBeforeHazardCanTakePlace.apply(fishState.getRandom()).intValue(),
                valueBelowWhichHazardHappens.apply(fishState.getRandom()).doubleValue(),
                hazardProbability.apply(fishState.getRandom()).doubleValue(),
                additionalMapFactory.getMapVariableName()

        );
    }

    AdditionalMapFactory additionalMapFactory = new AdditionalMapFactory();
    {
        additionalMapFactory.setGridFile(InputPath.of("inputs", "tests", "temperature.csv"));
        additionalMapFactory.setMapPeriod(365);
        additionalMapFactory.setMapVariableName("Temperature");
    }




    public DoubleParameter getMinimumDaysBeforeHazardCanTakePlace() {
        return minimumDaysBeforeHazardCanTakePlace;
    }

    public void setMinimumDaysBeforeHazardCanTakePlace(
            DoubleParameter minimumDaysBeforeHazardCanTakePlace) {
        this.minimumDaysBeforeHazardCanTakePlace = minimumDaysBeforeHazardCanTakePlace;
    }

    public DoubleParameter getValueBelowWhichHazardHappens() {
        return valueBelowWhichHazardHappens;
    }

    public void setValueBelowWhichHazardHappens(DoubleParameter valueBelowWhichHazardHappens) {
        this.valueBelowWhichHazardHappens = valueBelowWhichHazardHappens;
    }

    public DoubleParameter getHazardProbability() {
        return hazardProbability;
    }

    public void setHazardProbability(DoubleParameter hazardProbability) {
        this.hazardProbability = hazardProbability;
    }


    public InputPath getPathToMapFile() {
        return additionalMapFactory.getGridFile();
    }

    public void setPathToMapFile(InputPath pathToClorophillFile) {
        additionalMapFactory.setGridFile(pathToClorophillFile);
    }

    public int getMapPeriod() {
        return additionalMapFactory.getMapPeriod();
    }

    public void setMapPeriod(int mapPeriod) {
        additionalMapFactory.setMapPeriod(mapPeriod);
    }

    public String getMapVariableName() {
        return additionalMapFactory.getMapVariableName();
    }

    public void setMapVariableName(String mapVariableName) {
        additionalMapFactory.setMapVariableName(mapVariableName);
    }
}
