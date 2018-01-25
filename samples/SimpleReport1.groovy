import csvgraphs.CSVGraph

import static net.sf.dynamicreports.report.builder.DynamicReports.cht

def data = [
        ['Item', 'Quantity', 'Unit Price'],
        ['Book', 70, 100],
        ['Notebook', 25, 500],
        ['PDA', 40, 250]
]

new CSVGraph('CSV Graphs Area Chart', data)
        .setChart(cht.areaChart())
        .setColumnNamesForChart('Item', 'Quantity')
        .report
        .show()