
public class Noeud 
{
	private Patient patient;
	private Noeud suivant;
	
	public Noeud(Patient p_patient, Noeud p_suivant)
	{
		patient = p_patient;
		suivant = p_suivant;
	}
	
	public Patient getPatient()
	{
		return patient;
	}
	
	public void setPatient(Patient p)
	{
		patient = p;
	}
	
	public Noeud getSuivant()
	{
		return suivant;
	}
	
	public void setSuivant(Noeud n)
	{
		suivant = n;
	}
}
