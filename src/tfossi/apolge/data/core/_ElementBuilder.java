/**
 * _ElementBuilder.java
 * branch master
 * APolGe
 * tfossi-team
 * licence GPLv3   
 */
package tfossi.apolge.data.core;

import static tfossi.apolge.common.constants.ConstValue.LFCR;
import static tfossi.apolge.common.constants.ConstValue.LOGGER;
import static tfossi.apolge.common.constants.ConstValueExtension.VERSION;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import tfossi.apolge.common.scripting.vp.VP_Tokenlist;

/**
 * Enthält einen Elelemtbauplan und erstellt Elemente
 * 
 * @author tfossi
 * @version 29.10.2015
 * @modified -
 * @since Java 1.6
 */
public class _ElementBuilder {
	
	/** firstPassMap */
	private final Map<String, VP_Tokenlist<?>> firstPassMap = new HashMap<String,VP_Tokenlist<?>>();

	/**
	 * Bestandteile des Elements
	 *
	 * @author tfossi
	 * @version 26.11.2015
	 * @modified -
	 * @since Java 1.6
	 */
	enum scpt {
		/** ELEMENT */
		ELEMENT, /** EIGENSCHAFT */
		EIGENSCHAFT, /** DATENTYP */
		DATENTYP, /** INITIALDATEN */
		INITIALDATEN, /** INITIALPARAMETER */
		INITIALPARAMETER;

	}
	
	/**
	 * Füge eine 0-Pass-Eigenschaft hinzu
	 * @param key
	 * 			Name der Eigenschaft
	 * @param valuetokens
	 * 			Wert der Eigenschaft
	 * @modified - 
	 */
	public void addEigenschaften(final String key, final VP_Tokenlist<?> valuetokens){
		if(LOGGER)
			logger.info("Register "+key);
		this.firstPassMap.put(key, valuetokens);

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String rc = new String();
		
		for(String key :this.firstPassMap.keySet())
			rc += key+": "+this.firstPassMap.get(key)+LFCR;
		
		return rc;
	}
	
	// ---- Selbstverwaltung --------------------------------------------------
	/** serialVersionUID */
	@SuppressWarnings("unused")
	private final static long serialVersionUID = VERSION;

	/** logger */
	private final static Logger logger = Logger.getLogger(_ElementBuilder.class
			.getPackage().getName() + "._ElementBuilder");


}

