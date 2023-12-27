package Klijent;

import java.io.*;
import java.net.*;

public class Klijent {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;

    private static ObjectOutputStream outputStream;
    private static ObjectInputStream inputStream;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            //primanje poruke o uspesnoj konekciji
            String konekcija = (String) inputStream.readObject();
            System.out.println("Server: " + konekcija);
            
            boolean registrovan=false;
            while (true) {
            	
                String response = (String) inputStream.readObject();
                System.out.println("Server: " + response);

                int option = Integer.parseInt(reader.readLine());
                outputStream.writeObject(option);
                outputStream.flush();

           if(option==1) {
        	   primiFajl("src/bazaKartica.txt");
        	  
        	   
        	   if(!registrovan) {
        		   System.out.println("Prvo unesite svoje podatke da biste izvršili uplatu.\nIme:");
        	   String ime=reader.readLine();
        	   outputStream.writeObject(ime);
        	   outputStream.flush();
        	  
        	   //Prezime
        	  System.out.println("Prezime: "); 
        	  String prezime=reader.readLine();
        	  outputStream.writeObject(prezime);
        	  outputStream.flush();
        	   }
        	  //Adresa
        	  System.out.println("Adresa: ");
        	  String adresa=reader.readLine();
        	  outputStream.writeObject(adresa);
        	  outputStream.flush();
        	  //Broj kartice i cvv
        	  
        	  if(!registrovan) {
        	  boolean postoji=false;
        	  while(postoji==false) {
        	  System.out.println("Broj kartice u formatu xxxx-xxxx-xxxx-xxxx:");
        	  String brKartice=reader.readLine(); 
        	  System.out.println("Cvv:");
        	  String cvv=reader.readLine();
        	  
        	  postoji=daLiPostojiKartica(brKartice, cvv);
        	  if(postoji==false) {
        		  System.out.println("Kartica ne postoji u nasoj bazi molimo pokusajte opet!\n");
        	  }else {
        		  //Salje se broj kartice
        		  outputStream.writeObject(brKartice);
        		  outputStream.flush();
        		  //Salje se cvv broj
        		  outputStream.writeObject(cvv);
        		  outputStream.flush();
        	  }
        	  
        	  }
        	  }
        	  
        	  //iznos
        	  System.out.println("Iznos:");
        	  double iznos=0;
        	 boolean validan=false;
        	 while(validan==false) {
        	     String pare=reader.readLine();	
        	     while(!pare.matches("\\d+")) {
        	    	 System.out.println("NIste uneli iznos u dobrom formatu pokusajte ponovo:");
        	    	 pare=reader.readLine();
        	     }
        	     pare.trim();
        		  iznos=Double.parseDouble(pare);
        		 if(iznos>=200) {
        			 validan=true;
        			 break;
        		 }else System.out.println("Iznos mora biti veci od 200. Molimo pokusajte opet:");
        	 }
        	outputStream.writeObject(iznos);
        	outputStream.flush();
        	//primanje poruke o zavrsetku donacije
        	String zavrsetak = (String) inputStream.readObject();
            System.out.println("Server: " + zavrsetak);
            //primanje fiskalnog racuna
            String fiskalni=(String) inputStream.readObject();
            primiFajl(fiskalni);
           }
           if(option==2) {
        	   String stanje=(String) inputStream.readObject();
        	   System.out.println("Server: "+stanje);
           }
           if(option==3) {
        	   String pregled=(String) inputStream.readObject();
        	   System.out.println("Server: "+pregled);
           }
           if (option == 7) {
                String exit=(String) inputStream.readObject();
                System.out.println("Server: "+exit);
                break;
                }
           if(option==5) {  
        	   	  primiFajl("src/bazaKartica.txt");
        	   	  primiFajl("src/bazaNaloga.txt");
            	  System.out.println("Dobrodosli na registraciju.\nMolimo unesite zeljeni username:");
            	  String username=reader.readLine();
            	  //provera da li postoji jos neki korisnik sa datim usernamom
            	  while(daLiPostojiUsername(username)) {
            		  System.out.println("Dat username vec postoji,izaberite drugi: ");
            		  username=reader.readLine();
            	  }
            	  username.trim();
            	  outputStream.writeObject(username);
            	  outputStream.flush();
            	  //Slanje imena korisnika
            	  System.out.println("Vase ime: ");
            	  String ime=reader.readLine();
            	  ime.trim();
            	  outputStream.writeObject(ime);
            	  outputStream.flush();
            	  //Slanje prezimena korisnika
            	  System.out.println("Prezime: ");
            	  String prezime=reader.readLine();
            	  prezime.trim();
            	  outputStream.writeObject(prezime);
            	  outputStream.flush();
            	  //slanje JMBG-a
            	  System.out.println("JMBG: ");
            	  String jmbg=reader.readLine();
            	  while(!jmbg.matches("\\d{13}")) {
            		  System.out.println("Niste uneli jmbg u dobrom formatu.\nPokusajte opet");
            		  jmbg=reader.readLine();            
            		  }
            	  outputStream.writeObject(jmbg);
         			outputStream.flush();
         		 //slanje broja platne kartice
         			boolean postoji=false;
              	  while(postoji==false) {
              	  System.out.println("Broj kartice u formatu xxxx-xxxx-xxxx-xxxx:");
              	  String brKartice=reader.readLine(); 
              	  System.out.println("Cvv:");
              	  String cvv=reader.readLine();
              	  
              	  postoji=daLiPostojiKartica(brKartice, cvv);
              	  if(postoji==false) {
              		  System.out.println("Kartica ne postoji u nasoj bazi molimo pokusajte opet!\n");
              	  }else {
              		  //Salje se broj kartice
              		  outputStream.writeObject(brKartice);
              		  outputStream.flush();
              		  //Salje se cvv broj
              		  outputStream.writeObject(cvv);
              		  outputStream.flush();
              	  }
              	  }
              	  //slanje korisnickog e-maila
              	  System.out.println("E-mail adresa: ");
              	  String email=reader.readLine();
              	  while(!email.contains("@") || email.endsWith("@")) {
              		  System.out.println("Email adresa mora sadrzati @(majmunce) i mora imati nesto posle toga.Molimo pokusajte opet");
              		  email=reader.readLine();              	 
              		  }
              	  email.trim();
              	  outputStream.writeObject(email);
              	  outputStream.flush();
              	  //Slanje sifre
              	  System.out.println("I na kraju sifra: ");
              	  String password=reader.readLine();
              	  password.trim();
              	  while(password.contains(" ")) {
              		  System.out.println("Vasa sifra se mora sadrzati od jednog neprekidnog niza karaktera!\nPokusajte ponovo:");
              		  password=reader.readLine();
                	  password.trim();
              	  }
              	  outputStream.writeObject(password);
              	  outputStream.flush();
              	//primanje poruke o zavrsetku donacije
              	String zavrsetak = (String) inputStream.readObject();
                  System.out.println("Server: " + zavrsetak);
              	  }
           
           //Klijentska strana postupka logina korisnika
           if(option==4) {
        	   //primanje baze naloga
        	   primiFajl("src/bazaNaloga.txt");
        	   //unos username-a
        	   System.out.println("Unesite Vas username:");
        	   String username=reader.readLine();
        	   
        	   while(!daLiPostojiUsername(username)) {
        		   System.out.println("Dat username ne postoji pokusajte opet: ");
        		   username=reader.readLine();
        	   }
        	   username.trim();
         	   //Unos sifre
        	   System.out.println("Unesite sifru:");
        	   String password=reader.readLine();
        	   while(!proveriSifru(username, password)) {
        		   System.out.println("Ta sifra ne odgovara datom nalogu.\nMolimo da pokusate opet:");
        		   password=reader.readLine();
        	   }
        	   //poruka koja se ispisuje nakon uspesnog logina
        	   System.out.println("Uspesno ste se logovali na vas nalog!\n");
        	   outputStream.writeObject(username);
        	   outputStream.flush();
        	   registrovan=true;
        	   
        	   //poruka koja se ispisuje nakon uspesnog povezivanja korisnika
        	   
           }
           if(option==6) {
        	   String stanje=(String) inputStream.readObject();
        	   System.out.println("Server: "+stanje);
        	   registrovan=false;
           }
           			            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
       
    }
    
    
    private static boolean daLiPostojiKartica(String brojKartice,String cvv) {
    	try (BufferedReader reader = new BufferedReader(new FileReader("src/bazaKartica.txt"))) {
            String red;
            while ((red = reader.readLine()) != null) {
                String[] delovi = red.split(" ");
                if (delovi.length == 2) {
                    String kartica = delovi[0];
                    String cvv1 = delovi[1];

                    if (kartica.equals(brojKartice) && cvv1.equals(cvv)) {
                        return true;  // Kartica pronađena
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();  
        }

        return false;  // Kartica nije pronađena
    }
    private static boolean daLiPostojiUsername(String username) {
    	
    	try (BufferedReader reader = new BufferedReader(new FileReader("src/bazaNaloga.txt"))) {
            String red;
            while ((red = reader.readLine()) != null) {
                String[] delovi = red.split(";");
                if (delovi.length == 8) {
                    String username1 = delovi[0];

                    if (username1.equals(username)) {
                        return true;  // Username pronađen
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();  
        }
    	
    	
    	return false;
    }
    private static boolean proveriSifru(String username,String sifra) {
    	try (BufferedReader reader = new BufferedReader(new FileReader("src/bazaNaloga.txt"))) {
            String red;
            while ((red = reader.readLine()) != null) {
                String[] delovi = red.split(";");
                if (delovi.length == 8) {
                    String username1 = delovi[0];
                    String sifra1=delovi[7];
                    
                    
                    if (username1.equals(username) && sifra1.equals(sifra)) {
                        return true;  // Nalog pronadjen
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();  
        }
    	
    	
    	return false;
    }
  
    private static void primiFajl(String putanja) throws IOException, ClassNotFoundException {
        Object obj = inputStream.readObject();
        if (obj instanceof byte[]) {
            byte[] buffer = (byte[]) obj;

            File fajl = new File(putanja);

            if (fajl.exists() && fajl.length() != buffer.length) {
                dopuniFajl(putanja, buffer);
                System.out.println("Fajl dopunjen na putanji " + putanja + "\n");
            } else if (!fajl.exists()) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(putanja)) {
                    fileOutputStream.write(buffer, 0, buffer.length);
                    System.out.println("Fajl primljen i sačuvan kao " + putanja + "\n");
                } catch (IOException e) {
                    e.printStackTrace();  // Dodajte odgovarajuću obradu grešaka
                }
            } else {
                System.out.println("Fajl već postoji na putanji " + putanja);
            }
        }
    }

    private static void dopuniFajl(String putanja, byte[] noviSadrzaj) {
        try (FileInputStream fileInputStream = new FileInputStream(putanja)) {
            byte[] postojećiSadržaj = fileInputStream.readAllBytes();
            
            // Provera da li novi sadržaj već postoji u fajlu
            if (!sadrži(postojećiSadržaj, noviSadrzaj)) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(putanja, true)) {
                    fileOutputStream.write(noviSadrzaj, 0, noviSadrzaj.length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  
        }
    }

    private static boolean sadrži(byte[] niz, byte[] podniz) {
        int n = niz.length;
        int m = podniz.length;

        for (int i = 0; i <= n - m; i++) {
            int j;
            for (j = 0; j < m; j++) {
                if (niz[i + j] != podniz[j]) {
                    break;
                }
            }
            if (j == m) {
                return true; // podniz pronađen u nizu
            }
        }
        return false; // podniz nije pronađen u nizu
    }
}

