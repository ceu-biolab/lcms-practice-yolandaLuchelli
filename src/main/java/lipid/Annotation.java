package lipid;

import java.util.*;
import adduct.*;
/**
 * Class to represent the annotation over a lipid
 */
public class Annotation {

    private final Lipid lipid;
    private final double mz;
    private final double intensity; // intensity of the most abundant peak in the groupedPeaks
    private final double rtMin;
    private String adduct; // !!TODO The adduct will be detected based on the groupedSignals
    private final Set<Peak> groupedSignals;
    private int score;
    private int totalScoresApplied;
    private Ionization ionization;
    private static final double PPMTOLERANCE = 10;

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, Ionization ionization) {
        this(lipid, mz, intensity, retentionTime, Collections.emptySet(),ionization);
    }

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param groupedSignals
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, Set<Peak> groupedSignals,Ionization ionization) {
        this.lipid = lipid;
        this.mz = mz;
        this.rtMin = retentionTime;
        this.intensity = intensity;
        this.ionization = ionization;
        // !!TODO This set should be sorted according to help the program to deisotope the signals plus detect the adduct
        this.groupedSignals = new TreeSet<>(groupedSignals);
        this.score = 0;
        this.totalScoresApplied = 0;
        detectAdductFromPeaks();

    }

    public Lipid getLipid() {
        return lipid;
    }

    public double getMz() {
        return mz;
    }

    public double getRtMin() {
        return rtMin;
    }

    public String getAdduct() {
        return adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }

    public double getIntensity() {
        return intensity;
    }

    public Set<Peak> getGroupedSignals() {
        return Collections.unmodifiableSet(groupedSignals);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Ionization getIonization() {
        return ionization;
    }

    public void setIonization(Ionization ionization) {
        this.ionization = ionization;
    }


    // !TODO Take into account that the score should be normalized between 0 and 1
    public void addScore(int delta) {
        this.score += delta;
        this.totalScoresApplied++;
    }

    public double getNormalizedScore() {
        return (double) this.score / this.totalScoresApplied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        Annotation that = (Annotation) o;
        return Double.compare(that.mz, mz) == 0 &&
                Double.compare(that.rtMin, rtMin) == 0 &&
                Objects.equals(lipid, that.lipid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lipid, mz, rtMin);
    }

    @Override
    public String toString() {
        return String.format("Annotation(%s, mz=%.4f, RT=%.2f, adduct=%s, intensity=%.1f, score=%d)",
                lipid.getName(), mz, rtMin, adduct, intensity, score);
    }

    // !!TODO Detect the adduct with an algorithm or with drools, up to the user.

   /* public void detectAdductFromPeaks() {

        String finalAdduct = null;

        // Aductos positivos
        if (ionization == Ionization.POSITVE) {
            for (String adduct1 : AdductList.MAPMZPOSITIVEADDUCTS.keySet()) {
                for (String adduct2 : AdductList.MAPMZPOSITIVEADDUCTS.keySet()) {
                    if (adduct1.equals(adduct2)) continue;

                    for (Peak p1 : groupedSignals) {
                        for (Peak p2 : groupedSignals) {
                            if (p1.equals(p2)) continue;

                            Double mass1 = Adduct.getMonoisotopicMassFromMZ(p1.getMz(), adduct1);
                            Double mass2 = Adduct.getMonoisotopicMassFromMZ(p2.getMz(), adduct2);

                            if (mass1 != null && mass2 != null &&
                                    Adduct.calculatePPMIncrement(mass1, mass2) <= PPMTOLERANCE) {

                                if (Adduct.calculatePPMIncrement(p1.getMz(), this.mz) <= PPMTOLERANCE) {
                                    finalAdduct = adduct1;
                                    this.adduct = finalAdduct;
                                    return;
                                } else if (Adduct.calculatePPMIncrement(p2.getMz(), this.mz) <= PPMTOLERANCE) {
                                    finalAdduct = adduct2;
                                    this.adduct = finalAdduct;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } else if (ionization == Ionization.NEGATIVE) {
            for (String adduct1 : AdductList.MAPMZNEGATIVEADDUCTS.keySet()) {
                for (String adduct2 : AdductList.MAPMZNEGATIVEADDUCTS.keySet()) {
                    if (adduct1.equals(adduct2)) continue;

                    for (Peak p1 : groupedSignals) {
                        for (Peak p2 : groupedSignals) {
                            if (p1.equals(p2)) continue;

                            Double mass1 = Adduct.getMonoisotopicMassFromMZ(p1.getMz(), adduct1);
                            Double mass2 = Adduct.getMonoisotopicMassFromMZ(p2.getMz(), adduct2);

                            if (mass1 != null && mass2 != null &&
                                    Adduct.calculatePPMIncrement(mass1, mass2) <= PPMTOLERANCE) {

                                if (Adduct.calculatePPMIncrement(p1.getMz(), this.mz) <= PPMTOLERANCE) {
                                    finalAdduct = adduct1;
                                    this.adduct = finalAdduct;
                                    return;
                                } else if (Adduct.calculatePPMIncrement(p2.getMz(), this.mz) <= PPMTOLERANCE) {
                                    finalAdduct = adduct2;
                                    this.adduct = finalAdduct;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        this.adduct = finalAdduct; // Fallback por si no se cumple ninguna condición
    }*/
   public void detectAdductFromPeaks() {
       String finalAdduct = null;

       System.out.println("Detecting adduct... Ionization mode: " + ionization);

       if (ionization == Ionization.POSITVE) {
           for (String adduct1 : AdductList.MAPMZPOSITIVEADDUCTS.keySet()) {
               for (String adduct2 : AdductList.MAPMZPOSITIVEADDUCTS.keySet()) {
                   if (adduct1.equals(adduct2)) continue;

                   System.out.println("Trying adduct pair: " + adduct1 + " vs " + adduct2);

                   for (Peak p1 : groupedSignals) {
                       for (Peak p2 : groupedSignals) {
                           if (p1.equals(p2)) continue;

                           System.out.println("  Comparing peaks: " + p1.getMz() + " vs " + p2.getMz());

                           Double mass1 = Adduct.getMonoisotopicMassFromMZ(p1.getMz(), adduct1);
                           Double mass2 = Adduct.getMonoisotopicMassFromMZ(p2.getMz(), adduct2);

                           System.out.println("    Monoisotopic masses: " + mass1 + " vs " + mass2);

                           if (mass1 != null && mass2 != null &&
                                   Adduct.calculatePPMIncrement(mass1, mass2) <= PPMTOLERANCE) {

                               System.out.println("    --> Masses match within tolerance!");

                               if (Adduct.calculatePPMIncrement(p1.getMz(), this.mz) <= PPMTOLERANCE) {
                                   System.out.println("    ==> Match with current peak mz! Assigning: " + adduct1);
                                   this.adduct = adduct1;
                                   return;
                               } else if (Adduct.calculatePPMIncrement(p2.getMz(), this.mz) <= PPMTOLERANCE) {
                                   System.out.println("    ==> Match with current peak mz! Assigning: " + adduct2);
                                   this.adduct = adduct2;
                                   return;
                               }
                           }
                       }
                   }
               }
           }
       } else if (ionization == Ionization.NEGATIVE) {
           for (String adduct1 : AdductList.MAPMZNEGATIVEADDUCTS.keySet()) {
               for (String adduct2 : AdductList.MAPMZNEGATIVEADDUCTS.keySet()) {
                   if (adduct1.equals(adduct2)) continue;

                   System.out.println("Trying adduct pair: " + adduct1 + " vs " + adduct2);

                   for (Peak p1 : groupedSignals) {
                       for (Peak p2 : groupedSignals) {
                           if (p1.equals(p2)) continue;

                           System.out.println("  Comparing peaks: " + p1.getMz() + " vs " + p2.getMz());

                           Double mass1 = Adduct.getMonoisotopicMassFromMZ(p1.getMz(), adduct1);
                           Double mass2 = Adduct.getMonoisotopicMassFromMZ(p2.getMz(), adduct2);

                           System.out.println("    Monoisotopic masses: " + mass1 + " vs " + mass2);

                           if (mass1 != null && mass2 != null &&
                                   Adduct.calculatePPMIncrement(mass1, mass2) <= PPMTOLERANCE) {

                               System.out.println("    --> Masses match within tolerance!");

                               if (Adduct.calculatePPMIncrement(p1.getMz(), this.mz) <= PPMTOLERANCE) {
                                   System.out.println("    ==> Match with current peak mz! Assigning: " + adduct1);
                                   this.adduct = adduct1;
                                   return;
                               } else if (Adduct.calculatePPMIncrement(p2.getMz(), this.mz) <= PPMTOLERANCE) {
                                   System.out.println("    ==> Match with current peak mz! Assigning: " + adduct2);
                                   this.adduct = adduct2;
                                   return;
                               }
                           }

                       }
                   }
               }
           }
       }

       System.out.println("⚠️ No valid adduct found.");
       this.adduct = finalAdduct; // fallback
   }


}



