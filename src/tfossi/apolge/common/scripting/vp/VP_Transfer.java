/**
 * VP_Transfer.java
 * Branch master
 * APolGe
 * tfossi-team
 * licence GPLv3   
 */
package tfossi.apolge.common.scripting.vp;

import static tfossi.apolge.common.constants.ConstValue.CLOSE;
import static tfossi.apolge.common.constants.ConstValue.KOMMA;
import static tfossi.apolge.common.constants.ConstValue.LFCR;
import static tfossi.apolge.common.constants.ConstValue.LOGGER;
import static tfossi.apolge.common.constants.ConstValue.LOGTAB;
import static tfossi.apolge.common.constants.ConstValue.NTAB;
import static tfossi.apolge.common.constants.ConstValue.OPEN;
import static tfossi.apolge.common.constants.ConstValueExtension.VERSION;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import tfossi.apolge.common.scripting.t.Table;
import tfossi.apolge.common.scripting.vp.pm.FuncPType;
import tfossi.apolge.common.scripting.vp.pm.Operation;
import tfossi.apolge.data.core.Element;

/**
 * Übersetzt Anweisungen des _ElementtBuilder<br>
 * Es werden nur statische Anweisungen übersetzt bzw. vorab berechnet, keine
 * Addresswerte oder Zufallszahlen!
 * 
 * 
 * 
 * 
 * 
 * @author tfossi
 * @version 21.08.2014
 * @modified -
 * @since Java 1.6
 */
public class VP_Transfer {
	
	/**
	 * Übersetzt die Blockbeschreibung in ausgeführten und ausführbare
	 * Anweisungen.<br>
	 * 
	 * TODO Comment
	 * <ol>
	 * <li>Klammern <code>OPEN</code> <code>CLOSE</code> auflösen</li>
	 * <li></li>
	 * </ol>
	 * @param atomname 
	 * 			Eigenschaftsname
	 * @param block
	 *            ????
	 * 
	 * @param valuetokens
	 *            Die Tokens aus dem aktuellen Blockelement Key=
	 * @param quotes
	 *            Liste der Strings
	 * @param mode
	 *            0,2,3
	 * @return Liefert
	 *         <ul>
	 *         <li>Ready-Status für die Umsetzung.<br>
	 *         <code>true</code> ist vollständig umgesetzt, <code>false</code>
	 *         ist noch ein PASS erforderlich.</li>
	 *         <li>Eine angepasste <b>tokenlines</b> mit den Ergebnissen der
	 *         Berechnung der Funktionen in Werten oder Listen.</li>
	 *         </ul>
	 * @modified -
	 */
	public int transfer(String atomname, Table block, VP_Tokenlist<Object> valuetokens,
			List<String> quotes, byte mode) {
		return parseVariable(atomname, block, valuetokens, 0, valuetokens.size() - 1,
				quotes, mode);

	}

	/**
	 * Setze bekannte VAriable ein.
	 * @param atomname 
	 * 				????
	 * @param block
	 *            ????
	 * @param valuetokens
	 *            ????
	 * @param firstElement
	 *            ????
	 * @param lastElement
	 *            ????
	 * @param quotes
	 *            ????
	 * @param mode
	 *            ????
	 * @return ????
	 * @modified -
	 */
	public int parseVariable(String atomname, Table block, VP_Tokenlist<Object> valuetokens,
			final int firstElement, int lastElement, List<String> quotes,
			final int mode) {
		
		if (valuetokens == null)
			return -1;
		
		// Lauf 2PASS (mode=1) ist für 1PASS valuetokens nicht notwendig, da gelöst
		if(mode==1 && !(valuetokens.isTwoPass()|| valuetokens.isThreePass()))
			return -1;

		// Lauf 3PASS (mode=2) ist für 1PASS und 2PASS valuetokens nicht notwendig, da gelöst
		if(mode==2 && ! valuetokens.isThreePass())
			return -1;

		// Gehe jeden Listen-Eintrag durch und checke, ob es eine Variable ist.
		// wenn ja, Ersetze den Eintrag durch die Variable
		for (int ndx = firstElement; ndx <= lastElement; ndx++) {
			
			if (LOGGER)
				logger.trace("Check Variable  : " + valuetokens.get(ndx));
			// if (LOGGER)
			// logger.trace("Check IntAddress: "
			// + (valuetokens.get(ndx) instanceof IntAddress));
			//
			// if (block.containsKey(valuetokens.get(ndx))) {
			// if (LOGGER)
			// logger.trace("Block has " + valuetokens.get(ndx));
			// if (LOGGER)
			// logger.trace("Block ist complete? "
			// + ((VP_Tokenlist) block.get(valuetokens.get(ndx)))
			// .isComplete());
			//
			// if (!((VP_Tokenlist) block.get(valuetokens.get(ndx)))
			// .isComplete())
			// return false;
			// Object var = valuetokens.get(ndx);
			// valuetokens.remove(ndx);
			// valuetokens.addAll(ndx, (VP_Tokenlist) block.get(var));
			//
			// // x Table root = (Table) block.root().get(LoadScript.rootKey);
			//
			// if (LOGGER)
			// logger.trace("NEW: " + block.get(valuetokens.get(ndx)));
			// }
		}
		if (LOGGER)
			logger.debug("Ergebnis:" + NTAB + valuetokens);

		return parseKlammer(atomname, valuetokens, 0, valuetokens.size() - 1, quotes,
				mode);
	}

	/**
	 * Gehe rekursiv runter auf die innerste Klammerebene zum Auflösen // Gehe
	 * jeden Listen-Eintrag durch und checke Klammer auf/zu // Suche OPEN //
	 * Suche CLOSE Bearbeite klammerfreien Eintrag
	 * 
	 * max(1.,max(2,5.)) * (max(6.,@1.2.3.4*2) + (1.+rint(8.));
	 * 
	 * Klammer (1., max(2.,3.+4.)) Klammer (2.,5.) Seperra 2. Seperra 3.+4.
	 * @param atomname TODO
	 * 
	 * 
	 * @param valuetokens
	 *            Die Tokens aus dem aktuellen Blockelement Key=
	 * @param firstElement
	 *            erstes Element aus <code>valuetokens</code>, das zu
	 *            untersuchen ist
	 * @param lastElement
	 *            letztes Element aus <code>valuetokens</code>, das zu
	 *            untersuchen ist
	 * @param quotes
	 *            Liste der Strings
	 * @param mode
	 *            0,2,3
	 * @return Liefert
	 *         <ul>
	 *         <li>Ready-Status für die Umsetzung.<br>
	 *         <code>true</code> ist vollständig umgesetzt, <code>false</code>
	 *         ist noch ein PASS erforderlich.</li>
	 *         <li>Eine angepasste <b>tokenlines</b> mit den Ergebnissen der
	 *         Berechnung der Funktionen in Werten oder Listen.</li>
	 *         </ul>
	 */
	int parseKlammer(String atomname, VP_Tokenlist<Object> valuetokens,
			final int firstElement, int lastElement, List<String> quotes,
			final int mode) {

		if (valuetokens == null)
			return -1;

		if (LOGGER)
			logger.debug("Löse Klammern auf und setze Quotes ein:" + NTAB
					+ valuetokens.subList(firstElement, lastElement + 1));

		boolean isFunction = false;
		int last = lastElement;
		// Gehe jeden Listen-Eintrag durch und checke Klammer auf/zu

		// Suche OPEN
		int ndx = 0;

		while ((ndx = valuetokens.subList(firstElement).indexOf(OPEN)) != -1) {

			ndx += firstElement;

			if (ndx > 0
					&& (isFunction = valuetokens.get(ndx - 1).toString()
							.contains("f:="))) {	
//				assert false;
			}

			// OPEN-Klammer entfernen
			// valuetokens.remove(ndx);
			// last--;

			if (LOGGER)
				logger.trace("OPEN gefunden!" + NTAB
						+ valuetokens.subList(ndx, last + 1) + ": " + ndx
						+ " : Function: " + isFunction);

			// rekursiver Aufruf für verschachtelte Klammern
			int testA = valuetokens.size();
			int closeNdx = parseKlammer(atomname, valuetokens, ndx + 1, last, quotes, mode);
			int testB = valuetokens.size();
			last -= (testA-testB)+1;
			// Entferne Klammer
			valuetokens.remove(ndx);
			closeNdx--;
			
			
			if (LOGGER)
				logger.trace("OPEN abgearbeitet!" + NTAB
						+ valuetokens.subList(ndx, closeNdx + 1) + ": " + ndx
						+ " :" + (isFunction?" Parameter der Function: " +valuetokens.get(ndx-1):""));
			
			if(isFunction){
				
				if (LOGGER)
					logger.trace("Funktion lösen:" + closeNdx+NTAB
							+ valuetokens.subList(ndx-1, closeNdx + 1)+
							NTAB+valuetokens);
				
				testA = valuetokens.size();
				parseCoreFunction(valuetokens, ndx-1, closeNdx, 0);
				testB = valuetokens.size();
				last -= (testA-testB)+1;
				closeNdx -= (testA-testB)+2;
				if (LOGGER)
					logger.trace("Funktion gelöst:" + closeNdx + NTAB
							+ valuetokens.subList(ndx-1, closeNdx + 1)+
							NTAB+valuetokens);
				
			}
		}

		// Suche CLOSE
		ndx = firstElement;
		while ((ndx = valuetokens.subList(firstElement).indexOf(CLOSE)) != -1) {
			ndx += firstElement;

			if (LOGGER)
				logger.trace("CLOSE gefunden!" + NTAB
						+ valuetokens.subList(firstElement, ndx) + ": " + ndx
						+ " :" + isFunction);

			// Zusammen mit aktuellem OPEN ergibt das einen vollständigen
			// Ausdruck, der weiter zu untersuchen ist.
			// complete &=

			int testA = valuetokens.size();
			boolean complete = parseSeparator(valuetokens, firstElement, ndx - 1, quotes, mode);
			int testB = valuetokens.size();
			ndx -= (testA - testB);

			// CLOSE-Klammer entfernen
			if(complete){
				valuetokens.remove(ndx);
				ndx--;
			}

			if (LOGGER)
				logger.trace("CLOSE beendet!" + NTAB
						+ valuetokens.subList(firstElement, ndx+1));

			return ndx;

		}
		if (LOGGER)
			logger.trace("Unterste Rekursionsebene ist ohne Klammer!" + NTAB
					+ valuetokens.subList(firstElement, last + 1));

			parseOperation(valuetokens, firstElement, last, quotes,
					mode);

		if (LOGGER)
			logger.debug("Ergebnis:" + NTAB + valuetokens);
		if(this.e!=null){
			this.e.put(atomname, valuetokens);
		}

		return -1;
	}

	/**
	 * Prüft, ob mehrere Werte getrennt durch Seperator (,) zurückgegeben werden
	 * 
	 * <ol>
	 * <li>Einzelwert: Einzelwert</li>
	 * <li>Mehrwert: Array von Einzelwerten</li>
	 * <li>Einzelwert unaufgelöst: Liste&lt;Werte&gt;</li>
	 * <li>Einzelwert in Mehrwert unaufgelöst: Array mit Element
	 * Liste&lt;Werte&gt;</li>
	 * <//ol><br>
	 * 
	 * @param valuetokens
	 *            Die Tokens aus dem aktuellen Blockelement Key=
	 * @param firstElement
	 *            erstes Element aus <code>valuetokens</code>, das zu
	 *            untersuchen ist
	 * @param lastElement
	 *            letztes Element aus <code>valuetokens</code>, das zu
	 *            untersuchen ist
	 * @param quotes
	 *            Liste der Strings
	 * @param mode
	 *            0,2,3
	 * @return Liefert
	 *         <ul>
	 *         <li>Ready-Status für die Umsetzung.<br>
	 *         <code>true</code> ist vollständig umgesetzt, <code>false</code>
	 *         ist noch ein PASS erforderlich.</li> <li>DOC</li>
	 *         </ul>
	 */
	private boolean parseSeparator(VP_Tokenlist<Object> valuetokens,
			final int firstElement, int lastElement, List<String> quotes,
			final int mode) {

		boolean complete = true;
		
		if (LOGGER)
			logger.debug("Prüfe auf Separator , :" + NTAB
					+ valuetokens.subList(firstElement, lastElement + 1));


		// Suche ein SEPARATOR in valuetokens
		int index = valuetokens.subList(firstElement, lastElement + 1).indexOf(
				new Character(KOMMA))
				+ firstElement;

		if (index > firstElement) {

			int first = firstElement;
			int last = lastElement;

			while (index > first) {
				// KOMMA entfernen
				valuetokens.remove(index);
				index--;
				last--;

				if (LOGGER)
					logger.trace("KOMMA gefunden!" + NTAB
							+ valuetokens.subList(first, index + 1) + NTAB
							+ "Start " + first);

				int testA = valuetokens.size();
				complete &= parseOperation(valuetokens, first, index, quotes, mode);
				int testB = valuetokens.size();
				
				first -= (testA-testB);
				index -= (testA-testB);	
				last -= (testA-testB);
				
				// Suche weitere Kommas
				first = index + 1;
				
				index = valuetokens.subList(first, last + 1).indexOf(
						new Character(KOMMA))
						+ first;

			}
			if (LOGGER)
				logger.trace("NACHKOMMA vor OP" + NTAB
						+ valuetokens.subList(first, last + 1));
			int testA = valuetokens.size();
			complete &= parseOperation(valuetokens, first, last, quotes, mode);
			int testB = valuetokens.size();
			last -= (testA-testB);
			if (LOGGER)
				logger.trace("NACHKOMMA nach OP" + NTAB
						+ valuetokens.subList(first, last + 1));		
			
		} else {
			if (LOGGER)
				logger.trace("NOSEP");
			complete &= parseOperation(valuetokens, firstElement, lastElement, quotes, mode);
		}
		return complete;
	}

	/*
	 * Parst alle Elemente in der Formel von offset bis offset+length. Es sollte
	 * kein {@link #OPEN}, {@link #CLOSE} oder link #SEPARATOR} zwischen
	 * <code>firstElement</code> und <code>lastElement</code> (einschließlich)
	 * sein. Nachdem diese Methode fertig ist, wurden alle Elemente bis auf
	 * eines aus dem Bereich <code>firstElement lastElement</code> entfernt. <br>
	 * Auflösen der Quotes!
	 * 
	 * @param valuetokens
	 *            Die Tokens aus dem aktuellen Blockelement Key=
	 * @param firstElement
	 *            erstes Element aus <code>valuetokens</code>, das zu
	 *            untersuchen ist
	 * @param lastElement
	 *            letztes Element aus <code>valuetokens</code>, das zu
	 *            untersuchen ist
	 * @param quotes
	 *            Liste der Strings
	 * @param mode
	 *            {@linkplain ConstValue#PMODE0}
	 * @return Liefert
	 *         <ul>
	 *         <li>Ready-Status für die Umsetzung.<br>
	 *         <code>true</code> ist vollständig umgesetzt, <code>false</code>
	 *         ist noch ein PASS erforderlich.</li>
	 *         <li>DOC</li>
	 *         </ul>
	 */


	/**
	 * Funktionen ausführen
	 * 
	 * @param valuetokens
	 *            Die Tokens aus dem aktuellen Blockelement Key=
	 * @param firstElement
	 *            erstes Element aus <code>valuetokens</code>, das zu
	 *            untersuchen ist
	 * @param lastElement
	 *            letztes Element aus <code>valuetokens</code>, das zu
	 *            untersuchen ist
	 * @param mode
	 *           0,2,3
	 * @return Liefert
	 *         <ul>
	 *         <li>Ready-Status für die Umsetzung.<br>
	 *         <code>true</code> ist vollständig umgesetzt, <code>false</code>
	 *         ist noch ein PASS erforderlich.</li>
	 *         <li>DOC</li>
	 *         </ul>
	 */
	@SuppressWarnings("unchecked")
	private boolean parseCoreFunction(VP_Tokenlist<Object> valuetokens,
			final int firstElement, int lastElement, final int mode) {

		if (LOGGER)
			logger.trace("Löse Funktionen:" + NTAB
					+ valuetokens.subList(firstElement, lastElement + 1)+NTAB+valuetokens);
		
		
		int last = lastElement;

		// Formel endet hier!

		// Jedes Element der Token durchgehen
		// Nicht direkt, da sich die Liste dynamisch ändert
		// Rückwärts, da evtl. Vorzeichen vor den Funktionen.

//		for (int ndx = last; ndx >= firstElement; ndx--) {
			// // Aktueller Token: Operator, Parameter, Function oder
			// // "Toter Parameter"
			// // toter Parameter entsteht bei Matrizen. Das vorherige Ergebnis
			// ist
			// // NICHT Parameter der aktuellen Function, sondern der
			// zukünftigen
			// // zukünftigeFunction Function, Werte, vorherErgebnis -->
			// // zukünftigeFunction, aktuellesErgebnis, vorherErgebnis
			Object actToken = valuetokens.get(firstElement);
			if (LOGGER)
				logger.trace("Funktioncheck: " + actToken);

			assert actToken.toString().contains("f:="):actToken;

			// Liste der Functions
			List<? extends FuncPType> listFPT = (List<? extends FuncPType>) ((List<?>) actToken);
//			if (LOGGER)
//				logger.trace(valuetokens.subList(ndx, last + 1) + NTAB
//						+ lastElement + NTAB + last);
//			if (LOGGER) {
//				String p = "";
//				for (int pnr = ndx + 1; pnr <= last; pnr++)
//					p += valuetokens.get(pnr) + " ";
//
//				logger.trace("FUNCTIONDATA:" + NTAB
//						+ "PARAMETER bestimmen für Methode: " + NTAB + listFPT
//						+ NTAB + "Anzahl der infragekommenden Funktionen: "
//						+ listFPT.size() + NTAB + "First: " + firstElement
//						+ NTAB + "ndx  : " + ndx + NTAB + "Last : " + last
//						+ NTAB + "LaOP : " + lastOperator + NTAB
//						+ "Parameter: " + p + NTAB
//						+ "Über Parameter die Function bestimmen!" + NTAB
//						+ "2PASS: " + listFPT.get(0).function.twoPass() + NTAB
//						+ "3PASS: " + listFPT.get(0).function.threePass());
//			}
//
			// Strategie:
			// 1. Bekannte Funktion nehmen, Parameter-Typen und Länge mit
			// Aufrufparameter vergleichen.

			// Aufbereiten der Aufrufparameter:
			// 0 oder mehr		
			VP_Tokenlist<Object> aufrufparameter = valuetokens.subList(firstElement + 1, lastElement + 1) ;

			if (LOGGER)
				logger.trace("Parameter" + NTAB
						+ "Aufrufparameter: " + aufrufparameter + NTAB
						+ "Size           : " + aufrufparameter.size());

			// ----------------------------------------------------------------
			// Die Liste der Parameter muss jetzt einer Liste der bekannten
			// Parameter entsprechen
			// Size und Type/Class
			// Alle Varianten der Funktion durchgehen
			for (FuncPType fpt : listFPT) {
				if (LOGGER) {
					logger.trace(NTAB + "Prüfe         : " + fpt + NTAB
							+ "Parameteranzahl: " + fpt.parameterType.length
							+ NTAB + "Type           : "
							+ Arrays.asList(fpt.parameterType));
				}

				// Längenvergleich
				if (fpt.parameterType.length == 0 && aufrufparameter.size() > 0)
					continue;

				// Achtung: Der letzte Parameter kann Array sein. Dadurch
				// ist es möglich, dass aufrufparameter > als fpt.parameter
				// ist! Ein Vergleich mit == ist also nicht richtig!
				// Nur wenn aufrufparameter < als fpt.parameter, kommt die
				// Funktion nicht in Frage

				if (fpt.parameterType.length > aufrufparameter.size())
					continue;

				// Ein mehr an Parametern als Object[]-Array zusammenfassen
				// und anhängen

				int arrlen = aufrufparameter.size() - fpt.parameterType.length;
				// logger.trace("L�ngenvergleich: Aufrufe - Parameter = "+arrlen);

				// Erzeuge das value-Array f�r die Instanzierung
				// Object [] value = new Object [fpt.parameterType.length];

				// boolean fpt2pass = fpt.function.twoPass();
				
				int ndx =firstElement + (aufrufparameter.size()-arrlen);
				
				// arrlen > 0, dann stehen mehr Parameter bereit, als "Plätze"
				if (arrlen > 0) {
					
					arrlen++;
					Class<?> fptclazz = (Class<?>) fpt.parameterType[fpt.parameterType.length - 1];
					if (LOGGER) {

						logger.trace("Fasse "+ arrlen + " typengleiche Parameter zusammen."
								+ NTAB + "Anzahl der Parameter lt. FTP   : " + fpt.parameterType.length
								+ NTAB + "Anzahl der Parameter lt. Aufruf: " + aufrufparameter.size()
								+ NTAB + aufrufparameter
								+ NTAB + "Zusammenzufassen sind: " + arrlen
								+ NTAB + "Start: "+firstElement
								+ NTAB + "Beginn der Zusammenfassung bei ndx-Position: " + ndx
								+ NTAB + "Componente: " + fptclazz.getComponentType()
								+ NTAB + fptclazz
//								+ NTAB + valuetokens.subList(ndx, ndx
//										+ aufrufparameter.size() + 1)
										);
					}
					// Die Überzähligen Parameter in einem Array
					// zusammenfassen und in der letzen Position speichern
					//
					// int firstpos = ndx + fpt.parameterType.length - 2;
					// int lastpos = 0;
//					int insertPos = fpt.parameterType.length - 1;

					// Erzeuge ein neues Array mit dem richtigen Ergebnistypen
					Object o = Array
							.newInstance(fptclazz.getComponentType(), 0);

					// Caste das Object auf Array mit dem richtigen Typen
					Object[] oo = (Object[]) o.getClass().cast(o);

					// Schreibe die Parameter in das �bergabearray
					Object[] o3 = aufrufparameter.subList(
							aufrufparameter.size() - arrlen,
							aufrufparameter.size()).toArray(oo);
					

					if(LOGGER)
					logger.trace("Liste: "+Arrays.asList(o3)+NTAB+ndx+NTAB+firstElement) ;
//					System.err.println(aufrufparameter);

					// �berz�hlige Positionen l�schen:
					for (int del = aufrufparameter.size() - 1; del >= ndx; del--) {
//						System.err.println("RM: " + aufrufparameter.get(del));
						aufrufparameter.remove(del);
						valuetokens.remove(ndx);
						last--;
					}

					logger.trace("Liste: "+aufrufparameter+NTAB+ndx) ;
					// valuetokens.remove(ndx);
					// last--;
					// lastElement--;

					// ... und eintragen
					aufrufparameter.add(ndx-firstElement-1, o3);
					valuetokens.add(ndx, o3);
					logger.trace("Liste: "+aufrufparameter+NTAB+ndx+NTAB+valuetokens) ;

//					System.err.println(aufrufparameter);

				} else {
					// arrlen = 0, Anzahl Parameter entspricht Anzahl Typen

//					System.err.println(aufrufparameter);

				}
				
				if(LOGGER)
					logger.trace("Erg: "+valuetokens) ;
				
				
				if (checkParameter(fpt, aufrufparameter, mode)) {
					try {
						calculate(valuetokens, aufrufparameter, firstElement,
								last, fpt, mode);
						// In firstElement steht das Ergebnis
						// oder die Funktion mit den Parametern
						// Danach löschen.
						for(int rmndx = 0; rmndx < aufrufparameter.size();rmndx++){
							valuetokens.remove(firstElement+1);
							last--;
						}
						
						if (LOGGER) {
							if (fpt.function.twoPass()
									|| fpt.function.threePass()) {
								FuncPType fNew = (FuncPType) valuetokens
										.get(firstElement);
								logger.trace("Ergebnis: " + NTAB + valuetokens
										+ NTAB + "Func.: " + fNew.function
										+ NTAB + "First: " + firstElement
										+ NTAB + "ndx  : " + ndx + NTAB + "2PASS: "
										+ fNew.function.twoPass() + NTAB
										+ "3PASS: " + fNew.function.threePass()
										+ NTAB + "Para : "
										+ Arrays.asList(fNew.values));
								
								
								if(fpt.function.twoPass())valuetokens.setInit();
								if(fpt.function.threePass())valuetokens.setFlow();
								if(!(fpt.function.twoPass() || fpt.function.threePass())){
									valuetokens.setInit();
									valuetokens.setFlow();									
								}
							} else
								logger.trace("Ergebnis = " + valuetokens.subList(firstElement, last+1)
//										+ NTAB + "Func.: " + fpt.function
//										+ NTAB + "First: " + firstElement
//										+ NTAB + "ndx  : " + ndx + NTAB + "2PASS: "
//										+ fpt.function.twoPass() + NTAB
//										+ "3PASS: " + fpt.function.threePass()
										);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						assert false;
					}

					return true;
				}
			}
//		} // Die große Schleife....
//
		if (LOGGER)
			logger.debug("Kein Ergebnis!" 
					+ NTAB + "Funktion: "+ valuetokens.subList(firstElement,firstElement+1)
					+ NTAB + "Parameter: "+ valuetokens.subList(firstElement+1,lastElement+1)
					+ NTAB + valuetokens.subList(firstElement, last + 1));
		
		FuncPType fpt = (FuncPType) ((List<?>)valuetokens.get(firstElement)).get(0);
		
		if (LOGGER)
			logger.debug(LFCR+LOGTAB+fpt.function
					+LOGTAB+Arrays.asList(fpt.parameterType)
					+(fpt.values==null?"":LOGTAB+Arrays.asList(fpt.values)));
		
		return true;
	}

	/**
	 * Prüfe, ob Methode fpt die zum Aufrufparameter passenden Methodenparameter
	 * hat.
	 * Setzte Pass ein
	 * 
	 * @param fpt
	 *            die untersuchte Methode
	 * @param aufrufparameter
	 *            die gefundenen Aufrufparameter
	 * @param mode TODO
	 * @return <i>true</i> passen zusammen
	 * @modified -
	 */
	private final static boolean checkParameter(FuncPType fpt,
			VP_Tokenlist<Object> aufrufparameter, int mode) {

		boolean thisisit = true;
		boolean is2Pass = false;
		boolean is3Pass = false;
		// Alle Parameter durchgehen und checken, ob die Aufrufparameter
		// dazu passen
		for (int nr = 0; nr < fpt.parameterType.length; nr++) {

			logger.trace("check " + fpt);
			/**
			 * Die Klasse des Parameters (ggfs. aus Primitiven abgeleitet)
			 */
			Class<?> t = dotClass(fpt.parameterType[nr]);
			// // aufrufparameter
			// // .get(lastpos)
			// // .getClass()
			// // .asSubclass(fptclazz.getComponentType())
			/** Die korrespondierende Klasse */
			Class<?> ob = aufrufparameter.get(nr).getClass();

			if(aufrufparameter.get(nr) instanceof FuncPType){
				
				FuncPType f = (FuncPType) aufrufparameter.get(nr);
				ob = dotClass(f.function.retType());
				if( (is2Pass = f.function.twoPass())==true)	fpt.function.setTwoPass();
				if( (is3Pass = f.function.threePass())==true) fpt.function.setThreePass();
				
				logger.trace("check " + f);
				logger.trace("check " + ob +"/"+t);
				logger.trace("check 2-Pass "+is2Pass);
				logger.trace("check 3-Pass "+is3Pass);
				
				
			}
			
			// Prüfen, ob Arraydimensionen zusammenpassen
			Matcher m1 = Pattern.compile("\\[").matcher(
					fpt.parameterType[nr].toString());
			int i1 = 0;
			while (m1.find()) {
				i1++;
			}
			Matcher m2 = Pattern.compile("\\[").matcher(ob.toString());
			int i2 = 0;
			while (m2.find()) {
				i2++; // = m2.groupCount();
			}

			logger.trace("check " + i1 + "/" + i2 + "/" + ob + "/"
					+ aufrufparameter);
			
			if (i1 != i2) {
				// Arraydimensionen sind verschieden
				thisisit &= false;			
			} else {

				if (LOGGER)
					logger.trace("ARRAY ob:" + ob + LOGTAB + "ARRAY t :" + t);
				// Test, ob Aufrufklasse gleich t-Klasse oder
				// Unterklasse dazu ist	
				try {
					ob.asSubclass(t);
					if (LOGGER)
						logger.trace(aufrufparameter.get(nr).getClass()
								.getSimpleName()
								+ NTAB
								+ "ist Subclass von "
								+ NTAB
								+ t.getSimpleName());
									
					thisisit = true;
					break;
				} catch (java.lang.ClassCastException e) {
					if (t.isArray()) {

						if (LOGGER)
							logger.trace("ARRAY ob:" + ob + LOGTAB
									+ "ARRAY t :" + t);
						thisisit &= false;

						assert false : "Testfall: Wann wird dieser Zustand erreicht?";
					} else {
						if (LOGGER)
							logger.trace(aufrufparameter.get(nr) + "/" + ob
									+ NTAB + "ist keine Subclasse von " + NTAB
									+ t);
						thisisit &= false;
						
						break;
					}
				}

			}
		}
		return thisisit;
	}

	/**
	 * TODO Comment
	 * 
	 * @param valuetokens TODO
	 * @param aufrufparameter TODO
	 * @param firstElement  TODO
	 * @param lastElement TODO
	 * @param fpt TODO
	 * @param mode TODO
	 * @return TODO
	 * @modified -
	 */	
	@SuppressWarnings("static-method")
	private final int calculate(VP_Tokenlist<Object> valuetokens,
			VP_Tokenlist<Object> aufrufparameter, int firstElement, int lastElement,
			FuncPType fpt, int mode) {

//		int last = lastElement;

//		// Funktion und Parameter löschen...
//		for (int i = 0; i <= fpt.parameterType.length; i++) {
//			valuetokens.remove(ndx);
//			last--;
//		}

		// Methode aufrufen
		try {
			// Hinweis:
			// Es wird der erste Parameter firstElement überschrieben mit der neuen 2/3-Pass Methode oder dem Ergebnis. Die folgenden Parameter werden in der Aufrufmethode gelöscht
			
			if (fpt.function.twoPass() && mode < 1) {
				if (LOGGER) {
					logger.trace("TWOPASS " + fpt);
				}
				FuncPType fptNew = new FuncPType();
				fptNew.function = fpt.function;
				fptNew.parameterType = new Type[fpt.parameterType.length];
				fptNew.values = new Object[fpt.parameterType.length];
				for (int i = 0; i < fpt.parameterType.length; i++) {
					fptNew.parameterType[i] = fpt.parameterType[i];
					fptNew.values[i] = aufrufparameter.get(i);
				}
				valuetokens.set(firstElement, fptNew);
				
				logger.trace(fptNew.toString());
//				last++;
				
			} else if (fpt.function.threePass() && mode < 2) {
				if (LOGGER) {
					logger.trace("THREEPASS " + fpt);
				}
				FuncPType fptNew = new FuncPType();
				fptNew.function = fpt.function;
				fptNew.parameterType = new Type[fpt.parameterType.length];
				fptNew.values = new Object[fpt.parameterType.length];
				for (int i = 0; i < fpt.parameterType.length; i++) {

					fptNew.parameterType[i] = fpt.parameterType[i];
					fptNew.values[i] = aufrufparameter.get(i);
				}
				valuetokens.set(firstElement, fptNew);
//				last++;
				assert false;
			} else {
				
				if (LOGGER)
					logger.trace("DIRECTPASS: " + fpt
							+NTAB+valuetokens.subList(firstElement, lastElement));

				// Parameter zusammenstellen
				Object[] values = aufrufparameter.toArray();
				Object erg = fpt.function.calculate(values.length > 0 ? values
						: null);
				
//				valuetokens.remove(ndx);
				// ... und Ergebnis eintragen!
				valuetokens.set(firstElement, erg);
//				last = valuetokens.size() - 1;
				if (LOGGER)
					logger.trace("DIRECTPASS = "+erg );

			}

		} catch (Exception e1) {
			e1.printStackTrace();

			assert false : "Testfall: Fehlersituation klären.";
		}
		return -1;
	}

	/**
	 * @param valuetokens
	 *            Die Tokens aus dem aktuellen Blockelement Key=
	 * @param firstElement
	 *            erstes Element aus <code>valuetokens</code>, das zu
	 *            untersuchen ist
	 * @param lastElement
	 *            letztes Element aus <code>valuetokens</code>, das zu
	 *            untersuchen ist
	 * @param quotes TODO
	 * @param mode
	 *            0,2,3 für Pass
	 * @return Liefert
	 *         <ul>
	 *         <li>Ready-Status für die Umsetzung.<br>
	 *         <code>true</code> ist vollständig umgesetzt, <code>false</code>
	 *         ist noch ein PASS erforderlich.</li>
	 *         <li>DOC</li>
	 *         </ul>
	 */
	@SuppressWarnings({ "unchecked", "static-method" })
	private final boolean parseOperation(VP_Tokenlist<Object> valuetokens,
			final int firstElement, int lastElement, List<String> quotes,
			final int mode) {

		boolean complete = true;
		
		if (LOGGER)
			logger.debug("Operationen berechnen: " + NTAB
					+ valuetokens.subList(firstElement, lastElement + 1));

		int last = lastElement;
		int lowPrio = 0;

		for (int prio = 10; prio >= lowPrio; prio--) {
			// Ermittle die höchste Priorität aller Operationen (Punkt vor
			// Strichrechnung!)

			// Berechnung
			for (int ndx = firstElement + 1; ndx <= last; ndx++) {

				// if (valuetokens.get(ndx - 1).equals(OPEN))
				// continue;

				Object actToken = valuetokens.get(ndx);

				// Ist keine Operation
				if (!(actToken instanceof Operation))
					continue;

				// Die falsche Priorität
				if (prio != ((Operation) actToken).getPriority()) {
					continue;
				}
				// Links ist eine konkrete unaufgelöste Funktion
				if (valuetokens.get(ndx - 1) instanceof FuncPType) {
					// Operator ist niedrigste Priorität, da sonst Strich vor
					// Punktrechnung droht
					lowPrio = ((Operation) actToken).getPriority();

					if (LOGGER)
						logger.debug("lks. Function. Operationprio: " + lowPrio);
					complete &= false;
					continue;
				}
				// Rechts ist eine konkrete unaufgelöste Funktion
				if (valuetokens.get(ndx + 1) instanceof FuncPType) {
					// Operator ist niedrigste Priorität, da sonst Strich vor
					// Punktrechnung droht
					lowPrio = ((Operation) actToken).getPriority();

					if (LOGGER)
						logger.debug("re. Function. Operationprio: " + lowPrio);
					complete &= false;
					continue;
				}

				Object lks = null;
				Object re = null;

				// linker Operator a ist Liste
				if (valuetokens.get(ndx - 1) instanceof List) {
					// Element 0 holen
					lks = ((List<Object>) valuetokens.get(ndx - 1)).get(0);
					
					// Ist Liste von möglichen Funktionen
					if(lks.toString().contains("f:=")){
						// 	Operator ist niedrigste Priorität, da sonst Strich vor
						// 	Punktrechnung droht
						lowPrio = ((Operation) actToken).getPriority();
						if (LOGGER)
							logger.debug("lks. Function. Operationprio: " + lowPrio);
						complete &= false;
						continue;
					}
					assert false : "Testfall: Welche Situation kann das erzeugen?";
				} else {
					// ist Einzelwert.
					lks = valuetokens.get(ndx - 1);
				}

				// rechter Operator a ist Liste
				if (valuetokens.get(ndx + 1) instanceof List) {
					// Element 0 holen
					re = ((List<Object>) valuetokens.get(ndx + 1)).get(0);
					
					// Ist Liste von möglichen Funktionen
					if(re.toString().contains("f:=")){
						// 	Operator ist niedrigste Priorität, da sonst Strich vor
						// 	Punktrechnung droht
						lowPrio = ((Operation) actToken).getPriority();
						if (LOGGER)
							logger.debug("re. Function. Operationprio: " + lowPrio);
						complete &= false;
						continue;
					}
					assert false : "Testfall: Welche Situation kann das erzeugen?";
				} else {
					// ist Einzelwert.
					re = valuetokens.get(ndx + 1);
				}

				if (lks.getClass().equals(re.getClass())) {
					if (LOGGER)
						logger.trace("Operation " + actToken
								+ " ausführen mit " + lks + ", " + re);
					valuetokens.set(ndx - 1,
							((Operation) actToken).calculate(lks, re));
					if (LOGGER)
						logger.trace("Operation = " + valuetokens.get(ndx - 1));

					// Lösche Operator und die zwei Werte
					valuetokens.remove(ndx); // Operator 
					valuetokens.remove(ndx); // re Wert

					// ndx-Zeiger um 2 Elemente kürzen: -3 f. Op und Wert +1 f.
					// Ergebnis
					last--;
					last--;
					// Die gleiche Prio nochmals durchgehen, für weitere mgl.
					// Berechnungen dieser Priorität
					prio++;
					
				} else {
					assert false : "Operation " + actToken + " ausführen mit "+LFCR
							+ lks.getClass().getSuperclass() + LFCR
							+ re.getClass();
				}
			}
		}
		return complete;
	}
	
//	/**
//	 * Quotes wieder einsetzen
//	 * 
//	 * @param valuetokens
//	 *            Tokenliste
//	 * @param firstElement
//	 *            ????
//	 * @param lastElement
//	 *            ????
//	 * @param quotes
//	 *            Liste der Quotes
//	 * @modified -
//	 */
//	private static final void quotesEinsetzen(VP_Tokenlist valuetokens,
//			final int firstElement, final int lastElement, List<String> quotes) {
//		for (int ndx = firstElement; ndx <= lastElement; ndx++) {
//			Object actToken = valuetokens.get(ndx);
//
//			if (actToken instanceof String) {
//
//				String aT = (String) actToken;
//				if (aT.startsWith("$") && aT.endsWith("$")) {
//					int varStart = 1;
//					int varEnd = aT.length() - 1;
//					int nr = Integer.valueOf(aT.substring(varStart, varEnd))
//							.intValue();
//					valuetokens.remove(ndx);
//					valuetokens.add(ndx, quotes.get(nr));
//				}
//			}
//		}
//	}

	/**
	 * liefert die Objectklasse eines primitiven Typs
	 * 
	 * @author tfossi
	 * @version 01.08.2014
	 * @modified -
	 * @since Java 1.6
	 * 
	 * @param t
	 *            Zu übersetzende Probe
	 * @return liefert die Objectklasse eines primitiven Typs
	 */
	// @SuppressWarnings("unchecked")
	private final static Class<?> dotClass(Type t) {

		if (((Class<?>) t).isPrimitive()) {
			if (t == double.class)
				return Double.class;
			else if (t == float.class)
				return Float.class;
			else if (t == long.class)
				return Long.class;
			else if (t == int.class)
				return Integer.class;
			else if (t == short.class)
				return Short.class;
			else if (t == byte.class)
				return Byte.class;
			else if (t == char.class)
				return Character.class;
			else if (t == boolean.class)
				return Boolean.class;
			assert false;
		}	

		return (Class<?>) t;
	}

	// ---- Selbstverwaltung --------------------------------------------------
	/** serialVersionUID */
	@SuppressWarnings("unused")
	private final static long serialVersionUID = VERSION;

	/** logger */
	private final static Logger logger = Logger.getLogger(VP_Transfer.class
			.getPackage().getName() + ".VP_Transfer");

	/**
	 * TODO Comment
	 * @modified -
	 */
	public VP_Transfer() { 
		this.e = null;
	}

	/** e */
	private final Element e;
	/**
	 * TODO Comment
	 * @param e TODO
	 * @modified -
	 */
	public VP_Transfer(Element e) {
		this.e = e;
	}
}
