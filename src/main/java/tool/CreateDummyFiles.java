package tool;

import java.time.LocalDateTime;
import java.util.Locale;
import tool.cmnclslib.cls.ClsLogger;
import tool.cmnclslib.mdl.MdlConst;
import tool.cmnclslib.mdl.MdlDate;

/**
 * ダミーファイル生成アプリケーションのエントリーポイントを提供するクラスです。
 */
public final class CreateDummyFiles {

    private CreateDummyFiles() {
        // インスタンス化防止
    }

    /**
     * アプリケーションのメインエントリーポイントです。
     *
     * @param args コマンドライン引数の配列
     */
    public static void main(String[] args) {
        int returnCode = mainProcess(args);
        if (returnCode != MdlConst.LVL_I) {
            System.exit(returnCode);
        }
    }

    /**
     * コマンドライン引数を解析し、ダミーファイル生成処理を実行します。
     *
     * @param args コマンドライン引数の配列
     * @return 処理の終了コード（0: 正常終了, 10: 警告/ヘルプ表示, 20: エラー終了）
     */
    public static int mainProcess(String[] args) {
        var startNanoTime = System.nanoTime();
        var startTime = LocalDateTime.now();

        var logger = new ClsLogger();
        var prop = new ClsProp();
        var appArg = new ClsAppArg(logger, prop);
        var mainProc = new ClsMainProc(logger, prop);

        var isOk = appArg.parse(args != null ? args : new String[0]);

        if (prop.getVerbose() > 0) {
            logger.writeLine(MdlConst.LVL_NONE, "===<<< [" + prop.getExeBaseName() + "] START : "
                    + MdlDate.getFormattedDate(startTime, "yyyy/MM/dd HH:mm:ss") + ">>>===");
        }

        if (isOk && prop.getUsageFlag() == ClsProp.USAGE_NONE) {
            mainProc.setProp(appArg.getProp());
            prop.setReturnCode(mainProc.execute());
        } else {
            switch (prop.getUsageFlag()) {
                case ClsProp.USAGE_USAGE:
                    prop.setReturnCode(MdlConst.LVL_W);
                    appArg.showUsage();
                    break;
                default:
                    prop.setReturnCode(MdlConst.LVL_E);
                    break;
            }
        }

        if (prop.getVerbose() > 0) {
            var endTime = LocalDateTime.now();
            var elapsedTime = (System.nanoTime() - startNanoTime) / 1_000_000_000.0;
            logger.writeLine(MdlConst.LVL_NONE, String.format(Locale.ROOT,
                    "===<<< [%s] EXIT (%d) : %s : %.3f sec>>>===",
                    prop.getExeBaseName(),
                    prop.getReturnCode(),
                    MdlDate.getFormattedDate(endTime, "yyyy/MM/dd HH:mm:ss"),
                    elapsedTime));
        }

        return prop.getReturnCode();
    }
}
