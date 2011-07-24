package com.blork.anpod.search;

import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.provider.SearchRecentSuggestions;

// TODO: Auto-generated Javadoc
/**
 * The Class ApodSuggestionProvider.
 */
public class ApodSuggestionProvider extends SearchRecentSuggestionsProvider {
	
	/** The AUTH. */
	private static String AUTH="com.blork.anpod.search.ApodSuggestionProvider";
	
	/**
	 * Gets the bridge.
	 *
	 * @param ctxt the ctxt
	 * @return the bridge
	 */
	static SearchRecentSuggestions getBridge(Context ctxt) {
		return(new SearchRecentSuggestions(ctxt, AUTH, DATABASE_MODE_QUERIES));
	}
	
	/**
	 * Instantiates a new apod suggestion provider.
	 */
	public ApodSuggestionProvider() {
		super();
		setupSuggestions(AUTH, DATABASE_MODE_QUERIES);
	}
}
