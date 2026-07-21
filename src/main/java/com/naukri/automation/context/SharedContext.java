package com.naukri.automation.context;

import com.microsoft.playwright.Page;
import com.naukri.automation.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared state holder passed between Cucumber hooks and step definitions.
 * A new instance is created per scenario (dependency-injected by Cucumber).
 */
public class SharedContext {

    private Page page;
    private final ConfigManager config = ConfigManager.getInstance();

    // Naukri-specific state carried between steps
    private Object homePage;
    private Object searchPage;
    private Object applyPage;
    private Object profilePage;
    private List<?> allListings = new ArrayList<>();
    private List<?> qualifiedJobs = new ArrayList<>();
    private List<String> appliedJobs = new ArrayList<>();

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public ConfigManager getConfig() {
        return config;
    }

    @SuppressWarnings("unchecked")
    public <T> T getHomePage() {
        return (T) homePage;
    }

    public void setHomePage(Object homePage) {
        this.homePage = homePage;
    }

    @SuppressWarnings("unchecked")
    public <T> T getSearchPage() {
        return (T) searchPage;
    }

    public void setSearchPage(Object searchPage) {
        this.searchPage = searchPage;
    }

    @SuppressWarnings("unchecked")
    public <T> T getApplyPage() {
        return (T) applyPage;
    }

    public void setApplyPage(Object applyPage) {
        this.applyPage = applyPage;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProfilePage() {
        return (T) profilePage;
    }

    public void setProfilePage(Object profilePage) {
        this.profilePage = profilePage;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAllListings() {
        return (List<T>) allListings;
    }

    public void setAllListings(List<?> allListings) {
        this.allListings = allListings;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getQualifiedJobs() {
        return (List<T>) qualifiedJobs;
    }

    public void setQualifiedJobs(List<?> qualifiedJobs) {
        this.qualifiedJobs = qualifiedJobs;
    }

    public List<String> getAppliedJobs() {
        return appliedJobs;
    }

    public void setAppliedJobs(List<String> appliedJobs) {
        this.appliedJobs = appliedJobs;
    }

    public void reset() {
        page = null;
        homePage = null;
        searchPage = null;
        applyPage = null;
        profilePage = null;
        allListings = new ArrayList<>();
        qualifiedJobs = new ArrayList<>();
        appliedJobs = new ArrayList<>();
    }
}
