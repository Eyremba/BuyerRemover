package com.joachimvandersmissen;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Joachim Vandersmissen (joachimvandersmissen@gmail.com)
 * @since 6/01/17
 */
public class BuyerRemover {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("<input type=\"hidden\" name=\"_xfToken\" value=\"([^\"]+)\"/>");
    private static final List<String> ALLOWED_COOKIES = Arrays.asList("xf_session", "xf_user", "__cfduid");

    protected final String url;
    protected final String username;
    protected final String password;
    protected final Map<String, String> cookies = new HashMap<>();
    protected String xfToken;

    public BuyerRemover(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void login() throws IOException {
        // Retrieving cookies
        System.out.println("Retrieving cookies for " + this.url + "...");
        this.get("/", false);

        // Logging in
        System.out.println("Logging in " + this.username + " to " + this.url + "...");
        Map<String, String> payload = new HashMap<>();
        payload.put("login", this.username);
        payload.put("register", "0");
        payload.put("password", this.password);
        payload.put("remember", "1");
        payload.put("cookie_check", "1");
        payload.put("_xfToken", "");
        this.post("/login/login", payload, false);

        // Retrieving XF token
        System.out.println("Retrieving XF Token for " + this.username + "...");
        this.get("/", true).stream().map(TOKEN_PATTERN::matcher).filter(Matcher::matches).map(m -> m.group(1)).findFirst().ifPresent(t -> this.xfToken = t);
    }

    public void removeBuyer(int resourceId, int buyerId) throws IOException {
        // Removing buyer
        System.out.println("Removing buyer " + buyerId + " from resource " + resourceId + "...");
        Map<String, String> payload = new HashMap<>();
        payload.put("user_id", String.valueOf(buyerId));
        payload.put("_xfToken", this.xfToken);
        payload.put("_xfConfirm", "1");
        this.post("/resources/" + resourceId + "/delete-buyer/", payload, true);
    }

    protected String serializeCookies() {
        StringBuilder stringBuilder = new StringBuilder();
        this.cookies.forEach((k, v) -> stringBuilder.append(k).append('=').append(v).append("; "));
        return stringBuilder.toString();
    }

    protected void deserializeCookies(List<String> cookies) {
        if (cookies == null) return;

        for (String cookie : cookies) {
            for (String s : cookie.split(";")) {
                if (s.contains("=")) {
                    String key = s.substring(0, s.indexOf('='));
                    if (ALLOWED_COOKIES.contains(key)) this.cookies.put(key, s.substring(s.indexOf('=') + 1));
                }
            }
        }
    }

    protected List<String> get(String url, boolean readInput) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(this.url + url).openConnection();
        urlConnection.setRequestProperty("User-Agent", "_");
        urlConnection.setRequestProperty("Cookie", this.serializeCookies());

        this.deserializeCookies(urlConnection.getHeaderFields().get("Set-Cookie"));

        List<String> lines = new ArrayList<>();
        if (readInput) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
        }

        return lines;
    }

    protected void post(String url, Map<String, String> payload, boolean followRedirects) throws IOException {
        byte[] data = this.getData(payload);

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(this.url + url).openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setInstanceFollowRedirects(followRedirects);
        urlConnection.setRequestProperty("User-Agent", "_");
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
        urlConnection.setRequestProperty("Cookie", this.serializeCookies());

        urlConnection.setDoOutput(true);
        OutputStream outputStream = urlConnection.getOutputStream();
        outputStream.write(data);

        this.deserializeCookies(urlConnection.getHeaderFields().get("Set-Cookie"));
    }

    protected byte[] getData(Map<String, String> payload) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            stringBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            stringBuilder.append('=');
            stringBuilder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            stringBuilder.append('&');
        }
        String s = stringBuilder.toString();
        return s.substring(0, s.length() - 1).getBytes("UTF-8");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 5) {
            System.out.println("Please specify site url, username, password, resource id and target id(s)");
            System.out.println("java -jar BuyerRemover.jar <url> <username> <password> <resource id> <target id 1> <target id 2>...");
            System.exit(1);
        }

        long millis = System.currentTimeMillis();
        BuyerRemover buyerRemover = new BuyerRemover(args[0], args[1], args[2]);
        buyerRemover.login();
        for (int i = 4; i < args.length; i++) {
            buyerRemover.removeBuyer(Integer.valueOf(args[3]), Integer.valueOf(args[i]));
        }
        System.out.println("Took: " + (System.currentTimeMillis() - millis) + "ms");
    }
}
