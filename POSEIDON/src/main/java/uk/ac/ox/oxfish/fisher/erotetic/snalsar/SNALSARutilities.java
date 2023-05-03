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

/**
 * Created by carrknight on 5/26/16.
 */
public class SNALSARutilities
{

    private SNALSARutilities() {}

    /**
     * whether a choice is safe or not.
     */
    public static String SAFE_FEATURE = "Safe Feature";

    /**
     * Expected (?) profits at location
     */
    public static final String PROFIT_FEATURE = "Profit Feature";

    /**
     *  whether it is legal or not to fish here. By default as long as it is above 0 it is considered legal
     */
    public static final String LEGAL_FEATURE = "Legal Feature";


    /**
     * whether it is socially appropriate or not to fish here. By default as long as it is above 0 it is considered appropriate
     */
    public static final String SOCIALLY_APPROPRIATE_FEATURE = "Socially Appropriate Feature";



    /**
     * threshold below which profits are usually considered a failure
     */
    public static final String FAILURE_THRESHOLD = "Non-Failure Threshold Feature";



    /**
     * threshold above which profits are considered acceptable
     */
    public static final String ACCEPTABLE_THRESHOLD = "Acceptable Threshold Feature";




}
