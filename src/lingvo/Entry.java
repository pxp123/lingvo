/**
 * 
 */
package lingvo;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
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
	public static final String POSNOUN = "noun";

	public static final String POSADJ = "adjective";

	public static final String TYPEOTHER = "OTHER";

	public static final String TYPEERR = "ERR";

	public static final String TYPEFULL = "FULL";

	public static final String TYPEUPPER = "UPPER";

	public static final String TYPEC = "C";

	//public static final String TYPEBLANK = ""; //== TYPEC

	public static final String TYPEU = "U";

	//public static final String TYPECU = "C, U";

	//public static final String TYPEUC = "U, C";

	public static final String TYPEPL = "pl.";

	/**"usually pl."*/
	public static final String TYPEPL2 = "usually pl.";

	public static final String TYPESING = "sing.";

	/**"usually sing."*/
	public static final String TYPESING2 = "usually sing.";

	/**"sing.+ sing./pl. v."*/
	public static final String TYPESING3 = "sing.+ sing./pl. v.";

	/**"C+sing./pl. v."*/
	public static final String TYPESING4 = "C+sing./pl. v.";

	/**"U+sing./pl. v."*/
	public static final String TYPESING5 = "U+sing./pl. v.";

	/**"usually before noun"*/
	//public static final String TYPEIGNORE1 = "usually before noun";
	/**"only before noun"*/
	//public static final String TYPEIGNORE2 = "only before noun";
	/**Список типов кот. игнорируются*/
	static HashSet<String> ignoreTypes = null; //new HashSet<String>();

	public static final String THETHE = "THE";

	/**список грамматических типов для слова*/
	//HashSet<String> typeList = null;
	Vector<Value> typeList = null;

	HashSet<String> distinctTypes = null;

	//Vector<Value> distinctTypes = null;

	/**список частей речи для всего entry*/
	Properties posList = new Properties();

	/** список типов которые надо печатать*/
	HashSet<String> printPosList = new HashSet<String>();

	/**имя файла*/
	String fileName;

	char symbol = ' ';

	/**слово*/
	String word;

	/**разряд*/
	//String category = "";
	/**список значений*/
	Vector<Meaning> meanings = new Vector<Meaning>();

	String error = "";

	private boolean isThe = false;

	//private Boolean isThe = null;

	public Entry(String fileName) {
		this.fileName = fileName;
		if (ignoreTypes == null) {
			ignoreTypes = new HashSet<String>();
			ignoreTypes.add("not before noun");
			ignoreTypes.add("not usually before noun");
			ignoreTypes.add("often passive");
			ignoreTypes.add("only before noun");
			ignoreTypes.add("usually before noun");
			ignoreTypes.add("usually passive");
		}

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
	public boolean addMeaning(Element ng, String pos) throws IOException {
		boolean add = false;
		if (ng.getFirstChildElement("d") == null
				&& ng.getFirstChildElement("xr") == null) { //нет значения или ссылки
			if (isBlankType(getTypeList())) setTypeList(Entry.findTypeList(ng
					.getChildElements("span")));
		} else {
			Meaning mean = new Meaning(ng, pos);
			//OALD.display("addMeaning(" + pos + ") 1=" + POSNOUN.equals(pos) + " 2="
			//		+ isPos(POSNOUN, mean.getPos()) + " 3=" + mean.getPos());
			if (POSNOUN.equals(pos) || isPos(POSNOUN, mean.getPos())) {
				meanings.add(mean);
				add = true;
			}
		}
		return add;
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
		this.error += err + OALD.BR;
		//OALD.displayErr(toString() + ".1: " + err);
	}

	public String getFileName() {
		return fileName;
	}

	public String toString() {
		return fileName
				//+ "_" + symbol 
				+ ", " + word + ", " + Utils2.vector2String(typeList) + ", posList: "
				+ posList;
	}

	public String toStringFull() {
		String row = toString() + (Utils.isBlank(error) ? "" : ("\nErr: " + error))
				+ "\nthe: " + isThe
				//+ "\nCategory: " + category
				+ "\nMeanings [" + meanings.size() + "]:\n";
		for (int i = 0; i < meanings.size(); i++) {
			Meaning mean = meanings.get(i);
			row += "  " + mean.toString() + "\n";
		}
		return row;
	}

	/**ищем pos*/
	/*
	static int findPosInZ(Elements hgChildren, String pos) {
		int np = NPNOTFOUND;
		//Elements hgChildren = hg.getChildElements(); //берем список z
		for (int i = 0; i < hgChildren.size(); i++) {
			Element z = hgChildren.get(i);
			if (z.getQualifiedName() == "z") {//список pos
				OALD.printElement(2, z, "список pos=" + i);
				np = findPosInPOS(z, pos);
			}
			if (np == NPNOTNOUN || np >= 0) break;
		}
		if (np == NPNOTFOUND) np = 0;
		return np;
	}
	*/

	/**
	 * 
	 * @param z
	 * @return 
	 */
	/*
	static int findPosInPOS(Element z, String pos) {
		Elements poss = z.getChildElements(); //берем список pos
		int np = NPNOTFOUND;
		for (int i2 = 0; i2 < poss.size(); i2++) {
			Element epos = poss.get(i2);
			if (epos.getQualifiedName() == "pos") {// pos
				OALD.printElement(3, epos, "findPosInPOS(" + pos + ")=" + i2);
				//if (NOUN.equalsIgnoreCase(pos.getValue())) {
				if (isPos(pos, epos.getValue())) {
					np = i2;
					break;
				} else np = NPNOTNOUN;
			}
		}
		OALD.display("end findPosInPOS(" + pos + ")=" + np);
		return np;
	}
	*/

	/**
	 * 
	 * @param z
	 * @return 
	 * OALD.printChildren(2, hg.getChildElements("z"), "z list");
	 */
	static Properties findPosList(Elements zs) {
		Properties pList = new Properties();
		for (int i = 0; i < zs.size(); i++) {
			Elements poss = zs.get(i).getChildElements(); //берем список pos
			for (int i2 = 0; i2 < poss.size(); i2++) {
				Element epos = poss.get(i2);
				if (epos.getQualifiedName().equalsIgnoreCase("pos")) {// pos
					//OALD.printElement(3, epos, "findPosInPOS(" + pos + ")=" + i2);
					//if (NOUN.equalsIgnoreCase(pos.getValue())) {
					pList.put(epos.getValue(), "" + i2);
				}
			}
			if (pList.size() > 0) break;
		}
		OALD.display("end findPosList()=" + pList);
		return pList;
	}

	/**
	 * ищем pos1 in pos2
	 * @param pos1
	 * @param pos2
	 * @return
	 */
	static boolean isPos(String pos1, String pos2) {
		boolean isPos = false;
		//if (!Utils.isBlank(pos2)) isPos = pos1.indexOf(pos2) == -1 ? false : true;
		if (pos1.equalsIgnoreCase(pos2)) isPos = true; //11.04.08
		//OALD.display("  isPos(" + pos1 + "," + pos2 + ")=" + isPos);
		return isPos;
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
	      
	      //08759 crew, p. 362
	      <span class="label">
	        <p:z>[
	          <p:gr>
	            <p:zgct>C</p:zgct>
	            +sing./
	            <p:zgpl>pl.</p:zgpl>
	            <p:it>v</p:it>
	            .</p:gr>
	          ]</p:z>
	        <p:z></p:z>
	      </span>

	    
	    //classified 06780 p.270 игнорируем
	    <span class="label">
	      <p:z>[
	        <p:gr>
	          <p:zgpl>usually before noun</p:zgpl>
	        </p:gr>
	        ]</p:z>
	    </span>	      
	    [only before noun]
	      
	 * 
	 * @param hg
	 * @return
	 */
	static Vector findTypeList(Elements hgChildren) {
		Vector<Value> tList = new Vector();
		for (int i = 0; i < hgChildren.size(); i++) {
			Element span = hgChildren.get(i);
			//OALD.display("  span=" + span.getAttribute("class").getValue());
			if (span.getQualifiedName().equalsIgnoreCase("span")
					&& span.getAttribute("class") != null
					&& span.getAttribute("class").getValue().equals("label")) {
				//OALD.printElement(2, span, "findTypeList()");
				Element z = span.getFirstChildElement("z");
				if (z != null) {
					Elements grs = z.getChildElements("gr");
					for (int j = 0; j < grs.size(); j++) {
						Element gr = grs.get(j);
						/*
						Element zgct = gr.getFirstChildElement("zgct");
						String type = null;
						if (zgct != null) type = zgct.getValue();
						else type = gr.getValue();
						if (!Utils.isBlank(type)
								&& !(type.equalsIgnoreCase("usually before noun") || type
										.equalsIgnoreCase("only before noun"))) tList.add(type);
						*/
						//19.04.08
						String type = gr.getValue();
						OALD.totalTypeList.add(type);
						//						if (!Utils.isBlank(type)
						//								&& !(type.equalsIgnoreCase(TYPEIGNORE1) || type
						//										.equalsIgnoreCase(TYPEIGNORE2))) tList.add(new Value(type,
						//								true));
						if (!Utils.isBlank(type) && !ignoreTypes.contains(type)) tList
								.add(new Value(type, true));
					}
				}
			}
		}
		if (isBlankType(tList)) tList.add(new Value(TYPEC, false)); //20.04.08
		OALD.display("end findTypeList(): " + tList);
		return tList; //.size() == 0 ? null : tList;
	}

	public Vector<Value> getTypeList() {
		return typeList;
	}

	/**
	 * Выполнение завершающих работ
	 */
	public void finalize() {
		//если одно значение то переносим его характеристики на слово
		if (typeList.size() == 1 && meanings.size() == 1) {
			Meaning mean = meanings.get(0);
			Value etl = typeList.get(0);
			Value mtl = mean.typeList.get(0);
			if ((etl.value.equals(TYPEC) && etl.isVisible == false) && mtl.isVisible) {
				etl.value = mtl.value;
				etl.isVisible = true;
				mtl.isVisible = false;
			}
			if (isThe == false && mean.isThe() == true) {
				isThe = mean.isThe();
				mean.setThe(false);
			}
		}
	}

	//static boolean isBlank(Vector<Value> ht) {
	static boolean isBlank(Vector ht) {
		boolean isb = true;
		if (ht != null && ht.size() > 0) isb = false;
		return isb;
	}

	static boolean isBlankType(Vector<Value> ht) {
		boolean isb = true;
		if (ht != null
				&& ((ht.size() > 1) || (ht.size() == 1 && ht.get(0).isVisible))) isb = false;
		return isb;
	}

	/**
	 * в типах слова и значений только то что в allowed
	 * 
	 * @param allowed
	 * @return
	 */
	/*
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
	*/

	/**
	 * в типах слова и значений только то что в allowed
	 * 
	 * @param allowed
	 * @return
	 */
	/*
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
	*/

	/**
	 * в типах слова и значений только то, что в allowed
	 * 
	 * @param allowed
	 * @return
	 * @throws IOException 
	 */
	public boolean inTypes10(String allowed) throws IOException {
		HashSet<String> types = getDistinctTypes();
		boolean ret = false;
		//if ((isBlank(types) && TYPEC.equals(allowed))
		//		|| (types.size() == 1 && types.contains(allowed))) ret = true;

		if (types.size() == 1 && types.contains(allowed)) ret = true;

		//System.out.println("inTypes1.1:" + toString() + "\n" + allowed + " -> "
		//		+ types + "=" + ret);
		return ret;
	}

	/**
	 * true if:
	 *  - в типе слова и значений только одно значение и оно совпадает с одним из allowed;
	 *  - в слове и значениях THE имеет одно и тоже значение.
	 * 
	 * @param allowed
	 * @return
	 * @throws IOException 
	 */
	public boolean inTypes1(String[] allowed) throws IOException {
		boolean ret = false;
		boolean the1 = true;
		boolean type1 = false;
		if (!isThe && meanings.size() > 1) {
			boolean the0 = meanings.get(0).isThe;
			for (int j = 1; j < meanings.size(); j++) {
				//OALD.display("  " + isThe + "==" + meanings.get(j).isThe);
				if (the0 != meanings.get(j).isThe) {
					the1 = false;
					break;
				}
			}
		}
		if (the1) {
			getDistinctTypes();
			if (distinctTypes.size() == 1) {
				for (int i = 0; i < allowed.length; i++) {
					if (distinctTypes.contains(allowed[i])) {
						type1 = true;
						break;
					}
				}
			}
		}
		if (the1 && type1) ret = true;
		//System.out.println("inTypes1.1:" + toString() + "\n" + allowed + " -> "
		//		+ types + "=" + ret);
		return ret;
	}

	/**
	 * true if:
	 *  - в типе слова есть все значения из allowed;
	 * 
	 * @param allowed
	 * @return
	 * @throws IOException 
	 */
	public boolean inTypes2(String[] allowed) throws IOException {
		boolean ret = false;
		boolean type1 = false;
		if (allowed.length == typeList.size()) {
			for (int i = 0; i < allowed.length; i++) {
				type1 = false;
				for (Value gt : typeList) {
					//OALD.display("  " + allowed[i] + "==" + gt.getValue());
					if (allowed[i].equalsIgnoreCase(gt.getValue())) {
						type1 = true;
						break;
					}
				}
				if (type1 == false) break;
			}
		}
		if (type1) ret = true;
		//System.out.println("inTypes1.1:" + toString() + "\n" + allowed + " -> "
		//		+ types + "=" + ret);
		return ret;
	}

	public static boolean hasType(Vector<Value> types, String type) {
		//OALD.display("hasType(): " + types + " < " + type);
		boolean has = false;
		for (Value grType : types) {
			//OALD.display("  " + type + "==" + grType.getType());
			if (type.equalsIgnoreCase(grType.getValue())) {
				has = true;
				break;
			}
		}
		return has;
	}

	public boolean verTypes() throws IOException {
		boolean ret = true;
		/*
		if (typeList != null) {
			String err = "";
			for (int j = 0; j < meanings.size(); j++) {
				HashSet<String> tm = meanings.get(j).getTypeList();
				if (tm != null && typeList.containsAll(tm)) err += "Не совпадает тип для значения "
						+ (j + 1) + "[" + tm + "]" + OALD.BR;
			}
			if (!Utils.isBlank(err)) {
				ret = false;
				setErr("Тип слова [" + typeList + "]" + OALD.BR + err);
			}
		}
		*/
		return ret;
	}

	/**
	 * список всех типов: слово + значения
	 * @throws IOException 
	 */
	//	public String[] getFullTypes2() throws IOException {
	//		if (fullTypes == null) {
	//			fullTypes = new String[meanings.size() + 1];
	//			fullTypes[0] = type;
	//			for (int j = 0; j < meanings.size(); j++)
	//				fullTypes[j + 1] = meanings.get(j).getType();
	//
	//			/*
	//			String deb = "getFullTypes()=" + toString() + "\n";
	//			for (int j = 0; j < types.length; j++)
	//				deb += " [" + types[j] + "]";
	//			System.out.println(deb);
	//			*/
	//		}
	//		return fullTypes;
	//	}
	/**
	 * список только уникальных типов для слова + значения.
	 * [C] и [] представлены как [C]
	 * @return
	 * @throws IOException
	 */
	//	public Vector<Value> getDistinctTypes1() throws IOException {
	//		if (distinctTypes == null) {
	//			distinctTypes = new Vector<Value>();
	//			if (typeList != null) {
	//				distinctTypes.addAll(typeList);
	//			}
	//			for (int j = 0; j < meanings.size(); j++) {
	//				Vector<Value> tl = meanings.get(j).getTypeList();
	//				if (tl != null) distinctTypes.addAll(tl);
	//			}
	//			if (isBlank(distinctTypes)) distinctTypes.add(new Value(TYPEC, false));
	//			/*
	//			if (distinctTypes.contains(TYPEBLANK)) {
	//				distinctTypes.remove(TYPEBLANK);
	//				distinctTypes.add(TYPEC);
	//			}
	//			*/
	//		}
	//		//System.out.println("getDistinctTypes()=" + types);
	//		return distinctTypes;
	//	}
	/**
	 * список только уникальных типов для слова + значения.
	 * @return
	 * @throws IOException
	 */
	public HashSet<String> getDistinctTypes() throws IOException {
		if (distinctTypes == null) {
			distinctTypes = new HashSet<String>();
			for (Value gt : typeList) {
				distinctTypes.add(gt.value);
			}
			for (int j = 0; j < meanings.size(); j++) {
				Vector<Value> tl = meanings.get(j).getTypeList();
				for (Value gt : tl) {
					distinctTypes.add(gt.value);
				}
			}
		}
		//System.out.println("getDistinctTypes()=" + types);
		return distinctTypes;
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
			boolean sThe = word.toUpperCase().startsWith(THETHE + " ");
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

	public void setTypeList(Vector<Value> tList) {
		typeList = tList;
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
	public void analyzeEntry(Element entry) throws IOException {
		OALD.display("analyzeEntry()");
		Element hg = entry.getFirstChildElement("h-g");
		//Elements hgChildren = hg.getChildElements(); //берем список
		//OALD.printElement(1, hg, null);
		//OALD.printChildren(2, hg.getChildElements("z"), "z list");
		analyseHG(hg);

		if (!badEntry()) {
			if (posList.get(POSNOUN) != null) {
				printPosList.add(POSNOUN);
				analyzePG(entry, hg, POSNOUN);
			}

			if (posList.get(POSADJ) != null && analyzePG(entry, hg, POSADJ)) printPosList
					.add(POSADJ);

			if (printPosList.size() > 0) {
				finalize();
				if (meanings.size() == 0) OALD.writeLog("WAR: " + toString()
						+ " Не найдено значений");
			}
		}
		OALD.display("end analyzeEntry(): " + printPosList);
	}

	/**
	 * adj у которых есть значение noun:
	 * the abobe
	 * absurd в adj. 2 noun p. 00149
	 * the blind adj, noun p.150 03840
	 * the deaf adj p.391 09447
	 * the poor
	 * the rich
	 * 
	 * слова с adj, noun (blind) --> other
	 * слова с adj       (deaf)  --> adj
	 *  
	 * @param hg
	 * @return
	 * @throws IOException
	 */
	void analyseHG(Element hg) throws IOException {
		OALD.display("analyzeHG()");
		Elements hgChildren = hg.getChildElements(); //берем список
		posList = findPosList(hg.getChildElements("z"));
		setTypeList(findTypeList(hgChildren));

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
		//OALD.printElement(2, h, null);
		setWord(the + h.getValue());

		OALD.display("end analyseHG() type:" + getTypeList());
	}

	/**
	 *
	  1) один pos
	    INFO: no p-g - (adjective) entry00003.xml_A, A1, [C.false]
	    INFO: no p-g - (noun) entry00010.xml_A, aardvark, [C.false]
	    INFO: no p-g - (noun) entry00013.xml_A, abacus, [C.false]
	    INFO: no p-g - (noun) entry00043.xml_A, abeyance, [C.false]
	    <entry>
	      <h-g>
	      	<n-g> meaning </n-g>
		      <n-g> meaning </n-g>
		      ....
		    </h-g>  

	  2) 
	    INFO: p-g - (noun) entry00016.xml_A, abandon, [C.false] i=1 ngElements=1 sdgElements=0
			<entry>  
			  <p-g>
			    <z> pos </z>
	        <n-g> meaning </n-g>
	        <n-g> meaning </n-g>
	        ....
	        
	  3) <n-g> внутри <sd-g>
	    INFO: p-g - (noun) entry00223.xml_A, account, [C.false] i=0 ngElements=0 sdgElements=6
	    INFO: p-g - (noun) entry00330.xml_A, action, [C.false] i=0 ngElements=0 sdgElements=8
	    INFO: p-g - (noun) entry09325.xml_D, dark, [C.false] i=1 ngElements=0 sdgElements=2 p. 386 
			<entry>  
			  <p-g>
		      <z> pos </z>
		      <sd-g>
		        <n-g> meaning </n-g>
		        <n-g> meaning </n-g>
		        ....
		      </sd-g>
	     
	  4) 
	    INFO: p-g - (noun) entry29548.xml_P, public, [C.false] i=1 ngElements=1 sdgElements=2
	    INFO: p-g - (adjective) entry29548.xml_P, public, [C.false] i=0 ngElements=0 sdgElements=5
		  <entry>  
		    <p-g>
		      <z> pos </z>
		      <n-g>
		        <span type </span>
		      </n-g>  
		      <sd-g>
		        <n-g> meaning </n-g>
		        <n-g> meaning </n-g>
		        ....
		      </sd-g>  
	 * 
	 * @param entry
	 * @param hg
	 * @param pos в какой части речи (noun, adj) ищем NOUN
	 * @return true - найден  NOUN в pos
	 *         false - не найден
	 * @throws IOException
	 */
	public boolean analyzePG(Element entry, Element hg, String pos)
			throws IOException {
		OALD.display("analyzePG(" + pos + ")");
		boolean posPos = false;
		Elements ngElements = null; //список элементов <n-g>
		/* список значений <dr-g> 
		 * это случай когда adj. находится в noun
		 * entry22273.xml, mainstream, [C.false], posList: {adjective=1, verb=2, noun=0}
		 * 22.04.08
		 */
		//Elements drgElements = null;
		Elements sdgElements = null; //список элементов <sd-g>
		//1. ищем pos
		Element pg = entry.getFirstChildElement("p-g");
		//Element drg = entry.getFirstChildElement("dr-g");
		if (pg != null) { //1.1. если есть pg значит несклько pos
			//if (pg != null || drg != null) { //1.1. если есть pg значит несклько pos
			OALD.display("1.1. '" + pos + "' pg=" + pg); // + " drg=" + drg);
			//Elements entryChildren = entry.getChildElements("p-g"); //берем список p-g
			Elements entryChildren = entry.getChildElements(); //берем список p-g, dr-g
			for (int i = 0; i < entryChildren.size(); i++) { //ищем pos
				//OALD.display("i=" + i);
				Element el = entryChildren.get(i);
				if (el.getQualifiedName().equalsIgnoreCase("p-g")) {
					//if (el.getQualifiedName().equalsIgnoreCase("p-g")
					//		|| el.getQualifiedName().equalsIgnoreCase("dr-g")) {
					//OALD.printElement(1, el, "<p-g> i=" + i);
					OALD.display("<p-g> i=" + i + "=" + el);
					//ищем pos
					Properties pgPosList = findPosList(el.getChildElements("z"));
					if (pgPosList.get(pos) != null) {
						if (isBlankType(getTypeList())) setTypeList(Entry.findTypeList(el
								.getChildElements("span")));
						//OALD.display("type=" + typeList);
						//1
						//ngElements = el.getChildElements("n-g"); //берем список значений <n-g>
						//if (ngElements.size() == 0)						sdgElements = el.getChildElements("sd-g"); //берем список значений <sd-g>, если <n-g> внутри <sd-g>
						//						OALD.writeLog("INFO: p-g - (" + pos + ") " + toString() + " i=" + i
						//								+ " ngElements=" + ngElements.size() + " sdgElements="
						//								+ sdgElements.size());
						//??????
						//проверить ошибки типа:
						//Не найдено значение типа 'noun'
						//Не найдено значение типа 'adjective'
						//2
						//sdgElements = el.getChildElements("sd-g"); //берем список значений <sd-g>, если <n-g> внутри <sd-g>
						//if (sdgElements.size() == 0) ngElements = el
						//		.getChildElements("n-g"); //берем список значений  <n-g>
						//3
						ngElements = el.getChildElements("n-g"); //берем список значений <n-g>
						//drgElements = el.getChildElements("dr-g"); //берем список значений <dr-g> entry22273.xml, mainstream, [C.false], posList: {adjective=1, verb=2, noun=0}
						sdgElements = el.getChildElements("sd-g"); //берем список значений <sd-g>, если <n-g> внутри <sd-g>
						OALD.writeLog("INFO: p-g.1 - (" + pos + ") " + toString() + " i="
								+ i + " ngElements=" + ngElements.size()
								//+ " drgElements=" + drgElements.size() 
								+ " sdgElements=" + sdgElements.size());
						//OALD.display("1.1. ok '" + pos + "' ngElements="+ ngElements.size() + " sdgElements=" + sdgElements.size());
						break;
					}
				}
			} //for
			//22.04.08
			if (ngElements == null && posList.size() == 1) { //entry07974.xml
				ngElements = pg.getChildElements("n-g"); //берем список значений <n-g>
				//drgElements = pg.getChildElements("dr-g"); //берем список значений <dr-g> entry22273.xml, mainstream, [C.false], posList: {adjective=1, verb=2, noun=0}
				sdgElements = pg.getChildElements("sd-g"); //берем список значений <sd-g>, если <n-g> внутри <sd-g>
				OALD.writeLog("INFO: p-g.2 - (" + pos + ") " + toString()
						+ " ngElements=" + ngElements.size()
						//+ " drgElements="+ drgElements.size() 
						+ " sdgElements=" + sdgElements.size());
			}
		} else { //1.2. если нет pg значит один pos
			OALD.writeLog("INFO: no p-g - (" + pos + ") " + toString());
			//OALD.display("1.2. '" + pos + "' no p-g");
			if (posList.get(pos) != null) {
				ngElements = hg.getChildElements("n-g"); //берем список значений <n-g>
				//drgElements = hg.getChildElements("dr-g"); //берем список значений <dr-g> entry22273.xml, mainstream, [C.false], posList: {adjective=1, verb=2, noun=0}
				sdgElements = hg.getChildElements("sd-g"); //берем список значений <sd-g>, если <n-g> внутри <sd-g>
			}
		}

		if (ngElements == null) {
			if (POSNOUN.equalsIgnoreCase(pos)) {
				setErr("Не найдено значение для '" + pos + "'");
				OALD.writeEntry(this, Entry.TYPEERR);
			} else {
				OALD.writeLog("INFO: Не найдено значение для '" + pos
						+ "'. Возможно это случай когда adj. находится в noun");
			}
		} else {

			//2.1. разбираем значения: <n-g> </n-g>
			if (ngElements.size() > 0) {
				OALD.display("2.1 ng=" + ngElements.size());
				posPos = analyzeNG(ngElements, pos);
			}

			//2.3. разбираем значения: <dr-g> </dr-g>
			//			if (drgElements != null && drgElements.size() > 0) {
			//				OALD.display("2.3 drg=" + drgElements.size());
			//				posPos = analyzeNG(drgElements, pos);
			//			}

			//2.2. разбираем значения: <sd-g> <n-g> </n-g> </sd-g>
			//else 
			if (sdgElements != null && sdgElements.size() > 0) {
				OALD.display("2.2 sdg=" + sdgElements.size());
				for (int i = 0; i < sdgElements.size(); i++) {
					OALD.display("  2.2 ng=" + ngElements.size());
					posPos = analyzeNG(sdgElements.get(i).getChildElements("n-g"), pos);
				}
			}
		}
		OALD.display("end analyzePG(" + pos + ")=" + posPos);
		return posPos;
	}

	/**
	 * Заполняем значения
	 * @param ngElements
	 * @param pos
	 * @return
	 * @throws IOException
	 */
	boolean analyzeNG(Elements ngElements, String pos) throws IOException {
		OALD.display("analyzeNG(" + pos + ")");
		boolean posPos = false;
		//OALD.printChildren(2, ngElements, "ngElements");
		if (isBlankType(getTypeList())) setTypeList(Entry.findTypeList(ngElements));
		//OALD.display("  ng type=" + getTypeList());

		for (int i = 0; i < ngElements.size(); i++) {
			Element el = ngElements.get(i);
			if (el.getQualifiedName().equalsIgnoreCase("n-g")) {
				//OALD.printElement(2, el, "   <n-g> i=" + i);
				if (POSNOUN.equals(pos)) { //noun
					posPos = addMeaning(el, pos); //noun
					//posPos = true;
				} else { //adj
					//ищем noun
					Properties ngPosList = findPosList(el.getChildElements("z"));
					if (ngPosList.get(POSNOUN) != null) {
						posPos = addMeaning(el, pos); //noun
						//posPos = true;
					}
				}
			}
		}
		OALD.display("end analyzeNG(" + pos + ")=" + posPos);
		return posPos;
	}

	public Properties getPosList() {
		return posList;
	}

	/**
	 * 
	 * @return true если слово (но не The)  начинается с заглавной буквы
	 */
	public boolean isInitCap() {
		boolean isu = false;
		StringTokenizer st = new StringTokenizer(word, " ");
		for (int i = 0; st.hasMoreTokens(); i++) {
			String tok = st.nextToken().trim();
			if (!tok.equalsIgnoreCase(THETHE)) {
				isu = Character.isUpperCase(tok.charAt(0));
				break;
			}
		}
		return isu;
	}
}
