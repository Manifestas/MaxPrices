package com.company;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class QueryUtils {

    private static final String REQUEST_URL = "http://www.parad-shoes.ru/api/products?";
    private static final String MODEL_TAG = "tags%5B%5D=";
    private static final String USER_AGENT = "Mozilla/5.0";

    private String article;
    private MessageListener messageListener;

    public QueryUtils(MessageListener listener) {
        messageListener = listener;
    }

    public String fetchMaxPrice(String model) {
        this.article = model;
        URL url = createUrl(model);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            messageListener.onMessage("Проблема с HTTP запросом. " + e);
        }
        return extractMaxPriceFromJson(jsonResponse);
    }

    private String extractMaxPriceFromJson(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            return null;
        }
        String maxPrice = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray products = jsonObject.getJSONArray("products");
            //if array is empty - return null;
            if (products.isNull(0)) {
                messageListener.onMessage(article + " не найдено!");
                return null;
            }
            for (int i = 0; i < products.length(); ++i) {
                JSONObject model = products.getJSONObject(i);
                JSONObject properties = model.getJSONObject("PROPERTIES");
                JSONObject articul = properties.getJSONObject("ARTICUL");
                String propertiesModel = articul.getString("value");
                // убрать эскейп символ
                propertiesModel = propertiesModel.replace("\\", "");
                if (article.equals(propertiesModel)) {
                    // if key "IS_SALE_PRICE" false - return null
                    if (!model.getBoolean("IS_SALE_PRICE")) {
                        messageListener.onMessage(article + " не распродажная!");
                        return null;
                    }
                    int maxPriceInt = model.getInt("PRICE");
                    maxPrice = String.valueOf(maxPriceInt);
                }
            }
        } catch (JSONException e) {
            messageListener.onMessage("Проблема с парсингом JSON");
        }
        if (maxPrice == null) {
            messageListener.onMessage(article + " не найдено.");
        }
        return maxPrice;
    }

    private URL createUrl(String query) {
        URL url = null;
        try {
            // для русских букв в названии
            String utf8Query = URLEncoder.encode(query, "UTF-8");
            URI uri = URI.create(REQUEST_URL + MODEL_TAG + utf8Query);
            url = uri.toURL();
        } catch (Exception e) {
            messageListener.onMessage("Не удалось создать URL для " + query);
        }
        return url;
    }

    private String makeHttpRequest(URL url) throws IOException {
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
                messageListener.onMessage("Код ответа сервера: " + connection.getResponseCode());
            }
        } catch (IOException e) {
            messageListener.onMessage("Не удается получить JSON ответ от сервера. " + e);
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

    private String readFromStream(InputStream inputStream) throws IOException{
        StringBuilder builder = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
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
