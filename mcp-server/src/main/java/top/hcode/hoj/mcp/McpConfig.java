package top.hcode.hoj.mcp;

import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcStreamableServerTransportProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/** 官方 MCP Java SDK + Spring WebMVC：Streamable HTTP，端点 /mcp。 */
@Configuration
public class McpConfig {

    @Bean
    WebMvcStreamableServerTransportProvider mcpTransportProvider() {
        return WebMvcStreamableServerTransportProvider.builder()
                .mcpEndpoint("/mcp")
                .jsonMapper(new JacksonMcpJsonMapper(new com.fasterxml.jackson.databind.ObjectMapper()))
                .build();
    }

    @Bean(destroyMethod = "close")
    McpSyncServer mcpSyncServer(WebMvcStreamableServerTransportProvider transport, HojTools tools) {
        return McpServer.sync(transport).tools(tools.specifications()).build();
    }

    @Bean
    RouterFunction<ServerResponse> mcpRoutes(WebMvcStreamableServerTransportProvider transport) {
        return transport.getRouterFunction();
    }
}
