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
public class BanditSwitch
{

    /**
     * the index represents the "bandit arm" index, the number in the array at
     * that index represents the real group.
     */
    private final int[] armToGroup;


    /**
     * map where key is the real group and value is the arm associated with it
     */
    private final HashMap<Integer,Integer> groupToArm;


    /**
     *
     * @param numberOfRealGroups
     * @param groupValidator
     */
    public BanditSwitch(int numberOfRealGroups,
                        Predicate<Integer> groupValidator)
    {

        //go through all the groups and store only the valid ones
        ArrayList<Integer> validGroups = new ArrayList<>();
        for(int i = 0; i< numberOfRealGroups; i++)
        {
            if(groupValidator.test(i))
                validGroups.add(i);
        }

        //associate valid real groups to arms and viceversa
        groupToArm = new HashMap<>();
        armToGroup = new int[validGroups.size()];
        for(int arm=0; arm<armToGroup.length; arm++)
        {
            Integer realGroup = validGroups.get(arm);
            armToGroup[arm] = realGroup;
            groupToArm.put(realGroup, arm);
        }

    }


    /**
     * given the group index return the bandit arm associated with it
     * @param group the group index
     * @return the arm that is associated with the group, or null if no arm is associated with the group
     */
    public Integer getArm(Integer group){
        return groupToArm.get(group);
    }

    /**
     * given the arm index, return the group index associated with it
     * @param arm the arm index
     * @return the group index
     */
    public int getGroup(int arm)
    {
        return armToGroup[arm];
    }

    public int getNumberOfArms(){
        return armToGroup.length;

    }
}
