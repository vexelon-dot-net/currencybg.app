/*
 * CurrencyBG App
 * Copyright (C) 2016 Vexelon.NET Services
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.vexelon.currencybg.app.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

public class XmlUtils {

	/**
	 * Serialize an XML element recursively
	 * 
	 * @param node
	 * @param serializer
	 * @throws IOException
	 */
	private static void serializeXmlElement(Node node, XmlSerializer serializer) throws IOException {

		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node current = children.item(i);

			if (current.getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element) current;
				serializer.startTag("", child.getNodeName());
				serializeXmlElement(child, serializer);
				serializer.endTag("", child.getNodeName());
			} else if (current.getNodeType() == Node.TEXT_NODE) {
				Text child = (Text) current;
				serializer.text(child.getData());
			} else if (current.getNodeType() == Node.CDATA_SECTION_NODE) {
				CDATASection child = (CDATASection) current;
				serializer.cdsect(child.getData());
			} else if (current.getNodeType() == Node.COMMENT_NODE) {
				Comment child = (Comment) current;
				serializer.comment(child.getData());
			}
		}
	}

	/**
	 * Serialize a Root element and all it's descendants
	 * 
	 * @param document
	 *            - org.w3c.dom Xml Document
	 * @param serializer
	 * @throws Exception
	 */
	private static void serializeXml(Document document, XmlSerializer serializer) throws Exception {
		serializer.startDocument("UTF-8", true);
		document.getDocumentElement().normalize();
		serializeXmlElement(document, serializer);
		serializer.endDocument();
	}

	/**
	 * Parse org.w3c.dom Document and serialized to a String using Android
	 * Util.xml
	 * 
	 * @param document
	 *            - org.w3c.dom Xml Document
	 * @return
	 * @throws RuntimeException
	 */
	public static String getXmlDoc(Document document) throws RuntimeException {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter(1024);

		try {
			serializer.setOutput(writer);
			serializeXml(document, serializer);
		} catch (Exception e) {
			throw new RuntimeException("Failed converting Xml to String!", e);
		}

		return writer.toString();
	}

	/**
	 * Parse org.w3c.dom Document and serialized to a file using Android
	 * Util.xml
	 * 
	 * @param document
	 *            - org.w3c.dom Xml Document
	 * @param file
	 * @throws RuntimeException
	 */
	public static void saveXmlDoc(Document document, File file) throws RuntimeException {
		XmlSerializer serializer = Xml.newSerializer();

		try {
			FileWriter writer = new FileWriter(file);
			serializer.setOutput(writer);
			serializeXml(document, serializer);
		} catch (Exception e) {
			throw new RuntimeException("Failed save Xml to " + file.getName(), e);
		}
	}

}
