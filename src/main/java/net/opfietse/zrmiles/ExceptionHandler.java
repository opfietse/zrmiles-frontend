package net.opfietse.zrmiles;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public Response toResponse(Exception e) {

        if (e instanceof NotFoundException nfe) {
            LOGGER.error("HTTP 404");
            return Response.status(Response.Status.NOT_FOUND).entity("404").build();
        }

        LOGGER.error("Error: {}", e.getMessage(), e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("General error").build();
    }
}
