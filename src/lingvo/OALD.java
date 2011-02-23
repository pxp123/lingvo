package lingvo;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;
import java.util.TreeSet;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

/**
 * 
 * @author Paul
 *
 *Пожелания в 1.07:
 *01204 amniocentesis p.47 в группу [U, sing.]
 *01281 anafema p. в группу [U, usually sing., C]
 *08759 crew p.362 в группу [C+sing./pl. v.]
 *09325 dark p. 386
 *
 *@since v 1.01 06.03.08
 */
public class OALD {

	public static final boolean DEBUG = false;

	public static final String VERSION = "1.09";

	public static final String WINCODEPAGE = "windows-1251";

	public static final String UTF8CODEPAGE = "UTF-8";

	public static final String UTF16CODEPAGE = "UTF-16";

	public static final String XMLSEL = "out.sel";

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

	public static int cntSkip = 0;

	Builder parser = new Builder();

	//static Hashtable<String, Writer> writers = new Hashtable<String, Writer>();
	static TreeMap<String, Writer> writers = new TreeMap<String, Writer>();

	/**список типов*/
	public static TreeSet<String> totalTypeList = new TreeSet<String>();

	public static String baseDir, srcDir, outDir, outFile, outExt, title;

	static java.util.Date startDate;

	static void openLog() throws IOException {
		log = new FileWriter(outDir + "_OALDlog.txt", true);
		startDate = new java.util.Date();
		writeLog(OALD.ls + "*** Start " + VERSION + " " + df.format(startDate));
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

	static void writeEntry(Entry entry, String type) throws IOException {
		Writer wr = writers.get(type);
		if (wr == null) {
			wr = new Writer();
			writers.put(type, wr);
		}
		wr.writeEntry(entry, type);
	}

	private void closeFiles(String footer) throws IOException {
		writeLog("\n\n" + VERSION + " Статистика\n\tТипов файлов ["
				+ writers.size() + "]:");
		for (Writer wr : writers.values()) {
			writeLog(wr.getStatictic());
			wr.closeFile(footer);
		}
		writers.clear();
		writeLog("\n\tСписок типов [" + totalTypeList.size() + "]: "); // + totalTypeList);
		for (String ttl : totalTypeList) {
			writeLog(ttl);
		}
	}

	String getStatictic() {
		return title + "\nВсего прочитано слов/ошибок: " + cntRead + "/" + cntErr
				+ " Пропущено слов: " + cntSkip + " Скопировано слов: " + cntSel;
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

	static void printChildren(int level, Element el, String msg) {
		printChildren(level, el.getChildElements(), msg);
	}

	static void printChildren(int level, Elements els, String msg) {
		if (!DEBUG) return;
		if (!Utils.isBlank(msg)) display("--- Begin " + msg + " size=" + els.size());
		for (int i = 0; i < els.size(); i++) {
			Element pos = els.get(i);
			printElement(level + 1, pos, "i=" + i);
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
					OALD.UTF16CODEPAGE);
			//log("utf8=" + utf8);
			FileUtils.setBuffer(dst + list[i], utf8.getBytes(OALD.UTF8CODEPAGE));
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
	 * 1)Для слов с несколькими pos (entry31758.xml rose)       2)Для слов с 1 pos entry00004.xml A2
	 * 0.<entry>                                                0.<entry> 
	 *   1.<h-g>     то что относится ко всему слову              1.<h-g>
	 *     2.<hs> - the                                             2.<hs>
	 *     2.<h>  - word                                            2.<h>
	 *     2.<z>  - [] pos                                          2.<z>  
	 *     2.<span> - [] type                                       2.<span>
	 *   1.<p-g>    то что относится к одному значению по pos       2.<n-g>
	 *     2.<z>  - pos                                               3.* тоже что и в случае 1)  
	 *     2.<n-g> 
	 *       3.<alt>  - альтернативное слово
	 *       3.<ngnum> - номер начения
	 *       3.<span>  - type
	 *       4.<d>     - значение
	 *       5.<span>  - пример
	 *       
	 * 1)Для слов с несколькими pos entry31758.xml rose
	 * 
	<p:entry id="a731760"> - 0.
	  <p:h-g id="a731760_13"> - 1.
	    <hs>the</hs> - 2. the
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
			String xml = new String(FileUtils.getBuffer(fullName), OALD.UTF16CODEPAGE);
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
			//printElement(0, entry, null);
			entryW.analyzeEntry(entry);
			//if (entryW.getNounPos() < 0 && entryW.getAdjPos() < 0) return;
			display("printPosList=" + entryW.printPosList);
			if (!entryW.printPosList.contains(Entry.POSNOUN)
					&& !entryW.printPosList.contains(Entry.POSADJ)) return;
			//System.out.println(entryW.toStringFull()); //debug
			//if (entryW.badEntry()) return;
			//copySelected(fullName, fn);
			/////////
			entryW.verTypes();

			/*
			 	Список типов[15]: 
			C
			C+sing./pl. v.
			U
			U+sing./pl. v.
			not before noun
			not usually before noun
			often passive
			only before noun
			pl.
			sing.
			sing.+ sing./pl. v.
			usually before noun
			usually passive
			usually pl.
			usually sing.
			 */
			
			if (Utils.isBlank(entryW.error)) {
				//if (entryW.isInitCap()) writeEntry(entryW, TYPEUPPER); //upper
				//if (true) return;

				//печатаем только если больше одного типа 21.02.11
				entryW.getDistinctTypes();
				//OALD.display("entryW.1: " + entryW.printPosList+ " typeList="+entryW.typeList+" distinctTypes="+ entryW.distinctTypes);
				if (entryW.distinctTypes.size()>1)writeEntry(entryW, Entry.TYPEFULL); //full
				
				/*
				writeEntry(entryW, Entry.TYPEFULL); //full

				//adj.
				if (entryW.printPosList.contains(Entry.POSADJ)) writeEntry(entryW,
						Entry.POSADJ);

				if (entryW.inTypes2(new String[] { Entry.TYPEU, Entry.TYPESING })) writeEntry(
						entryW, Entry.TYPEU + ", " + Entry.TYPESING);//01204 amnioce
				if (entryW.inTypes2(new String[] { Entry.TYPEU, Entry.TYPESING2,
						Entry.TYPEC })) writeEntry(entryW, Entry.TYPEU + ", "
						+ Entry.TYPESING2 + ", " + Entry.TYPEC);//01281 anafema
				if (entryW.inTypes2(new String[] { Entry.TYPESING4 })) writeEntry(
						entryW, Entry.TYPESING4); //16422 group p.686, 08759 crew p.362
				if (entryW.inTypes2(new String[] { Entry.TYPESING5 })) writeEntry(
						entryW, Entry.TYPESING5);

				//THE
				if (entryW.isThe()) {
					if (entryW.inTypes1(new String[] { Entry.TYPEC })) writeEntry(entryW,
							Entry.TYPEC + " + " + Entry.THETHE);//departed
					else if (entryW.inTypes1(new String[] { Entry.TYPEU })) writeEntry(
							entryW, Entry.TYPEU + " + " + Entry.THETHE);
					else if (entryW.inTypes1(new String[] { Entry.TYPESING,
							Entry.TYPESING2 })) writeEntry(entryW, Entry.TYPESING + " + "
							+ Entry.THETHE); //absurd
					else if (entryW.inTypes1(new String[] { Entry.TYPESING3 })) writeEntry(
							entryW, Entry.TYPESING3 + " + " + Entry.THETHE);
					else if (entryW
							.inTypes1(new String[] { Entry.TYPEPL, Entry.TYPEPL2 })) writeEntry(
							entryW, Entry.TYPEPL + " + " + Entry.THETHE);//ancients
					else writeEntry(entryW, Entry.TYPEOTHER + " + " + Entry.THETHE);
				} else { //end THE
					if (entryW.inTypes1(new String[] { Entry.TYPEC })) writeEntry(entryW,
							Entry.TYPEC);
					else if (entryW.inTypes1(new String[] { Entry.TYPEU })) writeEntry(
							entryW, Entry.TYPEU);
					else if (entryW.inTypes1(new String[] { Entry.TYPESING,
							Entry.TYPESING2 })) writeEntry(entryW, Entry.TYPESING);
					else if (entryW.inTypes1(new String[] { Entry.TYPESING3 })) writeEntry(
							entryW, Entry.TYPESING3 + " + " + Entry.THETHE);
					else if (entryW
							.inTypes1(new String[] { Entry.TYPEPL, Entry.TYPEPL2 })) writeEntry(
							entryW, Entry.TYPEPL);
					else writeEntry(entryW, Entry.TYPEOTHER);
				}
				*/
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

	public void copySelected(String fullName, String fn) throws IOException {
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
		String baseDir = "c:\\src\\oxford\\"; //D:\src\oxford\ 
		String srcFolder = baseDir + "xml.full\\";
		if (DEBUG) srcFolder = baseDir + "xml.tst\\";
		String dstFolder = baseDir + "out\\";
		System.out.println("Start: " + srcFolder);
		OALD oald = null;
		try {
			//log("Файлов: "+ UTF16toUTF8(folder + "xml\\", folder + "xml8\\"));
			oald = new OALD();
			//			String fn = "entry31758.xml"; //rose
			//			fn = "entry00370.xml"; // Элемент Z (pos) пустой
			//			oald.parse(srcFolder + fn, fn);

			System.out.println("Список файлов ...");
			String[] list = FileUtils.dirList(srcFolder, FileUtils.DirFilter
					.dos2unixMask("*.xml"));
			OALD.baseDir = baseDir;
			OALD.srcDir = srcFolder;
			OALD.outDir = dstFolder;
			OALD.outFile = "oald_" + VERSION;
			OALD.outExt = "doc";
			OALD.title = "Анализ существительных "
					+ VERSION
					+ " Дата анализа: "
					+ new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
							.format(new java.util.Date()) + "<br>\n" + "Каталог: "
					+ srcFolder + " Файлов: " + list.length;
			OALD.writeLog(OALD.title + OALD.ls);
			System.out.println("Очистка ...");
			FileUtils.delete(dstFolder, FileUtils.DirFilter.dos2unixMask("*."
					+ OALD.outExt));
			FileUtils.delete(baseDir + XMLSEL + "\\", FileUtils.DirFilter
					.dos2unixMask("*.xml"));
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
				oald.closeFiles("");
				OALD.closeLog(OALD.ls + oald.getStatictic());
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Stop. " + oald.getStatictic());
		}
	}
}
