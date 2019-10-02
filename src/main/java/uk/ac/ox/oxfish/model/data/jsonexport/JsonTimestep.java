package uk.ac.ox.oxfish.model.data.jsonexport;

import java.util.List;

public class JsonTimestep {
    int timeInDays;
    List<JsonVesselPosition> vessels;

    public JsonTimestep(int timeInDays, List<JsonVesselPosition> vessels) {
        this.timeInDays = timeInDays;
        this.vessels = vessels;
    }

}
