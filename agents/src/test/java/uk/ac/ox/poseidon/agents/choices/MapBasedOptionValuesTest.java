package uk.ac.ox.poseidon.agents.choices;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapBasedOptionValuesTest {

    @Test
    void testGetBestEntriesWhenEmpty() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        assertThat(values.getBestEntries()).isEmpty();
    }

    @Test
    void testGetBestEntriesSingleEntry() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 10.0);
        assertThat(values.getBestEntries())
            .hasSize(1)
            .first()
            .satisfies(entry -> {
                assertThat(entry.getKey()).isEqualTo("A");
                assertThat(entry.getValue()).isEqualTo(10.0);
            });
    }

    @Test
    void testGetBestEntriesUniqueMax() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 10.0);
        values.observe("B", 20.0);
        values.observe("C", 15.0);
        assertThat(values.getBestEntries())
            .hasSize(1)
            .first()
            .satisfies(entry -> {
                assertThat(entry.getKey()).isEqualTo("B");
                assertThat(entry.getValue()).isEqualTo(20.0);
            });
    }

    @Test
    void testGetBestEntriesWithTie() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 20.0);
        values.observe("B", 20.0);
        values.observe("C", 10.0);
        assertThat(values.getBestEntries())
            .hasSize(2)
            .allSatisfy(entry -> assertThat(entry.getValue()).isEqualTo(20.0))
            .extracting(Map.Entry::getKey)
            .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void testGetBestEntriesAllEqual() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 5.0);
        values.observe("B", 5.0);
        values.observe("C", 5.0);
        assertThat(values.getBestEntries())
            .hasSize(3)
            .allSatisfy(entry -> assertThat(entry.getValue()).isEqualTo(5.0));
    }

    @Test
    void testGetBestEntriesCacheReuse() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 10.0);
        assertThat(values.getBestEntries())
            .isSameAs(values.getBestEntries());
    }

    @Test
    void testGetBestEntriesCacheInvalidation() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 10.0);
        values.observe("B", 20.0);

        assertThat(values.getBestEntries())
            .hasSize(1)
            .first()
            .satisfies(entry -> assertThat(entry.getKey()).isEqualTo("B"));

        values.observe("A", 30.0);

        assertThat(values.getBestEntries())
            .hasSize(2)
            .allSatisfy(entry -> assertThat(entry.getValue()).isEqualTo(20.0))
            .extracting(Map.Entry::getKey)
            .containsExactlyInAnyOrder("A", "B");
    }
}
