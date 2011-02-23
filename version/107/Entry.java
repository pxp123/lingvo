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
 * �������� ������ �����
 * @author �������������
 * @since 09.03.08
 */
public class Entry {
	public static final String POSNOUN = "noun";

	public static final String POSADJ = "adjective";

	public static final String TYPEOTHER = "OTHER";

	public static final String TYPEERR = "ERR";

	public static final String TYPEFULL = "FULL";

	public static final String TYPEC = "C";

	//public static final String TYPEBLANK = ""; //== TYPEC

	public static final String TYPEU = "U";

	public static final String TYPECU = "C, U";

	public static final String TYPEUC = "U, C";

	public static final String TYPEPL = "pl.";

	public static final String TYPESING = "sing.";

	public static final String THETHE = "THE";

	/**-1: ���. �� �������*/
	static int NPNOTFOUND = -1;

	/**-2: ������� �� ���.*/
	static int NPNOTNOUN = -2;

	/**�������������� ��� [x,y]��� ���� �������� �����*/
	//String type = "";
	HashSet<String> typeList = null;

	//String[] fullTypes = null;

	HashSet<String> distinctTypes = null;

	/**������ ������ ���� ��� ����� entry*/
	Properties posList = new Properties();

	/** ������ ����� ������ ���� ��������*/
	HashSet<String> printPosList = new HashSet<String>();

	/**��� �����*/
	String fileName;

	char symbol = ' ';

	/**�����*/
	String word;

	/**������� ���������������� � ������ p-g[]*/
	//int nounPos = NPNOTFOUND;
	/**������� ���������������*/
	//int adjPos = NPNOTFOUND;
	/**������*/
	//String category = "";
	/**������ ��������*/
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
		word = word.replaceAll("�", "");
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
	 *������ ������ � ����� row �� ����� word 
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
		Meaning mean = new Meaning(ng, pos);
		OALD.display("addMeaning(" + pos + ") 1=" + POSNOUN.equals(pos) + " 2="
				+ isPos(POSNOUN, mean.getPos()) + " 3=" + mean.getPos());
		if (POSNOUN.equals(pos) || isPos(POSNOUN, mean.getPos())) {
			meanings.add(mean);
			add = true;
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
		this.error += err;
		//OALD.displayErr(toString() + ".1: " + err);
	}

	public String getFileName() {
		return fileName;
	}

	public String toString() {
		return fileName + "_" + symbol + ", " + typeList + ", " + word;
	}

	public String toStringFull() {
		String row = toString() + "\nErr: " + error + "\nthe: " + isThe
		//+ "\nCategory: " + category
				+ ", posList: " + posList + "\nMeanings [" + meanings.size() + "]:\n";
		for (int i = 0; i < meanings.size(); i++) {
			Meaning mean = meanings.get(i);
			row += "  " + mean.toString() + "\n";
		}
		return row;
	}

	/**���� pos*/
	/*
	static int findPosInZ(Elements hgChildren, String pos) {
		int np = NPNOTFOUND;
		//Elements hgChildren = hg.getChildElements(); //����� ������ z
		for (int i = 0; i < hgChildren.size(); i++) {
			Element z = hgChildren.get(i);
			if (z.getQualifiedName() == "z") {//������ pos
				OALD.printElement(2, z, "������ pos=" + i);
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
		Elements poss = z.getChildElements(); //����� ������ pos
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
			Elements poss = zs.get(i).getChildElements(); //����� ������ pos
			for (int i2 = 0; i2 < poss.size(); i2++) {
				Element epos = poss.get(i2);
				if (epos.getQualifiedName() == "pos") {// pos
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
	 * ���� pos1 in pos2
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
	 * ���� ���� ��� ����� entry
	 * 
	 * 1)��� ���� � ����������� pos entry31758.xml rose 
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
	          ,�
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
	    
	    //classified 06780 p.270 ����������
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
	static HashSet findTypeList(Elements hgChildren) {
		HashSet<String> tList = new HashSet();
		for (int i = 0; i < hgChildren.size(); i++) {
			Element span = hgChildren.get(i);
			//OALD.display("  span=" + span.getAttribute("class").getValue());
			if (span.getQualifiedName() == "span"
					&& span.getAttribute("class") != null
					&& span.getAttribute("class").getValue().equals("label")) {
				//OALD.printElement(2, span, "findTypeList()");
				Element z = span.getFirstChildElement("z");
				if (z != null) {
					Elements grs = z.getChildElements("gr");
					for (int j = 0; j < grs.size(); j++) {
						Element gr = grs.get(j);
						Element zgct = gr.getFirstChildElement("zgct");
						String type = null;
						if (zgct != null) type = zgct.getValue();
						else type = gr.getValue();
						if (!Utils.isBlank(type)
								&& !(type.equalsIgnoreCase("usually before noun") || type
										.equalsIgnoreCase("only before noun"))) tList.add(type);
					}
				}
			}
		}
		return tList; //.size() == 0 ? null : tList;
	}

	public HashSet<String> getTypeList() {
		return typeList;
	}

	/**
	 * ���������� ����������� �����
	 */
	public void finalize() {
		//���� ���� �������� �� ��������� ��� �������������� �� �����
		if (meanings.size() == 1) {
			Meaning mean = meanings.get(0);
			if (isBlank(typeList) && !isBlank(mean.getTypeList())) {
				typeList = mean.getTypeList();
				mean.setTypeList(new HashSet<String>());
			}
			if (isThe == false && mean.isThe() == true) {
				isThe = mean.isThe();
				mean.setThe(false);
			}
		}
	}

	static boolean isBlank(HashSet<String> ht) {
		boolean isb = true;
		if (ht != null && ht.size() > 0) isb = false;
		return isb;
	}

	/**
	 * � ����� ����� � �������� ������ �� ��� � allowed
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
	 * � ����� ����� � �������� ������ �� ��� � allowed
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
	 * � ����� ����� � �������� ������ ��, ��� � allowed
	 * 
	 * @param allowed
	 * @return
	 * @throws IOException 
	 */
	public boolean inTypes1(String allowed) throws IOException {
		HashSet<String> types = getDistinctTypes();
		boolean ret = false;
		//if ((isBlank(types) && TYPEC.equals(allowed))
		//		|| (types.size() == 1 && types.contains(allowed))) ret = true;

		if (types.size() == 1 && types.contains(allowed)) ret = true;

		//System.out.println("inTypes1.1:" + toString() + "\n" + allowed + " -> "
		//		+ types + "=" + ret);
		return ret;
	}

	public boolean verTypes() throws IOException {
		boolean ret = true;
		/*
		if (typeList != null) {
			String err = "";
			for (int j = 0; j < meanings.size(); j++) {
				HashSet<String> tm = meanings.get(j).getTypeList();
				if (tm != null && typeList.containsAll(tm)) err += "�� ��������� ��� ��� �������� "
						+ (j + 1) + "[" + tm + "]" + OALD.BR;
			}
			if (!Utils.isBlank(err)) {
				ret = false;
				setErr("��� ����� [" + typeList + "]" + OALD.BR + err);
			}
		}
		*/
		return ret;
	}

	/**
	 * ������ ���� �����: ����� + ��������
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
	 * ������ ������ ���������� ����� ��� ����� + ��������.
	 * [C] � [] ������������ ��� [C]
	 * @return
	 * @throws IOException
	 */
	public HashSet<String> getDistinctTypes() throws IOException {
		if (distinctTypes == null) {
			distinctTypes = new HashSet<String>();
			if (typeList != null) {
				distinctTypes.addAll(typeList);
			}
			for (int j = 0; j < meanings.size(); j++) {
				HashSet<String> tl = meanings.get(j).getTypeList();
				if (tl != null) distinctTypes.addAll(tl);
			}
			if (isBlank(distinctTypes)) distinctTypes.add(TYPEC);
			/*
			if (distinctTypes.contains(TYPEBLANK)) {
				distinctTypes.remove(TYPEBLANK);
				distinctTypes.add(TYPEC);
			}
			*/
		}
		//System.out.println("getDistinctTypes()=" + types);
		return distinctTypes;
	}

	/**
	 * �� ����������� ��������� �����:
	 *  - �� 2 � ����� ���� (��� THE �� 3 � ����� ����)
	 *  - ���� �����
	 *  - ������ ��������� �����
	 *  - ������������ (D.O.B abbr.)
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

	public void setTypeList(HashSet<String> tList) {
		typeList = tList;
	}

	public boolean isThe() {
		return isThe;
	}

	public void setThe(boolean isThe) {
		this.isThe = isThe;
	}

	/**
	 * ����������� <h-g> 
	 * 
	 * @param entry
	 * @return nounPos, ���� <0, �� ��� �� noun
	 * @throws IOException
	 */
	public void analyzeEntry(Element entry) throws IOException {
		OALD.display("analyzeEntry()");
		Element hg = entry.getFirstChildElement("h-g");
		//Elements hgChildren = hg.getChildElements(); //����� ������
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

			finalize();
		}
		OALD.display("end analyzeEntry(): " + printPosList);
	}

	/**
	 * adj � ������� ���� �������� noun:
	 * the abobe
	 * absurd � adj. 2 noun p. 00149
	 * the blind adj, noun p.150 03840
	 * the deaf adj p.391 09447
	 * the poor
	 * the rich
	 * 
	 * ����� � adj, noun (blind) --> other
	 * ����� � adj       (deaf)  --> adj
	 *  
	 * @param hg
	 * @return
	 * @throws IOException
	 */
	void analyseHG(Element hg) throws IOException {
		Elements hgChildren = hg.getChildElements(); //����� ������
		posList = findPosList(hg.getChildElements("z"));
		setTypeList(findTypeList(hgChildren));
		//if (Entry.isBlank(typeList)) typeList.add(Entry.TYPEBLANK);

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

		Element h = hg.getFirstChildElement("h"); //�����
		OALD.printElement(2, h, null);
		setWord(the + h.getValue());

		OALD.display("end analyseHG() type [" + getTypeList() + "]");
	}

	/**
	 * ��������� ��������
	 * 
	 * @param entry
	 * @param hg
	 * @param pos � ����� ����� ���� (noun, adj) ���� NOUN
	 * @return true - ������  NOUN � pos
	 *         false - �� ������
	 * @throws IOException
	 */
	public boolean analyzePG(Element entry, Element hg, String pos)
			throws IOException {
		OALD.display("analyzePG(" + pos + ")");
		boolean posPos = false;
		Elements ngElements = null; //������ ��������� <n-g>

		//1. ���� pos
		Element pg = entry.getFirstChildElement("p-g");
		if (pg != null) { //���� ���� pg ������ �������� pos
			OALD.display("posPos.2 '" + pos + "' p-g not null");
			Elements entryChildren = entry.getChildElements("p-g"); //����� ������ p-g
			for (int i = 0; i < entryChildren.size(); i++) { //���� pos
				//OALD.display("i=" + i);
				Element el = entryChildren.get(i);
				if (el.getQualifiedName() == "p-g") {
					OALD.printElement(1, el, "<p-g> i=" + i);
					//���� pos
					Properties pgPosList = findPosList(el.getChildElements("z"));
					if (pgPosList.get(pos) != null) {
						ngElements = el.getChildElements("n-g"); //����� ������ ��������
						OALD.display("posPos.3 '" + pos + "'=" + posPos);
						break;
					}
				}
			}
			if (ngElements == null) { // && POSNOUN.equalsIgnoreCase(pos)) {
				setErr("�� ������� �������� ���� '" + pos + "'");
				OALD.writeEntry(this, Entry.TYPEERR);
				return posPos;
			}
		} else { //���� ��� pg ������ ���� pos
			OALD.display("posPos.2 '" + pos + "' p-g null");
			if (posList.get(pos) != null) {
				ngElements = hg.getChildElements("n-g"); //����� ������ ��������
			}
		}
		//2. ��������� �������� <n-g>
		if (ngElements != null) {
			OALD.printChildren(2, ngElements, "ngElements");
			if (getTypeList() == null) setTypeList(Entry.findTypeList(ngElements));
			OALD.display("  entry type=" + getTypeList());

			for (int i = 0; i < ngElements.size(); i++) {
				Element el = ngElements.get(i);
				if (el.getQualifiedName() == "n-g") {
					OALD.printElement(2, el, "   <n-g> i=" + i);
					if (POSNOUN.equals(pos)) {
						addMeaning(el, pos); //noun
						posPos = true;
					} else { //adj
						//���� noun
						Properties ngPosList = findPosList(el.getChildElements("z"));
						if (ngPosList.get(POSNOUN) != null) {
							addMeaning(el, pos); //noun
							posPos = true;
						}
					}
					//System.out.println("i=" + i + "=" + el.getValue());
					//printChildren(2, el, null);
				}
			}
		}
		OALD.display("end analyzePG(" + pos + ")=" + posPos);
		return posPos;
	}

	public Properties getPosList() {
		return posList;
	}
}
