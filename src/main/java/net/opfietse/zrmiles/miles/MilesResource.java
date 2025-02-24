package net.opfietse.zrmiles.miles;

import io.quarkiverse.renarde.Controller;
import io.quarkiverse.renarde.util.StringUtils;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.opfietse.zrmiles.CookieStuff;
import net.opfietse.zrmiles.DateExtension;
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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Stream;

import static net.opfietse.zrmiles.Constants.DATE_FORMAT_AMERICAN;
import static net.opfietse.zrmiles.Constants.DATE_FORMAT_EUROPEAN;
import static net.opfietse.zrmiles.Constants.PREFERENCES_COOKIE_NAME;

@Path("miles")
public class MilesResource extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(MilesResource.class);

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
            String owner,
            Motorcycle motorcycle,
            String distanceUnitText,
            String datePreference,
            String currentDate
        );

        static native TemplateInstance updateMiles(
            String owner,
            Motorcycle motorcycle,
            Miles miles,
            Integer milesId,
            String dateFormat
        );
    }

    @Path("/bike")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getMilesForMotorcycle(
        @CookieParam(PREFERENCES_COOKIE_NAME) String zrmilesPreferenceCookeValue,
        @RestPath Integer motorcycleId
    ) {
        logger.info("Get miles for motorcycle {}", motorcycleId);
        return createMilesForBikeTemplateInstance(zrmilesPreferenceCookeValue, motorcycleId);
    }

    @POST
    @Path("bike")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance addMiles(
        @CookieParam(PREFERENCES_COOKIE_NAME) String zrmilesPreferenceCookeValue,
        @RestPath Integer id,
        @RestForm Integer motorcycleId,
        @RestForm @NotBlank String date,
        @RestForm @NotBlank String odometer,
        @RestForm String correction,
        @RestForm String userComment,
        @RestForm String addMiles
    ) {
        LocalDate milesDate = null;

        if (StringUtils.isEmpty(date)) {
            validation.addError("date", "Miles date cannot be empty");
        } else {
            try {
                if (CookieStuff.preferenceIsAmericanDate(zrmilesPreferenceCookeValue)) {
                   milesDate = DateExtension.AMERICAN_DATE_FORMAT.parse(date, LocalDate::from);
                } else {
                   milesDate =  DateExtension.EUROPEAN_DATE_FORMAT.parse(date, LocalDate::from);
                }
            } catch (DateTimeParseException e) {
                validation.addError("date", "Miles date cannot be parsed");
            }
        }

        if (StringUtils.isEmpty(odometer)) {
            validation.addError("odometer", "Miles odometer cannot be negative");
        } else {
            try {
                Integer.parseInt(odometer);
            } catch (NumberFormatException e) {
                validation.addError("odometer", "Miles odometer must be an integer");
            }
        }

        if (validationFailed()) {
            return createMilesForBikeTemplateInstance(zrmilesPreferenceCookeValue, motorcycleId);
        }

        if ("Add".equals(addMiles)) {
            if (id.equals(motorcycleId)) {
                logger.info("Updating motorcycle {}, adding miles", motorcycleId);
//                if (StringUtils.isEmpty(date) || odometer.equals(0)) {
//                    // TODO: show error
//                    return Response
//                        .seeOther(uriInfo.getAbsolutePath())
//                        .header("Cache-Control", "no-cache, no-store, must-revalidate")
//                        .header("Pragma", "no-cache")
//                        .header("Expires", "0")
//                        .build();
//                }

                bikeClient.getBike(motorcycleId);

                Miles newMiles = new Miles(
                    null,
                    motorcycleId,
                    milesDate, // CookieStuff.preferenceIsAmericanDate(zrmilesPreferenceCookeValue) ? DateExtension.AMERICAN_DATE_FORMAT.parse(date, LocalDate::from) : DateExtension.EUROPEAN_DATE_FORMAT.parse(date, LocalDate::from),
                    null,
                    Integer.valueOf(odometer),
                    StringUtils.isEmpty(correction.trim()) ? null : Integer.parseInt(correction.trim()),
                    StringUtils.isEmpty(userComment.trim()) ? null : userComment.trim()
                );
                milesClient.addMiles(newMiles);
            }
        }

        return createMilesForBikeTemplateInstance(zrmilesPreferenceCookeValue, motorcycleId);
    }

    private TemplateInstance createMilesForBikeTemplateInstance(String zrmilesPreferenceCookeValue, Integer motorcycleId) {
        Motorcycle motorcycle = bikeClient.getBike(motorcycleId);
        Rider rider = riderClient.getRider(motorcycle.riderId());

        List<Miles> allMilesForMotorcycle = milesClient.getAllMilesForMotorcycle(motorcycleId).reversed();

        Stream<Miles> milesStream = allMilesForMotorcycle
            .stream()
            .map(
                m -> new Miles(
                    m.id(),
                    m.motorcycleId(),
                    m.milesDate(),
                    DateExtension.formatLocalDate(m.milesDate(), zrmilesPreferenceCookeValue),
                    m.odometerReading(),
                    m.correctionMiles(),
                    m.userComment()
                )
            );

        return Templates.milesForBike(
            milesStream.toList(),
            rider.firstName() + " " + rider.lastName(),
            motorcycle,
            motorcycle.distanceUnit() == 0 ? "Miles" : "Kilometers",
            CookieStuff.preferenceIsAmericanDate(zrmilesPreferenceCookeValue) ? DATE_FORMAT_AMERICAN : DATE_FORMAT_EUROPEAN,
            DateExtension.formatLocalDate(LocalDate.now(), CookieStuff.getDatePreference(zrmilesPreferenceCookeValue))
        );
    }

    @Path("update/{milesId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getMiles(
        @CookieParam(PREFERENCES_COOKIE_NAME) String zrmilesPreferenceCookeValue,
        @RestPath Integer milesId
    ) {
        Miles miles = milesClient.getMilesById(milesId);
        miles = new Miles(
            miles.id(),
            miles.motorcycleId(),
            miles.milesDate(),
            DateExtension.formatLocalDate(miles.milesDate(), zrmilesPreferenceCookeValue),
            miles.odometerReading(),
            miles.correctionMiles(),
            miles.userComment()
        );

        Motorcycle motorcycle = bikeClient.getBike(miles.motorcycleId());
        Rider rider = riderClient.getRider(motorcycle.riderId());

        return Templates.updateMiles(
            rider.firstName() + " " + rider.lastName(),
            motorcycle,
            miles,
            milesId,
            CookieStuff.getDatePreferenceFormat(zrmilesPreferenceCookeValue)
        );
    }

    @POST
    @Path("update/{pathMilesId}")
    @Produces(MediaType.TEXT_HTML)
    public void updateMiles(
        @CookieParam(PREFERENCES_COOKIE_NAME) String zrmilesPreferenceCookeValue,
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
                        CookieStuff.preferenceIsAmericanDate(zrmilesPreferenceCookeValue) ? DateExtension.AMERICAN_DATE_FORMAT.parse(date, LocalDate::from) : DateExtension.EUROPEAN_DATE_FORMAT.parse(date, LocalDate::from),
                        null,
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

        logger.info("Call miles page {}, {}", zrmilesPreferenceCookeValue, motorcycleId);

        redirect(MilesResource.class).getMilesForMotorcycle(zrmilesPreferenceCookeValue, motorcycleId);
//        return Response
//            .seeOther(uriInfo.getAbsolutePath())
//            .header("Cache-Control", "no-cache, no-store, must-revalidate")
//            .header("Pragma", "no-cache")
//            .header("Expires", "0")
//            .build();


//        getMilesForMotorcycle(zrmilesPreferenceCookeValue, motorcycleId);
    }
}
