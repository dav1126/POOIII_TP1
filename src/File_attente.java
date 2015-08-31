
public class File_attente 
{
	Noeud debut;
	Noeud fin;
	
	public File_attente()
	{
		debut = null;
		fin = null;
	}
	
	public void ajouterPatient(Patient p)
	{
		Noeud n = new Noeud(p, null);
		
		if(fin != null)
			fin.setSuivant(n);
		
		if (debut == null)
		{
			debut = n;
		}
		
		fin = n;
	}
	
	public Noeud enleverPatient()
	{
		Noeud retour = null;
		
		if(debut != null)
		{
			retour = debut;
			debut = debut.getSuivant();
			if(debut == null)
			{
				fin = null;
			}
		}
		
		return retour;
	}
	
	public Noeud getDebut()
	{
		return debut;
	}
	
	public void setDebut(Noeud n)
	{
		debut = n;
	}
	
	public Noeud getFin()
	{
		return fin;
	}
	
	public void setFin(Noeud n)
	{
		fin = n;
	}
	
	public boolean estVide()
	{
		return (debut == null);
	}	
}
