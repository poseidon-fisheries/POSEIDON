import sim.display.Console;
import sim.engine.Schedule;
import uk.ac.ox.oxfish.gui.FishGUI;

class Main{

    public static void main(String[] args)
    {

        FishGUI vid = new FishGUI();
        Console c = new Console(vid);

        c.setVisible(true);
    }
}
