package lingvo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
 *         ��������� � 1.07:
 *         01204 amniocentesis p.47 � ������ [U, sing.]
 *         01281 anafema p. � ������ [U, usually sing., C]
 *         08759 crew p.362 � ������ [C+sing./pl. v.]
 *         09325 dark p. 386
 * 
 * @since v 1.01 06.03.08
 */
public class OALD {

	public static boolean DEBUG = false;

	public static final String VERSION = "2.00";
	
	public static final String RARROW= "-�";

	public static final String WINCODEPAGE = "windows-1251";

	public static final String UTF8CODEPAGE = "UTF-8";

	public static final String UTF16CODEPAGE = "UTF-16";

	public static final String XMLSEL = "out.sel";

	public static final String BR = "<br>\n";

	public static final String BLANK = "&nbsp;";
	
	public static final ArrayList<String[]> ADJ_SUFFIXES = new ArrayList<String[]>();

	public static final int MAXFILE = 500;

	static java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss");
	static java.text.SimpleDateFormat df2 = new java.text.SimpleDateFormat(
			"yyyy_dd_MM_HH_mm_ss");

	//http://www.laliluna.de/articles/log4j-tutorial.html
	//For the standard levels, we have DEBUG < INFO < WARN < ERROR < FATAL.
    //private static final Logger log4 = Logger.getLogger( OALD.class );

	static FileWriter log = null;

	static String ls = System.getProperty("line.separator", "\n");
	static String fs = System.getProperty("file.separator", "\\");
	
	public int cntRead = 0;

	public int cntSel = 0;

	public int cntErr = 0;
	
	public int cntWar = 0;

	// public int cntSymbol = 0;

	public static int cntSkip = 0;

	Builder parser = new Builder();

	// static Hashtable<String, Writer> writers = new Hashtable<String,
	// Writer>();
	static TreeMap<String, Writer> writers = new TreeMap<String, Writer>();

	/** ������ ����� */
	public static TreeSet<String> totalGroupList = new TreeSet<String>();
	
	/** ������ ������ ���� */
	public static TreeSet<String> totalPOSList = new TreeSet<String>();

	public static String baseDir, srcDir, outDir, outFile, outExt, title;

	static java.util.Date startDate;
	
	static {
		ADJ_SUFFIXES.add(new String[] { "a" });
		ADJ_SUFFIXES.add(new String[] { "age" });
		ADJ_SUFFIXES.add(new String[] { "an" });
		ADJ_SUFFIXES.add(new String[] { "ard" });
		ADJ_SUFFIXES.add(new String[] { "ary", "ery" });
		ADJ_SUFFIXES.add(new String[] { "er" });
		ADJ_SUFFIXES.add(new String[] { "et" });
		ADJ_SUFFIXES.add(new String[] { "ie", "ey", "y" });
		ADJ_SUFFIXES.add(new String[] { "ist" });
		ADJ_SUFFIXES.add(new String[] { "ling" });
		ADJ_SUFFIXES.add(new String[] { "o" });
		ADJ_SUFFIXES.add(new String[] { "ster" });
		ADJ_SUFFIXES.add(new String[] { "dom" });
		ADJ_SUFFIXES.add(new String[] { "hood" });
		ADJ_SUFFIXES.add(new String[] { "ion" });
		ADJ_SUFFIXES.add(new String[] { "ment" });
		ADJ_SUFFIXES.add(new String[] { "our", "eur" });
		ADJ_SUFFIXES.add(new String[] { "th" });
		ADJ_SUFFIXES.add(new String[] { "tude" });
		ADJ_SUFFIXES.add(new String[] { "ure" });
		ADJ_SUFFIXES.add(new String[] { "ce", "cy" });
		ADJ_SUFFIXES.add(new String[] { "ty" });
		ADJ_SUFFIXES.add(new String[] { "ism" });
		ADJ_SUFFIXES.add(new String[] { "ness" });
	}

	static void openLog() throws IOException {
		log = new FileWriter(outDir + "_OALDlog.txt", true);
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
		if (log == null)
			openLog();
		log.write(row + ls);
		log.flush();
	}

	static void writeEntry(Entry entry, String group) throws IOException {
		Writer wr = writers.get(group);
		if (wr == null) {
			wr = new Writer();
			writers.put(group, wr);
		}
		wr.writeEntry(entry, group);
	}

	private void closeFiles(String footer) throws IOException {
		writeLog("\n\n" + VERSION + " ����������\n\t������ ������ ["
				+ writers.size() + "]:");
		for (Writer wr : writers.values()) {
			writeLog(wr.getStatictic());
			wr.closeFile(footer);
		}
		writers.clear();
		writeLog("\n\t������ ����� [" + totalGroupList.size() + "]: ");
		for (String ttl : totalGroupList) {
			writeLog(ttl);
		}
		writeLog("\n\t������ ������ ���� [" + totalPOSList.size() + "]: ");
		for (String ttl : totalPOSList) {
			writeLog(ttl);
		}
	}

	String getStatictic() {
		return title + "\n����� ��������� ����/�������: " + cntRead + "/"
				+ cntSel + " err/war: " + cntErr + "/" + cntWar
				+ " ��������� ����: " + cntSkip + " ����������� ����: "
				+ cntSel;
	}

	static void display(String msg) {
		if (DEBUG)
			System.out.println(msg);
	}

	static void displayErr(String msg) throws IOException {
		// if (DEBUG) System.err.println(msg);
		System.out.println("ERR: " + msg);
		writeLog("ERR: " + msg);
	}

	static void displayWar(String msg) throws IOException {
		// if (DEBUG) System.err.println(msg);
		System.out.println("WAR: " + msg);
		writeLog("WAR: " + msg);
	}

	static void printElement(int level, Element el, String msg) {
		if (!DEBUG)
			return;
		String prefix = "";
		for (int l = 0; l < level; l++) {
			prefix += ' ';
		}
		if (!Utils.isBlank(msg))
			display(prefix + msg);
		if (el == null)
			display(prefix + "Element is null");
		else {
			display(prefix + "Element." + level + ": " + el.getQualifiedName()
					+ '=' + el.getValue() + "=");
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
		if (!DEBUG)
			return;
		if (!Utils.isBlank(msg))
			display("--- Begin " + msg + " size=" + els.size());
		for (int i = 0; i < els.size(); i++) {
			Element pos = els.get(i);
			printElement(level + 1, pos, "i=" + i);
		}
		if (!Utils.isBlank(msg))
			display("--- End " + msg);
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
			// log("utf8=" + utf8);
			FileUtils
					.setBuffer(dst + list[i], utf8.getBytes(OALD.UTF8CODEPAGE));
			cnt++;
		}
		return cnt;
	}

	/**
	 * 1. ������ ������
	 * <?xml version="1.0" encoding="utf-8"?>
	 * <p:OALD xmlns:p="Edi" id="xmlDoc" isroot="1">
	 * �.�. ������ <OALD id="xmlDoc" isroot="1"> 2. �������� p: �� �����
	 * 
	 * @param xml
	 * @return
	 */
	static String fixXML(String xml) {
		// 1.1
		int pos = xml.indexOf("<p:");
		if (pos != -1)
			xml = xml.substring(pos);
		// 1.2
		String toDel = "xmlns:p=\"Edi\"";
		pos = xml.indexOf(toDel);
		if (pos != -1)
			xml = xml.substring(0, pos) + xml.substring(pos + toDel.length());
		// 2.
		xml = xml.replaceAll("p:", "");
		// log("xml=" + xml + "=");
		return xml;
	}

	/**
	 * ������� ���������:
	 * 1)��� ���� � ����������� pos (entry31758.xml rose) 2)��� ���� � 1 pos
	 * entry00004.xml A2
	 * 0.<entry> 0.<entry>
	 * 1.<h-g> �� ��� ��������� �� ����� ����� 1.<h-g>
	 * 2.<hs> - the 2.<hs>
	 * 2.<h> - word 2.<h>
	 * 2.<z> - [] pos 2.<z>
	 * 2.<span> - [] group 2.<span>
	 * 1.
	 * <p-g>
	 * �� ��� ��������� � ������ �������� �� pos 2. <n-g> 2. <z> - pos 3. ����
	 * ��� � � ������ 1) 2.<n-g> 3.<alt> - �������������� ����� 3.<ngnum> -
	 * ����� ������� 3.<span> - ������ 4.<d> - �������� 5.<span> - ������
	 * 
	 * 1)��� ���� � ����������� pos entry31758.xml rose
	 * 
	 * <p:entry id="a731760">
	 * - 0.
	 * <p:h-g id="a731760_13">
	 * - 1. <hs>the</hs> - 2. the
	 * <p:h>
	 * rose
	 * </p:h>
	 * - 2. word
	 * <p:z>
	 * 2. pos[]
	 * <p:pos>
	 * noun
	 * </p:pos>
	 * ,
	 * <p:pos>
	 * adjective
	 * </p:pos>
	 * �see also</p:z>
	 * <span class="label"> - 2. [] group
	 * <p:z>
	 * [
	 * <p:gr>
	 * <p:zgct>
	 * U
	 * </p:zgct>
	 * - group
	 * </p:gr>
	 * ]</p:z>
	 * </span>
	 * <p:p-g id="a731760_14">
	 * - 1. [] ������ �������� �����
	 * <p:z>
	 * - 2. pos <span class="dictbats"></span>
	 * <p:pos>
	 * noun
	 * </p:pos>
	 * </p:z>
	 * <p:n-g id="a731760_15">
	 * 2. ���� �� meaning
	 * <p:alt>
	 * the Beatitudes
	 * </p:alt>
	 * - 3. ������. �������� (03119 beatitude)
	 * <p:ngnum>
	 * 1
	 * </p:ngnum>
	 * - 3. number
	 * <span class="label"> - 3. group
	 * <p:z>
	 * [
	 * <p:gr>
	 * <p:zgct>
	 * C
	 * </p:zgct>
	 * </p:gr>
	 * ]</p:z>
	 * <p:z>
	 * </p:z>
	 * </span>
	 * <p:d id="d000059779">
	 * a flower with a sweet smell that grows on a bush with stems
	 * </p:d>
	 * -3. meaning
	 * <span class="exa"> - 3. example
	 * <p:z>
	 * :
	 * </p:z>
	 * <br/>
	 * <p:x id="x000062639">
	 * a bunch of red roses
	 * </p:x>
	 * <p:z>
	 * <span class="dictbats"></span>
	 * </p:z>
	 * <p:x id="x000062640">
	 * a
	 * <p:cl id="c000011880">
	 * rose bush�/�garden
	 * </p:cl>
	 * </p:x>
	 * <p:z>
	 * <span class="dictbats"></span>
	 * </p:z>
	 * <p:x id="x000062641">
	 * a
	 * <p:cl id="c000011881">
	 * climbing�/�rambling rose
	 * </p:cl>
	 * </p:x>
	 * </span
	 * 
	 * 
	 * 2)��� ���� � 1 pos entry00004.xml A2
	 * <p:entry id="a700005">
	 * - 0.
	 * <p:h-g id="a700005_1">
	 * - 1. <hs>the</hs> - 2. the
	 * <p:h>
	 * A2 (level)
	 * </p:h>
	 * - 2.word
	 * <p:z>
	 * - 2. pos
	 * <p:pos>
	 * noun
	 * </p:pos>
	 * </p:z>
	 * <p:n-g id="s000000005" class="single">
	 * - 2.[] ������ �������� ����� <span class="label">
	 * <p:z>
	 * [
	 * <p:gr>
	 * <p:zgct>
	 * C
	 * </p:zgct>
	 * </p:gr>
	 * ,�
	 * <p:gr>
	 * <p:zgct>
	 * U
	 * </p:zgct>
	 * </p:gr>
	 * ]</p:z>
	 * </span>
	 * <p:d id="d000000022">
	 * a British exam usually taken in ... to universities
	 * </p:d>
	 * 
	 * 
	 * @param fullName
	 * @throws Exception
	 */
	void parse(String fullName, String fn) throws Exception {
		try {
			// Builder parser = new Builder();
			// Document doc = parser.build(fn); //��������� ����� �� �����
			display("*** read: " + fn);
			Entry entryW = new Entry(fn);
			String xml = new String(FileUtils.getBuffer(fullName),
					OALD.UTF16CODEPAGE);
			cntRead++;
			xml = fixXML(xml);
			Document doc = parser.build(xml, ""); // ��������� �� ������
			// log("TO XML=" + doc.toXML());
			// log("TO str=" + doc.toString());

			// ������ XML
			// 0
			Element root = doc.getRootElement();
			// 1
			Element entry = root.getFirstChildElement("entry"); // ��� ��������
			// printElement(0, entry, null);
			entryW.analyzeEntry(entry);
			if (entryW.skip) return;
			entryW.verGroups();

			/*
			 * ������ ����� [17]:
			 * C
			 * C+sing./pl. v.
			 * U
			 * U+sing./pl. v.
			 * after noun
			 * no passive
			 * not before noun
			 * not usually before noun
			 * often passive
			 * only before noun
			 * pl.
			 * sing.
			 * sing.+ sing./pl. v.
			 * usually before noun
			 * usually passive
			 * usually pl.
			 * usually sing.
			 */

			if (!Utils.isBlank(entryW.warning)) {
				cntWar++;
				writeEntry(entryW, Entry.GROUPWAR);
				OALD.writeLog("WAR: " + entryW.warning);
			}
			
			if (Utils.isBlank(entryW.error)) {
				// if (entryW.isInitCap()) writeEntry(entryW, TYPEUPPER);
				// //upper
				// if (true) return;

				// �������� ������ ���� ������ ������ ���� 21.02.11
				entryW.getDistinctGroups();
				//OALD.display("entryW.1: " + " distinctGR=" + entryW.distinctGroups);

				if (entryW.distinctGroups.size() > 1) {
					//writeEntry(entryW, Entry.GROUPGROUPS);
					if (entryW.posList.size() == 1) { //&& entryW.posList.get(Entry.POSNOUN) != null) {
						String word = entryW.getWord().toLowerCase();
						boolean found = false;
						for (int i = 0; i < ADJ_SUFFIXES.size(); i++) {
							String[] sufs = ADJ_SUFFIXES.get(i);
							for (int j = 0; j < sufs.length; j++)
								if (word.endsWith(sufs[j])) {
									writeEntry(entryW, "adj+" + sufs[0]
											+ RARROW + Entry.POSNOUN);
									found = true;
									break;
								}
							if (found)
								break;
						}
						if (!found)
							writeEntry(entryW, Entry.POSNOUN);
					} else if (entryW.posList.size() == 2) {
						if (entryW.hasPOS(Entry.POSVERB, 1)
								&& entryW.hasPOS(Entry.POSNOUN, 2))
							writeEntry(entryW, Entry.POSVERB + RARROW
									+ Entry.POSNOUN);
						else if (entryW.hasPOS(Entry.POSADJ, 1)
								&& entryW.hasPOS(Entry.POSNOUN, 2))
							writeEntry(entryW, Entry.POSADJ + RARROW
									+ Entry.POSNOUN);
						else if (entryW.hasPOS(Entry.POSNOUN, 2))
							writeEntry(entryW, "�����������" + RARROW
									+ Entry.POSNOUN);
						else
							writeEntry(entryW, Entry.GROUPOTHER);
					} else
						writeEntry(entryW, Entry.GROUPOTHER);
				}

				/*
				 * writeEntry(entryW, Entry.TYPEFULL); //full
				 * 
				 * //adj.
				 * if (entryW.printPosList.contains(Entry.POSADJ))
				 * writeEntry(entryW,
				 * Entry.POSADJ);
				 * 
				 * if (entryW.inTypes2(new String[] { Entry.TYPEU,
				 * Entry.TYPESING })) writeEntry(
				 * entryW, Entry.TYPEU + ", " + Entry.TYPESING);//01204 amnioce
				 * if (entryW.inTypes2(new String[] { Entry.TYPEU,
				 * Entry.TYPESING2,
				 * Entry.TYPEC })) writeEntry(entryW, Entry.TYPEU + ", "
				 * + Entry.TYPESING2 + ", " + Entry.TYPEC);//01281 anafema
				 * if (entryW.inTypes2(new String[] { Entry.TYPESING4 }))
				 * writeEntry(
				 * entryW, Entry.TYPESING4); //16422 group p.686, 08759 crew
				 * p.362
				 * if (entryW.inTypes2(new String[] { Entry.TYPESING5 }))
				 * writeEntry(
				 * entryW, Entry.TYPESING5);
				 * 
				 * //THE
				 * if (entryW.isThe()) {
				 * if (entryW.inTypes1(new String[] { Entry.TYPEC }))
				 * writeEntry(entryW,
				 * Entry.TYPEC + " + " + Entry.THETHE);//departed
				 * else if (entryW.inTypes1(new String[] { Entry.TYPEU }))
				 * writeEntry(
				 * entryW, Entry.TYPEU + " + " + Entry.THETHE);
				 * else if (entryW.inTypes1(new String[] { Entry.TYPESING,
				 * Entry.TYPESING2 })) writeEntry(entryW, Entry.TYPESING + " + "
				 * + Entry.THETHE); //absurd
				 * else if (entryW.inTypes1(new String[] { Entry.TYPESING3 }))
				 * writeEntry(
				 * entryW, Entry.TYPESING3 + " + " + Entry.THETHE);
				 * else if (entryW
				 * .inTypes1(new String[] { Entry.TYPEPL, Entry.TYPEPL2 }))
				 * writeEntry(
				 * entryW, Entry.TYPEPL + " + " + Entry.THETHE);//ancients
				 * else writeEntry(entryW, Entry.TYPEOTHER + " + " +
				 * Entry.THETHE);
				 * } else { //end THE
				 * if (entryW.inTypes1(new String[] { Entry.TYPEC }))
				 * writeEntry(entryW,
				 * Entry.TYPEC);
				 * else if (entryW.inTypes1(new String[] { Entry.TYPEU }))
				 * writeEntry(
				 * entryW, Entry.TYPEU);
				 * else if (entryW.inTypes1(new String[] { Entry.TYPESING,
				 * Entry.TYPESING2 })) writeEntry(entryW, Entry.TYPESING);
				 * else if (entryW.inTypes1(new String[] { Entry.TYPESING3 }))
				 * writeEntry(
				 * entryW, Entry.TYPESING3 + " + " + Entry.THETHE);
				 * else if (entryW
				 * .inTypes1(new String[] { Entry.TYPEPL, Entry.TYPEPL2 }))
				 * writeEntry(
				 * entryW, Entry.TYPEPL);
				 * else writeEntry(entryW, Entry.TYPEOTHER);
				 * }
				 */
			} else {
				cntErr++;
				writeEntry(entryW, Entry.GROUPERR);
			}	

		} catch (ParsingException e) {
			displayErr(fullName + " is not well-formed.\n" + e.getMessage()
					+ "\nL, C: " + e.getLineNumber() + ", "
					+ e.getColumnNumber() + "\nURI: " + e.getURI());
			throw e;
		} catch (IOException e) {
			displayErr("Due to an IOException, the parser could not print: "
					+ fullName + "\n" + e.toString());
			throw e;
		} catch (Exception e) {
			displayErr("������ ������� �����: " + fullName + "\n"
					+ e.toString());
			throw e;
		}
	}

	public void copySelected(String fullName, String fn) throws IOException {
		cntSel++;
		FileUtils.copy(fullName, baseDir + XMLSEL + fs, true);
	}

	/**
	 * Sothink Swf Decompiler 3.7
	 * 
	 * http://_/files/ABBYY.Lingvo.12ML.torrent
	 * http://forum.ru-board.com/topic.cgi?forum=93&topic=2246#1
	 * 
	 * http://www.lexilogos.com/anglais_langue_dictionnaires.htm
	 * 
	 * http://www.oup.com/oald-bin/web_getald7index1a.pl?search_word=
	 * 
	 * [C] family is/are
	 * [U] staff is/are
	 * [sing.] applause is/are
	 * [U, sing.] affection
	 * [pl.] string 6 the strings
	 * [sing.] 1 the universe
	 * none abbot
	 * affair
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		startDate = new java.util.Date();
		/*
		// Set up a simple configuration that logs on the console.
		BasicConfigurator.configure();
		//For the standard levels, we have DEBUG < INFO < WARN < ERROR < FATAL.
		//log4.setLevel(Level.FATAL); 
		log4.trace("trace");
		log4.debug("debug");
		log4.info("info");
		log4.warn("warn");
		log4.error("error");
		log4.fatal("fatal");
		*/

		parseCmd(args);
		
		OALD oald = null;
		try {
			oald = new OALD();
			// String baseDir = "c:\\src\\oxford\\"; //D:\src\oxford\
			String srcFolder = baseDir + "xml.full" + fs;
			String dstFolder = baseDir + "out" + fs;
			if (DEBUG) {
				srcFolder = baseDir + "xml.tst" + fs;
			} else {
				dstFolder += df2.format(startDate);
				boolean success = (new File(dstFolder)).mkdirs();
				if (!success) {
					throw new Exception("�� ���� ������� �������: " + dstFolder);
				}
				dstFolder += fs;
			}
			System.out.println("Start: " + srcFolder);
	
			System.out.println("������ ������ ...");
			String[] list = FileUtils.dirList(srcFolder,
					FileUtils.DirFilter.dos2unixMask("*.xml"));
			// OALD.baseDir = baseDir;
			OALD.srcDir = srcFolder;
			OALD.outDir = dstFolder;
			OALD.outFile = ""; //"oald_" + VERSION;
			OALD.outExt = "doc";
			OALD.title = "������ ��������������� "
					+ VERSION
					+ " ���� �������: "
					+ df.format(startDate) + "<br>\n"
					+ "�������: " + srcFolder + " ������: " + list.length;
			OALD.writeLog(OALD.title + OALD.ls);
			System.out.println("������� ...");
			FileUtils.delete(dstFolder,
					FileUtils.DirFilter.dos2unixMask("*." + OALD.outExt));
			FileUtils.delete(baseDir + XMLSEL + fs,
					FileUtils.DirFilter.dos2unixMask("*.xml"));
			for (int i = 0; i < list.length; i++) {
				oald.parse(srcFolder + list[i], list[i]);
				// if (i > 2000) break; //debug
				if (i % MAXFILE == 0)
					System.out.println(i + "/"  + oald.cntSel + " err/war: " + oald.cntErr  + "/" + oald.cntWar + " "
							+ list.length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (oald == null)
			System.out.println("Stop");
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

	private static void parseCmd(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-DEBUG"))
				DEBUG = true;
			else if (args[i].equalsIgnoreCase("-baseDir")) {
				i++;
				baseDir = args[i];
				if (!baseDir.endsWith(fs))
					baseDir += fs;
				 //PropertyConfigurator.configure(baseDir+"log4j.properties");
			}
		}

	}
}
