package lingvo;

import java.io.FileWriter;
import java.io.IOException;

public class Writer {

	FileWriter fw = null;

	public String outDir, outFile, outExt, title;

	int cntNoun = 0, cntMean = 0, cntErr = 0, cntWar = 0;

	String type = "";

	public Writer() throws IOException {
		outDir = OALD.outDir;
		outFile = Utils.isBlank(OALD.outFile) ? "" : OALD.outFile + "_";
		outExt = OALD.outExt;
		title = OALD.title;
	}

	void openFile(String prefix) throws IOException {
		// System.out.println("file1="+prefix);
		prefix = prefix.replaceAll("/", "-");
		// prefix = prefix.replaceAll("\\", "-");
		// System.out.println("file2="+prefix);
		String fn = outDir + outFile + prefix + ".html." + outExt;
		fw = new FileWriter(fn, true);
		fw.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\""
				+ "\n\"http://www.w3.org/TR/REC-html40/strict.dtd\">"
				+ "\n<META http-equiv=Content-Type content=\"text/html; charset=windows-1251\">\n"
				+ "<html>\n" + "<head>\n" + "<title>" + title
				+ "</title>\n</head>\n<body>\n");
		writeText(title);
		writeText("��. ���.: <strong>[" + type + "]</strong>   ����: " + fn);
		// fw.write(getTable1());
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
		return "[" + type + "] ����� ����/��������: " + cntNoun + "/" + cntMean
				+ " ������: " + cntErr;
	}

	static String getTable1() {
		/*
		 * Entry
		 * ������
		 * ���������� 1 [U],[C],[sing],[usually sing]
		 * ������
		 * ���������� 2 the + ����� � ��. ����� (��� �����������)
		 * ������
		 * ������������ 3 [U],[C], ���������� �����. �������
		 * ������
		 * ������� 4 [pl],[usually pl],[often pl],[C]
		 * ������
		 * ���������� 5
		 * ������
		 * ������������ 6 the + ����� �� ��. ����� (��� �����������)
		 * ������
		 */
		return "<table width=\"100%\" border=\"1\">" + "<tr>"
				+ " <th scope=\"col\">����"
				+ OALD.BR
				+ "�����</th>"
				+ " <th scope=\"col\">�������� �����</th>"
				+ " <th scope=\"col\">������</th>"
				+ " <th scope=\"col\">���������� 1"
				+ OALD.BR
				+ "[U],[C],[sing],[usually sing]</th>"
				+ " <th scope=\"col\">������</th>"
				+ " <th scope=\"col\">���������� 2"
				+ OALD.BR
				+ "the + ����� � ��. ����� (��� �����������)</th>"
				+ " <th scope=\"col\">������</th>"
				+ " <th scope=\"col\">������������	3"
				+ OALD.BR
				+ "[U],[C], ���������� �����. �������</th>"
				+ " <th scope=\"col\">������</th>"
				+ " <th scope=\"col\">������� 4"
				+ OALD.BR
				+ "[pl],[usually pl],[often pl],[C]</th>"
				+ " <th scope=\"col\">������</th>"
				+ " <th scope=\"col\">���������� 5"
				+ OALD.BR
				+ "</th>"
				+ " <th scope=\"col\">������</th>"
				+ " <th scope=\"col\">������������ 6"
				+ OALD.BR
				+ "the + ����� �� ��. ����� (��� �����������)</th>"
				+ " <th scope=\"col\">������</th>" + "</tr>\n";
	}

	static String getTable2() {
		return "<table width=\"100%\" cellspacing=\"0\"  cellpadding=\"0\" border=\"1\">"
				+ "<tr>"
				+ " <th width=\"5%\">����� (1)</th>"
				+ " <th width=\"5%\">��. ���. ���.(2)</th>"
				+ " <th width=\"5%\">��. ���. ���.(3)</th>"
				+ " <th width=\"2%\">� (4)</th>"
				+ " <th width=\"50%\">�������� (5)</th>"
				+ " <th width=\"25%\"><i>������� (6)</i></th>"
				+ " <th width=\"3%\">���-��� (7)</th>"
				+ " <th width=\"5%\"><FONT COLOR=\"#D0D0D0\">���� (8)</FONT></th>"
				+ "</tr>\n";
	}

	void writeEntry(Entry entry, String group) throws IOException {
		OALD.display(group + ": " + entry.toStringFull());
		this.type = group;
		if (fw == null)
			openFile("[" + group + "]_00000");
		String color = "";
		if (Utils.isBlank(entry.getErr()) && !Entry.GROUPWAR.equals(group)) {
			cntNoun++;
			Object[] poss = (entry.getPosList().keySet()).toArray();
			String row = "<tr" + color + ">\n" + "  <td valign=\"top\">"
					+ entry.getWord();
			for (int i = 0; i < poss.length; i++) {
				row += OALD.BR
						+ poss[i]
						+ "("
						+ (Integer.parseInt((String) entry.getPosList().get(
								poss[i])) + 1) + ")";
			}
			row += "</td>\n"
					+ "  <td valign=\"top\">"
					+ (entry.getGroupList().toString().equals("[]") ? ""
							: entry.getGroupList())
					+ "</td>\n"
					+ "<td colspan=\"5\"><table width=\"100%\" cellspacing=\"0\" border=\"1\">";
			for (int i = 0; i < entry.getMeanings().size(); i++) {
				cntMean++;
				Meaning mean = entry.getMeanings().get(i);
				String ex = "";
				if (!Utils.isBlank(mean.getExample()))
					ex = "<i>" + mean.getExample() + "</i>";
				row += "<tr>"
						+ " <td valign=\"top\" width=\"5.9%\">"
						// + (Entry.isBlank(mean.getTypeList()) ? "" :
						// mean.getTypeList())
						+ (mean.getGroupList().toString().equals("[]") ? ""
								: mean.getGroupList()) + "</td>"
						+ " <td valign=\"top\" width=\"2.3%\">" + mean.getNum()
						+ "</td>" + " <td valign=\"top\" width=\"59%\">"
						+ mean.getMean() + "</td>"
						+ " <td valign=\"top\" width=\"29.5%\">" + ex + "</td>"
						+ " <td valign=\"top\" width=\"3.5%\">"
						+ mean.getCategory() + "</td>";
			}
			row += "</tr>\n</table>\n</td>"
					+ "<td valign=\"top\"><FONT COLOR=\"#D0D0D0\">"
					+ entry.getFileName() + "</FONT></td>\n" + "</tr>\n";
			fw.write(row);
		} else {
			String text;
			if (group.equals(Entry.GROUPERR)) {
				cntErr++;
				text = entry.getErr();
				OALD.displayErr(entry.toString() + ".2: " + entry.getErr());
			} else {
				cntWar++;
				text = entry.warning;
				OALD.displayWar(entry.toString() + ".2: " + entry.warning);
			}
			// color = " bgcolor=\"#FF9933\"";//bgcolor="#FF9933"
			fw.write("<tr"
					+ color
					+ ">\n"
					+ "  <td>"
					+ entry.getWord()
					+ "</td>\n"
					+ "  <td valign=\"top\">"
					+ (entry.getGroupList().toString().equals("[]") ? "" : entry
							.getGroupList()) + "</td>\n" + "<td>" + OALD.BLANK
					+ "</td>" + "<td>" + OALD.BLANK + "</td>" + "<td>"
					+ text + "</td>\n" + "<td>" + OALD.BLANK
					+ "</td>" + "<td>" + OALD.BLANK + "</td>"
					+ "<td valign=\"top\"><FONT COLOR=\"#D0D0D0\">"
					+ entry.getFileName() + "</FONT></td>\n" + "</tr>\n");
		}
		fw.flush();
		if ((cntNoun + cntErr+cntWar) % OALD.MAXFILE == 0) {
			closeFile("");
			openFile("[" + group + "]_"
					+ Utils.format('0', (cntNoun + cntErr+ cntWar), 5));
		}
	}

	void writeComment(String msg) throws IOException {
		// logErr(msg);
		fw.write("<!-- " + msg + " -->\n");
	}

	void writeText(String msg) throws IOException {
		fw.write(msg + OALD.BR);
	}

}
