package uk.ac.ox.poseidon.agents.choices;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReusableOptionValuesTest {

    @Test
    void forEachBestEntryOnlyEmitsTrueMaximum() {
        final ReusableOptionValues<String> values = new ReusableOptionValues<>();
        // Enough distinct entries, in an order unrelated to the map's hash-bucket iteration
        // order, that a single-pass implementation which forgets to retract an
        // entry once a higher value is found later (the original bug) would very
        // likely emit some non-maximal entry alongside the true maximum.
        for (int i = 0; i < 20; i++) {
            values.putIfGreater("option-" + i, i);
        }

        final List<Map.Entry<String, Double>> emitted = new ArrayList<>();
        values.forEachBestEntry((key, value) -> emitted.add(Map.entry(key, value)));

        assertThat(emitted)
            .as("only the entry with the true maximum value should be emitted")
            .containsExactly(Map.entry("option-19", 19.0));
    }

    @Test
    void forEachBestEntryEmitsEveryEntryTiedForMaximum() {
        final ReusableOptionValues<String> values = new ReusableOptionValues<>();
        values.putIfGreater("A", 5.0);
        values.putIfGreater("B", 10.0);
        values.putIfGreater("C", 10.0);
        values.putIfGreater("D", 3.0);

        final List<String> emittedKeys = new ArrayList<>();
        values.forEachBestEntry((key, value) -> emittedKeys.add(key));

        assertThat(emittedKeys).containsExactlyInAnyOrder("B", "C");
    }

    @Test
    void forEachBestEntryOnEmptyValuesEmitsNothing() {
        final ReusableOptionValues<String> values = new ReusableOptionValues<>();

        final List<String> emittedKeys = new ArrayList<>();
        values.forEachBestEntry((key, value) -> emittedKeys.add(key));

        assertThat(emittedKeys).isEmpty();
    }
}
