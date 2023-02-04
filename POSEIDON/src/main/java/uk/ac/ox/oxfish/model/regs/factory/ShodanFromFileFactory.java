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

package uk.ac.ox.oxfish.model.regs.factory;

import burlap.behavior.valuefunction.QProvider;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.ExternalOpenCloseSeason;
import uk.ac.ox.oxfish.model.regs.ShodanController;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;

/**
 * Reads QPlanner from xml file and uses it to run the model
 * Created by carrknight on 1/3/17.
 */
public class ShodanFromFileFactory implements AlgorithmFactory<ExternalOpenCloseSeason>{



    private String pathToXml = "/home/carrknight/code/oxfish/inputs/biomassQ.xml";

    /**
     * locker where we keep a single controller per state
     */
    private final Locker<String,ShodanController> locker = new Locker<>();


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public ExternalOpenCloseSeason apply(FishState fishState) {


        ShodanController controller = locker.presentKey(
                fishState.getHopefullyUniqueID(),
                new Supplier<ShodanController>() {
                    @Override
                    public ShodanController get() {
                        //create regulation object
                        ExternalOpenCloseSeason season = new ExternalOpenCloseSeason();


                        //read q table from file

                        try {
                            XStream xstream = new XStream(new StaxDriver());
                            String xml = null;
                            byte[] saves = new byte[0];
                            saves = Files.readAllBytes(Paths.get(pathToXml));
                            xml = new String(saves);

                            //put it straight into the controller
                            ShodanController shodan =
                                    new ShodanController((QProvider) xstream.fromXML(xml), season);

                            //set controller to start
                            fishState.registerStartable(shodan);

                            //return it
                            return shodan;
                        } catch (IOException e) {
                            throw new RuntimeException("couldn't read/find the xml containing the Q-Provider");
                        }

                    }
                });

        return controller.getRegulation();
    }


    /**
     * Getter for property 'pathToXml'.
     *
     * @return Value for property 'pathToXml'.
     */
    public String getPathToXml() {
        return pathToXml;
    }

    /**
     * Setter for property 'pathToXml'.
     *
     * @param pathToXml Value to set for property 'pathToXml'.
     */
    public void setPathToXml(String pathToXml) {
        this.pathToXml = pathToXml;
    }
}
