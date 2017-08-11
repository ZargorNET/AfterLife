package net.zargor.afterlife.web.objects;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import lombok.Getter;
import net.zargor.afterlife.web.WebServer;
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
    }

    //Using sessions
    private Group getUsersGroup() {
        Optional<Cookie> optionalCookie = cookies.stream().filter(c -> Objects.equals(c.name(), "z-sID")).findFirst();
        if (optionalCookie.isPresent()) {
            Session session = WebServer.getInstance().getHandler().getSessionM().getSessionByID(optionalCookie.get().value());
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
            Map<String, String> map = new HashMap<>();
            String[] splitA = uri().split("\\?");
            final String[] splitT = {splitA[1]};
            if (splitA.length > 2) {
                final int[] index = {0};
                Arrays.stream(splitA).forEach(s -> {
                    if (index[0] > 1) {
                        splitT[0] += "?" + s;
                    }
                    index[0]++;
                });
            }
            List<String> args = new ArrayList<>();
            if (splitT[0].contains("&")) {
                args.addAll(Arrays.asList(splitT[0].split("&")));
            } else {
                args.add(splitT[0]);
            }
            args.forEach(s -> {
                if (!s.equals(""))
                    if (s.contains("=")) {
                        String[] vs = s.split("=");
                        map.put(vs[0], vs[1]);
                    } else {
                        map.put(s, "true");
                    }
            });
            return map;
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
