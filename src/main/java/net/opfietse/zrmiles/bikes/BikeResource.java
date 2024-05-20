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
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        List<Motorcycle> bikes = bikeClient.get();
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
    public TemplateInstance getForRider(@PathParam("riderId") Integer riderId) {
        List<Motorcycle> bikes = riderBikeClient.getForRider(riderId);
        Rider rider = riderClient.getRider(riderId);
        return Templates.bikesForRider(null,
            bikes,
            String.format("Enter data for %s %s's bike below.", rider.firstName(), rider.lastName()),
            true,
            true
        );
    }

    //
//    @Path("/update/{id}")
//    @GET
//    @Produces(MediaType.TEXT_HTML)
//    public TemplateInstance getUpdateRiderForm(@PathParam("id") Integer id) {
//        Rider rider = riderClient.getRider(id);
//        return RiderResource.Templates.updateRider(rider);
//    }
//
//    @Path("/update/{id}")
//    @POST
//    @Produces(MediaType.TEXT_HTML)
//    public void updateRider(
//        @PathParam("id") Integer id,
//        @RestForm Integer riderId,
//        @RestForm String firstName,
//        @RestForm String lastName,
//        @RestForm String streetAddress,
//        @RestForm String updateRider,
//        @RestForm String deleteRider
//    ) {
//        if ("Update".equals(updateRider)) {
//            if (id.equals(riderId)) {
//                log.info("Updating rider {}", riderId);
//
//                riderClient.getRider(riderId);
//                Rider newRider = new Rider(riderId, firstName, lastName, streetAddress);
//                riderClient.updateRider(riderId, newRider);
//
//                getUpdateRiderForm(id);
////                return RiderResource.Templates.updateRider(newRider);
//            }
//        } else if ("Delete".equals(deleteRider)) {
//            if (id.equals(riderId)) {
//                log.info("Deleting rider {}", riderId);
//                riderClient.deleteRider(riderId);
//            } else {
//                log.warn("Id {} and rider.id {} do not match!", id, riderId);
//            }
//        }
//
//        getAllRiders();
//    }
//
//    @Path("/register")
//    @GET
//    @Produces(MediaType.TEXT_HTML)
//    public TemplateInstance getRegisterRiderForm() {
//        return RiderResource.Templates.registerRider(null, true);
//    }
//
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
