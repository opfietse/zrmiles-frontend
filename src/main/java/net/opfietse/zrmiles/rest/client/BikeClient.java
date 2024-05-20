package net.opfietse.zrmiles.rest.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.opfietse.zrmiles.model.Motorcycle;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/motorcycles")
@RegisterRestClient
public interface BikeClient {
    @GET
    List<Motorcycle> get();

    @POST
    Motorcycle addBike(Motorcycle motorcycle);

    @Path("/rider/{riderId}")
    @GET
    List<Motorcycle> getForRider(@PathParam("riderId") Integer riderId);
}
