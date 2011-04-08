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
 * 
 * @author администратор
 * @since 09.03.08
 */
public class Entry {
	public static final String TAG_SDG = "sd-g";

	public static final String POSNOUN = "noun";

	public static final String POSADJ = "adjective";
	
	public static final String POSVERB= "verb";

	public static final String GROUPOTHER = "OTHER";

	public static final String GROUPERR = "ERR";
	
	public static final String GROUPWAR = "WAR";

	public static final String GROUPFULL = "FULL";
	
	/** наличие более одной группы у слова*/ 
	public static final String GROUPGROUPS= "GROUPS";

	public static final String GROUPUPPER = "UPPER";

	public static final String GROUPC = "C";

	// public static final String TYPEBLANK = ""; //== TYPEC

	public static final String GROUPU = "U";

	// public static final String TYPECU = "C, U";

	// public static final String TYPEUC = "U, C";

	public static final String GROUPPL = "pl.";

	/** "usually pl." */
	public static final String GROUPPL2 = "usually pl.";

	public static final String GROUPSING = "sing.";

	/** "usually sing." */
	public static final String GROUPSING2 = "usually sing.";

	/** "sing.+ sing./pl. v." */
	public static final String GROUPSING3 = "sing.+ sing./pl. v.";

	/** "C+sing./pl. v." */
	public static final String GROUPSING4 = "C+sing./pl. v.";

	/** "U+sing./pl. v." */
	public static final String GROUPSING5 = "U+sing./pl. v.";

	/** Список групп кот. игнорируются */
	static HashSet<String> ignoreGroups = null;

	public static final String THETHE = "THE";

	// свойства /////////////////////////////////////////////////////////
	/**
	 * список грамматических групп для слова:
	 * <h-g> [<n-g>] (у слова одно значение 15.xml abalone)
	 * or
	 * [
	 * <p-g>
	 * <pos> [<n-g>]] (у слова несколько pos и значений 31758.xml rose) or [
	 * <p-g>
	 * <pos> [<sd-g> <n-g>]] (у слова несколько pos и значений 29548.xml public)
	 * <span> <z> [<gr> <zgct>] <span> <z> [<gr> <zgpl> <it>] (29548.xml public)
	 */
	private Vector<Value> groupList = new Vector<Value>();

	/**
	 * список ссылок на другие слова
	 * end analyzeHG(): entry00051.fix.xml, ability, gr[C:false], pos{}
	 * <h-g> <xr>
	 * <n-g> <xr>
	 */
	//private Vector<String> xrList = new Vector<String>();

	HashSet<String> distinctGroups = null;

	/**
	 * список частей речи (part of speech) для всего entry <h-g> <z> [<pos>] or
	 * [
	 * <p-g>
	 * <z> [<pos>]] (31758.xml rose)
	 */
	Properties posList = new Properties();

	/** имя файла (конструктор) */
	String fileName;

	/** (<h-g> <hs>) */
	private boolean isThe = false;

	/** первая буква слова (игнорируем the) (<h-g> <h>) */
	char symbol = ' ';

	/** слово (<h-g> <h>) */
	String word;

	/**
	 * список значений:
	 * <p-g>
	 * [<n-g>] (31758.xml rose) <h-g> [<n-g>] (126.xml absolutism)
	 */
	Vector<Meaning> meanings = new Vector<Meaning>();

	String error = "";
	String warning = "";

	public boolean skip = false;

	static {
		ignoreGroups = new HashSet<String>();
		ignoreGroups.add("not before noun");
		ignoreGroups.add("not usually before noun");
		ignoreGroups.add("often passive");
		ignoreGroups.add("only before noun");
		ignoreGroups.add("usually before noun");
		ignoreGroups.add("usually passive");
	}

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
		if (word.startsWith(THETHE + " ")) {// entry00249 the accused
			OALD.writeLog("the.1=" + word);
			symbol = word.charAt(4);
			isThe = true;
		} else
			symbol = word.charAt(0);
	}

	/*
	 * уберем лишнее в конце row со слова word
	 */
	static String rtrim(String row, String word) {
		if (row != null && word != null) {
			int pos = row.toUpperCase().indexOf(word.toUpperCase());
			if (pos != -1)
				row = row.substring(0, pos);
		}
		return row;
	}

	/**
	 * 
	 * @param ng
	 * @throws IOException
	 */
	public boolean addMeaning(Element ng) throws IOException {
		boolean add = false;
		Meaning mean = new Meaning(ng);
		meanings.add(mean);
		add = true;
		return add;
	}

	public String getAllMeaning() {
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

	public void setMeanings(Vector<Meaning> meanings) {
		this.meanings = meanings;
	}

	public String getErr() {
		return error;
	}

	public void setErr(String err) {
		this.error += err + OALD.BR;
		// OALD.displayErr(toString() + ".1: " + err);
	}

	public void setWar(String err) {
		this.warning += err + OALD.BR;
		// OALD.displayErr(toString() + ".1: " + err);
	}
	
	public String getFileName() {
		return fileName;
	}

	public String toString() {
		return fileName
				// + "_" + symbol
				+ ", " + word + ", gr" + groupList + ", pos" + posList; //+ ", xr" + xrList;
	}

	public String toStringFull() {
		String row = toString()
				+ (Utils.isBlank(error) ? "" : ("\nErr: " + error))
				+ (Utils.isBlank(warning) ? "" : ("\nWar: " + warning))
				+ "\nthe: "
				+ isThe
				// + "\nCategory: " + category
				+ "\nMeanings [" + meanings.size() + "]:\n";
		for (int i = 0; i < meanings.size(); i++) {
			Meaning mean = meanings.get(i);
			row += "  " + mean.toString() + "\n";
		}
		return row;
	}

	/**
	 * 
	 * @param z
	 * @return OALD.printChildren(2, hg.getChildElements("z"), "z list");
	 */
	static Properties findPosList(Elements zs) {
		Properties pList = new Properties();
		for (int i = 0; i < zs.size(); i++) {
			Elements poss = zs.get(i).getChildElements(); // берем список pos
			for (int i2 = 0; i2 < poss.size(); i2++) {
				Element epos = poss.get(i2);
				if (epos.getQualifiedName().equalsIgnoreCase("pos")) {// pos
					// OALD.printElement(3, epos, "findPosInPOS(" + pos + ")=" +
					// i2);
					// if (NOUN.equalsIgnoreCase(pos.getValue())) {
					pList.put(epos.getValue(), "" + i2);
					OALD.totalPOSList.add(epos.getValue());
				}
			}
			if (pList.size() > 0)
				break;
		}
		OALD.display("end findPosList()=" + pList);
		return pList;
	}

	/**
	 * ищем pos1 in pos2
	 * 
	 * @param pos1
	 * @param pos2
	 * @return
	 */
	static boolean isPos(String pos1, String pos2) {
		boolean isPos = false;
		// if (!Utils.isBlank(pos2)) isPos = pos1.indexOf(pos2) == -1 ? false :
		// true;
		if (pos1.equalsIgnoreCase(pos2))
			isPos = true; // 11.04.08
		// OALD.display("  isPos(" + pos1 + "," + pos2 + ")=" + isPos);
		return isPos;
	}

	/**
	 * ищем туре
	 * 
	 * <span class="label"> <z> [ <gr> <zgct>C</zgct> </gr> , <gr>
	 * <zgct>U</zgct> </gr> ] </z> </span>
	 * 
	 * 
	 * 1)Для слов с несколькими pos entry31758.xml rose 0.entry 1.h-g 2.<span
	 * class="label"> - группа
	 * <p:z>
	 * [
	 * <p:gr>
	 * <p:zgct>
	 * U
	 * </p:zgct>
	 * </p:gr> ]</p:z> </span>
	 * 
	 * 2.<span class="label">
	 * <p:z>
	 * [
	 * <p:gr>
	 * <p:zgct>
	 * C
	 * </p:zgct>
	 * </p:gr> , 
	 * <p:gr>
	 * <p:zgct>
	 * U
	 * </p:zgct>
	 * </p:gr> ]</p:z> </span>
	 * 
	 * <span class="label">
	 * <p:z>
	 * [
	 * <p:gr>
	 * pl.
	 * </p:gr>
	 * ]</p:z> </span> <span class="label">
	 * 
	 * //08759 crew, p. 362 <span class="label">
	 * <p:z>
	 * [
	 * <p:gr>
	 * <p:zgct>
	 * C
	 * </p:zgct>
	 * +sing./
	 * <p:zgpl>
	 * pl.
	 * </p:zgpl>
	 * <p:it>
	 * v
	 * </p:it>
	 * .</p:gr> ]</p:z>
	 * <p:z>
	 * </p:z>
	 * </span>
	 * 
	 * 
	 * //classified 06780 p.270 игнорируем <span class="label">
	 * <p:z>
	 * [
	 * <p:gr>
	 * <p:zgpl>
	 * usually before noun
	 * </p:zgpl>
	 * </p:gr> ]</p:z> </span> [only before noun]
	 * 
	 * 
	 * @param spanList
	 * @return
	 */
	static Vector<Value> findGroupList(Elements spanList) {
		// OALD.display("findGroupList(): " + spanList.size());
		Vector<Value> gList = new Vector<Value>();
		for (int i = 0; i < spanList.size(); i++) {
			Element span = spanList.get(i);
			// OALD.display("  " + i + " name=" + span.getQualifiedName()+
			// " class=" + span.getAttribute("class"));
			if (span.getQualifiedName().equalsIgnoreCase("span")
					&& span.getAttribute("class") != null
					&& span.getAttribute("class").getValue().equals("label")) {
				// OALD.printElement(2, span, "findTypeList()");
				Element z = span.getFirstChildElement("z");
				if (z != null) {
					Elements grs = z.getChildElements("gr");
					for (int j = 0; j < grs.size(); j++) {
						Element gr = grs.get(j);
						String group = gr.getValue();
						OALD.totalGroupList.add(group);
						if (!Utils.isBlank(group)
								&& !ignoreGroups.contains(group))
							gList.add(new Value(group, true));
					}
				}
			}
		}
		// entry00142.xml, abstract, type[],
		// if (isEmptyGroup(gList)) gList.add(new Value(GROUPC, false)); //
		// 20.04.08
		OALD.display("end findGroupList(" + spanList.size() + "): "
				+ gList.size() + gList);
		return gList;
	}

	public Vector<Value> getGroupList() {
		return groupList;
	}

	/**
	 * список ссылок на другие слова
	 * end analyzeHG(): entry00051.fix.xml, ability, gr[C:false], pos{}
	 * <xr xt="arrow">
	 *   <sc>
	 * 	  <xh type="dic" entry="00060">-able</xh>
	 *   </sc>
	 * </xr>
	 * 
	 * <h-g> <xr>
	 * <n-g> <xr>
	 * 
	 * @param xrs
	 * @return
	 */
	static Vector<String> findXRList(Elements xrs) {
		Vector<String> xList = new Vector<String>();
		for (int i = 0; i < xrs.size(); i++) {
			Element el = xrs.get(i);
			Elements scs = el.getChildElements("sc");
			for (int j = 0; j < scs.size(); j++) {
				Element xh = scs.get(j).getFirstChildElement("xh");
				String link = xh.getValue();
				if (xh.getAttribute("entry") != null)
					link = xh.getAttribute("entry").getValue() + "." + link;
				xList.add(link);
			}
		}
		OALD.display("end findXRList(" + xrs.size() + ")=" + xList);
		return xList;
	}

	/**
	 * Выполнение завершающих работ
	 */
	public void finalize() {
		// если одно значение то переносим его характеристики на слово 
		if (meanings.size() == 1) {
			//group
			Meaning mean = meanings.get(0);
			for (int i = 0; i < mean.groupList.size(); i++) {
				if (mean.groupList.get(i).isVisible()) {
					groupList.add(new Value(mean.groupList.get(i).getValue(),
							mean.groupList.get(i).isVisible()));
				}
			}
			mean.groupList.clear();
			if (groupList.size() > 1)
				for (int i = 0; i < groupList.size(); i++)
					if (!groupList.get(i).isVisible()) {
						groupList.remove(i);
						i--;
					}
			
			//THE
			if (isThe == false && mean.isThe() == true) {
				isThe = mean.isThe();
				mean.setThe(false);
			}
		}
	}

	// static boolean isBlank(Vector<Value> ht) {
	static boolean isBlank(Vector<?> ht) {
		boolean isb = true;
		if (ht != null && ht.size() > 0)
			isb = false;
		return isb;
	}

	static boolean isEmptyGroup(Vector<Value> grs) {
		boolean ise = true;
		if (grs != null
				&& ((grs.size() > 1) || (grs.size() == 1 && grs.get(0)
						.isVisible())))
			ise = false;
		return ise;
	}

	/**
	 * в типах слова и значений только то, что в allowed
	 * 
	 * @param allowed
	 * @return
	 * @throws IOException
	 */
	public boolean inGroups10(String allowed) throws IOException {
		HashSet<String> groups = getDistinctGroups();
		boolean ret = false;
		// if ((isBlank(types) && TYPEC.equals(allowed))
		// || (types.size() == 1 && types.contains(allowed))) ret = true;

		if (groups.size() == 1 && groups.contains(allowed))
			ret = true;

		// System.out.println("inTypes1.1:" + toString() + "\n" + allowed +
		// " -> "
		// + types + "=" + ret);
		return ret;
	}

	/**
	 * true if: - в типе слова и значений только одно значение и оно совпадает с
	 * одним из allowed; - в слове и значениях THE имеет одно и тоже значение.
	 * 
	 * @param allowed
	 * @return
	 * @throws IOException
	 */
	public boolean inGroup1(String[] allowed) throws IOException {
		boolean ret = false;
		boolean the1 = true;
		boolean group1 = false;
		if (!isThe && meanings.size() > 1) {
			boolean the0 = meanings.get(0).isThe;
			for (int j = 1; j < meanings.size(); j++) {
				// OALD.display("  " + isThe + "==" + meanings.get(j).isThe);
				if (the0 != meanings.get(j).isThe) {
					the1 = false;
					break;
				}
			}
		}
		if (the1) {
			getDistinctGroups();
			if (distinctGroups.size() == 1) {
				for (int i = 0; i < allowed.length; i++) {
					if (distinctGroups.contains(allowed[i])) {
						group1 = true;
						break;
					}
				}
			}
		}
		if (the1 && group1)
			ret = true;
		// System.out.println("inTypes1.1:" + toString() + "\n" + allowed +
		// " -> "
		// + types + "=" + ret);
		return ret;
	}

	/**
	 * true if: - в типе слова есть все значения из allowed;
	 * 
	 * @param allowed
	 * @return
	 * @throws IOException
	 */
	public boolean inGroups2(String[] allowed) throws IOException {
		boolean ret = false;
		boolean group1 = false;
		if (allowed.length == groupList.size()) {
			for (int i = 0; i < allowed.length; i++) {
				group1 = false;
				for (Value gt : groupList) {
					// OALD.display("  " + allowed[i] + "==" + gt.getValue());
					if (allowed[i].equalsIgnoreCase(gt.getValue())) {
						group1 = true;
						break;
					}
				}
				if (group1 == false)
					break;
			}
		}
		if (group1)
			ret = true;
		// System.out.println("inTypes1.1:" + toString() + "\n" + allowed +
		// " -> "
		// + types + "=" + ret);
		return ret;
	}

	public static boolean hasGroup(Vector<Value> groups, String group) {
		// OALD.display("hasType(): " + types + " < " + type);
		boolean has = false;
		for (Value gr : groups) {
			// OALD.display("  " + type + "==" + grType.getType());
			if (group.equalsIgnoreCase(gr.getValue())) {
				has = true;
				break;
			}
		}
		return has;
	}

	public boolean verGroups() throws IOException {
		boolean ret = true;
		/*
		 * if (typeList != null) { String err = ""; for (int j = 0; j <
		 * meanings.size(); j++) { HashSet<String> tm =
		 * meanings.get(j).getTypeList(); if (tm != null &&
		 * typeList.containsAll(tm)) err += "Не совпадает тип для значения " +
		 * (j + 1) + "[" + tm + "]" + OALD.BR; } if (!Utils.isBlank(err)) { ret
		 * = false; setErr("Тип слова [" + typeList + "]" + OALD.BR + err); } }
		 */
		return ret;
	}

	/**
	 * список только уникальных групп для слова и значений
	 * 
	 * @return
	 * @throws IOException
	 */
	public HashSet<String> getDistinctGroups() throws IOException {
		if (distinctGroups == null) {
			distinctGroups = new HashSet<String>();
			for (Value gt : groupList) {
				//System.out.println("gDT.1=" + gt);
				if (gt.isVisible())
					distinctGroups.add(gt.value);
			}
			for (int j = 0; j < meanings.size(); j++) {
				Vector<Value> tl = meanings.get(j).getGroupList();
				for (Value gt : tl) {
					//System.out.println("gDT.2=" + gt);
					if (gt.isVisible())
						distinctGroups.add(gt.value);
					distinctGroups.add(gt.value);
				}
			}
		}
		//System.out.println("getDistinctGroups()=" + distinctGroups);
		return distinctGroups;
	}

	/**
	 * не анализируем следующие слова:
	 * - posList не содержит noun
	 * - из 2 и более слов (для THE из 3 и более слов)
	 * - одна буква
	 * - только заглавные буквы
	 * - аббревиатуры (D.O.B abbr.)
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean skipEntry() throws IOException {
		boolean ret = false;
		if (word.length() <= 1)
			ret = true;
		else if (posList.get(POSNOUN) == null)
			ret = true;
		else {
			StringTokenizer st = new StringTokenizer(word, " ");
			int cntT = st.countTokens();
			boolean sThe = word.toUpperCase().startsWith(THETHE + " ");
			if ((sThe && cntT >= 3) || (!sThe && cntT >= 2))
				ret = true;
			else {
				// \p{Punct} Punctuation: One of
				// !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
				if (word.matches("[\\p{Punct}[A-Z]]*") == true)
					ret = true;
			}
		}
		if (ret == true) {
			skip = true;
			OALD.cntSkip++;
			OALD.writeLog(toString() + ": skipped");
		}
		return ret;
	}

	public void setGroupList(Vector<Value> gList) {
		groupList = gList;
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
	 * <entry> <h-g> [<n-g>] (у слова одно значение 15.xml abalone)
	 * <entry>
	 * <p-g>
	 * [<n-g>] (у слова несколько значений 31758.xml rose)
	 * 
	 * @param entry
	 * @throws IOException
	 */
	public void analyzeEntry(Element entry) throws IOException {
		//OALD.DEBUG=false;
		OALD.display("analyzeEntry()");
		analyzeHG(entry.getFirstChildElement("h-g"));
		/*
		//у слова только ссылка (entry00370.xml, acy, gr[C:false], pos{}, xr[])
		xrList.addAll(findXRList(entry.getChildElements("xr")));
		// у слова должно быть либо pos, либо xr
		if (posList.size() == 0 && xrList.size()==0)
			setErr("Не найдено POS и XR");
		*/	

		if (!skipEntry()) {
			analyzePG(entry.getChildElements("p-g"));

			finalize();
			// проверка ошибок
			//if (meanings.size() == 0) OALD.writeLog("WAR: " + toString() + " Не найдено значений");
			if (meanings.size() == 0) setWar(toString() + " Не найдено значений");
			getDistinctGroups();
			if (distinctGroups.size() == 0 && groupList.size() == 0)
				setErr("Не найдено грамматических характеристик: "
						+ toStringFull());
		}
		//System.out.println("end analyzeEntry(): " + toStringFull());
		OALD.display("end analyzeEntry(): " + toStringFull());
	}

	/**
	 * adj у которых есть значение noun:
	 * the abobe absurd в adj. 2 noun p. 00149
	 * the blind adj, noun p.150 03840
	 * the deaf adj p.391 09447 the poor the rich
	 * 
	 * слова с adj, noun (blind) --> other слова с adj (deaf) --> adj
	 * 
	 * @param hg
	 * @return
	 * @throws IOException
	 */
	void analyzeHG(Element hg) throws IOException {
		OALD.display("analyzeHG()");

		String the = "";
		Element hs = hg.getFirstChildElement("hs"); // the
		if (hs != null) {
			OALD.printElement(2, hs, null);
			if (hs.getValue().trim().toUpperCase().equals(THETHE)) {
				OALD.writeLog("the.2=" + hs.getValue());
				isThe = true;
				the = hs.getValue() + " ";
			}
		}

		Element h = hg.getFirstChildElement("h"); // слово
		setWord(the + h.getValue());

		posList = findPosList(hg.getChildElements("z"));
		groupList = findGroupList(hg.getChildElements("span"));
		if (isEmptyGroup(groupList)) groupList.add(new Value(GROUPC, false));
		//xrList = findXRList(hg.getChildElements("xr"));

		analyzeNG(hg.getChildElements("n-g"));

		OALD.display("end analyzeHG(): " + toString() + "\n");
	}

	/**
	 * Ищем p-g с pos=noun и обрабатываем его
	 * 
	 1) один pos INFO: no p-g - (adjective) entry00003.xml_A, A1, [C.false]
	 * INFO: no p-g - (noun) entry00010.xml_A, aardvark, [C.false] INFO: no p-g
	 * - (noun) entry00013.xml_A, abacus, [C.false] INFO: no p-g - (noun)
	 * entry00043.xml_A, abeyance, [C.false] <entry> <h-g> <n-g> meaning </n-g>
	 * <n-g> meaning </n-g> .... </h-g>
	 * 
	 * 2) INFO: p-g - (noun) entry00016.xml_A, abandon, [C.false] i=1
	 * ngElements=1 sdgElements=0 <entry>
	 * <p-g>
	 * <z> pos </z> <n-g> meaning </n-g> <n-g> meaning </n-g> ....
	 * 
	 * 3) <n-g> внутри <sd-g> INFO: p-g - (noun) entry00223.xml_A, account,
	 * [C.false] i=0 ngElements=0 sdgElements=6 INFO: p-g - (noun)
	 * entry00330.xml_A, action, [C.false] i=0 ngElements=0 sdgElements=8 INFO:
	 * p-g - (noun) entry09325.xml_D, dark, [C.false] i=1 ngElements=0
	 * sdgElements=2 p. 386 <entry>
	 * <p-g>
	 * <z> pos </z> <sd-g> <n-g> meaning </n-g> <n-g> meaning </n-g> ....
	 * </sd-g>
	 * 
	 * 4) INFO: p-g - (noun) entry29548.xml_P, public, [C.false] i=1
	 * ngElements=1 sdgElements=2 INFO: p-g - (adjective) entry29548.xml_P,
	 * public, [C.false] i=0 ngElements=0 sdgElements=5 <entry>
	 * <p-g>
	 * <z> pos </z> <n-g> <span group </span> </n-g> <sd-g> <n-g> meaning </n-g>
	 * <n-g> meaning </n-g> .... </sd-g>
	 * 
	 * @param pgList
	 * @return true если найден p-g с pos=noun
	 * @throws IOException
	 */
	public boolean analyzePG(Elements pgList) throws IOException {
		OALD.display("analyzePG(" + pgList.size() + ")");
		boolean hasNoun = false;
		for (int i = 0; i < pgList.size(); i++) {
			Element el = pgList.get(i);
			Properties pgPosList = findPosList(el.getChildElements("z"));
			if (pgPosList.get(POSNOUN) != null) {
				analyzeNG(el.getChildElements("n-g"));
				analyzeSDG(el.getChildElements(TAG_SDG));
				hasNoun = true;
				break;
			}
		}
		OALD.display("end analyzePG(" + pgList.size() + ")=" + hasNoun);
		return hasNoun;
	}

	public void analyzeSDG(Elements sdgList) throws IOException {
		OALD.display("analyzeSDG(" + sdgList.size() + ")");
		for (int i = 0; i < sdgList.size(); i++) {
			analyzeNG((sdgList.get(i)).getChildElements("n-g"));
		}
		OALD.display("end analyzeSDG(" + sdgList.size() + ")");
		return;
	}

	/**
	 * Заполняем значения
	 * 
	 * @param ngElements
	 * @param pos
	 * @return
	 * @throws IOException
	 */
	void analyzeNG(Elements ngElements) throws IOException {
		OALD.display("analyzeNG(" + ngElements.size() + ")");
		for (int i = 0; i < ngElements.size(); i++) {
			Element el = ngElements.get(i);
			if (el.getQualifiedName().equalsIgnoreCase("n-g")) {
				// OALD.printElement(2, el, "   <n-g> i=" + i);
				addMeaning(el);
			}
		}
		OALD.display("end analyzeNG(" + ngElements.size() + ")");
		return;
	}

	public Properties getPosList() {
		return posList;
	}

	/**
	 * 
	 * @return true если слово (но не The) начинается с заглавной буквы
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
	
	/**
	 * Возвращаем true только в случае если pos стоит в num позиции 
	 * @param pos
	 * @param num
	 * @return
	 */
	public boolean hasPOS(String pos, int num) {
		boolean has = false;
		String pn = (String) posList.get(pos);
		if (pn != null) {
			int posNum = Integer.parseInt(pn) + 1;
			if (num == posNum)
				has = true;
		}
		return has;
	}
}
