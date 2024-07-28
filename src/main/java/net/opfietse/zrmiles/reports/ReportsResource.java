package net.opfietse.zrmiles.reports;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

public class ReportsResource extends Controller {
    @CheckedTemplate
    static class Templates {

        static native TemplateInstance reports();
    }

    @GET
    @Path("/reports")
    public TemplateInstance reports() {
        return Templates.reports();
    }
}
