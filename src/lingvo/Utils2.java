/**
 * 
 */
package lingvo;

import java.util.Vector;

/**
 * @author Администратор
 *
 */
public class Utils2 {

	public static String vector2String(Vector<Value> typeList) {
		String ret = "";
		for (Value value : typeList) {
			ret += ", " + value.getValue() + "." + value.isVisible();
		}
		if (ret.length() > 0) ret = ret.substring(2);
		ret = "[" + ret + "]";
		return ret;
	}
}
