package tool;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ClsProp} クラスの単体テストです。
 */
class UnitTest_ClsProp {

    @Test
    @DisplayName("デフォルトプロパティが正しく初期化されること")
    void defaultProperties_ShouldInitializeCorrectly() {
        ClsProp prop = new ClsProp();

        assertEquals("", prop.getExeDir());
        assertEquals("", prop.getExeBaseName());
        assertEquals(0L, prop.getPid());
        assertEquals(0, prop.getVerbose());
        assertEquals(0, prop.getUsageFlag());
        assertFalse(prop.isStackTrace());
        assertEquals("", prop.getOutputDir());
        assertEquals(-7, prop.getDateFrom());
        assertEquals(0, prop.getDateTo());
        assertEquals("dummy_%Y%m%d.%H%M%S.log", prop.getFilenameTemplate());
        assertEquals("10KB", prop.getHumanReadableFileSize());
        assertEquals(0L, prop.getFileSize());
    }

    @ParameterizedTest
    @CsvSource({
            "10, 10",
            "10B, 10",
            "10b, 10",
            "'10 K', 10240",
            "10KB, 10240",
            "10kb, 10240",
            "'2 MB', 2097152",
            "1GB, 1073741824"
    })
    @DisplayName("有効なファイルサイズ表記が正しいバイト数に変換されること")
    void parseFileSize_ValidInput_ShouldReturnCorrectBytes(String input, long expected) {
        ClsProp prop = new ClsProp();
        long result = prop.parseFileSize(input);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "10TB", "-10KB"})
    @DisplayName("無効なファイルサイズ表記でIllegalArgumentExceptionがスローされること")
    void parseFileSize_InvalidInput_ShouldThrowArgumentException(String input) {
        ClsProp prop = new ClsProp();
        assertThrows(IllegalArgumentException.class, () -> prop.parseFileSize(input));
    }

    @Test
    @DisplayName("長すぎるファイルサイズ文字列でIllegalArgumentExceptionがスローされること")
    void parseFileSize_TooLongString_ShouldThrowArgumentException() {
        ClsProp prop = new ClsProp();
        String tooLongInput = "1".repeat(1024 * 1024 + 3);
        assertThrows(IllegalArgumentException.class, () -> prop.parseFileSize(tooLongInput));
    }

    @Test
    @DisplayName("nullのファイルサイズ文字列でNullPointerExceptionがスローされること")
    void parseFileSize_NullInput_ShouldThrowNullPointerException() {
        ClsProp prop = new ClsProp();
        assertThrows(NullPointerException.class, () -> prop.parseFileSize(null));
    }

    @Test
    @DisplayName("オフセット日数から現在日時基準の日時が正しく計算されること")
    void getDateTimeFromOffset_ShouldReturnOffsetDateTime() {
        ClsProp prop = new ClsProp();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime targetDate = prop.getDateTimeFromOffset(-7);
        assertTrue(Duration.between(targetDate, now.minusDays(7)).abs().getSeconds() < 2);
    }

    @Test
    @DisplayName("指定された長さのランダム文字列が生成されること")
    void getRandomString_ShouldReturnStringWithSpecifiedLength() {
        ClsProp prop = new ClsProp();

        String random1 = prop.getRandomString(null, 16);
        assertEquals(16, random1.length());

        Random customRnd = new Random(12345);
        String random2 = prop.getRandomString(customRnd, 32);
        assertEquals(32, random2.length());

        assertEquals("", prop.getRandomString(null, 0));
        assertEquals("", prop.getRandomString(null, -5));
    }

    @Test
    @DisplayName("各プロパティのSetterとGetterが正常に動作すること")
    void propertyAccessors_ShouldSetAndGetValues() {
        ClsProp prop = new ClsProp();

        prop.setExeDir("C:\\bin");
        assertEquals("C:\\bin", prop.getExeDir());

        prop.setExeBaseName("App.jar");
        assertEquals("App.jar", prop.getExeBaseName());

        prop.setPid(12345L);
        assertEquals(12345L, prop.getPid());

        prop.setVerbose(2);
        assertEquals(2, prop.getVerbose());

        prop.setReturnCode(10);
        assertEquals(10, prop.getReturnCode());

        prop.setUsageFlag(ClsProp.USAGE_USAGE);
        assertEquals(ClsProp.USAGE_USAGE, prop.getUsageFlag());

        prop.setStackTrace(true);
        assertTrue(prop.isStackTrace());

        prop.setOutputDir("C:\\output");
        assertEquals("C:\\output", prop.getOutputDir());

        prop.setDateFrom(-3);
        assertEquals(-3, prop.getDateFrom());

        prop.setDateTo(3);
        assertEquals(3, prop.getDateTo());

        prop.setFilenameTemplate("custom_%Y%m%d.txt");
        assertEquals("custom_%Y%m%d.txt", prop.getFilenameTemplate());

        prop.setHumanReadableFileSize("50MB");
        assertEquals("50MB", prop.getHumanReadableFileSize());

        prop.setFileSize(52428800L);
        assertEquals(52428800L, prop.getFileSize());
    }
}
