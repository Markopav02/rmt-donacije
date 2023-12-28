package Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class Server {
    private static final int PORT = 8080;
    private static final List<ClientHandler> klijenti = new ArrayList<>();
    private static int redniBroj;
    static boolean registrovan=false;
   
   
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server pokrenut.Cekaju se klijenti...");
        	
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Klijent povezan: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                klijenti.add(clientHandler);
                
                
                new Thread(clientHandler).start();
            }
        }
        catch (BindException e){
			System.err.println("Server je vec pokrenut na ovom portu.");

		}catch (IOException e) {
            e.printStackTrace();
        }
       
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
         ObjectOutputStream outputStream;
         ObjectInputStream inputStream;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                inputStream = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
            
            	boolean izvrsavanje=true;
                slanjePoruke(outputStream, "Uspesna konekcija!");
                while (izvrsavanje==true) {
                	if(registrovan) {
                		slanjePoruke(outputStream, "Dobrodosli "+"\nIzaberite opciju:\n1. Napravite donaciju\n2. Pregled svih sredstava\n3. Pregled poslednjih 10 transakcija\n4. Prijavite se na postojeci nalog\n5. Registracija\n6. Odjavite se sa naloga\n7. Kraj");
                	}
                	else
                	slanjePoruke(outputStream, "Izaberite opciju:\n1. Napravite donaciju\n2. Pregled svih sredstava\n3. Pregled poslednjih 10 transakcija\n4. Prijavite se na postojeci nalog\n5. Registracija\n6. Odjavite se sa naloga\n7. Kraj");
                   int option=(int) inputStream.readObject();

                    switch (option) {
                        case 1:
                            doniraj();
                            break;
                        case 2:
                            vidiStanje();
                            break;
                        case 3:
                        	registrovan=(boolean) inputStream.readObject();
                        	if(registrovan)
                            vidiTransakcije();
                        	else slanjePoruke(outputStream, "Morate biti registrovani za ovu funkcionalnost");
                            break;
                        case 4:
                        	login();
                        	break;
                        case 5:
                        	registracija();
                        	break;
                        case 6:
                        	registrovan=(boolean) inputStream.readObject();
                        	if(registrovan) {
                        	slanjePoruke(outputStream, "Uspesno ste se izlogovali sa vaseg naloga "+"\n");
                        	
                        	registrovan=false;
                        	}
                        	else slanjePoruke(outputStream, "Morate se prvo prijaviti na nalog da biste se odjavili!");
                        	
                        	break;
                        case 7:
                        	System.out.println("Klijent "+clientSocket.getInetAddress()+" je prekinuo konekciju.");
                            exit();
                            izvrsavanje=false;
                            break;
                        default:
                            slanjePoruke(outputStream, "Nepostojeca opcija.Pokusajte opet");
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

       
		private void doniraj() throws IOException, ClassNotFoundException {
			redniBroj=brojRedovaUFajlu("src/bazaTransakcija.txt")+1;
			posaljiFajl(outputStream, "src/bazaKartica.txt");
            // Korisniku se omogucava da izvrsi donaciju uz prvobitni unos svojih podataka
        	Transakcija t=new Transakcija(null, null, null, null, null, null, null, 0);
        	//Citanje unosa imena korisnika
        	
                t.ime = (String) inputStream.readObject();
                t.ime.trim();
        	
         //Citanje unosa prezimena i kreiranje korisnickog imena u datom formatu
                
        		t.prezime = (String) inputStream.readObject();
                t.prezime.trim();
                if(!registrovan) {
                t.korisnickoIme = t.ime.trim() + t.prezime.trim()+"_guest";
                }else {
                	t.korisnickoIme=(String) inputStream.readObject();
                	t.korisnickoIme.trim();
                }
         //Citanje adrese
                t.adresa=(String) inputStream.readObject();
                t.adresa.trim();
         //Citanje broja kartice	
            
        	String brojKartice=(String) inputStream.readObject();
        	String cvv=(String) inputStream.readObject();
         //Provera da li unete vrednosti postoje u bazi kartica se desava u klijentu    	
        	t.brojKartice=brojKartice;
        	t.cvv=cvv;
                
        	t.iznos=(double) inputStream.readObject();
        	
        	
    
        	GregorianCalendar datum=new GregorianCalendar();
        	int dan=datum.get(GregorianCalendar.DAY_OF_MONTH);
        	int mesec=datum.get(GregorianCalendar.MONTH)+1;
        	int godina=datum.get(GregorianCalendar.YEAR);
        	int sati=datum.get(GregorianCalendar.HOUR_OF_DAY);
        	int minuti=datum.get(GregorianCalendar.MINUTE);
        	t.datumIVreme=dan+"."+mesec+'.'+godina+'.'+" u "+sati+':'+minuti+'h';
        	
        	sacuvajTransakciju(t);
        	slanjePoruke(outputStream, "\nSacuvana transakcija br."+redniBroj+" sa sledecim podacima:"+"\nIme: "+t.ime+"\nPrezime: "+t.prezime+"\nKorisnicko ime: "+t.korisnickoIme+"\nAdresa: "+t.adresa+"\nBroj kartice: "+t.brojKartice+"\nIznos: "+t.iznos+"\nVreme: "+t.datumIVreme+"\n");
        	
        	//pravljenje fiskalnog racuna
        	String fiskalni="================================================================\n\n"+"Donacija br."+redniBroj+"\nIme donora: "+t.ime+"\nPrezime donora: "+t.prezime+"\nAdresa: "+t.adresa+"\nBroj kartice: "+t.brojKartice+"\nIznos: "+t.iznos+" dinara\nVreme: "+t.datumIVreme+"\n\n"+"================================================================";
        try (FileWriter writer = new FileWriter(t.korisnickoIme+"_"+redniBroj)) {
			writer.write(fiskalni);
		}
        //slanje fiskalnog
        outputStream.writeObject(t.korisnickoIme+"_"+redniBroj);
        outputStream.flush();
		posaljiFajl(outputStream, t.korisnickoIme+"_"+redniBroj);
		redniBroj++;
		}

        private void vidiStanje() throws IOException {
           slanjePoruke(outputStream, "U ovom trenutku ukupno je sklupljeno: "+izracunajSredstva("src/bazaTransakcija.txt")+" dinara u nasoj humanitarnoj organizaciji");
        }

        private void vidiTransakcije() throws IOException {
        	 List<Transakcija> transakcije = new ArrayList<>();
        	 transakcije=ucitajTransakcijeIzFajla("src/bazaTransakcija.txt");
        	 String pregled="";
        	 int numerator=brojRedovaUFajlu("src/bazaTransakcija.txt")-1;
        	 if(numerator>=10) {
        		 for(int i=numerator;i>=numerator-10;i--) {
        			 pregled=pregled+transakcije.get(i).toString();
        		 }
        	 }else {
        		 for(int i=numerator;i>=0;i--) {
        			 pregled=pregled+transakcije.get(i).toString();
        		 }
        	 }
        	 if(numerator>=10)
        	 slanjePoruke(outputStream, "Poslednjih 10 transakcija izgledaju ovako:\n"+pregled);
        	 else slanjePoruke(outputStream, "Poslednjih "+numerator+"transakcija izgledaju ovako:"+pregled);
        }
        public static int brojRedovaUFajlu(String putanja) {
            int brojRedova = 0;

            try (BufferedReader reader = new BufferedReader(new FileReader(putanja))) {
                while (reader.readLine() != null) {
                    brojRedova++;
                }
            } catch (IOException e) {
                e.printStackTrace(); // Dodajte odgovarajuću obradu grešaka
            }

            return brojRedova;
        }
        private void registracija() throws ClassNotFoundException, IOException {
        	posaljiFajl(outputStream, "src/bazaKartica.txt");
        	posaljiFajl(outputStream, "src/bazaNaloga.txt");
        	Nalog n=new Nalog(null, null, null, null, null, null, null, null);
        	//Ucitavanje username-a
        	n.username=(String) inputStream.readObject();
        	n.username.trim();
        	//Ucitavanje imena
        	n.ime=(String) inputStream.readObject();
        	n.ime.trim();
        	//Ucitavanje prezimena
        	n.prezime=(String) inputStream.readObject();
        	n.prezime.trim();
        	//Ucitavanje jmbg-a
        	n.jmbg=(String) inputStream.readObject();
        	//Ucitavanje broja kartice
        	n.brojKartice=(String) inputStream.readObject();
        	//Ucitavanje cvv broja
        	n.cvv=(String) inputStream.readObject();
        	//Ucitavanje korisnickog emaila
        	n.email=(String) inputStream.readObject();
        	n.email.trim();
        	//Ucitavanje korisnicke sifre
        	n.password=(String) inputStream.readObject();
        	n.password.trim();
        	//cuvanje korisnika u bazu
        	sacuvajNalog(n);
        	//Slanje poruke o sacuvanom korisniku
        	slanjePoruke(outputStream, "Sacuvan novi korisnik sa sledecim podacima:\nIme i prezime: "
        			+n.ime+" "+n.prezime+
        			"\nUsername: "+n.username+
        			"\nJMBG: "+n.jmbg+
        			"\nBroj kartice: "+n.brojKartice+
        			"\nE-mail adresa: "+n.email+"\n");
        	
        }
        //Serverska strana postupka logina korisnika
        private void login() throws ClassNotFoundException, IOException {
        	posaljiFajl(outputStream, "src/bazaNaloga.txt");
        	
        	registrovan=(boolean) inputStream.readObject();       	
        	
        }
        
        private boolean isClosed = false;
        private void exit() throws IOException {
        	 if (isClosed==false) {
        	        slanjePoruke(outputStream, "Hvala Vam što ste koristili naš servis!\nNadamo se budućoj saradnji!");
        	        outputStream.close();
        	        inputStream.close();
        	        clientSocket.close();
        	        klijenti.remove(this);
        	        isClosed = true;
        	    }
        }

        private void slanjePoruke(ObjectOutputStream outputStream,String poruka) throws IOException {
           
				outputStream.writeObject(poruka);
				outputStream.flush();
			
    }

private static void sacuvajTransakciju(Transakcija t) {
    try (FileWriter writer = new FileWriter("src/bazaTransakcija.txt", true)) {
    	if (isFileEmpty("src/bazaTransakcija.txt")) {
            writer.write(t.korisnickoIme+';'+t.ime+';'+t.prezime+';'+t.adresa+';'+t.brojKartice+';'+t.cvv+';'+t.iznos+';'+t.datumIVreme);
        }else {
        	//Ako fajl nije prazan dodaje se novi red na pocetku
        writer.write("\n"+t.korisnickoIme+';'+t.ime+';'+t.prezime+';'+t.adresa+';'+t.brojKartice+';'+t.cvv+';'+t.iznos+';'+t.datumIVreme);
        }
        } catch (IOException e) {
        e.printStackTrace();  // Dodajte odgovarajuću obradu grešaka
    }
}

private static void sacuvajNalog(Nalog n) {
    try (FileWriter writer = new FileWriter("src/bazaNaloga.txt", true)) {
    	if (isFileEmpty("src/bazaNaloga.txt")) {
            writer.write(n.username+';'+n.ime+';'+n.prezime+';'+n.jmbg+';'+n.brojKartice+';'+n.cvv+';'+n.email+';'+n.password);
        }else {
        	//Ako fajl nije prazan dodaje se novi red na pocetku
        writer.write("\n"+n.username+';'+n.ime+';'+n.prezime+';'+n.jmbg+';'+n.brojKartice+';'+n.cvv+';'+n.email+';'+n.password);
        }
        } catch (IOException e) {
        e.printStackTrace();  // Dodajte odgovarajuću obradu grešaka
    }
}

private static Nalog pronadjiNalog(String username) {
	Nalog n=new Nalog(null, null, null, null, null, null, null, null);
	
	try (BufferedReader reader = new BufferedReader(new FileReader("src/bazaNaloga.txt"))) {
        String red;
        while ((red = reader.readLine()) != null) {
            String[] delovi = red.split(";");
            if (delovi.length == 8) {
                String username1 = delovi[0];

                if (username1.equals(username)) {
                	n.username=username1;
                	n.ime=delovi[1];
                	n.prezime=delovi[2];
                	n.jmbg=delovi[3];
                	n.brojKartice=delovi[4];
                	n.cvv=delovi[5];
                	n.email=delovi[6];
                	n.password=delovi[7];
                	
                  return n; 
                }
            }
        }
    } catch (IOException | NumberFormatException e) {
        e.printStackTrace();  
    }
	
	return null;
}
private static boolean isFileEmpty(String putanja) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(putanja))) {
        return reader.readLine() == null;
    }
}
private static void posaljiFajl(ObjectOutputStream outputStream,String putanja){
	 File fajl = new File(putanja);
	    try (FileInputStream fileInputStream = new FileInputStream(fajl)) {
	        byte[] buffer = new byte[(int) fajl.length()];
	        fileInputStream.read(buffer, 0, buffer.length);

	       
				outputStream.writeObject(buffer);
			
	       
				outputStream.flush();
			
	    } catch (IOException e) {
	        e.printStackTrace();  
	    }
}

	public static List<Transakcija> ucitajTransakcijeIzFajla(String putanja) {
        List<Transakcija> transakcije = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(putanja))) {
            String linija;
            while ((linija = reader.readLine()) != null) {
                String[] delovi = linija.split(";");
                if (delovi.length == 8) {
                    String korisnickoIme = delovi[0];
                    String ime = delovi[1];
                    String prezime = delovi[2];
                    String adresa = delovi[3];
                    String brojKartice = delovi[4];
                    String cvv = delovi[5];
                    double iznos = Double.parseDouble(delovi[6]);
                    //fond+=iznos;
                    String datumIVreme = delovi[7];
                    Transakcija transakcija = new Transakcija(korisnickoIme, ime, prezime, adresa, datumIVreme, brojKartice, cvv, iznos);
                    transakcije.add(transakcija);
                    
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();  
        }

        return transakcije;
}
	public static double izracunajSredstva(String putanja) {
		double fond=0;
		try (BufferedReader reader = new BufferedReader(new FileReader(putanja))) {
            String linija;
            while ((linija = reader.readLine()) != null) {
                String[] delovi = linija.split(";");
                if (delovi.length == 8) {
                    double iznos = Double.parseDouble(delovi[6]);
                    fond+=iznos;
                    
                    
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();  
        }
		return fond;
	}
	private static class Transakcija implements Serializable {
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String korisnickoIme;
	    private String ime;
	    private String prezime;
	    private String adresa;
	    private String datumIVreme;
	    private String brojKartice;
	    private String cvv;
	    double iznos;

	    public Transakcija(String korisnickoIme, String ime,String prezime, String adresa, String datumIVreme,String brojKartice,String cvv, double iznos) {
	        this.korisnickoIme = korisnickoIme;
	        this.ime = ime;
	        this.prezime=prezime;
	        this.adresa = adresa;
	        this.datumIVreme = datumIVreme;
	        this.brojKartice=brojKartice;
	        this.cvv=cvv;
	        this.iznos = iznos;
	    }

	    @Override
		public String toString() {
			return   "\nIme: " + ime + "\nPrezime: " + prezime + 
					 "\nDatum i vreme transakcije: " + datumIVreme +  "\nIznos: " + iznos+" dinara\n";
		}
	}
	 private static class Nalog {
	       private String username;
	       private String password;
	       private String ime;
	       private String prezime;
	       private String jmbg;
	       private String email;
	       private String brojKartice;
	       private String cvv;

	       public Nalog(String username, String password, String ime, String prezime, String jmbg, String email, String brojKartice, String cvv) {
	           this.username = username;
	           this.password = password;
	           this.ime = ime;
	           this.prezime = prezime;
	           this.jmbg = jmbg;
	           this.email = email;
	           this.brojKartice = brojKartice;
	           this.cvv = cvv;
	       }

	       public String getUsername() {
	           return username;
	       }

	       // Implementirati toString() metodu kako bi se korisnici mogli zapisati u fajl
	       @Override
	       public String toString() {
	           return username + ";" + password + ";" + ime + ";" + prezime + ";" + jmbg + ";" + email + ";" + brojKartice + ";" + cvv;
	       }
	   }
	}
  
    }







