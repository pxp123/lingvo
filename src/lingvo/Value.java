/**
 * 
 */
package lingvo;

/**
 * Класс для хранения каких либо значений.
 * Например: Грамматический тип:  [C, U, pl., ...]
 *
 */
public class Value {
	/**Значение*/
	String value = null; //"";

	/**если true показывать, если false нет. 
	 * актуально для слов нет пометы вообще*/
	boolean isVisible = false;

	public Value(String v, boolean isV) {
		value = v;
		isVisible = isV;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String v) {
		this.value = v;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public String toString() {
		return (isVisible && value != null) ? value : "";

	}
}
