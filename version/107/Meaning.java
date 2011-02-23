/**
 * 
 */
package lingvo;

import java.io.IOException;
import java.util.HashSet;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * Одно значение слово и его параметры
 * 
 * @author администратор
 * @since 09.03.08
 */
public class Meaning {

	String num = "";

	String altWord = ""; //альтернативное имя

	String link = "";

	String mean = "";

	String example = "";

	/**часть речи слова*/
	String entryPos = "";

	/**часть речи значения*/
	String pos = "";

	/**разряд*/
	String category = "";

	boolean isThe = false;

	/**грамматический тип [x,y] конкретного значения*/
	//String type = "";
	/**список грамматических типов*/
	HashSet<String> typeList;

	/**
	 *     2.<n-g> 
	 *       3.<alt>  - альтернативное слово
	 *       3.<ngnum> - номер значения
	 *       3.<z> - часть речи значения
	 *          <pos>noun</pos>
	 *         </z> 
	 *       3.<span class="label">  - type
	 *       3.<d>     - значение
	 *       3.<span class="exa">  - пример
	 *       3.<p:xr xt="eq"> - ссылка на entry
	             <p:sc>
	               <p:xh type="dic" entry="05903">ceiling rose</p:xh>
	             </p:sc>
	          </p:xr>
	 * @throws IOException 
	 *       
	 */
	public Meaning(Element ng, String entryPos) throws IOException {
		//OALD.display("ng=" + ng.getValue());
		Elements ngElements = ng.getChildElements();
		typeList = Entry.findTypeList(ngElements);
		//if (Entry.isBlank(typeList)) typeList.add(Entry.TYPEBLANK);
		this.entryPos = entryPos;
		for (int i = 0; i < ngElements.size(); i++) {
			Element el = ngElements.get(i);
			String qn = el.getQualifiedName().trim();
			if (qn.equals("ngnum")) num = el.getValue().trim();
			else if (qn.equals("z")) { //classified 06780 p.270
				Element epos = el.getFirstChildElement("pos");
				if (epos != null) {
					pos = epos.getValue();
					if (pos.equalsIgnoreCase("usually before noun")
							|| pos.equalsIgnoreCase("only before noun")) pos = "";
				}
			} else if (qn.equals("alt")) {
				altWord = el.getValue().trim();
				if (altWord.toUpperCase().startsWith(Entry.THETHE + " ")) {
					isThe = true;
					OALD.writeLog("the.12=" + altWord);
				}
			} else if (qn.equals("d")) mean = el.getValue().trim();
			else if (qn.equals("xr")) {
				Attribute att = el.getAttribute("xt");
				if (att != null && att.getValue().equals("eq")) link = "= "
						+ el.getValue().trim();
			} else if (qn.equals("span")) {
				for (int j = 0; j < el.getAttributeCount(); j++) {
					Attribute att = el.getAttribute(j);
					String attQN = att.getQualifiedName().trim();
					String attVal = att.getValue().trim();
					//display(prefix + "  " + j + " " + att.getQualifiedName() + "="
					//		+ att.getValue() + "=");
					if (attQN.equals("class")) {
						if (attVal.equals("exa")) {
							example = el.getValue().substring(1).trim()
									.replace('\u00c7', '*'); //'\u25CA'); //romb, ?=199=c7h
						}
					}
				}
			}
		}
		//OALD.display("m1=" + toString());
	}

	public String getMean() {
		String ret = "";
		if (!Utils.isBlank(altWord)) ret += "<strong>" + altWord + "</strong>.";
		if (!Entry.isBlank(typeList)) ret += " " + typeList;
		if (!Utils.isBlank(link)) ret += " " + link.toUpperCase();
		ret += " " + mean;
		return ret;
	}

	public String toString() {
		String row = "#" + num + ",  the: " + isThe + ", entryPos: " + entryPos
				+ ", pos: " + pos + ", " + getMean();
		if (!Utils.isBlank(example)) row += " Ex: " + example;
		return row;
	}

	/*
	static String getType(String row) {
		String typ = "";
		int pos1 = row.indexOf("[");
		if (pos1 != -1) {
			int pos2 = row.indexOf("]");
			typ = row.substring(pos1 + 1, pos2);
		}
		return typ;
	}
	*/

	/*
	* получить список типов
	*/
	/*
	static HashSet<String> getTypes(String row) {
		HashSet<String> set = new HashSet<String>();
		if (!Utils.isBlank(row)) {
			StringTokenizer st = new StringTokenizer(row, ",");
			while (st.hasMoreTokens()) {
				String t2 = st.nextToken();
				set.add(t2.trim().toUpperCase());
			}
		}
		OALD.display("  getTypes(" + row + ")=" + set);
		return set;
	}
	*/

	public HashSet<String> getTypeList() {
		return typeList;
	}

	public String getExample() {
		return example;
	}

	public String getCategory() {
		return category;
	}

	public boolean isThe() {
		return isThe;
	}

	public void setThe(boolean isThe) {
		this.isThe = isThe;
	}

	public void setMean(String mean) {
		this.mean = mean;
	}

	public String getNum() {
		return num;
	}

	public String getPos() {
		return pos;
	}

	public void setTypeList(HashSet<String> tList) {
		typeList = tList;
	}
}
