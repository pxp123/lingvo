package lingvo;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

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
public class OALD104 {

	public static final boolean DEBUG = true; //false;

	public static final String VERSION = "1.04"; // 06.03.08;

	public static final String WINCODEPAGE = "windows-1251";

	public static final String UTF8CODEPAGE = "UTF-8";

	public static final String UTF16CODEPAGE = "UTF-16";

	public static final String XMLSEL = "xml.sel";

	public static final String BR = "<br>\n";

	public static final String BLANK = "&nbsp;";

	public static final int MAXFILE = 500;

	static java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss");

	//FileWriter fw = null;

	static FileWriter log = null;

	static String ls = System.getProperty("line.separator", "\n");

	public int cntRead = 0;

	public int cntSel = 0;

	public int cntErr = 0;

	//public int cntSymbol = 0;

	//public int cntMean = 0;

	Builder parser = new Builder();

	Hashtable<String, Writer> writers = new Hashtable<String, Writer>();

	public static String baseDir, srcDir, outDir, outFile, outExt, title;

	static void openLog() throws IOException {
		log = new FileWriter(outDir + "_OALDlog.txt", true);
		writeLog(OALD104.ls + "*** Start " + VERSION + " "
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

	void writeEntry(Entry entry, String type) throws IOException {
		Writer wr = writers.get(type);
		if (wr == null) {
			wr = new Writer();
			writers.put(type, wr);
		}
		wr.writeEntry(entry, type);
	}

	private void closeFiles(String footer) throws IOException {
		writeLog("\nСтатистика:");
		Enumeration e = writers.elements();
		while (e.hasMoreElements()) {
			Writer wr = (Writer) e.nextElement();
			writeLog(wr.getStatictic());
			wr.closeFile(footer);
		}
		writers.clear();
	}

	String getStatictic() {
		return "Всего прочитано/ошибок: " + cntRead + "/" + cntErr
				+ " Скопировано: " + cntSel;
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
					OALD104.UTF16CODEPAGE);
			//log("utf8=" + utf8);
			FileUtils.setBuffer(dst + list[i], utf8.getBytes(OALD104.UTF8CODEPAGE));
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
	 * 
  <p:entry id="a731760"> - 0.
    <p:h-g id="a731760_13"> - 1.
      <p:h>rose</p:h> - 2. word
       <p:z>            2. pos[] 
          <p:pos>noun</p:pos>
          ,
          <p:pos>adjective</p:pos>
        —see also</p:z>
       <span class="label"> - 2. [] type 
	          <p:z>[
	            <p:gr>
	              <p:zgct>U</p:zgct> - type
	            </p:gr>
	          ]</p:z>
	         </span>        
    <p:p-g id="a731760_14"> - 1. [] список значений слова
      <p:z> - 2. pos
        <span class="dictbats"></span>
        <p:pos>noun</p:pos>
      </p:z>
      <p:n-g id="a731760_15"> 2. одно из meaning
	      <p:alt>the Beatitudes</p:alt> - 3. альтер. значение (03119 beatitude)
        <p:ngnum>1</p:ngnum> - 3. number
        <span class="label"> - 3. type 
          <p:z>[
            <p:gr>
              <p:zgct>C</p:zgct>
            </p:gr>
            ]</p:z>
          <p:z></p:z>
        </span>
        <p:d id="d000059779">a flower with a sweet smell that grows on a bush with stems</p:d> -3. meaning
        <span class="exa"> - 3. example
          <p:z>:</p:z>
          <br/>
          <p:x id="x000062639">a bunch of red roses</p:x>
          <p:z>
            <span class="dictbats"></span>
          </p:z>
          <p:x id="x000062640">a
            <p:cl id="c000011880">rose bush / garden</p:cl>
          </p:x>
          <p:z>
            <span class="dictbats"></span>
          </p:z>
          <p:x id="x000062641">a
            <p:cl id="c000011881">climbing / rambling rose</p:cl>
          </p:x>
        </span
        
       
	 * 2)Для слов с 1 pos entry00004.xml A2
	     <p:entry id="a700005">     - 0. 
	       <p:h-g id="a700005_1">   - 1.
	         <hs>the</hs> - 2. the
           <p:h>A2 (level)</p:h> - 2.word
           <p:z>  - 2. pos
             <p:pos>noun</p:pos>
           </p:z>
           <p:n-g id="s000000005" class="single"> - 2.[] список значений слова
             <span class="label">
               <p:z>[
                 <p:gr>
                   <p:zgct>C</p:zgct>
                 </p:gr>
                     , 
                 <p:gr>
                   <p:zgct>U</p:zgct>
                 </p:gr>
                ]</p:z>
             </span>
            <p:d id="d000000022">a British exam usually taken in ... to universities</p:d>
	 * 
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
			String xml = new String(FileUtils.getBuffer(fullName), OALD104.UTF16CODEPAGE);
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
								OALD104.printElement(2, z, "список pos=" + i2);
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
					writeEntry(entryW, Entry.TYPEERR);
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
			/////////
			entryW.finalize();
			display(entryW.toStringFull()); //debug
			if (entryW.badEntry() == true) return;
			copySelected(fullName, fn);
			/////////
			if (entryW.verTypes()) {
				//THE
				if (entryW.isThe()) {
					if (entryW.inTypes1(Entry.TYPEPL) == true) writeEntry(entryW,
							Entry.THETHE + " + " + Entry.TYPEPL);
					else writeEntry(entryW, Entry.THETHE + " - " + Entry.TYPEPL);
				}
				//end THE
				
				writeEntry(entryW, Entry.TYPEFULL); //full
				//[C]
				if (entryW.inTypes1(Entry.TYPEC) == true) writeEntry(entryW,
						Entry.TYPEC);
				//[U]
				else if (entryW.inTypes1(Entry.TYPEU) == true) writeEntry(entryW,
						Entry.TYPEU);
				//[pl.]
				else if (entryW.inTypes1(Entry.TYPEPL) == true) writeEntry(entryW,
						Entry.TYPEPL);
				else writeEntry(entryW, Entry.TYPEOTHER);
			} else writeEntry(entryW, Entry.TYPEERR);

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

	private void copySelected(String fullName, String fn) throws IOException {
		cntSel++;
		FileUtils.copy(fullName, baseDir + XMLSEL + "\\", true);
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
		String baseDir = "C:\\1\\dict\\oxford\\";
		String srcFolder = "C:\\1\\dict\\oxford\\xml.tst\\";
		//srcFolder = "C:\\1\\dict\\oxford\\xml.full\\";
		String dstFolder = "C:\\1\\dict\\oxford\\out\\";
		System.out.println("Start: " + srcFolder);
		OALD104 oald = null;
		try {
			//log("Файлов: "+ UTF16toUTF8(folder + "xml\\", folder + "xml8\\"));
			oald = new OALD104();
			//			String fn = "entry31758.xml"; //rose
			//			fn = "entry00370.xml"; // Элемент Z (pos) пустой
			//			oald.parse(srcFolder + fn, fn);

			String[] list = FileUtils.dirList(srcFolder, FileUtils.DirFilter
					.dos2unixMask("*.xml"));
			OALD104.baseDir = baseDir;
			OALD104.srcDir = srcFolder;
			OALD104.outDir = dstFolder;
			OALD104.outFile = "oald_" + VERSION;
			OALD104.outExt = "doc";
			OALD104.title = "Анализ существительных "
					+ VERSION
					+ " Дата анализа: "
					+ new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(new java.util.Date()) + "<br>\n" + "Каталог: "
					+ srcFolder + " Файлов: " + list.length;
			OALD104.writeLog(OALD104.title + OALD104.ls);
			FileUtils.delete(dstFolder, FileUtils.DirFilter.dos2unixMask("*."
					+ OALD104.outExt));
			FileUtils.delete(baseDir + XMLSEL + "\\", FileUtils.DirFilter
					.dos2unixMask("*.xml"));
			for (int i = 0; i < list.length; i++) {
				oald.parse(srcFolder + list[i], list[i]);
				if (i > 2000) break; //debug
				if (i % MAXFILE == 0) System.out.println(i + "/" + oald.cntErr + " "
						+ list.length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (oald == null) System.out.println("Stop");
		else {
			try {
				oald.closeFiles("");
				OALD104.closeLog(OALD104.ls + oald.getStatictic());
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Stop. " + oald.getStatictic());
		}
	}
}
