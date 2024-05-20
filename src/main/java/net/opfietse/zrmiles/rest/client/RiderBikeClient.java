package net.opfietse.zrmiles.rest.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.opfietse.zrmiles.model.Motorcycle;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/riders")
@RegisterRestClient
public interface RiderBikeClient {
    @GET
    List<Motorcycle> get();

    @Path("/{riderId}/motorcycles")
    @GET
    List<Motorcycle> getForRider(@PathParam("riderId") Integer riderId);
}
