package net.opfietse.zrmiles.bikes;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.opfietse.zrmiles.model.Motorcycle;

import java.util.ArrayList;
import java.util.List;

@Path("bikes")
public class BikeResource {
    @CheckedTemplate
    static class Templates {

        static native TemplateInstance bikes(List<Motorcycle> bikes);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
//        List<Item> items = new ArrayList<>();
//        items.add(new Item(new BigDecimal(10), "Apple"));
//        items.add(new Item(new BigDecimal(16), "Pear"));
//        items.add(new Item(new BigDecimal(30), "Orange"));
        List<Motorcycle> riders = new ArrayList<>();
        riders.add(new Motorcycle(1, 1, "Kawasaki", "ZR-7", 1999, Short.valueOf("0")));
        return BikeResource.Templates.bikes(riders);
    }
}
