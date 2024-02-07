/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package progettotps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProgettoTPS {
    
    private static int start;
    private static int end;
    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("64.227.73.44", 5555);

            // Invia dati al server
            OutputStream outputStream = clientSocket.getOutputStream();
            sendData(outputStream);

            // Ricevi dati dal server
            InputStream inputStream = clientSocket.getInputStream();
            List<Integer> primeNumbers = new ArrayList<>();
            primeNumbers=receiveData(inputStream);
            String jsonInput = "{\"start\": "+start+", \"end\": "+end+", \"prime_numbers\": "+primeNumbers+", \"completed\": true}";
            outputStream.write(jsonInput.getBytes());
            
            
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }

    private static void sendData(OutputStream outputStream) throws IOException {
        try {
            // Creazione del JSON e invio al server
            String jsonInput = "{\"disponibile\": true}";
            outputStream.write(jsonInput.getBytes());
        } catch (Exception e) {
            System.out.println("Error sending data: " + e.getMessage());
        }
    }

    private static List<Integer> receiveData(InputStream inputStream) throws IOException {
        try {
            Scanner scanner = new Scanner(inputStream);
            StringBuilder riceviData = new StringBuilder();

      
            riceviData.append(scanner.nextLine());

            String responseData = riceviData.toString();
            System.out.println("Received data from server: " + responseData);

            // Analizza la risposta JSON dal server
            if (responseData.contains("completed")) {
                System.out.println("Calculation completed");
            } else {
                // Se la risposta contiene dati sui numeri primi, analizza e visualizza i risultati
                // (assicurati di modificare questa parte in base alla struttura effettiva della risposta JSON)
                // Esempio: {"start": 10, "end": 20, "prime_numbers": [11, 13, 17, 19]}
                // Considera l'uso di una libreria JSON in un'applicazione più complessa
                List<Integer> primeNumbers = new ArrayList<>();
                primeNumbers=extractAndCalculatePrimes(responseData);
                System.out.println("Received prime numbers from server: " + primeNumbers);
                return primeNumbers;
            }
        } catch (Exception e) {
            System.out.println("prova3");
            System.out.println("Error receiving data: " + e.getMessage());
        }
        return null;
    }

    public static List<Integer> extractAndCalculatePrimes(String jsonString) {
        List<Integer> primeNumbers = new ArrayList<>();

        try {
            // Utilizza espressioni regolari per estrarre i numeri associati ai campi "start" e "end"
            Pattern pattern = Pattern.compile("\"start\": (\\d+), \"end\": (\\d+)");
            Matcher matcher = pattern.matcher(jsonString);

            // Se trova una corrispondenza, estrae i valori e calcola i numeri primi nel range
            if (matcher.find()) {
                start = Integer.parseInt(matcher.group(1));
                end = Integer.parseInt(matcher.group(2));

                System.out.println("Start: " + start);
                System.out.println("End: " + end);

                primeNumbers = calculatePrimesInRange(start, end);
            } else {
                System.out.println("Unable to extract 'start' and 'end' values from JSON string.");
            }
        } catch (Exception e) {
            System.out.println("Error extracting and calculating primes: " + e.getMessage());
        }

        return primeNumbers;
    }

    private static List<Integer> calculatePrimesInRange(int start, int end) {
        List<Integer> primes = new ArrayList<>();

        for (int num = start; num <= end; num++) {
            if (isPrime(num)) {
                primes.add(num);
            }
        }

        return primes;
    }

    private static boolean isPrime(int num) {
        if (num <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
}
