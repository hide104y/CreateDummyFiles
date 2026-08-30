package tool;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tool.cmnclslib.mdl.MdlConst;

/**
 * ダミーファイル生成処理の設定プロパティおよび関連ユーティリティ機能を提供するクラスです。
 */
public class ClsProp {

    /** 使用方法未指定の定数（0） */
    public static final int USAGE_NONE = 0;
    /** 使用方法表示の定数（1） */
    public static final int USAGE_USAGE = 1;
    /** サンプル設定ファイル表示の定数（2） */
    public static final int USAGE_SHOW_SAMPLE_CONFIG = 2;

    private static final char[] RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&'()=~;:*+-_/<>".toCharArray();
    private static final Pattern FILE_SIZE_PATTERN = Pattern.compile("^(?<num>\\d+)\\s*(?<unit>B|K|KB|M|MB|G|GB)?$", Pattern.CASE_INSENSITIVE);

    private String exeDir = "";
    private String exeBaseName = "";
    private long pid = 0;
    private int verbose = 0;
    private int returnCode = MdlConst.LVL_I;
    private int usageFlag = 0;
    private boolean stackTrace = false;
    private String outputDir = "";
    private int dateFrom = -7;
    private int dateTo = 0;
    private String filenameTemplate = "dummy_%Y%m%d.%H%M%S.log";
    private String humanReadableFileSize = "10KB";
    private long fileSize = 0L;

    /**
     * {@link ClsProp} クラスの新しいインスタンスを初期化します。
     */
    public ClsProp() {
    }

    /**
     * 実行ファイルのディレクトリパスを取得します。
     *
     * @return 実行ファイルディレクトリパス
     */
    public String getExeDir() {
        return exeDir;
    }

    /**
     * 実行ファイルのディレクトリパスを設定します。
     *
     * @param exeDir 実行ファイルディレクトリパス
     */
    public void setExeDir(String exeDir) {
        this.exeDir = exeDir != null ? exeDir : "";
    }

    /**
     * 実行ファイルのベース名を取得します。
     *
     * @return 実行ファイルベース名
     */
    public String getExeBaseName() {
        return exeBaseName;
    }

    /**
     * 実行ファイルのベース名を設定します。
     *
     * @param exeBaseName 実行ファイルベース名
     */
    public void setExeBaseName(String exeBaseName) {
        this.exeBaseName = exeBaseName != null ? exeBaseName : "";
    }

    /**
     * プロセスIDを取得します。
     *
     * @return プロセスID
     */
    public long getPid() {
        return pid;
    }

    /**
     * プロセスIDを設定します。
     *
     * @param pid プロセスID
     */
    public void setPid(long pid) {
        this.pid = pid;
    }

    /**
     * ログ出力の冗長度レベルを取得します。
     *
     * @return 冗長度レベル
     */
    public int getVerbose() {
        return verbose;
    }

    /**
     * ログ出力の冗長度レベルを設定します。
     *
     * @param verbose 冗長度レベル
     */
    public void setVerbose(int verbose) {
        this.verbose = verbose;
    }

    /**
     * 処理の戻り値コードを取得します。
     *
     * @return 戻り値コード
     */
    public int getReturnCode() {
        return returnCode;
    }

    /**
     * 処理の戻り値コードを設定します。
     *
     * @param returnCode 戻り値コード
     */
    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    /**
     * 使用方法表示フラグを取得します。
     *
     * @return 使用方法フラグ
     */
    public int getUsageFlag() {
        return usageFlag;
    }

    /**
     * 使用方法表示フラグを設定します。
     *
     * @param usageFlag 使用方法フラグ
     */
    public void setUsageFlag(int usageFlag) {
        this.usageFlag = usageFlag;
    }

    /**
     * スタックトレース表示フラグを取得します。
     *
     * @return スタックトレース表示フラグ
     */
    public boolean isStackTrace() {
        return stackTrace;
    }

    /**
     * スタックトレース表示フラグを設定します。
     *
     * @param stackTrace スタックトレース表示フラグ
     */
    public void setStackTrace(boolean stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * ダミーファイルの出力先ディレクトリパスを取得します。
     *
     * @return 出力先ディレクトリパス
     */
    public String getOutputDir() {
        return outputDir;
    }

    /**
     * ダミーファイルの出力先ディレクトリパスを設定します。
     *
     * @param outputDir 出力先ディレクトリパス
     */
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir != null ? outputDir : "";
    }

    /**
     * 生成基準日からの開始オフセット日数を取得します。
     *
     * @return 開始オフセット日数
     */
    public int getDateFrom() {
        return dateFrom;
    }

    /**
     * 生成基準日からの開始オフセット日数を設定します。
     *
     * @param dateFrom 開始オフセット日数
     */
    public void setDateFrom(int dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * 生成基準日からの終了オフセット日数を取得します。
     *
     * @return 終了オフセット日数
     */
    public int getDateTo() {
        return dateTo;
    }

    /**
     * 生成基準日からの終了オフセット日数を設定します。
     *
     * @param dateTo 終了オフセット日数
     */
    public void setDateTo(int dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * 生成するダミーファイルのファイル名テンプレートを取得します。
     *
     * @return ファイル名テンプレート
     */
    public String getFilenameTemplate() {
        return filenameTemplate;
    }

    /**
     * 生成するダミーファイルのファイル名テンプレートを設定します。
     *
     * @param filenameTemplate ファイル名テンプレート
     */
    public void setFilenameTemplate(String filenameTemplate) {
        this.filenameTemplate = filenameTemplate != null ? filenameTemplate : "dummy_%Y%m%d.%H%M%S.log";
    }

    /**
     * 人間が読みやすい形式のファイルサイズ文字列（例: "10KB"）を取得します。
     *
     * @return ファイルサイズ文字列
     */
    public String getHumanReadableFileSize() {
        return humanReadableFileSize;
    }

    /**
     * 人間が読みやすい形式のファイルサイズ文字列（例: "10KB"）を設定します。
     *
     * @param humanReadableFileSize ファイルサイズ文字列
     */
    public void setHumanReadableFileSize(String humanReadableFileSize) {
        this.humanReadableFileSize = humanReadableFileSize != null ? humanReadableFileSize : "10KB";
    }

    /**
     * バイト単位のファイルサイズを取得します。
     *
     * @return バイト単位のファイルサイズ
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * バイト単位のファイルサイズを設定します。
     *
     * @param fileSize バイト単位のファイルサイズ
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * "10KB", "5MB", "1GB" などの人間が読みやすい表記の文字列をバイト数値（long）に変換します。
     *
     * @param humanReadableSize 変換対象のファイルサイズ文字列（例: "100B", "10KB", "2MB", "1GB"）
     * @return バイト単位を表す整数値（long）
     * @throws NullPointerException     humanReadableSize が null の場合
     * @throws IllegalArgumentException 文字列長超過、または数値・単位フォーマットが不正な場合
     */
    public long parseFileSize(String humanReadableSize) {
        Objects.requireNonNull(humanReadableSize, "humanReadableSize must not be null");

        if (humanReadableSize.length() > (1024 * 1024 + 2)) {
            throw new IllegalArgumentException("fileSizeの長さが長すぎます。");
        }

        var matcher = FILE_SIZE_PATTERN.matcher(humanReadableSize);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("fileSizeの形式が不正です。例: 10, 10B, 10KB, 10MB, 10GB");
        }

        var numStr = matcher.group("num");
        var unit = matcher.group("unit");
        if (unit == null) {
            unit = "";
        }

        long num;
        try {
            num = Long.parseLong(numStr);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("fileSizeの数値部分が不正です: " + numStr, ex);
        }

        if (unit.isBlank() || "B".equalsIgnoreCase(unit)) {
            return num;
        } else if ("K".equalsIgnoreCase(unit) || "KB".equalsIgnoreCase(unit)) {
            return num * 1024L;
        } else if ("M".equalsIgnoreCase(unit) || "MB".equalsIgnoreCase(unit)) {
            return num * 1024L * 1024L;
        } else if ("G".equalsIgnoreCase(unit) || "GB".equalsIgnoreCase(unit)) {
            return num * 1024L * 1024L * 1024L;
        } else {
            throw new IllegalArgumentException("不正な単位が指定されています。使用可能な単位はB, K, KB, M, MB, G, GBです。");
        }
    }

    /**
     * 現在日時から指定した日数だけオフセットした {@link LocalDateTime} オブジェクトを取得します。
     *
     * @param days 現在日時からの相対日数（過去の場合は負数、未来の場合は正数）
     * @return 計算されたオフセット日時
     */
    public LocalDateTime getDateTimeFromOffset(int days) {
        return LocalDateTime.now().plusDays(days);
    }

    /**
     * 指定された長さのランダムな英数字および記号からなる文字列を生成します。
     *
     * @param random 乱数ジェネレーター（nullの場合は {@link ThreadLocalRandom#current()} を使用）
     * @param length 生成する文字列の文字数
     * @return 生成されたランダム文字列
     */
    public String getRandomString(Random random, int length) {
        if (length <= 0) {
            return "";
        }
        var rnd = (random != null) ? random : ThreadLocalRandom.current();
        var buffer = new char[length];
        for (var i = 0; i < length; i++) {
            buffer[i] = RANDOM_CHARS[rnd.nextInt(RANDOM_CHARS.length)];
        }
        return new String(buffer);
    }
}
