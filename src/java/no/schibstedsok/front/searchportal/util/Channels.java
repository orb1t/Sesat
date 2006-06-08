/*
 * Channels.java
 *
 * Created on 29 May 2006, 12:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.util;

import com.sun.org.apache.xpath.internal.compiler.Keywords;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesContext;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.site.SiteContext;
import org.apache.log4j.Logger;

/**
 * Class that holds channels keys and names
 *
 * @author andersjj
 */
public class Channels {
    
    public interface Context extends SiteContext, PropertiesContext { };
    
    private static final Map<Site,Channels> instances = new HashMap<Site,Channels>();;
    
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();
    
    private static final Logger LOG = Logger.getLogger(Channels.class);
    
    private Context context;
    private Properties keys = new Properties();

    private static final String CHANNELS_RESOURCE = "channels.properties";

    /** Creates a new instance of Channels */
    private Channels(Context cxt) {
        INSTANCES_LOCK.writeLock().lock();
        context = cxt;
        loadChannels();
        instances.put(cxt.getSite(), this);
        INSTANCES_LOCK.writeLock().unlock();
    }

    private void loadChannels() {
        final PropertiesLoader loader =
                context.newPropertiesLoader(CHANNELS_RESOURCE, keys);
        loader.abut();
        loader.getProperties();
    }
    
    public static Channels valueOf(final Context cxt) {
        final Site site = cxt.getSite();
        INSTANCES_LOCK.readLock().lock();
        Channels instance = instances.get(site);
        INSTANCES_LOCK.readLock().unlock();
        
        if (instance == null) {
            instance = new Channels(cxt);
        }
        return instance;
    }
    
    public String getText(String channel) {
        return keys.getProperty(channel);
    }
}
