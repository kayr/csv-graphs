package csvgraphs

import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 12/8/13
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
class CSVGraphTest {

    def csv = [
            ['shop', 'laptops', 'bags'],
            ['1a', 3, 5],
            ['1b', 1],
            ['1c', 5, 2]
    ]

    @Test
    void testGetReport() {
        CSVGraph g = new CSVGraph('header', 'url', 'image', csv)
        assert g.report
    }

    @Test
    void testGetReportWithCustomChatHeader() {


        CSVGraph g = new CSVGraph('header', 'url', 'image', csv)
        g.columnNamesForChart = ['shop', 'bags']
        assert g.report
    }

    @Test
    void testTitleGroupWithTree() {
        def csv = [
                ['shop', 'laptops', 'bags', 'radios', 'shoes', 'macaroni'],
                ['1a', 3, 5, 0, 5, 4],
                ['1b', 1, 5, 3, 6],
                ['1c', 5, 2, null, 7]
        ]

        CSVGraph g = new CSVGraph('header', 'url', 'image', csv)

        g.titleGroup(
                ['All items': [
                        electronics: ['laptops', 'radios'],
                        others: ['radios']
                ]]
        )




    }

}
