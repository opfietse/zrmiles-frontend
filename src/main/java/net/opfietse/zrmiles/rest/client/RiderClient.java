package net.opfietse.zrmiles.rest.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.opfietse.zrmiles.model.Rider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/riders")
@RegisterRestClient
public interface RiderClient {
    @GET
    List<Rider> get();
}
