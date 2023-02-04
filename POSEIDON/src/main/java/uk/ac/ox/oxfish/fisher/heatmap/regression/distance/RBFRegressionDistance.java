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

package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

/**
 * 1/rbfKernel
 * Created by carrknight on 8/24/16.
 */
public class RBFRegressionDistance implements RegressionDistance
{


    private final RBFDistance delegate;


    public RBFRegressionDistance(double bandwidth) {
        this.delegate = new RBFDistance(bandwidth);
    }


    @Override
    public double distance(double firstObservation, double secondObservation) {
        return delegate.distance(firstObservation, secondObservation);
    }

    public double getBandwidth() {
        return delegate.getBandwidth();
    }

    public void setBandwidth(double bandwidth) {
        delegate.setBandwidth(bandwidth);
    }
}
