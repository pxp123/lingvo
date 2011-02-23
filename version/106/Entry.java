/**
 * 
 */
package lingvo;

import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import nu.xom.Element;
import nu.xom.Elements;

/**
 * Описание одного слова
 * @author администратор
 * @since 09.03.08
 */
public class Entry {
	public static final String NOUN = "noun";

	public static final String TYPEOTHER = "OTHER";

	public static final String TYPEERR = "ERR";

	public static final String TYPEFULL = "FULL";

	public static final String TYPEC = "C";

	public static final String TYPEBLANK = ""; //== TYPEC

	public static final String TYPEU = "U";

	public static final String TYPECU = "C, U";

	public static final String TYPEUC = "U, C";

	public static final String TYPEPL = "pl.";

	public static final String TYPESING = "sing.";

	public static final String THETHE = "THE";

	String[] fullTypes = null;

	HashSet<String> dictinctTypes = null;

	/**имя файла*/
	String fileName;

	char symbol = ' ';

	/**слово*/
	String word;

	/**цвет строки в вых файле*/
	//String color;
	/**часть речи*/
	//String pos;
	/**сущ. не найдено*/
	static int NPNOTFOUND = -1;

	/**найдено не сущ.*/
	static int NPNOTNOUN = -2;

	/**позиция существительного в списке p-g[]*/
	int nounPos = NPNOTFOUND;

	/**грамматический тип [x,y]для всех значений слова*/
	String type = "";

	/**разряд*/
	//String category = "";
	/**список значений*/
	Vector<Meaning> meanings = new Vector();

	String error = "";

	private boolean isThe = false;

	public Entry(String fileName) {
		this.fileName = fileName;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) throws IOException {
		word = word.replaceAll("·", "");
		word = word.replaceAll("%", "");
		word = word.replaceAll("'", "");
		word = word.replaceAll("-", "");
		this.word = word.trim();
		word = word.toUpperCase();
		if (word.startsWith(THETHE + " ")) {//entry00249 the accused
			OALD.writeLog("the.1=" + word);
			symbol = word.charAt(4);
			isThe = true;
		} else symbol = word.charAt(0);
	}

	/*
	 *уберем лишнее в конце row со слова word 
	 */
	static String rtrim(String row, String word) {
		if (row != null && word != null) {
			int pos = row.toUpperCase().indexOf(word.toUpperCase());
			if (pos != -1) row = row.substring(0, pos);
		}
		return row;
	}

	/**
	 * 
	 * @param ng
	 * @throws IOException 
	 */
	public void addMeaning(Element ng) throws IOException {
		Meaning mean = new Meaning(ng);
		meanings.add(mean);
	}

	public int getNounPos() {
		return nounPos;
	}

	public void setNounPos(int nounPos) {
		this.nounPos = nounPos;
	}

	public String getFullMeaning() {
		String full = "";
		for (int i = 0; i < meanings.size(); i++) {
			Meaning mean = meanings.get(i);
			full += mean.getMean() + OALD.BR;
		}
		return full;
	}

	public Vector<Meaning> getMeanings() {
		return meanings;
	}

	public void setMeanings(Vector meanings) {
		this.meanings = meanings;
	}

	public String getErr() {
		return error;
	}

	public void setErr(String err) throws IOException {
		this.error += err;
		//OALD.displayErr(toString() + ".1: " + err);
	}

	public String getFileName() {
		return fileName;
	}

	public String toString() {
		return fileName + "_" + symbol + " [" + type + "] " + word;
	}

	public String toStringFull() {
		String row = toString() + "\nErr: " + error + "\n[" + type + "], nounPos: "
				+ nounPos + ", the: " + isThe
				//+ "\nCategory: " + category
				+ "\nMeanings [" + meanings.size() + "]:\n";
		for (int i = 0; i < meanings.size(); i++) {
			Meaning mean = meanings.get(i);
			row += "  " + mean.toString() + "\n";
		}
		return row;
	}

	/**ищем noun*/
	static int findNounInZ(Elements hgChildren) {
		int np = NPNOTFOUND;
		//Elements hgChildren = hg.getChildElements(); //берем список z
		for (int i = 0; i < hgChildren.size(); i++) {
			Element z = hgChildren.get(i);
			if (z.getQualifiedName() == "z") {//список pos
				OALD.printElement(2, z, "список pos=" + i);
				np = findNounInPOS(z);
			}
			if (np == NPNOTNOUN || np >= 0) break;
		}
		if (np == NPNOTFOUND) np = 0;
		return np;
	}

	/**
	 * ищем туре для всего entry
	 * 
	 * 1)Для слов с несколькими pos entry31758.xml rose 
	 * 0.entry                      
	 *   1.h-g                       
	 *     2.<span class="label"> - type
	          <p:z>[
	            <p:gr>
	              <p:zgct>U</p:zgct>
	            </p:gr>
	          ]</p:z>
	         </span>
	         
	       2.<span class="label">
	        <p:z>[
	          <p:gr>
	            <p:zgct>C</p:zgct>
	          </p:gr>
	          , 
	          <p:gr>
	            <p:zgct>U</p:zgct>
	          </p:gr>
	          ]</p:z>
	      </span>
	      
	      <span class="label">
	        <p:z>[
	          <p:gr>pl.</p:gr>
	          ]</p:z>
	      </span>
	      <span class="label">
	 * 
	 * @param hg
	 * @return
	 */
	static String findType(Elements hgChildren) {
		for (int i = 0; i < hgChildren.size(); i++) {
			Element span = hgChildren.get(i);
			if (span.getQualifiedName() == "span") {
				//OALD.printElement(2, span, null);
				Element z = span.getFirstChildElement("z");
				if (z != null) {
					Element gr = z.getFirstChildElement("gr");
					if (gr != null) {
						Element zgct = gr.getFirstChildElement("zgct");
						if (zgct != null) { return zgct.getValue(); }
					}
				}
			}
		}
		return "";
	}

	/**
	 * 
	 * @param z
	 * @return 
	 */
	static int findNounInPOS(Element z) {
		Elements poss = z.getChildElements(); //берем список pos
		int np = NPNOTFOUND;
		for (int i2 = 0; i2 < poss.size(); i2++) {
			Element pos = poss.get(i2);
			if (pos.getQualifiedName() == "pos") {// pos
				OALD.printElement(3, pos, "pos=" + i2);
				//if (NOUN.equalsIgnoreCase(pos.getValue())) {
				if (isNoun(pos.getValue())) {
					np = i2;
					break;
				} else np = NPNOTNOUN;
			}
		}
		return np;
	}

	static boolean isNoun(String pos) {
		boolean isNoun = false;
		if (pos != null) isNoun = pos.indexOf(NOUN) == -1 ? false : true;
		OALD.display("  isNoun(): " + pos + "=" + isNoun);
		return isNoun;
	}

	public String getType() {
		return type;
	}

	/**
	 * Выполнение завершающих работ
	 */
	public void finalize() {
		//если одно значение то переносим его характеристики на слово
		if (meanings.size() == 1) {
			Meaning mean = meanings.get(0);
			if (type.equals(TYPEBLANK) && !mean.getType().equals(TYPEBLANK)) {
				type = mean.getType();
				mean.setType(TYPEBLANK);
			}
			if (isThe == false && mean.isThe() == true) {
				isThe = mean.isThe();
				mean.setThe(false);
			}
		}
	}

	/**
	 * в типах слова и значений только то что в allowed
	 * 
	 * @param allowed
	 * @return
	 */
	public boolean inTypesAND(String[] allowed) {
		String[] types = new String[meanings.size() + 1];
		types[0] = type;
		for (int j = 0; j < meanings.size(); j++)
			types[j + 1] = meanings.get(j).getType();

		String deb = "inTypesAND.1:" + toString() + "\n";
		for (int i = 0; i < allowed.length; i++)
			deb += " [" + allowed[i] + "]";

		deb += " --> ";
		for (int j = 0; j < types.length; j++)
			deb += " [" + types[j] + "]";
		System.out.println(deb);

		boolean ret = true;
		for (int i = 0; i < allowed.length; i++) {
			for (int j = 0; j < types.length; j++)
				if (allowed[i].equalsIgnoreCase(types[j])) {
					ret = false;
					break;
				}
			if (ret == false) break;
		}
		System.out.println("inTypesAND.2: " + ret);
		return ret;
	}

	/**
	 * в типах слова и значений только то что в allowed
	 * 
	 * @param allowed
	 * @return
	 */
	public boolean inTypesOR(String[] allowed) {
		String[] types = new String[meanings.size() + 1];
		types[0] = type;
		for (int j = 0; j < meanings.size(); j++)
			types[j + 1] = meanings.get(j).getType();

		String deb = "inTypesOR.1:" + toString() + "\n";
		for (int i = 0; i < allowed.length; i++)
			deb += " [" + allowed[i] + "]";

		deb += " --> ";
		for (int j = 0; j < types.length; j++)
			deb += " [" + types[j] + "]";
		System.out.println(deb);

		boolean ret = false;
		for (int i = 0; i < allowed.length; i++) {
			for (int j = 0; j < types.length; j++)
				if (allowed[i].equalsIgnoreCase(types[j])) {
					ret = true;
					break;
				}
			if (ret == true) break;
		}
		System.out.println("inTypesOR.2: " + ret);
		return ret;
	}

	/**
	 * в типах слова и значений только то, что в allowed
	 * 
	 * @param allowed
	 * @return
	 * @throws IOException 
	 */
	public boolean inTypes1(String allowed) throws IOException {
		HashSet<String> types = getDictinctTypes();
		boolean ret = false;
		if (types.size() == 1 && types.contains(allowed)) ret = true;

		//System.out.println("inTypes1.1:" + toString() + "\n" + allowed + " -> "
		//		+ types + "=" + ret);
		return ret;
	}

	public boolean verTypes() throws IOException {
		boolean ret = true;
		if (!type.equals(TYPEBLANK)) {
			String err = "";
			for (int j = 0; j < meanings.size(); j++) {
				String tm = meanings.get(j).getType();
				if (!tm.equals(TYPEBLANK) && !tm.equals(type)) err += "Не совпадает тип для значения "
						+ (j + 1) + "[" + tm + "]" + OALD.BR;
			}
			if (!Utils.isBlank(err)) {
				ret = false;
				setErr("Тип слова [" + type + "]" + OALD.BR + err);
			}
		}
		return ret;
	}

	/**
	 * список всех типов: слово + значения
	 * @throws IOException 
	 */
	public String[] getFullTypes() throws IOException {
		if (fullTypes == null) {
			fullTypes = new String[meanings.size() + 1];
			fullTypes[0] = type;
			for (int j = 0; j < meanings.size(); j++)
				fullTypes[j + 1] = meanings.get(j).getType();

			/*
			String deb = "getFullTypes()=" + toString() + "\n";
			for (int j = 0; j < types.length; j++)
				deb += " [" + types[j] + "]";
			System.out.println(deb);
			*/
		}
		return fullTypes;
	}

	/**
	 * список только уникальных типов для слова + значения.
	 * [C] и [] представлены как [C]
	 * @return
	 * @throws IOException
	 */
	public HashSet<String> getDictinctTypes() throws IOException {
		if (dictinctTypes == null) {
			dictinctTypes = new HashSet<String>();
			if (!type.equals(TYPEBLANK)) {
				dictinctTypes.add(type);
			} else {
				for (int j = 0; j < meanings.size(); j++) {
					String tm = meanings.get(j).getType();
					if (tm.equals(TYPEBLANK)) dictinctTypes.add(TYPEC);
					else dictinctTypes.add(tm);
				}
			}
		}
		//System.out.println("getDictinctTypes()=" + types);
		return dictinctTypes;
	}

	/**
	 * не анализируем следующие слова:
	 *  - из 2 и более слов (для THE из 3 и более слов)
	 *  - одна буква
	 *  - только заглавные буквы
	 *  - аббревиатуры (D.O.B abbr.)
	 * 
	 * @return
	 * @throws IOException 
	 */
	public boolean badEntry() throws IOException {
		boolean ret = false;
		if (word.length() <= 1) ret = true;
		else {
			StringTokenizer st = new StringTokenizer(word, " ");
			int cntT = st.countTokens();
			boolean sThe= word.toUpperCase().startsWith(THETHE+" ");
			if ((sThe && cntT >= 3) || (!sThe && cntT >= 2)) ret = true;
			else {
				//\p{Punct}  	Punctuation: One of !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
				if (word.matches("[\\p{Punct}[A-Z]]*") == true) ret = true;
			}
		}
		if (ret == true) {
			OALD.cntSkip++;
			OALD.writeLog(toString() + ": skipped");
		}
		return ret;
	}

	public void setType(String type) {
		this.type = type.trim();
	}

	public boolean isThe() {
		return isThe;
	}

	public void setThe(boolean isThe) {
		this.isThe = isThe;
	}

	/**
	 * анализируем <h-g> 
	 * 
	 * @param entry
	 * @return nounPos, если <0, то это не noun
	 * @throws IOException
	 */
	public int analyzeEntry(Element entry) throws IOException {
		Element hg = entry.getFirstChildElement("h-g");
		OALD.printElement(1, hg, null);
		nounPos = analyseHG(hg);

		//log("1. "+entryW.toStringFull());
		if (nounPos < 0) return nounPos;
		OALD.display("  <n-g> -----");
		Elements ngElements = null; //список элементов <n-g>
		Element pg = entry.getFirstChildElement("p-g");
		if (pg != null) { //если есть pg значит несклько pos
			OALD.display("nounPos.2=" + nounPos + " p-g not null");
			Elements entryChildren = entry.getChildElements(); //берем список p-g
			for (int i = 0; i < entryChildren.size(); i++) { //ищем noun
				Element el = entryChildren.get(i);
				if (el.getQualifiedName() == "p-g") {
					OALD.printElement(1, el, null);

					//ищем noun
					int np = Entry.NPNOTFOUND;
					Elements hgChildren = el.getChildElements(); //берем список z
					for (int i2 = 0; i2 < hgChildren.size(); i2++) {
						Element z = hgChildren.get(i2);
						if (z.getQualifiedName() == "z") {//список pos
							OALD.printElement(2, z, "список pos=" + i2);
							np = Entry.findNounInPOS(z);
						}
						if (np == Entry.NPNOTNOUN || np >= 0) break;
					}

					if (np >= 0) {
						ngElements = el.getChildElements(); //берем список значений
						OALD.display("nounPos.3=" + nounPos + "=" + i);
						break;
					}
				}
			}
			if (ngElements == null) {
				setErr("Не найдено значение типа NOUN");
				OALD.writeEntry(this, Entry.TYPEERR);
				nounPos = NPNOTFOUND;
				return nounPos;
			}
		} else { //если нет pg значит один pos
			OALD.display("nounPos.2=" + nounPos + " p-g null");
			ngElements = hg.getChildElements(); //берем список значений
		}
		//printChildren(2, ngList, "*** ngList");
		if (getType() == Entry.TYPEBLANK) setType(Entry.findType(ngElements));
		for (int i = 0; i < ngElements.size(); i++) {
			Element el = ngElements.get(i);
			if (el.getQualifiedName() == "n-g") {
				OALD.printElement(2, el, null);
				addMeaning(el); //el.getValue());
				//printChildren(2, el, null);
			}
		}
		return nounPos;
	}

	public int analyseHG(Element hg) throws IOException {
		String the = "";
		Element hs = hg.getFirstChildElement("hs"); //the
		if (hs != null) {
			OALD.printElement(2, hs, null);
			if (hs.getValue().trim().toUpperCase().equals(THETHE)) {
				OALD.writeLog("the.2=" + hs.getValue());
				isThe = true;
				the = hs.getValue() + " ";
			}
		}

		Element h = hg.getFirstChildElement("h"); //слово
		OALD.printElement(2, h, null);
		setWord(the + h.getValue());

		Elements hgChildren = hg.getChildElements(); //берем список 
		//ищем noun
		nounPos = findNounInZ(hgChildren);
		OALD.display("nounPos.1=" + nounPos);
		setType(findType(hgChildren));
		return nounPos;
	}
}
