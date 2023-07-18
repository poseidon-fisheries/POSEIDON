package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;

import java.util.Map;

public interface AbundanceFilters extends Map<Class<? extends AbstractSetAction>, Map<Species, NonMutatingArrayFilter>> {
}
