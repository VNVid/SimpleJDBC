package videneevanadezhda.service.dao;

import org.javatuples.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Charts {
    private static final int CHART_WIDTH = 2000;
    private static final int CHART_HEIGHT = 1200;

    private CategoryDataset createDataset(ArrayList<Pair<String, Integer>> data1,
                                          ArrayList<Pair<String, Integer>> data2,
                                          String rowKey1, String rowKey2) {
        var dataset = new DefaultCategoryDataset();
        for (Pair<String, Integer> val : data1) {
            dataset.setValue(val.getValue1(), rowKey1, val.getValue0());
        }
        if (!data2.isEmpty()) {
            for (Pair<String, Integer> val : data2) {
                dataset.setValue(val.getValue1(), rowKey2, val.getValue0());
            }
        }
        return dataset;
    }

    /**
     * creates bar chart
     *
     * @param data1,    data2  arrays with data to illustrate
     * @param valueAxis name of vertical axis
     * @param fileName
     * @throws IOException
     */
    public void createChart(ArrayList<Pair<String, Integer>> data1, ArrayList<Pair<String, Integer>> data2,
                            String title, String categoryAxis, String valueAxis, String fileName,
                            ArrayList<String> rowKey) throws IOException {
        CategoryDataset dataset = createDataset(data1, data2, rowKey.get(0), rowKey.get(1));

        JFreeChart barChart = ChartFactory.createBarChart3D(title, categoryAxis, valueAxis, dataset,
                PlotOrientation.VERTICAL, !data2.isEmpty(), false, false);

        // chart outlook
        CategoryPlot plot = barChart.getCategoryPlot();
        CategoryAxis axis = plot.getDomainAxis();

        final int fontSize30 = 30;
        final int fontSize35 = 35;
        final int fontSize40 = 40;
        Font font = new Font("Cambria", Font.BOLD, fontSize30);
        axis.setTickLabelFont(font);
        Font font3 = new Font("Cambria", Font.BOLD, fontSize35);
        barChart.setTitle(new org.jfree.chart.title.TextTitle(title,
                new java.awt.Font("Cambria", java.awt.Font.BOLD, fontSize40)));

        plot.getDomainAxis().setLabelFont(font3);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        plot.getRangeAxis().setLabelFont(font3);
        CategoryPlot categoryPlot = (CategoryPlot) barChart.getPlot();
        BarRenderer renderer = (BarRenderer) categoryPlot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setBaseLegendShape(new Rectangle(fontSize30, fontSize30));
        renderer.setBaseLegendTextFont(new Font("Helvetica", Font.ITALIC, fontSize40));


        File file = new File(fileName + ".png");
        file.createNewFile();
        ChartUtilities.saveChartAsPNG(file, barChart, CHART_WIDTH, CHART_HEIGHT);
    }
}
