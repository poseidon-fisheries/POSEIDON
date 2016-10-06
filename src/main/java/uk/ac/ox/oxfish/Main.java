package uk.ac.ox.oxfish;


import com.esotericsoftware.minlog.Log;
import com.google.common.io.Files;
import sim.display.Console;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.ScenarioSelector;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;


class Main{


    public static final long SEED = 0;

    //main
    public static void main(String[] args) throws IOException {


        //this is relatively messy, as all GUI functions are
        //basically it creates a widget to choose the scenario object and its parameters
        //once that's done you create a new Fisherstate and give it the  scenario
        //the you pass the FisherState to the FishGui and the model starts.


        final boolean[] instantiate = {false};

        final JDialog scenarioSelection = new JDialog((JFrame)null,true);
        final ScenarioSelector scenarioSelector = new ScenarioSelector();
        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(scenarioSelector,BorderLayout.CENTER);
        //create ok and exit button
        Box buttonBox = new Box( BoxLayout.LINE_AXIS);
        contentPane.add(buttonBox,BorderLayout.SOUTH);
        final JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            instantiate[0] = true;
            scenarioSelection.dispatchEvent(new WindowEvent(
                    scenarioSelection, WindowEvent.WINDOW_CLOSING
            ));
        });
        buttonBox.add(ok);
        final JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> System.exit(0));
        buttonBox.add(cancel);


        //create file opener (for YAML)
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        Log.info("current directory: " + Paths.get(".").toFile());
        chooser.setCurrentDirectory(Paths.get(".").toFile());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) //you can open directories
                    return true;
                String extension = Files.getFileExtension(f.getAbsolutePath()).trim().toLowerCase();
                if(extension.equals("yaml") || extension.equals("yml"))
                    return true;
                return false;
            }

            @Override
            public String getDescription() {
                return "Any YAML scenario";
            }
        });


        final JButton readFromFileButton = new JButton("Open scenario from file");
        readFromFileButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {


                        if (chooser.showOpenDialog(scenarioSelector) == JFileChooser.APPROVE_OPTION)
                        {
                            File file = chooser.getSelectedFile();
                            //log that you are about to write
                            Log.info("opened file " + file);
                            FishYAML yaml = new FishYAML();
                            try{
                                //read yaml
                                Scenario scenario = yaml.loadAs(
                                        String.join("\n", java.nio.file.Files.readAllLines(file.toPath())),
                                        Scenario.class);
                                //add it to the swing
                                SwingUtilities.invokeLater(() -> {
                                    if(scenarioSelector.hasScenario("yaml"))
                                        scenarioSelector.removeScenarioOption("yaml");
                                    scenarioSelector.addScenarioOption("yaml",scenario);
                                    scenarioSelector.select("yaml");
                                    scenarioSelector.repaint();
                                });

                            }
                            catch (Exception yamlError)
                            {
                                Log.warn(yamlError.getMessage());
                                Log.warn(file + " is not a valid YAML scenario!");
                            }
                        } else {
                            Log.info("open file cancelled");
                        }
                    }
                }


        );
        final JButton writeToFileButton = new JButton("Save scenario to file");
        writeToFileButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {


                        if (chooser.showSaveDialog(scenarioSelector) == JFileChooser.APPROVE_OPTION)
                        {
                            File file = chooser.getSelectedFile();
                            String currentExtension = FishStateUtilities.getFilenameExtension(file);
                            //if the extension is not correct
                            if(!(currentExtension.equalsIgnoreCase("yaml") | currentExtension.equalsIgnoreCase("yml") ))
                            {
                                //force it!
                                file = new File(file.toString() + ".yaml");
                            }

                            //log that you are about to write
                            Log.info("going to save config to file " + file);
                            FishYAML yaml = new FishYAML();
                            String toWrite = yaml.dump(scenarioSelector.getScenario());
                            try {
                                Files.write(toWrite.getBytes(),file);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                                Log.error("Failed to write to " + file);
                                Log.error(e1.getMessage());
                            }
                        } else {
                            Log.info("save cancelled");
                        }
                    }
                }


        );
        buttonBox.add(readFromFileButton);
        buttonBox.add(writeToFileButton);

        final JButton restoreButton = new JButton("Restore saved model");
        restoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                assert SwingUtilities.isEventDispatchThread();
                System.out.println(SwingUtilities.isEventDispatchThread());
                FishState state = new FishState(SEED, 1);
                FishGUI vid = new FishGUI(state);
                Console c = new Console(vid);
                scenarioSelection.setEnabled(false);
                c.doOpen();
                c.setVisible(true);

                scenarioSelection.dispatchEvent(new WindowEvent(
                        scenarioSelection,WindowEvent.WINDOW_CLOSING
                ));
            }
        });

        buttonBox.add(restoreButton);
        scenarioSelection.setContentPane(contentPane);
        scenarioSelection.pack();
        scenarioSelection.setVisible(true);

        if(instantiate[0]==true) {
            FishState state = new FishState(SEED, 1);
            Log.set(Log.LEVEL_INFO);
            Log.setLogger(new FishStateLogger(state, Paths.get("log.txt")));



            state.setScenario(scenarioSelector.getScenario());

            state.attachAdditionalGatherers();

            FishGUI vid = new FishGUI(state);
            Console c = new Console(vid);
            c.setVisible(true);
        }

    }




}
