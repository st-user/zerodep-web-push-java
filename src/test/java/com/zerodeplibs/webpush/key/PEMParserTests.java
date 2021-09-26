package com.zerodeplibs.webpush.key;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class PEMParserTests {

    @Test
    public void parseShouldExtractBytesFromPEMEncodedText() {

        String rawText = "Hello World!!";
        String base64Text = toBase64WithoutPadding(rawText);

        String pemString = concat("-----BEGIN PRIVATE KEY-----",// preeb
            new String(new char[] {0x9, 0x20}), // WSP
            "\r\n", // eol
            new String(new char[] {0x9, 0x20}) + "\r\n", // eolWSP
            base64Text.substring(0, 10) + new String(new char[] {0x9, 0x20}) + "\r\n", // base64line
            base64Text.substring(10) + "= \r\n= \r\n", // base64finl
            "-----END PRIVATE KEY-----", // posteb
            new String(new char[] {0x9, 0x20}), // WSP
            "\r\n"// eol
        );

        PEMParser parser = PEMParsers.ofStandard("PRIVATE KEY");

        assertParseResult(parser.parse(pemString), rawText);
    }

    @Test
    public void parseShouldExtractBytesFromPEMEncodedTextContainsOutsideTheEbs() {

        String rawText = "Hello World!!";
        String base64Text = toBase64(rawText);

        String pemString = concat("---",
            "-----BEGIN PRIVATE KEY-----", "\r\n",// preeb
            base64Text, "\r\n",
            "-----END PRIVATE KEY-----", // posteb
            "---"
        );

        PEMParser parser = PEMParsers.ofStandard("PRIVATE KEY");

        assertParseResult(parser.parse(pemString), rawText);
    }

    @Test
    public void illegalBeginEncapsulationBoundary() {

        String base64Text = toBase64("TEST");
        PEMParser parser = PEMParsers.ofStandard("PRIVATE KEY");

        String pemString1 = concat(
            "-----BEGIN PUBLIC KEY-----", "\r\n",
            base64Text, "\r\n",
            "-----END PRIVATE KEY-----"
        );

        assertThat(assertThrows(InvalidPEMFormatException.class,
            () -> parser.parse(pemString1)).getMessage(), equalTo(
            "A '-----BEGIN' encapsulation boundary doesn't exist or the format is invalid."));

        String pemString2 = concat(
            "-----BEGIN PRIVATE KEY-----", " X ", "\r\n",
            base64Text, "\r\n",
            "-----END PRIVATE KEY-----"
        );

        assertThat(assertThrows(InvalidPEMFormatException.class,
            () -> parser.parse(pemString2)).getMessage(), equalTo(
            "The line of the '-----BEGIN' encapsulation boundary contains an illegal character after '-----BEGIN PRIVATE KEY-----': X"));
    }

    @Test
    public void illegalEndEncapsulationBoundary() {

        String base64Text = toBase64("TEST");
        PEMParser parser = PEMParsers.ofStandard("PRIVATE KEY");

        String pemString1 = concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Text, "\r\n",
            "-----END Private KEY-----"
        );

        assertThat(assertThrows(InvalidPEMFormatException.class,
            () -> parser.parse(pemString1)).getMessage(), equalTo(
            "A '-----END' encapsulation boundary doesn't exist or the format is invalid."));

        assertParseWithInvalidBase64Text(parser, 2, concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Text,
            "-----END PRIVATE KEY-----"
            // This line is NOT posteb because preceding eol doesn't exist.
        ));
    }

    @Test
    public void checkBase64Line() {

        String rawText = "Hello World!!";
        String base64Text = toBase64(rawText);
        String base64Line1 = base64Text.substring(0, 5);
        String base64Line2 = base64Text.substring(5, 10);
        String base64Finl = base64Text.substring(10);
        PEMParser parser = PEMParsers.ofStandard("PRIVATE KEY");

        assertParseResult(parser.parse(concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Finl, "\r\n",
            "-----END PRIVATE KEY-----"
        )), rawText);

        assertParseResult(parser.parse(concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            " \r    \n   \r\n " + base64Line1 + base64Line2, "\r\n",
            base64Finl, "\r\n",
            "-----END PRIVATE KEY-----"
        )), rawText);

        assertParseResult(parser.parse(concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            "\r\n",
            "-----END PRIVATE KEY-----"
        )), "");

        assertParseWithInvalidBase64Text(parser, 2, concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            " - ", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Finl, "\r\n",
            "-----END PRIVATE KEY-----"
        ));

        assertParseWithInvalidBase64Text(parser, 3, concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1, "\r\n",
            " " + base64Line2, "\r\n",
            base64Finl, "\r\n",
            "-----END PRIVATE KEY-----"
        ));

        assertParseWithInvalidBase64Text(parser, 2, concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2 + " A", "\r\n",
            base64Finl, "\r\n",
            "-----END PRIVATE KEY-----"
        ));

        assertParseWithInvalidBase64Text(parser, 2, concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            "A-", "\r\n",
            base64Finl, "\r\n",
            "-----END PRIVATE KEY-----"
        ));

        assertParseWithInvalidBase64Text(parser, IllegalArgumentException.class, concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            "A\r\n",
            "-----END PRIVATE KEY-----"
        ));
    }

    private void assertParseWithInvalidBase64Text(PEMParser parser, int illegalLineNo,
                                                  String pemString) {

        String expectedMessage =
            "The base64 text is malformed or contains an illegal character(line: %d).";

        assertThat(assertThrows(InvalidPEMFormatException.class,
                () -> parser.parse(pemString)).getMessage(),
            equalTo(String.format(expectedMessage, illegalLineNo)));
    }

    private void assertParseWithInvalidBase64Text(PEMParser parser,
                                                  Class<? extends Throwable> cause,
                                                  String pemString) {

        InvalidPEMFormatException exception =
            assertThrows(InvalidPEMFormatException.class, () -> parser.parse(pemString));

        assertNotNull(exception.getCause(), exception.getMessage());
        assertThat(exception.getCause().getClass(), equalTo(cause));
    }

    @Test
    public void checkBase64Finl() {

        String rawText = "Hello World!!"; // 2 padding
        String base64Text = toBase64WithoutPadding(rawText);
        String base64Line1 = base64Text.substring(0, 5);
        String base64Line2 = base64Text.substring(5, 10);
        String base64Line3 = base64Text.substring(10);
        PEMParser parser = PEMParsers.ofStandard("PRIVATE KEY");

        // eol
        assertParseResult(parser.parse(concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Line3, "\r\n",
            "\r\n", // base64finl
            "-----END PRIVATE KEY-----"
        )), rawText);

        assertParseWithInvalidBase64Text(parser, 5, concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Line3, "\r\n",
            "\r\n", // base64finl
            "\r\n",
            "-----END PRIVATE KEY-----"
        ));

        // WSP
        assertParseResult(parser.parse(concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Line3, "   \r\n", // base64finl
            "-----END PRIVATE KEY-----"
        )), rawText);

        assertParseResult(parser.parse(concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Line3, "\r\n",
            "  \r\n", // base64finl
            "-----END PRIVATE KEY-----"
        )), rawText);

        // padding
        assertParseResult(parser.parse(concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Line3, "\r\n",
            "==\r\n", // base64finl
            "-----END PRIVATE KEY-----"
        )), rawText);

        assertParseResult(parser.parse(concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Line3,
            "=\r\n", // base64finl
            "=\r\n", // base64finl
            "-----END PRIVATE KEY-----"
        )), rawText);

        assertParseResult(parser.parse(concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Line3, "\r\n",
            "= \r\n", // base64finl
            "= \r\n", // base64finl
            "-----END PRIVATE KEY-----"
        )), rawText);

        assertParseWithInvalidBase64Text(parser, 3, concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Line3, "=a\r\n", // base64finl
            "\r\n",
            "-----END PRIVATE KEY-----"
        ));

        assertParseWithInvalidBase64Text(parser, 4, concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Line3, "==\r\n", // base64finl
            "\r\n",
            "-----END PRIVATE KEY-----"
        ));

        assertParseWithInvalidBase64Text(parser, 3, concat(
            "-----BEGIN PRIVATE KEY-----", "\r\n",
            base64Line1 + base64Line2, "\r\n",
            base64Line3, "===\r\n", // base64finl
            "-----END PRIVATE KEY-----"
        ));

    }

    @Test
    public void canConstructParserWithValidLabelCharacters() {

        // labelchar  = %x21-2C / %x2E-7E
        // ; any printable character, ; except hyphen-minus

        // label      = [ labelchar *( ["-" / SP] labelchar ) ]       ; empty ok

        String validCharacters = new String(
            new char[] {0x21, 0x2C, 0x2E, 0x7E}); // '!'(0x21), ','(0x2C), '.'(0x2E), '~'(0x7E)
        String space = new String(new char[] {0x20});
        String hyphen = new String(new char[] {0x2D});
        String delete = new String(new char[] {0x7F});

        // valid characters.
        PEMParsers.ofStandard(validCharacters);
        // invalid characters.
        assertParserConstruction(space);
        assertParserConstruction(hyphen);
        assertParserConstruction(delete);

        // valid formats.
        PEMParsers.ofStandard("");
        PEMParsers.ofStandard("A PRIVATE KEY X");
        PEMParsers.ofStandard("A-PRIVATE-KEY-X");

        // invalid formats.
        assertParserConstruction(" PRIVATE KEY");
        assertParserConstruction("PRIVATE KEY ");
        assertParserConstruction("-PRIVATE KEY");
        assertParserConstruction("PRIVATE KEY-");
        assertParserConstruction("PRIVATE  KEY");
        assertParserConstruction("PRIVATE--KEY");
        assertParserConstruction("PRIVATE- KEY");
        assertParserConstruction("PRIVATE -KEY");
    }

    private void assertParserConstruction(String label) {
        String expectedErrorMessageFmt = "The input label is invalid: %s";

        assertThat(assertThrows(IllegalArgumentException.class,
                () -> PEMParsers.ofStandard(label)).getMessage(),
            equalTo(String.format(expectedErrorMessageFmt, label)));
    }

    private void assertParseResult(byte[] bytes, String origText) {
        assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo(origText));
    }

    private String toBase64(String text) {
        return Base64.getEncoder()
            .encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private String toBase64WithoutPadding(String text) {
        return Base64.getEncoder().withoutPadding()
            .encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private String concat(String... s) {
        return Arrays.stream(s).collect(Collectors.joining());
    }
}
