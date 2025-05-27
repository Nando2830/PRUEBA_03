package servlets;

import dao.ClaseJpaController;
import dto.Clase;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import javax.json.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/ClaseServlet")
public class ClaseServlet extends HttpServlet {

    private ClaseJpaController claseDAO;

    @Override
    public void init() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_PRUEBA_03_war_1.0-SNAPSHOTPU");
        claseDAO = new ClaseJpaController(emf);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        List<Clase> clases = claseDAO.findClaseEntities();
        for (Clase clase : clases) {
            jsonArray.add(Json.createObjectBuilder()
                    .add("id", clase.getId())
                    .add("nombre", clase.getNombre())
            );
        }

        try (JsonWriter writer = Json.createWriter(response.getWriter())) {
            writer.writeArray(jsonArray.build());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonReader reader = Json.createReader(request.getReader());
        JsonObject obj = reader.readObject();

        Clase clase = new Clase();
        clase.setId(obj.getInt("id"));
        clase.setNombre(obj.getString("nombre"));

        try {
            claseDAO.create(clase);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error al crear: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonReader reader = Json.createReader(request.getReader());
        JsonObject obj = reader.readObject();

        Clase clase = new Clase();
        clase.setId(obj.getInt("id"));
        clase.setNombre(obj.getString("nombre"));

        try {
            claseDAO.edit(clase);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error al actualizar: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonReader reader = Json.createReader(request.getReader());
        JsonObject obj = reader.readObject();
        int id = obj.getInt("id");

        try {
            claseDAO.destroy(id);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error al eliminar: " + e.getMessage());
        }
    }
}
