package uk.ac.ox.oxfish.gui.widget;

import uk.ac.ox.oxfish.utility.StrategyFactory;

import javax.swing.*;
import java.awt.*;

/**
 * A panel containing a combo-box to select a strategy factory
 * Created by carrknight on 6/7/15.
 */
public class StrategyFactoryDialog<T> extends JDialog
{

    /**
     * what is selected
     */
    private StrategyFactory<? extends T> selected;



    /**
     * what strategy are we dealing with
     * @param strategyClass the strategy superclass
     */
    public StrategyFactoryDialog(Class<T> strategyClass)
    {
     //   super(null,"Strategy Maker",false);



    }
}
