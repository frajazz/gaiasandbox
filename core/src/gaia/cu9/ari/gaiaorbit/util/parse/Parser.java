/**
 * 
 */
package gaia.cu9.ari.gaiaorbit.util.parse;

import java.util.Locale;

/**
 * Parser utilities.
 * @author Miguel Gomes (mdg@uninova.pt)
 * @since 10/02/2015 14:29:47
 *
 */
public final class Parser {
    /**
     * <p>Quick long string parser that can handle negative and positive values.</p>
     * <p>Parser supports leading/trailing whitespace.</p>
     * @param input String to parse
     * @return Parsed long or 0 if the parsing fails
     */
    public static long parseLong(String input)
    {
        long result = 0;
        int pos = 0;
        int len = input.length();
        if (len == 0)
            return 0;
        char c = input.charAt(0);
        long sign = 1;

        // skip any starting white space
        while (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0')
        {
            ++pos;
            if (pos >= len)
                return 0;
            c = input.charAt(pos);
        }

        // handle both - and + signs
        if (c == '-')
        {
            sign = -1;
            ++pos;
            if (pos >= len)
                return 0;
        }
        else if (c == '+')
        {
            sign = 1;
            ++pos;
            if (pos >= len)
                return 0;
        }

        while (true) // breaks inside on pos >= len or non-digit character
        {
            if (pos >= len)
                return sign * result;
            c = input.charAt(pos++);
            if (c < '0' || c > '9') {
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0') // break on trailing white space or exponent
                    break;
                else
                    return 0;
            }
            result = (result * 10) + (c - '0');
        }

        return sign * result;
    }

    /**
     * <p>Quick long string parser that can handle negative and positive values.</p>
     * <p>Parser supports leading/trailing whitespace.</p>
     * @param input String to parse
     * @return Parsed long or 0 if the parsing fails
     * @throws NumberFormatException If the given input can not be parsed
     */
    public static long parseLongException(String input) throws NumberFormatException
    {
        long result = 0;
        int pos = 0;
        int len = input.length();
        if (len == 0)
            return 0;
        char c = input.charAt(0);
        long sign = 1;

        // skip any starting white space
        while (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0')
        {
            ++pos;
            if (pos >= len)
                throw new NumberFormatException("Invalid input");
            c = input.charAt(pos);
        }

        // handle both - and + signs
        if (c == '-')
        {
            sign = -1;
            ++pos;
            if (pos >= len)
                throw new NumberFormatException("Invalid input");
        }
        else if (c == '+')
        {
            sign = 1;
            ++pos;
            if (pos >= len)
                throw new NumberFormatException("Invalid input");
        }

        while (true) // breaks inside on pos >= len or non-digit character
        {
            if (pos >= len)
                return sign * result;
            c = input.charAt(pos++);
            if (c < '0' || c > '9') {
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0') // break on trailing white space or exponent
                    break;
                else
                    throw new NumberFormatException("Invalid input");
            }
            result = (result * 10) + (c - '0');
        }

        return sign * result;
    }

    /**
     * <p>Quick double string parser that can handle negative and positive doubles but not exponents.</p>
     * <p>Parser supports NaN, +/-Inf, exponents and leading/trailing whitespace.</p>
     * <p>Parser is mostly locale unaware except for dot and comma for decimal separator.
     * @param input String to parse
     * @return Parsed double or Double.NaN if the parsing fails
     * @see {@link Parser#parseDoubleException(String)} If exceptions on invalid input is necessary
     */
    public static double parseDouble(String input)
    {
        double result = 0;
        int pos = 0;
        int len = input.length();
        if (len == 0)
            return Double.NaN;
        char c = input.charAt(0);
        double sign = 1;

        // skip any starting white space
        while (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0')
        {
            ++pos;
            if (pos >= len)
                return Double.NaN;
            c = input.charAt(pos);
        }

        // if input looks like N, it probably is
        if (c == 'N' || c == 'n')
        {
            return Double.NaN;
        }

        // handle both - and + signs
        if (c == '-')
        {
            sign = -1;
            ++pos;
            if (pos >= len)
                return Double.NaN;
        }
        else if (c == '+')
        {
            sign = 1;
            ++pos;
            if (pos >= len)
                return Double.NaN;
        }

        c = input.charAt(pos);
        if (c == 'I' || c == 'i') // probably inf, let's confirm
        {
            ++pos;
            if (pos >= len)
                return Double.NaN;
            c = input.charAt(pos);
            if (!(c == 'N' || c == 'n'))
                return Double.NaN;
            ++pos;
            if (pos >= len)
                return Double.NaN;
            c = input.charAt(pos);
            if (!(c == 'F' || c == 'f'))
                return Double.NaN;
            else
            {
                if (sign == -1)
                    return Double.NEGATIVE_INFINITY;
                else
                    return Double.POSITIVE_INFINITY;
            }
        }

        while (true) // breaks inside on pos >= len or non-digit character
        {
            if (pos >= len)
                return sign * result;
            c = input.charAt(pos++);
            if (c < '0' || c > '9')
                break;
            result = (result * 10.0) + (c - '0');
        }

        if (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0') // break on trailing white space
            return sign * result;

        if (c != '.' && c != ',')
            return Double.NaN;
        double exp = 0.1;
        while (pos < len)
        {
            c = input.charAt(pos++);
            if (c < '0' || c > '9') {
                if (c == 'e' || c == 'E' || c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0') // break on trailing white space or exponent
                    break;
                else
                    return Double.NaN;
            }
            result += (c - '0') * exp;
            exp *= 0.1;
        }
        if (!(c == 'e' || c == 'E') || pos == len)
            return sign * result;

        int expSign = 1;
        c = input.charAt(pos);

        // handle both - and + signs
        if (c == '-')
        {
            expSign = -1;
            ++pos;
            if (pos >= len)
                return sign * result;
        }
        else if (c == '+')
        {
            expSign = 1;
            ++pos;
            if (pos >= len)
                return sign * result;
        }

        double expResult = 0;

        while (true) // breaks inside on pos >= len or non-digit character
        {
            if (pos >= len)
                return sign * result * Math.pow(10.0, expSign * expResult);
            c = input.charAt(pos++);
            if (c < '0' || c > '9')
                break;
            expResult = (expResult * 10.0) + (c - '0');
        }

        if (c != '.' && c != ',')
            return sign * result * Math.pow(10.0, expSign * expResult);

        double expExp = 0.1;
        while (pos < len)
        {
            c = input.charAt(pos++);
            if (c < '0' || c > '9') {
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0') // break on trailing white space or exponent
                    break;
                else
                    return Double.NaN;
            }
            expResult += (c - '0') * expExp;
            expExp *= 0.1;
        }

        return sign * result * Math.pow(10.0, expSign * expResult);
    }

    /**
     * <p>Quick double string parser that can handle negative and positive doubles but not exponents.</p>
     * <p>Parser supports NaN, +/-Inf and leading/trailing whitespace.</p>
     * <p>Parser is mostly locale unaware except for dot and comma for decimal separator.
     * @param input String to parse
     * @return Parsed double or Double.NaN if the parsing fails
     * @throws NumberFormatException If the input string is invalid
     */
    public static double parseDoubleException(String input) throws NumberFormatException
    {
        double result = 0;
        int pos = 0;
        int len = input.length();
        if (len == 0)
            throw new NumberFormatException("Invalid input");
        char c = input.charAt(0);
        double sign = 1;

        // skip any starting white space
        while (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0')
        {
            ++pos;
            if (pos >= len)
                throw new NumberFormatException("Invalid input");
            c = input.charAt(pos);
        }

        // if input looks like N, it probably is
        if (c == 'N' || c == 'n')
        {
            return Double.NaN;
        }

        // handle both - and + signs
        if (c == '-')
        {
            sign = -1;
            ++pos;
            if (pos >= len)
                throw new NumberFormatException("Invalid input");
        }
        else if (c == '+')
        {
            sign = 1;
            ++pos;
            if (pos >= len)
                throw new NumberFormatException("Invalid input");
        }

        c = input.charAt(pos);
        if (c == 'I' || c == 'i') // probably inf, let's confirm
        {
            ++pos;
            if (pos >= len)
                throw new NumberFormatException("Invalid input");
            c = input.charAt(pos);
            if (!(c == 'N' || c == 'n'))
                throw new NumberFormatException("Invalid input");
            ++pos;
            if (pos >= len)
                throw new NumberFormatException("Invalid input");
            c = input.charAt(pos);
            if (!(c == 'F' || c == 'f'))
                throw new NumberFormatException("Invalid input");
            else
            {
                if (sign == -1)
                    return Double.NEGATIVE_INFINITY;
                else
                    return Double.POSITIVE_INFINITY;
            }
        }

        while (true) // breaks inside on pos >= len or non-digit character
        {
            if (pos >= len)
                return sign * result;
            c = input.charAt(pos++);
            if (c < '0' || c > '9')
                break;
            result = (result * 10.0) + (c - '0');
        }

        if (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0') // break on trailing white space
            return sign * result;

        if (c != '.' && c != ',')
            throw new NumberFormatException("Invalid input");
        double exp = 0.1;
        while (pos < len)
        {
            c = input.charAt(pos++);
            if (c < '0' || c > '9') {
                if (c == 'e' || c == 'E' || c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0') // break on trailing white space or exponent
                    break;
                else
                    throw new NumberFormatException("Invalid input");
            }
            result += (c - '0') * exp;
            exp *= 0.1;
        }
        if (!(c == 'e' || c == 'E') || pos == len)
            return sign * result;

        int expSign = 1;
        c = input.charAt(pos);

        // handle both - and + signs
        if (c == '-')
        {
            expSign = -1;
            ++pos;
            if (pos >= len)
                return sign * result;
        }
        else if (c == '+')
        {
            expSign = 1;
            ++pos;
            if (pos >= len)
                return sign * result;
        }

        double expResult = 0;

        while (true) // breaks inside on pos >= len or non-digit character
        {
            if (pos >= len)
                return sign * result * Math.pow(10.0, expSign * expResult);
            c = input.charAt(pos++);
            if (c < '0' || c > '9')
                break;
            expResult = (expResult * 10.0) + (c - '0');
        }

        if (c != '.' && c != ',')
            return sign * result * Math.pow(10.0, expSign * expResult);

        double expExp = 0.1;
        while (pos < len)
        {
            c = input.charAt(pos++);
            if (c < '0' || c > '9') {
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\0') // break on trailing white space or exponent
                    break;
                else
                    throw new NumberFormatException("Invalid input");
            }
            expResult += (c - '0') * expExp;
            expExp *= 0.1;
        }

        return sign * result * Math.pow(10.0, expSign * expResult);
    }

    /**
     * Method used to parse accepted boolean values in incoming messages
     * @param what What to parse
     * @return True or false (also default return) depending on the value of what
     */
    public static boolean parseBoolean(Object what)
    {
        if (what instanceof String)
        {
            String text = ((String) what).toLowerCase(Locale.ENGLISH);
            return (text.startsWith("t") || text == "1" || text == "on" || text == "yes");
        }
        else if (what instanceof Boolean)
            return (Boolean) what;
        else
            return false;
    }

    /**
     * Parses an integer. If the input is not a valid integer representation it returns 0.
     * @param str The input string.
     * @return The integer representation of the string.
     */
    public static int parseInt(String str) {
        int ival = 0, idx = 0, end;
        boolean sign = false;
        char ch;

        if (str == null || (end = str.length()) == 0 ||
                ((ch = str.charAt(0)) < '0' || ch > '9')
                && (!(sign = ch == '-') || ++idx == end || ((ch = str.charAt(idx)) < '0' || ch > '9')))
            return 0;

        for (;; ival *= 10)
        {
            ival += '0' - ch;
            if (++idx == end)
                return sign ? ival : -ival;
            if ((ch = str.charAt(idx)) < '0' || ch > '9')
                return 0;
        }
    }

    /**
     * Parses an integer. Throws a {@link NumberFormatException} if the input is not a
     * valid integer representation.
     * @param str The input string.
     * @return The integer representation of the string.
     */
    public static int parseIntException(String str) throws NumberFormatException {
        int ival = 0, idx = 0, end;
        boolean sign = false;
        char ch;

        if (str == null || (end = str.length()) == 0 ||
                ((ch = str.charAt(0)) < '0' || ch > '9')
                && (!(sign = ch == '-') || ++idx == end || ((ch = str.charAt(idx)) < '0' || ch > '9')))
            throw new NumberFormatException(str);

        for (;; ival *= 10)
        {
            ival += '0' - ch;
            if (++idx == end)
                return sign ? ival : -ival;
            if ((ch = str.charAt(idx)) < '0' || ch > '9')
                throw new NumberFormatException(str);
        }
    }

    /**
     * Convenience method which uses the double parser and casts the result.
     * It will not throw 
     * Please check {@link Parser#parseDouble(String)}. 
     * @param input The input string.
     * @return The parsed float, or 0 if the parsing failed.
     */
    public static float parseFloat(String input) {
        return (float) parseDouble(input);
    }

    /**
     * Convenience method which uses the double parser and casts the result. 
     * Please check {@link Parser#parseDouble(String)}
     * @param input The input string.
     * @return The parsed float, or 0 if the parsing failed.
     */
    public static float parseFloatException(String input) {
        return (float) parseDouble(input);
    }

    public static void main(String[] args) {
        //	testPerformance(args);
        parseIntTest();
    }

    private static void testPerformance(String[] args) {
        boolean testPerformance = true;
        if (args.length > 0)
            testPerformance = true;
        System.out.println("Double parsing tests: ");
        String[] doubleTests = new String[] {
                "    ",
                "0.41415678167461873648172341234141413421",
                ".41415678167461873648172341234141413421",
                "   0,41415678167461873648172341234141413421  ",
                "   0.41415678167461873648172341234141413421  ",
                "00001.1414134141",
                "NaN",
                "nAn",
                "+Inf",
                "-Inf",
                "inf",
                "1.0e5",
                "1.0e5.8",
                "-1.0e5.8",
                "1.0e-5.8",
                "-1.0e-5.8",
                "-0.1514",
                "FailString",
                "014Fail"
        };
        // test validity
        for (String test : doubleTests) {
            System.out.println("Parsing: \"" + test + "\"...");
            double quickParserValue = parseDouble(test);
            System.out.println("\tQuickParser:\t" + quickParserValue);
            try {
                double javaParserValue = parseDoubleException(test);
                System.out.println("\tQuickParserE:\t" + javaParserValue);
            } catch (NumberFormatException e) {
                System.out.println("\tQuickParserE:\tNumberFormatException");
            }
            try {
                double javaParserValue = Double.parseDouble(test);
                System.out.println("\tJavaParser:\t" + javaParserValue);
            } catch (NumberFormatException e) {
                System.out.println("\tJavaParser:\tNumberFormatException");
            }
        }
        if (testPerformance) {
            System.out.println("Performance benchmark:");
            // test speed
            long startTime = System.nanoTime();
            for (int i = 0; i < 1000000; i++) {
                for (String test : doubleTests) {
                    parseDouble(test);
                }
            }
            System.out.println("\tQuickParser:\t" + (System.nanoTime() - startTime) + "ns (" + ((System.nanoTime() - startTime) / 1000000) + " ms)");
            startTime = System.nanoTime();
            for (int i = 0; i < 1000000; i++) {
                for (String test : doubleTests) {
                    try {
                        parseDoubleException(test);
                    } catch (NumberFormatException e) {
                        // ignore it 
                    }
                }
            }
            System.out.println("\tQuickParserE:\t" + (System.nanoTime() - startTime) + "ns (" + ((System.nanoTime() - startTime) / 1000000) + " ms)");
            startTime = System.nanoTime();
            for (int i = 0; i < 1000000; i++) {
                for (String test : doubleTests) {
                    try {
                        Double.parseDouble(test);
                    } catch (NumberFormatException e) {
                        // ignore it 
                    }
                }
            }
            System.out.println("\tJavaParser:\t" + (System.nanoTime() - startTime) + "ns (" + ((System.nanoTime() - startTime) / 1000000) + " ms)");
        }
        System.out.println("Long parsing tests: ");
        String[] longTests = new String[] {
                "   ",
                "413421",
                "  413421",
                "  413421  ",
                "-413421",
                "3147483647",
                "-3147483647",
                "9223372036854775804",
                "NaN",
        };
        // test validity
        for (String test : longTests) {
            System.out.println("Parsing: \"" + test + "\"...");
            long quickParserValue = parseLong(test);
            System.out.println("\tQuickParser:\t" + quickParserValue);
            try {
                long javaParserValue = parseLongException(test);
                System.out.println("\tQuickParserE:\t" + javaParserValue);
            } catch (NumberFormatException e) {
                System.out.println("\tQuickParserE:\tNumberFormatException");
            }
            try {
                long javaParserValue = Long.parseLong(test);
                System.out.println("\tJavaParser:\t" + javaParserValue);
            } catch (NumberFormatException e) {
                System.out.println("\tJavaParser:\tNumberFormatException");
            }
        }
        if (testPerformance) {
            System.out.println("Performance benchmark:");
            // test speed
            long startTime = System.nanoTime();
            for (int i = 0; i < 1000000; i++) {
                for (String test : longTests) {
                    parseLong(test);
                }
            }
            System.out.println("\tQuickParser:\t" + (System.nanoTime() - startTime) + "ns (" + ((System.nanoTime() - startTime) / 1000000) + " ms)");
            startTime = System.nanoTime();
            for (int i = 0; i < 1000000; i++) {
                for (String test : longTests) {
                    try {
                        parseLongException(test);
                    } catch (NumberFormatException e) {
                        // ignore it 
                    }
                }
            }
            System.out.println("\tQuickParserE:\t" + (System.nanoTime() - startTime) + "ns (" + ((System.nanoTime() - startTime) / 1000000) + " ms)");
            startTime = System.nanoTime();
            for (int i = 0; i < 1000000; i++) {
                for (String test : longTests) {
                    try {
                        Long.parseLong(test);
                    } catch (NumberFormatException e) {
                        // ignore it 
                    }
                }
            }
            System.out.println("\tJavaParser:\t" + (System.nanoTime() - startTime) + "ns (" + ((System.nanoTime() - startTime) / 1000000) + " ms)");
        }
        System.out.println("Integer parsing tests: ");
        String[] intTests = new String[] {
                "   ",
                "413421",
                "  413421",
                "  413421  ",
                "-413421",
                "3147483647",
                "-3147483647",
                "9223372036854775804",
                "NaN",
        };
        // test validity
        for (String test : intTests) {
            System.out.println("Parsing: \"" + test + "\"...");
            int quickParserValue = (int) parseLong(test);
            System.out.println("\tQuickParser:\t" + quickParserValue);
            try {
                int javaParserValue = (int) parseLongException(test);
                System.out.println("\tQuickParserE:\t" + javaParserValue);
            } catch (NumberFormatException e) {
                System.out.println("\tQuickParserE:\tNumberFormatException");
            }
            try {
                int javaParserValue = Integer.parseInt(test);
                System.out.println("\tJavaParser:\t" + javaParserValue);
            } catch (NumberFormatException e) {
                System.out.println("\tJavaParser:\tNumberFormatException");
            }
        }
        if (testPerformance) {
            System.out.println("Performance benchmark:");
            // test speed
            long startTime = System.nanoTime();
            for (int i = 0; i < 1000000; i++) {
                for (String test : doubleTests) {
                    parseLong(test);
                }
            }
            System.out.println("\tQuickParser:\t" + (System.nanoTime() - startTime) + "ns (" + ((System.nanoTime() - startTime) / 1000000) + " ms)");
            startTime = System.nanoTime();
            for (int i = 0; i < 1000000; i++) {
                for (String test : intTests) {
                    try {
                        parseLongException(test);
                    } catch (NumberFormatException e) {
                        // ignore it 
                    }
                }
            }
            System.out.println("\tQuickParserE:\t" + (System.nanoTime() - startTime) + "ns (" + ((System.nanoTime() - startTime) / 1000000) + " ms)");
            startTime = System.nanoTime();
            for (int i = 0; i < 1000000; i++) {
                for (String test : intTests) {
                    try {
                        Long.parseLong(test);
                    } catch (NumberFormatException e) {
                        // ignore it 
                    }
                }
            }
            System.out.println("\tJavaParser:\t" + (System.nanoTime() - startTime) + "ns (" + ((System.nanoTime() - startTime) / 1000000) + " ms)");
        }
    }

    public static void parseIntTest() {
        String[] tests = { "2323", "1009938775645", "-23234", "0", "10994877", "-09230", "32", "000", "-1", "8923784" };
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            for (String test : tests) {
                try {
                    Integer.parseInt(test);
                } catch (NumberFormatException e) {
                    // ignore it 
                }
            }
        }
        System.out.println("\tJavaParser:\t" + ((System.nanoTime() - startTime) / 1000000) + " ms");

        startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            for (String test : tests) {
                try {
                    parseInt(test);
                } catch (NumberFormatException e) {
                    // ignore it 
                }
            }
        }
        System.out.println("\tparseInt() Time:\t" + ((System.nanoTime() - startTime) / 1000000) + " ms");

        startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            for (String test : tests) {
                try {
                    parseDouble(test);
                } catch (NumberFormatException e) {
                    // ignore it 
                }
            }
        }
        System.out.println("\tparseDouble() Time:\t" + ((System.nanoTime() - startTime) / 1000000) + " ms");
    }
}
