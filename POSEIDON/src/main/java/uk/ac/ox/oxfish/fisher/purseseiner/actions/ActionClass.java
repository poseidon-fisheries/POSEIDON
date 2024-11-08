package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.function.Function.identity;

/**
 * These enums are mostly used to convert strings to action classes when loading data from CSV
 * files, e.g:
 * <p>
 * {@code ActionClasses.valueOf(record.getString("action_type")).getActionClass()}
 * <p>
 * They could also be used to get the action class directly, e.g. {@code OFS.getActionClass()}
 * instead of writing {@code OpportunisticFadSetAction.class}.
 */
public enum ActionClass {

    FAD(FadSetAction.class),
    DEL(DolphinSetAction.class),
    NOA(NonAssociatedSetAction.class),
    OFS(OpportunisticFadSetAction.class),
    DPL(FadDeploymentAction.class);

    public static final Set<String> CODES =
        Arrays.stream(values())
            .map(ActionClass::name)
            .collect(toImmutableSet());

    public static final Map<Class<? extends PurseSeinerAction>, ActionClass> classMap =
        Arrays.stream(values()).collect(toImmutableMap(
            ActionClass::getActionClass,
            identity()
        ));
    private final Class<? extends PurseSeinerAction> actionClass;

    ActionClass(final Class<? extends PurseSeinerAction> actionClass) {
        this.actionClass = actionClass;
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends AbstractSetAction> getSetActionClass(final String setActionCode) {
        return Optional
            .of(ActionClass.valueOf(setActionCode.toUpperCase()).getActionClass())
            .filter(AbstractSetAction.class::isAssignableFrom)
            .map(clazz -> (Class<? extends AbstractSetAction>) clazz)
            .orElseThrow(() -> new IllegalStateException(
                "Unknown set action code: " + setActionCode));
    }

    public Class<? extends PurseSeinerAction> getActionClass() {
        return actionClass;
    }

}
