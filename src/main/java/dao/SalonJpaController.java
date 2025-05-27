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
import dto.Clase;
import dto.Salon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author FERNANDO
 */
public class SalonJpaController implements Serializable {

    public SalonJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Salon salon) throws PreexistingEntityException, Exception {
        if (salon.getClaseCollection() == null) {
            salon.setClaseCollection(new ArrayList<Clase>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Clase> attachedClaseCollection = new ArrayList<Clase>();
            for (Clase claseCollectionClaseToAttach : salon.getClaseCollection()) {
                claseCollectionClaseToAttach = em.getReference(claseCollectionClaseToAttach.getClass(), claseCollectionClaseToAttach.getId());
                attachedClaseCollection.add(claseCollectionClaseToAttach);
            }
            salon.setClaseCollection(attachedClaseCollection);
            em.persist(salon);
            for (Clase claseCollectionClase : salon.getClaseCollection()) {
                Salon oldSalonIdOfClaseCollectionClase = claseCollectionClase.getSalonId();
                claseCollectionClase.setSalonId(salon);
                claseCollectionClase = em.merge(claseCollectionClase);
                if (oldSalonIdOfClaseCollectionClase != null) {
                    oldSalonIdOfClaseCollectionClase.getClaseCollection().remove(claseCollectionClase);
                    oldSalonIdOfClaseCollectionClase = em.merge(oldSalonIdOfClaseCollectionClase);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findSalon(salon.getId()) != null) {
                throw new PreexistingEntityException("Salon " + salon + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Salon salon) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Salon persistentSalon = em.find(Salon.class, salon.getId());
            Collection<Clase> claseCollectionOld = persistentSalon.getClaseCollection();
            Collection<Clase> claseCollectionNew = salon.getClaseCollection();
            Collection<Clase> attachedClaseCollectionNew = new ArrayList<Clase>();
            for (Clase claseCollectionNewClaseToAttach : claseCollectionNew) {
                claseCollectionNewClaseToAttach = em.getReference(claseCollectionNewClaseToAttach.getClass(), claseCollectionNewClaseToAttach.getId());
                attachedClaseCollectionNew.add(claseCollectionNewClaseToAttach);
            }
            claseCollectionNew = attachedClaseCollectionNew;
            salon.setClaseCollection(claseCollectionNew);
            salon = em.merge(salon);
            for (Clase claseCollectionOldClase : claseCollectionOld) {
                if (!claseCollectionNew.contains(claseCollectionOldClase)) {
                    claseCollectionOldClase.setSalonId(null);
                    claseCollectionOldClase = em.merge(claseCollectionOldClase);
                }
            }
            for (Clase claseCollectionNewClase : claseCollectionNew) {
                if (!claseCollectionOld.contains(claseCollectionNewClase)) {
                    Salon oldSalonIdOfClaseCollectionNewClase = claseCollectionNewClase.getSalonId();
                    claseCollectionNewClase.setSalonId(salon);
                    claseCollectionNewClase = em.merge(claseCollectionNewClase);
                    if (oldSalonIdOfClaseCollectionNewClase != null && !oldSalonIdOfClaseCollectionNewClase.equals(salon)) {
                        oldSalonIdOfClaseCollectionNewClase.getClaseCollection().remove(claseCollectionNewClase);
                        oldSalonIdOfClaseCollectionNewClase = em.merge(oldSalonIdOfClaseCollectionNewClase);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = salon.getId();
                if (findSalon(id) == null) {
                    throw new NonexistentEntityException("The salon with id " + id + " no longer exists.");
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
            Salon salon;
            try {
                salon = em.getReference(Salon.class, id);
                salon.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The salon with id " + id + " no longer exists.", enfe);
            }
            Collection<Clase> claseCollection = salon.getClaseCollection();
            for (Clase claseCollectionClase : claseCollection) {
                claseCollectionClase.setSalonId(null);
                claseCollectionClase = em.merge(claseCollectionClase);
            }
            em.remove(salon);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Salon> findSalonEntities() {
        return findSalonEntities(true, -1, -1);
    }

    public List<Salon> findSalonEntities(int maxResults, int firstResult) {
        return findSalonEntities(false, maxResults, firstResult);
    }

    private List<Salon> findSalonEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Salon.class));
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

    public Salon findSalon(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Salon.class, id);
        } finally {
            em.close();
        }
    }

    public int getSalonCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Salon> rt = cq.from(Salon.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
