package tool;

import java.io.BufferedWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;
import tool.cmnclslib.cls.ClsLogger;
import tool.cmnclslib.mdl.MdlConst;
import tool.cmnclslib.mdl.MdlFile;

/**
 * ダミーファイルの生成処理を実行するクラスです。
 */
public class ClsMainProc {

    private static final DateTimeFormatter FMT_YYYY = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter FMT_YY = DateTimeFormatter.ofPattern("yy");
    private static final DateTimeFormatter FMT_MM = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter FMT_DD = DateTimeFormatter.ofPattern("dd");
    private static final DateTimeFormatter FMT_HH = DateTimeFormatter.ofPattern("HH");
    private static final DateTimeFormatter FMT_NN = DateTimeFormatter.ofPattern("mm");
    private static final DateTimeFormatter FMT_SS = DateTimeFormatter.ofPattern("ss");

    private static final int BUFFER_SIZE = 1024;

    private final ClsLogger logger;
    private ClsProp prop;

    /**
     * {@link ClsMainProc} クラスの新しいインスタンスを初期化します。
     *
     * @param logger ログ出力オブジェクト
     * @param prop   アプリケーション設定プロパティ
     */
    public ClsMainProc(ClsLogger logger, ClsProp prop) {
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
        this.prop = Objects.requireNonNull(prop, "prop must not be null");
    }

    /**
     * アプリケーション設定プロパティを取得します。
     *
     * @return アプリケーション設定プロパティ
     */
    public ClsProp getProp() {
        return prop;
    }

    /**
     * アプリケーション設定プロパティを設定します。
     *
     * @param prop アプリケーション設定プロパティ
     */
    public void setProp(ClsProp prop) {
        this.prop = Objects.requireNonNull(prop, "prop must not be null");
    }

    /**
     * ダミーファイルの生成処理を実行します。
     *
     * @return 処理結果コード（{@link MdlConst#LVL_I}: 正常終了, その他: エラーコード）
     */
    public int execute() {
        // 出力ディレクトリが存在しない場合は作成
        switch (MdlFile.getPathType(prop.getOutputDir())) {
            case MdlFile.PATH_IS_DIRECTORY:
                break;
            case MdlFile.PATH_IS_FILE:
                logger.writeLine(MdlConst.LVL_E, "Invalid Argument -d is exist file : " + prop.getOutputDir());
                prop.setReturnCode(MdlConst.LVL_E);
                break;
            default:
                if (MdlFile.createDirectory(prop.getOutputDir()) > MdlFile.OK_MKDIR_HANTEI) {
                    logger.writeLine(MdlConst.LVL_E, "Failed to create directory : " + prop.getOutputDir());
                    prop.setReturnCode(MdlConst.LVL_E);
                }
                break;
        }

        if (prop.getReturnCode() != MdlConst.LVL_I) {
            return prop.getReturnCode();
        }

        // 指定された日数を元に開始・終了日を取得
        var startDate = prop.getDateTimeFromOffset(prop.getDateFrom());
        var endDate = prop.getDateTimeFromOffset(prop.getDateTo());

        var computerName = getMachineName();
        var userName = System.getProperty("user.name", "");
        var random = new Random();

        for (var date = startDate; !date.toLocalDate().isAfter(endDate.toLocalDate()); date = date.plusDays(1)) {
            try {
                var fileName = buildFileName(date, computerName, userName);
                var filePath = Path.of(prop.getOutputDir(), fileName);

                try (var writer = Files.newBufferedWriter(
                        filePath,
                        StandardCharsets.US_ASCII,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE)) {

                    long loopMax;
                    int lastSize;
                    if (prop.getFileSize() > BUFFER_SIZE) {
                        loopMax = prop.getFileSize() / BUFFER_SIZE;
                        lastSize = (int) (prop.getFileSize() % BUFFER_SIZE);
                    } else {
                        loopMax = 0;
                        lastSize = (int) prop.getFileSize();
                    }

                    for (long i = 0; i < loopMax; i++) {
                        writer.write(prop.getRandomString(random, BUFFER_SIZE));
                        writer.newLine();
                    }
                    if (lastSize > 0) {
                        writer.write(prop.getRandomString(random, lastSize));
                        writer.newLine();
                    }
                    writer.flush();
                }

                logger.writeLine(MdlConst.LVL_NONE, "Created " + filePath);
            } catch (Exception ex) {
                logger.writeLine(MdlConst.LVL_E, "Failed to create file : " + ex.getMessage());
                prop.setReturnCode(MdlConst.LVL_E);
                break;
            }
        }

        return prop.getReturnCode();
    }

    /**
     * 日時および環境情報を反映したファイル名を生成します。
     *
     * @param date         対象日時
     * @param computerName マシン名
     * @param userName     ユーザー名
     * @return 置換後のファイル名文字列
     */
    private String buildFileName(LocalDateTime date, String computerName, String userName) {
        return prop.getFilenameTemplate()
                .replace("%Y", date.format(FMT_YYYY))
                .replace("%y", date.format(FMT_YY))
                .replace("%m", date.format(FMT_MM))
                .replace("%d", date.format(FMT_DD))
                .replace("%H", date.format(FMT_HH))
                .replace("%M", date.format(FMT_NN))
                .replace("%S", date.format(FMT_SS))
                .replace("%w", String.valueOf(date.getDayOfWeek().getValue() % 7))
                .replace("%pid", String.valueOf(prop.getPid()))
                .replace("_COMPUTERNAME_", computerName)
                .replace("_USERNAME_", userName);
    }

    /**
     * マシン名（ホスト名）を取得します（Windows, Linux, macOS クロスプラットフォーム対応）。
     *
     * @return マシン名文字列
     */
    private static String getMachineName() {
        String machine = System.getenv("COMPUTERNAME");
        if (machine == null || machine.isBlank()) {
            machine = System.getenv("HOSTNAME");
        }
        if (machine == null || machine.isBlank()) {
            machine = System.getenv("HOST");
        }
        if (machine == null || machine.isBlank()) {
            try {
                machine = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException | SecurityException e) {
                machine = "localhost";
            }
        }
        return (machine != null && !machine.isBlank()) ? machine : "localhost";
    }
}
