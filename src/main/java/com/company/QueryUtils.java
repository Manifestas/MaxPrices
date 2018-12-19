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

    private Gui.TextAreaLog log;
    private String article;

    public QueryUtils(Gui.TextAreaLog log) {
        this.log = log;
    }

    public String fetchMaxPrice(String model) {
        this.article = model;
        URL url = createUrl(model);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            log.textAppend("Проблема с HTTP запросом. " + e);
        }
        String maxPrice = extractMaxPriceFromJson(jsonResponse);
        return maxPrice;
    }

    private String extractMaxPriceFromJson(String jsonResponse) {
        if (jsonResponse.isEmpty()) {
            return null;
        }
        String maxPrice = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray products = jsonObject.getJSONArray("products");
            //if array is empty - return null;
            if (products.isNull(0)) {
                log.textAppend(article + " не найдено!");
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
                        log.textAppend(article + " не распродажная!");
                        return null;
                    }
                    int maxPriceInt = model.getInt("PRICE");
                    maxPrice = String.valueOf(maxPriceInt);
                }
            }
        } catch (JSONException e) {
            log.textAppend("Проблема с парсингом JSON");
        }
        return maxPrice;
    }

    private URL createUrl(String query) {
        URL url = null;
        URI uri = URI.create(REQUEST_URL + MODEL_TAG + query);
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            log.textAppend("Не удалось создать URL");
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
                log.textAppend("Код ответа сервера: " + connection.getResponseCode());
            }
        } catch (IOException e) {
            log.textAppend("Не удается получить JSON ответ от сервера. " + e);
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
