/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.maximization.generic;

import uk.ac.ox.oxfish.model.FishState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AsymmetricFixedDataLastStepTargetFromFile implements DataTarget {


    private String pathToCsvFile;

    private final AsymmetricFixedDataLastStepTarget delegate = new AsymmetricFixedDataLastStepTarget();


    @Override
    public double computeError(FishState model) {


        try {
            List<String> strings = Files.readAllLines(Paths.get(pathToCsvFile));
            delegate.setFixedTarget(Double.parseDouble(
                    strings.get(strings.size()-1)));

        } catch (IOException e) {

            throw new RuntimeException("can't read " + pathToCsvFile +" because of " + e);
        }
        return delegate.computeError(model);
    }

    public AsymmetricFixedDataLastStepTargetFromFile() {
    }


    public String getPathToCsvFile() {
        return pathToCsvFile;
    }

    public void setPathToCsvFile(String pathToCsvFile) {
        this.pathToCsvFile = pathToCsvFile;
    }

    public String getYearlyDataColumnName() {
        return delegate.getColumnName();
    }

    public void setYearlyDataColumnName(String columnName) {
        delegate.setColumnName(columnName);
    }


    /**
     * Getter for property 'exponentAbove'.
     *
     * @return Value for property 'exponentAbove'.
     */
    public double getExponentAbove() {
        return delegate.getExponentAbove();
    }

    /**
     * Setter for property 'exponentAbove'.
     *
     * @param exponentAbove Value to set for property 'exponentAbove'.
     */
    public void setExponentAbove(double exponentAbove) {
        delegate.setExponentAbove(exponentAbove);
    }

    /**
     * Getter for property 'exponentBelow'.
     *
     * @return Value for property 'exponentBelow'.
     */
    public double getExponentBelow() {
        return delegate.getExponentBelow();
    }

    /**
     * Setter for property 'exponentBelow'.
     *
     * @param exponentBelow Value to set for property 'exponentBelow'.
     */
    public void setExponentBelow(double exponentBelow) {
        delegate.setExponentBelow(exponentBelow);
    }
}
