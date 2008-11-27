/* Copyright (2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 * AbstractXmlSearchConfiguration.java
 *
 * Created on June 12, 2006, 10:58 AM
 *
 */

package no.sesat.search.mode.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.CommandConfig.Controller;
import org.w3c.dom.Element;

/** Searching against a Solr index using the Solrj client.
 *
 * @version $Id$
 */
@Controller("SolrSearchCommand")
public class SolrCommandConfig extends CommandConfig {


    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    private String serverUrl = "";

    private String filteringQuery = "";

    private final Map<String,String> sort = new HashMap<String,String>();

    private Integer timeout = Integer.MAX_VALUE;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    /**
     * Getter for property serverUrl.
     * The value returned is the key used
     *   to look up the real value via SiteConfiguration(via in configuration.properties)
     *
     * @return Value of property serverUrl.
     */
    public String getServerUrl() {
        return this.serverUrl;
    }

    /**
     * @see #getServerUrl()
     * @param serverUrl New value of property serverUrl.
     */
    public void setServerUrl(final String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /** The filter query.
     * Used like an additional filter to narrow the query down.
     * @see org.apache.solr.client.solrj.SolrQuery#setFilterQueries(String...)
     *
     * TODO change bean property from String to String[] to support multiple filtering queries.
     *
     * @return
     */
    public String getFilteringQuery(){
        return filteringQuery;
    }

    /** @see #getFilteringQuery()
     *
     * @param filteringQuery
     */
    public void setFilteringQuery(final String filteringQuery){
        this.filteringQuery = filteringQuery;
    }

    /** @see #setFieldFilters(java.lang.String[])
     *
     * @return Value of map property sort.
     */
    public Map<String,String> getSortMap() {
        return Collections.unmodifiableMap(sort);
    }

    public void clearSort(){
        sort.clear();
    }

    /**
     * Syntax: sort="fieldName1 asc, fieldName2 desc"
     *
     * Just "fieldName1" will presume ascending (asc) order.
     *
     * @param sortFields Array of sort fields.
     */
    public void setSort(final String[] sortFields) {

        for (String string : sortFields) {
            setSort(string);
        }
    }

    /**
     *
     * @param integer
     */
    public void setTimeout(final Integer integer){
        timeout = integer;
    }

    /** @see #setTimeout(java.lang.Integer)
     *
     * @return
     */
    public int getTimeout(){
        return timeout;
    }

    @Override
    public SearchConfiguration readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit,
            final Context context) {

        if(null!=inherit && inherit instanceof SolrCommandConfig){
            sort.putAll(((SolrCommandConfig)inherit).getSortMap());
        }

        ModesSearchConfigurationDeserializer.readSearchConfiguration(this, element, inherit);

        if (element.hasAttribute("sort")) {
            if (element.getAttribute("sort").length() == 0) {
               clearSort();
            }
        }

        return this;
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private void setSort(final String sortFieldAndOrder) {

        final String parsed[] = sortFieldAndOrder.trim().split(" ");
        final String field = parsed[0].trim();
        sort.put(field, (parsed.length > 1) ? parsed[1].trim() : "asc");
    }

    // Inner classes -------------------------------------------------

}
