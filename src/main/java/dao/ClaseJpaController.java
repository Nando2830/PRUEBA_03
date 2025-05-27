/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import dao.exceptions.PreexistingEntityException;
import dto.Clase;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Profesor;
import dto.Salon;
import dto.Inscripcion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author NITRO
 */
public class ClaseJpaController implements Serializable {

    public ClaseJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Clase clase) throws PreexistingEntityException, Exception {
        if (clase.getInscripcionCollection() == null) {
            clase.setInscripcionCollection(new ArrayList<Inscripcion>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Profesor profesorId = clase.getProfesorId();
            if (profesorId != null) {
                profesorId = em.getReference(profesorId.getClass(), profesorId.getId());
                clase.setProfesorId(profesorId);
            }
            Salon salonId = clase.getSalonId();
            if (salonId != null) {
                salonId = em.getReference(salonId.getClass(), salonId.getId());
                clase.setSalonId(salonId);
            }
            Collection<Inscripcion> attachedInscripcionCollection = new ArrayList<Inscripcion>();
            for (Inscripcion inscripcionCollectionInscripcionToAttach : clase.getInscripcionCollection()) {
                inscripcionCollectionInscripcionToAttach = em.getReference(inscripcionCollectionInscripcionToAttach.getClass(), inscripcionCollectionInscripcionToAttach.getId());
                attachedInscripcionCollection.add(inscripcionCollectionInscripcionToAttach);
            }
            clase.setInscripcionCollection(attachedInscripcionCollection);
            em.persist(clase);
            if (profesorId != null) {
                profesorId.getClaseCollection().add(clase);
                profesorId = em.merge(profesorId);
            }
            if (salonId != null) {
                salonId.getClaseCollection().add(clase);
                salonId = em.merge(salonId);
            }
            for (Inscripcion inscripcionCollectionInscripcion : clase.getInscripcionCollection()) {
                Clase oldClaseIdOfInscripcionCollectionInscripcion = inscripcionCollectionInscripcion.getClaseId();
                inscripcionCollectionInscripcion.setClaseId(clase);
                inscripcionCollectionInscripcion = em.merge(inscripcionCollectionInscripcion);
                if (oldClaseIdOfInscripcionCollectionInscripcion != null) {
                    oldClaseIdOfInscripcionCollectionInscripcion.getInscripcionCollection().remove(inscripcionCollectionInscripcion);
                    oldClaseIdOfInscripcionCollectionInscripcion = em.merge(oldClaseIdOfInscripcionCollectionInscripcion);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findClase(clase.getId()) != null) {
                throw new PreexistingEntityException("Clase " + clase + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Clase clase) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Clase persistentClase = em.find(Clase.class, clase.getId());
            Profesor profesorIdOld = persistentClase.getProfesorId();
            Profesor profesorIdNew = clase.getProfesorId();
            Salon salonIdOld = persistentClase.getSalonId();
            Salon salonIdNew = clase.getSalonId();
            Collection<Inscripcion> inscripcionCollectionOld = persistentClase.getInscripcionCollection();
            Collection<Inscripcion> inscripcionCollectionNew = clase.getInscripcionCollection();
            if (profesorIdNew != null) {
                profesorIdNew = em.getReference(profesorIdNew.getClass(), profesorIdNew.getId());
                clase.setProfesorId(profesorIdNew);
            }
            if (salonIdNew != null) {
                salonIdNew = em.getReference(salonIdNew.getClass(), salonIdNew.getId());
                clase.setSalonId(salonIdNew);
            }
            Collection<Inscripcion> attachedInscripcionCollectionNew = new ArrayList<Inscripcion>();
            for (Inscripcion inscripcionCollectionNewInscripcionToAttach : inscripcionCollectionNew) {
                inscripcionCollectionNewInscripcionToAttach = em.getReference(inscripcionCollectionNewInscripcionToAttach.getClass(), inscripcionCollectionNewInscripcionToAttach.getId());
                attachedInscripcionCollectionNew.add(inscripcionCollectionNewInscripcionToAttach);
            }
            inscripcionCollectionNew = attachedInscripcionCollectionNew;
            clase.setInscripcionCollection(inscripcionCollectionNew);
            clase = em.merge(clase);
            if (profesorIdOld != null && !profesorIdOld.equals(profesorIdNew)) {
                profesorIdOld.getClaseCollection().remove(clase);
                profesorIdOld = em.merge(profesorIdOld);
            }
            if (profesorIdNew != null && !profesorIdNew.equals(profesorIdOld)) {
                profesorIdNew.getClaseCollection().add(clase);
                profesorIdNew = em.merge(profesorIdNew);
            }
            if (salonIdOld != null && !salonIdOld.equals(salonIdNew)) {
                salonIdOld.getClaseCollection().remove(clase);
                salonIdOld = em.merge(salonIdOld);
            }
            if (salonIdNew != null && !salonIdNew.equals(salonIdOld)) {
                salonIdNew.getClaseCollection().add(clase);
                salonIdNew = em.merge(salonIdNew);
            }
            for (Inscripcion inscripcionCollectionOldInscripcion : inscripcionCollectionOld) {
                if (!inscripcionCollectionNew.contains(inscripcionCollectionOldInscripcion)) {
                    inscripcionCollectionOldInscripcion.setClaseId(null);
                    inscripcionCollectionOldInscripcion = em.merge(inscripcionCollectionOldInscripcion);
                }
            }
            for (Inscripcion inscripcionCollectionNewInscripcion : inscripcionCollectionNew) {
                if (!inscripcionCollectionOld.contains(inscripcionCollectionNewInscripcion)) {
                    Clase oldClaseIdOfInscripcionCollectionNewInscripcion = inscripcionCollectionNewInscripcion.getClaseId();
                    inscripcionCollectionNewInscripcion.setClaseId(clase);
                    inscripcionCollectionNewInscripcion = em.merge(inscripcionCollectionNewInscripcion);
                    if (oldClaseIdOfInscripcionCollectionNewInscripcion != null && !oldClaseIdOfInscripcionCollectionNewInscripcion.equals(clase)) {
                        oldClaseIdOfInscripcionCollectionNewInscripcion.getInscripcionCollection().remove(inscripcionCollectionNewInscripcion);
                        oldClaseIdOfInscripcionCollectionNewInscripcion = em.merge(oldClaseIdOfInscripcionCollectionNewInscripcion);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = clase.getId();
                if (findClase(id) == null) {
                    throw new NonexistentEntityException("The clase with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Clase clase;
            try {
                clase = em.getReference(Clase.class, id);
                clase.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The clase with id " + id + " no longer exists.", enfe);
            }
            Profesor profesorId = clase.getProfesorId();
            if (profesorId != null) {
                profesorId.getClaseCollection().remove(clase);
                profesorId = em.merge(profesorId);
            }
            Salon salonId = clase.getSalonId();
            if (salonId != null) {
                salonId.getClaseCollection().remove(clase);
                salonId = em.merge(salonId);
            }
            Collection<Inscripcion> inscripcionCollection = clase.getInscripcionCollection();
            for (Inscripcion inscripcionCollectionInscripcion : inscripcionCollection) {
                inscripcionCollectionInscripcion.setClaseId(null);
                inscripcionCollectionInscripcion = em.merge(inscripcionCollectionInscripcion);
            }
            em.remove(clase);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Clase> findClaseEntities() {
        return findClaseEntities(true, -1, -1);
    }

    public List<Clase> findClaseEntities(int maxResults, int firstResult) {
        return findClaseEntities(false, maxResults, firstResult);
    }

    private List<Clase> findClaseEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Clase.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Clase findClase(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Clase.class, id);
        } finally {
            em.close();
        }
    }

    public int getClaseCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Clase> rt = cq.from(Clase.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
