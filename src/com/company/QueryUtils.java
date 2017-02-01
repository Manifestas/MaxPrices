package com.company;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class QueryUtils {

    private static final String REQUEST_URL = "http://www.parad-shoes.ru/api/products?";
    private static final String MODEL_TAG = "tags%5B%5D=";
    private static final String USER_AGENT = "Mozilla/5.0";

    private QueryUtils() {
    }

    public static String fetchMaxPrice(String model) throws MalformedURLException,
            IOException {
        URL url = createUrl(model);
        String jsonResponse = makeHttpRequest(url);
        String maxPrice = extractMaxPriceFromJson(jsonResponse);
        return maxPrice;
    }

    private static String extractMaxPriceFromJson(String jsonResponse) {
        if (jsonResponse.isEmpty()) {
            return null;
        }
        String maxPrice = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray products = jsonObject.getJSONArray("products");
            //if array is empty - return null;
            if (products.isNull(0)) {
                return maxPrice;
            }
            JSONObject model = products.getJSONObject(0);
            // if key "IS_SALE_PRICE" false - return null
            if (!model.getBoolean("IS_SALE_PRICE")) {
                return maxPrice;
            }
            int maxPriceInt = model.getInt("PRICE");
            maxPrice = String.valueOf(maxPriceInt);
            System.out.println(maxPrice);
        } catch (JSONException e) {
            System.out.println("Problem parsing JSON results" + e);
        }
        return maxPrice;
    }

    private static URL createUrl(String query) throws MalformedURLException{
        URI uri = URI.create(REQUEST_URL + MODEL_TAG + query);
        return uri.toURL();
    }

    private static String makeHttpRequest(URL url) throws IOException{
        String jsonResponse = "";
        //if url is null - return early
        if (url == null) {
            return jsonResponse;
        }
        InputStream inputStream = null;
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);

            //if the request was successful(response code 200),
            //then read the input stream and parse the response.
            if (connection.getResponseCode() == 200) {
                inputStream = connection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                System.out.println("Error response code: " + connection.getResponseCode());
            }
        } catch (IOException e) {
            System.out.println("Problem retrieving json result." + e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                builder.append(line);
                line = reader.readLine();
            }
        }
        return builder.toString();
    }
}
