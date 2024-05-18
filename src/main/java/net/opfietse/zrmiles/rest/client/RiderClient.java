package net.opfietse.zrmiles.rest.client;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.opfietse.zrmiles.model.Rider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/riders")
@RegisterRestClient
public interface RiderClient {
    @GET
    List<Rider> getAllRiders();

    @Path("/{riderId}")
    @GET
    Rider getRider(@PathParam("riderId") Integer riderId);

    @Path("/{riderId}")
    @PUT
    Rider updateRider(@PathParam("riderId") Integer riderId, Rider rider);

    @Path("/{riderId}")
    @DELETE
    Rider deleteRider(@PathParam("riderId") Integer riderId);
}
