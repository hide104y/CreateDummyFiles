package tool;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tool.cmnclslib.mdl.MdlConst;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CreateDummyFiles} クラス（エントリーポイント）の単体テストです。
 */
class UnitTest_CreateDummyFiles {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("ヘルプオプション指定時に警告コード（10）が返されること")
    void main_WithHelpOption_ShouldReturnWarningCode() {
        String[] args = {"-h"};
        int exitCode = CreateDummyFiles.mainProcess(args);

        assertEquals(MdlConst.LVL_W, exitCode);
    }

    @Test
    @DisplayName("不正なオプション指定時にエラーコード（20）が返されること")
    void main_WithInvalidOption_ShouldReturnErrorCode() {
        String[] args = {"-d", ""};
        int exitCode = CreateDummyFiles.mainProcess(args);

        assertEquals(MdlConst.LVL_E, exitCode);
    }

    @Test
    @DisplayName("有効なオプション指定時に正常終了コード（0）が返されファイルが生成されること")
    void main_WithValidOptions_ShouldExecuteSuccessfully() {
        Path outputDir = tempDir.resolve("Output");
        String[] args = {"-d", outputDir.toString(), "-s", "1KB", "-f", "0", "-t", "0", "-v"};

        int exitCode = CreateDummyFiles.mainProcess(args);

        assertEquals(MdlConst.LVL_I, exitCode);
        assertTrue(Files.exists(outputDir));
    }

    @Test
    @DisplayName("nullの引数配列指定時にエラーコード（20）が返されること")
    void main_WithNullArgs_ShouldReturnErrorCode() {
        int exitCode = CreateDummyFiles.mainProcess(null);

        assertEquals(MdlConst.LVL_E, exitCode);
    }
}
