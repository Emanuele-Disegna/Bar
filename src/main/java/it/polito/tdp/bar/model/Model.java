package it.polito.tdp.bar.model;

public class Model {
	private Simulator s;
	
	public Model() {
		s = new Simulator();
	}
	
	public void simula() {
		s.init();
		s.run();
	}
	
	public Statistiche getStats() {
		return s.getStats();
	}
}
