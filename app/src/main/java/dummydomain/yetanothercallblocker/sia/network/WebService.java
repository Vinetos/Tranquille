package dummydomain.yetanothercallblocker.sia.network;

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

    public interface WSParameterProvider {
        String getAppId();

        String getDevice();
        String getModel();
        String getManufacturer();
        int getAndroidApi();

        String getAppFamily();
        int getAppVersion();
        int getDbVersion();
        String getCountry();
    }

    public static abstract class DefaultWSParameterProvider implements WSParameterProvider {
        @Override
        public String getDevice() {
            return "dreamlte";
        }

        @Override
        public String getModel() {
            return "SM-G950F";
        }

        @Override
        public String getManufacturer() {
            return "Samsung";
        }

        @Override
        public int getAndroidApi() {
            return 26;
        }

        @Override
        public String getAppFamily() {
            return "SIA-NEXT";
        }

        @Override
        public String getCountry() {
            return "US";
        }
    }

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

    private final WSParameterProvider parameterProvider;

    public WebService(WSParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider;
    }

    public String getGetDatabaseUrlPart() {
        return GET_DATABASE_URL_PART;
    }

    public String getGetReviewsUrlPart() {
        return GET_REVIEWS_URL_PART;
    }

    public Response call(String path, Map<String, String> params) {
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

    public WSResponse callForJson(String path, Map<String, String> params) {
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

    private String addHostname(String path) {
        return "https://aapi.shouldianswer.net/srvapp" + path;
    }

    private RequestBody createRequestBody(Map<String, String> params) {
        if (params == null) params = new HashMap<>();

        params.put("_appId", parameterProvider.getAppId());

        params.put("_device", parameterProvider.getDevice());
        params.put("_model", parameterProvider.getModel());
        params.put("_manufacturer", parameterProvider.getManufacturer());
        params.put("_api", String.valueOf(parameterProvider.getAndroidApi()));

        params.put("_appFamily", parameterProvider.getAppFamily());
        params.put("_appVer", String.valueOf(parameterProvider.getAppVersion()));
        params.put("_dbVer", String.valueOf(parameterProvider.getDbVersion()));
        params.put("_country", parameterProvider.getCountry());

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
