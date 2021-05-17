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

package uk.ac.ox.oxfish.model.regs;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * turns on and off all the agents given an allocation policy. THIS IS NOT A REGULATION, this is a steppable that assumes
 * the agents have as regulation an "off-switch" regulation
 */
public class OnOffSwitchRegulator implements AdditionalStartable, Steppable {



    private Stoppable receipt;


    private PermitAllocationPolicy allocationPolicy;

    /**
     * a list of tags that denote those who participate in the fishery;
     * if empty, everybody is in.
     */
    private final Collection<String> tagsOfParticipants;


    public OnOffSwitchRegulator(
            PermitAllocationPolicy allocationPolicy,
            Collection<String> tagsOfParticipants) {
        this.allocationPolicy = allocationPolicy;
        this.tagsOfParticipants = tagsOfParticipants;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

        Preconditions.checkArgument(receipt==null, "Already started!");

        //should start every year, first day of the year.
        model.scheduleOnceInXDays(
                new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        receipt = model.scheduleEveryYear(OnOffSwitchRegulator.this::step, StepOrder.DAWN);
                    }
                }
                , StepOrder.DAWN, 1);

        step(model);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

        if(receipt!=null)
            receipt.stop();

    }


    @Override
    public void step(SimState simState) {

        FishState state = (FishState)simState;

        List<Fisher> participants = new ArrayList<>();

        //add all fishers who match at least one tag
        fisherloop:
        for (Fisher fisher : state.getFishers()) {

            if(tagsOfParticipants.isEmpty()) {
                assert  fisher.getRegulation() instanceof OffSwitchDecorator;
                participants.add(fisher);
            }
            else{
                for (String validTag : tagsOfParticipants) {
                    if(fisher.getTags().contains(validTag))
                    {
                        assert fisher.getRegulation() instanceof OffSwitchDecorator;
                        participants.add(fisher);
                        continue  fisherloop;
                    }
                }
            }



        }

        List<Fisher> allowedFishers = allocationPolicy.computeWhichFishersAreAllowed(participants,
                                                                              state);

        for (Fisher allowedFisher : allowedFishers) {
            ((OffSwitchDecorator) allowedFisher.getRegulation()).setTurnedOff(false);

        }
        participants.removeAll(allowedFishers);
        for(Fisher notAllowedFisher : participants)
            ((OffSwitchDecorator) notAllowedFisher.getRegulation()).setTurnedOff(true);



    }

    /**
     * Getter for property 'allocationPolicy'.
     *
     * @return Value for property 'allocationPolicy'.
     */
    public PermitAllocationPolicy getAllocationPolicy() {
        return allocationPolicy;
    }

    /**
     * Setter for property 'allocationPolicy'.
     *
     * @param allocationPolicy Value to set for property 'allocationPolicy'.
     */
    public void setAllocationPolicy(PermitAllocationPolicy allocationPolicy) {
        this.allocationPolicy = allocationPolicy;
    }

    /**
     * Getter for property 'tagsOfParticipants'.
     *
     * @return Value for property 'tagsOfParticipants'.
     */
    public Collection<String> getTagsOfParticipants() {
        return tagsOfParticipants;
    }
}
