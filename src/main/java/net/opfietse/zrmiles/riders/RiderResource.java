package net.opfietse.zrmiles.riders;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.opfietse.zrmiles.model.Rider;
import net.opfietse.zrmiles.rest.client.RiderClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.List;

@Path("riders")
public class RiderResource {
    @RestClient
    private RiderClient riderClient;

    @CheckedTemplate
    static class Templates {

        static native TemplateInstance riders(List<Rider> riders);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        List<Rider> riders = riderClient.get();

//        List<Item> items = new ArrayList<>();
//        items.add(new Item(new BigDecimal(10), "Apple"));
//        items.add(new Item(new BigDecimal(16), "Pear"));
//        items.add(new Item(new BigDecimal(30), "Orange"));
//        List<Rider> riders = new ArrayList<>();
//        riders.add(new Rider(1, "Mark", "R", "NL"));
//        riders.add(new Rider(2, "Tom", "W", "USA"));
//        riders.add(new Rider(3, "Thom", "B", "USA"));
        return RiderResource.Templates.riders(riders);
    }
}
