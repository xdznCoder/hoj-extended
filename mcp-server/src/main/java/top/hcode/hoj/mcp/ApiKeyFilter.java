package top.hcode.hoj.mcp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 可选 API Key 鉴权：根据 application.yml 的 hoj.api-keys（逗号分隔）校验 Authorization: Bearer <key>。
 * 为空时放行（仅建议内网/本地）。
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${hoj.api-keys:}")
    private String apiKeys;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String allowed = apiKeys == null ? "" : apiKeys.trim();
        if (allowed.isBlank()) {
            chain.doFilter(req, res);
            return;
        }
        String auth = req.getHeader("Authorization");
        boolean ok = auth != null && auth.startsWith("Bearer ") && isAllowed(auth.substring(7).trim());
        if (ok) {
            chain.doFilter(req, res);
        } else {
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"error\":\"unauthorized\"}");
        }
    }

    private boolean isAllowed(String key) {
        for (String k : apiKeys.split(",")) {
            if (k.trim().equals(key)) return true;
        }
        return false;
    }
}
