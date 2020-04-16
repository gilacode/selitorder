package com.avicenna;

import com.google.inject.Inject;
import com.mohiva.play.htmlcompressor.HTMLCompressorFilter;
import com.mohiva.play.xmlcompressor.XMLCompressorFilter;
import play.filters.csrf.CSRFFilter;
import play.filters.gzip.GzipFilter;
import play.filters.headers.SecurityHeadersFilter;
import play.filters.hosts.AllowedHostsFilter;
import play.http.DefaultHttpFilters;

public class DefaultFilter extends DefaultHttpFilters {

    @Inject DefaultFilter(
            CSRFFilter csrfFilter,
            AllowedHostsFilter allowedHostsFilter,
            SecurityHeadersFilter securityHeadersFilter,
            GzipFilter gzipFilter,
            HTMLCompressorFilter htmlCompressorFilter,
            XMLCompressorFilter xmlCompressorFilter) {

        super(csrfFilter,
                allowedHostsFilter,
                securityHeadersFilter,
                gzipFilter,
                htmlCompressorFilter,
                xmlCompressorFilter);
    }
}
