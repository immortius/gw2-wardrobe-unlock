import java.util.Base64;

/**
 *
 */
public class ChatCode {

    public static int toId(String chatCode) {
        String base64Part = chatCode.substring(2, chatCode.length() - 1);
        System.out.println(base64Part);
        byte[] bits = Base64.getDecoder().decode(base64Part);
        int code = (bits[5] << 24) + (bits[4] << 16) + (bits[3] << 8) + bits[2];
        return code;
    }
}
