package uk.ac.ox.oxfish.gui;

import org.metawidget.swing.SwingMetawidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by carrknight on 9/25/16.
 */
public class BatchRunnerSetup extends JPanel {


    private final BatchRunnerFactory factory = new BatchRunnerFactory();

    private final SwingMetawidget widget = new SwingMetawidget();


    /**
     * Constructs a new frame that is initially invisible.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @throws HeadlessException if GraphicsEnvironment.isHeadless()
     *                           returns true.
     * @see GraphicsEnvironment#isHeadless
     * @see Component#setSize
     * @see Component#setVisible
     * @see JComponent#getDefaultLocale
     */
    public BatchRunnerSetup() throws HeadlessException {


        setLayout(new BorderLayout());

        MetaInspector.STANDARD_WIDGET_SETUP(widget, null);


        widget.setToInspect(factory);
        add(widget,BorderLayout.CENTER);

        JButton start = new JButton("Start");
        add(start,BorderLayout.SOUTH);
        start.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println(factory.getColumnsToPrint());
                        JFrame parent = (JFrame)
                                SwingUtilities.getWindowAncestor(BatchRunnerSetup.this);
                      //  parent.removeAll();
                        start.removeActionListener(this);

                    //    parent.setVisible(false);
                        BatchRunnerProgress progress = new BatchRunnerProgress(
                                factory.build(),
                                factory.getNumberOfRuns()
                        );
                        parent.setContentPane(progress);
                        parent.setSize(800,600);
                        parent.pack();
                        parent.revalidate();
                        parent.setVisible(true);
                        parent.repaint();
                        progress.getTask().execute();

                    }
                }
        );
    }


    public static void main(String[] args)
    {
        JFrame jFrame = new JFrame();
        jFrame.setSize(800,600);
        jFrame.add(new BatchRunnerSetup());
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setVisible(true);

    }
}
