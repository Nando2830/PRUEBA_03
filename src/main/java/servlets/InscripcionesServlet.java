package servlets;

import dao.InscripcionJpaController;
import dto.Inscripcion;
import dto.Clase;
import dto.Estudiante;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import javax.json.*;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/inscripciones")
public class InscripcionesServlet extends HttpServlet {
    private InscripcionJpaController controller;

    @Override
    public void init() throws ServletException {
        controller = new InscripcionJpaController(Persistence.createEntityManagerFactory("com.mycompany_PRUEBA_03_war_1.0-SNAPSHOTPU"));
    }

    private JsonObject toJson(Inscripcion inscripcion) {
        return Json.createObjectBuilder()
            .add("id", inscripcion.getId())
            .add("fechaInscripcion", new SimpleDateFormat("yyyy-MM-dd").format(inscripcion.getFechaInscripcion()))
            .add("claseId", inscripcion.getClaseId().getId())
            .add("estudianteId", inscripcion.getEstudianteId().getId())
            .build();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        List<Inscripcion> inscripciones = controller.findInscripcionEntities();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (Inscripcion i : inscripciones) {
            arrayBuilder.add(toJson(i));
        }

        try (PrintWriter out = response.getWriter()) {
            out.print(Json.createObjectBuilder().add("data", arrayBuilder).build().toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        JsonReader reader = Json.createReader(request.getInputStream());
        JsonObject json = reader.readObject();

        try {
            Inscripcion i = new Inscripcion();
            i.setId(json.getInt("id"));

            Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(json.getString("fechaInscripcion"));
            i.setFechaInscripcion(fecha);

            i.setClaseId(new Clase(json.getInt("claseId")));
            i.setEstudianteId(new Estudiante(json.getInt("estudianteId")));

            controller.create(i);

            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        JsonReader reader = Json.createReader(request.getInputStream());
        JsonObject json = reader.readObject();

        try {
            Inscripcion i = controller.findInscripcion(json.getInt("id"));

            if (i == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No encontrado");
                return;
            }

            i.setFechaInscripcion(new SimpleDateFormat("yyyy-MM-dd").parse(json.getString("fechaInscripcion")));
            i.setClaseId(new Clase(json.getInt("claseId")));
            i.setEstudianteId(new Estudiante(json.getInt("estudianteId")));

            controller.edit(i);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        JsonReader reader = Json.createReader(request.getInputStream());
        JsonObject json = reader.readObject();

        try {
            controller.destroy(json.getInt("id"));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
