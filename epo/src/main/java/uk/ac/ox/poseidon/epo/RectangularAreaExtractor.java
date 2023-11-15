package uk.ac.ox.poseidon.epo;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.poseidon.regulations.core.conditions.InRectangularArea;
import uk.ac.ox.poseidon.regulations.api.ConditionalRegulations;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RectangularAreaExtractor implements RowProvider {
    private final Iterable<? extends Collection<?>> rows;

    @Override
    public List<String> getHeaders() {
        return ImmutableList.of("min_lon", "max_lon", "min_lat", "max_lat");
    }

    @Override
    public Iterable<? extends Collection<?>> getRows() {
        return rows;
    }

    RectangularAreaExtractor(FishState fishState) {
        this.rows =
            extract(fishState.getRegulations(), Regulations::getSubRegulations)
                .filter(ConditionalRegulations.class::isInstance)
                .map(r -> ((ConditionalRegulations) r).getCondition())
                .flatMap(c -> extract(c, Condition::getSubConditions))
                .filter(InRectangularArea.class::isInstance)
                .map(InRectangularArea.class::cast)
                .map(rect -> ImmutableList.of(
                    rect.getEnvelope().getMinX(),
                    rect.getEnvelope().getMaxX(),
                    rect.getEnvelope().getMinY(),
                    rect.getEnvelope().getMaxY()
                ))
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> extract(T obj, Function<T, ? extends Collection<T>> f) {
        return Stream.concat(
            Stream.of(obj),
            f.apply(obj)
                .stream()
                .flatMap(sub -> extract(sub, f))
        );
    }

}
