/*
 * Copyright (2005) Schibsted S�k AS
 * 
 */
package no.schibstedsok.front.searchportal.util;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PagingDisplayHelper {

    private int pageSize = 10;
    private int maxPages = 10;
    private int windowSize = 3;

    private int numberOfResults;
    private int currentPage;

    public PagingDisplayHelper(int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    public PagingDisplayHelper(int currentPage, int numberOfResults) {
        this.currentPage = currentPage;
        this.numberOfResults = numberOfResults;
    }

    public PagingDisplayHelper(int pageSize, int maxPages, int currentPage, int numberOfResults) {
        this.pageSize = pageSize;
        this.numberOfResults = numberOfResults;
        this.maxPages = maxPages;
        this.currentPage = currentPage;
    }


    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getNumberOfPages() {
        return numberOfResults / pageSize + 1;
    }

    public int getNumberOfPagesLimited() {
        return getNumberOfPages() > maxPages ? maxPages : getNumberOfPages();
    }

    public boolean isFirstPage() {
        return currentPage == 1;
    }

    public boolean isLastPage() {
        return currentPage == getNumberOfPages();
    }

    public void setCurrentOffset(int offset) {
        currentPage = offset / pageSize + 1;
    }

    public int getOffsetOfNextPage() {
        return currentPage * pageSize + 1;
    }

    public int getOffsetOfPreviousPage() {
        return (currentPage - 2) * pageSize;
    }


    public int getOffsetOfPage(int page) {
        return (page - 1) * pageSize;
    }

    public int getFirstVisiblePage() {
        int first = (getCurrentPage() - 1) / pageSize * maxPages + 1;

        if (first != 1) {
            first -= windowSize;
        }

        return first;
    }

    public int getLastVisiblePage() {
        int pageSet = getFirstVisiblePage() + maxPages;

        int first = getFirstVisiblePage();

        if (first != 1) {
            pageSet += windowSize;
        }

        return getNumberOfPages()  < pageSet ? getNumberOfPages() : pageSet;
    }

    public int getMaxPages() {
        return maxPages;
    }
}
