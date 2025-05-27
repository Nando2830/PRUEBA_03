/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import dao.exceptions.PreexistingEntityException;
import dto.Estudiante;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Inscripcion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author FERNANDO
 */
public class EstudianteJpaController implements Serializable {

    public EstudianteJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Estudiante estudiante) throws PreexistingEntityException, Exception {
        if (estudiante.getInscripcionCollection() == null) {
            estudiante.setInscripcionCollection(new ArrayList<Inscripcion>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Inscripcion> attachedInscripcionCollection = new ArrayList<Inscripcion>();
            for (Inscripcion inscripcionCollectionInscripcionToAttach : estudiante.getInscripcionCollection()) {
                inscripcionCollectionInscripcionToAttach = em.getReference(inscripcionCollectionInscripcionToAttach.getClass(), inscripcionCollectionInscripcionToAttach.getId());
                attachedInscripcionCollection.add(inscripcionCollectionInscripcionToAttach);
            }
            estudiante.setInscripcionCollection(attachedInscripcionCollection);
            em.persist(estudiante);
            for (Inscripcion inscripcionCollectionInscripcion : estudiante.getInscripcionCollection()) {
                Estudiante oldEstudianteIdOfInscripcionCollectionInscripcion = inscripcionCollectionInscripcion.getEstudianteId();
                inscripcionCollectionInscripcion.setEstudianteId(estudiante);
                inscripcionCollectionInscripcion = em.merge(inscripcionCollectionInscripcion);
                if (oldEstudianteIdOfInscripcionCollectionInscripcion != null) {
                    oldEstudianteIdOfInscripcionCollectionInscripcion.getInscripcionCollection().remove(inscripcionCollectionInscripcion);
                    oldEstudianteIdOfInscripcionCollectionInscripcion = em.merge(oldEstudianteIdOfInscripcionCollectionInscripcion);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findEstudiante(estudiante.getId()) != null) {
                throw new PreexistingEntityException("Estudiante " + estudiante + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Estudiante estudiante) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Estudiante persistentEstudiante = em.find(Estudiante.class, estudiante.getId());
            Collection<Inscripcion> inscripcionCollectionOld = persistentEstudiante.getInscripcionCollection();
            Collection<Inscripcion> inscripcionCollectionNew = estudiante.getInscripcionCollection();
            Collection<Inscripcion> attachedInscripcionCollectionNew = new ArrayList<Inscripcion>();
            for (Inscripcion inscripcionCollectionNewInscripcionToAttach : inscripcionCollectionNew) {
                inscripcionCollectionNewInscripcionToAttach = em.getReference(inscripcionCollectionNewInscripcionToAttach.getClass(), inscripcionCollectionNewInscripcionToAttach.getId());
                attachedInscripcionCollectionNew.add(inscripcionCollectionNewInscripcionToAttach);
            }
            inscripcionCollectionNew = attachedInscripcionCollectionNew;
            estudiante.setInscripcionCollection(inscripcionCollectionNew);
            estudiante = em.merge(estudiante);
            for (Inscripcion inscripcionCollectionOldInscripcion : inscripcionCollectionOld) {
                if (!inscripcionCollectionNew.contains(inscripcionCollectionOldInscripcion)) {
                    inscripcionCollectionOldInscripcion.setEstudianteId(null);
                    inscripcionCollectionOldInscripcion = em.merge(inscripcionCollectionOldInscripcion);
                }
            }
            for (Inscripcion inscripcionCollectionNewInscripcion : inscripcionCollectionNew) {
                if (!inscripcionCollectionOld.contains(inscripcionCollectionNewInscripcion)) {
                    Estudiante oldEstudianteIdOfInscripcionCollectionNewInscripcion = inscripcionCollectionNewInscripcion.getEstudianteId();
                    inscripcionCollectionNewInscripcion.setEstudianteId(estudiante);
                    inscripcionCollectionNewInscripcion = em.merge(inscripcionCollectionNewInscripcion);
                    if (oldEstudianteIdOfInscripcionCollectionNewInscripcion != null && !oldEstudianteIdOfInscripcionCollectionNewInscripcion.equals(estudiante)) {
                        oldEstudianteIdOfInscripcionCollectionNewInscripcion.getInscripcionCollection().remove(inscripcionCollectionNewInscripcion);
                        oldEstudianteIdOfInscripcionCollectionNewInscripcion = em.merge(oldEstudianteIdOfInscripcionCollectionNewInscripcion);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = estudiante.getId();
                if (findEstudiante(id) == null) {
                    throw new NonexistentEntityException("The estudiante with id " + id + " no longer exists.");
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
            Estudiante estudiante;
            try {
                estudiante = em.getReference(Estudiante.class, id);
                estudiante.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The estudiante with id " + id + " no longer exists.", enfe);
            }
            Collection<Inscripcion> inscripcionCollection = estudiante.getInscripcionCollection();
            for (Inscripcion inscripcionCollectionInscripcion : inscripcionCollection) {
                inscripcionCollectionInscripcion.setEstudianteId(null);
                inscripcionCollectionInscripcion = em.merge(inscripcionCollectionInscripcion);
            }
            em.remove(estudiante);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Estudiante> findEstudianteEntities() {
        return findEstudianteEntities(true, -1, -1);
    }

    public List<Estudiante> findEstudianteEntities(int maxResults, int firstResult) {
        return findEstudianteEntities(false, maxResults, firstResult);
    }

    private List<Estudiante> findEstudianteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Estudiante.class));
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

    public Estudiante findEstudiante(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Estudiante.class, id);
        } finally {
            em.close();
        }
    }

    public int getEstudianteCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Estudiante> rt = cq.from(Estudiante.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
