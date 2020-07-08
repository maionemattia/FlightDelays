package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private Map<Integer, Airport> idMap;
	private List<Airport> percorso;
	private ExtFlightDelaysDAO dao;

	private Map<Airport, Airport> visita = new HashMap<>();

	public Model() {
		idMap = new HashMap<>();
		dao = new ExtFlightDelaysDAO();
		this.dao.loadAllAirports(idMap);
	}

	public void creaGrafo(int x) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

		// Aggiungiamo i vertici
		List<Integer> idAirport = dao.loadAirports(x);
		for (Integer i : idAirport) {
			this.grafo.addVertex(idMap.get(i));
		}

		// Aggiungiamo archi
		for (Rotta r : dao.loadRotte(idMap)) {
			if (this.grafo.vertexSet().contains(r.a1) && this.grafo.vertexSet().contains(r.a2)) {
				DefaultWeightedEdge e = this.grafo.getEdge(r.a1, r.a2);
				if (e == null) {
					Graphs.addEdgeWithVertices(grafo, r.getA1(), r.getA2(), r.getPeso());
				} else {
					double pesoVecchio = this.grafo.getEdgeWeight(e);
					double pesoNuovo = pesoVecchio + r.getPeso();
					this.grafo.setEdgeWeight(e, pesoNuovo);
				}
			}
		}
	}

	public int nVertici() {
		return this.grafo.vertexSet().size();
	}

	public int nArchi() {
		return this.grafo.edgeSet().size();
	}

	public Collection<Airport> getAirport() {
		return this.grafo.vertexSet();
	}

	public List<Airport> getPercorso(Airport a1, Airport a2) {

		percorso = new ArrayList<>();

		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo, a1);

		visita.put(a1, null);

		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				Airport source = grafo.getEdgeSource(e.getEdge());
				Airport destinazione = grafo.getEdgeTarget(e.getEdge());
				if (!visita.containsKey(destinazione) && visita.containsKey(source)) {
					visita.put(destinazione, source);
				} else {
					if (!visita.containsKey(source) && visita.containsKey(destinazione)) {
						visita.put(source, destinazione);
					}
				}
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub

			}

		});
		while (it.hasNext()) {
			it.next();
		}

		if (!visita.containsKey(a1) || !visita.containsKey(a2)) {
			return null;
		} else {
			Airport step = a2;
			while (!step.equals(a1)) {
				percorso.add(step);
				step = visita.get(step);
			}
			percorso.add(a1);
			return percorso;
		}

	}

}
