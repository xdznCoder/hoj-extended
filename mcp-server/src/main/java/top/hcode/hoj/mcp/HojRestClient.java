package top.hcode.hoj.mcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

/** 只读调用 HOJ 公共 REST 接口。 */
@Component
public class HojRestClient {

    private final RestClient hoj;

    public HojRestClient(RestClient.Builder builder, @Value("${hoj.upstream:http://hoj-backend:6688}") String upstream) {
        this.hoj = builder.baseUrl(upstream).build();
    }

    public String listProblems(Map<String, Object> a) {
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("currentPage", i(a, "page", 1));
        q.put("limit", i(a, "limit", 20));
        put(q, "keyword", s(a, "keyword"));
        put(q, "difficulty", a.get("difficulty"));
        return get("/api/get-problem-list", q);
    }

    public String getProblem(Map<String, Object> a) {
        return get("/api/get-problem-detail", Map.of("problemId", s(a, "problemId")));
    }

    public String getRanking(Map<String, Object> a) {
        boolean recent = "recent7".equalsIgnoreCase(s(a, "type"));
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("currentPage", i(a, "page", 1));
        q.put("limit", i(a, "limit", 20));
        if (!recent) q.put("type", 1);
        put(q, "searchUser", s(a, "searchUser"));
        return get(recent ? "/api/get-recent-seven-ac-rank" : "/api/get-rank-list", q);
    }

    public String getDailyProblem(Map<String, Object> a) {
        return get("/api/get-training-list", Map.of("currentPage", 1, "limit", 50));
    }

    private String get(String path, Map<String, Object> query) {
        return hoj.get().uri(u -> {
            u.path(path);
            query.forEach((k, v) -> { if (v != null) u.queryParam(k, String.valueOf(v)); });
            return u.build();
        }).retrieve().body(String.class);
    }

    private static String s(Map<String, Object> a, String k) {
        Object v = a.get(k);
        return v == null ? "" : String.valueOf(v);
    }
    private static int i(Map<String, Object> a, String k, int d) {
        Object v = a.get(k);
        return v instanceof Number n ? n.intValue() : d;
    }
    private static void put(Map<String, Object> m, String k, Object v) {
        if (v != null && !String.valueOf(v).isBlank()) m.put(k, v);
    }
}
