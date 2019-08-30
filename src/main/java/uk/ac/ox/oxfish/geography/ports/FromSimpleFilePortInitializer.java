package uk.ac.ox.oxfish.geography.ports;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;
import uk.ac.ox.oxfish.utility.parameters.PortReader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class FromSimpleFilePortInitializer implements PortInitializer {

    private final PortReader reader = new PortReader();
    private Path filePath;

    public FromSimpleFilePortInitializer(Path filePath) { this.filePath = filePath; }

    @Override public List<Port> buildPorts(
        NauticalMap map,
        MersenneTwisterFast mapmakerRandom,
        Function<SeaTile, MarketMap> marketFactory,
        FishState model,
        GasPriceMaker gasPriceMaker
    ) {
        final Collection<Port> ports =
            reader.readSimplePortFile(filePath, map, marketFactory, gasPriceMaker, model);
        ports.forEach(port -> {
            map.addPort(port);
            gasPriceMaker.start(port, model);
        });
        return new ArrayList<>(ports);
    }

    public Path getFilePath() { return filePath; }

    public void setFilePath(Path filePath) { this.filePath = filePath; }
}
