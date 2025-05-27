/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import dao.exceptions.PreexistingEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Estudiante;
import dto.Clase;
import dto.Inscripcion;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author FERNANDO
 */
public class InscripcionJpaController implements Serializable {

    public InscripcionJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Inscripcion inscripcion) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Estudiante estudianteId = inscripcion.getEstudianteId();
            if (estudianteId != null) {
                estudianteId = em.getReference(estudianteId.getClass(), estudianteId.getId());
                inscripcion.setEstudianteId(estudianteId);
            }
            Clase estudianteId1 = inscripcion.getEstudianteId1();
            if (estudianteId1 != null) {
                estudianteId1 = em.getReference(estudianteId1.getClass(), estudianteId1.getId());
                inscripcion.setEstudianteId1(estudianteId1);
            }
            em.persist(inscripcion);
            if (estudianteId != null) {
                estudianteId.getInscripcionCollection().add(inscripcion);
                estudianteId = em.merge(estudianteId);
            }
            if (estudianteId1 != null) {
                estudianteId1.getInscripcionCollection().add(inscripcion);
                estudianteId1 = em.merge(estudianteId1);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findInscripcion(inscripcion.getId()) != null) {
                throw new PreexistingEntityException("Inscripcion " + inscripcion + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Inscripcion inscripcion) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Inscripcion persistentInscripcion = em.find(Inscripcion.class, inscripcion.getId());
            Estudiante estudianteIdOld = persistentInscripcion.getEstudianteId();
            Estudiante estudianteIdNew = inscripcion.getEstudianteId();
            Clase estudianteId1Old = persistentInscripcion.getEstudianteId1();
            Clase estudianteId1New = inscripcion.getEstudianteId1();
            if (estudianteIdNew != null) {
                estudianteIdNew = em.getReference(estudianteIdNew.getClass(), estudianteIdNew.getId());
                inscripcion.setEstudianteId(estudianteIdNew);
            }
            if (estudianteId1New != null) {
                estudianteId1New = em.getReference(estudianteId1New.getClass(), estudianteId1New.getId());
                inscripcion.setEstudianteId1(estudianteId1New);
            }
            inscripcion = em.merge(inscripcion);
            if (estudianteIdOld != null && !estudianteIdOld.equals(estudianteIdNew)) {
                estudianteIdOld.getInscripcionCollection().remove(inscripcion);
                estudianteIdOld = em.merge(estudianteIdOld);
            }
            if (estudianteIdNew != null && !estudianteIdNew.equals(estudianteIdOld)) {
                estudianteIdNew.getInscripcionCollection().add(inscripcion);
                estudianteIdNew = em.merge(estudianteIdNew);
            }
            if (estudianteId1Old != null && !estudianteId1Old.equals(estudianteId1New)) {
                estudianteId1Old.getInscripcionCollection().remove(inscripcion);
                estudianteId1Old = em.merge(estudianteId1Old);
            }
            if (estudianteId1New != null && !estudianteId1New.equals(estudianteId1Old)) {
                estudianteId1New.getInscripcionCollection().add(inscripcion);
                estudianteId1New = em.merge(estudianteId1New);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = inscripcion.getId();
                if (findInscripcion(id) == null) {
                    throw new NonexistentEntityException("The inscripcion with id " + id + " no longer exists.");
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
            Inscripcion inscripcion;
            try {
                inscripcion = em.getReference(Inscripcion.class, id);
                inscripcion.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The inscripcion with id " + id + " no longer exists.", enfe);
            }
            Estudiante estudianteId = inscripcion.getEstudianteId();
            if (estudianteId != null) {
                estudianteId.getInscripcionCollection().remove(inscripcion);
                estudianteId = em.merge(estudianteId);
            }
            Clase estudianteId1 = inscripcion.getEstudianteId1();
            if (estudianteId1 != null) {
                estudianteId1.getInscripcionCollection().remove(inscripcion);
                estudianteId1 = em.merge(estudianteId1);
            }
            em.remove(inscripcion);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Inscripcion> findInscripcionEntities() {
        return findInscripcionEntities(true, -1, -1);
    }

    public List<Inscripcion> findInscripcionEntities(int maxResults, int firstResult) {
        return findInscripcionEntities(false, maxResults, firstResult);
    }

    private List<Inscripcion> findInscripcionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Inscripcion.class));
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

    public Inscripcion findInscripcion(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Inscripcion.class, id);
        } finally {
            em.close();
        }
    }

    public int getInscripcionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Inscripcion> rt = cq.from(Inscripcion.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
