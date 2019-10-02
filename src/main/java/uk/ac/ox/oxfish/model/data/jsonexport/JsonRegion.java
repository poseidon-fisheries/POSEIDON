package uk.ac.ox.oxfish.model.data.jsonexport;

import java.util.List;

public class JsonRegion {
    List<JsonRegionVertex> vertices;

    public JsonRegion(List<JsonRegionVertex> vertices) {
        this.vertices = vertices;
    }
}
