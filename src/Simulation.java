import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import jxl.*;
import jxl.write.*;
import jxl.write.Number;


public class Simulation 
{	
	//Heure d'admission max des patients fix�e � 18h30 (570min)
	static final double HEURE_MAX_ADMISSION = 570; 
	
	//Param�tres lambda
	static double lambda1;
	static double lambda2;
	
	//Valeur max et min de lambda
	static final double LAMBDAMAX = 0.1;
	static final double LAMBDAMIN = 0.01;
	
	//Statistiques
	static double tempsAttenteMoy;//Calcul du temps d'attente moyen
	static double tempsAttenteMax = 0.0;//Le temps maximum attendu par un patient 
	static int noPatientMaxAttente;//le num�ro du patient ayant attendu le plus longtemps
	static int nbrePatientEnFile = 0;//tient le compte du nombre de patient en file d'attente
	static int nbrePatientEnConsult = 0;//Nombre de patient en consultation(min 0, max 2)
	static int nbreMaxPatientSysteme = 0;//Nombre max de patient dans le syst�me(incluant en consultation)
	static double tempsMaxPatient;//Temps auquel on atteint le plus de patients dans le syst�me
	static double tempsFinDernierPatient = 0;
	
	//Listes servant � conserver les consultations et les patients trait�s en m�moire
	static ArrayList<Consultation> consultList = new ArrayList<Consultation>();
	static ArrayList<Patient> patientList = new ArrayList<Patient>();
	
	//Serveurs, au nombre de 2
	static Serveur serveur1 = new Serveur(1);
	static Serveur serveur2 = new Serveur(2);
	
	//Consultations temporaires, maximum 2 (car 2 serveurs)
	static Consultation consultationEnCours1 = new Consultation();//Constructeur vide permet d'initialiser les heures de d�but et de fin � -1 (n�cessaire pour effectuer des tests bool�ens)
	static Consultation consultationEnCours2 = new Consultation();//Constructeur vide permet d'initialiser les heures de d�but et de fin � -1 (n�cessaire pour effectuer des tests bool�ens)
	
	//File d'attente logique dans laquelle les patients sont plac�s temporairement lorsque les serveurs sont occup�s.
	static File_attente file = null;
	
	
	public static void main(String[] args) 
	{
		Object[] options = {"D�buter une simulation", "Quitter"};
		int reponse = JOptionPane.showOptionDialog(null, "Bienvenue!", "Simulation de file d'attente", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
		
		if (reponse == 0)
		{
			demanderLambdas();
			simulerConsultations();
			genererStatistiques();
			
			JOptionPane.showMessageDialog(null, "Simulation termin�e!");
		
		}
		
		//Test d'affichage
		/*for (int i=0; i<patientList.size(); i++)
		{
			System.out.println(patientList.get(i));
		}
		
		for (int i=0; i<consultList.size(); i++)
		{
			System.out.println(consultList.get(i));
		}*/
		
		if (lambda1 != 0.0 && lambda2 != 0.0)
		{	
			try
			{
			genererFichierExcel();
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
		
		System.exit(0);
	}
	
	//Demande les 2 lambdas � l'utilisateur
	public static void demanderLambdas()
	{
		lambda1 = Double.parseDouble(JOptionPane.showInputDialog(null, "Entrez un lambda 1 (intervalles d'arriv�es) entre " + LAMBDAMIN + " et " + LAMBDAMAX + ".", "Simulation de file d'attente", JOptionPane.QUESTION_MESSAGE));	
		while (!validerLambda(lambda1))
		{
			lambda1 = Double.parseDouble(JOptionPane.showInputDialog(null, "Le lambda doit �tre entre " + LAMBDAMIN + " et " + LAMBDAMAX + ".\n Entrez un nouveau lamda 1 (intervalles d'arriv�es)", "Simulation de file d'attente", JOptionPane.WARNING_MESSAGE));
		}
		
		lambda2 = Double.parseDouble(JOptionPane.showInputDialog(null, "Entrez un lambda 2 (temps de service) entre " + LAMBDAMIN + " et " + LAMBDAMAX + ".", "Simulation de file d'attente", JOptionPane.QUESTION_MESSAGE));
		while (!validerLambda(lambda2))
		{
			lambda2 = Double.parseDouble(JOptionPane.showInputDialog(null, "Le lambda doit �tre entre " + LAMBDAMIN + " et " + LAMBDAMAX + ".\n Entrez un nouveau lamda 2 (temps de service)", "Simulation de file d'attente", JOptionPane.WARNING_MESSAGE));
		}	
	}
	
	//Valide les lambdas entr�s par l'utilisateur
	public static boolean validerLambda(double lambda)
	{
		return (lambda >= LAMBDAMIN && lambda <= LAMBDAMAX);
	}
	
	//Place la patient directement en consultation ou dans la file d'attente, d�pendamment de si la file d'attente est vide
	//et de si les consultations sont termin�es lors de son arriv�e.	
	public static void simulerConsultations()
	{
		boolean heureArriveeMaxDepasse = false; //L'heure d'arriv�e max est de 18h30. Par la suite, les nouveaux patients ne sont pas admis.
		int noPatient = 0; //Le num�ro du patient, selon l'ordre d'arriv�e
		double heureArrivee = 0; //L'heure � laquelle le patient arrive � l'h�pital
		
		Patient patientEntrant = null;
		file = new File_attente();
				
		//Tant que l'heure d'arriv�e du patientEntrant ne d�passe par l'heure d'admission max.
		while (!heureArriveeMaxDepasse)
		{
			if (patientEntrant != null)
			{	
				//Calculer un jeure d'arriv�e pour le patientEntrant
				heureArrivee += calculerIntervalleArrivee();
			}
			noPatient++;
			
			//Initialiser le patientEntrant avec seulement 2 param�tres (les autres param�tres sont calcul�s et ajout�s plus tard)
			patientEntrant = new Patient(noPatient, heureArrivee);
			
			//Si l'arriv�e du nouveau patient d�passe l'heure maximale d'admission (18h30),
			//le nouveau patient n'est pas trait�.
			if (heureArrivee > HEURE_MAX_ADMISSION)
			{
				heureArriveeMaxDepasse = true;
				
				//Envoyer les patients de la file d'attente en consultation, jusqu'� ce qu'elle soit vide
				while(!(file.estVide()))
				{
					demarrerConsult(file.debut);
				}
			}
			
			//Sinon, si la file d'attente est vide...
			else if(file.estVide())
			{
				/*Si le patientEntrant arrive alors qu'au moins une des consultations est termin�e,
				 *envoyer le patient directement en consultation.
				 */
				if(heureArrivee >= consultationEnCours1.getHeureFin() || heureArrivee >= consultationEnCours2.getHeureFin()) 
				{
					demarrerConsult(new Noeud(patientEntrant, null));
				}
				
				//Sinon, envoyer le patient dans la file d'attente
				else
				{
					nbrePatientEnFile++;
					patientEntrant.setNbrePatientFile(nbrePatientEnFile);
					file.ajouterPatient(patientEntrant);	
				}
			}
			
			//Sinon (si l'heure d'admission n'est pas d�pass�e et que la file d'attente n'est pas vide)
			else
			{
				/* Si l'heure d'arriv�e du patientEntrant est inf�rieure � la fin des deux consultations,
				 * on envoie le patient directement au bout de la file.
				 */
				if(heureArrivee < consultationEnCours1.getHeureFin() && heureArrivee < consultationEnCours2.getHeureFin())
				{
					nbrePatientEnFile++;
					patientEntrant.setNbrePatientFile(nbrePatientEnFile);
					file.ajouterPatient(patientEntrant);
				}
				
				//Sinon
				else
				{
					/*Tant que l'heure d'arriv�e du patientEntrant est sup�rieure ou �gale � l'heure de fin de l'une ou 
					 * l'autre des consultations ET tant que la file d'attente n'est pas vide, 
					 * envoyer le patient du d�but de la file d'attente en consultation.
					 */
					while( (heureArrivee >= consultationEnCours1.getHeureFin() || heureArrivee >= consultationEnCours2.getHeureFin()) 
							&& !(file.estVide()) )
					{
						demarrerConsult(file.getDebut());
					}
					
					/*Suite au while pr�c�dent (qui aura chang� l'heure de fin des consultationsEnCours), si l'heure d'arriv�e 
					 * du patientEntrant est inf�rieure � la fin des deux consultations, on l'envoie dans la file
					 */
					if(heureArrivee < consultationEnCours1.getHeureFin() && heureArrivee < consultationEnCours2.getHeureFin())
					{
						nbrePatientEnFile++;
						patientEntrant.setNbrePatientFile(nbrePatientEnFile);
						file.ajouterPatient(patientEntrant);
					}
					
					//Sinon, le patient entre directement en consultation
					else
					{
						demarrerConsult(new Noeud(patientEntrant, null));
					}	
				}
			}
			
			//Calcul du nbreMaxPatientSysteme et du tempsMaxPatient
				//Premi�rement, on d�termine le nombre de patient en consultation.
					//Si la file est vide, on v�rifie si des consultations sont en cours
					if(file.estVide())
					{
						nbrePatientEnConsult = 0;
						
						if(consultationEnCours1.getHeureFin() >= heureArrivee)
						nbrePatientEnConsult++;
						
						if(consultationEnCours2.getHeureFin() >= heureArrivee)
						nbrePatientEnConsult++;
					}
					
					//Si la file d'attente n'est pas vide, le nombre de patient en consultation est n�cessairement de 2
					else
					{	
						nbrePatientEnConsult = 2;	
					}
				
				/*Ensuite, avec les variables nbrePatienEnFile et nbrePatientEnConsult, 
				 *on met � jour les variables nbreMaxPatientSysteme et tempsMaxPatient, au besoin.
				 */
				if ((nbrePatientEnFile + nbrePatientEnConsult) > nbreMaxPatientSysteme)
				{
					nbreMaxPatientSysteme = nbrePatientEnFile + nbrePatientEnConsult;
					
					tempsMaxPatient = heureArrivee;//l'heure auquel il y a le plus de patient dans le syst�me = heure d'arriv�e du patientEntrant
				}
			
			//Test d'affichage
			/*System.out.println(nbrePatientEnFile);
			System.out.println(nbrePatientEnConsult);
			System.out.println(nbreMaxPatientSysteme);
			
			System.out.println(tempsMaxPatient+"\n");*/
		}
	}
	
	/** Calcule des temps de service al�atoire selon une loi exponentielle utilisant le param�tre
	 * 	lambda2 demand� � l'utilisateur.
	 * @return	temps de service al�atoire
	 */
	public static double calculerTempsDeService()
	{
		return (-(Math.log(1-Math.random()))*(1/lambda2));	
	}
	
	/** Calcule des intervalle de temps entre deux arriv�es selon une loi exponentielle utilisant le param�tre
	 *	lambda1 demand� � l'utilisateur.
	 * @return	intervalle de temps al�atoire
	 */	
	public static double calculerIntervalleArrivee()
	{
		return (-(Math.log(1-Math.random()))*(1/lambda2));
	}
	
	//Place le patient d'un noued pass� en param�tre en consultation
	//Cr�� aussi les patients et consultations permanents qui sont enregistr�s dans les listes
	public static void demarrerConsult(Noeud noeud)
	{
		Patient patient = noeud.getPatient();
		
		//Si la consultationEnCours 1 se termine avant ou en m�me temps que l'autre, on place le patient dans la consultationEnCours1
		if (consultationEnCours1.getHeureFin() <= consultationEnCours2.getHeureFin())
		{
			//On set l'heure de d�but de la consultation
				//Si l'arriv�e du patient est inf�rieure � la fin de la consultation, il est pris en charge � la fin de la consultation
				if(patient.getHeureArrivee() <= consultationEnCours1.getHeureFin())
				{
					consultationEnCours1.setHeureDebut(consultationEnCours1.getHeureFin());
				}
				
				//Si l'arriv�e du patient est sup�rieure � la fin de la consultation, il est pris en charge � son heure d'arriv�e
				else
				{
					consultationEnCours1.setHeureDebut(patient.getHeureArrivee());
				}
			
			//On set l'heure de fin de la consultation
			double tempsDeService = calculerTempsDeService();
			consultationEnCours1.setHeureFin(consultationEnCours1.getHeureDebut() + tempsDeService);
			
			//On set le patient et le serveur1 � la consultation
			consultationEnCours1.setPatient(patient);
			consultationEnCours1.setServeur(serveur1);
			
			//Faire une sauvegarde permanente de la consultation, entrepos�e dans la liste des consultations
			Consultation consultPerm = new Consultation(consultationEnCours1.getPatient(), consultationEnCours1.getServeur(), consultationEnCours1.getHeureDebut(), consultationEnCours1.getHeureFin());
			consultList.add(consultPerm);
			
			//Attribuer les param�tres manquants du patient
			patient.setHeureSortie(consultationEnCours1.getHeureFin());
			patient.setTempsAttente(consultationEnCours1.getHeureDebut() - patient.getHeureArrivee());
				
			//Faire une sauvegarde permanente du patient, entrepos�e dans la liste des patients
			Patient patientPerm = new Patient(patient.getNumero(), patient.getHeureArrivee(), patient.getTempsAttente(), patient.getHeureSortie(), patient.getNbrePatientFile());
			patientList.add(patientPerm);
		}
				
		//Sinon, on place le patient dans la consultationEnCours2
		else
		{
				//On set l'heure de d�but de la consultation
				//Si l'arriv�e du patient est inf�rieure � la fin de la consultation, il est pris en charge � la fin de la consultation
				if(patient.getHeureArrivee() <= consultationEnCours2.getHeureFin())
				{
					consultationEnCours2.setHeureDebut(consultationEnCours2.getHeureFin());
				}
				
				//Si l'arriv�e du patient est sup�rieure � la fin de la consultation, il est pris en charge � son heure d'arriv�e
				else
				{
					consultationEnCours2.setHeureDebut(patient.getHeureArrivee());
				}
			
			//On set l'heure de fin de la consultation
			double tempsDeService = calculerTempsDeService();
			consultationEnCours2.setHeureFin(consultationEnCours2.getHeureDebut() + tempsDeService);
			
			//On set le patient et le serveur2 � la consultation
			consultationEnCours2.setPatient(patient);
			consultationEnCours2.setServeur(serveur2);
		
			//Faire une sauvegarde permanente de la consultation, entrepos�e dans la liste des consultations
			Consultation consultPerm = new Consultation(consultationEnCours2.getPatient(), consultationEnCours2.getServeur(), consultationEnCours2.getHeureDebut(), consultationEnCours2.getHeureFin());
			consultList.add(consultPerm);
			
			//Attribuer les param�tres manquants du patient
			patient.setHeureSortie(consultationEnCours2.getHeureFin());
			patient.setTempsAttente(consultationEnCours2.getHeureDebut() - patient.getHeureArrivee());
			
			//Faire une sauvegarde permanente du patient, entrepos�e dans la liste des patients
			Patient patientPerm = new Patient(patient.getNumero(), patient.getHeureArrivee(), patient.getTempsAttente(), patient.getHeureSortie(), patient.getNbrePatientFile());
			patientList.add(patientPerm);
		}
		
		/*Si le patient qui vient d'�tre ajout�e � une consultation �tait le d�but de la file,
		 * on d�place le d�but de la file au patient suivant. 
		 */
		if (noeud == file.getDebut())
		{
			file.enleverPatient();
			nbrePatientEnFile--;	
		}
	}
	
	/**
	 * � la fin de la simulation, g�n�re les statistiques relatives au temps d'attente
	 */
	public static void genererStatistiques()
	{
				
		double tempsAttenteSomme = 0;
		int compteurPatient = 0;
		
		/*Parcourir les patients enregistr�s dans la liste patientList et sortir la moyenne des temps d'attente 
		 * et le temps d'attente max avec le numero de patient correspondant.
		 */		
		for (Patient p : patientList)
		{
			compteurPatient++;
			tempsAttenteSomme += p.getTempsAttente();
			
			if(p.getTempsAttente() > tempsAttenteMax)
			{
				tempsAttenteMax = p.getTempsAttente();
				noPatientMaxAttente = p.getNumero();
			}
		}
		System.out.println("NOATTENTEMAX:" +noPatientMaxAttente + " "+tempsAttenteMax);
		tempsAttenteMoy = tempsAttenteSomme/compteurPatient;
		
		//Parmis les consultations enregistr�es danns la liste consultList, v�rifier celle qui se termine le plus tard
		for (Consultation c : consultList)
		{
			if (c.getHeureFin() > tempsFinDernierPatient)
			{
				tempsFinDernierPatient = c.getHeureFin();
			}
		}
		System.out.println("FINDERNIER"+tempsFinDernierPatient);
	}
	
	public static void genererFichierExcel() throws WriteException,  IOException
	{
		try {
			// cr�ation d'un fichier Excel
			WritableWorkbook workbook = Workbook.createWorkbook(new File("Simulation.xls"));
			
			// cr�ation d'une feuille de calcul
			WritableSheet sheet = workbook.createSheet("First Sheet", 0);
			
			// cr�ation du format des ent�tes de colonnes
			WritableCellFormat wrappedText = new WritableCellFormat();
			wrappedText.setWrap(true);
			wrappedText.setAlignment(jxl.format.Alignment.CENTRE);
			wrappedText.setBackground(jxl.format.Colour.GREEN,
					jxl.format.Pattern.SOLID);
			wrappedText.setBorder(jxl.format.Border.ALL,
					jxl.format.BorderLineStyle.MEDIUM,
					jxl.format.Colour.AUTOMATIC);
			
			// cr�ation du format pour heure de sortie en heure
			WritableCellFormat centerText = new WritableCellFormat();
			centerText.setAlignment(jxl.format.Alignment.CENTRE);
			centerText.setBorder(jxl.format.Border.ALL,
					jxl.format.BorderLineStyle.MEDIUM,
					jxl.format.Colour.AUTOMATIC);
			
			// cr�ation du format pour nombre entier
			WritableCellFormat integerFormat = new WritableCellFormat(
					NumberFormats.INTEGER);
			integerFormat.setAlignment(jxl.format.Alignment.CENTRE);
			integerFormat.setBorder(jxl.format.Border.ALL,
					jxl.format.BorderLineStyle.MEDIUM,
					jxl.format.Colour.AUTOMATIC);
			
			// cr�ation du format pour nombre en r�el double
			WritableCellFormat fourdps = new WritableCellFormat(
					new NumberFormat("0.0000"));
			fourdps.setAlignment(jxl.format.Alignment.CENTRE);
			fourdps.setBorder(jxl.format.Border.ALL,
					jxl.format.BorderLineStyle.MEDIUM,
					jxl.format.Colour.AUTOMATIC);
			
			// fixe la largeur des colonnes
			for (int i = 0; i < 9; i++) {
				sheet.setColumnView(i, 12);
			}
			// si des patients sont venues
			if (patientList.size() > 0) 
			{
				
				// afficher les stats globales
				System.out.println(tempsFinDernierPatient);
				sheet.addCell(new Label(
						0,
						0,
						"A quel temps le dernier patient quitte le bloc de r�alisation de la consultation du patient avant l�intervention?"));
				sheet.addCell(new Number(8, 0, tempsFinDernierPatient, fourdps));
				sheet.addCell(new Label(0, 2,
						"Quel est le temps moyen d�attente dans la file pour les M patients ?"));
				sheet.addCell(new Number(8, 2, tempsAttenteMoy, fourdps));
				sheet.addCell(new Label(0, 4,
						"Quel est le patient qui va attendre le maximum dans la file par rapport aux autres patients ?"));
				sheet.addCell(new Number(8, 4, noPatientMaxAttente, integerFormat));
				sheet.addCell(new Label(0, 6,
						"Quel est ce temps d'attente ?"));
				sheet.addCell(new Number(8, 6, tempsAttenteMax, fourdps));
				sheet.addCell(new Label(0, 8,
						"Quel est le nombre maximum de patients dans le syst�me ?"));
				sheet.addCell(new Number(8, 8, nbreMaxPatientSysteme, integerFormat));
				sheet.addCell(new Label(0, 10,
						"� quel temps atteint-on ce nombre ?"));
				sheet.addCell(new Number(8, 10, tempsMaxPatient, fourdps));
				
				//afficher la colonne "Ordre des patients"			
				sheet.addCell(new Label(0, 12, "Ordre des patients", wrappedText));
				for (int i = 0; i < patientList.size(); i++)
				{
					sheet.addCell(new Number(0, 13+i, patientList.get(i).getNumero(), integerFormat));
				}
				
				//afficher la colonne "Heure d'arriv�e en min"
				sheet.addCell(new Label(1, 12, "Heure d'arriv�e en min", wrappedText));
				for (int i = 0; i < patientList.size(); i++)
				{
					sheet.addCell(new Number(1, 13+i, patientList.get(i).getHeureArrivee(), fourdps));
				}
				
				//afficher la colonne "Temps moyen de service"
				sheet.addCell(new Label(2, 12, "Temps moyen de service", wrappedText));
				for (int i = 0; i < consultList.size(); i++)
				{
					double tempsDeService = consultList.get(i).getHeureFin() - consultList.get(i).getHeureDebut();
					sheet.addCell(new Number(2, 13+i, tempsDeService, fourdps));
				}
				
				//afficher la colonne "Heure de prise en charge"
				sheet.addCell(new Label(3, 12, "Heure de prise en charge en min", wrappedText));
				for (int i = 0; i < consultList.size(); i++)
				{
					sheet.addCell(new Number(3, 13+i, consultList.get(i).getHeureDebut(), fourdps));
				}
				
				//afficher la colonne "Heure de sortie en min"
				sheet.addCell(new Label(4, 12, "Heure de sortie en min", wrappedText));
				for (int i = 0; i < consultList.size(); i++)
				{
					sheet.addCell(new Number(4, 13+i, consultList.get(i).getHeureFin(), fourdps));
				}
				
				//afficher la colonne "Heure de sortie en heure"
				sheet.addCell(new Label(5, 12, "Heure de sortie en heure", wrappedText));
				for (int i = 0; i < consultList.size(); i++)
				{
					int nbreHeure = (int)consultList.get(i).getHeureFin() / 60;
					int nbreMin = (int)consultList.get(i).getHeureFin() % 60;
					String tempsEnHeure = "";
					
					if (nbreMin < 10)
					tempsEnHeure = nbreHeure + "h0" +nbreMin;
					
					else
					tempsEnHeure = nbreHeure + "h" +nbreMin;
							
					sheet.addCell(new Label(5, 13+i, tempsEnHeure, centerText));
				}
				
				//afficher la colonne Serveurs (1 ou 2)
				sheet.addCell(new Label(6, 12, "Serveur (1 ou 2)", wrappedText));
				for (int i = 0; i < consultList.size(); i++)
				{
					sheet.addCell(new Number(6, 13+i, consultList.get(i).getServeur().getNumero(), integerFormat));
				}
				
				//afficher la colonne "Temps d'attente"
				sheet.addCell(new Label(7, 12, "Temps d'attente", wrappedText));
				for (int i = 0; i < patientList.size(); i++)
				{
					sheet.addCell(new Number(7, 13+i, patientList.get(i).getTempsAttente(), fourdps));
				}
				
				//afficher la colonne "Nombre de patients en attente ds la file"
				sheet.addCell(new Label(8, 12, "Nombre de patients en attente ds la file", wrappedText));
				for (int i = 0; i < patientList.size(); i++)
				{
					sheet.addCell(new Number(8, 13+i, patientList.get(i).getNbrePatientFile(), integerFormat));
				}
			} 
			
			//si personne n'est venue
			else 
			{
				sheet.addCell(new Label(0, 0, "Personne n'est venu"));
			}
			
			// �crire les r�sultats
			workbook.write();
			workbook.close();
			// confirmer l'�criture des r�sultats
			Confirmation dialogue = new Confirmation();
			dialogue.afficherDialogueMessage();

		} catch ( IOException | WriteException  ie) {
			// avertissement si le fichier n'a pas pu �tre cr�e
			JOptionPane.showMessageDialog(null,
					"Le fichier n'a pas pu �tre cr�e", "Bo�te Message",
					JOptionPane.WARNING_MESSAGE);

		}
	}
}
