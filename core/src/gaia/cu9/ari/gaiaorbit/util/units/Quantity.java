package gaia.cu9.ari.gaiaorbit.util.units;

import gaia.cu9.ari.gaiaorbit.util.units.Quantity.Length.LengthUnit;

/**
 * A wee utility class that provides unit conversion mechanisms.
 * @author Toni Sagrista
 *
 */
public class Quantity {

    public static class Length {
        public enum LengthUnit {
            /** Millimetres **/
            MM(1d / 1000d),
            /** Centimetres **/
            CM(1d / 100d),
            /** Metres **/
            M(1d),
            /** Kilometres **/
            KM(1000d),
            /** Astronomical units **/
            AU(149597870700d),
            /** Parsecs **/
            PC(3.08567758e16),
            /** Megaparsecs **/
            MPC(3.08567758e22);

            final double m;

            LengthUnit(double m) {
                this.m = m;
            }

        }

        double value_m;

        public Length(double value, LengthUnit unit) {
            value_m = value * unit.m;
        }

        public Length(double value, String unit) {
            this(value, LengthUnit.valueOf(unit.toUpperCase()));
        }

        public double get(LengthUnit unit) {
            return value_m * (1d / unit.m);
        }

    }

    public static class Angle {

        public enum AngleUnit {
            /** Degrees **/
            DEG(1d),
            /** Radians **/
            RAD(180d / Math.PI),
            /** Milliarcseconds **/
            MAS(1d / 3600000d),
            /** Arcseconds **/
            ARCSEC(1d / 3600d), ARCMIN(1d / 60d);

            final double deg;

            AngleUnit(double deg) {
                this.deg = deg;
            }

        }

        double value_deg;

        public Angle(double value, AngleUnit unit) {
            value_deg = value * unit.deg;
        }

        public Angle(double value, String unit) {
            this(value, AngleUnit.valueOf(unit.toUpperCase()));
        }

        public double get(AngleUnit unit) {
            return value_deg * (1d / unit.deg);
        }

        /**
         * Gets the parallax distance of this angle.
         * @return A length with the distance of this angle interpreted as a parallax.
         */
        public Length getParallaxDistance() {
            double mas = get(AngleUnit.MAS);
            if (mas < 0) {
                mas = 0.001;
            }
            return new Length(1000d / mas, LengthUnit.PC);
        }

    }

    public static class Brightness {

        public enum BrightnessUnit {
            MAG(1d);

            final double mag;

            BrightnessUnit(double mag) {
                this.mag = mag;
            }
        }

        double value_mag;

        public Brightness(double value, BrightnessUnit unit) {
            value_mag = value * unit.mag;
        }

        public Brightness(double value, String unit) {
            this(value, BrightnessUnit.valueOf(unit.toUpperCase()));
        }

        public double get(BrightnessUnit unit) {
            return value_mag * (1d / unit.mag);
        }

    }
}
