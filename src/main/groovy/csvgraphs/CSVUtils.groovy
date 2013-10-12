package csvgraphs

import net.sf.dynamicreports.report.datasource.DRDataSource

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/11/13
 * Time: 6:19 PM
 * To change this template use File | Settings | File Templates.
 */
class CSVUtils {

    static List<List<?>> toCSV(List<Map> list, String[] cols) {
        if (!cols && list)
            cols = list[0].keySet() as String[]

        List<List<String>> flattened = [cols.toList()]
        for (mapRow in list) {
            def row = []
            for (columns in cols) {
                row << mapRow[columns]
            }
            flattened << row
        }
        return flattened
    }
    /**
     * if u have a table like this
     * class,sex,number
     * p1,m,3
     * p2,f,4
     * p5,m,6
     *
     * if( sex is unique)
     *
     * then #transpose(map,class,number,[sex])
     * sex, p1, p2, p5
     * m,   3   ,   ,
     * f,   ,   2   ,
     * m    ,   ,   ,5
     *
     * @param list lise is usually from GroovySql.rows()
     * @param columnToBeHeader the column u want to transform to Header
     * @param columnNeeded the column whose values in need in the table
     * @param primaryKeys columns that uniquely identify a row
     * @return Map contain [header -> [header list],
     *                      data -> [map list]]
     */

    static Map transpose(List<Map> list, String columnToBeHeader, String columnNeeded, String[] primaryKeys) {

        Map<List, Map> mapTransposed = [:]

        def headers = primaryKeys.toList()

        for (rowMap in list) {
            def key = primaryKeys.collect { rowMap[it] }

            //check if this row was already visited
            if (!mapTransposed.containsKey(key))
                mapTransposed[key] = [:]

            //get the already mapped row
            def newRow = mapTransposed[key]

            //add the primary keys first
            for (prKey in primaryKeys) {
                newRow[prKey] = rowMap[prKey]
            }

            //feed in the data
            def headerColumn = rowMap[columnToBeHeader]
            newRow[headerColumn] = rowMap[columnNeeded]

            //collect the header
            if (!headers.contains(headerColumn))
                headers.add(headerColumn)
        }
        mapTransposed = mapTransposed.sort { it.key.toString() }
        return [headers: headers, data: mapTransposed.values()]
    }

    static List<List<?>> transposeToCSV(List<Map> list, String columnToBeHeader, String columnNeeded, String[] primaryKeys) {
        Map map = transpose(list, columnToBeHeader, columnNeeded, primaryKeys)

        List<String> headers = map.headers
        Collection<Map> rows = map.data

        List<List<String>> csv = [map.headers]
        rows.each { Map values ->
            def csvRow = []
            headers.each { header ->
                csvRow << values[header]
            }
            println "adding $csvRow"
            csv.add(csvRow)
        }
        return csv
    }

    static DRDataSource createDataSource(List<String> headers, Collection<Map> transposedRows) {
        DRDataSource dataSource = new DRDataSource(headers as String[])
        transposedRows.each { Map values ->

            def sortedValues = []

            headers.each { header ->
                sortedValues << values[header]
            }

            println "adding $sortedValues"
            dataSource.add(sortedValues as Object[])
        }
        dataSource
    }

    static DRDataSource createDataSourceFromCsv(List<Object> headers, List<List<Object>> transposed) {
        def ds = new DRDataSource(headers as String[])
        transposed.each {
            def items = it.toArray()
            println "adding $items"
            ds.add(items)
        }
        ds
    }
}
