package com.inmobi.datapopulator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by narayan.periwal on 8/9/16.
 */
public class DataPopulator {

    public static void main(String[] args) {

        //getAvailability("http://dp2001.grid.uh1.inmobi.com:8080/firstapp/api/hourly/day?date=2016-10-03");
        getAvailability("http://dp2001.grid.uh1.inmobi.com:8080/firstapp/api/hourly/today");
        getAvailability("http://dp2001.grid.uh1.inmobi.com:8080/firstapp/api/hourly/yesterday");
    }

    private static void getAvailability(String urlString) {

        try {

            Client client = Client.create();
            WebResource webResource = client.resource(urlString);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            String output = response.getEntity(String.class);

            Type listType = new TypeToken<ArrayList<ModifiedResult>>(){}.getType();
            List<ModifiedResult> modifiedList = new Gson().fromJson (output, listType);

            for (ModifiedResult result : modifiedList) {
                System.out.println(result);
                buildHTML(result.getDate(), result.getVerticaRequest(), result.getRequestAvailability(),
                        result.getVerticaClick(), result.getClickAvailability(),
                        result.getVerticaRender(), result.getRenderAvailability());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void buildHTML(String date, String m1, String a1, String m2, String a2, String m3, String a3) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            System.out.println(date);
            Date dt = formatter.parse(date);
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            String modifiedDate = formatter.format(dt);

            m1 = modify(m1);
            m2 = modify(m2);
            m3 = modify(m3);

            double category1 = 0;
            if (!a1.equals("NA")) {
                category1 = calculateCategory(a1);
            } else {
                a1 = "0%";
            }
            double category2 = 0;
            if (!a2.equals("NA")) {
                category2 = calculateCategory(a2);
            } else {
                a2 = "0%";
            }
            double category3 = 0;
            if (!a3.equals("NA")) {
                category3 = calculateCategory(a3);
            } else {
                a3 = "0%";
            }

            String htmlString = "{\"info_Request Measures\":\"History :http://track.corp.inmobi.com/bender/dash/175 <br> <div><h4></h4></div> <br> <div> <table border=\\\"1\\\" width=\\\"700\\\" CELLSPACING=\\\"1\\\" CELLPADDING=\\\"3\\\"> <caption style=\\\"text-align:center\\\"><b>Audit vs Vertica</b></caption> <tr> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Measure</td> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Vertica</td> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Availability</td> </tr> <tr> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">Request Count</td> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">" + m1 + "</td> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">"+ a1 +"</td> </tr> </table> </div>\", \"x\":\""+ modifiedDate +"\", \"Request Measures\":" + category1 + "}";
            System.out.println(htmlString);
            sendData(htmlString);

            htmlString = "{\"info_Click Measures\":\"History :http://track.corp.inmobi.com/bender/dash/175 <br> <div><h4></h4></div> <br> <div> <table border=\\\"1\\\" width=\\\"700\\\" CELLSPACING=\\\"1\\\" CELLPADDING=\\\"3\\\"> <caption style=\\\"text-align:center\\\"><b>Audit vs Vertica</b></caption> <tr> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Measure</td> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Vertica</td> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Availability</td> </tr> <tr> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">Click Count</td> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">" + m2 + "</td> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">"+ a2 +"</td> </tr> </table> </div>\", \"x\":\""+ modifiedDate +"\", \"Click Measures\":" + category2 + "}";
            //System.out.println(htmlString);
            sendData(htmlString);

            htmlString = "{\"info_Render Measures\":\"History :http://track.corp.inmobi.com/bender/dash/175 <br> <div><h4></h4></div> <br> <div> <table border=\\\"1\\\" width=\\\"700\\\" CELLSPACING=\\\"1\\\" CELLPADDING=\\\"3\\\"> <caption style=\\\"text-align:center\\\"><b>Audit vs Vertica</b></caption> <tr> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Measure</td> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Vertica</td> <td bgcolor=\\\"#3399FF \\\" align=\\\"center\\\">Availability</td> </tr> <tr> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">Render Count</td> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">" + m3 + "</td> <td bgcolor=\\\"#FFFF33 \\\" align=\\\"center\\\">"+ a3 +"</td> </tr> </table> </div>\", \"x\":\""+ modifiedDate +"\", \"Render Measures\":" + category3 + "}";
            //System.out.println(htmlString);
            sendData(htmlString);


        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private static String modify(String input) {
        if (input.equals("")) {
            return "0";
        } else {
            return input;
        }
    }

    private static double calculateCategory(String input) {
        double percentage = Double.parseDouble(input.substring(0, input.length()-1));
        return -1*percentage;
        /*if (percentage < 95d) {
            return  2;
        } else if (percentage < 99.5d) {
            return  1;
        } else {
             return  0;
        }*/
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

            System.out.println("Output from Server .... ");
            String output = response.getEntity(String.class);
            System.out.println(output);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static class ModifiedResult {
        String date;
        String verticaRequest;
        String verticaClick;
        String verticaRender;
        String verticaBilling;
        String requestAvailability;
        String clickAvailability;
        String renderAvailability;
        String billingAvailability;

        public String getDate() {
            return date;
        }

        public String getVerticaRequest() {
            return verticaRequest;
        }

        public String getVerticaClick() {
            return verticaClick;
        }

        public String getVerticaRender() {
            return verticaRender;
        }

        public String getVerticaBilling() {
            return verticaBilling;
        }

        public String getRequestAvailability() {
            return requestAvailability;
        }

        public String getClickAvailability() {
            return clickAvailability;
        }

        public String getRenderAvailability() {
            return renderAvailability;
        }

        public String getBillingAvailability() {
            return billingAvailability;
        }

    }
}
