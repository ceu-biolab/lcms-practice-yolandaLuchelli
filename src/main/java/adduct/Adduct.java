package adduct;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adduct {

    /**
     * Calculate the mass to search depending on the adduct hypothesis
     *
     * @param  mz
     * @param adduct adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     *
     * @return the mass difference within the tolerance respecting to the
     * massToSearch
     */
    public static Double getMonoisotopicMassFromMZ(Double mz, String adduct) {
        if (mz == null || adduct == null || adduct.isEmpty()) {
            return null;
        }

        Double adductMass = AdductList.MAPMZPOSITIVEADDUCTS.getOrDefault(adduct, AdductList.MAPMZNEGATIVEADDUCTS.get(adduct));
        if (adductMass == null) {
            return null;
        }

        int charge = 1;
        Matcher chargeMatcher = Pattern.compile("([2-9])([+-])").matcher(adduct);
        if (chargeMatcher.find()) {
            charge = Integer.parseInt(chargeMatcher.group(1));
        }

        int multimer = 1;
        Matcher multimerMatcher = Pattern.compile("\\[([2-9])M").matcher(adduct);
        if (multimerMatcher.find()) {
            multimer = Integer.parseInt(multimerMatcher.group(1));
        }

        double numerator = (mz * charge) + adductMass;

        return numerator / multimer;
    }


    /**
     * Calculate the mz of a monoisotopic mass with the corresponding adduct
     *
     * @param monoisotopicMass
     * @param adduct           adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     * @return
     */
    public static Double getMZFromMonoisotopicMass(Double monoisotopicMass, String adduct) {
        if (monoisotopicMass == null || adduct == null || adduct.isEmpty()) {
            return null;
        }

        Double adductMass = AdductList.MAPMZPOSITIVEADDUCTS.get(adduct);
        if (adductMass == null) {
            adductMass = AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);
        }

        if (adductMass == null) {
            return null;
        }

        int multimer = 1;
        Matcher multimerMatcher = Pattern.compile("\\[(\\d*)M").matcher(adduct);
        if (multimerMatcher.find()) {
            String num = multimerMatcher.group(1);
            if (!num.isEmpty()) multimer = Integer.parseInt(num);
        }

        int charge = 1;
        Matcher chargeMatcher = Pattern.compile("(\\d*)([+-])\\]").matcher(adduct);
        if (chargeMatcher.find()) {
            String chargeStr = chargeMatcher.group(1);
            if (!chargeStr.isEmpty()) charge = Integer.parseInt(chargeStr);
        }

        double totalMass = monoisotopicMass * multimer;
        return (totalMass + adductMass) / charge;
    }

        /*
        if Adduct is single charge the formula is m/z = M +- adductMass. Charge is 1 so it does not affect

        if Adduct is double or triple charged the formula is mz = M/charge +- adductMass

        if adduct is a dimer or multimer the formula is mz = M * numberOfMultimer +- adductMass

        return monoisotopicMass;

         */
        //return null;


    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param experimentalMass Mass measured by MS
     * @param theoreticalMass  Theoretical mass of the compound
     */
    public static int calculatePPMIncrement(Double experimentalMass, Double theoreticalMass) {
        int ppmIncrement;
        ppmIncrement = (int) Math.round(Math.abs((experimentalMass - theoreticalMass) * 1000000
                / theoreticalMass));
        return ppmIncrement;
    }

    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param experimentalMass Mass measured by MS
     * @param ppm          ppm of tolerance
     */
    public static double calculateDeltaPPM(Double experimentalMass, int ppm) {
        double deltaPPM;
        deltaPPM = Math.round(Math.abs((experimentalMass * ppm) / 1000000));
        return deltaPPM;

    }
}




