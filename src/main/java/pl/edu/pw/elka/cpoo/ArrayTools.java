package pl.edu.pw.elka.cpoo;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

/**
 *
 * @author Alex
 */
public class ArrayTools {

    public static double[] convert2D1D(float[][] in, int height, int width) {
        double out[] = new double[width * height];
        for (int i = 0; i < in.length; i++) {
            for (int j = 0; j < in[i].length; j++) {
                out[j + i * width] = in[i][j];
            }
        }
        return out;
    }

    public static double[][] subarray2D(double[][] in, int lb1, int ub1, int lb2, int ub2) {
        double out[][] = new double[ub1 - lb1 + 1][ub2 - lb2 + 1];
        for (int i = 0; i < out.length; i++) {
            for (int j = 0; j < out[i].length; j++) {
                out[i][j] = in[lb1 + i][lb2 + j];
            }
        }
        return out;
    }

    public static double[] subarray1D(double[] in, int lb1, int ub1) {
        double out[] = new double[ub1 - lb1 + 1];
        for (int i = 0; i < out.length; i++) {
            out[i] = in[lb1 + i];
        }
        return out;
    }

    static int rank(double[] in, int m, int n) {
        double eps = Math.pow(2.0, -52.0);
        double tol = Math.max(m, n) * in[0] * eps;
        int r = 0;
        for (int i = 0; i < in.length; i++) {
            if (in[i] > tol) {
                r++;
            }
        }
        return r;
    }

    public static String arrayToString(Object array) {
        if (array == null) {
            return "[NULL]";
        } else {
            Object obj = null;
            if (array instanceof Hashtable) {
                array = ((Hashtable) array).entrySet().toArray();
            } else if (array instanceof HashSet) {
                array = ((HashSet) array).toArray();
            } else if (array instanceof Collection) {
                array = ((Collection) array).toArray();
            }
            int length = Array.getLength(array);
            int lastItem = length - 1;
            StringBuffer sb = new StringBuffer("[");
            for (int i = 0; i < length; i++) {
                obj = Array.get(array, i);
                if (obj != null) {
                    sb.append(obj);
                } else {
                    sb.append("[NULL]");
                }
                if (i < lastItem) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    public static float[][] convert1D2D(float[] in, int height, int width) {
        float[][] out = new float[height][width];
        for (int i = 0; i < out.length; i++) {
            for (int j = 0; j < out[i].length; j++) {
                out[i][j] = in[j + i * width];
            }
        }
        return out;
    }
}
