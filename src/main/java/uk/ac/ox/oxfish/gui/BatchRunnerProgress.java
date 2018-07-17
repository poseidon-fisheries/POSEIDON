/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.gui;

import uk.ac.ox.oxfish.model.BatchRunner;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by carrknight on 9/25/16.
 */
public class BatchRunnerProgress extends JPanel implements PropertyChangeListener {



    private final BatchRunner runner;

    private final int  numberOfRuns;

    private final JTextArea taskOutput;

    private final JProgressBar progressBar;

    private final Task task;

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
    public BatchRunnerProgress(BatchRunner runner,
                               int numberOfRuns) {
        this.runner = runner;
        this.numberOfRuns = numberOfRuns;


        progressBar = new JProgressBar(0, numberOfRuns);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);

        //JPanel panel = new JPanel();
        this.setLayout(new BorderLayout());
        this.add(progressBar, BorderLayout.NORTH);

        //add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        task = new Task();
        task.addPropertyChangeListener(this);
      //  task.execute();
        this.setPreferredSize(new Dimension(800,600));

    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);

        }
        taskOutput.repaint();
        this.repaint();

    }


    class Task extends SwingWorker<StringBuffer,Void>{




        /**
         * Computes a result, or throws an exception if unable to do so.
         * <p>
         * <p>
         * Note that this method is executed only once.
         * <p>
         * <p>
         * Note: this method is executed in a background thread.
         *
         * @return the computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        protected StringBuffer doInBackground() throws Exception {
            StringBuffer tidy = new StringBuffer("run,year,variable,value\n");
            Toolkit.getDefaultToolkit().beep();
            setProgress(0);
            while(runner.getRunsDone()<numberOfRuns) {
                taskOutput.append("Starting run " + runner.getRunsDone()+"\n");
                tidy=runner.run(tidy);
                taskOutput.append("Finished run " + runner.getRunsDone() +"\n");
                publish();
            }

            return tidy;

        }

        /**
         * Receives data chunks from the {@code publish} method asynchronously on the
         * <i>Event Dispatch Thread</i>.
         * <p>
         * <p>
         * Please refer to the {@link #publish} method for more details.
         *
         * @param chunks intermediate results to process
         * @see #publish
         */
        @Override
        protected void process(List<Void> chunks) {
            super.process(chunks);
            setProgress(runner.getRunsDone());
            taskOutput.revalidate();
            progressBar.revalidate();
        }

        // Executed in EDT
        protected void done() {
            try {
                System.out.println("Done");
                //write down tidy version
                StringBuffer batchOutput = get();
                Files.write(runner.getOutputFolder().resolve(runner.guessSimulationName()+"_batch.csv"),
                            batchOutput.toString().getBytes(), StandardOpenOption.CREATE);
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
                String msg = String.format("Unexpected problem: %s",
                                           e.getCause().toString());
                JOptionPane.showMessageDialog(null,
                                              msg, "Error", JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException e) {
                // Process e here
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Getter for property 'task'.
     *
     * @return Value for property 'task'.
     */
    public Task getTask() {
        return task;
    }
}
