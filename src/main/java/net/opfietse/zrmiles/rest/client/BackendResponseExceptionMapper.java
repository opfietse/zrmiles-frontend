package net.opfietse.zrmiles.rest.client;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class BackendResponseExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    @Override
    public RuntimeException toThrowable(Response response) {
        if (response.getStatus() == 400) {
            throw new RuntimeException("The remote service responded with HTTP 400");
        } else if (response.getStatus() == 404) {
            throw new RuntimeException("The remote service responded with HTTP 404");
        } else if (response.getStatus() == 500) {
            throw new RuntimeException("The remote service responded with HTTP 500");
        }

        return null;
    }
}
