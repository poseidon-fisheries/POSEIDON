/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.network.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.NetworkPredicate;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.List;

/**
 * if "from" has one of these tags, then "to" has to have it in at least one case.
 *
 * So for example if the tags are "A" and "B" then:
 *
 *  nothing ---> "A" is okay
 *  nothing ---> nothing is okay
 *  "A" ----> "A" is okay
 *  "A","B" ---> "A" is okay
 *  "B" ---> "A" is not okay
 *  "B" ---> nothing is not okay
 */
public class MustShareTag implements AlgorithmFactory<NetworkPredicate> {


    private String mustShareOneOfThese = "population0,population1";

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public NetworkPredicate apply(FishState state) {


        final List<String> tags = Splitter.on(",").splitToList(mustShareOneOfThese);

        Preconditions.checkArgument(tags.size()>0,"No valid tags for network predicate");

        return new NetworkPredicate() {
            @Override
            public boolean test(Fisher from, Fisher to) {

                //empty is easy
                if(from.getTags().isEmpty())
                    return true;


                boolean atLeastOneMismatch = false;


              // go through all the tags
                for(String tag : tags)
                {
                    //if there is a match you are done
                    if(from.getTags().contains(tag))
                        if(to.getTags().contains(tag))
                        {
                            return true;
                        }
                    else{
                        //otherwise remember there was a mismatch
                        atLeastOneMismatch = true;
                        }


                }
                //if there was a mismatch and no matches, you are done!
                if(atLeastOneMismatch)
                 return false;
                else
                    return true;

            }
        };
    }


    /**
     * Getter for property 'mustShareOneOfThese'.
     *
     * @return Value for property 'mustShareOneOfThese'.
     */
    public String getMustShareOneOfThese() {
        return mustShareOneOfThese;
    }

    /**
     * Setter for property 'mustShareOneOfThese'.
     *
     * @param mustShareOneOfThese Value to set for property 'mustShareOneOfThese'.
     */
    public void setMustShareOneOfThese(String mustShareOneOfThese) {
        this.mustShareOneOfThese = mustShareOneOfThese;
    }
}
