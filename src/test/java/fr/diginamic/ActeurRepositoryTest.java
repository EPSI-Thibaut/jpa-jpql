package fr.diginamic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

public class ActeurRepositoryTest {

	private static EntityManagerFactory emf;
	private EntityManager em;

	@BeforeEach
	public void ouvertureEm() {
		em = emf.createEntityManager();
	}

	@AfterEach
	public void fermetureEm() {
		em.close();
	}

	@BeforeAll
	public static void initDatabase() {
		emf = Persistence.createEntityManagerFactory("movie_db");
		EntityManager em = emf.createEntityManager();

		try {
			if (em.createQuery("FROM Acteur").getResultList().isEmpty()) {
				em.getTransaction().begin();
				Path home = Paths.get(ActeurRepositoryTest.class.getClassLoader().getResource("data.sql").toURI());
				String[] queries = Files.readAllLines(home).stream().collect(Collectors.joining("\n")).split(";");
				for (String query : queries) {
					em.createNativeQuery(query).executeUpdate();
				}
				em.getTransaction().commit();
			}
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
		em.close();
	}

	@AfterAll
	public static void fermetureRessources() {
		emf.close();
	}

	/**
	 * Extraire tous les acteurs triés dans l'ordre alphabétique des identités
	 */
	@Test
	public void testExtraireActeursTriesParIdentite() {
		TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a ORDER BY a.identite ASC", Acteur.class);
		List<Acteur> acteurs = query.getResultList();

		assertEquals(1137, acteurs.size());
		assertEquals("A.J. Danna", acteurs.get(0).getIdentite());
	}

	/**
	 * Extraire l'actrice appelée Marion Cotillard
	 */
	@Test
	public void testExtraireActeursParIdentite() {
		TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a WHERE a.identite = 'Marion Cotillard'", Acteur.class);
		List<Acteur> acteurs = query.getResultList();

		assertEquals(1, acteurs.size());
		assertEquals("Marion Cotillard", acteurs.get(0).getIdentite());
	}

	/**
	 * Extraire la liste des acteurs dont l'année de naissance est 1985. Astuce:
	 * fonction year(...)
	 */
	@Test
	public void testExtraireActeursParAnneeNaissance() {
		TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a WHERE year(a.anniversaire) = 1985", Acteur.class);
		List<Acteur> acteurs = query.getResultList();

		assertEquals(10, acteurs.size());
	}

	/**
	 * Extraire la liste des actrices ayant joué le rôle d'Harley Quinn
	 */
	@Test
	public void testExtraireActeursParRole() {
		TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a JOIN a.roles r WHERE r.nom = 'Harley Quinn'", Acteur.class);
		List<Acteur> acteurs = query.getResultList();

		assertEquals(1, acteurs.size());
		assertEquals("Margot Robbie", acteurs.get(0).getIdentite());
	}

	/**
	 * Extraire la liste de tous les acteurs ayant joué dans un film paru en 2015.
	 */
	@Test
	public void testExtraireActeursParFilmParuAnnee() {
		TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a JOIN a.roles r JOIN r.film f WHERE f.annee = 2015", Acteur.class);
		List<Acteur> acteurs = query.getResultList();

		assertEquals(119, acteurs.size());
	}

	/**
	 * Extraire la liste de tous les acteurs ayant joué dans un film dont le pays d'origine est France
	 */
	@Test
	public void testExtraireActeursParPays() {
		TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a JOIN a.roles r JOIN r.film f JOIN f.pays p WHERE p.nom = 'France'", Acteur.class);
		List<Acteur> acteurs = query.getResultList();

		assertEquals(158, acteurs.size());
	}

	/**
	 * Extraire la liste de tous les acteurs ayant joué dans un film paru en 2017 et dont le pays d'origine
	 * est France
	 */
	@Test
	public void testExtraireActeursParListePaysEtAnnee() {
		TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a JOIN a.roles r JOIN r.film f JOIN f.pays p WHERE f.annee = 2017 AND p.nom = 'France'", Acteur.class);
		List<Acteur> acteurs = query.getResultList();

		assertEquals(24, acteurs.size());
	}

	/**
	 * Extraire la liste de tous les acteurs ayant joué dans un film réalisé par
	 * Ridley Scott entre les années 2010 et 2020
	 */
	@Test
	public void testExtraireParRealisateurEntreAnnee() {
		TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a JOIN a.roles r JOIN r.film f JOIN f.realisateurs real WHERE real.identite = 'Ridley Scott' AND f.annee BETWEEN 2010 AND 2020", Acteur.class);
		List<Acteur> acteurs = query.getResultList();

		assertEquals(27, acteurs.size());
	}

	/**
	 * Extraire la liste de tous les réalisateurs ayant réalisé un film dans lequel Brad Pitt a joué
	 */
	@Test
	public void testExtraireRealisateursParActeur() {
		TypedQuery<Realisateur> query = em.createQuery("SELECT DISTINCT r FROM Realisateur r JOIN r.films f JOIN f.roles ro WHERE ro.acteur.identite = 'Brad Pitt'", Realisateur.class);
		List<Realisateur> realisateurs = query.getResultList();

		assertEquals(6, realisateurs.size());
	}
}
