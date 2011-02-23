/*
 *   Utils
 *
 */
package lingvo;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.JarURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;

/**
 *  Набор сервистных функций.
 *
 *  @author      <a href="mailto:movsikov@uib.cherkassy.net">Vladyslav Movsikov</a>
 *  @version     1.01
 *  @since       ZOO 4.0
*/
public final class Utils {

	/** Курсор в форме часов. */
	public static final Cursor WAIT_CURSOR = Cursor
			.getPredefinedCursor(Cursor.WAIT_CURSOR);

	/** Курсор по умолчанию. */
	public static final Cursor DEFAULT_CURSOR = Cursor
			.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

	private static int DEFAULT_WIDTH = 80;

	/** Определяет путь к каталогу с картинками;
	    Все картинки должны быть в каталоге /images/. */
	public static final String IMAGE_DIR = "/zoo/images/";

	/** Иконки из IMAGE_DIR */
	private static Hashtable<String, ImageIcon> icons = new Hashtable<String, ImageIcon>(
			100);

	private static Calendar cal = Calendar.getInstance();

	private static Comparator alphaComparator;

	private static NumberFormat frm2 = new DecimalFormat("##");

	private static NumberFormat frm3 = new DecimalFormat("###");

	/** Форматирует дату к SQL виду: {d 'yyyy-MM-dd'}. */
	//private static SimpleDateFormat dSQLFormat = new SimpleDateFormat("{'d' ''yyyy-MM-dd''}");
	//private static SimpleDateFormat dSQLFormat = new SimpleDateFormat("dd-MMM-yy", Locale.US);
	private static SimpleDateFormat dSQLFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	/** Форматирует дату к виду: dd.MM.yy */
	private static SimpleDateFormat dShortFormat = new SimpleDateFormat(
			"dd.MM.yy");

	/** Форматирует дату к SQL виду: dd.MM.yyyy */
	private static SimpleDateFormat dFormat = new SimpleDateFormat("dd.MM.yyyy");

	/** Форматирует дату к виду: dd.MM.yyyy HH:mm:ss */
	private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss");

	/** 01.01.1900 для проверки: Clarion пишет 0002 год в обязательные поля. */
	public static final java.util.Date YEAR1900 = new java.sql.Date(
			-2208996000000L);

	private static Hashtable actions;

	static {
		frm2.setMaximumIntegerDigits(2);
		frm2.setMinimumIntegerDigits(2);
	}

	/**
	 *  Все методы статические.
	 */
	private Utils() {
	}

	/**
	 *  Проверяет пустая ли это строка. Строка считается пустой если:
	 *  <br>- null;
	 *  <br>- в строке нет ни одного символа;
	 *  <br>- строка заполнена только символами с кодами от 0 до 32(пробел),
	 *  т.е. whitespaces;
	 *
	 *  @param  str строка
	 *  @return true - если строка пустая
	 */
	public static boolean isBlank(String str) {
		if (str == null || str.trim().length() == 0) return true;
		return false;
	}

	/**
	 *  Проверяет пустая ли это строка.
	 *  Если строка пустая возвращается оригинальная строка.
	 *  Если строка НЕ пустая возвращается str.trim().
	 *  @param  str строка
	 *  @return строка
	 */
	public static String trim(String str) {
		if (!isBlank(str)) return str.trim();
		return str;
	}

	/**
	 *  Возвращает o == null? null : o.toString();
	 */
	public static String toString(Object o) {
		return (o == null ? null : o.toString());
	}

	/**
	 *  Показывает заданное PopupMenu с учетом координат объекта.
	 *  Стандартный метод не учитывает границы окна.
	 *
	 *  @param  popup menu
	 *  @param  comp The component to show it for
	 *  @param  x x координата курсора относительно объекта.
	 *  @param  y y координата курсора относительно объекта.
	 */
	public static void showPopupMenu(JPopupMenu popup, Component comp, int x,
			int y) {
		if (popup == null) return;
		MenuElement[] els = popup.getSubElements();
		if (els.length < 1) return;

		int origX = x;
		int offset = 40; // for windows taskbar
		Dimension size = popup.getPreferredSize();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Point p = new Point(x, y);
		SwingUtilities.convertPointToScreen(p, comp);
		boolean horiz = false;
		boolean vert = false;

		if (p.x + size.width > screen.width && size.width < screen.width) {
			x += (screen.width - p.x - size.width);
			horiz = true;
		}

		if (p.y + size.height + offset > screen.height
				&& size.height < screen.height) {
			y += (screen.height - p.y - size.height - offset);
			vert = true;
		}

		// If popup needed to be moved both horizontally and
		// vertically, the mouse pointer might end up over a
		// menu item, which will be invoked when the mouse is
		// released. This is bad, so move popup to a different
		// location.
		if (horiz && vert) x = origX - size.width - 2;

		popup.show(comp, x, y);
	}

	/**
	 *  Returns the Icon associated with the name from the resources.
	 *  The resouce should be in the path.
	 *  @param name Name of the icon file i.e., help16.gif
	 *  @return the name of the image or null if the icon is not found.
	 */
	public static ImageIcon getIcon(String name) {
		ImageIcon im = icons.get(name);
		if (im == null) {
			String imagePath = IMAGE_DIR + name;
			URL url = new Object().getClass().getResource(imagePath);
			if (url != null) im = new ImageIcon(url);
			if (im != null) icons.put(name, im);
		}
		return im;
	}

	/**
	 *  Пытается установить фокус на компонент comp; если comp на закладке
	 *  JTabbedPane то переключается на нужную закладку.
	 */
	public static boolean setFocusTo(Component comp) {
		boolean b = comp.requestFocusInWindow();
		if (b) return b;

		// компонент может быть на закладке JTabbedPane.
		Component c = comp;
		while (c != null) {
			Component parent = c.getParent();
			if (parent instanceof JTabbedPane) {
				int i = ((JTabbedPane) parent).indexOfComponent(c);
				if (i >= 0) {
					((JTabbedPane) parent).setSelectedIndex(i);
					return comp.requestFocusInWindow();
				}
			}
			c = c.getParent();
		}
		return false;
	}

	/**
	 *  Сравнение двух объектов.
	 *  Результат: o1 == null? o2 == null : o1.equals(o2)
	 */
	public static boolean equals(Object o1, Object o2) {
		return (o1 == null ? o2 == null : o1.equals(o2));
	}

	/**
	 *  Возвращает Comparator для строк без учета регистра.
	 *  Оба аргумента могут быть null. В этом случае они считаются равными.
	 *  <br>Сравниваются строковые представления объектов.
	 */
	public static Comparator getAlphabeticComparator() {
		if (alphaComparator == null) alphaComparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1 = String.valueOf(o1);
				String s2 = String.valueOf(o2);
				return s1.toLowerCase().compareTo(s2.toLowerCase());
			}
		};
		return alphaComparator;
	}

	/**
	 *  Разбивает строку на несколько строк заданной длины.
	 *  Обрезает слишком длинные строки.
	 *  <br>Используется для вывода сообщений на экран.
	 *
	 *  @param  mes строка.
	 */
	public static String splitLine(String mes) {
		return splitLine(mes, DEFAULT_WIDTH);
	}

	/**
	 *  Разбивает строку на несколько строк заданной длины.
	 *  Обрезает слишком длинные строки.
	 *  <br>Используется для вывода сообщений на экран.
	 *
	 *  @param  mes строка.
	 *  @param  maxLen - максимальная длина строки.
	 */
	public static String splitLine(String mes, int maxLen) {
		if (mes == null || mes.length() <= maxLen) return mes;

		String res = "";

		// сохранить оригинальную разбивку на строки
		StringTokenizer lineTokenizer = new StringTokenizer(mes, "\n", true);
		while (lineTokenizer.hasMoreTokens()) {
			//if (res.length() > 0) res += "\n";
			String token = lineTokenizer.nextToken();
			//Log.debugS("line: =" + token + "=");
			if (token.length() > maxLen) {

				// разбить на слова
				String line = "";
				StringTokenizer spaceTokenizer = new StringTokenizer(token, " ", false);
				for (String word = ""; spaceTokenizer.hasMoreTokens();) {
					word = spaceTokenizer.nextToken();
					//Log.debugS("word: =" + word + "=  line: = " + line + "=");
					if (line.length() + word.length() <= maxLen) {
						if (line.length() > 0) line += " ";
						line += word;

					} else {
						res += line;
						if (word.length() <= maxLen) {
							line = "\n" + word;

						} else {
							line = "";
							while (word.length() > maxLen) {
								line += "\n" + word.substring(0, maxLen);
								word = word.substring(maxLen);
							}
							line += "\n" + word;
						}
					}
				}
				res += line;

			} else res += token;
		}
		return res;
	}

	/**
	 * Форматирование числа. На выходе строка размера max, дополненная
	 * (если необходимо)справа указанным символом.
	 *  @param   number число для форматирования
	 *  @param   filler символ заполнитель
	 *  @param   max длина выходной строки
	 *  @return  строка длины max
	 */
	public static String format(int number, char filler, int max) {
		return format("" + number, filler, max);
	}

	/**
	 * Форматирование строки. На выходе строка размера max, дополненная
	 * (если необходимо)справа указанным символом.
	 *  @param   str строка для форматирования
	 *  @param   filler символ заполнитель
	 *  @param   max длина выходной строки
	 *  @return  строка длины max
	 */
	public static String format(String str, char filler, int max) {
		if (str == null) str = "";
		int len = str.length();
		String fmt = null;
		if (len < max) {
			fmt = str;
			for (int i = len; i < max; i++)
				fmt += filler;
		} else if (len > max) fmt = str.substring(0, max);
		else fmt = str;
		return fmt;
	}

	/**
	 * Форматирование строки. На выходе строка размера max, дополненная
	 * (если необходимо)слева указанным символом.
	 *  @param   filler символ заполнитель
	 *  @param   number число для форматирования
	 *  @param   max длина выходной строки
	 *  @return  строка длины max
	 */
	public static String format(char filler, int number, int max) {
		return format(filler, "" + number, max);
	}

	/**
	 * Форматирование строки. На выходе строка размера max, дополненная
	 * (если необходимо)слева указанным символом.
	 *  @param   filler символ заполнитель
	 *  @param   str строка для форматирования
	 *  @param   max длина выходной строки
	 *  @return  строка длины max
	 */
	public static String format(char filler, String str, int max) {
		if (str == null) str = "";
		int len = str.length();
		String fmt = null;
		if (len < max) {
			fmt = str;
			for (int i = len; i < max; i++)
				fmt = filler + fmt;

		} else if (len > max) fmt = str.substring(0, max);
		else fmt = str;
		return fmt;
	}

	/**
	 *  Форматирует дату к виду "дд.MM.гггг";
	 *  возвращает пустую строку если аргумент d is null.
	 */
	public static String format(Date d) {
		if (d == null) return "";
		return dFormat.format(d);
	}

	/**
	 *  Форматирует дату к виду "дд.MM.гггг ЧЧ:мм:сс";
	 *  возвращает пустую строку если аргумент d is null.
	 */
	public static String formatDateTime(Date d) {
		if (d == null) return "";
		return dateTimeFormat.format(d);
	}

	/**
	 *  Форматирует дату к виду "дд.MM.гг";
	 *  возвращает пустую строку если аргумент d is null.
	 */
	public static String formatShort(Date d) {
		if (d == null) return "";
		return dShortFormat.format(d);
	}

	/**
	 *  Форматирует дату к виду, пригодному для использования
	 *  в SQL запросах: "to_date("2005-01-23", "YYYY-MM-DD")";
	 *  аргумент d должен быть не null.
	 */
	public static String formatSQL(Date d) {
		//if (d == null) return "";
		return "to_date('" + dSQLFormat.format(d) + "', 'YYYY-MM-DD')";
	}

	/**
	 *  Форматирование промежутка времени к формату ЧЧ:ММ:СС.mmm;
	 *  аргумент time должен содержать только время (кол-во миллисекунд);
	 *  Обычное время этим методом форматировать нельзя, так как
	 *  время хранится в формате UTC.
	 */
	public static String formatTime(long time) {
		long t = time / 1000;
		long hour = t / 3600;
		long min = (t % 3600) / 60;
		long sec = t % 60;
		long ms = time - t - hour - min - sec;
		return frm2.format(hour) + ":" + frm2.format(min) + ":" + frm2.format(sec)
				+ "." + frm3.format(ms);
	}

	/**
	 *  Форматирование промежутка времени к формату ЧЧ:ММ:СС;
	 *  аргумент time должен содержать только время (кол-во миллисекунд);
	 *  Обычное время этим методом форматировать нельзя, так как
	 *  время хранится в формате UTC.
	 */
	public static String formatTime2Sec(long time) {
		long t = time / 1000;
		long hour = t / 3600;
		long min = (t % 3600) / 60;
		long sec = t % 60;
		return frm2.format(hour) + ":" + frm2.format(min) + ":" + frm2.format(sec);
	}

	//    /**
	//     *  Форматирование времени к формату ЧЧ:ММ:СС;
	//     *  Возвращает пустую строку если time is null;
	//     */
	//    public static String format(Time time) {
	//        if (time == null) return "";
	//        //return formatTime(time.getTime());
	//        return time.toString();
	//    }

	/**
	 *  Возвращает кол-во КАЛЕНДАРНЫХ дней между двумя датами.
	 *
	 *  @param d1 - дата начала.
	 *  @param d2 - дата конца.
	 *  @return количество дней между датами. Возвращает отрицательное число если d2 < d1.
	 *  @throws  IllegalArgumentException если одна из дат is null.
	 */
	public static long getDaysBetween(Date d1, Date d2) {
		if (d1 == null) throw new IllegalArgumentException(
				"argument d1 cannot be null!");
		if (d2 == null) throw new IllegalArgumentException(
				"argument d2 cannot be null!");

		// Convert dates to Calendar
		Calendar c1 = new GregorianCalendar();
		c1.setTime(d1);
		c1.set(Calendar.HOUR_OF_DAY, 0);
		c1.set(Calendar.MINUTE, 0);
		c1.set(Calendar.SECOND, 0);
		c1.set(Calendar.MILLISECOND, 0);
		Calendar c2 = new GregorianCalendar();
		c2.setTime(d2);
		c2.set(Calendar.HOUR_OF_DAY, 0);
		c2.set(Calendar.MINUTE, 0);
		c2.set(Calendar.SECOND, 0);
		c2.set(Calendar.MILLISECOND, 0);

		// First convert the from and to Calender to long (milli seconds)
		// MAKE SURE THE Hour, Seconds and Milli seconds are set to 0, if you
		// already have you own Claender object otherwise the time will be
		// used in the comparision, later on.
		long from = c1.getTimeInMillis();
		long to = c2.getTimeInMillis();

		// Next subtract the from date from the to date (make sure the
		// result is a double, this is needed in case of Winter and Summer
		// Time (changing the clock one hour ahead or back) the result will
		// then be not exactly rounded on days. If you use long, this slighty
		// different result will be lost.
		double difference = to - from;
		//long diff = to - from;
		//System.out.println("double: " + difference + "  res: " + difference/(1000*60*60*24));
		//System.out.println("  long: " + diff + "  res: " + diff/(1000*60*60*24));

		// Next divide the difference by the number of milliseconds in a day
		// (1000 * 60 * 60 * 24). Next round the result, this is needed of the
		// Summer and Winter time. If the period is 5 days and the change from
		// Winter to Summer time is in the period the result will be
		// 5.041666666666667 instead of 5 because of the extra hour. The
		// same will happen from Winter to Summer time, the result will be
		// 4.958333333333333 instead of 5 because of the missing hour. The
		// round method will round both to 5 and everything is OKE....
		return Math.round((difference / (1000 * 60 * 60 * 24)));
	}

	/**
	 *  Прибавляет days дней к заданной дате. Отнимает, если days отрицательное.
	 *  Возвращает null если аргумент d is null.
	 */
	public static java.sql.Date addDays(Date d, int days) {
		if (d == null) return null;
		cal.setTime(d);
		cal.add(Calendar.DATE, days);
		return new java.sql.Date(cal.getTimeInMillis());
	}

	/**
	 *  Добавляет один год и отнимает один день от заданной даты.
	 *  Используется для задания периода в формах ввода.
	 *  Возвращает null если аргумент d is null.
	 */
	public static java.sql.Date addYear(Date d) {
		if (d == null) return null;
		cal.setTime(d);
		cal.add(Calendar.YEAR, 1);
		cal.add(Calendar.DATE, -1);
		return new java.sql.Date(cal.getTimeInMillis());
	}

	//    /**
	//     *  Возвращает время, обрезая дату.
	//     */
	//    public static java.sql.Time getTime(java.util.Date date) {
	//        if (date == null) return null;
	//
	//        //время без даты
	//        Calendar c1 = Calendar.getInstance();
	//        c1.clear();
	//        Calendar c2 = Calendar.getInstance();
	//        c2.setTime(date);
	//        c1.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
	//        c1.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
	//        c1.set(Calendar.SECOND, c2.get(Calendar.SECOND));
	//        c1.set(Calendar.MILLISECOND, c2.get(Calendar.MILLISECOND));
	//        return new java.sql.Time(c1.getTimeInMillis());
	//    }
	//
	/**
	 *  Соединяет дату и время в один объект.
	 *
	 *  @param   date дата
	 *  @param   time время
	 */
	public static java.sql.Timestamp getTime(java.util.Date date,
			java.util.Date time) {
		if (date == null && time != null) return new java.sql.Timestamp(time
				.getTime());
		if (time == null && date != null) return new java.sql.Timestamp(date
				.getTime());

		Calendar c1 = Calendar.getInstance();
		c1.setTime(date);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(time);

		c1.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
		c1.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
		c1.set(Calendar.SECOND, c2.get(Calendar.SECOND));
		c1.set(Calendar.MILLISECOND, c2.get(Calendar.MILLISECOND));

		return new java.sql.Timestamp(c1.getTimeInMillis());
	}

	/**
	 *  Возвращает название текущего потока и stackTrace для
	 *  аргумента <b>е</b> в виде строки;
	 *  возвращает пустую строку если аргумента <b>е</b> is null.
	 *
	 *  @return строку вида:
	 *  <pre>
	 *   Поток: AWT-EventQueue-0
	 *   Опис помилки:
	 *   dori.jasper.engine.JRException: Помилка читання файлу: '/reports/deposit.jasper'
	 *        at dog.ChargeDepReporter.fillReport(ChargeDepReporter.java:258)
	 *        at dog.Dog.charge(Dog.java:144)
	 *        at dog.Dog.access$200(Dog.java:36)
	 *        at dog.Dog$DogTree.actionPerformed(Dog.java:236)
	 *        at zoo.ui.ZOOTree$3.mouseClicked(ZOOTree.java:134)
	 *        ...
	 *  </pre>
	 */
	public static String stackTrace(Exception e) {
		if (e == null) return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		return "\nПомилка: " + sw.toString() + "\nПоток: "
				+ Thread.currentThread().getName();
	}

	/**
	 *  Возвращает атрибуты jar файла из файла META-INF/MANIFEST.MF,
	 *  или null в случае ошибки.
	 *
	 *  @param jarURL URL jar файла.
	 */
	public static Attributes getJarAttributes(URL jarURL) {
		try {
			if (jarURL == null) return null;
			JarURLConnection jurConn = (JarURLConnection) jarURL.openConnection();
			Manifest mf = jurConn.getManifest();
			return mf.getMainAttributes();

		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Клас опису дії користувача, об'єкти якого є елементами таблиці actions
	 * <p>@author <a href="mailto:churchyn@uib.cherkassy.net">Andriy Churchyn</a></p>
	 */
	public class ActionRecord {
		public String actionID;

		public String actionName;

		public String actionDescr;

		public String className;

		public String methodName;

		public ActionRecord(String actionID, String actionName, String actionDescr,
				String className, String methodName) {
			this.actionID = actionID;
			this.actionName = actionName;
			this.actionDescr = actionDescr;
			this.className = className;
			this.methodName = methodName;
		}
	}

	/**
	* Разбирает строку параметров вида <парам1> = <знач1>; <парам2> = <знач2>; ...
	*
	* @param params - исходная строка параметров
	* @return Properties - содержит разобранные параметры
	* author <a href="mailto:churchyn@uib.cherkassy.net">Andriy Churchyn</a>
	*/
	public static Properties parsePropertiesLine(String params) {
		Properties properties = new Properties();
		if (params == null) return properties;
		StringTokenizer tkn = new StringTokenizer(params, ";");
		while (tkn.hasMoreTokens()) {
			String line = tkn.nextToken();
			int i = line.indexOf('=');
			if (i > 0) {
				properties.put(line.substring(0, i).trim().toUpperCase(), line
						.substring(i + 1).trim());
			}
		}
		return properties;
	}

	/**
	 * Возвращает объект определенного типа по строковому представлению значения
	 * @param strValue строковое представление значения
	 * @return Object типы возвращаемых объектов: String, Integer, BigDecimal, Date
	 */
	public static Object getParsedObject(String strValue) {
		try { // Integer
			return new Integer(strValue);
		} catch (Exception e) {
		}
		;

		try { // BigDecimal
			return new BigDecimal(strValue);
		} catch (Exception e) {
		}
		;

		try { // Date DD.MM.YYYY
			return new SimpleDateFormat("dd.MM.yyyy").parse(strValue);
		} catch (Exception e) {
		}
		;

		return strValue;
	}

	/**
	 * Возвращает объект определенного типа по строковому представлению значения
	 * @param strValue строковое представление значения
	 * @param sqlType код типа данных (SQL Data Type)
	 * @return Object типы возвращаемых объектов: String, Integer, BigDecimal, Date
	 */
	public static Object getParsedObject(String strValue, int sqlType) {
		try { // Integer
			switch (sqlType) {
			//            case 1:
			//                return new Integer(strValue);
			case 10:
				return new BigDecimal(strValue.replace(',', '.'));
			case 13:
				return new SimpleDateFormat("dd.MM.yyyy").parse(strValue);
			}
		} catch (Exception e) {
		}
		;
		return strValue;
	}

	/**
	 *  Конвертирует строку из шестнадцатиричных чисел в массив байт;
	 *  Предполагается что каждое число в строке состоит из двух символов: FF;
	 */
	public static byte[] toByte(String s) {
		int len = s.length() / 2;
		byte[] bb = new byte[len];
		int j = 0;
		for (int i = 0; i < s.length(); i += 2) {
			String ss = s.substring(i, i + 2);
			BigInteger bi = new BigInteger(ss, 16);
			byte b = bi.byteValue();
			//byte b = Byte.parseByte(ss, 16); // так делать нельзя!!!
			bb[j] = b;
			j++;
		}
		return bb;
	}

	/**
	 * Обрезает время до минуты
	 */
	public static long stripTimeToMinute(long time) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(time));
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	/** for debug purposes only */
	public static void main(String[] args) {
		//        Object o=null;
		//        o=Utils.getParsedObject("123456");
		//        o=Utils.getParsedObject("123.456");
		//        o=Utils.getParsedObject("123,456");
		//        o=Utils.getParsedObject("01.02.2006");
		//        o=Utils.getParsedObject("djhjd 277294");

		// test splitLine()
		//String s = "123 12123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n3 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n123 123 123 123 123 123 123 123 123 123 123 123 123 123\n";
		//String s = "1234567890 1234567890 12345\n67890 123456\n7890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 ";
		//String s = "Строка 1\n\nСтрока 2\nСтрокa 3 Строка4 Строка5\nСтрока6Строка777777777777777777777777777777123lh21 3g12u3 123g 12i3g12j3g12 312yj3 u123y12u31239";
		//Log.debugS(s);
		//String res = splitLine(s, 8);
		//Log.debugS("res: " + res);
		//JOptionPane.showMessageDialog(null, splitLine(s));
		//System.out.println(ZOOUtils.addYear(new Date()));

		// проверка даты
		Date d1 = new java.util.GregorianCalendar(2003, Calendar.JANUARY, 1)
				.getTime();
		//Date d2 = new java.util.GregorianCalendar(2003, Calendar.SEPTEMBER, 1).getTime();
		System.out.println("add day:" + addDays(d1, -1));
		System.out.println("add year:" + addYear(d1));

		//JOptionPane.showMessageDialog(null, "" + getDaysBetween(d1, d2));
		System.exit(0);
	}

	public static Hashtable getActions() {
		return actions;
	}

} // end of class Utils
