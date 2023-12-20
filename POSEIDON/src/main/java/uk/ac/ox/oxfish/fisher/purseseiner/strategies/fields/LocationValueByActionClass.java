package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;

import java.util.Map;

public class LocationValueByActionClass {
    private final ImmutableMap<Class<? extends PurseSeinerAction>, ? extends LocationValues> locationValueByActionClass;

    LocationValueByActionClass(
        final Map<Class<? extends PurseSeinerAction>, ? extends LocationValues> locationValueByActionClass
    ) {
        this.locationValueByActionClass = ImmutableMap.copyOf(locationValueByActionClass);
    }

    LocationValues get(final Class<? extends PurseSeinerAction> actionClass) {
        return locationValueByActionClass.get(actionClass);
    }

    public Map<Class<? extends PurseSeinerAction>, ? extends LocationValues> asMap() {
        return locationValueByActionClass;
    }

}
