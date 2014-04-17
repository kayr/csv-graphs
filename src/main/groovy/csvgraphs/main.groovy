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
        ['shop', 'laptops', 'bags', 'radios'],
        ['1a', 3, 5, 0],
        ['1b', 1, 5, 3],
        ['1c', 5, 2]
]



CSVGraph g = new CSVGraph('header', 'header2', 'url', 'image', csv)


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


g.unGroupedColum('shop', 'bags')
        .titleGroup('electronics', 'laptops', 'radios')


g.columnsForChart.each { println it.getName() }

g.report.show()

