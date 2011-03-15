/**
 * 
 */
package lingvo;

import java.io.IOException;
import java.util.Vector;

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

	String altWord = ""; // альтернативное имя

	String link = "";

	String mean = "";

	String example = "";

	/** часть речи значения */
	String pos = "";
	
	/**
	 * список ссылок на другие слова
	 * entry01116.xml, am, gr[C:false], pos{}, xr[]
	 * type="dic" entry="00060">-able</xh>
	 * <h-g> <xr>
	 */
	//private Vector<String> xrList = new Vector<String>();

	/** разряд */
	String category = "";

	boolean isThe = false;

	/** список грамматических групп конкретного значения */
	Vector<Value> groupList = new Vector<Value>();

	/**
	 * 2.<n-g>
	 * 3.<alt> - альтернативное слово
	 * 3.<ngnum> - номер значения
	 * 3.<z> - часть речи значения <pos>noun</pos> </z>
	 * 3.<span class="label"> - группа
	 * 3.<d> - значение
	 * 3.<span class="exa"> - пример
	 * 3.
	 * <p:xr xt="eq">
	 * - ссылка на entry
	 * <p:sc>
	 * <p:xh type="dic" entry="05903">
	 * ceiling rose
	 * </p:xh>
	 * </p:sc> </p:xr>
	 * 
	 * @throws IOException
	 * 
	 */
	public Meaning(Element ng) throws IOException {
		OALD.display("Meaning(ng=" + ng.getValue() + ")");
		groupList = Entry.findGroupList(ng.getChildElements("span"));
		//xrList = Entry.findXRList(ng.getChildElements("xr"));
		Elements ngChildren = ng.getChildElements();
		for (int i = 0; i < ngChildren.size(); i++) {
			Element el = ngChildren.get(i);
			String qn = el.getQualifiedName().trim();
			if (qn.equals("ngnum")) {
				num = el.getValue().trim();
			} else if (qn.equals("z")) { // classified 06780 p.270
				Element epos = el.getFirstChildElement("pos");
				if (epos != null) {
					pos = epos.getValue();
				}
			} else if (qn.equals("alt")) {
				altWord = el.getValue().trim();
				if (altWord.toUpperCase().startsWith(Entry.THETHE + " ")) {
					isThe = true;
					OALD.writeLog("the.12=" + altWord);
				}
			} else if (qn.equals("d"))
				mean = el.getValue().trim();
			else if (qn.equals("xr")) {
				Attribute att = el.getAttribute("xt");
				if (att != null && att.getValue().equals("eq"))
					link = "= " + el.getValue().trim();
			} else if (qn.equals("span")) {
				for (int j = 0; j < el.getAttributeCount(); j++) {
					Attribute att = el.getAttribute(j);
					String attQN = att.getQualifiedName().trim();
					String attVal = att.getValue().trim();
					// display(prefix + "  " + j + " " + att.getQualifiedName()
					// + "="
					// + att.getValue() + "=");
					if (attQN.equals("class")) {
						if (attVal.equals("exa")) {
							example = el.getValue().substring(1).trim()
									.replace('\u00c7', '*'); // '\u25CA');
																// //romb,
																// ?=199=c7h
						}
					}
				}
			}
		}
		OALD.display("end Meaning(ng=" + ng + ")=" + toString());
	}

	public String getMean() {
		String ret = "";
		if (!Utils.isBlank(altWord))
			ret += "<strong>" + altWord + "</strong>.";
		if (!Utils.isBlank(link))
			ret += " " + link.toUpperCase();
		ret += " " + mean;
		return ret;
	}

	public String toString() {
		String row = "#" + num + ",  the: " + isThe + ", pos: " + pos
				+ ", gr" + groupList + ", " + getMean();
		if (!Utils.isBlank(example))
			row += " Ex: " + example;
		return row;
	}

	public Vector<Value> getGroupList() {
		return groupList;
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

	public void setGroupList(Vector<Value> tList) {
		groupList = tList;
	}
}
