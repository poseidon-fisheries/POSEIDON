package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class PurseSeinerActionClassToDouble implements ToDoubleFunction<Class<? extends PurseSeinerAction>> {
    private final Map<Class<? extends PurseSeinerAction>, Double> values;

    public PurseSeinerActionClassToDouble(Map<Class<? extends PurseSeinerAction>, Double> values) {
        this.values = ImmutableMap.copyOf(values);
    }

    public static PurseSeinerActionClassToDouble fromFile(Path path, String actionColumn, String valueColumn) {
        return new PurseSeinerActionClassToDouble(
            parseAllRecords(path)
                .stream()
                .collect(toImmutableMap(
                    r -> ActionClass.valueOf(r.getString(actionColumn)).getActionClass(),
                    r -> r.getDouble(valueColumn)
                ))
        );
    }

    @Override
    public String toString() {
        return values.toString();
    }

    public PurseSeinerActionClassToDouble mapValues(DoubleUnaryOperator doubleUnaryOperator) {
        return new PurseSeinerActionClassToDouble(
            values.entrySet().stream()
                .collect(toImmutableMap(
                    Map.Entry::getKey,
                    entry -> doubleUnaryOperator.applyAsDouble(entry.getValue())
                ))
        );
    }

    @Override
    public double applyAsDouble(Class<? extends PurseSeinerAction> purseSeinerActionClass) {
        return values.get(purseSeinerActionClass);
    }

}
