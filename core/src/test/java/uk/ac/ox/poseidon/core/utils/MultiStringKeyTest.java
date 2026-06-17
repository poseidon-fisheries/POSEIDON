package uk.ac.ox.poseidon.core.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MultiStringKeyTest {

    @Test
    void joinsMultipleFieldsWithSeparator() {
        assertThat(Utils.multiStringKey("a", "b", "c")).isEqualTo("a;b;c");
    }

    @Test
    void singleFieldHasNoSeparator() {
        assertThat(Utils.multiStringKey("a")).isEqualTo("a");
    }

    @Test
    void emptyVarargsReturnsEmptyString() {
        assertThat(Utils.multiStringKey()).isEqualTo("");
    }

    @Test
    void nullFieldContributesOnlyItsSeparator() {
        assertThat(Utils.multiStringKey("a", null, "b")).isEqualTo("a;;b");
    }

    @Test
    void allNullFieldsProduceOnlySeparators() {
        assertThat(Utils.multiStringKey(null, null, null)).isEqualTo(";;");
    }

    @Test
    void leadingNullProducesLeadingSeparator() {
        assertThat(Utils.multiStringKey(null, "a")).isEqualTo(";a");
    }

    @Test
    void emptyStringIsTreatedAsAbsent() {
        assertThat(Utils.multiStringKey("a", "", "b")).isEqualTo("a;;b");
    }

    @Test
    void naIsTreatedAsAbsent() {
        assertThat(Utils.multiStringKey("a", "NA", "b")).isEqualTo("a;;b");
    }

    @Test
    void naIsCaseInsensitive() {
        assertThat(Utils.multiStringKey("a", "na", "b")).isEqualTo("a;;b");
    }

    @Test
    void trimsValues() {
        assertThat(Utils.multiStringKey("  a  ", "\tb\t")).isEqualTo("a;b");
    }

    @Test
    void rejectsSeparatorInValue() {
        assertThatThrownBy(() -> Utils.multiStringKey("a;b"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(";");
    }

    @Test
    void rejectsClosingParenInValue() {
        assertThatThrownBy(() -> Utils.multiStringKey("a)b"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(")");
    }

    @Test
    void validatesAllFieldsBeforeAppending() {
        assertThatThrownBy(() -> Utils.multiStringKey("valid", "also;pipe"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fieldWithOnlyWhitespaceIsAbsent() {
        assertThat(Utils.multiStringKey("a", "   ", "b")).isEqualTo("a;;b");
    }
}
