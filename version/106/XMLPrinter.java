package lingvo;

/* Copyright 2002-2004 Elliotte Rusty Harold

This library is free software; you can redistribute it and/or modify
it under the terms of version 2.1 of the GNU Lesser General Public 
License as published by the Free Software Foundation.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the 
Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
Boston, MA 02111-1307  USA

You can contact Elliotte Rusty Harold by sending e-mail to
elharo@metalab.unc.edu. Please include the word "XOM" in the
subject line. The XOM home page is located at http://www.xom.nu/
*/

//package nu.xom.samples;
import java.io.IOException;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;

/**
* <p>
* Demonstrates parsing a document and outputting
* it again using the <code>toXML</code> method.
* </p>
* 
* @author Elliotte Rusty Harold
* @version 1.0
*
*/
public class XMLPrinter {

	public static void main(String[] args) {

		if (args.length <= 0) {
			System.out.println("Usage: java nu.xom.samples.XMLPrinter URL");
			//return;
		}

		//String folder = "C:\\1\\dict\\oxford\\xml\\";
		String folder2 = "C:\\1\\dict\\oxford\\";
		try {
			Builder parser = new Builder();
			//Document doc = parser.build(args[0]);
			Document doc = parser.build(folder2 + "XU0III4S.701");
			System.out.println("TO XML="+doc.toXML());
			Element root = doc.getRootElement();
			//Element id = root.getFirstChildElement("p:id");
			Element id = root.getFirstChildElement("file-name");
			System.out.println("id=" + id);
			String idv = id.getValue();
			System.out.println("idv=" + idv);
		} catch (ParsingException ex) {
			System.out.println(args[0] + " is not well-formed.");
			System.out.println(ex.getMessage());
		} catch (IOException ex) {
			System.out.println("Due to an IOException, the parser could not print "
					+ args[0]);
		}

	}

}