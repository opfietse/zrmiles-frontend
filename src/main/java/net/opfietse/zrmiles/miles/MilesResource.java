package net.opfietse.zrmiles.miles;

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
import net.opfietse.zrmiles.model.Rider;
import net.opfietse.zrmiles.rest.client.BikeClient;
import net.opfietse.zrmiles.rest.client.MilesClient;
import net.opfietse.zrmiles.rest.client.RiderClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Path("miles")
public class MilesResource extends Controller {
    private static Logger logger = LoggerFactory.getLogger(MilesResource.class);

    @RestClient
    MilesClient milesClient;

    @RestClient
    BikeClient bikeClient;

    @RestClient
    RiderClient riderClient;

    @CheckedTemplate
    static class Templates {

        static native TemplateInstance milesForBike(
            List<Miles> miles,
            String pageHeader,
            String owner,
            Motorcycle motorcycle,
            String distanceUnitText,
            String currentDate
        );

        static native TemplateInstance updateMiles(
            Miles miles,
            Integer milesId,
            String dateFormat
        );
    }

    @Path("/bike")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getMilesForMotorcycle(@RestPath Integer motorcycleId) {
        logger.info("Get miles for motorcycle {}", motorcycleId);
        Motorcycle motorcycle = bikeClient.getBike(motorcycleId);
        Rider rider = riderClient.getRider(motorcycle.riderId());

        List<Miles> allMilesForMotorcycle = milesClient.getAllMilesForMotorcycle(motorcycleId).reversed();

        return Templates.milesForBike(
            allMilesForMotorcycle,
            "Moi",
            rider.firstName() + " " + rider.lastName(),
            motorcycle,
            motorcycle.distanceUnit() == 0 ? "Miles" : "Kilometers",
            LocalDate.now().toString()
        );
    }

    @POST
    @Path("bike")
    @Produces(MediaType.TEXT_HTML)
    public void addMiles(
        @RestPath Integer id,
        @RestForm Integer motorcycleId,
        @RestForm String date,
        @RestForm Integer odometer,
        @RestForm String correction,
        @RestForm String userComment,
        @RestForm String addMiles
    ) {
        if ("Add".equals(addMiles)) {
            if (id.equals(motorcycleId)) {
                logger.info("Updating motorcycle {}, adding miles", motorcycleId);
                if (StringUtils.isEmpty(date) || odometer.equals(0)) {
                    return; // TODO: supply the right values
                }

                bikeClient.getBike(motorcycleId);

                Miles newMiles = new Miles(
                    null,
                    motorcycleId,
                    LocalDate.parse(date),
                    odometer,
                    StringUtils.isEmpty(correction.trim()) ? null : Integer.parseInt(correction.trim()),
                    StringUtils.isEmpty(userComment.trim()) ? null : userComment.trim()
                );
                milesClient.addMiles(newMiles);
            }
        }

        getMilesForMotorcycle(id);
    }

    @Path("update/{milesId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getMiles(@RestPath Integer milesId) {
        Miles miles = milesClient.getMilesById(milesId);
        return Templates.updateMiles(miles, milesId, "TODO: mm-dd-yyyy");
    }

    @POST
    @Path("update/{pathMilesId}")
    @Produces(MediaType.TEXT_HTML)
    public void updateMiles(
        @RestPath Integer pathMilesId,
        @RestForm Integer milesId,
        @RestForm Integer motorcycleId,
        @RestForm String date,
        @RestForm Integer odometer,
        @RestForm String correction,
        @RestForm String userComment,
        @RestForm String updateMiles
    ) {
        if ("Update".equals(updateMiles)) {
            if (motorcycleId != null && odometer != null) {
                logger.info("Updating miles {}", milesId);

                milesClient.updateMiles(
                    milesId,
                    new Miles(
                        milesId,
                        motorcycleId,
                        LocalDate.parse(date),
                        odometer,
                        StringUtils.isEmpty(correction.trim()) ? null : Integer.parseInt(correction.trim()),
                        StringUtils.isEmpty(userComment.trim()) ? null : userComment.trim())
                );
            }
        } else if ("Delete".equals(updateMiles)) {
            if (milesId.equals(pathMilesId)) {
                milesClient.deleteMiles(milesId);
            }
        }

        getMilesForMotorcycle(motorcycleId);
    }
}
