package servlets;

import dao.SalonJpaController;
import dao.exceptions.NonexistentEntityException;
import dto.Salon;

import javax.json.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
            JsonWriter jsonWriter = Json.createWriter(out);

            if (idParam != null) {
                try {
                    int id = Integer.parseInt(idParam);
                    Salon salon = salonController.findSalon(id);
                    if (salon != null) {
                        jsonWriter.writeObject(toJson(salon));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
                }
            } else {
                List<Salon> salones = salonController.findSalonEntities();
                JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                for (Salon salon : salones) {
                    arrayBuilder.add(toJson(salon));
                }
                jsonWriter.writeArray(arrayBuilder.build());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try (JsonReader reader = Json.createReader(request.getInputStream());
             PrintWriter out = response.getWriter()) {

            JsonObject json = reader.readObject();

            // Validaciones básicas de campos
            if (!json.containsKey("id") || !json.containsKey("numero") || !json.containsKey("capacidad")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Faltan campos obligatorios: id, numero, capacidad");
                return;
            }

            int id = json.getInt("id");
            String numero = json.getString("numero", "").trim();
            int capacidad = json.getInt("capacidad");

            if (numero.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El campo 'numero' no puede estar vacío");
                return;
            }

            // Verificar que no exista ya ese id
            if (salonController.findSalon(id) != null) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Ya existe un salón con ese ID");
                return;
            }

            Salon salon = new Salon();
            salon.setId(id);
            salon.setNumero(numero);
            salon.setCapacidad(capacidad);

            salonController.create(salon);
            response.setStatus(HttpServletResponse.SC_CREATED);

            out.write("{\"message\":\"Salón creado correctamente\"}");

        } catch (JsonException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JSON inválido o datos incompletos");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno al crear el salón");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try (JsonReader reader = Json.createReader(request.getInputStream());
             PrintWriter out = response.getWriter()) {

            JsonObject json = reader.readObject();

            if (!json.containsKey("id") || !json.containsKey("numero") || !json.containsKey("capacidad")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Faltan campos obligatorios: id, numero, capacidad");
                return;
            }

            int id = json.getInt("id");
            String numero = json.getString("numero", "").trim();
            int capacidad = json.getInt("capacidad");

            if (numero.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El campo 'numero' no puede estar vacío");
                return;
            }

            Salon salon = salonController.findSalon(id);

            if (salon == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Salón no encontrado");
                return;
            }

            salon.setNumero(numero);
            salon.setCapacidad(capacidad);

            salonController.edit(salon);
            response.setStatus(HttpServletResponse.SC_OK);

            out.write("{\"message\":\"Salón actualizado correctamente\"}");

        } catch (NonexistentEntityException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Salón no encontrado");
        } catch (JsonException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JSON inválido o datos incompletos");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno al actualizar el salón");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");

        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID requerido");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            salonController.destroy(id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NonexistentEntityException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Salón no encontrado");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
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
