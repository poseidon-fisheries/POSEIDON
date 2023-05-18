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

package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.MonthlyDepartingDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Creates the monthly departing Strategy
 * Created by carrknight on 1/6/16.
 */
public class MonthlyDepartingFactory implements AlgorithmFactory<MonthlyDepartingDecorator> {


    private List<Integer> monthsNotGoingOut = new LinkedList<>();
    private AlgorithmFactory<? extends DepartingStrategy> delegate = new FixedRestTimeDepartingFactory();

    {
        monthsNotGoingOut.add(1);//jan
        monthsNotGoingOut.add(12);//dec
        monthsNotGoingOut.add(6); //jun
        monthsNotGoingOut.add(7); //july


    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MonthlyDepartingDecorator apply(FishState fishState) {
        boolean[] months = new boolean[12];
        for (int i = 0; i < 12; i++)
            months[i] = !monthsNotGoingOut.contains(i + 1);

        return new MonthlyDepartingDecorator(
            delegate.apply(fishState),
            months
        );

    }

    /**
     * Getter for property 'monthsNotGoingOut'.
     *
     * @return Value for property 'monthsNotGoingOut'.
     */
    public List<Integer> getMonthsNotGoingOut() {
        return monthsNotGoingOut;
    }

    /**
     * Setter for property 'monthsNotGoingOut'.
     *
     * @param monthsNotGoingOut Value to set for property 'monthsNotGoingOut'.
     */
    public void setMonthsNotGoingOut(List<Integer> monthsNotGoingOut) {
        this.monthsNotGoingOut = monthsNotGoingOut;
    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public AlgorithmFactory<? extends DepartingStrategy> getDelegate() {
        return delegate;
    }

    /**
     * Setter for property 'delegate'.
     *
     * @param delegate Value to set for property 'delegate'.
     */
    public void setDelegate(
        AlgorithmFactory<? extends DepartingStrategy> delegate
    ) {
        this.delegate = delegate;
    }
}
