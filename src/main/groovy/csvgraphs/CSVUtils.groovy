package csvgraphs

import fuzzycsv.FuzzyCSV
import groovy.util.logging.Log4j
import net.sf.dynamicreports.report.datasource.DRDataSource

/**
 * @author kayr
 */
@Log4j
class CSVUtils {

    /**
     * This Method is Deprecated use {@link FuzzyCSV#toCSV(java.util.List, java.lang.String [ ])}
     */
    @Deprecated
    static List<List> toCSV(List<? extends Map> list, String[] cols) {
        return FuzzyCSV.toCSV(list, cols)
    }
    /**
     * Use {@link FuzzyCSV#transpose(java.util.List, java.lang.String, java.lang.String, java.lang.String [ ])}
     */
    @Deprecated
    static Map transpose(List<? extends Map> list, String columnToBeHeader, String columnNeeded, String[] primaryKeys) {
        FuzzyCSV.transpose(FuzzyCSV.toCSV(list), columnToBeHeader, columnNeeded, primaryKeys)
    }

    /**
     * Deprecated: use {@link FuzzyCSV#transposeToCSV(java.util.List, java.lang.String, java.lang.String, java.lang.String [ ])}
     */
    @Deprecated
    static List<List> transposeToCSV(List<? extends Map> list, String columnToBeHeader, String columnNeeded, String[] primaryKeys) {
        FuzzyCSV.transposeToCSV(FuzzyCSV.toCSV(list), columnToBeHeader, columnNeeded, primaryKeys)
    }

    static DRDataSource createDataSource(List<String> headers, Collection<Map> data) {
        DRDataSource dataSource = new DRDataSource(headers as String[])
        data.each { Map values ->
            def sortedValues = []

            headers.each { header ->
                sortedValues << values[header]
            }

            if (log.isTraceEnabled())
                log.trace "adding $sortedValues"
            dataSource.add(sortedValues as Object[])
        }
        dataSource
    }

    static DRDataSource createDataSourceFromCsv(List<? extends List> csv) {
        def headers = csv[0]
        def ds = new DRDataSource(headers as String[])
        csv.eachWithIndex { List entry, int i ->
            if (i == 0) return
            def items = entry.toArray()
            if (log.isTraceEnabled())
                log.trace "CSVUtils:createDataSourceFromCsv: adding $items"
            ds.add(items)
        }
        ds
    }
}
