/*
 * @(#)DomEcho04.java   1.9 98/11/10
 *
 * Copyright (c) 1998 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */
package lingvo;

import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DomEcho04 extends JPanel {
	// Global value so it can be ref'd by the tree-adapter
	static Document document;

	static int cnt = 0;

	public static void main(String argv[]) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//factory.setValidating(true);   
		//factory.setNamespaceAware(true);
		//String folder2 = "C:\\1\\dict\\oxford\\";
		String folder2 = "G:\\dict\\oxford\\";
		try {
			String fn16 = "entry31758.xm0";
			String fn = "entry31758.xml";
			//*
			String utf8 = new String(FileUtils.getBuffer(folder2 + fn16),
					OALD.UTF16CODEPAGE);
			System.out.println("utf8=" + utf8);
			FileUtils.setBuffer(folder2 + fn, utf8.getBytes(OALD.UTF8CODEPAGE));
			//if (true) return;
			//*/
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new File(folder2 + fn));
			NodeList list = document.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node n = list.item(i);
				print(0, n);
			}
			System.out.println("total: " + cnt);
			//System.out.println("doc="+document);
			//System.out.println("nodes="+document.getChildNodes());

		} catch (SAXException sxe) {
			// Error generated during parsing)
			Exception x = sxe;
			if (sxe.getException() != null) x = sxe.getException();
			x.printStackTrace();

		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			pce.printStackTrace();

		} catch (IOException ioe) {
			// I/O error
			ioe.printStackTrace();
		}
	} // main

	public static void print(int level, Node n) {
		if (n == null) return;
		String prefix = "";
		for (int l = 0; l < level; l++) {
			prefix += ' ';
		}
		cnt++;
		System.out.println(prefix + "nodeName=" + n.getNodeName() + "="
				+ n.getNodeValue() + "= nodeType=" + n.getNodeType());
		if (n.hasAttributes()) {
			NamedNodeMap attributes = n.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attr = attributes.item(i);
				System.out.println(prefix + "  Attribute=" + attr.getNodeName() + "="
						+ attr.getNodeValue() + '=');
			}
		}
		if (n.hasChildNodes()) {
			NodeList list = n.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node nn = list.item(i);
				print(level++, nn);
			}
		}
	}
}
