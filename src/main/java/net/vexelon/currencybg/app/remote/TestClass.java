package net.vexelon.currencybg.app.remote;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;


import com.google.gson.reflect.TypeToken;

import net.vexelon.currencybg.app.Defs;
import net.vexelon.currencybg.app.db.models.CurrencyDataNew;

public class TestClass {
//    OkHttpClient client = new OkHttpClient();
//    // code request code here
//    String doGetRequest(String url) throws IOException {
//        Request request = new Request.Builder()
//                .url(url)
//                .header("APIKey","CurrencyBgUser")
//                .build();
//
//        Response response = client.newCall(request).execute();
//        return response.body().string();
//    }
//
//    // post request code here
//
//    public static final MediaType JSON
//            = MediaType.parse("application/json; charset=utf-8");
//
//    // test data
//    String bowlingJson(String player1, String player2) {
//        return "{'winCondition':'HIGH_SCORE',"
//                + "'name':'Bowling',"
//                + "'round':4,"
//                + "'lastSaved':1367702411696,"
//                + "'dateStarted':1367702378785,"
//                + "'players':["
//                + "{'name':'" + player1 + "','history':[10,8,6,7,8],'color':-13388315,'total':39},"
//                + "{'name':'" + player2 + "','history':[6,10,5,10,10],'color':-48060,'total':41}"
//                + "]}";
//    }
//
//    String doPostRequest(String url, String json) throws IOException {
//        RequestBody body = RequestBody.create(JSON, json);
//        Request request = new Request.Builder()
//                .url(url)
//                .post(body)
//                .build();
//        Response response = client.newCall(request).execute();
//        return response.body().string();
//    }

    public static void main(String[] args) throws IOException {

        // issue the Get request
//        TestClass example = new TestClass();
//        String getResponse = example.doGetRequest(/*"http://www.vogella.com"*/"http://localhost:8080/currencybg.server/api/currencies/today/2016-07-16T20:55:06+0300/300");
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
//                .url("http://localhost:8080/currencybg.server/api/currencies/today/2016-07-16T20:55:06+0300/300")
                .url("http://currencybg-tsvetoslav.rhcloud.com/currencybg.server/api/currencies/2016-08-31T20:55:06+0300")
                .header("APIKey","CurrencyBgUser")
                .build();

        Response response = client.newCall(request).execute();
        String getResponse = response.body().string();

        System.out.println(getResponse);

        Gson gson = new GsonBuilder().setDateFormat(Defs.DATEFORMAT_ISO_8601).create();
        Type type = new TypeToken<List<CurrencyDataNew>>() {
        }.getType();

        //TODO - to be set Authentication information
        List<CurrencyDataNew> currencies = gson.fromJson(getResponse, type);
        for(CurrencyDataNew currency : currencies){
            System.out.println(currency.getCode());
        }

        // issue the post request

//        String json = example.bowlingJson("Jesse", "Jake");
//        String postResponse = example.doPostRequest("http://www.roundsapp.com/post", json);
//        System.out.println(postResponse);
    }
}
