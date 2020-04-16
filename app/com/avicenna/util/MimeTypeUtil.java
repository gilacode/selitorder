package com.avicenna.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.Logger;

import java.util.*;

@Singleton
public class MimeTypeUtil {

    private static int count = 1;

    private static String defaultMimeType = "text/html";

    private static Map mimeTypeMap = null;

    @Inject
    public MimeTypeUtil() {
        Logger.debug(this.getClass().getSimpleName() + " instantiated "+count+" time(s)");
        count++;

        mimeTypeMap = new HashMap(161);
        mimeTypeMap.put("ai", "application/postscript");
        mimeTypeMap.put("aif", "audio/x-aiff");
        mimeTypeMap.put("aifc", "audio/x-aiff");
        mimeTypeMap.put("aiff", "audio/x-aiff");
        mimeTypeMap.put("asc", "text/plain");
        mimeTypeMap.put("asf", "video/x.ms.asf");
        mimeTypeMap.put("asx", "video/x.ms.asx");
        mimeTypeMap.put("au", "audio/basic");
        mimeTypeMap.put("avi", "video/x-msvideo");
        mimeTypeMap.put("bcpio", "application/x-bcpio");
        mimeTypeMap.put("bin", "application/octet-stream");
        mimeTypeMap.put("cab", "application/x-cabinet");
        mimeTypeMap.put("cdf", "application/x-netcdf");
        mimeTypeMap.put("class", "application/java-vm");
        mimeTypeMap.put("cpio", "application/x-cpio");
        mimeTypeMap.put("cpt", "application/mac-compactpro");
        mimeTypeMap.put("crt", "application/x-x509-ca-cert");
        mimeTypeMap.put("csh", "application/x-csh");
        mimeTypeMap.put("css", "text/css");
        mimeTypeMap.put("csv", "text/comma-separated-values");
        mimeTypeMap.put("dcr", "application/x-director");
        mimeTypeMap.put("dir", "application/x-director");
        mimeTypeMap.put("dll", "application/x-msdownload");
        mimeTypeMap.put("dms", "application/octet-stream");
        mimeTypeMap.put("doc", "application/msword");
        mimeTypeMap.put("dtd", "application/xml-dtd");
        mimeTypeMap.put("dvi", "application/x-dvi");
        mimeTypeMap.put("dxr", "application/x-director");
        mimeTypeMap.put("eps", "application/postscript");
        mimeTypeMap.put("etx", "text/x-setext");
        mimeTypeMap.put("exe", "application/octet-stream");
        mimeTypeMap.put("ez", "application/andrew-inset");
        mimeTypeMap.put("gif", "image/gif");
        mimeTypeMap.put("gtar", "application/x-gtar");
        mimeTypeMap.put("gz", "application/gzip");
        mimeTypeMap.put("gzip", "application/gzip");
        mimeTypeMap.put("hdf", "application/x-hdf");
        mimeTypeMap.put("htc", "text/x-component");
        mimeTypeMap.put("hqx", "application/mac-binhex40");
        mimeTypeMap.put("html", "text/html");
        mimeTypeMap.put("htm", "text/html");
        mimeTypeMap.put("ice", "x-conference/x-cooltalk");
        mimeTypeMap.put("ief", "image/ief");
        mimeTypeMap.put("iges", "model/iges");
        mimeTypeMap.put("igs", "model/iges");
        mimeTypeMap.put("jar", "application/java-archive");
        mimeTypeMap.put("java", "text/plain");
        mimeTypeMap.put("jnlp", "application/x-java-jnlp-file");
        mimeTypeMap.put("jpeg", "image/jpeg");
        mimeTypeMap.put("jpe", "image/jpeg");
        mimeTypeMap.put("jpg", "image/jpeg");
        mimeTypeMap.put("js", "application/x-javascript");
        mimeTypeMap.put("jsp", "text/plain");
        mimeTypeMap.put("kar", "audio/midi");
        mimeTypeMap.put("latex", "application/x-latex");
        mimeTypeMap.put("lha", "application/octet-stream");
        mimeTypeMap.put("lzh", "application/octet-stream");
        mimeTypeMap.put("man", "application/x-troff-man");
        mimeTypeMap.put("mathml", "application/mathml+xml");
        mimeTypeMap.put("me", "application/x-troff-me");
        mimeTypeMap.put("mesh", "model/mesh");
        mimeTypeMap.put("mid", "audio/midi");
        mimeTypeMap.put("midi", "audio/midi");
        mimeTypeMap.put("mif", "application/vnd.mif");
        mimeTypeMap.put("mol", "chemical/x-mdl-molfile");
        mimeTypeMap.put("movie", "video/x-sgi-movie");
        mimeTypeMap.put("mov", "video/quicktime");
        mimeTypeMap.put("mp2", "audio/mpeg");
        mimeTypeMap.put("mp3", "audio/mpeg");
        mimeTypeMap.put("mpeg", "video/mpeg");
        mimeTypeMap.put("mpe", "video/mpeg");
        mimeTypeMap.put("mpga", "audio/mpeg");
        mimeTypeMap.put("mpg", "video/mpeg");
        mimeTypeMap.put("ms", "application/x-troff-ms");
        mimeTypeMap.put("msh", "model/mesh");
        mimeTypeMap.put("msi", "application/octet-stream");
        mimeTypeMap.put("nc", "application/x-netcdf");
        mimeTypeMap.put("oda", "application/oda");
        mimeTypeMap.put("ogg", "application/ogg");
        mimeTypeMap.put("pbm", "image/x-portable-bitmap");
        mimeTypeMap.put("pdb", "chemical/x-pdb");
        mimeTypeMap.put("pdf", "application/pdf");
        mimeTypeMap.put("pgm", "image/x-portable-graymap");
        mimeTypeMap.put("pgn", "application/x-chess-pgn");
        mimeTypeMap.put("png", "image/png");
        mimeTypeMap.put("pnm", "image/x-portable-anymap");
        mimeTypeMap.put("ppm", "image/x-portable-pixmap");
        mimeTypeMap.put("ppt", "application/vnd.ms-powerpoint");
        mimeTypeMap.put("ps", "application/postscript");
        mimeTypeMap.put("qt", "video/quicktime");
        mimeTypeMap.put("ra", "audio/x-pn-realaudio");
        mimeTypeMap.put("ra", "audio/x-realaudio");
        mimeTypeMap.put("ram", "audio/x-pn-realaudio");
        mimeTypeMap.put("ras", "image/x-cmu-raster");
        mimeTypeMap.put("rdf", "application/rdf+xml");
        mimeTypeMap.put("rgb", "image/x-rgb");
        mimeTypeMap.put("rm", "audio/x-pn-realaudio");
        mimeTypeMap.put("roff", "application/x-troff");
        mimeTypeMap.put("rpm", "application/x-rpm");
        mimeTypeMap.put("rpm", "audio/x-pn-realaudio");
        mimeTypeMap.put("rtf", "application/rtf");
        mimeTypeMap.put("rtx", "text/richtext");
        mimeTypeMap.put("ser", "application/java-serialized-object");
        mimeTypeMap.put("sgml", "text/sgml");
        mimeTypeMap.put("sgm", "text/sgml");
        mimeTypeMap.put("sh", "application/x-sh");
        mimeTypeMap.put("shar", "application/x-shar");
        mimeTypeMap.put("silo", "model/mesh");
        mimeTypeMap.put("sit", "application/x-stuffit");
        mimeTypeMap.put("skd", "application/x-koan");
        mimeTypeMap.put("skm", "application/x-koan");
        mimeTypeMap.put("skp", "application/x-koan");
        mimeTypeMap.put("skt", "application/x-koan");
        mimeTypeMap.put("smi", "application/smil");
        mimeTypeMap.put("smil", "application/smil");
        mimeTypeMap.put("snd", "audio/basic");
        mimeTypeMap.put("spl", "application/x-futuresplash");
        mimeTypeMap.put("src", "application/x-wais-source");
        mimeTypeMap.put("sv4cpio", "application/x-sv4cpio");
        mimeTypeMap.put("sv4crc", "application/x-sv4crc");
        mimeTypeMap.put("svg", "image/svg+xml");
        mimeTypeMap.put("swf", "application/x-shockwave-flash");
        mimeTypeMap.put("t", "application/x-troff");
        mimeTypeMap.put("tar", "application/x-tar");
        mimeTypeMap.put("tar.gz", "application/x-gtar");
        mimeTypeMap.put("tcl", "application/x-tcl");
        mimeTypeMap.put("tex", "application/x-tex");
        mimeTypeMap.put("texi", "application/x-texinfo");
        mimeTypeMap.put("texinfo", "application/x-texinfo");
        mimeTypeMap.put("tgz", "application/x-gtar");
        mimeTypeMap.put("tiff", "image/tiff");
        mimeTypeMap.put("tif", "image/tiff");
        mimeTypeMap.put("tr", "application/x-troff");
        mimeTypeMap.put("tsv", "text/tab-separated-values");
        mimeTypeMap.put("txt", "text/plain");
        mimeTypeMap.put("ustar", "application/x-ustar");
        mimeTypeMap.put("vcd", "application/x-cdlink");
        mimeTypeMap.put("vrml", "model/vrml");
        mimeTypeMap.put("vxml", "application/voicexml+xml");
        mimeTypeMap.put("wav", "audio/x-wav");
        mimeTypeMap.put("wbmp", "image/vnd.wap.wbmp");
        mimeTypeMap.put("wmlc", "application/vnd.wap.wmlc");
        mimeTypeMap.put("wmlsc", "application/vnd.wap.wmlscriptc");
        mimeTypeMap.put("wmls", "text/vnd.wap.wmlscript");
        mimeTypeMap.put("wml", "text/vnd.wap.wml");
        mimeTypeMap.put("wrl", "model/vrml");
        mimeTypeMap.put("wtls-ca-certificate", "application/vnd.wap.wtls-ca-certificate");
        mimeTypeMap.put("xbm", "image/x-xbitmap");
        mimeTypeMap.put("xht", "application/xhtml+xml");
        mimeTypeMap.put("xhtml", "application/xhtml+xml");
        mimeTypeMap.put("xls", "application/vnd.ms-excel");
        mimeTypeMap.put("xml", "application/xml");
        mimeTypeMap.put("xpm", "image/x-xpixmap");
        mimeTypeMap.put("xpm", "image/x-xpixmap");
        mimeTypeMap.put("xsl", "application/xml");
        mimeTypeMap.put("xslt", "application/xslt+xml");
        mimeTypeMap.put("xul", "application/vnd.mozilla.xul+xml");
        mimeTypeMap.put("xwd", "image/x-xwindowdump");
        mimeTypeMap.put("xyz", "chemical/x-xyz");
        mimeTypeMap.put("z", "application/compress");
        mimeTypeMap.put("zip", "application/zip");
    }

    public String getMimeType(String extension) {
        String strMimeType = null;

        // get value for particular key
        strMimeType = mimeTypeMap.get(extension).toString();
        if (strMimeType == null || strMimeType.trim().length() == 0) {
            strMimeType = defaultMimeType;
        }

        return strMimeType;
    }
}
