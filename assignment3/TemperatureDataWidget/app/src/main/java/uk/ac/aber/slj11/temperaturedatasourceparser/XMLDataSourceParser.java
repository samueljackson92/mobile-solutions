package uk.ac.aber.slj11.temperaturedatasourceparser; /**
 * Created by samuel on 31/03/16.
 */
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import android.util.Log;

import uk.ac.aber.slj11.temperaturedata.model.TemperatureData;
import uk.ac.aber.slj11.temperaturedata.model.TemperatureReading;

public class XMLDataSourceParser {
    // Returns the entire XML document
    public Document getDocument(InputStream inputStream) {
        Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(inputStream);
            document = db.parse(inputSource);
        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage(), e);
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage(), e);
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage(), e);
            return null;
        }
        return document;
    }

    public TemperatureData parseDataSource(Document doc) {
        NodeList nodeList = doc.getDocumentElement().getChildNodes();
        Date currentTime = new Date();
        ArrayList<TemperatureReading> readings = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            // check if this node is an element
            if (node instanceof Element) {
                Element element = (Element) node;

                // choose what type of element we're parsing
                if (element.getTagName().equals("currentTime")) {
                    currentTime = parseCurrentTime(element);
                } else if (element.getTagName().equals("reading")) {
                    TemperatureReading reading = parseTemperatureReading(element);
                    readings.add(reading);
                }
            }

        }

        TemperatureData data = new TemperatureData(currentTime, readings);
        return data;
    }

    private Date parseCurrentTime(Element element) {
        DateFormat df = new SimpleDateFormat("HH:mm");
        Date currentTime = null;

        try {
            currentTime = df.parse(element.getTextContent());
        } catch (ParseException ex) {
            Log.e(this.getClass().toString(), "Could not parse time string" + element.getTextContent(), ex);
        }

        return currentTime;
    }

    private TemperatureReading parseTemperatureReading(Element element) {
        int hour = Integer.parseInt(element.getAttribute("hour"));
        int minute = Integer.parseInt(element.getAttribute("min"));
        double temp = Double.parseDouble(element.getAttribute("temp"));
        TemperatureReading reading = new TemperatureReading(hour, minute, temp);
        return reading;
    }

    public String getValue(Element item, String name) {
        NodeList nodes = item.getElementsByTagName(name);
        return this.getTextNodeValue(nodes.item(0));
    }

    private final String getTextNodeValue(Node node) {
        Node child;
        if (node != null) {
            if (node.hasChildNodes()) {
                child = node.getFirstChild();
                while (child != null) {
                    if (child.getNodeType() == Node.TEXT_NODE) {
                        return child.getNodeValue();
                    }
                    child = child.getNextSibling();
                }
            }
        }
        return "";
    }
}