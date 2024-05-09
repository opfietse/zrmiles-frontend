package net.opfietse.zrmiles.rest;

import jakarta.ws.rs.Path;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkiverse.renarde.Controller;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class Application extends Controller {
    @ConfigProperty(name = "quarkus.application.version")
    java.util.Optional<String> version;

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance index(String version);
        public static native TemplateInstance zrmilescss();
    }

    @Path("/")
    public TemplateInstance index() {
        return Templates.index(version.orElse("Beta"));
    }

    @Path("/zrmiles.css")
    @Produces("text/css")
    public TemplateInstance css() {
        return Templates.zrmilescss();
    }
}
