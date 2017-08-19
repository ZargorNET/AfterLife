package net.zargor.afterlife.objects;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import lombok.Getter;
import net.zargor.afterlife.WebServer;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.nio.charset.Charset;
import java.util.*;

/**
 * A better version from [io.netty.handler.codec.http.FullHttpRequest]
 */
@Getter
public class FullHttpReq extends DefaultFullHttpRequest {

    private Set<Cookie> cookies;
    private Map<String, String> getParameters;
    private List<Attribute> postAttributes;
    private Group group;
    private String language;

    public FullHttpReq(FullHttpRequest fullHttpRequest, String lang) {
        super(fullHttpRequest.protocolVersion(), fullHttpRequest.method(), fullHttpRequest.uri(), fullHttpRequest.content());
        this.language = lang;

        String cookiesReq = fullHttpRequest.headers().get(HttpHeaderNames.COOKIE);
        cookies = cookiesReq != null ? ServerCookieDecoder.STRICT.decode(cookiesReq) : new HashSet<>();

        Map<String, String> getParams = splitGetArguments();
        getParameters = getParams != null ? getParams : new HashMap<>();

        List<Attribute> getPostAttr = getPostAttri();
        postAttributes = getPostAttr != null ? getPostAttr : new ArrayList<>();

        group = getUsersGroup();

        if (getParameters.containsKey("lang")) {
            language = getParameters.get("lang");
        }
        setUri(uri().contains("?") ? uri().split("\\?")[0] : uri());
    }

    //Using sessions
    private Group getUsersGroup() {
        Optional<Cookie> optionalCookie = cookies.stream().filter(c -> Objects.equals(c.name(), "z-sID")).findFirst();
        if (optionalCookie.isPresent()) {
            Session session = WebServer.getInstance().getSessionManagement().getSessionByID(optionalCookie.get().value());
            return session == null ? null : session.getGroup();
        }
        return null;
    }


    private List<Attribute> getPostAttri() {
        List<Attribute> attributes = new ArrayList<>();
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(this);
        decoder.getBodyHttpDatas().stream().filter(ihd -> ihd instanceof Attribute).forEach(ihd -> attributes.add((Attribute) ihd));
        return attributes;
    }

    private Map<String, String> splitGetArguments() {
        if (uri().contains("?")) {
            if (uri().substring(1).chars().allMatch(i -> (char) i == '?')) {
                return null;
            }
            if (uri().charAt(1) == '?') {
                String newUri = uri().substring(1);

                List<String> vars = new ArrayList<>();
                String[] keys = newUri.split("&");
                vars.addAll(Arrays.asList(keys));
                Map<String, String> map = new HashMap<>();
                vars.forEach(s -> {
                    if (s.startsWith("?"))
                        s = s.substring(1);
                    if (s.contains("=")) {
                        String[] keyValue = s.split("=");
                        if (keyValue.length == 2) {
                            map.put(keyValue[0], keyValue[1]);
                        }
                    } else {
                        map.put(s, "true");
                    }
                });
                map.forEach((s, s2) -> System.out.println(s + " : " + s2));
                return map;
            }
        }
        return null;
    }

    public ResourceBundle getClassResourceBundle(String fileName) {
        return ResourceBundle.getBundle(String.format("backend/pages/%s/%s", fileName.toLowerCase(), fileName), new Locale(language), new ResourceBundle.Control() {
            @Override
            public Locale getFallbackLocale(String baseName, Locale locale) {
                return new Locale("en");
            }
        });
    }

    public byte[] renderHtml(String fileName, Optional<Map<String, String>> optionalExtraAttributes) {
        JtwigTemplate temp = JtwigTemplate.classpathTemplate(String.format("/pages/%s/%s.html", fileName, fileName));
        JtwigModel model = JtwigModel.newModel();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(String.format("pages/%s/%s", fileName, fileName), new Locale(language), new ResourceBundle.Control() {
            @Override
            public Locale getFallbackLocale(String baseName, Locale locale) {
                return new Locale("en");
            }
        });
        Map<String, String> extraAttributes;
        extraAttributes = optionalExtraAttributes.orElseGet(HashMap::new);
        resourceBundle.keySet().forEach(s -> extraAttributes.put(s, resourceBundle.getString(s)));
        extraAttributes.forEach(model::with);

        return temp.render(model).getBytes(Charset.forName("UTF-8"));
    }
}
