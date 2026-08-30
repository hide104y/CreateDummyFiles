package tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tool.cmnclslib.cls.ClsLogger;
import tool.cmnclslib.mdl.MdlConst;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ClsMainProc} クラスの単体テストです。
 */
class UnitTest_ClsMainProc {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("コンストラクタでプロパティが正しく初期化されること")
    void constructor_ShouldInitializeProperties() {
        ClsLogger logger = new ClsLogger();
        ClsProp prop = new ClsProp();
        ClsMainProc mainProc = new ClsMainProc(logger, prop);

        assertNotNull(mainProc.getProp());
        assertSame(prop, mainProc.getProp());
    }

    @Test
    @DisplayName("出力先ディレクトリに既存ファイルが指定された場合エラーコードを返すこと")
    void execute_WhenOutputDirIsExistingFile_ShouldReturnErrorCode() throws IOException {
        ClsLogger logger = new ClsLogger();
        Path filePathAsDir = tempDir.resolve("exist_file.txt");
        Files.writeString(filePathAsDir, "dummy");

        ClsProp prop = new ClsProp();
        prop.setOutputDir(filePathAsDir.toString());

        ClsMainProc mainProc = new ClsMainProc(logger, prop);
        int resultCode = mainProc.execute();

        assertEquals(MdlConst.LVL_E, resultCode);
    }

    @Test
    @DisplayName("複数日の範囲指定でダミーファイルが複数生成されること")
    void execute_WhenMultiDayRange_ShouldCreateMultipleFiles() {
        ClsLogger logger = new ClsLogger();
        Path outputSubDir = tempDir.resolve("MultiDayOutput");

        ClsProp prop = new ClsProp();
        prop.setOutputDir(outputSubDir.toString());
        prop.setDateFrom(-2);
        prop.setDateTo(0);
        prop.setFilenameTemplate("log_%Y%m%d.txt");
        prop.setFileSize(50L);

        ClsMainProc mainProc = new ClsMainProc(logger, prop);
        int resultCode = mainProc.execute();

        assertEquals(MdlConst.LVL_I, resultCode);
        assertTrue(Files.exists(outputSubDir));

        // -2, -1, 0 の 3ファイルが生成されていることを確認
        for (int i = -2; i <= 0; i++) {
            LocalDateTime dt = LocalDateTime.now().plusDays(i);
            String name = "log_" + dt.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
            assertTrue(Files.exists(outputSubDir.resolve(name)));
        }
    }

    @Test
    @DisplayName("バッファサイズを超えるファイルサイズでファイルが正常に生成されること")
    void execute_WhenLargeFileSize_ShouldCreateFileWithCorrectContent() throws IOException {
        ClsLogger logger = new ClsLogger();
        Path outputSubDir = tempDir.resolve("LargeFileOutput");

        ClsProp prop = new ClsProp();
        prop.setOutputDir(outputSubDir.toString());
        prop.setDateFrom(0);
        prop.setDateTo(0);
        prop.setFilenameTemplate("large_%Y%m%d_%pid.log");
        prop.setPid(9999L);
        prop.setFileSize(2500L); // 1024バイト超

        ClsMainProc mainProc = new ClsMainProc(logger, prop);
        int resultCode = mainProc.execute();

        assertEquals(MdlConst.LVL_I, resultCode);
        String expectedFileName = "large_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_9999.log";
        Path expectedFile = outputSubDir.resolve(expectedFileName);
        assertTrue(Files.exists(expectedFile));
        assertTrue(Files.size(expectedFile) > 2500L); // ランダム文字列＋改行コード
    }

    @Test
    @DisplayName("setPropで新しいプロパティが設定できること")
    void setProp_ShouldUpdateProperty() {
        ClsLogger logger = new ClsLogger();
        ClsProp prop1 = new ClsProp();
        ClsProp prop2 = new ClsProp();
        ClsMainProc mainProc = new ClsMainProc(logger, prop1);

        mainProc.setProp(prop2);
        assertSame(prop2, mainProc.getProp());
    }
}
