package tool;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import tool.cmnclslib.cls.ClsCmmnArgs;
import tool.cmnclslib.cls.ClsLogger;
import tool.cmnclslib.mdl.MdlArg;
import tool.cmnclslib.mdl.MdlConst;
import tool.cmnclslib.mdl.MdlDate;
import tool.cmnclslib.mdl.MdlUtil;

/**
 * アプリケーションのコマンドライン引数を解析し、各種プロパティを設定するクラスです。
 */
public class ClsAppArg {

    private static final List<String> OUT_KEYS = List.of("d", "out");
    private static final List<String> FROM_KEYS = List.of("f", "date-from");
    private static final List<String> TO_KEYS = List.of("t", "date-to");
    private static final List<String> NAME_KEYS = List.of("n", "name");
    private static final List<String> SIZE_KEYS = List.of("s", "size");

    private final ClsLogger logger;
    private ClsProp prop;
    private final ClsCmmnArgs cmmnArgs;

    /**
     * {@link ClsAppArg} クラスの新しいインスタンスを初期化します。
     *
     * @param logger ログ出力制御用オブジェクト
     * @param prop   アプリケーションプロパティ設定用オブジェクト
     */
    public ClsAppArg(ClsLogger logger, ClsProp prop) {
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
        this.prop = Objects.requireNonNull(prop, "prop must not be null");
        this.cmmnArgs = new ClsCmmnArgs(this.logger);
        this.cmmnArgs.getModuleInfo();
        this.prop.setExeDir(this.cmmnArgs.getExeDir());
        this.prop.setExeBaseName(this.cmmnArgs.getExeBaseName());
        this.prop.setPid(this.cmmnArgs.getPid());
    }

    /**
     * アプリケーションの設定プロパティを取得します。
     *
     * @return アプリケーション設定プロパティ
     */
    public ClsProp getProp() {
        return prop;
    }

    /**
     * アプリケーションの設定プロパティを設定します。
     *
     * @param prop アプリケーション設定プロパティ
     */
    public void setProp(ClsProp prop) {
        this.prop = Objects.requireNonNull(prop, "prop must not be null");
    }

    /**
     * コマンドライン引数を解析し、アプリケーションプロパティへ設定および検証を行います。
     *
     * @param args コマンドライン引数の配列
     * @return すべての引数が正常に解析・検証された場合は {@code true}、無効または不足している引数がある場合は {@code false}
     */
    public boolean parse(String[] args) {
        Objects.requireNonNull(args, "args must not be null");

        var namedArguments = MdlArg.getNamedArgs(args, false);
        cmmnArgs.setNamedArgs(namedArguments);
        var isValid = cmmnArgs.getCommonArgs();

        // -----------------------------------------------------------------
        // ClsCmmnArgs引数取得：ETC
        // -----------------------------------------------------------------
        prop.setUsageFlag(cmmnArgs.isUsage() ? ClsProp.USAGE_USAGE : ClsProp.USAGE_NONE);
        prop.setVerbose(cmmnArgs.getVerbose());
        prop.setStackTrace(cmmnArgs.isStackTrace());

        // -----------------------------------------------------------------
        // 各オプションの抽出と設定
        // -----------------------------------------------------------------
        findFirstValue(namedArguments, OUT_KEYS).ifPresent(prop::setOutputDir);

        findFirstValue(namedArguments, FROM_KEYS).ifPresent(val -> {
            var parsed = MdlUtil.parseInt(val, MdlConst.INT_NULL);
            if (parsed != MdlConst.INT_NULL) {
                prop.setDateFrom(parsed);
            }
        });

        findFirstValue(namedArguments, TO_KEYS).ifPresent(val -> {
            var parsed = MdlUtil.parseInt(val, MdlConst.INT_NULL);
            if (parsed != MdlConst.INT_NULL) {
                prop.setDateTo(parsed);
            }
        });

        if (prop.getDateFrom() > prop.getDateTo()) {
            logger.writeLine(MdlConst.LVL_E, "INVALID ARGUMENT : -f " + prop.getDateFrom() + " < -t " + prop.getDateTo());
            isValid = false;
        }

        findFirstValue(namedArguments, NAME_KEYS).ifPresent(prop::setFilenameTemplate);

        findFirstValue(namedArguments, SIZE_KEYS).ifPresent(prop::setHumanReadableFileSize);

        if (prop.getOutputDir().isBlank()) {
            if (!cmmnArgs.isUsage()) {
                logger.writeLine(MdlConst.LVL_E, "INVALID ARGUMENT : -d is empty : " + prop.getOutputDir());
            }
            isValid = false;
        }

        try {
            prop.setFileSize(prop.parseFileSize(prop.getHumanReadableFileSize()));
        } catch (IllegalArgumentException ex) {
            logger.writeLine(MdlConst.LVL_E, "INVALID ARGUMENT : -size " + prop.getHumanReadableFileSize() + " \u21D2 " + ex.getMessage());
            isValid = false;
        }

        return isValid;
    }

    /**
     * 指定されたキーリストのうち最初に見つかった非空の値を返します。
     *
     * @param namedArguments 名前付き引数マップ
     * @param keys           検索対象のキーリスト
     * @return 最初に見つかった値を含む {@link Optional}、存在しない場合は {@link Optional#empty()}
     */
    private static Optional<String> findFirstValue(Map<String, String> namedArguments, List<String> keys) {
        for (var key : keys) {
            if (MdlArg.containsKey(namedArguments, key)) {
                var val = MdlArg.getValue(namedArguments, key);
                if (val != null && !val.isBlank()) {
                    return Optional.of(val);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * コマンドラインの各種オプションおよび使用方法（ヘルプメッセージ）をログに出力します。
     */
    public void showUsage() {
        LocalDateTime dateFromTime = prop.getDateTimeFromOffset(prop.getDateFrom());
        LocalDateTime dateToTime = prop.getDateTimeFromOffset(prop.getDateTo());
        String dateFromFormatted = MdlDate.getFormattedDate(dateFromTime, "yyyy/MM/dd HH:mm:ss");
        String dateToFormatted = MdlDate.getFormattedDate(dateToTime, "yyyy/MM/dd HH:mm:ss");

        String progName = prop.getExeBaseName();
        String exeCommand;
        if (progName == null || progName.isBlank() || progName.contains("apiguardian") || progName.contains("surefire")) {
            exeCommand = "java -jar CreateDummyFiles.jar";
        } else if (progName.endsWith(".jar")) {
            exeCommand = "java -jar " + progName;
        } else {
            exeCommand = progName;
        }

        logger.writeLine(MdlConst.LVL_NONE, "");
        logger.writeLine(MdlConst.LVL_NONE, "Usage : " + exeCommand + " [Option] [Option]...");
        logger.writeLine(MdlConst.LVL_NONE, "");
        logger.writeLine(MdlConst.LVL_NONE, "Basic Option：");
        logger.writeLine(MdlConst.LVL_NONE, "   -d|-out path        ：出力先ディレクトリ     （現在値=" + prop.getOutputDir() + "）");
        logger.writeLine(MdlConst.LVL_NONE, "   -f|-date-from +|-num：何日前からかの日数     （現在値=" + prop.getDateFrom() + " \u21D2 " + dateFromFormatted + "）");
        logger.writeLine(MdlConst.LVL_NONE, "   -t|-date-to +|-num  ：何日後までかの日数     （現在値=" + prop.getDateTo() + " \u21D2 " + dateToFormatted + "）");
        logger.writeLine(MdlConst.LVL_NONE, "   -n|-name name       ：ファイル名テンプレート （現在値=" + prop.getFilenameTemplate() + "）");
        logger.writeLine(MdlConst.LVL_NONE, "   -s|-size size       ：1-1024B|KB|MB|GB       （現在値=" + prop.getHumanReadableFileSize() + " \u21D2 " + prop.getFileSize() + "Byte）");
        logger.writeLine(MdlConst.LVL_NONE, "");
        logger.writeLine(MdlConst.LVL_NONE, "Other Option：");
        logger.writeLine(MdlConst.LVL_NONE, "   -v | -vv            ：冗長表示                          （現在値=" + prop.getVerbose() + "）");
        logger.writeLine(MdlConst.LVL_NONE, "   -stacktrace         ：例外時STACKTRACE表示フラグ        （現在値=" + prop.isStackTrace() + "）");
        logger.writeLine(MdlConst.LVL_NONE, "   -console mode       ：メッセージ表示 off|stdout|stderr");
        logger.writeLine(MdlConst.LVL_NONE, "");
        logger.writeLine(MdlConst.LVL_NONE, "※ファイル名テンプレート書式指定子：%Y、%y、%m、%d、%H、%M、%S、%w、_COMPUTERNAME_、_USERNAME_");
        logger.writeLine(MdlConst.LVL_NONE, "");
        logger.writeLine(MdlConst.LVL_NONE, "Return Code : " + MdlConst.LVL_I + ":SUCCESS / " + MdlConst.LVL_E + ":ERROR");
        logger.writeLine(MdlConst.LVL_NONE, "");
    }
}
