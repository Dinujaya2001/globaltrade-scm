package lk.globaltrade.scm.web;

import lk.globaltrade.scm.entity.InventoryItem;
import lk.globaltrade.scm.entity.Shipment;
import lk.globaltrade.scm.service.CustomsComplianceService;
import lk.globaltrade.scm.service.OrderProcessingService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Stateless
@Path("/scm")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShipmentResource {

    @EJB
    private OrderProcessingService orderProcessingService;

    @EJB
    private CustomsComplianceService customsComplianceService;

    @GET
    @Path("/shipments")
    public Response getShipments() {
        return Response.ok(orderProcessingService.getAllShipments()).build();
    }

    @GET
    @Path("/inventory")
    public Response getInventory() {
        return Response.ok(orderProcessingService.getAllInventory()).build();
    }

    @POST
    @Path("/shipments")
    public Response createShipment(@QueryParam("trackingNo") String trackingNo,
                                   @QueryParam("origin") String origin,
                                   @QueryParam("destination") String destination,
                                   @QueryParam("weight") double weight,
                                   @QueryParam("vendor") String vendor,
                                   @QueryParam("itemId") Long itemId,
                                   @QueryParam("qty") int qty) {
        try {
            Shipment result = orderProcessingService.createShipmentWithItems(
                    trackingNo, origin, destination, weight, vendor, itemId, qty);
            return Response.status(Response.Status.CREATED).entity(result).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + ex.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/shipments/{id}/clearance")
    public Response updateClearance(@PathParam("id") Long id, @QueryParam("approved") boolean approved) {
        boolean success = customsComplianceService.updateCustomsClearance(id, approved);
        if (success) {
            return Response.ok("{\"message\": \"Customs verification successfully processed\"}").build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Failed to process clearance\"}").build();
    }

    @GET
    @Path("/users")
    public Response getUsers() {
        return Response.ok(orderProcessingService.getAllUsers()).build();
    }
}