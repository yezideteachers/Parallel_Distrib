/**
 * Tri d'un tableau d'entiers multi-thread.
 * Utilisation de wait() et notifyAll() au lieu de join()
 */
public class Trieur extends Thread {

  private int[] t; // tableau � trier
  private int debut, fin; // tranche de ce tableau qu'il faut trier
  private Trieur parent;  // tread Trieur qui a lanc� ce (this) Trieur
  private int nbNotify = 0; // Nombre de notifys envoy�s � ce (this) Trieur

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
    // Attention, quand le message sera envoy� au parent (dans run()),
    // on incr�mentera la variable nbNotify du parent (qui sera le this
    // implicite) et on notifiera le parent.
    // On peut aussi ne pas utiliser de m�thode mais dans ce cas, il faut
    // �crire parent.nbNotify++; parent.notifyAll(); dans un bloc 
    // synchronis� sur parent plac� dans la m�thode run (� la place de
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
      // attend les 2 threads
      synchronized(this) {
        try {
	  // Tant que 2 notifications n'ont pas �t� re�ues (1 par
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
   * Fusionne 2 tranches d�j� tri�es du tableau t.
   *   - 1�re tranche : de debut � milieu = (debut + fin) / 2
   *   - 2�me tranche : de milieu + 1 � fin
   * @param debut premier indice de la 1�re tranche
   * @param fin dernier indice de la 2�me tranche
   */
  private void triFusion(int debut, int fin) {
    // tableau o� va aller la fusion
    int[] tFusion = new int[fin - debut + 1];
    int milieu = (debut + fin) / 2;
    // Indices des �l�ments � comparer
    int i1 = debut, 
        i2 = milieu + 1;
    // indice de la prochaine case du tableau tFusion � remplir
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
      // la 1�re tranche est �puis�e
      for (int i = i2; i <= fin; ) {
        tFusion[iFusion++] = t[i++];
      }
    }
    else {
      // la 2�me tranche est �puis�e
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
    int[] t = {5, 8, 3, 2, 7, 10, 1};
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

