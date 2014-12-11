package gaia.cu9.ari.gaiaorbit.util.math;

public class Test {

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	float[] cac = new float[] { 2.2234f, 124324.32432f, 435345534534.45435345f };
	double[] d = new double[3];

	System.arraycopy(cac, 0, d, 0, cac.length);

	System.out.println(cac);
	System.out.println(d);
    }

}
