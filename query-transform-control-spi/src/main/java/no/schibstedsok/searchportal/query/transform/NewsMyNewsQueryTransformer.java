// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.Clause;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Expects a parameter to be on the format: ((&lt;value&gt;::&lt;type&gt;)(||&lt;value&gt;::&lt;type&gt;)*)?
 * <p/>
 * It the type matches the type for this transformer, it will use the value(s) to transform to a new query.
 * <p/>
 * <b>Note:</b> This queryTransformer ignores all earlier transforms on the query. All transforms to the resulting
 * query should be done after this.
 * 
 * @author geir
 * @version $Id$
 */
public final class NewsMyNewsQueryTransformer extends AbstractQueryTransformer {
    
    private static final Logger LOG = Logger.getLogger(NewsMyNewsQueryTransformer.class);
    private static final Pattern queryPattern = Pattern.compile("(?:\\A|\\|)([^\\|]+)\\:{2}([^\\|]+)\\|?");

    private final NewsMyNewsQueryTransformerConfig config;

    /**
     * 
     * @param config 
     */
    public NewsMyNewsQueryTransformer(final QueryTransformerConfig config) {
        this.config = (NewsMyNewsQueryTransformerConfig) config;
    }

    /**
     * 
     * @param clause 
     */
    public void visitImpl(final Clause clause) {
        
        final String myNews = (String) getContext().getDataModel().getJunkYard().getValue(config.getQueryParameter());
        
        // FIXME use instead the following
        //final String myNews 
        //        = (String) getContext().getDataModel().getParameters().getValue(config.getQueryParameter()).getString();
        
        LOG.debug("Transforming query according to query = " + myNews);
        final String transformedQuery = transformQuery(myNews);
        
        if (transformedQuery != null) {
            LOG.debug("New query is: '" + transformedQuery + '\'');
            
            for (Clause keyClause : getContext().getTransformedTerms().keySet()) {
                getContext().getTransformedTerms().put(keyClause, "");
            }
            
            if (transformedQuery.length() > 0) {
                getContext().getTransformedTerms().put(getContext().getQuery().getFirstLeafClause(), transformedQuery);
            }
        }
    }

    /**
     * 
     * @return 
     */
    protected int getOffset() {
        
        return null != getContext().getDataModel().getJunkYard().getValue("offset")
                ? Integer.parseInt((String) getContext().getDataModel().getJunkYard().getValue("offset"))
                : 0;
    }

    private String transformQuery(final String myNews) {
        
        final StringBuilder newQuery = new StringBuilder();
        
        if (myNews != null && myNews.length() > 0) {
            
            final Matcher matcher = queryPattern.matcher(myNews);
            if (config.getPosition() == -1) {
                
                LOG.debug("No position. Appending all matches.");
                                
                while (matcher.find()) {
                    if (matcher.group(2).equals(config.getType())) {
                        if (newQuery.length() == 0) {
                            if (config.getFilterField() != null) {
                                newQuery.append("filter(").append(config.getFilterField()).append(":or(");
                            }
                        } else {
                            newQuery.append(',');
                        }
                        newQuery.append('\"').append(matcher.group(1)).append('\"');
                    }
                }
                if (newQuery.length() > 0 && config.getFilterField() != null) {
                    newQuery.append("))");
                }
                
            } else {
                
                LOG.debug("Position is: " + config.getPosition());

                int curPos = 0;
                final int offset = getOffset();
                final int pos = config.getPosition() + offset;
                
                while (matcher.find() && curPos < pos) {
                    // Just searching for the correct match.
                    curPos++;
                }
                
                try{
                    // YUK! any way to do this and avoid having to wrap in try-catch?? XXX
                    LOG.debug("Group at pos: " + pos + " is " + matcher.group(0) + ", looking for " + config.getType());
                    
                }catch(IllegalStateException ise){
                    LOG.trace(ise.getMessage(), ise);
                }
                
                if (matcher.groupCount() > 0 && matcher.group(2).equals(config.getType())) {
                    
                    final String fastCompatibleString = matcher.group(1).replace('?', ' ');
                    
                    return null == config.getFilterField() 
                            ? fastCompatibleString 
                            : config.getFilterField() + ":(\"" + fastCompatibleString + "\")";                    
                }
            }
        }
        return newQuery.toString();
    }
}
