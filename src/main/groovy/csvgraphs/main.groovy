package csvgraphs

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 12/8/13
 * Time: 1:32 PM
 * To change this template use File | Settings | File Templates.
 */

def csv = [
        ['shop', 'laptops', 'bags', 'radios', 'shoes', 'macaroni'],
        ['1a', 3, 5, 0, 5, 4],
        ['1b', 1, 5, 3, 6],
        ['1c', 5, 2, null, 7]
]



CSVGraph g = new CSVGraph('header', 'header2', 'url', 'image', csv)
        .setColors(Colors.warm.values() as List)


g.headings = [
        sdlsd: 'sdlsdl'
]
//g.setShowTable(false)

g.callBeforeHeadings{ List components ->
    CSVGraph g4 = new CSVGraph('mini report', csv)
    //    g4.setShowChart(false)
    components <<  cmp.horizontalFlowList(g4.createChart())
    components << cmp.line()
}


g.unGroupedColumn('shop')
        .titleGroup(
        [
                electronics: ['laptops', 'radios'],
                others: ['shoes', 'bags']
        ])
        .unGroupedColumn('macaroni')


g.columnsForChart.each { println it.getName() }

g.report.show()

