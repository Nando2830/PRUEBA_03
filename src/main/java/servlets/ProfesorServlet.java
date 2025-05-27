package servlets;

import dao.ProfesorJpaController;
import dao.exceptions.NonexistentEntityException;
import dto.Profesor;

import javax.json.*;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/profesores")
public class ProfesorServlet extends HttpServlet {

    private ProfesorJpaController profesorDAO;

    @Override
    public void init() throws ServletException {
        profesorDAO = new ProfesorJpaController(
                Persistence.createEntityManagerFactory("com.mycompany_PRUEBA_03_war_1.0-SNAPSHOTPU")
        );
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        List<Profesor> profesores = profesorDAO.findProfesorEntities();

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (Profesor p : profesores) {
            JsonObjectBuilder obj = Json.createObjectBuilder()
                .add("id", p.getId())
                .add("nombre", p.getNombre() != null ? p.getNombre() : "")
                .add("especialidad", p.getEspecialidad() != null ? p.getEspecialidad() : "");
            arrayBuilder.add(obj);
        }

        try (PrintWriter out = resp.getWriter()) {
            JsonWriter writer = Json.createWriter(out);
            writer.writeArray(arrayBuilder.build());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        JsonReader reader = Json.createReader(req.getInputStream());
        JsonObject json = reader.readObject();

        try {
            Profesor p = new Profesor();
            p.setId(json.getInt("id"));
            p.setNombre(json.getString("nombre", ""));
            p.setEspecialidad(json.getString("especialidad", ""));

            profesorDAO.create(p);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        JsonReader reader = Json.createReader(req.getInputStream());
        JsonObject json = reader.readObject();

        try {
            int id = json.getInt("id");
            Profesor p = profesorDAO.findProfesor(id);

            if (p == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Profesor no encontrado");
                return;
            }

            p.setNombre(json.getString("nombre", ""));
            p.setEspecialidad(json.getString("especialidad", ""));

            profesorDAO.edit(p);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (NonexistentEntityException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID requerido");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            profesorDAO.destroy(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NonexistentEntityException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        } catch (NumberFormatException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        }
    }
}
