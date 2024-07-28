package net.opfietse.zrmiles.about;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

public class AboutResource extends Controller {
    @CheckedTemplate
    static class Templates {

        static native TemplateInstance about(
            boolean loggedIn
        );
    }

    @GET
    @Path("/about")
    public TemplateInstance about() {
        return Templates.about(false);
    }
}
