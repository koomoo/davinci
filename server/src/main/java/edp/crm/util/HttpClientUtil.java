package edp.crm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
 
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
 
public class HttpClientUtil {
    private static final String CHARSET = "UTF-8";
 
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);
 
    //刘飞 调用crm的接口需要统一加上product参数
    public static String doGet(String url, Map<String, String> param) throws Exception {
        String resultString = "";
        CloseableHttpResponse response = null;
        try (
                // 创建Httpclient对象
                CloseableHttpClient httpclient = HttpClients.createDefault();
        ) {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
 
                Set<Map.Entry<String, String>> entries = param.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    builder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            URI uri = builder.build();
 
            // 创建http GET请求
            HttpGet httpGet = new HttpGet(uri);
 
            // 执行请求
            response = httpclient.execute(httpGet);
 
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), CHARSET);
            }
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }
        return resultString;
    }
 
    public static String doGet(String url) throws Exception {
        return doGet(url, null);
    }
 
    public static String doPost(String url, Map<String, String> param) {
 
        CloseableHttpResponse response = null;
        String resultString = "";
        try (
                // 创建Httpclient对象
                CloseableHttpClient httpClient = HttpClients.createDefault();
        ) {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建参数列表
            if (param != null) {
                List<NameValuePair> paramList = new ArrayList();
                for (String key : param.keySet()) {
                    paramList.add(new BasicNameValuePair(key, param.get(key)));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), CHARSET);
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }
 
        return resultString;
    }
 
    public static String doPost(String url) {
        return doPost(url, null);
    }
 
    public static String doPostJson(String url, JSONObject jsonObject) {
        return doPostJson(url, jsonObject.toString());
    }
 
    public static String doPostJson(String url, String json) {
 
        CloseableHttpResponse response = null;
        String resultString = "";
        try (
                // 创建Httpclient对象
                CloseableHttpClient httpClient = HttpClients.createDefault();
        ) {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), CHARSET);
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }
 
        return resultString;
    }
 
    /**
     * 通过http访问获取json数据
     *
     * @param targetURL
     * @return
     * @throws IOException
     */
    public static String getJsonString(String targetURL) throws IOException {
        String res = "";
        StringBuilder buffer = new StringBuilder();
        URL url = new URL(targetURL);
        HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
        if (200 == urlCon.getResponseCode()) {
            InputStream is = urlCon.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
 
            String str = null;
            while ((str = br.readLine()) != null) {
                buffer.append(str);
            }
            br.close();
            isr.close();
            is.close();
            res = buffer.toString();
            // res = res.substring(19,res.indexOf(");"));
 
        }
        return res;
    }
 
 
    public static void main(String[] args) {
 
        JSONObject jsonObject = new JSONObject();
 
        jsonObject.put("id", 1000012342378L);
        JSONObject data = new JSONObject();
        data.put("mode", "sync");
        data.put("terminals", 333);
        JSONObject content = new JSONObject();
        content.put("id", "1");
        content.put("l1", "333");
        content.put("l2", "333");
        content.put("l3", "333");
        data.put("content", content);
        jsonObject.put("data", data);
 
 
        System.out.println(jsonObject);
        String s = HttpClientUtil.doPostJson("http://192.168.13.123:9530/uploadById.do", jsonObject);
        System.out.println(s);
 
    }
}