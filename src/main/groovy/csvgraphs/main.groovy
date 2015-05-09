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



CSVGraph g = new CSVGraph('', csv)
        .setColors(Colors.warm.values() as List)
        .setShowTable(true)
        .setShowChart(false)
        .setColumnNamesForChart(['shop', 'laptops'])
        .setChart(cht.pieChart())

g.callAfterTable {
    it << cmp.horizontalFlowList(
            g.createChart(),
            g.setChart(cht.pieChart()).setColumnNamesForChart(['shop', 'bags']).createChart())
}

g.miniReport.setPageFormat(PageType.A5, PageOrientation.LANDSCAPE).show()
//g2.miniReport.setPageFormat(PageType.A5, PageOrientation.LANDSCAPE).show()

//g.headings = [
//        sdlsd: 'sdlsdl'
//]
////g.setShowTable(false)
//
//g.callBeforeHeadings{ List components ->
//
//    components << ReportUtils.bulletedParagraph('Sub-indicators water quantity:',
//            'Score (%) Sub-indicator water quantity delivered = % of water facilities passing the yield test (i.e. whose yield is sufficient for supplying users at a consumption rate of 20 litres per person per day)',
//            'Score (%) Sub-indicator water quantity accessed = % of water facilities where a majority of users collect at least 20 litres per person per day')
//
//
//    CSVGraph g4 = new CSVGraph('mini report', csv)
//    //    g4.setShowChart(false)
//    components <<  cmp.horizontalFlowList(g4.createChart())
//    components << cmp.line()
//}
//
//
//g.unGroupedColumn('shop')
//        .titleGroup(
//        [
//                electronics: ['laptops', 'radios'],
//                others: ['shoes', 'bags']
//        ])
//        .unGroupedColumn('macaroni')
//
//
//g.columnsForChart.each { println it.getName() }
//
//g.getReport('SERVICE DELIVERED').show()

