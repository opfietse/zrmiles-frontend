package net.opfietse.zrmiles.rest.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class EchoFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext
            .getHeaders()
            .keySet()
            .stream()
            .forEach(h -> System.out.println(h + ": " + requestContext.getHeaders().get(h)));
    }
}
