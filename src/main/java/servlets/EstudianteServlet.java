package servlets;

import dao.EstudianteJpaController;
import dao.exceptions.NonexistentEntityException;
import dto.Estudiante;

import javax.json.*;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "EstudianteServlet", urlPatterns = {"/estudiantes"})
public class EstudianteServlet extends HttpServlet {

    private EstudianteJpaController estudianteDAO;

    @Override
    public void init() throws ServletException {
        estudianteDAO = new EstudianteJpaController(Persistence.createEntityManagerFactory("com.mycompany_PRUEBA_03_war_1.0-SNAPSHOTPU"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String idParam = request.getParameter("id");

        try (PrintWriter out = response.getWriter()) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            if (idParam != null) {
                int id = Integer.parseInt(idParam);
                Estudiante est = estudianteDAO.findEstudiante(id);
                if (est != null) {
                    out.print(estudianteToJson(est).toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Estudiante no encontrado\"}");
                }
            } else {
                List<Estudiante> estudiantes = estudianteDAO.findEstudianteEntities();
                for (Estudiante e : estudiantes) {
                    arrayBuilder.add(estudianteToJson(e));
                }
                out.print(arrayBuilder.build().toString());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (InputStream is = request.getInputStream()) {
            JsonObject json = Json.createReader(is).readObject();

            Estudiante estudiante = new Estudiante();
            estudiante.setId(json.getInt("id"));
            estudiante.setNombre(json.getString("nombre"));
            estudiante.setCorreo(json.getString("correo"));

            estudianteDAO.create(estudiante);

            response.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"message\":\"Estudiante creado exitosamente\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Error al crear estudiante: " + e.getMessage() + "\"}");
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (InputStream is = request.getInputStream()) {
            JsonObject json = Json.createReader(is).readObject();

            int id = json.getInt("id");
            Estudiante estudiante = estudianteDAO.findEstudiante(id);
            if (estudiante == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"error\":\"Estudiante no encontrado\"}");
                }
                return;
            }

            estudiante.setNombre(json.getString("nombre"));
            estudiante.setCorreo(json.getString("correo"));

            estudianteDAO.edit(estudiante);

            try (PrintWriter out = response.getWriter()) {
                out.print("{\"message\":\"Estudiante actualizado exitosamente\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Error al actualizar estudiante: " + e.getMessage() + "\"}");
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");

        try (PrintWriter out = response.getWriter()) {
            if (idParam == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"ID requerido para eliminar\"}");
                return;
            }

            int id = Integer.parseInt(idParam);
            estudianteDAO.destroy(id);

            out.print("{\"message\":\"Estudiante eliminado exitosamente\"}");
        } catch (NonexistentEntityException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Estudiante no encontrado\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Error al eliminar estudiante: " + e.getMessage() + "\"}");
            }
        }
    }

    private JsonObject estudianteToJson(Estudiante e) {
        return Json.createObjectBuilder()
                .add("id", e.getId())
                .add("nombre", e.getNombre())
                .add("correo", e.getCorreo())
                .build();
    }
}
