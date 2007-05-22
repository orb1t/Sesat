// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.io.Serializable;
import java.util.Map;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * A simple implementation of a search result.
 * Is not multi-thread safe. 
 * All fields (of all types) handled by superclass BasicSearchResultItem.
 *
 * @param T the type of ResultItem the ResultList contains.
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class BasicSearchResult<T extends ResultItem> extends BasicSearchResultItem implements ResultList<T> {

    private static final Logger LOG = Logger.getLogger(BasicSearchResult.class);

    private int hitCount = -1;
    private final List<T> results = new ArrayList<T>();
    private final Map<String,List<WeightedSuggestion>> spellingSuggestions = new HashMap<String,List<WeightedSuggestion>>();
    private final List<Suggestion> querySuggestions = new ArrayList<Suggestion>();

    private final List<WeightedSuggestion> relevantQueries = new ArrayList<WeightedSuggestion>(); 
    
    /** Plain constructor.
     * 
     */
    public BasicSearchResult(){}
    
    /** Copy constructor. 
     * Does not copy results, spellingSuggestions, querySuggestions, or relevantQueries.
     *
     * ** @param copy 
     */
    public BasicSearchResult(final ResultItem copy){
        super(copy);
    }

    /** {@inheritDoc} **/
    public void setHitCount(final int docCount) {
        this.hitCount = docCount;
    }

    /** {@inheritDoc} **/
    public int getHitCount() {
        return hitCount;
    }

    /** {@inheritDoc} **/
    public void addResult(final T item) {
        results.add(item);
    }

    public void addResults(List<? extends T> items){
        results.addAll(items);
    }
    
    public void replaceResult(final T original, final T theNew){
        
        if(original != theNew){
            // if the instances vary then replace
            results.set(results.indexOf(original), theNew);
        }
    }
    
    public void removeResult(final T item){
        results.remove(item);
    }

    public void removeResults(){
        results.clear();
    }

    public void sortResults(final Comparator comparator){
        Collections.sort(results, comparator);
    }

    /** {@inheritDoc} **/
    public void addSpellingSuggestion(final WeightedSuggestion suggestion) {
        
        if (spellingSuggestions.containsKey(suggestion.getOriginal())) {
            final List<WeightedSuggestion> exising = spellingSuggestions.get(suggestion.getOriginal());
            exising.add(suggestion);
        } else {
            final List<WeightedSuggestion> existingSuggestions = new ArrayList<WeightedSuggestion>();
            existingSuggestions.add(suggestion);
            spellingSuggestions.put(suggestion.getOriginal(), existingSuggestions);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Spelling suggestions " + suggestion + " " + "added");
        }
    }

    /** {@inheritDoc} **/
    public List<WeightedSuggestion> getSpellingSuggestions() {
        
        final List<WeightedSuggestion> result = new ArrayList<WeightedSuggestion>();
        for(List<WeightedSuggestion> v : spellingSuggestions.values()){
            result.addAll(v);
        }
        return result;
    }
    
    /** {@inheritDoc} **/
    public Map<String,List<WeightedSuggestion>> getSpellingSuggestionsMap() {
        return spellingSuggestions;
    }

    /** {@inheritDoc} **/
    public Collection<Suggestion> getQuerySuggestions() {
       return Collections.unmodifiableList(querySuggestions);
    }

    /** {@inheritDoc} **/
    public void addQuerySuggestion(final Suggestion query) {
        querySuggestions.add(query);
    }

    /** {@inheritDoc} **/
    public List<T> getResults() {
        return Collections.unmodifiableList(results);
    }

    /** {@inheritDoc} **/
    @Override
    public BasicSearchResult<T> addField(final String field, final String value) {
        
        super.addField(field, value);
        return this;
    }

    @Override
    public BasicSearchResult<T> addObjectField(final String field, final Serializable value) {
        
        super.addObjectField(field, value);
        return this;
    }    

    @Override
    public BasicSearchResult<T> addToMultivaluedField(final String field, final String value) {
        
        super.addToMultivaluedField(field, value);
        return this;
    }
    
    /**
     * 
     * @param query 
     */
    public void addRelevantQuery(final WeightedSuggestion query) {
        relevantQueries.add(query);
    }

    /**
     * Get the relevantQueries.
     *
     * @return the relevantQueries.
     */
    public List<WeightedSuggestion> getRelevantQueries() {
        
        Collections.sort(relevantQueries);
        return Collections.unmodifiableList(relevantQueries);
    }

}

