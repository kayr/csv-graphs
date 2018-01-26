package csvgraphs

import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType

import static net.sf.dynamicreports.report.builder.DynamicReports.cht
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp

def csv = [
        ['s', 'shop', 'laptops', 'bags', 'radios', 'shoes', 'macaroni'],
        ['s', '1a', 3, 5, 0, 5, 4],
        ['s', '1b', 1, 5, 3, 6],
        ['s', '1c', 5, 2, null, 7]
]



CSVGraph g = new CSVGraph(csv)
        .setColors(Colors.warm.values() as List)
        .setShowTable(true)
        .setShowChart(false)

//Set the Grouped columns
g.unGroupedColumn('shop')
 .titleGroup(
        [
                'Electronics': ['laptops', 'radios'],
                'Other Items': ['shoes', 'bags']
        ])
 .unGroupedColumn('macaroni')

// add two pie charts at the below the main table
g.callAfterTable {
    it << cmp.horizontalFlowList(
            g.setColumnNamesForChart('shop', 'laptops').setChart(cht.pieChart()).createChart(),
            g.setColumnNamesForChart('shop', 'bags').setChart(cht.pieChart()).createChart()
    )
}



g.miniReport.setPageFormat(PageType.A5, PageOrientation.LANDSCAPE).show()