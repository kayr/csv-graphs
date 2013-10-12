package csvgraphs

import fuzzycsv.FuzzyCSV
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

    String title, reportUrl, reportImage
    List<List<?>> csv
    Map<String, String> labelMap = [:]
    Map<String, String> headings = [:]
    List<TextColumnBuilder> reportColumns
    int beginColumnIndexForChart = 1
    def chart = cht.bar3DChart()
    Templates tmp

    CSVGraph() {}

    CSVGraph(String reportHeader, String reportUrl, String imageUrl, List<List<?>> csv) {
        this.title = reportHeader
        this.reportUrl = reportUrl
        this.reportImage = imageUrl
        tmp = Templates.get(reportHeader, reportUrl, imageUrl)
        this.csv = csv
    }

    def getReport() {
        DRDataSource ds = CSVUtils.createDataSourceFromCsv(csv)
        def report = createReport(ds, csv[0])
        report
    }

    def getSubReport() {
        DRDataSource ds = CSVUtils.createDataSourceFromCsv(csv)
        def report = createSubReport(ds, csv[0])
        cmp.subreport(report)
    }

    def getReport(SubreportBuilder[] subReports) {
        def components = []
        components.addAll(subReports)
        def rep = report()
                .setTemplate(tmp.reportTemplate)
                .title(tmp.createTitleComponent(title))
                .summary(subReports)
                .pageFooter(tmp.footerComponent)
        return rep
    }

    def createSubReport(DRDataSource dataSource, List headers) {

        List<TextColumnBuilder> cols = getColumns(headers)

        def category = cols[0]

        def titleComponents = []

        createChart(cols, category)

        headings.each { key, value ->
            titleComponents << cmp.text(key).setStyle(tmp.boldStyle)
            titleComponents << cmp.text(value)
        }
        titleComponents << cmp.line()
        titleComponents << chart
        titleComponents << cmp.verticalGap(10)




        def rep = report()
                .setTemplate(tmp.reportTemplate)
                .columns(cols as ColumnBuilder[])
                .title(titleComponents as ComponentBuilder[])
                .setDataSource(dataSource)
        return rep
    }

    def createReport(DRDataSource dataSource, List headers) {
        def subReport = createSubReport(dataSource, headers)
        def rep = report()
                .setTemplate(tmp.reportTemplate)
                .title(tmp.createTitleComponent(title), cmp.subreport(subReport))
                .pageFooter(tmp.footerComponent)
        return rep
    }

    private List<TextColumnBuilder> getColumns(List headers) {
        if (reportColumns)
            return reportColumns

        reportColumns = headers.collect { String header ->
            def type = detectTypeForColumn(header)
            println "Resolved column [$header] to [$type]"
            return col.column(labelMap[header] ?: header, header, type)
        }
        reportColumns
    }



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
}
