package com.example.restomind;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Scanner;

public class Calendar {

    public class RestaurantClock {

        // return the correct business date (4 am will still count as yesterday business date)
        public static LocalDate getBusinessDate() {
            LocalDateTime now = LocalDateTime.now();
            // if the time right now is lower than 4 am then return the date yesterday
            if (now.toLocalTime().isBefore(LocalTime.of(4, 0))) {
                return now.toLocalDate().minusDays(1);
            }
            // else return today's date
            return now.toLocalDate();
        }
    }

    // gets today holiday or regular day
    public static String getTodayHoliday() {
        // put on try because it is external api
        try {
            LocalDate businessDate = RestaurantClock.getBusinessDate();
            //LocalDate testDate = LocalDate.of(2026, 4, 2); // test holiday days

            // url for api
            // v=1: version 1, cfg=json: takes only data, maj=on: major holidays, start/end: today
            String urlString = "https://www.hebcal.com/hebcal?v=1&cfg=json&maj=on&start="
                    + businessDate + "&end=" + businessDate;

            // string to url object, open a connection, the request is to get info, and connect with hebcal
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            // checks if we got a response, if not return regular day
            if (conn.getResponseCode() != 200) {
                return "REGULAR";
            }

            // the info will arrive from the url to scanner(url.openStream())
            Scanner scanner = new Scanner(url.openStream());
            // reads the info in the scanner to our StringBuilder response to append everything
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            // close the stream of info from the url
            scanner.close();

            String jsonResponse = response.toString();

            //example: {"title": "Hebcal April 2026", "items": [{"title": "Passover 2026", "date": "2026-04-02"}]}
            // checks if holiday today
            if (jsonResponse.contains("\"items\":[]")) {
                return "REGULAR";
            }

            // identify holiday
            if (jsonResponse.contains("Passover")) return "PASSOVER";
            if (jsonResponse.contains("Sukkot")) return "SUKKOT";
            if (jsonResponse.contains("Shavuot")) return "SHAVUOT";
            if (jsonResponse.contains("Rosh Hashana")) return "ROSH_HASHANAH";
            if (jsonResponse.contains("Chanukah")) return "CHANUKAH";
            if (jsonResponse.contains("Yom HaAtzma'ut")) return "Yom_HaAtzma'ut";

            return "HOLIDAY"; // default for small holidays

        // error while trying to get information, return error
        } catch (Exception e) {
            System.out.println("Error, Default REGULAR.");
            return "REGULAR";
        }
    }
}
