package tp1;

/**
 * Tri d'un tableau d'entiers multi-thread.
 * Utilisation de wait() et notifyAll() au lieu de join()
 */
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import javax.print.DocFlavor.STRING;

public class Trieur extends Thread {

  private int[] t; // tableau à trier
  private int debut, fin; // tranche de ce tableau qu'il faut trier
  private Trieur parent;  // tread Trieur qui a lancé ce (this) Trieur
  private int nbNotify = 0; // Nombre de notifys envoyés à ce (this) Trieur
  private static int compt = 0;

  public Trieur(int[] t) {
    this(null, t, 0, t.length - 1);
  }
  
  private Trieur(Trieur parent, int[] t, int debut, int fin) {
    this.parent = parent;
    this.t = t;
    this.debut = debut;
    this.fin = fin;
   
    start();
   
   
   
  }

  public synchronized void notifier() {
    // Notifie tous les thread en attente sur "ce" (this) moniteur
    // Attention, quand le message sera envoyé au parent (dans run()),
    // on incrémentera la variable nbNotify du parent (qui sera le this
    // implicite) et on notifiera le parent.
    // On peut aussi ne pas utiliser de méthode mais dans ce cas, il faut
    // écrire parent.nbNotify++; parent.notifyAll(); dans un bloc 
    // synchronisé sur parent placé dans la méthode run (à la place de
    // "parent.notifier()").
    this.nbNotify++;
    this.notifyAll();
  }

  public void run() {
    if (fin - debut < 2) {
      if (t[debut] > t[fin]) {
        echanger(debut, fin);
      }
    }
    else {
      int milieu = debut + (fin - debut) / 2;
      Trieur trieur1 = new Trieur(this, t, debut, milieu);
      Trieur trieur2 = new Trieur(this, t, milieu + 1, fin);
      System.out.print("compt :"+compt+" , ");
      compt=compt+1;
      // attend les 2 threads
      synchronized(this) {
        try {
	  // Tant que 2 notifications n'ont pas été reçues (1 par
	  // trieur "fils"), on attend.
          while (nbNotify < 2) {
            wait();
          }
        }
        catch(InterruptedException e) {}
      }
      triFusion(debut, fin);
    }
    if (parent != null) {
      parent.notifier(); // indique qu'il a fini au parent qui l'attend
    }
  }

  /**
   * Echanger t[i] et t[j]
   */
  private void echanger(int i, int j) {
    int valeur = t[i];
    t[i] = t[j];
    t[j] = valeur;
  }

  /**
   * Fusionne 2 tranches déjà triées du tableau t.
   *   - 1ère tranche : de debut à milieu = (debut + fin) / 2
   *   - 2ème tranche : de milieu + 1 à fin
   * @param debut premier indice de la 1ère tranche
   * @param fin dernier indice de la 2ème tranche
   */
  private void triFusion(int debut, int fin) {
    // tableau où va aller la fusion
    int[] tFusion = new int[fin - debut + 1];
    int milieu = (debut + fin) / 2;
    // Indices des éléments à comparer
    int i1 = debut, 
        i2 = milieu + 1;
    // indice de la prochaine case du tableau tFusion à remplir
    int iFusion = 0;
    while (i1 <= milieu && i2 <= fin) {
      if (t[i1] < t[i2]) {
        tFusion[iFusion++] = t[i1++];
      }
      else {
        tFusion[iFusion++] = t[i2++]; 
      }
    }
    if (i1 > milieu) {
      // la 1ère tranche est épuisée
      for (int i = i2; i <= fin; ) {
        tFusion[iFusion++] = t[i++];
      }
    }
    else {
      // la 2ème tranche est épuisée
      for (int i = i1; i <= milieu; ) {
        tFusion[iFusion++] = t[i++];
      }
    }
    // Copie tFusion dans t
    for (int i = 0, j = debut; i <= fin - debut; ) {
      t[j++] = tFusion[i++];
    }
  }
  public static void main(String[] args) {
    String input = null ;
    System.out.print("input size array : ");
    try{
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    input = br.readLine();
 
    while(input.length()<=0){
      System.out.print("input size array : ");
       br = new BufferedReader(new InputStreamReader(System.in));
       input = br.readLine();
    }
  }catch(IOException io){
    io.printStackTrace();
  }
    int size = 0;
    try {
		size = Integer.parseInt(input);
	} catch (Exception e) {
		// TODO: handle exception
		System.out.println("type of your input value is not integer ;;; "+ e);
	}
     
    Random r = new Random();
    int []  t = new int[size];
    for(int i=0;i<size;i++){
    	t[i] = r.nextInt(10);
    }
   // int[] t = {5, 8, 3, 2, 7, 10, 1};
    Trieur trieur = new Trieur(t);
    try {
      trieur.join();
    }
    catch(InterruptedException e) {}
      for (int i = 0; i <t.length; i++) {
      System.out.print(t[i] + " ; ");
     }

    System.out.println("Done");
  }

}

