package TKPIU.core.utils;

import java.util.Scanner;

import TKPIU.config.Parameters;

/**
 * Thực hiện xử lý input đầu vào.
 */
public class InputProcessing {

    static Scanner sc = new Scanner(System.in);

    public static Parameters inputHandler() {
        int K = -1;
        double alpha = 0.5; // mặc định

        while (true) {
            try {
                System.out.print("Please enter Top-K value (integer > 0): ");
                String kInput = sc.nextLine().trim();
                K = Integer.parseInt(kInput);
                if (K > 0) {
                    break;
                } else {
                    System.out.println("K must be greater than 0. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input for K. Please enter an integer.");
            }
        }

        while (true) {
            System.out.print("Please enter alpha value (1-9, empty for default=0.5): ");
            String alphaInput = sc.nextLine().trim();

            if (alphaInput.isEmpty()) {
                alpha = 0.5; 
                break;
            }

            try {
                int alphaInt = Integer.parseInt(alphaInput);
                if (alphaInt >= 1 && alphaInt <= 9) {
                    alpha = alphaInt / 10.0; 
                    break;
                } else {
                    System.out.println("Alpha must be between 1 and 9. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input for alpha. Please enter an integer between 1 and 9.");
            }
        }
        return new Parameters(K, alpha, 1 - alpha);
    }
}
