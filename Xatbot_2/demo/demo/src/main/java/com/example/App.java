package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class App {
    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String SYSTEM_ROLE = "Ets una eina que només genera llistes Java amb dades de prova. " +
            "Retorna SEMPRE i NOMÉS una llista en format Java: [\"element1\", \"element2\", ...] o [123, 456, ...]. " +
            "NO afegeixis cap explicació, només la llista.";
    
    private static Map<String, List<String>> datasets = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: No s'ha trobat la variable d'entorn GEMINI_API_KEY.");
            System.err.println("Executa: $env:GEMINI_API_KEY=\"la_teva_clau\"");
            return;
        }

        Client client = null;
        try {
            client = Client.builder().apiKey(apiKey).build();
        } catch (Exception e) {
            System.err.println("Error de connexió amb l'API: " + e.getMessage());
            return;
        }

        boolean sortir = false;
        while (!sortir) {
            mostrarMenu();
            String opcio = scanner.nextLine().trim();

            switch (opcio) {
                case "1":
                    generarNouSet(client);
                    break;
                case "2":
                    visualitzarSets();
                    break;
                case "3":
                    eliminarSets();
                    break;
                case "4":
                    System.out.println("Tancant el programa. Fins aviat!");
                    sortir = true;
                    break;
                default:
                    System.out.println("Opció no vàlida. Si us plau, tria una opció del 1 al 4.");
            }
        }
        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("\n------------------------------");
        System.out.println("Generador de Sets de Dades");
        System.out.println("------------------------------");
        System.out.println("1. Generar un nou set de dades");
        System.out.println("2. Visualitzar un o tots els sets de dades");
        System.out.println("3. Esborrar un o tots els sets de dades");
        System.out.println("4. Sortir");
        System.out.print("Tria una opció: ");
    }

    private static void generarNouSet(Client client) {
        try {
            System.out.println("\n------------------------------");
            System.out.println("Generació d'un nou set");
            System.out.println("------------------------------");
            
            System.out.print("Introdueix un nom per al set de dades: ");
            String nomSet = scanner.nextLine().trim();
            
            if (nomSet.isEmpty()) {
                System.out.println("Error: El nom no pot estar buit.");
                return;
            }
            
            if (datasets.containsKey(nomSet)) {
                System.out.print("Ja existeix un set amb aquest nom. Vols sobreescriure'l? (s/n): ");
                String resposta = scanner.nextLine().trim().toLowerCase();
                if (!resposta.equals("s")) {
                    System.out.println("Operació cancel·lada.");
                    return;
                }
            }

            System.out.println("Quin tipus de dada vols que sigui?");
            System.out.println("1 - Enters");
            System.out.println("2 - Decimals");
            System.out.println("3 - Text");
            System.out.print("Tipus de dada: ");
            String tipusOpcio = scanner.nextLine().trim();
            
            String tipusDada;
            String formatLlista;
            switch (tipusOpcio) {
                case "1":
                    tipusDada = "números enters";
                    formatLlista = "[123, 456, 789]";
                    break;
                case "2":
                    tipusDada = "números decimals";
                    formatLlista = "[12.5, 34.7, 56.9]";
                    break;
                case "3":
                    tipusDada = "text";
                    formatLlista = "[\"text1\", \"text2\", \"text3\"]";
                    break;
                default:
                    System.out.println("Opció no vàlida.");
                    return;
            }

            System.out.print("Quants elements vols? ");
            int quantitat;
            try {
                quantitat = Integer.parseInt(scanner.nextLine().trim());
                if (quantitat <= 0 || quantitat > 1000) {
                    System.out.println("Error: La quantitat ha d'estar entre 1 i 1000.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Has d'introduir un número vàlid.");
                return;
            }

            System.out.print("Quines dades necessites que et generi?\n> ");
            String descripcio = scanner.nextLine().trim();
            
            if (descripcio.isEmpty()) {
                System.out.println("Error: La descripció no pot estar buida.");
                return;
            }

            // Construcció del prompt segons l'estructura requerida
            String prompt = "Genera exactament " + quantitat + " valors de tipus " + tipusDada + 
                          " sobre: " + descripcio + ". " +
                          "Retorna NOMÉS la llista en format " + formatLlista + " sense cap text addicional.";

            System.out.println("Generant dades... si us plau, espera.");

            GenerateContentResponse response = client.models.generateContent(MODEL_NAME, prompt, null);
            String respostaRaw = response.text().trim();

            // Netejar la resposta (eliminar markdown si n'hi ha)
            respostaRaw = respostaRaw.replace("```java", "").replace("```", "").trim();
            
            // Extreure la llista
            List<String> llista = parsejarLlista(respostaRaw);
            
            if (llista == null || llista.isEmpty()) {
                System.out.println("Error: La resposta de la IA no és una llista vàlida.");
                System.out.println("Resposta rebuda: " + respostaRaw);
                return;
            }
            
            if (llista.size() != quantitat) {
                System.out.println("Advertència: S'esperaven " + quantitat + " elements però se n'han rebut " + llista.size());
            }

            datasets.put(nomSet, llista);
            System.out.println("Set \"" + nomSet + "\" guardat correctament!");

        } catch (Exception e) {
            System.out.println("Error durant la generació: " + e.getMessage());
        }
    }

    private static List<String> parsejarLlista(String text) {
        List<String> resultat = new ArrayList<>();
        
        try {
            // Buscar el primer [ i l'últim ]
            int inici = text.indexOf('[');
            int fi = text.lastIndexOf(']');
            
            if (inici == -1 || fi == -1 || inici >= fi) {
                return null;
            }
            
            // Extreure el contingut entre []
            String contingut = text.substring(inici + 1, fi).trim();
            
            if (contingut.isEmpty()) {
                return resultat;
            }
            
            // Separar per comes
            String[] elements = contingut.split(",");
            
            for (String element : elements) {
                element = element.trim();
                // Eliminar cometes si n'hi ha
                if ((element.startsWith("\"") && element.endsWith("\"")) ||
                    (element.startsWith("'") && element.endsWith("'"))) {
                    element = element.substring(1, element.length() - 1);
                }
                resultat.add(element);
            }
            
            return resultat;
            
        } catch (Exception e) {
            return null;
        }
    }

    private static void visualitzarSets() {
        System.out.println("\n------------------------------");
        System.out.println("Visualitzar Sets de Dades");
        System.out.println("------------------------------");
        
        if (datasets.isEmpty()) {
            System.out.println("No hi ha datasets disponibles.");
            return;
        }
        
        System.out.println("1 - Visualitzar un set concret");
        System.out.println("2 - Visualitzar tots els sets");
        System.out.print("Opció: ");
        String opcio = scanner.nextLine().trim();
        
        switch (opcio) {
            case "1":
                visualitzarSetConcret();
                break;
            case "2":
                visualitzarTotsSets();
                break;
            default:
                System.out.println("Opció no vàlida.");
        }
    }

    private static void visualitzarSetConcret() {
        System.out.println("\nSets disponibles:");
        for (String nom : datasets.keySet()) {
            System.out.println("- " + nom);
        }
        
        System.out.print("Quin vols visualitzar? ");
        String nom = scanner.nextLine().trim();
        
        if (datasets.containsKey(nom)) {
            List<String> dades = datasets.get(nom);
            System.out.println("\nSet: " + nom);
            System.out.println("Dades: " + dades);
            System.out.println("Nombre d'elements: " + dades.size());
        } else {
            System.out.println("El set '" + nom + "' no existeix.");
        }
    }

    private static void visualitzarTotsSets() {
        System.out.println("\nTots els sets:");
        for (Map.Entry<String, List<String>> entry : datasets.entrySet()) {
            System.out.println("\nSet: " + entry.getKey());
            System.out.println("Dades: " + entry.getValue());
            System.out.println("Nombre d'elements: " + entry.getValue().size());
        }
    }

    private static void eliminarSets() {
        System.out.println("\n------------------------------");
        System.out.println("Esborrar Sets de Dades");
        System.out.println("------------------------------");
        
        if (datasets.isEmpty()) {
            System.out.println("No hi ha datasets disponibles per esborrar.");
            return;
        }
        
        System.out.println("1 - Esborrar un set concret");
        System.out.println("2 - Esborrar tots els sets");
        System.out.print("Opció: ");
        String opcio = scanner.nextLine().trim();
        
        switch (opcio) {
            case "1":
                eliminarSetConcret();
                break;
            case "2":
                eliminarTotsSets();
                break;
            default:
                System.out.println("Opció no vàlida.");
        }
    }

    private static void eliminarSetConcret() {
        System.out.println("\nSets disponibles:");
        for (String nom : datasets.keySet()) {
            System.out.println("- " + nom);
        }
        
        System.out.print("Quin vols esborrar? ");
        String nom = scanner.nextLine().trim();
        
        if (datasets.containsKey(nom)) {
            datasets.remove(nom);
            System.out.println("Set '" + nom + "' esborrat correctament.");
        } else {
            System.out.println("El set '" + nom + "' no existeix.");
        }
    }

    private static void eliminarTotsSets() {
        System.out.print("Estàs segur que vols esborrar TOTS els sets? (s/n): ");
        String confirmacio = scanner.nextLine().trim().toLowerCase();
        
        if (confirmacio.equals("s")) {
            int quantitat = datasets.size();
            datasets.clear();
            System.out.println("S'han esborrat " + quantitat + " sets correctament.");
        } else {
            System.out.println("Operació cancel·lada.");
        }
    }
}