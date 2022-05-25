package it.polito.tdp.bar.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import it.polito.tdp.bar.model.Event.EventType;


public class Simulator {
	//Modello
	private List<Tavolo> tavoli;
	
	//Parametri
	private int NUM_EVENTI = 2000;
	private int T_ARRIVO_MAX = 10;
	private int NUM_PERSONE_MAX = 10;
	private int DURATA_MIN = 60;
	private int DURATA_MAX = 120;
	private double TOLLERANZA_MAX = 0.9;
	private double OCCUPAZIONE_MAX = 0.5;
	
	//Coda degli eventi
	private PriorityQueue<Event> coda;
	
	//Output
	private Statistiche stats;
	
	public void init() {
		coda = new PriorityQueue<Event>();
		stats = new Statistiche();
		tavoli = new ArrayList<Tavolo>();
		
		creaTavoli();
		creaEventi();
	}

	private void creaEventi() {
		Duration arrivo = Duration.ofMinutes(0);
		for(int i=0; i<NUM_EVENTI; i++) {
			int nPersone = (int) (Math.random() * NUM_PERSONE_MAX + 1);
			Duration durata = Duration.ofMinutes((int) (DURATA_MIN+Math.random()*(DURATA_MAX-DURATA_MIN+1)));
			double tolleranza = Math.random()*TOLLERANZA_MAX;
			
			Event e = new Event(EventType.ARRIVO_GRUPPO_CLIENTI, arrivo,
					nPersone, durata, tolleranza, null);
			coda.add(e);
			 
			arrivo = arrivo.plusMinutes((int) (Math.random()*T_ARRIVO_MAX+1));
		}
	}

	private void creaTavoli() {
		creaTavolo(2,10);
		creaTavolo(4,8);
		creaTavolo(4,6);
		creaTavolo(5,4);
		
		Collections.sort(tavoli,Comparator.comparing(Tavolo::getPosti));
	}
	
	private void creaTavolo(int q, int d) {
		for(int i=0; i<q; i++)
			tavoli.add(new Tavolo(d,false));	
	}
	
	public void run() {
		while(!coda.isEmpty()) {
			Event e = coda.poll();
			processaEvento(e);
		}
	}

	private void processaEvento(Event e) {
		switch(e.getType()) {
		case ARRIVO_GRUPPO_CLIENTI: 
			stats.incrementaClientiTot(e.getnPersone());
			
			Tavolo tavolo = null;
			for(Tavolo t : tavoli) {
				if(t.isOccupato()==false 
						&& e.getnPersone()>=OCCUPAZIONE_MAX*t.getPosti()
						&& t.getPosti()>=e.getnPersone()) {
					tavolo = t;
					break;
				}
			}
			
			if(tavolo!=null) {
				System.out.format("Trovato un tavolo da %d posti per %d persone", tavolo.getPosti(), e.getnPersone());
				stats.incrementaClientiSoddisfatti(e.getnPersone());
				tavolo.setOccupato(true);
				e.setTavolo(tavolo);
				
				coda.add(new Event(EventType.TAVOLO_LIBERATO, e.getTime().plus(e.getDurata()), e.getnPersone(), e.getDurata(), e.getTolleranza(), e.getTavolo()));
			} else {
				double bancone = Math.random();
				if(bancone<=e.getTolleranza()) {
					stats.incrementaClientiSoddisfatti(e.getnPersone());
				} else {
					stats.incrementaClientiInsoddisfatti(e.getnPersone());
				}
			}
			
			break;
		case TAVOLO_LIBERATO:
			e.getTavolo().setOccupato(false);
			break;
		}
		
	}

	public Statistiche getStats() {
		return stats;
	}
	
	
}
