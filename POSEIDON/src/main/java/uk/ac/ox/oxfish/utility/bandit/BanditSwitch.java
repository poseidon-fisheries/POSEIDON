/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.utility.bandit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

/**
 * Bandit algorithms work on a set of "arms". Each arm usually link to the index of something else
 * (for example in map discretization it links to the index of a group).
 * This little object keeps track of the link from arm to group and from group to arm
 * Created by carrknight on 12/1/16.
 */
public class BanditSwitch {

    /**
     * the index represents the "bandit arm" index, the number in the array at
     * that index represents the real group.
     */
    private final int[] armToGroup;


    /**
     * map where key is the real group and value is the arm associated with it
     */
    private final HashMap<Integer, Integer> groupToArm;


    /**
     * @param numberOfRealGroups
     * @param groupValidator
     */
    public BanditSwitch(
        int numberOfRealGroups,
        Predicate<Integer> groupValidator
    ) {

        //go through all the groups and store only the valid ones
        ArrayList<Integer> validGroups = new ArrayList<>();
        for (int i = 0; i < numberOfRealGroups; i++) {
            if (groupValidator.test(i))
                validGroups.add(i);
        }

        //associate valid real groups to arms and viceversa
        groupToArm = new HashMap<>();
        armToGroup = new int[validGroups.size()];
        for (int arm = 0; arm < armToGroup.length; arm++) {
            Integer realGroup = validGroups.get(arm);
            armToGroup[arm] = realGroup;
            groupToArm.put(realGroup, arm);
        }

    }

    /**
     * given the arm index, return the group index associated with it
     *
     * @param arm the arm index
     * @return the group index
     */
    public int getGroup(int arm) {
        return armToGroup[arm];
    }

    public int getNumberOfArms() {
        return armToGroup.length;

    }

    /**
     * checks if this group belongs to any arm
     *
     * @param group
     * @return
     */
    public boolean containsGroup(Integer group) {
        return getArm(group) != null;
    }

    /**
     * given the group index return the bandit arm associated with it
     *
     * @param group the group index
     * @return the arm that is associated with the group, or null if no arm is associated with the group
     */
    public Integer getArm(Integer group) {
        return groupToArm.get(group);
    }
}
