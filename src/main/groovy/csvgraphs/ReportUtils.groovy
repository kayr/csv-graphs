package csvgraphs

import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.stl

/**
 * Created by kay on 4/22/14.
 */
class ReportUtils {

    static ComponentBuilder<?, ?> createCellComponent(String label, ComponentBuilder<?, ?> content) {
        VerticalListBuilder cell = cmp.verticalList(
                cmp.text(label).setStyle(stl.style().bold()).setHorizontalAlignment(HorizontalAlignment.CENTER),
                cmp.horizontalList(
                        cmp.horizontalGap(20),
                        content,
                        cmp.horizontalGap(5)));
        cell.setStyle(stl.style(stl.penThin()));
        return cell;
    }

    static ComponentBuilder<?, ?> bulletedParagraph(String label, String... statements) {
        //Todo make the style and font configurable
        VerticalListBuilder content = cmp.verticalList(
                cmp.text(label).setStyle(stl.style().bold().underline().setFontSize(13)))

        statements.each { String line ->
            content.add(
                    cmp.horizontalList(
                            cmp.horizontalGap(20),
                            cmp.text(".").setStyle(stl.style().bold().setFontSize(15)).setFixedWidth(5),
                            cmp.text("$line"),
                            cmp.horizontalGap(10))
            )
        }

        content.add(cmp.verticalGap(15))
        return content
    }

}
