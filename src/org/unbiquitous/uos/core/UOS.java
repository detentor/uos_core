package org.unbiquitous.uos.core;


import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.unbiquitous.uos.core.adaptabitilyEngine.AdaptabilityEngine;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.connectivity.ConnectivityManager;
import org.unbiquitous.uos.core.driverManager.DriverManagerException;
import org.unbiquitous.uos.core.messageEngine.MessageEngine;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.ontologyEngine.Ontology;

/**
 * 
 * This class centralizes the process of initialization e teardown of the
 * middleware and it's dependencies.
 * 
 * @author Fabricio Nogueira Buzeto
 * 
 */
public class UOS {

	private static final Logger logger = Logger.getLogger(UOS.class);

	private static String DEFAULT_UBIQUIT_BUNDLE_FILE = "ubiquitos";

    private Ontology ontology;
    private ResourceBundle properties;

	private UOSComponentFactory factory;
	private List<UOSComponent> components ;
	
	public static void main(String[] args) throws Exception{
		new UOS().init();
	}
	
	/**
	 * Initializes the components of the uOS middleware using 'ubiquitos' as the
	 * name of the resouce bundle to be used.
	 * 
	 * @throws ContextException
	 */
	public void init() throws ContextException {
		init(DEFAULT_UBIQUIT_BUNDLE_FILE);
	}

	/**
	 * Initializes the components of the uOS middleware acording to the
	 * resourceBundle informed.
	 * 
	 * @param resourceBundleName
	 *            Name of the <code>ResourceBundle</code> to be used for finding
	 *            the properties of the uOS middleware.
	 * @throws ContextException
	 */
	@SuppressWarnings("serial")
	public void init(ResourceBundle resourceBundle) throws ContextException {
		
		try {
			this.properties	= resourceBundle;
			this.factory		= new UOSComponentFactory(resourceBundle);
			this.components = new ArrayList<UOSComponent>(){
				{
					add(factory.get(ConnectionManagerControlCenter.class));
					add(factory.get(CurrentDeviceInitializer.class));
					add(factory.get(MessageEngine.class));
					add(factory.get(AdaptabilityEngine.class));
				}
			};
			/*---------------------------------------------------------------*/
			/* 							CREATE								 */
			/*---------------------------------------------------------------*/
			
			for(UOSComponent component:components){
				component.create(properties);
			}
			
			/*---------------------------------------------------------------*/
			/* 							INIT								 */
			/*---------------------------------------------------------------*/
			for(UOSComponent component:components){
				component.init(factory);
			}
			

			/*---------------------------------------------------------------*/
			
            initOntology();
                        
            /*---------------------------------------------------------------*/
			/* 							START								 */
			/*---------------------------------------------------------------*/
			for(UOSComponent component:components){
				component.start();
			}
            
			// Start Connectivity Manager
			logger.debug("Initializing ConnectivityManager");
			initConnectivityManager();

			// Start Radar Control Center
			logger.debug("Initializing RadarControlCenter");

		} catch (DriverManagerException e) {
			logger.error(e);
			throw new ContextException(e);
		} catch (Exception e) {
			throw new ContextException(e);
		} 
	}

	/**
	 * Initializes the components of the uOS middleware acording to the
	 * resourceBundle informed.
	 * 
	 * @param resourceBundleName
	 *            Name of the <code>ResourceBundle</code> to be used for finding
	 *            the properties of the uOS middleware.
	 * @throws ContextException
	 */
	public void init(String resourceBundleName) throws ContextException {
		// Log start Message
		logger.info("..::|| Starting uOS ||::..");

		// Get the resource Bundle
		logger.debug("Retrieving Resource Bundle Information");
		ResourceBundle resourceBundle = ResourceBundle
				.getBundle(resourceBundleName);

		init(resourceBundle);
	}

	private void initConnectivityManager() {
		//Read proxying attribute from the resource bundle
		boolean doProxying = false;

		try {
			if ((properties.getString("ubiquitos.connectivity.doProxying")).equalsIgnoreCase("yes")) {
				doProxying = true;
			}
		} catch (MissingResourceException e) {
			logger.info("No proxying attribute found in the properties. Proxying set as false.");
		}

		factory.get(ConnectivityManager.class)
			.init(	this, factory.gateway(), doProxying);
	}

	private void initOntology() {
		if (!properties.containsKey("ubiquitos.ontology.path"))
			return;
		ontology = factory.get(Ontology.class);
		// ontology.setDriverManager(driverManager);
		ontology.initializeOntology();
	}
        
	/**
	 * Shutdown the middleware infrastructure.
	 */
	public void tearDown() {

		/*---------------------------------------------------------------*/
		/* 							STOP								 */
		/*---------------------------------------------------------------*/
		for(UOSComponent component:components){
			component.stop();
		}
	}

	/**
	 * @return The Gateway used by Drivers and Applications to interact with the
	 *         Smart Space
	 */
	public Gateway getGateway() {
		return factory.gateway();
	}

	public UOSComponentFactory getFactory() {
		return factory;
	}
	
}
