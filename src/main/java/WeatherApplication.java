import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class WeatherApplication {
    // Retrieve weather data for a location using coordinates
    public static JSONObject getWeatherData(String locationName) {
        JSONArray locationData = getLocationData(locationName);

        if (locationData == null || locationData.isEmpty()) {
            System.out.println("Error: Location data is missing or invalid.");
            return null;
        }

        // Extract latitude and longitude
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // Build API request URL
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude="+ latitude +"&longitude=" + longitude +
                "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=America%2FLos_Angeles&start_date=2024-10-13&end_date=2024-10-20";

        try {
            // Fetch API response
            HttpURLConnection conn = fetchApiResponse(urlString);

            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API.");
                return null;
            }

            // Read and store the API response
            StringBuilder resultJson = new StringBuilder();
            Scanner sc = new Scanner(conn.getInputStream());
            while (sc.hasNext()) {
                resultJson.append(sc.nextLine());
            }
            sc.close();
            conn.disconnect();

            // Parse the JSON response
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // Retrieve hourly data
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            // Check if hourly data is missing
            if (hourly == null) {
                System.out.println("Error: Hourly data is missing.");
                return null;
            }

            // Retrieve time array and find the current hour's index
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // Retrieve temperature
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            if (temperatureData == null) {
                System.out.println("Error: 'temperature_2m' data is missing.");
                return null;
            }
            double temperature = ((Number) temperatureData.get(index)).doubleValue();

            // Retrieve weather code and convert it to weather condition
            JSONArray weatherCodeData = (JSONArray) hourly.get("weather_code");
            if (weatherCodeData == null) {
                System.out.println("Error: 'weather_code' data is missing.");
                return null;
            }
            long weatherCode = ((Number) weatherCodeData.get(index)).longValue();
            String weatherCondition = convertWeatherCode(weatherCode);

            // Retrieve humidity
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            if (relativeHumidity == null) {
                System.out.println("Error: 'relative_humidity_2m' data is missing.");
                return null;
            }
            long humidity = ((Number) relativeHumidity.get(index)).longValue();

            // Retrieve windspeed
            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            if (windspeedData == null) {
                System.out.println("Error: 'wind_speed_10m' data is missing.");
                return null;
            }
            double windspeed = ((Number) windspeedData.get(index)).doubleValue();

            // Build weather data JSON object
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Retrieves geographical coordinates for a given location name
    public static JSONArray getLocationData(String locationName) {
        locationName = locationName.replaceAll(" ", "+");
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API.");
                return null;
            }

            // Read and store the API response
            StringBuilder resultJson = new StringBuilder();
            Scanner sc = new Scanner(conn.getInputStream());
            while (sc.hasNext()) {
                resultJson.append(sc.nextLine());
            }
            sc.close();
            conn.disconnect();

            // Parse the JSON response
            JSONParser parse = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parse.parse(String.valueOf(resultJson));

            // Get the list of location data
            return (JSONArray) resultsJsonObj.get("results");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Helper method to fetch API response
    private static HttpURLConnection fetchApiResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        return conn;
    }

    // Find the index of the current time in the time list
    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {
                return i;
            }
        }

        return 0;
    }

    // Get the current time formatted as the API expects
    public static String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
        return currentDateTime.format(formatter);
    }

    // Convert the weather code to a human-readable weather condition
    private static String convertWeatherCode(long weatherCode) {
        if (weatherCode == 0L) {
            return "Clear";
        } else if (weatherCode > 0L && weatherCode <= 3L) {
            return "Cloudy";
        } else if ((weatherCode >= 51L && weatherCode <= 67L) ||
                (weatherCode >= 80L && weatherCode <= 99L)) {
            return "Rain";
        } else if (weatherCode >= 71L && weatherCode <= 77L) {
            return "Snow";
        }
        return "Unknown";
    }
}
