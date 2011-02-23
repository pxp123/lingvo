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

public class OALD100 {

	public static final boolean DEBUG = false;

	public static final String VERSION = "v 1.01 06.03.08";

	public static final String WINCODEPAGE = "windows-1251";

	public static final String UTF8CODEPAGE = "UTF-8";

	public static final String UTF16CODEPAGE = "UTF-16";

	public static final String NOUN = "noun";

	FileWriter fw = null;

	String ls = System.getProperty("line.separator", "\n");

	public int cntRead = 0;

	public int cntNoun = 0;

	public int cntErr = 0;

	public int cntSymbol = 0;

	Builder parser = new Builder();

	String symbol = "";

	public String outDir, outFile, outExt, title;

	void openFile(String sym) throws IOException {
		symbol = sym.toUpperCase();
		cntSymbol = 0;
		fw = new FileWriter(outDir + outFile + "_" + symbol + "." + outExt, true);
		fw
				.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\""
						+ "\n\"http://www.w3.org/TR/REC-html40/strict.dtd\">"
						+ "\n<META http-equiv=Content-Type content=\"text/html; charset=windows-1251\">\n"
						+ "<html>\n" + "<head>\n" + "<title>" + title
						+ "</title>\n</head>\n<body>\n");
		writeText(title);
		fw.write("<table width=\"100%\" border=\"1\">" + "<tr>"
				+ "  <th scope=\"col\">Файл</th>" + "  <th scope=\"col\">Слово</th>"
				+ "  <th scope=\"col\">Значения слова</th>" + "</tr>\n");
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

	String getStatictic() {
		return "Всего прочитано: " + cntRead + " Найдено по букве '" + symbol
				+ "': " + cntSymbol + " Всего найдено: " + cntNoun + " Всего ошибок: "
				+ cntErr;
	}

	void log(String msg) {
		if (DEBUG) System.out.println(msg);
	}

	void logErr(String msg) {
		if (DEBUG) System.err.println(msg);
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
					OALD100.UTF16CODEPAGE);
			//log("utf8=" + utf8);
			FileUtils.setBuffer(dst + list[i], utf8.getBytes(OALD100.UTF8CODEPAGE));
			cnt++;
		}
		return cnt;
	}

	void printElement(int level, Element el) {
		if (!DEBUG) return;
		String prefix = "";
		for (int l = 0; l < level; l++) {
			prefix += ' ';
		}
		if (el == null) log(prefix + "Element is null");
		else {
			log(prefix + "Element: " + el.getQualifiedName() + '=' + el.getValue());
			log(prefix + " Atts: " + el.getAttributeCount());
			for (int i = 0; i < el.getAttributeCount(); i++) {
				Attribute att = el.getAttribute(i);
				log(prefix + "  " + i + " " + att.getQualifiedName() + "="
						+ att.getValue());
			}
			Elements els = el.getChildElements();
			log(prefix + " Childs: " + els.size());
			for (int i = 0; i < els.size(); i++) {
				log(prefix + "  " + i + " " + els.get(i));
			}
		}
	}

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
	 * 1. Вместо строки
	 * <?xml version="1.0" encoding="utf-8"?>
	 * <p:OALD xmlns:p="Edi" id="xmlDoc" isroot="1">
	 * д.б. строка
	 * <OALD id="xmlDoc" isroot="1">
	 * 2. Заменить p: на пусто
	 * 

	 * @param fullName
	 * @throws Exception 
	 */
	void parse(String fullName, String fn) throws Exception {
		try {
			//Builder parser = new Builder();
			//Document doc = parser.build(fn); //разбираем прямо из файла
			log("read: " + fn);
			String xml = new String(FileUtils.getBuffer(fullName), OALD100.UTF16CODEPAGE);
			cntRead++;
			xml = fixXML(xml);
			Document doc = parser.build(xml, ""); //разбираем из строки
			//log("TO XML=" + doc.toXML());
			//log("TO str=" + doc.toString());

			// разбор XML
			//0
			Element root = doc.getRootElement();
			//1
			Element entry = root.getFirstChildElement("entry"); //все
			printElement(1, entry);
			//*
			//2
			Element hg = entry.getFirstChildElement("h-g");
			printElement(2, hg);
			Element h = hg.getFirstChildElement("h");
			printElement(3, h); //слово
			Element z = hg.getFirstChildElement("z");
			if (z == null) {
				cntErr++;
				//writeComment("ERROR: Файл: " + fn + " Слово: '" + h.getValue()
				//		+ "' - элемент Z (pos) пустой");
				writeEntry(fn, h.getValue(), "Элемент Z (pos) пустой", "#FF9933");
				return;
			}
			printElement(3, z); //*

			Elements poss = z.getChildElements(); //берем список pos
			boolean isNoun = false;
			for (int i = 0; i < poss.size(); i++) {
				Element pos = poss.get(i);
				printElement(4, pos);
				if (NOUN.equalsIgnoreCase(pos.getValue())) {
					isNoun = true;
					break;
				}
			}
			log("isNoun=" + isNoun);
			if (isNoun) {
				cntNoun++;
				cntSymbol++;
				writeEntry(fn, h.getValue(), entry.getValue(), null);
			}
			//1
			//Element pg = entry.getFirstChildElement("p-g");
			//printElement(2, pg);
			//*/
			/*
			Element transport_file = doc.getRootElement();
			Element file_meta_data = transport_file
					.getFirstChildElement("file-meta-data");
			Element file_name = file_meta_data.getFirstChildElement("file-name");
			log("fileYName=" + file_name.getValue());
			*/
		} catch (ParsingException e) {
			logErr(fullName + " is not well-formed.\n" + e.getMessage() + "\nL, C: "
					+ e.getLineNumber() + ", " + e.getColumnNumber() + "\nURI: "
					+ e.getURI());
			throw e;
		} catch (IOException e) {
			logErr("Due to an IOException, the parser could not print: " + fullName
					+ "\n" + e.toString());
			throw e;
		} catch (Exception e) {
			logErr("Ошибка разбора файла: " + fullName + "\n" + e.toString());
			throw e;
		}
	}

	void writeEntry(String entry, String word, String context, String color)
			throws IOException {
		word = word.replaceAll("·", "");
		word = word.replaceAll("%", "");
		word = word.replaceAll("'", "");
		word = word.replaceAll("-", "");
		log(entry + ": " + word + ": " + context);
		if (!symbol.equalsIgnoreCase(word.substring(0, 1))) {
			closeFile("");
			openFile(word.substring(0, 1));
		}
		//+"  <td>&nbsp;</td>"
		if (Utils.isBlank(color)) color = "";
		else color = " bgcolor=\"" + color + "\""; //bgcolor="#FF9933"
		fw.write("<tr" + color + ">\n" + "  <td>" + entry + "</td>\n" + "  <td>"
				+ word + "</td>\n" + "  <td>" + context + "</td>\n" + "</tr>\n");
	}

	void writeComment(String msg) throws IOException {
		//logErr(msg);
		fw.write("<!-- " + msg + " -->\n");
	}

	void writeText(String msg) throws IOException {
		fw.write(msg + "<br>\n");
	}

	/**
	Sothink Swf Decompiler 3.7

	http://_/files/ABBYY.Lingvo.12ML.torrent 
	http://forum.ru-board.com/topic.cgi?forum=93&topic=2246#1

	http://www.lexilogos.com/anglais_langue_dictionnaires.htm

	http://www.oup.com/oald-bin/web_getald7index1a.pl?search_word=

	 * @param args
	 */
	public static void main(String[] args) {
		String srcFolder = "C:\\1\\dict\\oxford\\xml.tst\\";
		//srcFolder = "C:\\1\\dict\\oxford\\xml.full\\";
		String dstFolder = "C:\\1\\dict\\oxford\\out\\";
		System.out.println("Start: " + srcFolder);
		OALD100 oald = null;
		try {
			//log("Файлов: "+ UTF16toUTF8(folder + "xml\\", folder + "xml8\\"));
			oald = new OALD100();
			//			String fn = "entry31758.xml"; //rose
			//			fn = "entry00370.xml"; // Элемент Z (pos) пустой
			//			oald.parse(srcFolder + fn, fn);

			String[] list = FileUtils.dirList(srcFolder, FileUtils.DirFilter
					.dos2unixMask("*.xml"));
			oald.outDir = dstFolder;
			oald.outFile = "oald_nouns";
			oald.outExt = "doc";
			oald.title = "Анализ существительных "
					+ VERSION
					+ " Дата анализа: "
					+ new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(new java.util.Date()) + "<br>\n" + "Каталог: "
					+ srcFolder + " Файлов: " + list.length;
			FileUtils.delete(dstFolder, FileUtils.DirFilter.dos2unixMask("*."
					+ oald.outExt));
			for (int i = 0; i < list.length; i++) {
				oald.parse(srcFolder + list[i], list[i]);
				//if (i > 10) break; //debug
				if (i % 1000 == 0) System.out.println(oald.symbol + " " + i + "/"
						+ list.length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (oald == null) System.out.println("Stop");
		else {
			try {
				oald.closeFile("");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Stop. " + oald.getStatictic());
		}
	}
}
