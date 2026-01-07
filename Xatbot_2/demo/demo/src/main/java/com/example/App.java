package com.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class App {
    // Definimos el modelo recomendado
    private static final String MODEL_NAME = "gemini-2.5-flash"; 
    private static Map<String, List<String>> datasets = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Leer la clave de la variable de entorno
        String apiKey = System.getenv("GEMINI_API_KEY");
        
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: No s'ha trobat la variable d'entorn GEMINI_API_KEY.");
            return;
        }

        // Configuración del cliente para la versión 1.30.0
        Client client = Client.builder().apiKey(apiKey).build();

        boolean salir = false;
        while (!salir) {
            mostrarMenu(); 
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1": generarNuevoSet(client); break;
                case "2": visualizarSets(); break;
                case "3": eliminarSets(); break;
                case "4": 
                    System.out.println("Tancant el programa. Fins aviat!");
                    salir = true; 
                    break;
                default: System.out.println("Opció no vàlida.");
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n--- Generador de Sets de Dades ---");
        System.out.println("1. Generar un nou set de dades");
        System.out.println("2. Visualitzar un o tots els sets de dades");
        System.out.println("3. Esborrar un o tots els sets de dades");
        System.out.println("4. Sortir");
        System.out.print("Tria una opció: ");
    }

    private static void generarNuevoSet(Client client) {
        try {
            System.out.println("\nGeneració d'un nou set");
            System.out.print("Introdueix un nom per al set de dades: ");
            String nombreSet = scanner.nextLine();
            
            System.out.println("Tipus de dada (1.Enters, 2.Decimals, 3.Text):");
            String tipoOpc = scanner.nextLine(); // Declaramos la variable que faltaba
            String tipoDato = tipoOpc.equals("1") ? "Enters" : tipoOpc.equals("2") ? "Decimals" : "Text";

            System.out.print("Quants elements vols? ");
            int cantidad = Integer.parseInt(scanner.nextLine());

            System.out.print("Quines dades necessites que et generi? ");
            String descripcion = scanner.nextLine();

            // Definir rol del sistema y construir el prompt 
            String systemRole = "Ets una eina que només genera llistes Java amb dades de prova.";
            String prompt = systemRole + " Genera una llista Java amb " + cantidad + " valors de tipus " + tipoDato + 
                            " sobre: " + descripcion + ". Torna només la llista en format [\"a\", \"b\"].";

            System.out.println("Generant dades... si us plau, espera.");
            
            // Llamada correcta a la API 1.30.0
            GenerateContentResponse response = client.models.generateContent(MODEL_NAME, prompt, null);
            String rawResponse = response.text().trim();

            if (rawResponse.contains("[") && rawResponse.contains("]")) {
                String cleanData = rawResponse.substring(rawResponse.indexOf("[") + 1, rawResponse.lastIndexOf("]"));
                List<String> lista = Arrays.asList(cleanData.split(",\\s*"));
                datasets.put(nombreSet, lista); // Guardamos el set
                System.out.println("Set \"" + nombreSet + "\" guardat correctament!");
            } else {
                System.out.println("Error: La resposta de la IA no és una llista vàlida.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void visualizarSets() {
        if (datasets.isEmpty()) {
            System.out.println("No hi ha datasets disponibles.");
            return;
        }
        System.out.println("\nSets disponibles: " + datasets.keySet());
        System.out.print("Quin vols visualitzar? ");
        String nombre = scanner.nextLine();
        
        if (datasets.containsKey(nombre)) {
            System.out.println("Set: " + nombre + " | Dades: " + datasets.get(nombre));
        } else {
            System.out.println("El set no existeix.");
        }
    }

    private static void eliminarSets() {
        System.out.print("Nom del set a eliminar (o 'tots'): ");
        String opc = scanner.nextLine();
        if (opc.equalsIgnoreCase("tots")) {
            datasets.clear();
            System.out.println("Tots els sets eliminats.");
        } else {
            datasets.remove(opc);
            System.out.println("Set eliminat.");
        }
    }
}