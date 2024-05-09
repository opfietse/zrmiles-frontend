package net.opfietse.zrmiles.bikes;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.opfietse.zrmiles.model.Motorcycle;
import net.opfietse.zrmiles.rest.client.BikeClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@Path("bikes")
public class BikeResource {
    @RestClient
    private BikeClient bikeClient;

    @CheckedTemplate
    static class Templates {

        static native TemplateInstance bikes(List<Motorcycle> bikes);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        List<Motorcycle> bikes = bikeClient.get();
        return Templates.bikes(bikes);
    }
}
