package spikes;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.ac.ox.oxfish.model.data.jsonexport.JsonSeries;

import static com.google.common.collect.Lists.*;

public class JsonSeriesTest {
    public static void main(String[] args) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final JsonSeries series = new JsonSeries(newLinkedList(newArrayList(3d, 3d, 1d)), "A real name", null);
        System.out.println(gson.toJson(series));
    }
}
