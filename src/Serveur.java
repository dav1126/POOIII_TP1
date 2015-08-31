
public class Serveur 
{
	private int numero;
	
	public Serveur(int p_numero)
	{
		setNumero(p_numero);
	}
	
	public int getNumero()
	{
		return numero;
	}
	
	public void setNumero(int p_numero)
	{
			numero = p_numero;
	}
	
	public boolean validerNumero(int p_disponible)
	{
		return true; 
	}
}
