package uk.ac.ox.oxfish.model.data;

/**
 * Any object that wants to write a file out at the end of the simulation can register as a OutputPlugin with the state
 * and the YamlMain will output it before closing the simulation
 * Created by carrknight on 1/10/17.
 */
public interface OutputPlugin {


    public String getFileName();

    public String composeFileContents();

}
