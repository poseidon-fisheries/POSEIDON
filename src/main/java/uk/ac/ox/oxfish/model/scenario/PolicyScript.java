package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * A bunch of factories to apply to a model, usually to change its policies or something similar
 * Created by carrknight on 5/3/16.
 */
public class PolicyScript
{

    private AlgorithmFactory<? extends Regulation> regulation = null;

    private AlgorithmFactory<? extends Gear> gear = null;

    private Integer changeInNumberOfFishers = null;

    public PolicyScript() {
    }


    public void apply(FishState state)
    {

        //apply regulations
        if(regulation != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setRegulation(regulation.apply(state));
            }
            //new fishers will follow these new rules
            state.getFisherFactory().setRegulations(regulation);
        }
        //apply gear
        if(gear != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setGear(gear.apply(state));
            }
            //new fishers will use the new gear
            state.getFisherFactory().setGear(gear);
        }
        //create new fishers if needed
        if(changeInNumberOfFishers != null) {
            if(changeInNumberOfFishers>0)
                for (int i = 0; i < changeInNumberOfFishers; i++)
                    state.createFisher();
            else
            {
                for (int i = 0; i < -changeInNumberOfFishers; i++)
                    state.killRandomFisher();
            }
        }
    }

    public AlgorithmFactory<? extends Regulation> getRegulation() {
        return regulation;
    }

    public void setRegulation(
            AlgorithmFactory<? extends Regulation> regulation) {
        this.regulation = regulation;
    }

    public AlgorithmFactory<? extends Gear> getGear() {
        return gear;
    }

    public void setGear(AlgorithmFactory<? extends Gear> gear) {
        this.gear = gear;
    }

    public Integer getChangeInNumberOfFishers() {
        return changeInNumberOfFishers;
    }

    public void setChangeInNumberOfFishers(Integer changeInNumberOfFishers) {
        this.changeInNumberOfFishers = changeInNumberOfFishers;
    }


}
