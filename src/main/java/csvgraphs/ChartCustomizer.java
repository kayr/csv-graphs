package csvgraphs;

import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.chart.DRIChartCustomizer;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.renderer.category.BarRenderer;

import java.awt.*;
import java.io.Serializable;

public class ChartCustomizer implements DRIChartCustomizer, Serializable {
    private static final long serialVersionUID = 1L;
    private double angleOfTilt = 6.0;

    public ChartCustomizer() {

    }

    public ChartCustomizer(double angleOfTilt) {
        this.angleOfTilt = angleOfTilt;
    }


    @Override
    public void customize(JFreeChart chart, ReportParameters reportParameters) {
        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setItemMargin(0.0);

        CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
        angleOfTilt = 6.0;
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / angleOfTilt));
    }
}