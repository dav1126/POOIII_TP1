import javax.swing.JOptionPane;


public class Confirmation 
{
	public void afficherDialogueMessage() {
		JOptionPane.showMessageDialog(null,
				"Le fichier Simulation.xls a été généré", "Boîte de Message",
				JOptionPane.INFORMATION_MESSAGE);
	}
}
