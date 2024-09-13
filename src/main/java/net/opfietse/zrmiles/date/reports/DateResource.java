package net.opfietse.zrmiles.date.reports;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateResource extends Controller {
    @CheckedTemplate
    static class Templates {

        static native TemplateInstance date(String dateAssen, String dateLondon, String dateUtc);
    }

    @GET
    @Path("/date")
    public TemplateInstance date() {
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return Templates.date(
            formatter.format(now.withZoneSameInstant(ZoneId.of("Europe/Amsterdam"))),
            formatter.format(now.withZoneSameInstant(ZoneId.of("Europe/London"))),
            formatter.format(now.withZoneSameInstant(ZoneId.of("UTC")))
        );
    }
}
