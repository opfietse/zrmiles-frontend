package net.opfietse.zrmiles.bikes;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.opfietse.zrmiles.model.Motorcycle;
import net.opfietse.zrmiles.model.MotorcycleWithRider;
import net.opfietse.zrmiles.model.Rider;
import net.opfietse.zrmiles.rest.client.BikeClient;
import net.opfietse.zrmiles.rest.client.RiderClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("bikes")
public class BikeResource {
    @RestClient
    private BikeClient bikeClient;
    @RestClient
    private RiderClient riderClient;

    @CheckedTemplate
    static class Templates {

        static native TemplateInstance bikes(List<MotorcycleWithRider> bikes);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        List<Motorcycle> bikes = bikeClient.get();
        List<Rider> riders = riderClient.get();
        Map<Integer, String> riderById = riders
            .stream()
            .collect(Collectors.toMap(r -> r.id(), r -> r.firstName() + " " + r.lastName()));
        List<MotorcycleWithRider> motorcyclesWithRider =
            bikes
                .stream()
                .map(m -> new MotorcycleWithRider(
                    m.id(),
                    m.riderId(),
                    m.make(),
                    m.model(),
                    m.year(),
                    m.distanceUnit(),
                    riderById.get(m.riderId())
                )).toList();
        return Templates.bikes(motorcyclesWithRider);
    }
}
