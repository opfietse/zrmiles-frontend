package net.opfietse.zrmiles.rest.client;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.opfietse.zrmiles.model.Motorcycle;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/motorcycles")
@RegisterRestClient
public interface BikeClient {
    @GET
    List<Motorcycle> getAllBikes();

    @Path("/{bikeId}")
    @GET
    Motorcycle getBike(@PathParam("bikeId") Integer bikeId);

    @POST
    Motorcycle addBike(Motorcycle motorcycle);

    @Path("/rider/{riderId}")
    @GET
    List<Motorcycle> getForRider(@PathParam("riderId") Integer riderId);

    @Path("/{bikeId}")
    @PUT
    Motorcycle updateBike(@PathParam("bikeId") Integer bikeId,  Motorcycle motorcycle);

    @Path("/{bikeId}")
    @DELETE
    Motorcycle deleteBike(@PathParam("bikeId") Integer bikeId);
}
