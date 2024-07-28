package net.opfietse.zrmiles.help;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

public class HelpResource extends Controller {
    @CheckedTemplate
    static class Templates {

        static native TemplateInstance help();
    }

    @GET
    @Path("/help")
    public TemplateInstance help() {
        return Templates.help();
    }
}
