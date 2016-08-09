package com.inmobi.datapopulator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by oozie on 8/9/16.
 */
public class DataPopulator {

    public static void main(String[] args) {

        getAvailability("http://dp2001.grid.uh1.inmobi.com:8080/firstapp/api/request/hourly/today");
        getAvailability("http://dp2001.grid.uh1.inmobi.com:8080/firstapp/api/request/hourly/yesterday");
    }

    private static void getAvailability(String urlString) {

        try {

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() +
                        conn.getResponseMessage());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String response = "";
            while ((output = br.readLine()) != null) {
                response += output;
            }

            Type listType = new TypeToken<ArrayList<ModifiedResult>>(){}.getType();
            List<ModifiedResult> modifiedList = new Gson().fromJson (response, listType);
            conn.disconnect();

            for (ModifiedResult result : modifiedList) {
                buildHTML(result.getDate(), result.getVertica(), result.getAvailability());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void buildHTML(String date, String measure, String availability) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            System.out.println(date);
            Date dt = formatter.parse(date);
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            String modifiedDate = formatter.format(dt);

            if (measure.equals("")) {
                measure = "2";
            }
            int category = 2;
            if (!availability.equals("NA")) {
                double percentage = Double.parseDouble(availability.substring(0, availability.length()-1));
                if (percentage < 95d) {
                    category = 2;
                } else if (percentage < 99.5d) {
                    category = 1;
                } else {
                    category = 0;
                }
            } else {
                availability = "0%";
            }


            String htmlString = "{\"info_Request Measures\":\"History :http://track.corp.inmobi.com/bender/dash/175 <br> <div><h4></h4></div> <br> <div> <table border=\\\"1\\\" width=\\\"700\\\" CELLSPACING=\\\"1\\\" CELLPADDING=\\\"3\\\"> <caption style=\\\"text-align:center\\\"><b>Audit vs Vertica</b></caption> <tr> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Measure</td> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Vertica</td> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Availability</td> </tr> <tr> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">Request Count</td> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">" + measure + "</td> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">"+ availability +"</td> </tr> </table> </div>\", \"x\":\""+ modifiedDate +"\", \"Request Measures\":" + category + "}";
            sendData(htmlString);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private static void sendData(String input) {

        try {

            Client client = Client.create();
            WebResource webResource = client.resource("http://track.corp.inmobi.com/bender/dash/175");

            MultivaluedMap formData = new MultivaluedMapImpl();
            formData.add("log", input);

            ClientResponse response = webResource.type("application/x-www-form-urlencoded")
                    .post(ClientResponse.class, formData);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            System.out.println("Output from Server .... \n");
            String output = response.getEntity(String.class);
            System.out.println(output);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static class ModifiedResult {
        String date;
        String vertica;
        String availability;

        public String getDate() {
            return date;
        }

        public String getVertica() {
            return vertica;
        }

        public String getAvailability() {
            return availability;
        }

        public ModifiedResult(String date, String vertica, String availability) {
            this.date = date;
            this.vertica = vertica;
            this.availability = availability;
        }
    }

}
