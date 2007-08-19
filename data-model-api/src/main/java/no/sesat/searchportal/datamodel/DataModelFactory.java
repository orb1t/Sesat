/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * DataModelFactory.java
 *
 * Created on 23 January 2007, 09:27
 *
 */

package no.sesat.searchportal.datamodel;

import no.sesat.searchportal.datamodel.access.ControlLevel;
import no.sesat.searchportal.datamodel.generic.DataObject;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.searchportal.site.Site;
import no.sesat.searchportal.site.SiteContext;
import no.sesat.searchportal.site.SiteKeyedFactory;
import no.sesat.searchportal.site.SiteKeyedFactoryInstantiationException;
import no.sesat.searchportal.site.config.PropertiesContext;
import no.sesat.searchportal.site.config.SiteConfiguration;
import org.apache.log4j.Logger;


/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public abstract class DataModelFactory implements SiteKeyedFactory{

    /**
     * The context any DataModelFactory must work within.
     */
    public interface Context extends BaseContext, SiteContext, PropertiesContext {
    }

   // Constants -----------------------------------------------------

    private static final Map<Site, DataModelFactory> INSTANCES = new HashMap<Site,DataModelFactory>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static final String DATA_MODEL_FACTORY_IMPL = "sesam.datamodel.impl";

    private static final Logger LOG = Logger.getLogger(DataModelFactory.class);

    // Attributes ----------------------------------------------------

    private final Context context;

    // Static --------------------------------------------------------

    /**
     * 
     * @param cxt 
     * @return 
     * @throws no.sesat.searchportal.site.SiteKeyedFactoryInstantiationException 
     */
    public static DataModelFactory valueOf(final Context cxt) throws SiteKeyedFactoryInstantiationException {

        final Site site = cxt.getSite();
        assert null != site;

        DataModelFactory instance;
        try{
            INSTANCES_LOCK.readLock().lock();
            instance = INSTANCES.get(site);
        }finally{
            INSTANCES_LOCK.readLock().unlock();
        }

        if (instance == null) {
            try{
                INSTANCES_LOCK.writeLock().lock();

                instance = newInstance(cxt);

                // update the store of DataModelFactory instances
                INSTANCES.put(cxt.getSite(), instance);

            }finally{
                INSTANCES_LOCK.writeLock().unlock();
            }
        }
        return instance;
    }

    /**
     * 
     * @param site 
     * @return 
     */
    public boolean remove(final Site site){

        try{
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }


    // Constructors --------------------------------------------------

    private DataModelFactory(){
        throw new IllegalArgumentException("Must use constructor with Context argument.");
    }

    /** Creates a new instance of DataModelToolkitFactory 
     * @param cxt 
     */
    protected DataModelFactory(final Context cxt) {

        context = cxt;
    }

    // Public --------------------------------------------------------

    /**
     * 
     * @return 
     */
    public abstract DataModel instantiate();

    /**
     * <pre>
     * Example usage:
     *     DataModelFactory dm = DataModelFactory.valueOf(null);
     *     BrowserDataObject bdo = dm.instantiate(
     *       BrowserDataObject.class,
     *       new DataObject.Property("locale", null),
     *       new DataObject.Property("supportedLocales", null));
     * </pre>
     ** @param cls 
     * @param properties 
     * @return 
     */
    public abstract <T> T instantiate(final Class<T> cls, DataObject.Property... properties);
    
    /** Lets the datamodel instance know that it has moved on and is now being accessed by a different level in the 
     * control process stack.
     * 
     * <b>This method is only to be used by SESAT classes, not skin implementations!</b>
     * 
     ** @param datamodel 
     * @return 
     */
    public abstract DataModel assignControlLevel(final DataModel datamodel, final ControlLevel controlLevel);

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    /** Constructs an DataModelFactory implementation.
     * Matching the class as specified
     * by the value for DATA_MODEL_FACTORY_IMPL in the Site's configuration.properties.
     **/
    private static DataModelFactory newInstance(final Context cxt) throws SiteKeyedFactoryInstantiationException{

        try{

            final SiteConfiguration siteConf = SiteConfiguration.valueOf(
                    ContextWrapper.wrap(SiteConfiguration.Context.class, cxt));

            final String clsName = siteConf.getProperty(DATA_MODEL_FACTORY_IMPL);
            LOG.info("constructing for " + cxt.getSite() + " instance of " + clsName);
            final Class<DataModelFactory> cls = (Class<DataModelFactory>) Class.forName(clsName);

            return cls.getDeclaredConstructor(Context.class).newInstance(cxt);


        }catch (ClassNotFoundException ex) {
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }catch (NoSuchMethodException ex) {
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }catch (InstantiationException ex) {
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }catch(IllegalArgumentException ex){
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }catch(IllegalAccessException ex){
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }catch(InvocationTargetException ex){
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }
    }

    // Inner classes -------------------------------------------------

}