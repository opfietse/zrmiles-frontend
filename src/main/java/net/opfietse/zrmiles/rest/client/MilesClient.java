package net.opfietse.zrmiles.rest.client;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.opfietse.zrmiles.model.Miles;
import net.opfietse.zrmiles.model.StatisticsRecord;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient
public interface MilesClient {
    @Path("motorcycles/{motorcycleId}/miles")
    @GET
    List<Miles> getAllMilesForMotorcycle(@PathParam("motorcycleId") Integer motorcycleId);

    @Path("/miles/{milesId}")
    @GET
    Miles getMilesById(@PathParam("milesId") Integer milesId);

    @Path("/miles/year/{year}/distanceUnit/{distanceUnit}")
    @GET
    List<StatisticsRecord> getMilesForYear(@PathParam("year") Integer milesId, @PathParam("distanceUnit") String distanceUnit);

    @Path("/miles")
    @POST
    Miles addMiles(Miles miles);

    @Path("/miles/{milesId}")
    @PUT
    Miles updateMiles(@PathParam("milesId") Integer milesId, Miles miles);

    @Path("/miles/{milesId}")
    @DELETE
    Miles deleteMiles(@PathParam("milesId") Integer milesId);
}
