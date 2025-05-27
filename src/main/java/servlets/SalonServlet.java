package servlets;

import dao.SalonJpaController;
import dao.exceptions.NonexistentEntityException;
import dto.Salon;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.json.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "SalonServlet", urlPatterns = {"/salon"})
public class SalonServlet extends HttpServlet {

    private SalonJpaController salonController;

    @Override
    public void init() throws ServletException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_PRUEBA_03_war_1.0-SNAPSHOTPU");
        salonController = new SalonJpaController(emf);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String idParam = request.getParameter("id");

            if (idParam != null) {
                Integer id = Integer.parseInt(idParam);
                Salon salon = salonController.findSalon(id);
                if (salon != null) {
                    out.print(toJson(salon).toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                List<Salon> salones = salonController.findSalonEntities();
                JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                for (Salon s : salones) {
                    arrayBuilder.add(toJson(s));
                }
                out.print(arrayBuilder.build().toString());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JsonObject json = Json.createReader(request.getInputStream()).readObject();
        Salon salon = new Salon(
                json.getInt("id"),
                json.getString("numero"),
                json.getInt("capacidad")
        );
        try {
            salonController.create(salon);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JsonObject json = Json.createReader(request.getInputStream()).readObject();
        Salon salon = new Salon(
                json.getInt("id"),
                json.getString("numero"),
                json.getInt("capacidad")
        );
        try {
            salonController.edit(salon);
        } catch (NonexistentEntityException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer id = Integer.parseInt(request.getParameter("id"));
        try {
            salonController.destroy(id);
        } catch (NonexistentEntityException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private JsonObject toJson(Salon salon) {
        return Json.createObjectBuilder()
                .add("id", salon.getId())
                .add("numero", salon.getNumero())
                .add("capacidad", salon.getCapacidad())
                .build();
    }
}
