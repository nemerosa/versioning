package net.nemerosa.versioning.core.support;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class XMLElement {

    private final Element element;

    public XMLElement(Element element) {
        this.element = element;
    }

    public static XMLElement parse(String xml) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document dom = builder.parse(new InputSource(new StringReader(xml)));
            return new XMLElement(dom.getDocumentElement());
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new XMLElementException(ex);
        }
    }

    public XMLElement get(String elementName) {
        NodeList list = element.getElementsByTagName(elementName);
        if (list.getLength() > 0) {
            Element e = (Element) list.item(0);
            return new XMLElement(e);
        } else {
            throw new XMLElementNotFoundException(elementName);
        }
    }

    public String getText() {
        return element.getTextContent();
    }

    public String getAttribute(String name) {
        Attr attr = element.getAttributeNode(name);
        if (attr != null) {
            return attr.getValue();
        } else {
            throw new XMLElementNotFoundException(name);
        }
    }

    public List<XMLElement> getList(String elementName) {
        List<XMLElement> children = new ArrayList<>();
        NodeList list = element.getElementsByTagName(elementName);
        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            children.add(new XMLElement((Element) list.item(i)));
        }
        return children;
    }
}
