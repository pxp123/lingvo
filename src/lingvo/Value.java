/**
 * 
 */
package lingvo;

/**
 * ����� ��� �������� ����� ���� ��������.
 * ��������: �������������� xap.: [C, U, pl., ...]
 * 
 */
public class Value {
	/** �������� */
	String value = null; // "";

	/**
	 * ���� true ����������, ���� false ���.
	 * ��������� ��� ���� ��� ��� ������ ������
	 * ��������: false ��� ��� ���� ��� ������ ��� �������������� xap. (entry00142.xml, abstract, type[])
	 */
	private boolean isVisible = false;

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

	public String toStringDisplay() {
		return (isVisible && value != null) ? value : "";
	}

	public String toString() {
		if (OALD.DEBUG)
			return value + ":" + isVisible;
		return toStringDisplay();
	}
}
