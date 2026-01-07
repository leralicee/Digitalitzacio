import java.util.*;

public class PE04_xatbot {
    private static Scanner scanner = new Scanner(System.in);
    private static List<String> conversationHistory = new ArrayList<>();
    
    // Color codes
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    
    // Configuration
    private static String systemRole = "Eres un asistente útil";
    private static double temperature = 0.7;
    
    public static void main(String[] args) {
        System.out.println(CYAN + "=== XATBOT - Sistema de Diálogo ===" + RESET);
        mostrarComandos();
        
        while (true) {
            System.out.print(BLUE + "\nTú: " + RESET);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) continue;
            
            // Comandos especiales
            if (input.equalsIgnoreCase("quit")) {
                System.out.println(GREEN + "¡Hasta pronto!" + RESET);
                break;
            } else if (input.equalsIgnoreCase("help")) {
                mostrarComandos();
            } else if (input.equalsIgnoreCase("clear")) {
                limpiarHistorial();
            } else if (input.equalsIgnoreCase("history")) {
                mostrarHistorial();
            } else if (input.equalsIgnoreCase("config")) {
                cambiarConfiguracion();
            } else {
                // Mensaje normal - lo guardamos en el historial
                conversationHistory.add("Tú: " + input);
                conversationHistory.add("Bot: [Respuesta de la API]");
                System.out.println(GREEN + "Bot: [Aquí irá la respuesta de la API]" + RESET);
            }
        }
        
        scanner.close();
    }
    
    private static void mostrarComandos() {
        System.out.println(YELLOW + "\n--- Comandos Disponibles ---" + RESET);
        System.out.println("• help - Mostrar esta ayuda");
        System.out.println("• config - Cambiar configuración");
        System.out.println("• clear - Limpiar historial de conversación");
        System.out.println("• history - Mostrar historial");
        System.out.println("• quit - Salir del programa");
    }
    
    private static void limpiarHistorial() {
        conversationHistory.clear();
        System.out.println(GREEN + "✓ Historial limpiado" + RESET);
    }
    
    private static void mostrarHistorial() {
        if (conversationHistory.isEmpty()) {
            System.out.println(YELLOW + "El historial está vacío" + RESET);
            return;
        }
        
        System.out.println(CYAN + "\n--- Historial de Conversación ---" + RESET);
        for (String mensaje : conversationHistory) {
            if (mensaje.startsWith("Tú:")) {
                System.out.println(BLUE + mensaje + RESET);
            } else {
                System.out.println(GREEN + mensaje + RESET);
            }
        }
    }
    
    private static void cambiarConfiguracion() {
        System.out.println(YELLOW + "\n--- Configuración Actual ---" + RESET);
        System.out.println("Rol: " + systemRole);
        System.out.println("Temperatura: " + temperature);
        
        System.out.print("\nNuevo rol [" + systemRole + "]: ");
        String nuevoRol = scanner.nextLine().trim();
        if (!nuevoRol.isEmpty()) {
            systemRole = nuevoRol;
        }
        
        System.out.print("Nueva temperatura (0.0-1.0) [" + temperature + "]: ");
        String nuevaTemp = scanner.nextLine().trim();
        if (!nuevaTemp.isEmpty()) {
            try {
                temperature = Double.parseDouble(nuevaTemp);
                System.out.println(GREEN + "✓ Configuración actualizada" + RESET);
            } catch (NumberFormatException e) {
                System.out.println(RED + "✗ Temperatura inválida" + RESET);
            }
        }
    }
}