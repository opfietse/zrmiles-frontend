package net.opfietse.zrmiles.rest;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import net.opfietse.zrmiles.model.FrontendStatisticsRecord;
import net.opfietse.zrmiles.model.MilesPlusName;
import net.opfietse.zrmiles.model.StatisticsRecord;
import net.opfietse.zrmiles.rest.client.MilesClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

public class Application extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @ConfigProperty(name = "quarkus.application.version")
    java.util.Optional<String> version;

    @RestClient
    MilesClient milesClient;

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance index(
            String version,
            Integer year,
            List<FrontendStatisticsRecord> stats,
            String distanceUnit
        );

        public static native TemplateInstance zrmilescss();
    }

    @Path("/")
    public TemplateInstance indexSlash() {
        int year = determineCurrentYear();
        return getHome(year);
    }

    @Path("/home")
    public TemplateInstance indexHome(@QueryParam("year") Integer givenYear) {
        int year = givenYear == null ? determineCurrentYear() : givenYear;
        return getHome(year);
    }

    @POST
    @Path("/home")
    public TemplateInstance indexHomePost(
        @FormParam("year") Integer givenYear,
        @FormParam("changeYear") String changeYear,
        @CookieParam("zrmilesPreferences") String zrmilesPreferences
    ) {
        logger.info("Get home page for year {} ({})", givenYear, zrmilesPreferences);

        int year = givenYear == null ? determineCurrentYear() : givenYear;
        return getHome(year);
    }

    private TemplateInstance getHome(int year) {
        logger.info("Getting home page");

        List<StatisticsRecord> milesForYear = milesClient.getMilesForYear(year);

        List<FrontendStatisticsRecord> stats =
            milesForYear
                .stream()
                .sorted((s1, s2) -> s1.mileage() > s2.mileage() ? -1 : s1.mileage() < s2.mileage() ? 1 : 0)
                .map(s -> new FrontendStatisticsRecord(s.name(), s.riderId(), s.mileage(), makeStringFromMileagesList(s.mileages(), 1.0f)))
                .toList();

        return Templates.index(version.orElse("Beta"), year, stats, "kilometers");
    }

    private static int determineCurrentYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();

        if (today.getMonth() == Month.JANUARY && today.getDayOfMonth() < 15) {
            year -= 1;
        }

        return year;
    }

    @Path("/zrmiles.css")
    @Produces("text/css")
    public TemplateInstance css() {
        return Templates.zrmilescss();
    }

    private String makeStringFromMileagesList(List<MilesPlusName> milesPlusNames, float conversion) {
        return milesPlusNames
            .stream()
            .sorted((s1, s2) -> s1.miles() > s2.miles() ? -1 : s1.miles() < s2.miles() ? 1 : 0)
            .map(s -> ((int) (s.miles() * conversion)) + " (" + s.name() + ")")
            .collect(Collectors.joining(", "));
    }
}
