package dummydomain.yetanothercallblocker.sia.network;

import android.os.Build;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import dummydomain.yetanothercallblocker.sia.utils.Utils;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WebService {

    public static class WSResponse {
        private boolean successful;
        private int responseCode;
        private String responseMessage;
        private JSONObject jsonObject;

        public void setSuccessful(boolean successful) {
            this.successful = successful;
        }

        public boolean getSuccessful() {
            return this.successful;
        }

        public void setResponseCode(int i) {
            this.responseCode = i;
        }

        public void setResponseMessage(String str) {
            this.responseMessage = str;
        }

        public String getResponseMessage() {
            return this.responseMessage;
        }

        public void setJsonObject(JSONObject jSONObject) {
            this.jsonObject = jSONObject;
        }

        public final JSONObject getJsonObject() {
            return this.jsonObject;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(WebService.class);

    private static final String GET_DATABASE_URL_PART = "/get-database";
    private static final String GET_REVIEWS_URL_PART = "/get-reviews";

    public static String getGetDatabaseUrlPart() {
        return GET_DATABASE_URL_PART;
    }

    public static String getGetReviewsUrlPart() {
        return GET_REVIEWS_URL_PART;
    }

    public static Response call(String path, Map<String, String> params) {
        LOG.debug("call() started; path={}", path);

        // TODO: retries

        Request request = new Request.Builder().url(addHostname(path)).post(createRequestBody(params)).build();
        try {
            return new OkHttpClient().newCall(request).execute();
        } catch (IOException e) {
            LOG.warn("call()", e);
        }
        return null;
    }

    public static WSResponse callForJson(String path, Map<String, String> params) {
        LOG.debug("callForJson() started; path={}", path);

        try {
            Response response = call(path, params);
            if (response != null) {
                WSResponse wsResponse = new WSResponse();
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String bodyString = body.string();
                        LOG.trace("callForJson() response body: {}", bodyString);
                        try {
                            wsResponse.setJsonObject(new JSONObject(bodyString));
                            JSONObject c = wsResponse.getJsonObject();
                            wsResponse.setSuccessful(c.getBoolean("success"));
                        } catch (Exception e) {
                            LOG.error("callForJson()", e);
                        }
                    }
                } else {
                    LOG.trace("callForJson() response is not successful");
                    wsResponse.setResponseCode(response.code());
                    wsResponse.setResponseMessage(response.message());
                    response.close();
                }
                return wsResponse;
            } else {
                LOG.warn("callForJson() response is null");
            }
        } catch (Exception e) {
            LOG.warn("callForJson()", e);
        }
        return null;
    }

    private static String addHostname(String path) {
        return "https://aapi.shouldianswer.net/srvapp" + path;
    }

    private static RequestBody createRequestBody(Map<String, String> params) {
        if (params == null) params = new HashMap<>();

        params.put("_appId", Utils.getAppId());

        params.put("_device", "dreamlte");
        params.put("_model", "SM-G950F");
        params.put("_manufacturer", "Samsung");
        params.put("_api", String.valueOf(Build.VERSION_CODES.O));

        params.put("_appFamily", "SIA-NEXT");
        params.put("_appVer", String.valueOf(Utils.getAppVersion()));
        params.put("_dbVer", String.valueOf(Utils.getEffectiveDbVersion()));
        params.put("_country", "US"); // TODO: remove hardcode

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        StringBuilder paramsSb = new StringBuilder();
        for (String key : params.keySet()) {
            String val = params.get(key);
            if (val != null) {
                paramsSb.append(val);
                bodyBuilder.addFormDataPart(key, val);
            }
        }

        bodyBuilder.addFormDataPart("_checksum", Utils.md5String(paramsSb.toString() + "saltandmira2"));

        return bodyBuilder.build();
    }

}
