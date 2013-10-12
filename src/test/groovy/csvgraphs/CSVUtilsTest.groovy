package csvgraphs

import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/11/13
 * Time: 6:25 PM
 * To change this template use File | Settings | File Templates.
 */
class CSVUtilsTest {
    def map = [
            ['name': 'p2', 'sex': 'male', 'number_passed': 2],
            ['name': 'p3', 'sex': 'female', 'number_passed': 4],
            ['name': 'p4', 'sex': 'male', 'number_passed': 1]
    ]

    @Test
    void testToCSV() {

        def actual = CSVUtils.toCSV(map, 'name', 'sex')

        def expected = [
                ['name', 'sex'],
                ['p2', 'male'],
                ['p3', 'female'],
                ['p4', 'male']
        ]

        assert actual == expected

    }

    @Test
    void testToCSVNoColumns() {

        def actual = CSVUtils.toCSV(map)

        def expected = [
                ['name', 'sex', 'number_passed'],
                ['p2', 'male', 2],
                ['p3', 'female', 4],
                ['p4', 'male', 1]
        ]

        assert actual == expected

    }

    @Test
    void testTranspose() {

        def actual = CSVUtils.transposeToCSV(map, 'name', 'number_passed', 'sex')

        def expectedMap = [
                ['sex', 'p2', 'p3', 'p4'],
                ['female', null, 4, null],
                ['male', 2, null, 1]
        ]

        assert actual == expectedMap

    }
}
