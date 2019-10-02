package uk.ac.ox.oxfish.model.data.jsonexport;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.OutputPlugin;
import uk.ac.ox.oxfish.utility.MasonUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.ac.ox.oxfish.model.data.jsonexport.JsonExportUtils.seaTileHeight;
import static uk.ac.ox.oxfish.model.data.jsonexport.JsonExportUtils.seaTileWidth;

public class JsonRegionsManager implements OutputPlugin, AdditionalStartable {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String fileName;
    private JsonRegions jsonRegions;

    public JsonRegionsManager(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void reactToEndOfSimulation(FishState state) {
        jsonRegions = makeRegionsFromMap(state.getMap());
    }

    JsonRegions makeRegionsFromMap(NauticalMap map) {
        final double w = seaTileWidth(map) / 2.0;
        final double h = seaTileHeight(map) / 2.0;
        Function<SeaTile, JsonRegion> seaTileToRegion = seaTile -> {
            final Coordinate c = map.getCoordinates(seaTile);
            return new JsonRegion(ImmutableList.of(
                new JsonRegionVertex(c.x - w, c.y - h),
                new JsonRegionVertex(c.x + w, c.y - h),
                new JsonRegionVertex(c.x + w, c.y + h),
                new JsonRegionVertex(c.x - w, c.y + h)
            ));
        };
        final List<JsonRegion> regions =
            MasonUtils.<SeaTile>bagToStream(map.getAllSeaTiles())
                .filter(seaTile -> seaTile.isWater() || map.isCoastal(seaTile))
                .filter(SeaTile::isProtected)
                .map(seaTileToRegion)
                .collect(Collectors.toList());
        return new JsonRegions(regions);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String composeFileContents() {
        return gson.toJson(jsonRegions);
    }

    @Override
    public void start(FishState model) {
        model.getOutputPlugins().add(this);
    }

    @Override
    public void turnOff() { }
}
