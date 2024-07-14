package net.opfietse.zrmiles.preferences;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import net.opfietse.zrmiles.Constants;
import org.jboss.resteasy.reactive.NoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

import static net.opfietse.zrmiles.Constants.DATE_FORMAT_AMERICAN;
import static net.opfietse.zrmiles.Constants.DATE_FORMAT_EUROPEAN;
import static net.opfietse.zrmiles.Constants.DISTANCE_UNIT_KILOMETERS;
import static net.opfietse.zrmiles.Constants.DISTANCE_UNIT_MILES;

@Path("preferences")
public class PreferenceResource extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(PreferenceResource.class);

    private static final List<String> distanceUnits = List.of(DISTANCE_UNIT_MILES, Constants.DISTANCE_UNIT_KILOMETERS);
    private static final List<String> dateFormats = List.of(DATE_FORMAT_AMERICAN, Constants.DATE_FORMAT_EUROPEAN);
    private static final Logger log = LoggerFactory.getLogger(PreferenceResource.class);

    @CheckedTemplate
    static class Templates {

        static native TemplateInstance preference(
            String currentDistanceUnit,
            String currentDateFormat,
            List<String> distanceUnits,
            List<String> dateFormats
        );
    }

    @GET
    @Path("")
    @NoCache
    public TemplateInstance preferences(@CookieParam("zrmilesPreferences") String zrmilesPreferenceCookeValue) {
        logger.info("Get preferences: {}", zrmilesPreferenceCookeValue);

        return Templates.preference(
            zrmilesPreferenceCookeValue.contains("Miles") ? DISTANCE_UNIT_MILES : DISTANCE_UNIT_KILOMETERS,
            zrmilesPreferenceCookeValue.contains("American") ? DATE_FORMAT_AMERICAN : DATE_FORMAT_EUROPEAN,
            distanceUnits,
            dateFormats);
    }

    @POST
    @Path("")
    public Response changePreferences(
        @Context UriInfo uriInfo,
        @FormParam("distanceUnitSelect") String distanceUnitSelect,
        @FormParam("dateFormatSelect") String dateFormatSelect,
        @FormParam("changePreferences") String changePreferences
    ) {
        logger.info("Change preferences: {}, {}", distanceUnitSelect, dateFormatSelect);

        if (!checkInput(distanceUnitSelect, dateFormatSelect)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String newDistanceUnit = (DISTANCE_UNIT_MILES.equals(distanceUnitSelect) ? DISTANCE_UNIT_MILES : DISTANCE_UNIT_KILOMETERS);
        String newDateFormat = (DATE_FORMAT_AMERICAN.equals(dateFormatSelect) ? DATE_FORMAT_AMERICAN : DATE_FORMAT_EUROPEAN);

        return Response
            .seeOther(URI.create(uriInfo.getBaseUri() + "preferences"))
            .cookie(
                new NewCookie(
                    new Cookie(
                        "zrmilesPreferences",
                        newDistanceUnit + "And" + (DATE_FORMAT_AMERICAN.equals(newDateFormat) ? "American" : "European")
                    ),
                    "comment",
                    365 * 24 * 60 * 60,
                    true
                )
            )
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            .build();
    }

    private boolean checkInput(String distanceUnitSelect, String dateFormatSelect) {
        return (!DISTANCE_UNIT_MILES.equals(distanceUnitSelect) && !DISTANCE_UNIT_KILOMETERS.equals(distanceUnitSelect)) ||
            (!DATE_FORMAT_AMERICAN.equals(dateFormatSelect) && !DATE_FORMAT_EUROPEAN.equals(dateFormatSelect));
    }
}
