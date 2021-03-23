package uk.ac.ox.oxfish.biology;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SpeciesCodes {

    private final BiMap<String, String> bimap;

    /**
     * @param bimap A map from species code to species name.
     */
    public SpeciesCodes(Map<String, String> bimap) {
        this.bimap = ImmutableBiMap.copyOf(bimap);
    }

    public Set<String> getSpeciesNames() {
        return bimap.values();
    }

    @SuppressWarnings("unused")
    public Set<String> getSpeciesCodes() {
        return bimap.keySet();
    }

    public String getSpeciesName(String speciesCode) {
        return Optional.ofNullable(bimap.get(speciesCode))
            .orElseThrow(() -> new IllegalArgumentException("Unknown species name for code " + speciesCode));
    }

    public String getSpeciesCode(String speciesName) {
        return Optional.ofNullable(bimap.inverse().get(speciesName))
            .orElseThrow(() -> new IllegalArgumentException("Unknown species code for name " + speciesName));

    }

}
