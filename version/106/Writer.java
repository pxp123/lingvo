package lingvo;

import java.io.FileWriter;
import java.io.IOException;

public class Writer {

	FileWriter fw = null;

	public String outDir, outFile, outExt, title;

	int cntNoun = 0, cntMean = 0, cntErr = 0;

	String type = "";

	public Writer() throws IOException {
		outDir = OALD.outDir;
		outFile = OALD.outFile;
		outExt = OALD.outExt;
		title = OALD.title;
	}

	void openFile(String prefix) throws IOException {
		String fn = outDir + outFile + "_" + prefix + ".html." + outExt;
		fw = new FileWriter(fn, true);
		fw
				.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\""
						+ "\n\"http://www.w3.org/TR/REC-html40/strict.dtd\">"
						+ "\n<META http-equiv=Content-Type content=\"text/html; charset=windows-1251\">\n"
						+ "<html>\n" + "<head>\n" + "<title>" + title
						+ "</title>\n</head>\n<body>\n");
		writeText(title);
		writeText("Гр. хар.: <strong>[" + type + "]</strong>   Файл: " + fn);
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

	String getStatictic() {
		return OALD.VERSION + " [" + type + "] Всего NOUNs/значений: " + cntNoun
				+ "/" + cntMean + " Ошибок: " + cntErr;
	}

	static String getTable1() {
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
				+ OALD.BR
				+ "Слово</th>"
				+ " <th scope=\"col\">Значения слова</th>"
				+ " <th scope=\"col\">Разряд</th>"
				+ " <th scope=\"col\">Континуати 1"
				+ OALD.BR
				+ "[U],[C],[sing],[usually sing]</th>"
				+ " <th scope=\"col\">модель</th>"
				+ " <th scope=\"col\">Консолідати 2"
				+ OALD.BR
				+ "the + слово в ед. числе (нет обозначения)</th>"
				+ " <th scope=\"col\">модель</th>"
				+ " <th scope=\"col\">Мультиплікати	3"
				+ OALD.BR
				+ "[U],[C], отсутствие грамм. пометки</th>"
				+ " <th scope=\"col\">модель</th>"
				+ " <th scope=\"col\">Плюрати 4"
				+ OALD.BR
				+ "[pl],[usually pl],[often pl],[C]</th>"
				+ " <th scope=\"col\">модель</th>"
				+ " <th scope=\"col\">Парсифікат 5"
				+ OALD.BR
				+ "</th>"
				+ " <th scope=\"col\">модель</th>"
				+ " <th scope=\"col\">Конгломерати 6"
				+ OALD.BR
				+ "the + слово во мн. числе (нет обозначения)</th>"
				+ " <th scope=\"col\">модель</th>" + "</tr>\n";
	}

	static String getTable2() {
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

	void writeEntry(Entry entry, String type) throws IOException {
		OALD.display(entry.toStringFull());
		this.type = type;
		if (fw == null) openFile("[" + type + "]_00000");
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
			OALD.displayErr(entry.toString() + ".2: " + entry.getErr());
			color = " bgcolor=\"#FF9933\"";//bgcolor="#FF9933"
			fw.write("<tr" + color + ">\n" + "  <td>" + entry.getFileName()
					+ "</td>\n" + "  <td>" + entry.getWord() + "</td>\n"
					+ "  <td valign=\"top\">" + entry.getType() + "</td>\n" + "  <td>"
					+ entry.getErr() + "</td>\n" + "</tr>\n");
		}
		fw.flush();
		if ((cntNoun + cntErr) % OALD.MAXFILE == 0) {
			closeFile("");
			openFile("[" + type + "]_" + Utils.format('0', (cntNoun + cntErr), 5));
		}
	}

	void writeComment(String msg) throws IOException {
		//logErr(msg);
		fw.write("<!-- " + msg + " -->\n");
	}

	void writeText(String msg) throws IOException {
		fw.write(msg + OALD.BR);
	}

}
