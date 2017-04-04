/**
 * DynamicReports - Free Java reporting library for creating reports dynamically
 * <p>
 * Copyright (C) 2010 - 2013 Ricardo Mariaca
 * http://www.dynamicreports.org
 * <p>
 * This file is part of DynamicReports.
 * <p>
 * DynamicReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DynamicReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with DynamicReports. If not, see <http://www.gnu.org/licenses/>.
 */

package csvgraphs;

import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.builder.HyperLinkBuilder;
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.ImageBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.datatype.BigDecimalType;
import net.sf.dynamicreports.report.builder.style.ReportStyleBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.tableofcontents.TableOfContentsCustomizerBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.VerticalAlignment;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.definition.ReportParameters;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.Locale;
import java.util.concurrent.Callable;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.codehaus.groovy.runtime.ResourceGroovyMethods.toURL;

/**
 * @author Ricardo Mariaca (r.mariaca@dynamicreports.org)
 */
public class Templates extends ReportUtils {
    public static final StyleBuilder rootStyle;
    public static final StyleBuilder boldStyle;
    public static final StyleBuilder italicStyle;
    public static final StyleBuilder boldCenteredStyle;
    public static final StyleBuilder bold12CenteredStyle;
    public static final StyleBuilder bold18CenteredStyle;
    public static final StyleBuilder bold22CenteredStyle;
    public static final StyleBuilder columnStyle;
    public static final StyleBuilder columnTitleStyle;
    public static final StyleBuilder groupStyle;
    public static final StyleBuilder subtotalStyle;
    public static final CurrencyType currencyType;
    private ComponentBuilder<?, ?> dynamicReportsComponent;
    private ComponentBuilder<?, ?> footerComponent;
    private String header, url;

    static {
        rootStyle = stl.style().setPadding(2);
        boldStyle = stl.style(rootStyle).bold();
        italicStyle = stl.style(rootStyle).italic();
        boldCenteredStyle = stl.style(boldStyle)
                .setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE);
        bold12CenteredStyle = stl.style(boldCenteredStyle)
                .setFontSize(12);
        bold18CenteredStyle = stl.style(boldCenteredStyle)
                .setFontSize(18);
        bold22CenteredStyle = stl.style(boldCenteredStyle)
                .setFontSize(22);
        columnStyle = stl.style(rootStyle).setVerticalTextAlignment(VerticalTextAlignment.MIDDLE);
        columnTitleStyle = stl.style(columnStyle)
                .setBorder(stl.pen1Point())
                .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                .setBackgroundColor(Color.LIGHT_GRAY)
                .bold();
        groupStyle = stl.style(boldStyle)
                .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        subtotalStyle = stl.style(boldStyle)
                .setTopBorder(stl.pen1Point());


        currencyType = new CurrencyType();


    }

    private Templates() {
    }

    public static Templates get(String reportHeader, String linkUrl, Object imageUrl) throws Exception {
        Templates t = new Templates();
        HyperLinkBuilder link = hyperLink(linkUrl);
        ImageBuilder image = null;
        if (imageUrl instanceof String) {
            String imageUtlStr = (String) imageUrl;

            if (imageUtlStr.startsWith("http")) {
                image = cmp.image(org.codehaus.groovy.runtime.ResourceGroovyMethods.toURL(imageUtlStr));
            } else {
                image = cmp.image(Templates.class.getResource((String) imageUrl));
            }

            if (image == null)
                image = image.setImage("");

            image.setFixedDimension(60, 60);
        } else if (imageUrl instanceof Callable) {
            image = ((Callable<ImageBuilder>) imageUrl).call();
        }

        t.dynamicReportsComponent =
                cmp.horizontalList(
                        image,
                        cmp.verticalList(
                                cmp.text(reportHeader).setStyle(bold22CenteredStyle).setHorizontalAlignment(HorizontalAlignment.LEFT),
                                cmp.text(linkUrl).setStyle(italicStyle).setHyperLink(link))).setFixedWidth(300);

        t.header = reportHeader;
        t.url = linkUrl;
        return t;
    }

    public ComponentBuilder<?, ?> getDynamicReportsComponent() {
        return dynamicReportsComponent;
    }

    public ComponentBuilder<?, ?> getFooterComponent() {
        if (footerComponent == null) {
            footerComponent = cmp.pageXofY()
                    .setStyle(
                            stl.style(boldCenteredStyle)
                                    .setTopBorder(stl.pen1Point()));
        }
        return footerComponent;
    }

    public ReportTemplateBuilder getReportTemplate() {
        StyleBuilder crosstabGroupStyle = stl.style(columnTitleStyle);
        StyleBuilder crosstabGroupTotalStyle = stl.style(columnTitleStyle)
                .setBackgroundColor(new Color(170, 170, 170));
        StyleBuilder crosstabGrandTotalStyle = stl.style(columnTitleStyle)
                .setBackgroundColor(new Color(140, 140, 140));
        StyleBuilder crosstabCellStyle = stl.style(columnStyle)
                .setBorder(stl.pen1Point());

        TableOfContentsCustomizerBuilder tableOfContentsCustomizer = tableOfContentsCustomizer()
                .setHeadingStyle(0, stl.style(rootStyle).bold());

        ReportTemplateBuilder reportTemplate = template()
                .setLocale(Locale.ENGLISH)
                .setColumnStyle(columnStyle)
                .setColumnTitleStyle(columnTitleStyle)
                .setGroupStyle(groupStyle)
                .setGroupTitleStyle(groupStyle)
                .setSubtotalStyle(subtotalStyle)
                .highlightDetailEvenRows()
                .crosstabHighlightEvenRows()
                .setCrosstabGroupStyle(crosstabGroupStyle)
                .setCrosstabGroupTotalStyle(crosstabGroupTotalStyle)
                .setCrosstabGrandTotalStyle(crosstabGrandTotalStyle)
                .setCrosstabCellStyle(crosstabCellStyle)
                .setTableOfContentsCustomizer(tableOfContentsCustomizer);

        return reportTemplate;
    }

    /**
     * Creates custom component which is possible to add to any report band component
     */
    public ComponentBuilder<?, ?> createTitleComponent(String label) {
        return cmp.horizontalList()
                .add(
                        getDynamicReportsComponent(),
                        cmp.text(label).setStyle(bold18CenteredStyle).setHorizontalAlignment(HorizontalAlignment.RIGHT))
                .newRow()
                .add(cmp.line())
                .newRow()
                .add(cmp.verticalGap(10));
    }

    public ComponentBuilder<?, ?> create2TitleComponent(String mainTitle, String label) {

        return cmp.horizontalList()
                .add(
                        cmp.verticalList(
                                cmp.text(mainTitle).setStyle(stl.style().bold().setFontSize(18).italic()),
                                cmp.text(label).setStyle(stl.style().bold().setFontSize(22))
                        ),

                        cmp.verticalList(
                                cmp.text(header).setStyle(stl.style().bold().setFontSize(16).setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)),
                                cmp.text(url).setStyle(stl.style().italic().setFontSize(10).setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP))
                        )

                )
                .newRow()
                .add(cmp.verticalGap(10));
    }

    public static CurrencyValueFormatter createCurrencyValueFormatter(String label) {
        return new CurrencyValueFormatter(label);
    }

    public static HorizontalListBuilder textCell(String text, int size, ReportStyleBuilder cellStyle, Integer cellWidth, Integer cellHeight) {
        HorizontalListBuilder list = cmp.horizontalList();

        String cellText = StringUtils.rightPad(text, size);
        cellText = StringUtils.left(cellText, size);
        for (char character : cellText.toCharArray()) {
            TextFieldBuilder<String> cell = cmp.text(String.valueOf(character)).setStyle(cellStyle).setFixedDimension(cellWidth, cellHeight);
            list.add(cell);
        }
        return list;
    }

    public static TextFieldBuilder<String> label(String text, int size, StyleBuilder style, int cellWidth) {
        TextFieldBuilder<String> label = cmp.text(text).setFixedWidth(cellWidth * size);
        if (style != null) {
            label.setStyle(style);
        }
        return label;
    }

    public static class CurrencyType extends BigDecimalType {
        private static final long serialVersionUID = 1L;

        @Override
        public String getPattern() {
            return "$ #,###.00";
        }
    }

    private static class CurrencyValueFormatter extends AbstractValueFormatter<String, Number> {
        private static final long serialVersionUID = 1L;
        private String label;

        public CurrencyValueFormatter(String label) {
            this.label = label;
        }

        @Override
        public String format(Number value, ReportParameters reportParameters) {
            return label + currencyType.valueToString(value, reportParameters.getLocale());
        }
    }
}