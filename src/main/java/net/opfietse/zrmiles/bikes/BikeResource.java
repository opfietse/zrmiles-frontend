package net.opfietse.zrmiles.bikes;

import io.quarkiverse.renarde.util.StringUtils;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.opfietse.zrmiles.model.Motorcycle;
import net.opfietse.zrmiles.model.MotorcycleWithRider;
import net.opfietse.zrmiles.model.Rider;
import net.opfietse.zrmiles.rest.client.BikeClient;
import net.opfietse.zrmiles.rest.client.RiderBikeClient;
import net.opfietse.zrmiles.rest.client.RiderClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("bikes")
public class BikeResource {
    private static final Logger log = LoggerFactory.getLogger(BikeResource.class);

    @RestClient
    private BikeClient bikeClient;
    @RestClient
    private RiderClient riderClient;
    @RestClient
    private RiderBikeClient riderBikeClient;

    @CheckedTemplate
    static class Templates {

        static native TemplateInstance allBikes(
            List<MotorcycleWithRider> bikes,
            String pageHeader);

        static native TemplateInstance bikesForRider(
            Motorcycle newBike,
            List<Motorcycle> bikes,
            String pageHeader,
            boolean registerOk,
            boolean kilometers);

        static native TemplateInstance updateBike(
            Motorcycle bike,
            String pageHeader,
            boolean kilometers,
            boolean updateOk);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getAllBikes() {
        log.info("getAllBikes called");

        List<Motorcycle> bikes = bikeClient.getAllBikes();
        List<Rider> riders = riderClient.getAllRiders();
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
        return Templates.allBikes(motorcyclesWithRider, "All bikes in the database.");
    }

    @Path("/rider/{riderId}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getBikesForRider(@PathParam("riderId") Integer riderId) {
        log.info("Get bikes for rider {}", riderId);

        List<Motorcycle> bikes = riderBikeClient.getForRider(riderId);
        Rider rider = riderClient.getRider(riderId);
        return Templates.bikesForRider(null,
            bikes,
            String.format("Enter data for %s %s's bike below.", rider.firstName(), rider.lastName()),
            true,
            true
        );
    }

    @Path("/update/{id}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getUpdateBikeForm(@PathParam("id") Integer id) {
        log.info("Get update form for bike {}", id);

        Motorcycle bike = bikeClient.getBike(id);
        return BikeResource.Templates.updateBike(
            bike,
            "",
            bike.distanceUnit().equals(Short.valueOf("1")),
            true
        );
    }

    @Path("/update/{id}")
    @POST
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance updateBike(
        @PathParam("id") Integer id,
        @RestForm Integer bikeId,
        @RestForm Integer riderId,
        @RestForm String make,
        @RestForm String model,
        @RestForm String year,
        @RestForm String distanceUnit,
        @RestForm String updateMotorcycle,
        @RestForm String deleteMotorcycle
    ) {
        log.info("Update/delete bike {}", id);

        if ("Update".equals(updateMotorcycle)) {
            if (id.equals(bikeId)) {
                log.info("Updating bike {}", bikeId);

                bikeClient.getBike(bikeId);
                Motorcycle newMotorcycle = new Motorcycle(
                    id,
                    riderId,
                    make,
                    model,
                    StringUtils.isEmpty(year) ? null : Integer.parseInt(year),
                    Short.valueOf(distanceUnit)
                );

                bikeClient.updateBike(id, newMotorcycle);

                return getBikesForRider(riderId);
            } else {
                log.warn("Update: Id {} and bike.id {} do not match!", id, bikeId);
            }
        } else if ("Delete".equals(deleteMotorcycle)) {
            if (id.equals(bikeId)) {
                log.info("Deleting bike {}", bikeId);
                Motorcycle deletedBike = bikeClient.deleteBike(bikeId);
                return getBikesForRider(deletedBike.riderId());
            } else {
                log.warn("Delete: Id {} and bike.id {} do not match!", id, bikeId);
                return getUpdateBikeForm(id);
            }
        }

        return getBikesForRider(riderId);
    }

    @Path("/rider/{riderId}")
    @POST
    public TemplateInstance registerBike(
        @PathParam("riderId") Integer riderId,
        @RestForm String make,
        @RestForm String model,
        @RestForm String year,
        @RestForm String distanceUnit,
        @RestForm String addMotorcycle
    ) {
        if ("Add".equals(addMotorcycle)) {
            System.out.println("Distance unit: " + distanceUnit);

            if (StringUtils.isEmpty(make) || StringUtils.isEmpty(model) || StringUtils.isEmpty(distanceUnit)) {
                List<Motorcycle> bikes = riderBikeClient.getForRider(riderId);

                return Templates.bikesForRider(
                    new Motorcycle(
                        null,
                        riderId,
                        make,
                        model,
                        StringUtils.isEmpty(year) ? null : Integer.parseInt(year),
                        StringUtils.isEmpty(distanceUnit) ? null : Short.valueOf(distanceUnit)),
                    bikes,
                    "Please fill out all required fields!"
                    , false,
                    StringUtils.isEmpty(distanceUnit) ? true : "1".equals(distanceUnit)
                );
            }

            Motorcycle newMotorcycle = new Motorcycle(
                null,
                riderId,
                make,
                model,
                StringUtils.isEmpty(year) ? null : Integer.parseInt(year),
                Short.valueOf(distanceUnit)
            );

            bikeClient.addBike(newMotorcycle);
        }

        List<Motorcycle> bikes = riderBikeClient.getForRider(riderId);
        Rider rider = riderClient.getRider(riderId);

        return Templates.bikesForRider(
            null,
            bikes,
            String.format("Enter data for %s %s's bike below.", rider.firstName(), rider.lastName()),
            true,
            true);
    }
}
