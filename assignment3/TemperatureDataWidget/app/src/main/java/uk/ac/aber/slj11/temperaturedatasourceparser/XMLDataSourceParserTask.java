package uk.ac.aber.slj11.temperaturedatasourceparser;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by samuel on 31/03/16.
 */

public class XMLDataSourceParserTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... urls) {
        String xml = null;
        for (String url : urls) {
            xml = getXmlFromUrl(url);
        }
        return xml;
    }

    @Override
    protected void onPostExecute(String xml) {

        XMLDataSourceParser parser = new XMLDataSourceParser();
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        Document doc = parser.getDocument(stream);

//        NodeList nodeList = doc.getElementsByTagName(NODE_EMP);

//        employees = new ArrayList<Employee>();
//        Employee employee = null;
//        for (int i = 0; i < nodeList.getLength(); i++) {
//            employee = new Employee();
//            Element e = (Element) nodeList.item(i);
//            employee.setId(Integer.parseInt(e.getAttribute(ATTR_ID)));
//            employee.setName(parser.getValue(e, NODE_NAME));
//            employee.setDepartment(parser.getValue(e, NODE_DEPT));
//            employee.setType(parser.getValue(e, NODE_TYPE));
//            employee.setEmail(parser.getValue(e, NODE_EMAIL));
//            employees.add(employee);
//        }
//
//        listViewAdapter = new CustomListViewAdapter(context, employees);
//        listView.setAdapter(listViewAdapter);
    }

    /* uses HttpURLConnection to make Http request from Android to download
     the XML file */
    private String getXmlFromUrl(String urlString) {
        StringBuffer output = new StringBuffer("");

        InputStream stream = null;
        URL url;
        try {
            url = new URL(urlString);
            URLConnection connection = url.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null)
                    output.append(s);
            }
        } catch (MalformedURLException e) {
            Log.e("Error", "Unable to parse URL", e);
        } catch (IOException e) {
            Log.e("Error", "IO Exception", e);
        }

        return output.toString();
    }
}