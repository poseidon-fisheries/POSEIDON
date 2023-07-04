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

package uk.ac.ox.oxfish.biology.boxcars;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ListChangeListener;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * class that keeps track of what is caught per bin in a subset of fishers where the fish subset is specified
 * with some probability
 */
public class StochasticCatchSampler implements ListChangeListener<Fisher>, CatchAtLengthSampler {


    /**
     * object that returns true whenever the fisher is to be sampled for
     */
    private final Predicate<Fisher> samplingSelector;


    private final CatchSample delegate;


    /**
     * here we keep all the fishers we sample when we are counting for abundance
     */
    private final List<Fisher> observedFishers = new LinkedList<>();


    private final Species species;

    /**
     * if this is not null, we are going to tag all the fishers we are surveying with this.
     */
    @Nullable
    private final String surveyTag;
    private FishState model;


    public StochasticCatchSampler(
        final Predicate<Fisher> samplingSelector,
        final Species species,
        @Nullable final String surveyTag
    ) {
        Preconditions.checkArgument(species != null);
        this.samplingSelector = samplingSelector;
        this.delegate = new CatchSample(
            species,
            new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()]
        );
        this.surveyTag = surveyTag;
        this.species = species;
    }

    @Override
    public void start(final FishState model) {
        Preconditions.checkArgument(this.model == null);
        this.model = model;
        checkWhichFisherToObserve(model);
        model.getFishers().addListener(this);
    }

    /**
     * builds the list of fishers to observe
     *
     * @param model the model
     */
    private void checkWhichFisherToObserve(final FishState model) {

        resetObservedFishers();

        for (final Fisher fisher : model.getFishers()) {
            checkIfAddFisherToSurvey(fisher);
        }


    }

    /**
     * clear the previous list of fishers
     */
    private void resetObservedFishers() {
        if (surveyTag != null) {
            final String totalTag = surveyTag + " " + species;
            for (final Fisher fisher : observedFishers) {
                fisher.getTags().remove(totalTag);
            }
        }
        observedFishers.clear();
    }

    private void checkIfAddFisherToSurvey(final Fisher fisher) {
        if (samplingSelector.test(fisher)) {
            if (surveyTag != null)
                fisher.getTags().add(surveyTag + " " + species);
            observedFishers.add(fisher);
        }
    }

    @Override
    public void turnOff() {
        resetObservedFishers();
        delegate.resetCatchObservations();
        model.getFishers().removeListener(this);
        this.model = null;

    }

    /**
     * supposedly what you'd step on: looks at all the fishers we are sampling and sum up all their landings and ADD it to the abundance vector.
     * Because fishers store their landings in weight, we need a function to turn them back into abundance. Here
     * we use the REAL weight function to do so
     */
    public void observeDaily() {

        delegate.observeDaily(observedFishers);

    }

    public void resetCatchObservations() {
        delegate.resetCatchObservations();
    }

    public double[][] getAbundance() {
        return delegate.getAbundance();
    }

    public double[][] getAbundance(final Function<Entry<Integer, Integer>, Double> subdivisionBinToWeightFunction) {
        return delegate.getAbundance(subdivisionBinToWeightFunction);
    }


    public Species getSpecies() {
        return delegate.getSpecies();
    }

    public double[][] getLandings() {
        return delegate.getLandings();
    }

    /**
     * Getter for property 'surveyTag'.
     *
     * @return Value for property 'surveyTag'.
     */
    @Nullable
    public String getSurveyTag() {
        return surveyTag;
    }


    @Override
    public void onChanged(final Change<? extends Fisher> c) {
        while (c.next()) {
            for (final Fisher newFisher : c.getAddedSubList()) {
                checkIfAddFisherToSurvey(newFisher);
            }
        }

    }


    /**
     * returns unmodifiable list showing fishers
     *
     * @return
     */
    public List<Fisher> viewObservedFishers() {
        return Collections.unmodifiableList(observedFishers);
    }
}
