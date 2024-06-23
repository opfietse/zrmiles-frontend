package net.opfietse.zrmiles.rest;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @ConfigProperty(name = "quarkus.application.version")
    java.util.Optional<String> version;

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance index(String version);

        public static native TemplateInstance zrmilescss();
    }

    @Path("/")
    public TemplateInstance indexSlash() {
        return getHome();
    }

    @Path("/home")
    public TemplateInstance indexHome() {
        return getHome();
    }

    private TemplateInstance getHome() {
        logger.info("Getting home page");
        return Templates.index(version.orElse("Beta"));
    }

    @Path("/zrmiles.css")
    @Produces("text/css")
    public TemplateInstance css() {
        return Templates.zrmilescss();
    }
}
