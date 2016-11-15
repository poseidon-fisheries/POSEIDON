package uk.ac.ox.oxfish.gui;

import uk.ac.ox.oxfish.model.BatchRunner;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
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


    class Task extends SwingWorker<Void,Void>{



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
        protected Void doInBackground() throws Exception {
            Toolkit.getDefaultToolkit().beep();
            setProgress(0);
            while(runner.getRunsDone()<numberOfRuns) {
                taskOutput.append("Starting run " + runner.getRunsDone()+"\n");
                runner.run();
                taskOutput.append("Finished run " + runner.getRunsDone() +"\n");
                publish();
            }

            return null;

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
            taskOutput.repaint();
            progressBar.repaint();
        }

        // Executed in EDT
        protected void done() {
            try {
                System.out.println("Done");
                get();
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
                String msg = String.format("Unexpected problem: %s",
                                           e.getCause().toString());
                JOptionPane.showMessageDialog(null,
                                              msg, "Error", JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException e) {
                // Process e here
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
