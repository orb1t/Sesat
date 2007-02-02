/*
 * FactoryReloadsTest.java
 * JUnit based test
 *
 * Created on 5 May 2006, 07:50
 */

package no.schibstedsok.searchportal.http.servlet;


import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.http.servlet.FactoryReloads.ReloadArg;
import no.schibstedsok.searchportal.site.SiteTestCase;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.FileResourceLoader;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.*;
import no.schibstedsok.searchportal.view.config.SearchTabFactory;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class FactoryReloadsTest extends SiteTestCase {
    
    private static final Logger LOG = Logger.getLogger(FactoryReloadsTest.class);
    
    public FactoryReloadsTest(String testName) {
        super(testName);
    }

    /**
     * Test of remove method, of class no.schibstedsok.searchportal.servlet.FactoryReloads.
     */
    @Test
    public void testRemoveAll() {
        
        FactoryReloads.performReloads(genericContext(), ReloadArg.SITE_CONFIGURATION); 
        FactoryReloads.performReloads(genericContext(), ReloadArg.SEARCH_TAB_FACTORY); 
        FactoryReloads.performReloads(genericContext(), ReloadArg.SEARCH_MODE_FACTORY); 
        FactoryReloads.performReloads(genericContext(), ReloadArg.ANALYSIS_RULES_FACTORY); 
        FactoryReloads.performReloads(genericContext(), ReloadArg.REG_EXP_EVALUATOR_FACTORY); 
        // skip "velocity" because VelocityEngineFactory harcodes to URLVelocityTemplateLoader
        
    }

    private SiteContext genericContext(){
        
        // BaseContext providing SiteContext and ResourceContext.
        //  We need it casted as a SiteContext for the ResourceContext code to be happy.
        return new SiteContext(){
            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteCxt, 
                    final String resource, 
                    final Properties properties) {
                
                return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }
            public DocumentLoader newDocumentLoader(
                    final SiteContext siteCxt, 
                    final String resource, 
                    final DocumentBuilder builder) {
                
                return FileResourceLoader.newDocumentLoader(siteCxt, resource, builder);
            }
            public Site getSite() {
                return getTestingSite();
            }
        };
    }
}
