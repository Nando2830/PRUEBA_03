package servlets;

import dao.ClaseJpaController;
import dao.exceptions.NonexistentEntityException;
import dto.Clase;
import dto.Profesor;
import dto.Salon;

import javax.json.*;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/claseservlet")
public class ClaseServlet extends HttpServlet {

    private ClaseJpaController claseController;

    @Override
    public void init() throws ServletException {
        claseController = new ClaseJpaController(Persistence.createEntityManagerFactory("com.mycompany_PRUEBA_03_war_1.0-SNAPSHOTPU"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        List<Clase> clases = claseController.findClaseEntities();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (Clase clase : clases) {
            JsonObjectBuilder obj = Json.createObjectBuilder()
                .add("id", clase.getId())
                .add("nombre", clase.getNombre());

            if (clase.getProfesorId() != null) {
                obj.add("profesorId", clase.getProfesorId().getId());
            } else {
                obj.addNull("profesorId");
            }

            if (clase.getSalonId() != null) {
                obj.add("salonId", clase.getSalonId().getId());
            } else {
                obj.addNull("salonId");
            }

            arrayBuilder.add(obj);
        }

        try (PrintWriter out = response.getWriter()) {
            JsonWriter writer = Json.createWriter(out);
            writer.writeArray(arrayBuilder.build());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        JsonReader reader = Json.createReader(request.getInputStream());
        JsonObject json = reader.readObject();

        try {
            Clase clase = new Clase();
            clase.setId(json.getInt("id"));
            clase.setNombre(json.getString("nombre"));

            if (!json.isNull("profesorId")) {
                clase.setProfesorId(new Profesor(json.getInt("profesorId")));
            }

            if (!json.isNull("salonId")) {
                clase.setSalonId(new Salon(json.getInt("salonId")));
            }

            claseController.create(clase);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        JsonReader reader = Json.createReader(request.getInputStream());
        JsonObject json = reader.readObject();

        try {
            Clase clase = claseController.findClase(json.getInt("id"));
            if (clase == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Clase no encontrada");
                return;
            }

            clase.setNombre(json.getString("nombre"));

            if (!json.isNull("profesorId")) {
                clase.setProfesorId(new Profesor(json.getInt("profesorId")));
            } else {
                clase.setProfesorId(null);
            }

            if (!json.isNull("salonId")) {
                clase.setSalonId(new Salon(json.getInt("salonId")));
            } else {
                clase.setSalonId(null);
            }

            claseController.edit(clase);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NonexistentEntityException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID requerido");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            claseController.destroy(id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NonexistentEntityException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        }
    }
}
