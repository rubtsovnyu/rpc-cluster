package gui;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.builder.Tessellator;
import org.jzy3d.plot3d.builder.delaunay.DelaunayTessellator;
import org.jzy3d.plot3d.primitives.AbstractComposite;
import org.jzy3d.plot3d.primitives.axes.layout.AxeBoxLayout;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.legends.colorbars.AWTColorbarLegend;

import java.util.List;

public class PlotWindow extends AbstractAnalysis {
    private final Tessellator tessellator;

    public PlotWindow() {
        super();
        tessellator = new DelaunayTessellator();
    }

    @Override
    public void init() throws Exception {
        chart = AWTChartComponentFactory.chart(Quality.Fastest, IChartComponentFactory.Toolkit.awt);
    }

    public Chart getChart() {
        return chart;
    }

    public void updateChart(final List<Coord3d> coord3ds) {
        final AbstractComposite tessellatedPlot = tessellator.build(coord3ds);

        tessellatedPlot.setColorMapper(new ColorMapper(
                new ColorMapRainbow(),
                tessellatedPlot.getBounds().getZmin(),
                tessellatedPlot.getBounds().getZmax()
        ));
        AWTColorbarLegend legend = new AWTColorbarLegend(tessellatedPlot, new AxeBoxLayout());
        tessellatedPlot.setLegend(legend);
        tessellatedPlot.setLegendDisplayed(true);

        if (chart.getScene().getGraph().getAll().size() == 0) {
            try {
                AnalysisLauncher.open(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            chart.getScene().getGraph().getAll().clear();
        }
        chart.add(tessellatedPlot);
    }
}
