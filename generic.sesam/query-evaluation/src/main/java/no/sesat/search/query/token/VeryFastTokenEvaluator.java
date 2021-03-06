/* Copyright (2005-2012) Schibsted ASA
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
package no.sesat.search.query.token;


import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.http.HTTPClient;
import no.sesat.search.query.token.AbstractEvaluatorFactory.Context;
import static no.sesat.search.query.parser.AbstractQueryParser.SKIP_REGEX;
import static no.sesat.search.query.parser.AbstractQueryParser.OPERATOR_REGEX;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * VeryFastTokenEvaluator is part of no.sesat.search.query.
 *
 * @todo make CGI_PATH easily configurable. configurable cache settings.
 *
 *
 *
 *
 * @version $Id$
 */
public final class VeryFastTokenEvaluator implements TokenEvaluator {


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(VeryFastTokenEvaluator.class);
    private static final String ERR_FAILED_INITIALISATION = "Failed reading configuration files";
    private static final String ERR_QUERY_FAILED = "Querying the fast list failed on ";
    private static final String ERR_PARSE_FAILED = "XML parsing of fast list response failed on ";

    /** The configuration file from the skin that specifies token predicate to list mappings. **/
    public static final String VERYFAST_EVALUATOR_XMLFILE = "VeryFastEvaluators.xml";
    private static final String TOKEN_HOST_PROPERTY = "tokenevaluator.host";
    private static final String TOKEN_PORT_PROPERTY = "tokenevaluator.port";
    private static final String LIST_PREFIX = "FastQT_";
    private static final String LIST_SUFFIX = "QM";
    // use the lowercase version of TokenPredicate.EXACT_PREFIX
    private static final String EXACT_PREFIX = TokenPredicate.EXACT_PREFIX.toLowerCase();
    private static final String CGI_PATH = "/cgi-bin/xsearch?sources=alone&qtpipeline=lookupword&query=";
    private static final String ERR_FAILED_TO_ENCODE = "Failed to encode query string: ";

    /** General properties to regular expressions configured. **/
    private static final int REG_EXP_OPTIONS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

    // @todo this will leak when sites are redeploy without Possom being restarted.
    // @todo move deserialisation & this map to FastQueryMatchingEvaluatorFactory
    private static final Map<Site,Map<TokenPredicate,String[]>> LIST_NAMES
            = new HashMap<Site,Map<TokenPredicate,String[]>>();
    private static final ReentrantReadWriteLock LIST_NAMES_LOCK = new ReentrantReadWriteLock();

    private static final GeneralCacheAdministrator CACHE_QUERY = new GeneralCacheAdministrator();
    private static final int REFRESH_PERIOD = 60;
    // smaller than usual as each entry can contain up to 600 values!
    private static final int CACHE_QUERY_CAPACITY = 100;


    // Attributes ----------------------------------------------------

    private final Context context;
    private final Site site;
    private final Map<String, List<TokenMatch>> analysisResult;

    // Static --------------------------------------------------------

    static{
        CACHE_QUERY.setCacheCapacity(CACHE_QUERY_CAPACITY);
    }

    // Constructors -------------------------------------------------

    /** Only possible constructor.
     *
     * @param cxt the required context
     * @param factory the factory constructing this.
     * @throws EvaluationException if the evaluation, request to the fast query matching index, fails.
     **/
    VeryFastTokenEvaluator(final Context cxt) throws EvaluationException{

        // pre-condition check

        context = cxt;
        site = cxt.getSite();

        init();

        // the responsible() method checks if we are responsible for any tokenPredicate at all.
        // if we are not then the restful request in query(..) is a waste of time and resource.
        analysisResult = responsible()
                ? queryFast(cleanString(context.getQueryString()))
                : Collections.<String, List<TokenMatch>>emptyMap();
    }

    // Public --------------------------------------------------------

    @Override
    public boolean evaluateToken(final TokenPredicate token, final String term, final String query) {

        boolean evaluation = false;

        if(!analysisResult.isEmpty()){

            final String[] listnames = getListNames(token);

            if(null != listnames){
                for(int i = 0; !evaluation && i < listnames.length; ++i){

                    final String listname = listnames[i];

                    if (analysisResult.containsKey(listname)) {
                        if (term == null) {
                            evaluation = true;
                        }  else  {

                            // HACK since DefaultOperatorClause wraps its children in parenthesis
                            final String hackTerm = cleanString(term.replaceAll("\\(|\\)",""));

                            for (TokenMatch occurance : analysisResult.get(listname)) {

                                final Matcher m = occurance.getMatcher(hackTerm);
                                evaluation = m.find() && m.start() == 0 && m.end() == hackTerm.length();

                                if (evaluation) {
                                    break;
                                }
                            }
                        }

                    }
                }
            }else{
                LOG.info(site + " does not define lists behind the token predicate " + token);
            }
        }
        return evaluation;
    }

    @Override
    public Set<String> getMatchValues(final TokenPredicate token, final String term) {

        final Set<String> values;

        if(!analysisResult.isEmpty()){
            values = new HashSet<String>();

            final String[] listnames = getListNames(token);
            if(null != listnames){
                for(int i = 0; i < listnames.length; i++){
                    final String listname = listnames[i];

                    if (analysisResult.containsKey(listname)) {

                        // HACK since DefaultOperatorClause wraps its children in parenthesis
                        final String hackTerm = cleanString(term.replaceAll("\\(|\\)",""));

                        for (TokenMatch occurance : analysisResult.get(listname)) {

                            final Matcher m = occurance.getMatcher(hackTerm);

                            if (m.find() && m.start() == 0 && m.end() == hackTerm.length()) {
                                values.add(occurance.getValue());
                            }
                        }
                    }
                }
            }
        }else{
            values = Collections.<String>emptySet();
        }
        return Collections.unmodifiableSet(values);
    }

    @Override
    public boolean isQueryDependant(final TokenPredicate predicate) {
        return predicate.name().startsWith(EXACT_PREFIX.toUpperCase());
    }

    public boolean isResponsibleFor(final TokenPredicate predicate){

        return null != getListNames(predicate);
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private void init() {

        try {
            initImpl(context);

        } catch (ParserConfigurationException ex) {
            LOG.error(ERR_FAILED_INITIALISATION, ex);
        }
    }

    static boolean initImpl(final Context cxt) throws ParserConfigurationException  {

        final Site site = cxt.getSite();
        final Site parent = site.getParent();
        final boolean parentUninitialised;

        try{
            LIST_NAMES_LOCK.readLock().lock();

            // initialise the parent loopSite's configuration
            parentUninitialised = (null != parent && null == LIST_NAMES.get(parent));

        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }

        if(parentUninitialised){
            initImpl(ContextWrapper.wrap(
                    AbstractEvaluatorFactory.Context.class,
                    new SiteContext(){
                        @Override
                        public Site getSite(){
                            return parent;
                        }
                    },
                    cxt
                ));
        }

        if(null == LIST_NAMES.get(site)){
            try{
                LIST_NAMES_LOCK.writeLock().lock();


                    // create map entry for this loopSite
                    LIST_NAMES.put(site, new HashMap<TokenPredicate,String[]>());

                    // initialise this loopSite's configuration
                    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    final DocumentBuilder builder = factory.newDocumentBuilder();
                    final DocumentLoader loader = cxt.newDocumentLoader(cxt, VERYFAST_EVALUATOR_XMLFILE, builder);
                    loader.abut();

                    LOG.info("Parsing " + VERYFAST_EVALUATOR_XMLFILE + " started");
                    final Map<TokenPredicate,String[]> listNames = LIST_NAMES.get(site);
                    final Document doc = loader.getDocument();

                    if(null != doc && null != doc.getDocumentElement()){

                        final Element root = doc.getDocumentElement();
                        final NodeList lists = root.getElementsByTagName("list");
                        for (int i = 0; i < lists.getLength(); ++i) {

                            final Element list = (Element) lists.item(i);

                            final String tokenName = list.getAttribute("token");
                            LOG.info(" ->list@token: " + tokenName);

                            TokenPredicate token;
                            try{
                                token = TokenPredicateUtility.getTokenPredicate(tokenName);

                            }catch(IllegalArgumentException iae){
                                LOG.debug(tokenName + " does not exist. Will create it. Underlying exception was " + iae);
                                token = TokenPredicateUtility.createAnonymousTokenPredicate(tokenName);
                            }

                            final String[] listNameArr = list.getAttribute("list-name").split(",");
                            LOG.info(" ->lists: " + list.getAttribute("list-name"));

                            // update each listname to the format the fast query matching servers use
                            if(null != listNameArr){
                                for(int j = 0; j < listNameArr.length; ++j){
                                    listNameArr[j] = LIST_PREFIX + listNameArr[j] + LIST_SUFFIX;
                                }

                                // put the listnames in
                                Arrays.sort(listNameArr, null);
                                listNames.put(token, listNameArr);
                            }
                        }
                    }
                    LOG.info("Parsing " + VERYFAST_EVALUATOR_XMLFILE + " finished");
            }finally{
                LIST_NAMES_LOCK.writeLock().unlock();
            }
        }
        try{
            LIST_NAMES_LOCK.readLock().lock();

            Site s = site;
            boolean evaluatorUsedAnywhere = false;

            while(!evaluatorUsedAnywhere && null != s){

                evaluatorUsedAnywhere |= 0 < LIST_NAMES.get(s).values().size();
                if(!evaluatorUsedAnywhere){
                    // prepare to go to parent
                    s = s.getParent();
                }
            }
            return evaluatorUsedAnywhere;

        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }
    }

    /**
     * Search fast and find out if the given tokens are company, firstname, lastname etc
     * @param query
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<TokenMatch>> queryFast(final String query) throws EvaluationException{

        LOG.trace("queryFast( " + query + " )");
        Map<String, List<TokenMatch>> result = null;

        if (query != null && 0 < query.length()) {

            try{
                result = (Map<String, List<TokenMatch>>) CACHE_QUERY.getFromCache(query, REFRESH_PERIOD);

            } catch (NeedsRefreshException nre) {

                boolean updatedCache = false;
                result = new HashMap<String,List<TokenMatch>>();
                String url = null;

                try {

                    final Properties props = SiteConfiguration.instanceOf(
                                    ContextWrapper.wrap(SiteConfiguration.Context.class,context)).getProperties();

                    final String host = props.getProperty(TOKEN_HOST_PROPERTY);
                    final int port = Integer.parseInt(props.getProperty(TOKEN_PORT_PROPERTY));
                    if(0 < port){

                        final HTTPClient httpClient = HTTPClient.instance(host, port);

                        final String token = URLEncoder.encode(query.replaceAll("\"", ""), "utf-8");

                        url = CGI_PATH + token;

                        final Document doc = httpClient.getXmlDocument(url);

                        NodeList l = doc.getElementsByTagName("QUERYTRANSFORMS");
                        final Element e = (Element) l.item(0);

                        l = e.getElementsByTagName("QUERYTRANSFORM");

                        for (int i = 0; i < l.getLength(); ++i) {

                            final Element trans = (Element) l.item(i);
                            final String name = trans.getAttribute("NAME");
                            final String custom = trans.getAttribute("CUSTOM");
                            final String exactname = 0 <= name.indexOf(LIST_PREFIX) && 0 < name.indexOf(LIST_SUFFIX)
                                    ? LIST_PREFIX + EXACT_PREFIX
                                        + name.substring(name.indexOf('_') + 1, name.indexOf("QM"))
                                        + LIST_SUFFIX
                                    : null;

                            if(custom.matches(".+->.*") && usesListName(name, exactname)){

                                final String match = (custom.indexOf("->") >0
                                        ? custom.substring(0, custom.indexOf("->"))
                                        : custom)
                                        // remove words made solely of characters that the parser considers whitespace
                                        .replaceAll("\\b" + SKIP_REGEX + "+\\b", " ");

                                final String value = custom.indexOf("->") > 0
                                        ? custom.substring(custom.indexOf("->") + 2)
                                        : null;

                                addMatch(name, match, value,query, result);

                                if (match.equalsIgnoreCase(query.trim())) {

                                    addMatch(exactname, match, value, query, result);
                                }
                            }
                        }
                    }
                    result = Collections.unmodifiableMap(result);
                    CACHE_QUERY.putInCache(query, result);
                    updatedCache = true;

                } catch (UnsupportedEncodingException ignore) {
                    LOG.warn(ERR_FAILED_TO_ENCODE + query);
                    result = (Map<String, List<TokenMatch>>)nre.getCacheContent();
                } catch (IOException e1) {
                    LOG.error(ERR_QUERY_FAILED + url, e1);
                    result = (Map<String, List<TokenMatch>>)nre.getCacheContent();
                    throw new EvaluationException(ERR_QUERY_FAILED + url, e1);
                } catch (SAXException e1) {
                    LOG.error(ERR_PARSE_FAILED + url, e1);
                    result = (Map<String, List<TokenMatch>>)nre.getCacheContent();
                    throw new EvaluationException(ERR_PARSE_FAILED + url, e1);
                }finally{
                    if(!updatedCache){
                        CACHE_QUERY.cancelUpdate(query);
                    }
                }
            }
        } else {
            result = Collections.<String, List<TokenMatch>>emptyMap();
        }
        return result;
    }

    private static void addMatch(
            final String name,
            final String match,
            final String value,
            final String query,
            final Map<String, List<TokenMatch>> result) {

        final String expr = "\\b" + match + "\\b";
        final Pattern pattern = Pattern.compile(expr, REG_EXP_OPTIONS);
        final String qNew = query.replaceAll("\\b" + SKIP_REGEX + "+\\b", " ");
        final Matcher m = pattern.matcher(
                // remove words made solely of characters that the parser considers whitespace
                qNew);

        while (m.find()) {
            final TokenMatch tknMatch = TokenMatch.instanceOf(name, match, value);

            if (!result.containsKey(name)) {
                result.put(name, new ArrayList<TokenMatch>());
            }

            result.get(name).add(tknMatch);

            if (result.get(name).size() % 100 == 0) {
                LOG.warn("Pattern: " + pattern.pattern()
                        + " name: " + name
                        + " query: " + query
                        + " match: " + match
                        + " query2: " + qNew);
            }
        }
    }

    private boolean usesListName(final String listname, final String exactname){

        boolean uses = false;
        try{
            LIST_NAMES_LOCK.readLock().lock();
            Site site = this.site;

            while(!uses && null != site){

                // find listnames used for this token predicate
                for(String[] listnames : LIST_NAMES.get(site).values()){
                    uses |= 0 <= Arrays.binarySearch(listnames, listname, null);
                    uses |= null != exactname && 0 <= Arrays.binarySearch(listnames, exactname, null);
                    if(uses){  break; }
                }

                // prepare to go to parent
                site = site.getParent();
            }
        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }
        return uses;
    }

    private String[] getListNames(final TokenPredicate token){

        String[] listNames = null;
        try{
            LIST_NAMES_LOCK.readLock().lock();
            Site site = this.site;

            while(null == listNames && null != site){

                // find listnames used for this token predicate
                listNames = LIST_NAMES.get(site).get(token);

                // prepare to go to parent
                site = site.getParent();
            }
        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }
        return listNames;
    }

    private String cleanString(final String string){

        // Strip out SKIP characters we are not interested in.
        // Also remove any operator characters. (SEARCH-3883 & SEARCH-3967)

        return string.toLowerCase()
                .replaceAll(" ", "xxKEEPWSxx") // Hack to keep spaces. multiple spaces always normalised.
                .replaceAll(SKIP_REGEX, " ")
                .replaceAll("xxKEEPWSxx", " ") // Hack to keep spaces.
                .replaceAll(OPERATOR_REGEX, " ")
                .replaceAll(" +", " "); // normalise
    }

    private boolean responsible(){

        boolean responsible = false;
        try{
            LIST_NAMES_LOCK.readLock().lock();
            Site loopSite = this.site;

            while(null != loopSite){

                // find listnames used for this token predicate
                responsible =  !LIST_NAMES.get(loopSite).isEmpty();
                if(responsible){ break; }

                // prepare to go to parent
                loopSite = loopSite.getParent();
            }
        }finally{
            LIST_NAMES_LOCK.readLock().unlock();
        }
        return responsible;
    }

    // Inner classes -------------------------------------------------
}
