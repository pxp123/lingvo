package lingvo;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

/**
 * 
 * @author администратор
 *
 *"v 1.01 06.03.08";
 */
public class OALD102 {

	public static final boolean DEBUG = true; //false;

	public static final String VERSION = "1.03"; // 06.03.08;

	public static final String WINCODEPAGE = "windows-1251";

	public static final String UTF8CODEPAGE = "UTF-8";

	public static final String UTF16CODEPAGE = "UTF-16";

	public static final String BR = "<br>\n";

	public static final String BLANK = "&nbsp;";

	public static final int MAXFILE = 500;

	static java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss");

	FileWriter fw = null;

	static FileWriter log = null;

	static String ls = System.getProperty("line.separator", "\n");

	public int cntRead = 0;

	public int cntNoun = 0;

	public int cntErr = 0;

	//public int cntSymbol = 0;

	public int cntMean = 0;

	Builder parser = new Builder();

	public static String outDir, outFile, outExt, title;

	static void openLog() throws IOException {
		log = new FileWriter(outDir + "_OALDlog.txt", true);
		writeLog(OALD102.ls + "*** Start " + VERSION + " "
				+ df.format(new java.util.Date()));
	}

	static void closeLog(String footer) throws IOException {
		if (log != null) {
			writeLog(footer + ls + "*** Stop " + VERSION + " "
					+ df.format(new java.util.Date()));
			log.close();
		}
	}

	static void writeLog(String row) throws IOException {
		display(row);
		if (log == null) openLog();
		log.write(row + ls);
		log.flush();
	}

	void openFile(String prefix) throws IOException {
		//cntSymbol = 0;
		fw = new FileWriter(outDir + outFile + "_" + prefix + ".html." + outExt,
				true);
		fw
				.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\""
						+ "\n\"http://www.w3.org/TR/REC-html40/strict.dtd\">"
						+ "\n<META http-equiv=Content-Type content=\"text/html; charset=windows-1251\">\n"
						+ "<html>\n" + "<head>\n" + "<title>" + title
						+ "</title>\n</head>\n<body>\n");
		writeText(title);
		//fw.write(getTable1());
		fw.write(getTable2());
	}

	void closeFile(String footer) throws IOException {
		if (fw != null) {
			fw.write("</table>\n");
			writeText(footer);
			writeText(getStatictic()
					+ " "
					+ new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(new java.util.Date()));
			fw.write("</body>\n</html>\n");
			fw.close();
		}
	}

	String getTable1() {
		/*
		Entry              	
		Разряд	
		Континуати 1 [U],[C],[sing],[usually sing]
		модель	
		Консолідати 2	the + слово в ед. числе (нет обозначения)
		модель	
		Мультиплікати	3	[U],[C], отсутствие грамм. пометки
		модель	
		Плюрати 4	[pl],[usually pl],[often pl],[C]
		модель	
		Парсифікат 5	
		модель	
		Конгломерати 6 the + слово во мн. числе (нет обозначения)	
		модель
		*/
		return "<table width=\"100%\" border=\"1\">" + "<tr>"
				+ " <th scope=\"col\">Файл"
				+ BR
				+ "Слово</th>"
				+ " <th scope=\"col\">Значения слова</th>"
				+ " <th scope=\"col\">Разряд</th>"
				+ " <th scope=\"col\">Континуати 1"
				+ BR
				+ "[U],[C],[sing],[usually sing]</th>"
				+ " <th scope=\"col\">модель</th>"
				+ " <th scope=\"col\">Консолідати 2"
				+ BR
				+ "the + слово в ед. числе (нет обозначения)</th>"
				+ " <th scope=\"col\">модель</th>"
				+ " <th scope=\"col\">Мультиплікати	3"
				+ BR
				+ "[U],[C], отсутствие грамм. пометки</th>"
				+ " <th scope=\"col\">модель</th>"
				+ " <th scope=\"col\">Плюрати 4"
				+ BR
				+ "[pl],[usually pl],[often pl],[C]</th>"
				+ " <th scope=\"col\">модель</th>"
				+ " <th scope=\"col\">Парсифікат 5"
				+ BR
				+ "</th>"
				+ " <th scope=\"col\">модель</th>"
				+ " <th scope=\"col\">Конгломерати 6"
				+ BR
				+ "the + слово во мн. числе (нет обозначения)</th>"
				+ " <th scope=\"col\">модель</th>" + "</tr>\n";
	}

	String getTable2() {
		return "<table width=\"100%\" cellspacing=\"0\"  cellpadding=\"0\" border=\"1\">"
				+ "<tr>"
				+ " <th width=\"5%\">Файл</th>\n"
				+ " <th width=\"5%\">Слово</th>"
				+ " <th width=\"5%\">Гр. хар.</th>"
				+ " <th width=\"5%\">Гр. хар.</th>"
				+ " <th width=\"2%\">№</th>"
				+ " <th width=\"50%\">Значення</th>"
				+ " <th width=\"25%\"><i>Приклад</i></th>"
				+ " <th width=\"3%\">Роз-ряд</th>" + "</tr>\n";
	}

	void writeEntry(Entry entry) throws IOException {
		display(entry.toStringFull());
		if (fw == null) openFile("0");
		String color = "";
		if (Utils.isBlank(entry.getErr())) {
			cntNoun++;
			String row = "<tr"
					+ color
					+ ">\n"
					+ "  <td valign=\"top\">"
					+ entry.getFileName()
					+ "</td>\n"
					+ "  <td valign=\"top\">"
					+ entry.getWord()
					+ "</td>\n"
					+ "  <td valign=\"top\">"
					+ entry.getType()
					+ "</td>\n"
					+ "<td colspan=\"5\"><table width=\"100%\" cellspacing=\"0\" border=\"1\">";
			for (int i = 0; i < entry.getMeanings().size(); i++) {
				cntMean++;
				Meaning mean = entry.getMeanings().get(i);
				String ex = "";
				if (!Utils.isBlank(mean.getExample())) ex = "<i>" + mean.getExample()
						+ "</i>";
				row += "<tr>" + " <td valign=\"top\" width=\"5.9%\">" + mean.getType()
						+ "</td>" + " <td valign=\"top\" width=\"2.3%\">" + mean.getNum()
						+ "</td>" + " <td valign=\"top\" width=\"59%\">" + mean.getMean()
						+ "</td>" + " <td valign=\"top\" width=\"29.5%\">" + ex + "</td>"
						+ " <td valign=\"top\" width=\"3.5%\">" + mean.getCategory()
						+ "</td>" + "</tr>\n";
			}
			row += "</table>\n</td>\n</tr>\n";
			fw.write(row);
		} else {
			cntErr++;
			displayErr(entry.toString() + ".2: " + entry.getErr());
			color = " bgcolor=\"#FF9933\"";//bgcolor="#FF9933"
			fw.write("<tr" + color + ">\n" + "  <td>" + entry.getFileName()
					+ "</td>\n" + "  <td>" + entry.getWord() + "</td>\n"
					+ "  <td valign=\"top\">" + entry.getType() + "</td>\n" + "  <td>"
					+ entry.getErr() + "</td>\n" + "</tr>\n");
		}
		fw.flush();
		if ((cntNoun + cntErr) % MAXFILE == 0) {
			closeFile("");
			openFile("" + (cntNoun + cntErr));
		}
	}

	void writeComment(String msg) throws IOException {
		//logErr(msg);
		fw.write("<!-- " + msg + " -->\n");
	}

	void writeText(String msg) throws IOException {
		fw.write(msg + "<br>\n");
	}

	String getStatictic() {
		return "Всего прочитано: " + cntRead + " Всего NOUN: " + cntNoun
				+ " Всего значений: " + cntMean + " Всего ошибок: " + cntErr;
	}

	static void display(String msg) {
		if (DEBUG) System.out.println(msg);
	}

	static void displayErr(String msg) throws IOException {
		//if (DEBUG) System.err.println(msg);
		System.out.println("ERR: " + msg);
		writeLog("ERR: " + msg);
	}

	static void printElement(int level, Element el, String msg) {
		if (!DEBUG) return;
		String prefix = "";
		for (int l = 0; l < level; l++) {
			prefix += ' ';
		}
		if (!Utils.isBlank(msg)) display(prefix + msg);
		if (el == null) display(prefix + "Element is null");
		else {
			display(prefix + "Element." + level + ": " + el.getQualifiedName() + '='
					+ el.getValue() + "=");
			display(prefix + " Atts: " + el.getAttributeCount());
			for (int i = 0; i < el.getAttributeCount(); i++) {
				Attribute att = el.getAttribute(i);
				display(prefix + "  " + i + " " + att.getQualifiedName() + "="
						+ att.getValue() + "=");
			}
			Elements els = el.getChildElements();
			display(prefix + " Childs: " + els.size());
			for (int i = 0; i < els.size(); i++) {
				display(prefix + "  " + i + " " + els.get(i));
			}
		}
	}

	void printChildren(int level, Element el, String msg) {
		printChildren(level, el.getChildElements(), msg);
	}

	void printChildren(int level, Elements els, String msg) {
		if (!DEBUG) return;
		if (!Utils.isBlank(msg)) display("--- Begin " + msg);
		for (int i = 0; i < els.size(); i++) {
			Element pos = els.get(i);
			printElement(level + 1, pos, null);
		}
		if (!Utils.isBlank(msg)) display("--- End " + msg);
	}

	/**
	 * @param src
	 * @param dst
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	static int UTF16toUTF8(String src, String dst)
			throws UnsupportedEncodingException, IOException {
		int cnt = 0;
		String[] list = FileUtils.dirList(src, null);
		for (int i = 0; i < list.length; i++) {
			String utf8 = new String(FileUtils.getBuffer(src + list[i]),
					OALD102.UTF16CODEPAGE);
			//log("utf8=" + utf8);
			FileUtils.setBuffer(dst + list[i], utf8.getBytes(OALD102.UTF8CODEPAGE));
			cnt++;
		}
		return cnt;
	}

	/**
	* 1. Вместо строки
	* <?xml version="1.0" encoding="utf-8"?>
	* <p:OALD xmlns:p="Edi" id="xmlDoc" isroot="1">
	* д.б. строка
	* <OALD id="xmlDoc" isroot="1">
	* 2. Заменить p: на пусто
	* 
	* @param xml
	* @return
	*/
	static String fixXML(String xml) {
		//1.1
		int pos = xml.indexOf("<p:");
		if (pos != -1) xml = xml.substring(pos);
		//1.2
		String toDel = "xmlns:p=\"Edi\"";
		pos = xml.indexOf(toDel);
		if (pos != -1) xml = xml.substring(0, pos)
				+ xml.substring(pos + toDel.length());
		//2.
		xml = xml.replaceAll("p:", "");
		//log("xml=" + xml + "=");
		return xml;
	}

	/**
	 * Процесс обработки:
	 * 1)Для слов с несколькими pos entry31758.xml rose 
	 * 0.entry                      
	 *   1.h-g                       
	 *     2.h - слово               
	 *     2.z[] список pos
	 *     2.<span class="label"> 
	          <p:z>[
	            <p:gr>
	              <p:zgct>U</p:zgct> - type
	            </p:gr>
	          ]</p:z>
	         </span>         
	 *   1.p-g[pos  
	 *     2.<span class="label"> 
	          <p:z>[
	            <p:gr>
	              <p:zgct>U</p:zgct> - type
	            </p:gr>
	          ]</p:z>
	         </span>                
	 *     2.z - pos
	 *     2.n-g[] - список значений слова
	 *       3. <p:alt>the Beatitudes</p:alt> - альтер. значение (03119 beatitude)
	 *       3. ngnum - номер
	 *       3. span - тип
	 *       3. d - значение
	 *       3. span - пример
	 *       
	 * 2)Для слов с 1 pos entry00004.xml A2
	 * 0.entry
	 *   1.h-g 
	 *     2.h - слово 
	 *     2.z - pos
	 *     2.n-g[] - список значений слова
	 * 
	 * 3) для слов где pos пустой, подразумеваем [C] entry00249.xm the accused
	 *   
	 * 0.entry
	     1.h-g
	       2.<hs>the</hs> - the
	       2.<p:z></p:z>  - pos
	       2.<p:h>ac·cused</p:h> - слово

	 * 
	 * @param fullName
	 * @throws Exception 
	 */
	void parse(String fullName, String fn) throws Exception {
		try {
			//Builder parser = new Builder();
			//Document doc = parser.build(fn); //разбираем прямо из файла
			display("*** read: " + fn);
			Entry entryW = new Entry(fn);
			String xml = new String(FileUtils.getBuffer(fullName), OALD102.UTF16CODEPAGE);
			cntRead++;
			xml = fixXML(xml);
			Document doc = parser.build(xml, ""); //разбираем из строки
			//log("TO XML=" + doc.toXML());
			//log("TO str=" + doc.toString());

			// разбор XML
			//0
			Element root = doc.getRootElement();
			//1
			Element entry = root.getFirstChildElement("entry"); //все элементы
			printElement(0, entry, null);
			//*
			Element hg = entry.getFirstChildElement("h-g");
			printElement(1, hg, null);
			int nounPos = entryW.analyseHG(hg);

			if (entryW.badEntry() == true) return;

			//log("1. "+entryW.toStringFull());
			if (nounPos < 0) return;
			//cntNoun++;
			//cntSymbol++;
			//writeEntry(fn, h.getValue(), entry.getValue(), null);
			///////////////////////////////////
			//Element pg = hg.getFirstChildElement("n-g");
			//если нет в hg, то берем из entry
			//if (pg == null) pg = entry.getFirstChildElement("p-g");

			display("  means -----");
			Elements ngList = null; //список значений
			Element pg = entry.getFirstChildElement("p-g");
			if (pg != null) {
				display("nounPos.2=" + nounPos + " p-g not null");
				Elements entryChildren = entry.getChildElements(); //берем список p-g
				for (int i = 0; i < entryChildren.size(); i++) {
					Element el = entryChildren.get(i);
					if (el.getQualifiedName() == "p-g") {
						printElement(1, el, null);

						//ищем noun
						int np = Entry.NPNOTFOUND;
						Elements hgChildren = el.getChildElements(); //берем список z
						for (int i2 = 0; i2 < hgChildren.size(); i2++) {
							Element z = hgChildren.get(i2);
							if (z.getQualifiedName() == "z") {//список pos
								OALD102.printElement(2, z, "список pos=" + i2);
								np = Entry.findNounInPOS(z);
							}
							if (np == Entry.NPNOTNOUN || np >= 0) break;
						}

						if (np >= 0) {
							ngList = el.getChildElements(); //берем список значений
							display("nounPos.3=" + nounPos + "=" + i);
							break;
						}
					}
				}
				if (ngList == null) {
					entryW.setErr("Не найдено значение NOUN");
					writeEntry(entryW);
					return;
				}
			} else {
				display("nounPos.2=" + nounPos + " p-g null");
				ngList = hg.getChildElements(); //берем список значений
			}
			//printChildren(2, ngList, "*** ngList");
			if (entryW.getType() == Entry.TYPEBLANK) entryW.setType(Entry
					.findType(ngList));
			for (int i = 0; i < ngList.size(); i++) {
				Element el = ngList.get(i);
				if (el.getQualifiedName() == "n-g") {
					printElement(2, el, null);
					entryW.addMeaning(el); //el.getValue());
					//printChildren(2, el, null);
				}
			}
			entryW.finalize();
			if (entryW.verTypes()) {
				writeEntry(entryW); //full
				//[C]
				//if (entryW.inTypes1(Entry.TYPEC) == true) writeEntry(entryW);
				//[U]
				//if (entryW.inTypes1(Entry.TYPEU) == true) writeEntry(entryW);
				//[pl.]
				//if (entryW.inTypes1(Entry.TYPEPL) == true) writeEntry(entryW);
			} else writeEntry(entryW);

		} catch (ParsingException e) {
			displayErr(fullName + " is not well-formed.\n" + e.getMessage()
					+ "\nL, C: " + e.getLineNumber() + ", " + e.getColumnNumber()
					+ "\nURI: " + e.getURI());
			throw e;
		} catch (IOException e) {
			displayErr("Due to an IOException, the parser could not print: "
					+ fullName + "\n" + e.toString());
			throw e;
		} catch (Exception e) {
			displayErr("Ошибка разбора файла: " + fullName + "\n" + e.toString());
			throw e;
		}
	}

	/**
	Sothink Swf Decompiler 3.7

	http://_/files/ABBYY.Lingvo.12ML.torrent 
	http://forum.ru-board.com/topic.cgi?forum=93&topic=2246#1

	http://www.lexilogos.com/anglais_langue_dictionnaires.htm

	http://www.oup.com/oald-bin/web_getald7index1a.pl?search_word=

	[C] family is/are
	[U] staff is/are
	[sing.] applause is/are
	[U, sing.] affection
	[pl.] string  6 the strings
	[sing.] 1 the universe
	none abbot
	   affair
	
	 * @param args
	 */
	public static void main(String[] args) {
		String srcFolder = "C:\\1\\dict\\oxford\\xml.tst\\";
		//srcFolder = "C:\\1\\dict\\oxford\\xml.full\\";
		String dstFolder = "C:\\1\\dict\\oxford\\out\\";
		System.out.println("Start: " + srcFolder);
		OALD102 oald = null;
		try {
			//log("Файлов: "+ UTF16toUTF8(folder + "xml\\", folder + "xml8\\"));
			oald = new OALD102();
			//			String fn = "entry31758.xml"; //rose
			//			fn = "entry00370.xml"; // Элемент Z (pos) пустой
			//			oald.parse(srcFolder + fn, fn);

			String[] list = FileUtils.dirList(srcFolder, FileUtils.DirFilter
					.dos2unixMask("*.xml"));
			OALD102.outDir = dstFolder;
			OALD102.outFile = "oald_" + VERSION;
			OALD102.outExt = "doc";
			OALD102.title = "Анализ существительных "
					+ VERSION
					+ " Дата анализа: "
					+ new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(new java.util.Date()) + "<br>\n" + "Каталог: "
					+ srcFolder + " Файлов: " + list.length;
			OALD102.writeLog(OALD102.title + OALD102.ls);
			FileUtils.delete(dstFolder, FileUtils.DirFilter.dos2unixMask("*."
					+ OALD102.outExt));
			for (int i = 0; i < list.length; i++) {
				oald.parse(srcFolder + list[i], list[i]);
				//if (i > 2000) break; //debug
				if (i % MAXFILE == 0) System.out.println(i + "/" + oald.cntErr + " "
						+ list.length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (oald == null) System.out.println("Stop");
		else {
			try {
				oald.closeFile("");
				OALD102.closeLog(OALD102.ls + oald.getStatictic());
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Stop. " + oald.getStatictic());
		}
	}
}
