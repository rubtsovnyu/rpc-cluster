package gui;

import javax.swing.*;
import java.awt.*;

public class PlotWindow extends JFrame {
    public PlotWindow(String title) throws HeadlessException {
        super("Plot: " + title);
        init();
    }

    private void init() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }
}
