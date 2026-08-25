# HOJ 对外开放 MCP 服务：查看 / 使用 / 其它 Agent 接入

> 技术栈：JDK 21 + Spring Boot 3.5.x + Spring AI MCP（`spring-ai-starter-mcp-server-webmvc`，稳定版 2.0.1）。
> 服务为独立只读服务，调用 HOJ 公共 REST 接口，**不改 OJ 本体**。

## 一、服务地址与鉴权

部署后对外：
```
https://<你的域名>/mcp        # MCP Streamable HTTP 端点（Spring AI MCP Server）
Authorization: Bearer <MCP_API_KEYS>   # 这个 key 来自 .env 的 MCP_API_KEYS
```

未设置 `MCP_API_KEYS` 时不鉴权（仅建议内网/本地用）；生产建议必填。

## 二、如何查看“已拥有的 MCP 服务/工具”

### 0) 在线文档/查询页（已集成到前端）
浏览器直接打开：`https://xdzn.asia/mcp-docs.html` —— 可在线列出工具、调用工具、查看各 Agent 接入方式，无需装任何客户端。

MCP 客户端会自动发现工具（`tools/list`）。你也可以手动看：

### 1) 用官方 MCP Inspector（最直观）
```bash
npx @modelcontextprotocol/inspector http://127.0.0.1:3000/mcp
# 或公网： npx @modelcontextprotocol/inspector https://xdzn.asia/mcp
# （若服务要求 Bearer，Inspector 连接配置里填 Authorization header）
# 打开后会列出该服务提供的所有工具及其参数/描述，可在线调用测试。
```

### 2) 直接用 curl 调 `tools/list`（Streamable HTTP 用 JSON-RPC）
```bash
curl -X POST 'https://xdzn.asia/mcp' \

  -H 'Authorization: Bearer <你的KEY>' \

  -H 'Content-Type: application/json' \

  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'
```
返回示例：
```json
{"result":{"tools":[
  {"name":"listProblems","description":"查询 HOJ 公开题库...","inputSchema":{...}},
  {"name":"getProblem","description":"查询题目详情...","inputSchema":{...}},
  {"name":"getRanking","description":"查询排名...","inputSchema":{...}},
  {"name":"getDailyProblem","description":"最新每日一题...","inputSchema":{...}}
]}}
```

### 3) 工具清单（本服务提供）
| 工具 | 说明 | 对应 HOJ 接口 |
|---|---|---|
| `listProblems` | 题库分页/关键词/难度/标签 | `/api/get-problem-list` |
| `getProblem` | 题目详情（题面/样例/标签） | `/api/get-problem-detail` |
| `getRanking` | 用户排名（全局/近7天） | `/api/get-rank-list` |
| `getDailyProblem` | 最新每日一题（训练） | `/api/get-training-*` |

## 三、使用方式

### A. 手动测试（Inspector / curl）
上面第三节即可直接调用，例如取排名：
```bash
curl -X POST 'https://xdzn.asia/mcp' -H 'Authorization: Bearer <KEY>' -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"getRanking","arguments":{"type":"user","page":1,"limit":20}}}'
```

### B. 让一个 Agent（如 DSH/Cursor/Claude）调用
把该 MCP 服务配置到 Agent 的 MCP 客户端里（见第四节），然后自然语言触发：
> "用 HOJ 的 MCP 查一下 1001 题的详情" / "看下最近 7 天排名" / "今天的每日一题是什么"

## 四、其它 Agent / MCP 客户端的接入方式

MCP 客户端都是“配置一个 MCP Server 条目（类型=HTTP/URL + headers）”，填入 `/mcp` 地址与 API Key 即可。

### Claude Desktop / Claude Code
`claude_desktop_config.json` 或 `~/.claude.json`（或 `claude mcp add`）：
```json
{
  "mcpServers": {
    "hoj": {
      "type": "http",
      "url": "https://xdzn.asia/mcp",
      "headers": { "Authorization": "Bearer <你的KEY>" }
    }
  }
}
```
Claude Code 命令行：
```bash
claude mcp add hoj --transport http --url https://xdzn.asia/mcp --header "Authorization: Bearer <KEY>"
```

### Cursor
`Settings → MCP → Add MCP Server`：
```json
{ "type": "http", "url": "https://xdzn.asia/mcp", "headers": { "Authorization": "Bearer <KEY>" } }
```

### Cherry Studio / 其它 MCP 客户端
- 新建 **MCP 服务器**，类型选 **HTTP/SSE**，地址填 `https://xdzn.asia/mcp`；
- 在 **请求头** 里加 `Authorization: Bearer <KEY>`；
- 启用后即可在对话中使用该服务的工具。

### DSH（本机 DeepSeek Harness）直接复用
- 若你要在 **DSH 新会话**里用：给 DSH 配一个 MCP 客户端/或直接让模型调用上面 curl/HTTP（DSH 可通过工具调用 MCP HTTP）。
- 更简单：构建一个小的“HOJ 查询”skill 封装这些 REST 调用（与前文 `github-commit-workflow` 同思路），新会话说“查题目/排名/每日一题”即用。

## 五、安全与生产建议
- **必须**设置 `MCP_API_KEYS`（Bearer）；生产不要留空。
- 只读工具，不含任何写操作，减少误改风险。
- 加限流（服务端可引入 Bucket4j）+ 缓存（Caffeine TTL）保护上游 OJ。
- `https` 走现有 nginx 证书（`location /mcp` 反代到 `mcp-server:3000`）。

## 六、本项目落地文件
```
mcp-server/                        # Spring Boot + Spring AI MCP 独立服务
  pom.xml                          # JDK21 + Boot 3.5.16 + spring-ai-bom 2.0.1
  src/main/java/top/hcode/hoj/mcp/ # 主类 + 各 @Tool 工具
  src/main/resources/application.yml
docker/mcp.Dockerfile              # 构建/运行镜像 (maven:3.9-eclipse-temurin-21 → eclipse-temurin:21-jre)
standAlone/docker-compose.yml      # + mcp-server 服务
src/frontend/default.conf.template # + location /mcp
.env.example                       # + MCP_API_KEYS / MCP_UPSTREAM
.github/workflows/build-images.yml # + mcp 镜像 job → ghcr
```
