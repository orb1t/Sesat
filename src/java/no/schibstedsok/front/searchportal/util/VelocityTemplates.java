/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.util;

/**
 * A VelocityTemplates.
 * 
 * All our Velocity templates used and their corresponding file name.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public final class VelocityTemplates {

	// Our various template used for merging data and design 
	
	/** @deprecated */
	public static final String ALL_COLLECTIONS_SEARCH = "fast-search-result.vm";			
	
	public static final String WIKI_COLLECTION_SEARCH = "fast-wiki-search-result.vm";			
	public static final String MEDIA_COLLECTION_SEARCH = "fast-retriever-search-result.vm";	
	public static final String WEBCRAWL_COLLECTION_SEARCH = "fast-webcrawl-result.vm";
	public static final String GENERIC_SEARCH = "search-results.vm";			
	public static final String GLOBAL_SEARCH = "sensis-search-result.vm";
	public static final String GLOBAL_COUNT = "global-counter.vm";
	public static final String LOCAL_COUNT = "local-counter.vm";
	public static final String TV_SEARCH = "tv-enrich.vm";

    public static final String COMPANIES_COLLECTION_SEARCH = "fast-companies-search-result.vm";

    public static final String INFOPAGE = "infopage.vm";
    public static final String PERSONS_COLLECTION_SEARCH = "fast-persons-search-result.vm";
    public static final String MOREOVER_COLLECTION_SEARCH = "fast-moreover.vm";
    public static final String PERSON = "person.vm";
    public static final String NORDIC_NEWS_COLLECTION_SEARCH = "fast-nordic.vm";
}
