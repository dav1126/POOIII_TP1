
public class Consultation 
{
	Patient patient;
	Serveur serveur;
	double heureDebut;
	double heureFin;
	
	public Consultation()
	{
		patient = null;
		serveur = null;
		heureDebut = -1;
		heureFin = -1;
	}
	
	public  Consultation(Patient p_patient, Serveur p_serveur, double p_heureDebut, double p_heureFin)
	{
		setPatient(p_patient);
		setServeur(p_serveur);
		setHeureDebut(p_heureDebut);
		setHeureFin(p_heureFin);
	}
	
	public Patient getPatient()
	{
		return patient;
	}
	
	public void setPatient(Patient p_patient)
	{
		patient = p_patient;
	}
	
	public Serveur getServeur()
	{
		return serveur;
	}
	
	public void setServeur(Serveur p_serveur)
	{
		serveur = p_serveur;
	}
	
	public double getHeureDebut()
	{
		return heureDebut;
	}
	
	public void setHeureDebut(double p_heureDebut)
	{
		heureDebut = p_heureDebut;
	}
	
	public double getHeureFin()
	{
		return heureFin;
	}
	
	public void setHeureFin(double p_heureFin)
	{
		heureFin = p_heureFin;
	}
	
	@Override
	public String toString() {
		
		return "Consultation:\t Patient: "
				+this.getPatient().getNumero() + "\tServeur" 
				+ this.getServeur().getNumero()+ "\tDebut: " 
				+ this.getHeureDebut() + "\tFin: " + this.getHeureFin()+"\n";
	}
}
