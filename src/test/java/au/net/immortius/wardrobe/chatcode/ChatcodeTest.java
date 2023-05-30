package au.net.immortius.wardrobe.chatcode;

import au.net.immortius.wardrobe.gw2api.Chatcode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ChatcodeTest {

    private final int type;
    private final int id;
    private final String expected;
    private final String testName;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"Weapon", 10, 643, "[&CoMCAAA=]"},
                {"Gathering Tool", 10, 2389, "[&ClUJAAA=]"},
                {"Outfit", 11, 35, "[&CyMAAAA=]"},
                {"Mini", 2, 49292, "[&AgGMwAAA]"}
        });
    }

    public ChatcodeTest(String testName, int type, int id, String expected) {
        this.testName = testName;
        this.type = type;
        this.id = id;
        this.expected = expected;
    }

    @Test
    public void test() {
        assertEquals(testName, expected, Chatcode.create(type, Integer.toString(id)));
    }
}
