package top.hcode.hoj.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * HOJ 对外只读 MCP 工具（Spring AI MCP 会把这些 @Tool 方法注册为 MCP tool）。
 * 通过 RestClient 调用 HOJ 公共 REST 接口（默认 http://hoj-backend:6688）。
 */
@Component
public class HojMcpTools {

    private final RestClient hoj;

    public HojMcpTools(RestClient.Builder builder, @Value("${hoj.upstream:http://hoj-backend:6688}") String upstream) {
        this.hoj = builder.baseUrl(upstream).build();
    }

    @Tool(description = "查询 HOJ 公开题库列表（支持分页/关键词/难度筛选）")
    public String listProblems(
            @ToolParam(description = "搜索关键词，可选") String keyword,
            @ToolParam(description = "难度，可选；如 1/2/3/4/5") Integer difficulty,
            @ToolParam(description = "页码，从1开始，默认1") Integer page,
            @ToolParam(description = "每页条数，默认20") Integer limit) {
        int p = page == null ? 1 : page;
        int l = limit == null ? 20 : limit;
        return hoj.get().uri(u -> {
            u.path("/api/get-problem-list")
             .queryParam("currentPage", p).queryParam("limit", l);
            if (keyword != null && !keyword.isBlank()) u.queryParam("keyword", keyword);
            if (difficulty != null) u.queryParam("difficulty", difficulty);
            return u.build();
        }).retrieve().body(String.class);
    }

    @Tool(description = "查询某道题目的详细信息（题面/输入输出/样例/标签/语言）")
    public String getProblem(
            @ToolParam(description = "题目ID，如 1001") String problemId) {
        return hoj.get().uri(u -> u.path("/api/get-problem-detail")
                .queryParam("problemId", problemId == null ? "" : problemId).build())
                .retrieve().body(String.class);
    }

    @Tool(description = "查询用户排名。type 为 user(全局) 或 recent7(近7天)；可搜索用户名")
    public String getRanking(
            @ToolParam(description = "排名类型：user 或 recent7，默认 user") String type,
            @ToolParam(description = "搜索用户名，可选") String searchUser,
            @ToolParam(description = "页码，默认1") Integer page,
            @ToolParam(description = "每页条数，默认20") Integer limit) {
        boolean recent = "recent7".equalsIgnoreCase(type);
        int p = page == null ? 1 : page;
        int l = limit == null ? 20 : limit;
        String path = recent ? "/api/get-recent-seven-ac-rank" : "/api/get-rank-list";
        return hoj.get().uri(u -> {
            u.path(path).queryParam("currentPage", p).queryParam("limit", l);
            if (!recent) u.queryParam("type", 1);
            if (searchUser != null && !searchUser.isBlank()) u.queryParam("searchUser", searchUser);
            return u.build();
        }).retrieve().body(String.class);
    }

    @Tool(description = "获取最新“每日一题”（每日一题被建模为一种训练；返回匹配训练列表，可进一步取详情）")
    public String getDailyProblem(
            @ToolParam(description = "训练关键词，默认“每日一题”，可选") String trainKeyword) {
        String list = hoj.get().uri(u -> u.path("/api/get-training-list")
                .queryParam("currentPage", 1).queryParam("limit", 50).build())
                .retrieve().body(String.class);
        return "训练列表: " + list + "（如需自动取最新题，可再由 getTrainingProblemList 取该训练题目）";
    }
}
