package uk.ac.aber.slj11.temperaturedata.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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

/** XML Data Source Parser
 *
 * A collection of methods that can be used to parse a temperature data source in XML
 * format to generate a temperature data object
 *
 * Created by samuel on 31/03/16.
 */
public class XMLDataSourceParser {

    /** Get a parseable document from the XML data represented as a raw string
     *
     * @param xmlData the XML data as a raw string
     * @return a document from the XML data
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Document getDocument(String xmlData) throws IOException, SAXException, ParserConfigurationException {
        InputStream stream = new ByteArrayInputStream(xmlData.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(stream);
        Document document = documentBuilder.parse(inputSource);
        return document;
    }

    /** Get a temperature data object from a Document object
     *
     * @param doc the document object representing the loaded data source
     * @return a TemperatureData object with data from the data source
     */
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

    /** Get the raw XML as a string from a given URL
     *
     * @param urlString the string representing the URL to load
     * @return the raw XML loaded from the URL as a String
     * @throws IOException if a response could not be loaded from the URL
     */
    public String getXmlFromUrl(String urlString) throws IOException {
        StringBuffer output = new StringBuffer("");

        // start up a connection
        InputStream stream;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setRequestMethod("GET");
        httpConnection.connect();

        // check the response is ok and parse it
        if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            stream = httpConnection.getInputStream();
            BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(stream));
            String s = "";
            while ((s = buffer.readLine()) != null)
                output.append(s);
        } else {
            throw new IOException("Could not load data.");
        }
        return output.toString();
    }

    /** Helper method to parse an element which should be converted to a Date object
     *
     * @param element the raw Document element to be converted to a Date
     * @return a Date object representing this particular element
     */
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

    /** Parses a single temperature reading from a Document element
     *
     * @param element to convert to a temperature reading
     * @return a TemperatureReading object for the element
     */
    private TemperatureReading parseTemperatureReading(Element element) {
        int hour = Integer.parseInt(element.getAttribute("hour"));
        int minute = Integer.parseInt(element.getAttribute("min"));
        double temp = Double.parseDouble(element.getAttribute("temp"));
        TemperatureReading reading = new TemperatureReading(hour, minute, temp);
        return reading;
    }

}