package net.opfietse.zrmiles.bikes;

import io.quarkiverse.renarde.Controller;
import io.quarkiverse.renarde.util.StringUtils;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.opfietse.zrmiles.model.Miles;
import net.opfietse.zrmiles.model.Motorcycle;
import net.opfietse.zrmiles.model.MotorcycleWithRider;
import net.opfietse.zrmiles.model.Rider;
import net.opfietse.zrmiles.rest.client.BikeClient;
import net.opfietse.zrmiles.rest.client.MilesClient;
import net.opfietse.zrmiles.rest.client.RiderBikeClient;
import net.opfietse.zrmiles.rest.client.RiderClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("bikes")
public class BikeResource extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(BikeResource.class);

    @RestClient
    private BikeClient bikeClient;
    @RestClient
    private RiderClient riderClient;
    @RestClient
    private RiderBikeClient riderBikeClient;
    @RestClient
    private MilesClient milesClient;

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

        static native TemplateInstance milesForBike(
            List<Miles> miles,
            String pageHeader,
            String owner,
            Motorcycle motorcycle,
            String distanceUnitText,
            String currentDate
        );
    }

    @Path("")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getAllBikes() {
        logger.info("getAllBikes called");

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
                    riderById.get(m.riderId()),
                    m.totalMiles()
                )).toList();
        return Templates.allBikes(motorcyclesWithRider, "All bikes in the database.");
    }

    @Path("/rider")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getBikesForRider(@RestPath Integer riderId) {
        logger.info("Get bikes for rider {}", riderId);

        List<Motorcycle> bikes = riderBikeClient.getForRider(riderId);
        Rider rider = riderClient.getRider(riderId);
        return Templates.bikesForRider(null,
            bikes,
            String.format("Enter data for %s %s's bike below.", rider.firstName(), rider.lastName()),
            true,
            true
        );
    }

    @Path("/update")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getUpdateBikeForm(@RestPath Integer id) {
        logger.info("Get update form for bike {}", id);

        Motorcycle bike = bikeClient.getBike(id);
        return BikeResource.Templates.updateBike(
            bike,
            "",
            bike.distanceUnit().equals(Short.valueOf("1")),
            true
        );
    }

    @POST
    @Path("/update")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance updateBike(
        @RestPath Integer id,
        @RestForm Integer bikeId,
        @RestForm Integer riderId,
        @RestForm String make,
        @RestForm String model,
        @RestForm String year,
        @RestForm String distanceUnit,
        @RestForm String updateMotorcycle,
        @RestForm String deleteMotorcycle
    ) {
        logger.info("Update/delete bike {}", id);

        if ("Update".equals(updateMotorcycle)) {
            if (id.equals(bikeId)) {
                logger.info("Updating bike {}", bikeId);

                bikeClient.getBike(bikeId);
                Motorcycle newMotorcycle = new Motorcycle(
                    id,
                    riderId,
                    make,
                    model,
                    StringUtils.isEmpty(year) ? null : Integer.parseInt(year),
                    Short.valueOf(distanceUnit),
                    null
                );

                bikeClient.updateBike(id, newMotorcycle);

                return getBikesForRider(riderId);
            } else {
                logger.warn("Update: Id {} and bike.id {} do not match!", id, bikeId);
            }
        } else if ("Delete".equals(deleteMotorcycle)) {
            if (id.equals(bikeId)) {
                logger.info("Deleting bike {}", bikeId);
                Motorcycle deletedBike = bikeClient.deleteBike(bikeId);
                return getBikesForRider(deletedBike.riderId());
            } else {
                logger.warn("Delete: Id {} and bike.id {} do not match!", id, bikeId);
                return getUpdateBikeForm(id);
            }
        }

        return getBikesForRider(riderId);
    }

    @POST
    @Path("/rider")
    public TemplateInstance registerBike(
        @RestPath Integer riderId,
        @RestForm String make,
        @RestForm String model,
        @RestForm String year,
        @RestForm String distanceUnit,
        @RestForm String addMotorcycle
    ) {
        if ("Add".equals(addMotorcycle)) {
            logger.info("Add bike for rider {}: {} {} {} {}", riderId, make, model, year, distanceUnit);

            if (StringUtils.isEmpty(make) || StringUtils.isEmpty(model) || StringUtils.isEmpty(distanceUnit)) {
                List<Motorcycle> bikes = riderBikeClient.getForRider(riderId);

                return Templates.bikesForRider(
                    new Motorcycle(
                        null,
                        riderId,
                        make,
                        model,
                        StringUtils.isEmpty(year) ? null : Integer.parseInt(year),
                        StringUtils.isEmpty(distanceUnit) ? null : Short.valueOf(distanceUnit),
                        null
                    ),
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
                Short.valueOf(distanceUnit),
                null
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

    @Path("/miles")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getMilesforBike(@RestPath Integer motorcycleId) {
        logger.info("Get miles for motorcycle {}", motorcycleId);
        Motorcycle motorcycle = bikeClient.getBike(motorcycleId);
        Rider rider = riderClient.getRider(motorcycle.riderId());

        List<Miles> allMilesForMotorcycle = milesClient.getAllMilesForMotorcycle(motorcycleId);

        return Templates.milesForBike(
            allMilesForMotorcycle,
            "Moi",
            rider.firstName() + " " + rider.lastName(),
            motorcycle,
            motorcycle.distanceUnit() == 0 ? "Miles" : "Kilometers",
            LocalDate.now().toString()
        );
    }
}
