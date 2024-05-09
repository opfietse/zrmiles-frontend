package net.opfietse.zrmiles.rest;

import jakarta.ws.rs.Path;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkiverse.renarde.Controller;
import jakarta.ws.rs.Produces;

public class Application extends Controller {

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance index();
        public static native TemplateInstance zrmilescss();
    }

    @Path("/")
    public TemplateInstance index() {
        return Templates.index();
    }

    @Path("/zrmiles.css")
    @Produces("text/css")
    public TemplateInstance css() {
        return Templates.zrmilescss();
    }
}
