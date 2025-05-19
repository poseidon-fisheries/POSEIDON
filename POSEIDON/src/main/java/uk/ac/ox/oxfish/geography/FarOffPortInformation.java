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

package uk.ac.ox.oxfish.geography;


import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * an object containing basic info about where a far off port ought to be without
 */
public class FarOffPortInformation {

    private String portName = "FarOffPort";

    private int exitGridX = 5;

    private int exitGridY = 5;

    private double distanceFromExitInKm = 100;

    private double gasPriceAtPort = 0.1;

    /**
     * needs to keep track of this until it is initialized
     */
    private AlgorithmFactory<? extends Market> marketMaker = new FixedPriceMarketFactory();

    public AlgorithmFactory<? extends Market> getMarketMaker() {
        return marketMaker;
    }

    public void setMarketMaker(AlgorithmFactory<? extends Market> marketMaker) {
        this.marketMaker = marketMaker;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public int getExitGridX() {
        return exitGridX;
    }

    public void setExitGridX(int exitGridX) {
        this.exitGridX = exitGridX;
    }

    public int getExitGridY() {
        return exitGridY;
    }

    public void setExitGridY(int exitGridY) {
        this.exitGridY = exitGridY;
    }

    public double getDistanceFromExitInKm() {
        return distanceFromExitInKm;
    }

    public void setDistanceFromExitInKm(double distanceFromExitInKm) {
        this.distanceFromExitInKm = distanceFromExitInKm;
    }

    public double getGasPriceAtPort() {
        return gasPriceAtPort;
    }

    public void setGasPriceAtPort(double gasPriceAtPort) {
        this.gasPriceAtPort = gasPriceAtPort;
    }
}
