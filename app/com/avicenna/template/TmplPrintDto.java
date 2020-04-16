package com.avicenna.template;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TmplPrintDto {

    private String title;
    private List<Style> styles = new ArrayList<>();

    private PaperSize paperSize = PaperSize.LETTER;

    private String printUrl;
    private String previewUrl;

    private String reportName;

    // paper size
    public enum PaperSize {
        A5("A5"),
        A5_LANDSCAPE("A5 landscape"),
        A4("A4"),
        A4_LANDSCAPE("A4 landscape"),
        A3("A3"),
        A3_LANDSCAPE("A3 landscape"),
        LETTER("letter"),
        LETTER_LANDSCAPE("letter landscape"),
        LEGAL("legal"),
        LEGAL_LANDSCAPE("legal landscape")
        ;

        private final String size;

        PaperSize(String size) {
            this.size = size;
        }

        public String getSize() {
            return size;
        }
    }

    public String getPaperCss() {
        return "css/shared/template/page-size/"
                +StringUtils.replace(this.paperSize.getSize()," ","-")+".css";
    }

    public TmplPrintDto() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }

    public String getPrintUrl() {
        return printUrl;
    }

    public void setPrintUrl(String printUrl) {
        this.printUrl = printUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public PaperSize getPaperSize() {
        return paperSize;
    }

    public void setPaperSize(PaperSize paperSize) {
        this.paperSize = paperSize;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public static class Style {

        private String path;

        public Style() { }

        public Style(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

}
