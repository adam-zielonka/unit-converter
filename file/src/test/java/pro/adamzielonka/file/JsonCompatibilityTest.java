package pro.adamzielonka.file;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class JsonCompatibilityTest {
    static class ConverterData {
        String name;
        double factor;
        double[] values;
    }

    private ConverterData read(String json) {
        return Open.openJSON(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                ConverterData.class);
    }

    @Test public void savedConverterRoundTripsUnicodeAndNumbers() {
        ConverterData original = new ConverterData();
        original.name = "Zażółć gęślą";
        original.factor = 0.001;
        original.values = new double[] { -12.5, 0, 1e12 };
        ConverterData restored = read(FileTools.toJSON(original));
        assertEquals(original.name, restored.name);
        assertEquals(original.factor, restored.factor, 0);
        assertArrayEquals(original.values, restored.values, 0);
    }

    @Test public void existingSpecialFloatingPointValuesRemainReadable() {
        ConverterData original = new ConverterData();
        original.factor = Double.POSITIVE_INFINITY;
        original.values = new double[] { Double.NaN, Double.NEGATIVE_INFINITY };
        ConverterData restored = read(FileTools.toJSON(original));
        assertEquals(Double.POSITIVE_INFINITY, restored.factor, 0);
        assertTrue(Double.isNaN(restored.values[0]));
        assertEquals(Double.NEGATIVE_INFINITY, restored.values[1], 0);
    }

    @Test public void importsKeepIgnoringUnknownFields() {
        ConverterData restored = read("{\"name\":\"Metres\",\"factor\":1,\"futureField\":{\"value\":42}}");
        assertEquals("Metres", restored.name);
        assertEquals(1, restored.factor, 0);
    }
}
