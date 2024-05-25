package net.opfietse.zrmiles.riders;

import io.quarkiverse.renarde.util.StringUtils;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.opfietse.zrmiles.model.Rider;
import net.opfietse.zrmiles.rest.client.RiderClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("riders")
public class RiderResource {
    private static final Logger logger = LoggerFactory.getLogger(RiderResource.class);

    @RestClient
    private RiderClient riderClient;

    @CheckedTemplate
    static class Templates {

        static native TemplateInstance allRiders(List<Rider> riders);

        static native TemplateInstance updateRider(Rider rider);

        static native TemplateInstance registerRider(Rider rider, boolean registerOk);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getAllRiders() {
        List<Rider> riders = riderClient.getAllRiders();
        return RiderResource.Templates.allRiders(riders);
    }

    @Path("/update/{id}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getUpdateRiderForm(@PathParam("id") Integer id) {
        Rider rider = riderClient.getRider(id);
        return RiderResource.Templates.updateRider(rider);
    }

    @Path("/update/{id}")
    @POST
    @Produces(MediaType.TEXT_HTML)
    public void updateRider(
        @PathParam("id") Integer id,
        @RestForm Integer riderId,
        @RestForm String firstName,
        @RestForm String lastName,
        @RestForm String streetAddress,
        @RestForm String updateRider,
        @RestForm String deleteRider
    ) {
        if ("Update".equals(updateRider)) {
            if (id.equals(riderId)) {
                logger.info("Updating rider {}", riderId);

                riderClient.getRider(riderId);
                Rider newRider = new Rider(riderId, firstName, lastName, streetAddress);
                riderClient.updateRider(riderId, newRider);

                getUpdateRiderForm(id);
//                return RiderResource.Templates.updateRider(newRider);
            }
        } else if ("Delete".equals(deleteRider)) {
            if (id.equals(riderId)) {
                logger.info("Deleting rider {}", riderId);
                riderClient.deleteRider(riderId);
            } else {
                logger.warn("Id {} and rider.id {} do not match!", id, riderId);
            }
        }

        getAllRiders();
    }

    @Path("/register")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getRegisterRiderForm() {
        return RiderResource.Templates.registerRider(null, true);
    }

    @Path("/register")
    @POST
    public TemplateInstance registerRider(
        @RestForm String firstName,
        @RestForm String lastName,
        @RestForm String streetAddress,
        @RestForm String addRider
    ) {
        if ("Add".equals(addRider)) {
            if (StringUtils.isEmpty(firstName) || StringUtils.isEmpty(lastName)) {
                return RiderResource.Templates.registerRider(
                    new Rider(null, firstName, lastName, streetAddress),
                    false
                );
            }

            Rider newRider = new Rider(null, firstName, lastName, streetAddress);
            Rider registeredRider = riderClient.addRider(newRider);

            return RiderResource.Templates.registerRider(registeredRider, true);
        }

        return RiderResource.Templates.registerRider(null, false);
    }
}
