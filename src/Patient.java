
public class Patient 
{
	private int numero;
	private double heureArrivee;
	private double tempsAttente;
	private double heureSortie;
	private int nbrePatientFile;
	
	public Patient(int noPatient, double p_heureArrivee)
	{
		setNumero(noPatient);
		setHeureArrivee(p_heureArrivee);
		
	}
	
	public Patient(int numero, double p_heureArrivee, double p_tempsAttente, double p_heureSortie, int p_nbrePatientFile)
	{
		setNumero(numero);
		setHeureArrivee(p_heureArrivee);
		setTempsAttente(p_tempsAttente);
		setHeureSortie(p_heureSortie);
		setNbrePatientFile(p_nbrePatientFile);
	}
	
	public int getNumero()
	{
		return numero;
	}
	
	public boolean setNumero(int p_numero)
	{
		boolean ok = false;
		
		if (validerNumero(p_numero))
		{
			numero = p_numero;
			ok=true;
		}
		
		return ok;
	}
	
	public boolean validerNumero(int p_numero)
	{
		boolean ok = false;
		
		if (p_numero >=0)
		{
			ok = true;
		}
		
		return ok;
	}
	
	public double getHeureArrivee()
	{
		return heureArrivee;
	}
	
	public boolean setHeureArrivee(double p_HeureArrivee)
	{
		boolean ok = false;
		
		if (validerHeureArrivee(p_HeureArrivee))
		{
			heureArrivee = p_HeureArrivee;
			ok=true;
		}
		
		return ok;
	}
	
	public boolean validerHeureArrivee(double p_HeureArrivee)
	{
		boolean ok = false;
		
		if (p_HeureArrivee >= 0)//Après 18h00 (570min), on ne prends plus de nouveaux patients
		{
			ok = true;
		}
		
		return ok;
	}
	
	public double getTempsAttente()
	{
		return tempsAttente;
	}
	
	public void setTempsAttente(double p_tempsAttente)
	{
		tempsAttente = p_tempsAttente;
	}
	
	public double getHeureSortie()
	{
		return heureSortie;
	}
	
	public boolean setHeureSortie(double p_HeureSortie)
	{
		boolean ok = false;
		
		if (validerHeureSortie())
		{
			heureSortie = p_HeureSortie;
			ok = true;
		}
		
		return ok;
	}
	
	public boolean validerHeureSortie()
	{
		return heureSortie >= 0 && heureSortie <= Simulation.HEURE_MAX_ADMISSION; //Rejeter les patients lorsque l'heure de sortie prévue dépasse 18h30 (570 min)
	}
	
	public int getNbrePatientFile()
	{
		return nbrePatientFile;
	}
	
	public void setNbrePatientFile(int nbre)
	{
		nbrePatientFile = nbre;
	}
	
	@Override
	public String toString() {
		
		return "Patient " + this.getNumero() + "\tArrivee: " + this.getHeureArrivee() + "\tAttente: " + this.getTempsAttente() + "\tSortie: " + this.getHeureSortie()+"\n";
	}
}