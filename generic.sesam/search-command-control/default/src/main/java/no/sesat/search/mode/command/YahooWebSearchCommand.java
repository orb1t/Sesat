/*
 * Copyright (2008-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.mode.command;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Map;

import no.sesat.search.mode.config.YahooWebCommandConfig;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Search against Yahoo! Contextual Web Service.
 * http://developer.yahoo.com/search/web/V1/contextSearch.html
 *
 *
 * @version $Id$
 */
public class YahooWebSearchCommand extends AbstractYahooSearchCommand {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(YahooWebSearchCommand.class);
    private static final String ERR_FAILED_CREATING_URL = "Failed to create command url";

    private static final String COMMAND_URL_PATTERN =
            "/WebSearchService/V1/webSearch?appid={0}&query={1}&context={2}&"
            + "results={3}&start={4}&"
            + "format={5}&{6}{7}language={8}&{9}{10}";

    private static final String TOTALHITS_ATTRIBUTE ="totalResultsAvailable";
    private static final String RESULT_ELEMENT = "Result";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Create new command.
     *
     * @param cxt The context to execute in.
     */
    public YahooWebSearchCommand(final Context cxt) {

        super(cxt);

        setXmlRestful(
                new AbstractXmlRestful(cxt) {
                    public String createRequestURL() {

                        final YahooWebCommandConfig conf = YahooWebSearchCommand.this.getSearchConfiguration();

                        final String wrappedTransformedQuery =  YahooWebSearchCommand.this.getTransformedQuery()
                                + ' ' + YahooWebSearchCommand.this.getFilter();

                        final String site = null != conf.getSite()
                                ? "site=" + conf.getSite()
                                : null != cxt.getDataModel().getParameters().getValue("site")
                                ? "site=" + cxt.getDataModel().getParameters().getValue("site").getUtf8UrlEncoded()
                                : "";

                        try {
                            return MessageFormat.format(
                                    COMMAND_URL_PATTERN,
                                    conf.getAppid(),
                                    URLEncoder.encode(wrappedTransformedQuery, "UTF-8"),
                                    URLEncoder.encode(wrappedTransformedQuery, "UTF-8"),
                                    conf.getResultsToReturn(),
                                    YahooWebSearchCommand.this.getOffset(),
                                    conf.getFormat(),
                                    conf.getAdult() ? "adult_ok=1&" : "",
                                    conf.getSimilar() ? "similar_ok=1&" : "",
                                    conf.getLanguage(),
                                    null != conf.getCountry() ? "country=" + conf.getCountry() + "&" : "",
                                    site);

                        } catch (UnsupportedEncodingException ex) {
                            throw new SearchCommandException(ERR_FAILED_CREATING_URL, ex);
                        }
                    }
            });
    }

    // Public --------------------------------------------------------

    public ResultList<ResultItem> execute() {

        try {

            final ResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();

            if(getTransformedQuery().trim().length() > 0
                    || getFilter().trim().length() > 0
                    || "*".equals(getQuery().getQueryString())){

                final Document doc = getXmlRestful().getXmlResult();

                if (doc != null) {
                    final Element searchResponseE = doc.getDocumentElement();
                    final String totalHitsStr = searchResponseE.getAttribute(TOTALHITS_ATTRIBUTE);

                    int totalHits;
                    try {
                        totalHits = Integer.parseInt(totalHitsStr);
                    }catch(NumberFormatException e) {
                        totalHits = Integer.MAX_VALUE;
                    }
                    searchResult.setHitCount(totalHits);

                    // build results
                    final NodeList list = searchResponseE.getElementsByTagName(RESULT_ELEMENT);
                    for (int i = 0; i < list.getLength(); ++i) {
                        final Element listing = (Element) list.item(i);
                        final BasicResultItem item = createItem(listing);
                        searchResult.addResult(item);
                    }
                }
            }
            return searchResult;

        } catch (SocketTimeoutException ste) {

            LOG.error(getSearchConfiguration().getId() +  " --> " + ste.getMessage());
            return new BasicResultList<ResultItem>();

        } catch (IOException e) {
            throw new SearchCommandException(e);

        } catch (SAXException e) {
            throw new SearchCommandException(e);
        }
    }

    /** Assured that associated SearchConfiguration is always of this type. **/
    @Override
    public YahooWebCommandConfig getSearchConfiguration() {
        return (YahooWebCommandConfig)super.getSearchConfiguration();
    }

    @Override
    public String getTransformedQuery() {
        final String tq = super.getTransformedQuery();
        if(tq == null) {
            LOG.debug("transformedQuery is null, using \"\"");
            return "";
        }
        return tq;
    }


    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /**
     *
     * @param result
     * @return
     */
    protected BasicResultItem createItem(final Element result) {

        final BasicResultItem item = new BasicResultItem();

        for (final Map.Entry<String,String> entry : getSearchConfiguration().getResultFieldMap().entrySet()){

            final Element fieldE = (Element) result.getElementsByTagName(entry.getKey()).item(0);
            if(null != fieldE && fieldE.getChildNodes().getLength() >0){
                item.addField(entry.getValue(), fieldE.getFirstChild().getNodeValue());
            }
        }

        return item;
    }

    @Override
    protected String getFilter() {
        return super.getFilter();
    }

    @Override
    protected int getOffset() {
        return super.getOffset();
    }

    @Override
    protected String getParameter(String paramName) {
        return super.getParameter(paramName);
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
