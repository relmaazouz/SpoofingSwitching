package manipulations;

import java.util.Random;

public class TriangularDistribution {

    public static void main(String[] args) {
        // Example usage
        double minValue = 0.25;
        double maxValue = 0.75;
        double centerValue = 0.5;
        int numberOfValues = 10;

        double[] triangularValues = generateTriangularDistribution(minValue, maxValue, centerValue, numberOfValues);

        // Print the generated values
        for (double value : triangularValues) {
            System.out.println(value);
        }
    }

    public static double[] generateTriangularDistribution(double minValue, double maxValue, double centerValue, int count) {
        double[] result = new double[count];
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            result[i] = generateTriangularValue(minValue, maxValue, centerValue, random);
        }

        return result;
    }

    private static double generateTriangularValue(double minValue, double maxValue, double centerValue, Random random) {
        double u = random.nextDouble();
        double f = (centerValue - minValue) / (maxValue - minValue);

        if (u <= f) {
            return minValue + Math.sqrt(u * (maxValue - minValue) * (centerValue - minValue));
        } else {
            return maxValue - Math.sqrt((1 - u) * (maxValue - minValue) * (maxValue - centerValue));
        }
    }
}