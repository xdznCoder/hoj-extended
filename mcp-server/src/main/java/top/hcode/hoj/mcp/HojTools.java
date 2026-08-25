package top.hcode.hoj.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/** 4 个只读 MCP 工具定义。 */
@Component
public class HojTools {

    private final HojRestClient hoj;

    public HojTools(HojRestClient hoj) { this.hoj = hoj; }

    public List<McpServerFeatures.SyncToolSpecification> specifications() {
        return List.of(
            spec("listProblems", "查询 HOJ 公开题库列表（支持分页/关键词/难度筛选）",
                 new String[]{"keyword", "difficulty", "page", "limit"},
                 (ex, a) -> text(hoj.listProblems(a))),
            spec("getProblem", "查询某道题目的详细信息（题面/输入输出/样例/标签）",
                 new String[]{"problemId"},
                 (ex, a) -> text(hoj.getProblem(a))),
            spec("getRanking", "查询用户排名（type=user 或 recent7，可搜索用户名）",
                 new String[]{"type", "searchUser", "page", "limit"},
                 (ex, a) -> text(hoj.getRanking(a))),
            spec("getDailyProblem", "获取最新“每日一题”（每日一题被建模为一种训练）",
                 new String[]{"trainKeyword"},
                 (ex, a) -> text(hoj.getDailyProblem(a)))
        );
    }

    private static McpServerFeatures.SyncToolSpecification spec(
            String name, String desc, String[] props,
            BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler) {
        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name(name)
                .description(desc)
                .inputSchema(schema(props))
                .build();
        return new McpServerFeatures.SyncToolSpecification(tool, handler);
    }

    private static McpSchema.JsonSchema schema(String[] props) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String p : props) {
            properties.put(p, Map.of("type", "string", "description", p));
        }
        return new McpSchema.JsonSchema("object", properties, List.of(), false, null, null);
    }

    private static McpSchema.CallToolResult text(String json) {
        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent(json)), false);
    }
}
