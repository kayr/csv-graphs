package csvgraphs

import fuzzycsv.FuzzyCSV
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.chart.AbstractChartBuilder
import net.sf.dynamicreports.report.builder.chart.CategoryChartSerieBuilder
import net.sf.dynamicreports.report.builder.chart.Charts
import net.sf.dynamicreports.report.builder.column.ColumnBuilder
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.component.SubreportBuilder
import net.sf.dynamicreports.report.builder.datatype.DataTypes
import net.sf.dynamicreports.report.builder.grid.ColumnGridComponentBuilder
import net.sf.dynamicreports.report.builder.grid.ColumnTitleGroupBuilder
import net.sf.dynamicreports.report.builder.style.FontBuilder
import net.sf.dynamicreports.report.datasource.DRDataSource
import net.sf.dynamicreports.report.definition.datatype.DRIDataType

import java.awt.*
import java.util.List

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
    private List<ColumnTitleGroupBuilder> columnTitleGroups = []
    List columnNamesForChart, groupedColumnNames = []

    int beginColumnIndexForChart = 1
    def chart = cht.bar3DChart()
    Templates template
    boolean showChart = true, showTable = true

    boolean chartLabelTilt = false

    Closure beforeHeadings, beforeChart, beforeTable, afterTable
    Number maxGraphValue

    List<Color> colors = []


    CSVGraph() {}

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
        DRDataSource dataSource = CSVUtils.createDataSourceFromCsv(csv)
        def originalCall = beforeHeadings
        callBeforeHeadings { List cmp ->
            cmp << template.createTitleComponent(title)
            originalCall?.call(cmp)
        }
        def subReport = createSubReport(dataSource, true)
        callBeforeHeadings(originalCall)
        return subReport
    }

    SubreportBuilder getSubReport() {
        DRDataSource ds = CSVUtils.createDataSourceFromCsv(csv)
        def report = createSubReport(ds)
        cmp.subreport(report)
    }

    SubreportBuilder getSubReportWithPager() {
        DRDataSource ds = CSVUtils.createDataSourceFromCsv(csv)
        def report = createSubReport(ds, true)
        cmp.subreport(report)
    }

    CSVGraph setMaxGraphValue(Number number) {
        this.maxGraphValue = number
        return this
    }

    JasperReportBuilder getMiniReport() {
        DRDataSource ds = CSVUtils.createDataSourceFromCsv(csv)
        createSubReport(ds)
    }

    JasperReportBuilder getReport(SubreportBuilder[] subReports) {
        def rep = report()
                .setTemplate(template.reportTemplate)
                .title(template.createTitleComponent(title))
                .summary(subReports)
                .pageFooter(template.footerComponent)
        return rep
    }

    //this is the meat of the class. Where all report building occurs
    private JasperReportBuilder createSubReport(DRDataSource dataSource, boolean addPager = false) {

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

        def report = report()
                .setTemplate(template.reportTemplate)
                .title(titleComponents as ComponentBuilder[])
                .setDataSource(dataSource)

        if (columnTitleGroups) {
            report.columnGrid(columnTitleGroups as ColumnGridComponentBuilder[])
        }

        def summaryComponents = []

        afterTable?.call(summaryComponents)
        if (summaryComponents)
            report.summary(summaryComponents as ComponentBuilder[])

        if (showTable) {
            report.columns(cols as ColumnBuilder[])
        }

        if (addPager) {
            report.pageFooter(template.footerComponent)
        }

        return report
    }

    List<TextColumnBuilder> getColumnsForChart() {
        if (!columnNamesForChart) {
            columnNamesForChart = csv[0][beginColumnIndexForChart - 1..-1]
        }
        List<Integer> chartIndices = columnNamesForChart.collect { csv[0].indexOf(it) }
        return getColumns().getAt(chartIndices)
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


    ColumnBuilder getColumn(String name) {
        def column = columns?.find {
            it.getColumn().getName() == name
        }
        return column
    }

    CSVGraph titleGroup(String name, String[] columns) {

        assert columns, 'you did not provide any columns'

        assert name, 'name for title group is not allowed to be null'


        def reportColumns = columns.collect {
            def column = getColumn(it)
            assert column, "column [$it] could not be found in the report"
            if (!groupedColumnNames.contains(column)) {
                groupedColumnNames.add(it)
            }
            return column
        }

        columnTitleGroups << grid.titleGroup(name, reportColumns as ColumnGridComponentBuilder[])

        return this
    }

    CSVGraph titleGroup(Map tree) {

        assert tree, 'name for title group is not allowed to be null'

        def titleGroupBuilders = getTitleGroupFromMap(tree)

        for (builder in titleGroupBuilders) {
            columnTitleGroups << builder
        }

        return this
    }

    private List<ColumnTitleGroupBuilder> getTitleGroupFromMap(Map tree) {

        assert tree, 'name for title group is not allowed to be null'

        def rt = []

        def keySet = tree.keySet()

        for (String groupKey in keySet) {
            def gpComps = tree[groupKey]

            if (gpComps instanceof List) {
                def group = getTitleGroupFromList(groupKey, gpComps)
                rt << group
            }

            if (gpComps instanceof Map) {
                def groups = getTitleGroupFromMap(gpComps)
                def group = grid.titleGroup(groupKey, groups as ColumnGridComponentBuilder[])
                rt << group
            }
        }

        return rt
    }


    private ColumnTitleGroupBuilder getTitleGroupFromList(String name, List<String> columns) {
        assert columns, 'you did not provide any columns'

        assert name, 'name for title group is not allowed to be null'

        def reportColumns = columns.collect {
            def column = getColumn(it)
            assert column, "column [$it] could not be found in the report"
            if (!groupedColumnNames.contains(column)) {
                groupedColumnNames.add(it)
            }
            return column
        }

        def group = grid.titleGroup(name, reportColumns as ColumnGridComponentBuilder[])

        return group

    }

    CSVGraph unGroupedColumn(String... names) {

        assert names, 'name for title group is not allowed to be null'

        names.each { name ->
            def column = getColumn(name)
            assert column, "column [$name] could not be found in the report"
            columnTitleGroups << column
        }
        return this
    }


    AbstractChartBuilder createChart() {
        List<TextColumnBuilder> cols = getColumnsForChart()

        def category = columnsForChart[0]

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

        if (chartLabelTilt)
            chart.customizers(new ChartCustomizer(10))

        if (colors) {
            chart.seriesColors(colors as Color[])
        }

        if (maxGraphValue != null)
            chart.setValueAxisFormat(Charts.axisFormat().setRangeMaxValueExpression(maxGraphValue))

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

    CSVGraph callBeforeTable(Closure beforeTable) {
        this.beforeTable = beforeTable
        return this
    }

    CSVGraph callBeforeChart(Closure beforeChart) {
        this.beforeChart = beforeChart
        return this
    }

    CSVGraph callBeforeHeadings(Closure beforeHeadings) {
        this.beforeHeadings = beforeHeadings
        return this
    }

    CSVGraph callAfterTable(Closure afterTable) {
        this.afterTable = afterTable
        return this
    }

    CSVGraph setChart(chart) {
        this.chart = chart
        return this
    }

    CSVGraph setColumnNamesForChart(List headersForChart) {
        this.columnNamesForChart = headersForChart
        return this
    }

    CSVGraph setShowChart(boolean showChart) {
        this.showChart = showChart
        return this
    }

    CSVGraph setColors(Collection<Color> colors) {
        this.colors = colors
        return this
    }

    CSVGraph setShowTable(boolean showTable) {
        this.showTable = showTable
        return this
    }

    CSVGraph setBeginColumnIndexForChart(int beginColumnIndexForChart) {
        this.beginColumnIndexForChart = beginColumnIndexForChart
        return this
    }

    CSVGraph setGraphTitle(String graphTitle) {
        this.graphTitle = graphTitle
        return this
    }

    CSVGraph setLabelMap(Map map) {
        this.labelMap = map
        return this
    }

    CSVGraph setHeadings(Map map) {
        this.headings = map
        return this
    }

    CSVGraph setChartLabelTilt(boolean value) {
        this.chartLabelTilt = value
        return this
    }
}
