package lipid;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdductDetectionTest {
    // !!TODO For the adduct detection both regular algorithms or drools can be used as far the tests are passed.


    @Before
    public void setup() {
        // !! TODO Empty by now,you can create common objects for all tests.
    }

    @Test
    public void shouldDetectAdductBasedOnMzDifference() {// test para comprobar los negativos

        // Given two peaks with ~21.98 Da difference (e.g., [M+H]+ and [M+Na]+)
        Peak mH = new Peak(700.500, 100000.0); // [M+H]+
        Peak mNa = new Peak(722.482, 80000.0);  // [M+Na]+
        Lipid lipid = new Lipid(1, "PC 34:1", "C42H82NO8P", LipidType.PC, 34, 1);

        double annotationMZ = 700.49999d;
        double annotationIntensity = 80000.0;
        double annotationRT = 6.5d;
        Annotation annotation = new Annotation(lipid, annotationMZ, annotationIntensity, annotationRT, Set.of(mH, mNa),Ionization.POSITVE);


        assertNotNull("[M+H]+ should be detected", annotation.getAdduct());
        assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());

    }


    @Test
    public void shouldDetectLossOfWaterAdduct() {
        Peak mh = new Peak(700.500, 90000.0);        // [M+H]+
        Peak mhH2O = new Peak(682.4894, 70000.0);     // [M+H–H₂O]+, ~18.0106 Da less

        Lipid lipid = new Lipid(1, "PE 36:2", "C41H78NO8P", LipidType.PE, 36, 2);
        Annotation annotation = new Annotation(lipid, mh.getMz(), mh.getIntensity(), 7.5d, Set.of(mh, mhH2O),Ionization.POSITVE);



        assertNotNull("[M+H]+ should be detected", annotation.getAdduct());

        assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());
    }

    @Test
    public void shouldDetectDoublyChargedAdduct() {
        // Assume real M = (700.500 - 1.0073) = 699.4927
        // So [M+2H]2+ = (M + 2.0146) / 2 = 350.7536
        Peak singlyCharged = new Peak(700.500, 100000.0);  // [M+H]+
        Peak doublyCharged = new Peak(350.754, 85000.0);   // [M+2H]2+

        Lipid lipid = new Lipid(3, "TG 54:3", "C57H104O6", LipidType.TG, 54, 3);
        Annotation annotation = new Annotation(lipid, singlyCharged.getMz(), singlyCharged.getIntensity(), 10d, Set.of(singlyCharged, doublyCharged),Ionization.POSITVE);

        assertNotNull("[M+H]+ should be detected", annotation.getAdduct());

        assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());
    }
    @Test
    public void shouldDetectNegativeAdductBasedOnMzDifference() {

        // Given two peaks with ~36.97 Da difference (e.g., [M–H]– and [M+Cl]–)
        Peak mH = new Peak(700.500, 100000.0); // [M–H]–
        Peak mCl = new Peak(736.4767, 80000.0);  // [M+Cl]–, con ~35.9767 Da diferencia


        Lipid lipid = new Lipid(4, "PI 38:4", "C47H83O13P", LipidType.PI, 38, 4);

        double annotationMZ = 700.49999d;
        double annotationIntensity = 95000.0;
        double annotationRT = 6.8d;

        Annotation annotation = new Annotation(
                lipid,
                annotationMZ,
                annotationIntensity,
                annotationRT,
                Set.of(mH, mCl),
                Ionization.NEGATIVE
        );

        // Suponiendo que detectAdductFromPeaks se llama en el constructor o se llama manualmente si no
        // annotation.detectAdductFromPeaks();

        assertNotNull("[M-H]− should be detected", annotation.getAdduct());
        assertEquals("Adduct inferred from lowest mz in group", "[M-H]−", annotation.getAdduct());
    }
    @Test
    public void shouldDetectAdductFromMultiplePeaks() {
        // Definimos 4 picos compatibles con 4 aductos distintos para la misma molécula
        Peak p1 = new Peak(700.500, 100000.0);     // [M+H]+
        Peak p2 = new Peak(722.489, 80000.0);      // [M+Na]+
        Peak p3 = new Peak(350.753, 85000.0);      // [M+2H]2+
        Peak p4 = new Peak(682.489, 70000.0);      // [M+H–H₂O]+

        // Datos del lípido (nombre y fórmula solo a efectos de completar la estructura)
        Lipid lipid = new Lipid(5, "PC 36:4", "C44H80NO8P", LipidType.PC, 36, 4);

        // Creamos la anotación usando uno de los picos como mz de referencia
        double annotationMz = 700.500;  // corresponde a [M+H]+
        double intensity = 100000.0;
        double rt = 6.0;

        Annotation annotation = new Annotation(lipid, p3.getMz(), p3.getIntensity(), rt, Set.of(p1, p2, p3, p4),Ionization.POSITVE);

        // Comprobamos que ha detectado correctamente el aducto principal
        assertNotNull("Adduct should be detected", annotation.getAdduct());
        assertEquals("[M+2H]2+", annotation.getAdduct());  // Es el que coincide con annotationMz
    }

    @Test
    public void shouldDetectNegativeAdductFromMultiplePeaks() {
        double neutralMass = 700.5; // ejemplo neutro

        Peak p1 = new Peak(neutralMass - 1.0073, 85000.0);   // [M-H]−
        Peak p2 = new Peak(neutralMass - 1.0073 - 18.0106, 60000.0);   // [M-H-H2O]−
        Peak p3 = new Peak(neutralMass + 34.969, 55000.0);   // [M+Cl]−
        Peak p4 = new Peak(neutralMass + 44.998, 58000.0);   // [M+HCOOH-H]−


        Lipid lipid = new Lipid(6, "PG 34:2", "C40H74O10P", LipidType.PG, 34, 2);

        // Creamos anotación con m/z de p2 → debería devolver [M-H-H2O]−
        double annotationMz = p2.getMz();
        double intensity = p2.getIntensity();
        double rt = 5.8;

        Annotation annotation = new Annotation(lipid, annotationMz, intensity, rt, Set.of(p1, p2, p3, p4), Ionization.NEGATIVE);

        // Verificación
        assertNotNull("Negative adduct should be detected", annotation.getAdduct());
        assertEquals("[M-H-H2O]−", annotation.getAdduct());
    }

    @Test
    public void shouldDetectDimerAdductAmongNegativeOptions() {
        // Masa neutra simulada de la molécula
        double neutralMass = 350.25;

        // Calculamos manualmente las m/z esperadas
        Peak p1 = new Peak(349.2427, 80000.0);   // [M-H]⁻
        Peak p2 = new Peak(331.2321, 60000.0);   // [M-H-H2O]⁻
        Peak p3 = new Peak(385.2190, 55000.0);   // [M+Cl]⁻
        Peak p4 = new Peak(395.2480, 58000.0);   // [M+HCOOH-H]⁻
        Peak p5 = new Peak(699.4927, 70000.0);   // [2M-H]⁻

        // Lipid de prueba
        Lipid lipid = new Lipid(7, "FA 18:1", "C18H34O2", LipidType.FA, 18, 1);

        // Se crea la anotación sobre el pico del dímero
        Annotation annotation = new Annotation(
                lipid,
                p5.getMz(),               // Se usa el dímero como mz principal
                p5.getIntensity(),
                10.0,
                Set.of(p1, p2, p3, p4, p5),
                Ionization.NEGATIVE
        );

        assertEquals("[2M-H]−", annotation.getAdduct());
    }

}
