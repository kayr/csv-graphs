package csvgraphs

import net.sf.dynamicreports.report.builder.chart.AbstractChartBuilder
import net.sf.dynamicreports.report.builder.chart.Bar3DChartBuilder
import net.sf.dynamicreports.report.builder.chart.CategoryChartSerieBuilder
import net.sf.dynamicreports.report.builder.column.ColumnBuilder
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.SubreportBuilder
import net.sf.dynamicreports.report.builder.style.FontBuilder
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.dynamicreports.report.datasource.DRDataSource
import net.sf.dynamicreports.report.definition.datatype.DRIDataType

import static net.sf.dynamicreports.report.builder.DynamicReports.*

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/11/13
 * Time: 5:45 PM
 * To change this template use File | Settings | File Templates.
 */
class CSVGraph {

    String title, reportUrl, reportImage
    List<List<Object>> csv
    Map<String, DRIDataType> types = [:]
    Map<String, String> labelMap = [:]
    List<TextColumnBuilder> columns
    int beginColumnIndexForChart = 1
    Templates tmp

    CSVGraph() {}

    CSVGraph(String reportHeader, String reportUrl, String imageUrl) {
        this.title = reportHeader
        this.reportUrl = reportUrl
        this.reportImage = imageUrl
        tmp = Templates.get(reportHeader, reportUrl, imageUrl)
    }

    def getReport() {

    }

    def getSubReport() {

    }

    def getReport(SubreportBuilder[] subreports) {

    }

    def getSubReport(DRDataSource dataSource, List headers) {

        List<TextColumnBuilder> cols = getColumns(headers)

        def category = cols[0]

        Bar3DChartBuilder chart = createChart(cols, category)

        //end extract chart code

        def rep = report()
                .setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setTemplate(tmp.reportTemplate)
                .columns(cols as ColumnBuilder[])
                .title(chart, cmp.verticalGap(10))
                .setDataSource(dataSource)
        return rep
    }



    private List<TextColumnBuilder> getColumns(List headers) {
        if (columns)
            return columns

        columns = headers.collect { String header ->
            println "Coneverting to column Header[$header]"
            def type = header == 'period' ? type.stringType() : type.bigDecimalType()
            //replace incase there are any absolute types
            //implement heristice to improve
            type = types[header] ?: type
            return col.column(labelMap[header] ?: header, header, type)
        }
        columns
    }

    def chart = cht.bar3DChart()

    private AbstractChartBuilder createChart(List<TextColumnBuilder> cols, TextColumnBuilder category) {
        def chatSeries = cols[beginColumnIndexForChart..cols.size() - 1].collect {
            cht.serie(it)
        }
        def graphTitle = title
        def keyTitle = title
        FontBuilder boldFont = stl.fontArialBold().setFontSize(12);

        chart.setTitle(graphTitle)
                .setTitleFont(boldFont)
                .setCategory(category)
                .series(chatSeries as CategoryChartSerieBuilder[])
                .setCategoryAxisFormat(cht.axisFormat().setLabel(keyTitle))
        chart
    }


}
