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
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * turns on and off all the agents given an allocation policy. THIS IS NOT A REGULATION, this is a steppable that assumes
 * the agents have as regulation an "off-switch" regulation
 */
public class OnOffSwitchRegulator implements AdditionalStartable, Steppable {


    private static final long serialVersionUID = 5712743950451964367L;
    /**
     * a list of tags that denote those who participate in the fishery;
     * if empty, everybody is in.
     */
    private final Collection<String> tagsOfParticipants;
    private Stoppable receipt;
    private PermitAllocationPolicy allocationPolicy;


    public OnOffSwitchRegulator(
        final PermitAllocationPolicy allocationPolicy,
        final Collection<String> tagsOfParticipants
    ) {
        this.allocationPolicy = allocationPolicy;
        this.tagsOfParticipants = tagsOfParticipants;
    }

    /**
     * trawl through the startables in fishstate and turn off all the ones that are OnOffSwitchRegulators
     *
     * @param state
     */
    public static void turnOffAllSwitchRegulators(final FishState state) {
        for (final Startable viewStartable : state.viewStartables()) {
            if (viewStartable instanceof OnOffSwitchRegulator)
                if (((OnOffSwitchRegulator) viewStartable).isStarted())
                    viewStartable.turnOff();
        }
    }

    public boolean isStarted() {
        return receipt != null;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {

        Preconditions.checkArgument(receipt == null, "Already started!");

        //should start every year, first day of the year.
        model.scheduleOnceInXDays(
            (Steppable) simState -> receipt = model.scheduleEveryYear(OnOffSwitchRegulator.this::step, StepOrder.DAWN)
            , StepOrder.DAWN, 1);

        step(model);
    }

    @Override
    public void step(final SimState simState) {

        final FishState state = (FishState) simState;

        final List<Fisher> participants = new ArrayList<>();

        //add all fishers who match at least one tag
        fisherloop:
        for (final Fisher fisher : state.getFishers()) {

            if (tagsOfParticipants.isEmpty()) {
                assert fisher.getRegulation() instanceof OffSwitchDecorator;
                participants.add(fisher);
            } else {
                for (final String validTag : tagsOfParticipants) {
                    if (fisher.getTagsList().contains(validTag)) {
                        assert fisher.getRegulation() instanceof OffSwitchDecorator;
                        participants.add(fisher);
                        continue fisherloop;
                    }
                }
            }


        }

        final List<Fisher> allowedFishers = allocationPolicy.computeWhichFishersAreAllowed(
            participants,
            state
        );

        for (final Fisher allowedFisher : allowedFishers) {
            ((OffSwitchDecorator) allowedFisher.getRegulation()).setTurnedOff(false);

        }
        participants.removeAll(allowedFishers);
        for (final Fisher notAllowedFisher : participants)
            ((OffSwitchDecorator) notAllowedFisher.getRegulation()).setTurnedOff(true);


    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

        if (receipt != null)
            receipt.stop();

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
    public void setAllocationPolicy(final PermitAllocationPolicy allocationPolicy) {
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
