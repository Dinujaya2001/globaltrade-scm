package lk.globaltrade.scm.web;

import lk.globaltrade.scm.entity.InventoryItem;
import lk.globaltrade.scm.entity.Shipment;
import lk.globaltrade.scm.entity.User;
import lk.globaltrade.scm.service.CustomsComplianceService;
import lk.globaltrade.scm.service.OrderProcessingService;

import javax.naming.InitialContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/scm")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShipmentResource {

    private OrderProcessingService lookupOrderService() {
        try {
            InitialContext ctx = new InitialContext();
            return (OrderProcessingService) ctx.lookup("java:app/lk.globaltrade-globaltrade-scm-ejb-1.0.0/OrderProcessingService!lk.globaltrade.scm.service.OrderProcessingService");
        } catch (Exception e) {
            try {
                InitialContext ctx = new InitialContext();
                return (OrderProcessingService) ctx.lookup("java:global/globaltrade-scm-ear-1.0.0/lk.globaltrade-globaltrade-scm-ejb-1.0.0/OrderProcessingService!lk.globaltrade.scm.service.OrderProcessingService");
            } catch (Exception ex) {
                throw new RuntimeException("EJB Lookup failed for OrderProcessingService", ex);
            }
        }
    }

    private CustomsComplianceService lookupCustomsService() {
        try {
            InitialContext ctx = new InitialContext();
            return (CustomsComplianceService) ctx.lookup("java:app/lk.globaltrade-globaltrade-scm-ejb-1.0.0/CustomsComplianceService!lk.globaltrade.scm.service.CustomsComplianceService");
        } catch (Exception e) {
            try {
                InitialContext ctx = new InitialContext();
                return (CustomsComplianceService) ctx.lookup("java:global/globaltrade-scm-ear-1.0.0/lk.globaltrade-globaltrade-scm-ejb-1.0.0/CustomsComplianceService!lk.globaltrade.scm.service.CustomsComplianceService");
            } catch (Exception ex) {
                throw new RuntimeException("EJB Lookup failed for CustomsComplianceService", ex);
            }
        }
    }

    @GET
    @Path("/inventory")
    public Response getInventory() {
        try {
            List<InventoryItem> items = lookupOrderService().getAllInventory();
            return Response.ok(items).build();
        } catch (Exception e) {
            return Response.status(500).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/users")
    public Response getUsers() {
        try {
            List<User> users = lookupOrderService().getAllUsers();
            return Response.ok(users).build();
        } catch (Exception e) {
            return Response.status(500).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/shipments")
    public Response getShipments() {
        try {
            List<Shipment> shipments = lookupOrderService().getAllShipments();
            return Response.ok(shipments).build();
        } catch (Exception e) {
            return Response.status(500).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
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
            Shipment result = lookupOrderService().createShipmentWithItems(
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
        try {
            boolean success = lookupCustomsService().updateCustomsClearance(id, approved);
            if (success) {
                return Response.ok("{\"message\": \"Customs verification successfully processed\"}").build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Clearance transaction failed\"}").build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"" + ex.getMessage() + "\"}").build();
        }
    }
}