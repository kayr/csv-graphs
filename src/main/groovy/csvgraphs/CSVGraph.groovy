package csvgraphs

import fuzzycsv.Fuzzy
import groovy.util.logging.Log4j
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.chart.*
import net.sf.dynamicreports.report.builder.column.ColumnBuilder
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.component.SubreportBuilder
import net.sf.dynamicreports.report.builder.datatype.DataTypes
import net.sf.dynamicreports.report.builder.datatype.NumberType
import net.sf.dynamicreports.report.builder.datatype.StringType
import net.sf.dynamicreports.report.builder.grid.ColumnGridComponentBuilder
import net.sf.dynamicreports.report.builder.grid.ColumnTitleGroupBuilder
import net.sf.dynamicreports.report.builder.group.CustomGroupBuilder
import net.sf.dynamicreports.report.builder.group.GroupBuilder
import net.sf.dynamicreports.report.builder.style.FontBuilder
import net.sf.dynamicreports.report.datasource.DRDataSource

import java.awt.*
import java.util.List

import static net.sf.dynamicreports.report.builder.DynamicReports.*

/**
 * @author kayr
 */
@Log4j
class CSVGraph {

    List<? extends List> csv
    Map<String, String>  labelMap = [:]
    Map<String, String>  headings = [:]
    String               reportHeader
    String               title
    String               reportUrl
    String               reportImage
    String               graphTitle

    boolean showChart         = true
    boolean showTable         = true
    boolean showChartBoundary = false
    boolean showColumLines    = false
    boolean chartLabelTilt    = false


    List<TextColumnBuilder> reportColumns
    List                    columnNamesForChart
    List                    groupedColumnNames       = []
    List<Color>             colors                   = []
    int                     beginColumnIndexForChart = 1
    def                     chart                    = cht.bar3DChart()
    Templates               template


    Closure beforeHeadings, beforeChart, beforeTable, afterTable
    Number  maxGraphValue

    private List<CustomGroupBuilder>         groups            = []
    private List<ColumnGridComponentBuilder> columnTitleGroups = []


    CSVGraph() {}

    CSVGraph(String reportHeader, String reportUrl, String imageUrl, List<? extends List> csv) {
        this(reportHeader, reportHeader, reportUrl, imageUrl, csv)
    }

    CSVGraph(String reportHeader, List<? extends List> csv) {
        this("", reportHeader, "", "", csv)
    }

    CSVGraph(List<? extends List> csv) {
        this('', csv)
    }

    CSVGraph(String reportHeader, String title, String reportUrl, String imageUrl, List<? extends List> csv) {
        this.title = title
        this.reportHeader = reportHeader
        this.reportUrl = reportUrl
        this.reportImage = imageUrl
        template = Templates.get(reportHeader, reportUrl, imageUrl)
        this.csv = csv
    }

    CSVGraph setImage(Object image) {
        template = Templates.get(reportHeader, reportUrl, image)
        return this
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

    JasperReportBuilder getReport(String mainHeader) {
        DRDataSource dataSource = CSVUtils.createDataSourceFromCsv(csv)
        def originalCall = beforeHeadings
        callBeforeHeadings { List cmp ->
            cmp << template.create2TitleComponent(mainHeader, title)
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

        beforeHeadings?.call(titleComponents)
        if (headings) {
            headings.each { key, value ->
                titleComponents << cmp.text(key).setStyle(this.template.boldStyle)
                titleComponents << cmp.text(value)
            }
            titleComponents << cmp.line()
        }

        beforeChart?.call(titleComponents)

        if (showChart) {
            createChart()
            if (chart) {
                if (showChartBoundary) {
                    def component = ReportUtils.createCellComponent(graphTitle ?: title, chart)
                    titleComponents << component
                } else {
                    titleComponents << chart
                }
            }
        }

        beforeTable?.call(titleComponents)

        titleComponents << cmp.verticalGap(10)


        def template = template.reportTemplate
        if (showColumLines) {
            template.setColumnStyle(stl.style(Templates.columnStyle).setBorder(stl.penThin()))
        }
        def report = report()
                .setTemplate(template)
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

        if (groups) {
            report.groupBy(groups as GroupBuilder[])
        }

        if (addPager) {
            report.pageFooter(this.template.footerComponent)
        }

        return report
    }

    List<TextColumnBuilder> getColumnsForChart() {
        if (!columnNamesForChart) {
            if (csv.size() == 1) {
                columnNamesForChart = csv[0][beginColumnIndexForChart - 1..-1]
            }
            return getAppropriateMappableColumns()
        }
        List<Integer> chartIndices = columnNamesForChart.collect { csv[0].indexOf(it) }
        return getColumns().getAt(chartIndices)
    }

    List<TextColumnBuilder> getAppropriateMappableColumns() {

        def numberColumns = getColumns().findAll { TextColumnBuilder columnBuilder ->
            detectTypeForColumn.call(columnBuilder.getColumn().getName()) instanceof NumberType
        }
        def stringColumn = getColumns().find { TextColumnBuilder c -> detectTypeForColumn.call(c.column.name) instanceof StringType }
        def rt = [stringColumn]
        rt.addAll(numberColumns)
        return rt
    }

    List<TextColumnBuilder> getColumns() {
        if (reportColumns)
            return reportColumns

        reportColumns = csv[0].collect { String header ->
            def type = detectTypeForColumn.call(header)
            log.debug "Resolved column [$header] to [${type.getClass().name}]"
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
        columnTitleGroups << getTitleGroupFromList(name, columns as List)
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
            if (!groupedColumnNames.contains(it)) {
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
        if (chart instanceof AbstractCategoryChartBuilder) {
            chart = createBarChart()
        } else if (chart instanceof AbstractPieChartBuilder) {
            chart = createPieChart()
        } else {
            throw new UnsupportedOperationException("Chart Not Supported: $chart")
        }

        if (!showChartBoundary) {
            def graphTitle = this.graphTitle ?: title
            chart?.setTitle(graphTitle)
        }

        return chart
    }

    private AbstractChartBuilder createBarChart() {
        List<TextColumnBuilder> cols = getColumnsForChart()

        if (!cols || cols.size() <= 1) {
            log.warn("Not creating bar chart because data cannot be graphed")
            return null
        }

        def category = columnsForChart[0]

        def chatSeries = cols[1..-1].collect {
            cht.serie(it)
        }

        FontBuilder boldFont = stl.fontArialBold().setFontSize(12);

        chart.setTitleFont(boldFont)
             .setCategory(category)
             .series(chatSeries as CategoryChartSerieBuilder[])
             .setCategoryAxisFormat(cht.axisFormat()/*.setLabel(keyTitle)*/)


        if (chartLabelTilt)
            chart.customizers(new ChartLabelTiltCustomizer(10))

        if (colors) {
            chart.seriesColors(colors as Color[])
        }

        if (maxGraphValue != null)
            chart.setValueAxisFormat(Charts.axisFormat().setRangeMaxValueExpression(maxGraphValue))

        chart
    }

    private AbstractChartBuilder createPieChart() {
        List<TextColumnBuilder> cols = getColumnsForChart()

        if (!cols || cols.size() <= 1) {
            log.warn("Not creating chart because data cannot be graphed")
            return null
        }

        def category = columnsForChart[0]

        def chatSeries = cols[1..-1].collect { cht.serie(it) }

        FontBuilder boldFont = stl.fontArialBold().setFontSize(12);

        chart.setTitleFont(boldFont).setKey(category).series(*chatSeries)

        return chart

    }

    def detectTypeForColumn = { String header ->
        def position = Fuzzy.findPosition(csv[0], header)

        def item
        def zeroCandidate
        for (int i = 1; i < csv.size(); i++) {
            def entry = csv[i]
            if (i == 0 || entry == null || entry[position] == null)
                continue

            item = entry[position]
            if (item != 0) break

            zeroCandidate = item
        }

        if (item != null)
            return DataTypes.detectType(item.class)

        if (zeroCandidate != null)
            return DataTypes.detectType(zeroCandidate.class)

        return type.bigDecimalType()
    }.memoize()

    CSVGraph groupBy(String... colNames) {
        for (colName in colNames) {
            def column = getColumn(colName)
            assert column, "$colName should exist in the columns"
            groups << grp.group(column)
        }
        return this
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

    CSVGraph setColumnNamesForChart(String... headersForChart) {
        setColumnNamesForChart(headersForChart as List)
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
        this.colors = colors as List
        return this
    }

    CSVGraph setShowChartBoundary(boolean value) {
        this.showChartBoundary = value
        return this
    }

    CSVGraph setShowColumnLines(boolean showColumnLines) {
        this.showColumLines = showColumnLines
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
