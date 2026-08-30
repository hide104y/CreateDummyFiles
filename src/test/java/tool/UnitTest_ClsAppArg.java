package tool;

import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tool.cmnclslib.cls.ClsLogger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ClsAppArg} クラスの単体テストです。
 */
class UnitTest_ClsAppArg {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("コンストラクタでプロパティが正しく初期化されること")
    void constructor_ShouldInitializeProperties() {
        ClsLogger logger = new ClsLogger();
        ClsProp prop = new ClsProp();

        ClsAppArg appArg = new ClsAppArg(logger, prop);

        assertSame(prop, appArg.getProp());
    }

    @Test
    @DisplayName("有効な引数が正常に解析されプロパティに設定されること")
    void parse_ValidArguments_ShouldReturnTrueAndSetProperties() {
        ClsLogger logger = new ClsLogger();
        ClsProp prop = new ClsProp();
        ClsAppArg appArg = new ClsAppArg(logger, prop);

        String outputDir = tempDir.resolve("Output").toString();
        String[] args = {
                "-d", outputDir,
                "-f", "-5",
                "-t", "2",
                "-n", "dummy_%Y%m%d.log",
                "-s", "5MB"
        };

        boolean result = appArg.parse(args);

        assertTrue(result);
        assertEquals(outputDir, prop.getOutputDir());
        assertEquals(-5, prop.getDateFrom());
        assertEquals(2, prop.getDateTo());
        assertEquals("dummy_%Y%m%d.log", prop.getFilenameTemplate());
        assertEquals("5MB", prop.getHumanReadableFileSize());
        assertEquals(5242880L, prop.getFileSize());
    }

    @Test
    @DisplayName("出力先ディレクトリ引数が不足している場合にfalseを返すこと")
    void parse_MissingOutputDir_ShouldReturnFalse() {
        ClsLogger logger = new ClsLogger();
        ClsProp prop = new ClsProp();
        ClsAppArg appArg = new ClsAppArg(logger, prop);

        String[] args = {"-f", "-5", "-s", "1KB"};

        boolean result = appArg.parse(args);

        assertFalse(result);
    }

    @Test
    @DisplayName("開始日数が終了日数より大きい場合にfalseを返すこと")
    void parse_DateFromGreaterThanDateTo_ShouldReturnFalse() {
        ClsLogger logger = new ClsLogger();
        ClsProp prop = new ClsProp();
        ClsAppArg appArg = new ClsAppArg(logger, prop);

        String outputDir = tempDir.resolve("Output").toString();
        String[] args = {
                "-d", outputDir,
                "-f", "10",
                "-t", "5"
        };

        boolean result = appArg.parse(args);

        assertFalse(result);
    }

    @Test
    @DisplayName("不正なファイルサイズ指定の場合にfalseを返すこと")
    void parse_InvalidSizeFormat_ShouldReturnFalse() {
        ClsLogger logger = new ClsLogger();
        ClsProp prop = new ClsProp();
        ClsAppArg appArg = new ClsAppArg(logger, prop);

        String outputDir = tempDir.resolve("Output").toString();
        String[] args = {
                "-d", outputDir,
                "-s", "INVALID_SIZE"
        };

        boolean result = appArg.parse(args);

        assertFalse(result);
    }

    @Test
    @DisplayName("エイリアス引数（-out, -date-from, -date-to, -name, -size）が正常に解析されること")
    void parse_AliasArguments_ShouldReturnTrueAndSetProperties() {
        ClsLogger logger = new ClsLogger();
        ClsProp prop = new ClsProp();
        ClsAppArg appArg = new ClsAppArg(logger, prop);

        String outputDir = tempDir.resolve("AliasOutput").toString();
        String[] args = {
                "-out", outputDir,
                "-date-from", "-3",
                "-date-to", "1",
                "-name", "alias_%Y%m%d.log",
                "-size", "1MB",
                "-v",
                "-stacktrace"
        };

        boolean result = appArg.parse(args);

        assertTrue(result);
        assertEquals(outputDir, prop.getOutputDir());
        assertEquals(-3, prop.getDateFrom());
        assertEquals(1, prop.getDateTo());
        assertEquals("alias_%Y%m%d.log", prop.getFilenameTemplate());
        assertEquals("1MB", prop.getHumanReadableFileSize());
        assertEquals(1048576L, prop.getFileSize());
        assertEquals(1, prop.getVerbose());
        assertTrue(prop.isStackTrace());
    }

    @Test
    @DisplayName("nullのargs配列でNullPointerExceptionがスローされること")
    void parse_NullArgs_ShouldThrowNullPointerException() {
        ClsLogger logger = new ClsLogger();
        ClsProp prop = new ClsProp();
        ClsAppArg appArg = new ClsAppArg(logger, prop);

        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> appArg.parse(null));
    }

    @Test
    @DisplayName("setPropで新しいプロパティが設定できること")
    void setProp_ShouldUpdateProperty() {
        ClsLogger logger = new ClsLogger();
        ClsProp prop1 = new ClsProp();
        ClsProp prop2 = new ClsProp();
        ClsAppArg appArg = new ClsAppArg(logger, prop1);

        appArg.setProp(prop2);
        assertSame(prop2, appArg.getProp());
    }

    @Test
    @DisplayName("showUsageが例外なく実行されること")
    void showUsage_ShouldExecuteWithoutException() {
        ClsLogger logger = new ClsLogger();
        ClsProp prop = new ClsProp();
        ClsAppArg appArg = new ClsAppArg(logger, prop);

        assertDoesNotThrow(appArg::showUsage);
    }
}
