package hu.benkoata.imdb.configurations;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@SuppressWarnings("unused")
@Component
public class AddCacheControlNoStoreHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader(
                "cache-control", "no-store");
        chain.doFilter(request, response);
    }
}
