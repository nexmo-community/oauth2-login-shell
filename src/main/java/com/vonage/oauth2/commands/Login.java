package com.vonage.oauth2.commands;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@ShellComponent
@RestController
public class Login {

    private static final String OAUTH_STATE = UUID.randomUUID().toString();
    private static final String CALLBACK_URL = "/callback";

    @Value("${oauth.baseUrl}")
    public String baseUrl;

    @Value("${oauth.authorizeEndpoint}")
    public String authorizeEndpoint;

    @Value("${oauth.tokenEndpoint}")
    public String tokenEndpoint;

    @Value("${oauth.clientId}")
    public String clientId;

    @Value("${oauth.clientSecret}")
    public String clientSecret;

    @Autowired
    private EmbeddedWebApplicationContext appContext;

    // The login() and callback() methods are likely run in separate threads, however CountDownLatch
    // acts as a memory barrier for `code`.
    private volatile CountDownLatch latch;
    private String code;
    private OAuth2Token token;

    @ShellMethod("login")
    public void login() throws IOException, URISyntaxException, InterruptedException {
        // Launch a web browser that will be redirected to the login screen. When complete it will be
        // redirected to
        // the callback endpoint and we wait until that happens
        latch = new CountDownLatch(1);
        String contextPath = getBaseUrl();
        Desktop.getDesktop().browse(new URI(getAuthorizeUrl(contextPath)));
        latch.await();

        // Once we have the code, call the token endpoint to get a token
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + getBasicAuth());
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> httpEntity = new HttpEntity<>(getTokenFields(contextPath, code), headers);

        token = restTemplate.postForObject(getTokenUrl(), httpEntity, OAuth2Token.class);
        System.out.println("Got token: " + token);
    }

    @ShellMethod("userinfo")
	public void userinfo() {
       RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token.getAccessToken());
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        String url = baseUrl + "/userinfo";
        String userinfo = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class).getBody();
        System.out.println("User info: " + userinfo);
	}

    @GetMapping(CALLBACK_URL)
    public String callback(@RequestParam Map<String, String> parameters) {
        // Double check that the state is what we set, although not too terribly critical since we are
        // running this
        // endpoint on localhost for this example, but included for completeness sake
        String state = parameters.get("state");
        if (!OAUTH_STATE.equals(state)) {
            System.out.println("Invalid state " + state + " provided to callback");
            code = null;
        } else {
            code = parameters.get("code");
        }
        latch.countDown();

        return "You may close this tab/window";
    }

    private String getBasicAuth() {
        return new String(Base64.encodeBase64((clientId + ":" + clientSecret).getBytes()));
    }

    private String getAuthorizeUrl(String contextPath) {
        // Instead of building the call to /authorize yourself you may use any OAuth2 client you wish to
        // achieve
        // the redirection code flow login
        StringBuilder sb = new StringBuilder(baseUrl + authorizeEndpoint + "?");
        sb.append("scope=openid");
        sb.append("&response_type=code");
        sb.append("&redirect_uri=" + contextPath + CALLBACK_URL);
        sb.append("&client_id=").append(clientId);
        sb.append("&state=").append(OAUTH_STATE);
        return sb.toString();
    }

    private String getTokenUrl() {
        return baseUrl + tokenEndpoint;
    }

    private String getTokenFields(String baseUrl, String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("grant_type=authorization_code");
        sb.append("&code=").append(code);
        sb.append("&redirect_uri=" + baseUrl + CALLBACK_URL);
        return sb.toString();
    }


    private String getBaseUrl() throws UnknownHostException {
        Connector connector =
                ((TomcatEmbeddedServletContainer) appContext.getEmbeddedServletContainer()).getTomcat().getConnector();
        int port = connector.getPort();
        String contextPath = appContext.getServletContext().getContextPath();
        return "http://localhost:" + port + contextPath;
    }
}
