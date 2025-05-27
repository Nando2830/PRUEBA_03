package servlets;

import dao.ProfesorJpaController;
import dto.Profesor;
import java.io.IOException;
import java.util.List;
import javax.json.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "ProfesorServlet", urlPatterns = {"/profesores"})
public class ProfesorServlet extends HttpServlet {

    private ProfesorJpaController profesorDAO;

    @Override
    public void init() throws ServletException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_PRUEBA_03_war_1.0-SNAPSHOTPU");
        profesorDAO = new ProfesorJpaController(emf);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (Profesor p : profesorDAO.findProfesorEntities()) {
            jsonArray.add(Json.createObjectBuilder()
                    .add("id", p.getId())
                    .add("nombre", p.getNombre())
                    .add("especialidad", p.getEspecialidad() != null ? p.getEspecialidad() : "")
            );
        }
        try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
            writer.writeArray(jsonArray.build());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject json = Json.createReader(req.getReader()).readObject();
        Profesor p = new Profesor();
        p.setId(json.getInt("id"));
        p.setNombre(json.getString("nombre"));
        p.setEspecialidad(json.getString("especialidad", ""));
        try {
            profesorDAO.create(p);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "No se pudo crear el profesor");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject json = Json.createReader(req.getReader()).readObject();
        Profesor p = new Profesor();
        p.setId(json.getInt("id"));
        p.setNombre(json.getString("nombre"));
        p.setEspecialidad(json.getString("especialidad", ""));
        try {
            profesorDAO.edit(p);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Profesor no encontrado");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        try {
            profesorDAO.destroy(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Profesor no encontrado");
        }
    }
}
