package lk.globaltrade.scm.web;

import lk.globaltrade.scm.entity.InventoryItem;
import lk.globaltrade.scm.entity.Shipment;
import lk.globaltrade.scm.entity.User;
import lk.globaltrade.scm.service.CustomsComplianceService;
import lk.globaltrade.scm.service.OrderProcessingService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/scm")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShipmentResource {

    @EJB
    private OrderProcessingService orderService;

    @EJB
    private CustomsComplianceService customsService;

    @GET
    @Path("/shipments")
    public Response getShipments() {
        try {
            return Response.ok(orderService.getAllShipments()).build();
        } catch (Exception e) {
            return Response.status(500).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/shipments")
    public Response createShipment(@QueryParam("trackingNo") String trackingNo,
                                   @QueryParam("origin") String origin,
                                   @QueryParam("destination") String destination,
                                   @QueryParam("weight") String weightStr,
                                   @QueryParam("vendor") String vendor,
                                   @QueryParam("itemId") String itemIdStr,
                                   @QueryParam("qty") String qtyStr) {
        try {
            double weight = 1500.0;
            if (weightStr != null && !weightStr.trim().isEmpty()) {
                try { weight = Double.parseDouble(weightStr.trim()); } catch (Exception ignored) {}
            }

            Long itemId = null;
            if (itemIdStr != null && !itemIdStr.trim().isEmpty() && !itemIdStr.equals("null")) {
                try { itemId = Long.parseLong(itemIdStr.trim()); } catch (Exception ignored) {}
            }

            int qty = 5;
            if (qtyStr != null && !qtyStr.trim().isEmpty()) {
                try { qty = Integer.parseInt(qtyStr.trim()); } catch (Exception ignored) {}
            }

            Shipment result = orderService.createShipmentWithItems(
                    trackingNo, origin, destination, weight, vendor, itemId, qty);

            return Response.status(Response.Status.CREATED).entity(result).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + ex.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/shipments/{id}/clearance")
    public Response updateClearance(@PathParam("id") Long id, @QueryParam("approved") boolean approved) {
        try {
            boolean success = customsService.updateCustomsClearance(id, approved);
            if (success) {
                return Response.ok("{\"message\": \"Customs clearance updated successfully\"}").build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Clearance failed\"}").build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + ex.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/inventory")
    public Response getInventory() {
        try {
            return Response.ok(orderService.getAllInventory()).build();
        } catch (Exception e) {
            return Response.status(500).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/inventory")
    public Response createInventoryItem(@QueryParam("code") String code,
                                        @QueryParam("name") String name,
                                        @QueryParam("qty") String qtyStr,
                                        @QueryParam("threshold") String threshStr) {
        try {
            int qty = (qtyStr != null && !qtyStr.trim().isEmpty()) ? Integer.parseInt(qtyStr.trim()) : 100;
            int threshold = (threshStr != null && !threshStr.trim().isEmpty()) ? Integer.parseInt(threshStr.trim()) : 20;

            InventoryItem item = orderService.createInventoryItem(code, name, qty, threshold);
            return Response.status(Response.Status.CREATED).entity(item).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + ex.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/users")
    public Response getUsers() {
        try {
            return Response.ok(orderService.getAllUsers()).build();
        } catch (Exception e) {
            return Response.status(500).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/users")
    public Response createUser(@QueryParam("username") String username,
                               @QueryParam("password") String password,
                               @QueryParam("role") String role) {
        try {
            User user = orderService.createUser(username, password, role);
            return Response.status(Response.Status.CREATED).entity(user).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + ex.getMessage() + "\"}").build();
        }
    }
}