package net.opfietse.zrmiles.riders;

import io.quarkiverse.renarde.Controller;
import io.quarkiverse.renarde.util.StringUtils;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.opfietse.zrmiles.model.Rider;
import net.opfietse.zrmiles.rest.client.RiderClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

//@Path("riders")
public class RiderResource extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(RiderResource.class);

    @RestClient
    private RiderClient riderClient;

    @CheckedTemplate
    static class Templates {

        static native TemplateInstance allRiders(List<Rider> riders);

        static native TemplateInstance updateRider(Rider rider);

        static native TemplateInstance registerRider(Rider rider, boolean registerOk);
    }

    @Path("/riders")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getAllRiders() {
        logger.info("Getting all riders");
        List<Rider> riders = riderClient.getAllRiders();
        return RiderResource.Templates.allRiders(riders);
    }

    @Path("/riders/update")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getUpdateRiderForm(@RestPath Integer id) {
        Rider rider = riderClient.getRider(id);
        return RiderResource.Templates.updateRider(rider);
    }

    @POST
    @Path("/riders/update")
    @Produces(MediaType.TEXT_HTML)
    public void updateRider(
        @RestPath @Positive Integer id,
        @RestForm @Positive Integer riderId,
        @RestForm @NotBlank String firstName,
        @RestForm @NotBlank String lastName,
        @RestForm String streetAddress,
        @RestForm String updateRider,
        @RestForm String deleteRider
    ) {
        if (StringUtils.isEmpty(firstName)) {
            validation.addError("firstName", "Please give a valid first name");
        }

        if (StringUtils.isEmpty(lastName)) {
            validation.addError("lastName", "Please give a valid last name");
        }

        if (validationFailed()) {
            getUpdateRiderForm(riderId);
        }

        if ("Update".equals(updateRider)) {
            if (id.equals(riderId)) {
                logger.info("Updating rider {}", riderId);

                if (!StringUtils.isEmpty(firstName) && !StringUtils.isEmpty(lastName)) {
                    riderClient.getRider(riderId);
                    Rider newRider = new Rider(
                        riderId,
                        firstName.trim(),
                        lastName.trim(),
                        StringUtils.isEmpty(streetAddress.trim()) ? null : streetAddress.trim()
                    );
                    riderClient.updateRider(riderId, newRider);
                }

                getUpdateRiderForm(id);
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

    @Path("/riders/register")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getRegisterRiderForm() {
        return RiderResource.Templates.registerRider(null, true);
    }

    @POST
    @Path("/riders/register")
    public TemplateInstance registerRider(
        @RestForm String firstName,
        @RestForm String lastName,
        @RestForm String streetAddress,
        @RestForm String addRider
    ) {
        if ("Add".equals(addRider)) {
            if (StringUtils.isEmpty(firstName) || StringUtils.isEmpty(lastName)) {
                return RiderResource.Templates.registerRider(
                    new Rider(
                        null,
                        firstName,
                        lastName,
                        StringUtils.isEmpty(streetAddress.trim()) ? null : streetAddress.trim()
                    ),
                    false
                );
            }

            Rider newRider = new Rider(
                null,
                firstName,
                lastName,
                StringUtils.isEmpty(streetAddress.trim()) ? null : streetAddress.trim()
            );
            Rider registeredRider = riderClient.addRider(newRider);

            return RiderResource.Templates.registerRider(registeredRider, true);
        }

        return RiderResource.Templates.registerRider(null, false);
    }
}
