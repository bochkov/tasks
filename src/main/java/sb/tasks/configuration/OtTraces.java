package sb.tasks.configuration;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public final class OtTraces extends OncePerRequestFilter {

    private final Tracer tracer;

    private @Nullable String getTraceId() {
        TraceContext ctx = this.tracer.currentTraceContext().context();
        return ctx == null ? null : ctx.traceId();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse resp, @NonNull FilterChain chain)
            throws ServletException, IOException {
        String traceId = getTraceId();
        if (traceId != null) {
            resp.setHeader("X-Trace-Id", traceId);
        }
        chain.doFilter(req, resp);
    }
}
