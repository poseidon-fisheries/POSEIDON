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

package uk.ac.ox.oxfish.model.scenario;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Splitter;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.AllocatedBiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesDerisoInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.event.AbundanceDrivenFixedExogenousCatches;
import uk.ac.ox.oxfish.model.event.BiomassDrivenFixedExogenousCatches;
import uk.ac.ox.oxfish.model.event.ExogenousCatches;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.gas.FixedGasFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by carrknight on 6/19/17.
 */
public class DerisoCaliforniaScenario extends CaliforniaAbstractScenario {



    private MultipleSpeciesDerisoInitializer initializer;


    private String derisoFileNames = "deriso_2007.yaml";

    private HashMap<String,String> movement = new HashMap<>();
    {
        movement.put("Sablefish","0.0001");
    }

    /**
     * build the biology part!
     *
     * @param model
     * @param folderMap
     * @return
     */
    @Override
    protected GlobalBiology buildBiology(
            FishState model, LinkedHashMap<String, Path> folderMap) {
        initializer = new MultipleSpeciesDerisoInitializer(folderMap,true);
        initializer.setDerisoYamlFileName(derisoFileNames);


        GlobalBiology biology = initializer.generateGlobal(model.getRandom(),
                                             model);

        LinkedHashMap<Species,Double>  recast = new LinkedHashMap<>();
        for (Map.Entry<String, String> exogenous : movement.entrySet()) {
            recast.put(biology.getSpecie(exogenous.getKey()),Double.parseDouble(exogenous.getValue()));
        }
        initializer.setMovementRate(recast);

        return biology;
    }

    @Override
    public AllocatedBiologyInitializer getBiologyInitializer() {
        return initializer;
    }

    @NotNull
    @Override
    protected ExogenousCatches turnIntoExogenousCatchesObject(
            LinkedHashMap<Species, Double> recast) {

            return new BiomassDrivenFixedExogenousCatches(recast);

    }


    {

        super.setMainDirectory(Paths.get("inputs","simple_california"));
        super.setUsePremadeInput(false);
    }

    /**
     * Getter for property 'movement'.
     *
     * @return Value for property 'movement'.
     */
    public HashMap<String, String> getMovement() {
        return movement;
    }

    /**
     * Setter for property 'movement'.
     *
     * @param movement Value to set for property 'movement'.
     */
    public void setMovement(HashMap<String, String> movement) {
        this.movement = movement;
    }


    /**
     * Getter for property 'derisoFileNames'.
     *
     * @return Value for property 'derisoFileNames'.
     */
    public String getDerisoFileNames() {
        return derisoFileNames;
    }

    /**
     * Setter for property 'derisoFileNames'.
     *
     * @param derisoFileNames Value to set for property 'derisoFileNames'.
     */
    public void setDerisoFileNames(String derisoFileNames) {
        this.derisoFileNames = derisoFileNames;
    }
}
