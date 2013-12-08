package csvgraphs

import fuzzycsv.FuzzyCSV
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.chart.AbstractChartBuilder
import net.sf.dynamicreports.report.builder.chart.CategoryChartSerieBuilder
import net.sf.dynamicreports.report.builder.column.ColumnBuilder
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.component.SubreportBuilder
import net.sf.dynamicreports.report.builder.datatype.DataTypes
import net.sf.dynamicreports.report.builder.style.FontBuilder
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

    List<? extends List> csv
    Map<String, String> labelMap = [:]
    Map<String, String> headings = [:]

    String reportHeader, title, reportUrl, reportImage, graphTitle



    List<TextColumnBuilder> reportColumns
    List headersForChart

    int beginColumnIndexForChart = 1
    def chart = cht.bar3DChart()
    Templates template
    boolean showChart = true, showTable = true

    Closure beforeHeadings, beforeChart, beforeTable

    private CSVGraph() {}

    CSVGraph(String reportHeader, String reportUrl, String imageUrl, List<? extends List> csv) {
        this(reportHeader, reportHeader, reportUrl, imageUrl, csv)
    }

    CSVGraph(String reportHeader, List<? extends List> csv) {
        this("", reportHeader, "", "", csv)
    }

    CSVGraph(String reportHeader, String title, String reportUrl, String imageUrl, List<? extends List> csv) {
        this.title = title
        this.reportHeader = reportHeader
        this.reportUrl = reportUrl
        this.reportImage = imageUrl
        template = Templates.get(reportHeader, reportUrl, imageUrl)
        this.csv = csv
    }

    JasperReportBuilder getReport() {
        DRDataSource ds = CSVUtils.createDataSourceFromCsv(csv)
        def report = createReport(ds)
        report
    }

    SubreportBuilder getSubReport() {
        DRDataSource ds = CSVUtils.createDataSourceFromCsv(csv)
        def report = createSubReport(ds)
        cmp.subreport(report)
    }

    JasperReportBuilder getReport(SubreportBuilder[] subReports) {
        def components = []
        components.addAll(subReports)
        def rep = report()
                .setTemplate(template.reportTemplate)
                .title(template.createTitleComponent(title))
                .summary(subReports)
                .pageFooter(template.footerComponent)
        return rep
    }

    //this is the meat of the class. Where all report building occurs
    private JasperReportBuilder createSubReport(DRDataSource dataSource) {

        List<TextColumnBuilder> cols = getColumns()

        def titleComponents = []

        if (showChart)
            createChart()

        beforeHeadings?.call(titleComponents)
        if (headings) {
            headings.each { key, value ->
                titleComponents << cmp.text(key).setStyle(template.boldStyle)
                titleComponents << cmp.text(value)
            }
            titleComponents << cmp.line()
        }

        beforeChart?.call(titleComponents)
        if (showChart) {
            titleComponents << chart
        }

        beforeTable?.call(titleComponents)

        titleComponents << cmp.verticalGap(10)

        def rep = report()
                .setTemplate(template.reportTemplate)
                .title(titleComponents as ComponentBuilder[])
                .setDataSource(dataSource)

        if (showTable) {
            rep.columns(cols as ColumnBuilder[])
        }
        return rep
    }

    private List<TextColumnBuilder> getColumnsForChat() {
        if (!headersForChart) {
            headersForChart = csv[0][beginColumnIndexForChart - 1..-1]
        }
        List<Integer> chartIndices = headersForChart.collect { csv[0].indexOf(it) }
        return getColumns().getAt(chartIndices)
    }

    private JasperReportBuilder createReport(DRDataSource dataSource) {
        def subReport = createSubReport(dataSource)
        def rep = report()
                .setTemplate(template.reportTemplate)
                .title(template.createTitleComponent(title), cmp.subreport(subReport))
                .pageFooter(template.footerComponent)
        return rep
    }

    List<TextColumnBuilder> getColumns() {
        if (reportColumns)
            return reportColumns

        reportColumns = csv[0].collect { String header ->
            def type = detectTypeForColumn(header)
            println "Resolved column [$header] to [${type.getClass().name}]"
            return col.column(labelMap[header] ?: header, header, type)
        }
        reportColumns
    }



    AbstractChartBuilder createChart() {
        List<TextColumnBuilder> cols = getColumnsForChat()

        def category = columnsForChat[0]

        def chatSeries = cols[1..-1].collect {
            cht.serie(it)
        }
        def graphTitle = this.graphTitle ?: title
        FontBuilder boldFont = stl.fontArialBold().setFontSize(12);

        chart.setTitle(graphTitle)
                .setTitleFont(boldFont)
                .setCategory(category)
                .series(chatSeries as CategoryChartSerieBuilder[])
                .setCategoryAxisFormat(cht.axisFormat()/*.setLabel(keyTitle)*/)
        chart
    }

    DRIDataType detectTypeForColumn(String header) {
        def position = FuzzyCSV.getColumnPosition(csv, header)

        def item
        for (int i = 1; i < csv.size(); i++) {
            def entry = csv[i]
            if (i == 0 || entry == null || entry[position] == null)
                continue

            item = entry[position]
            break
        }
        if (item != null)
            return DataTypes.detectType(item.class)
        return type.bigDecimalType()
    }

    def callBeforeTable(Closure beforeTable) {
        this.beforeTable = beforeTable
    }

    def callBeforeChart(Closure beforeChart) {
        this.beforeChart = beforeChart
    }

    def callBeforeHeadings(Closure beforeHeadings) {
        this.beforeHeadings = beforeHeadings
    }
}
