using CmnClsLib.Class;
using CmnClsLib.Module;
using System;
using System.Text;
using System.Text.RegularExpressions;

// 2026/08/15 Gemini 3.6 Flash (High) Review & Modified

namespace CreateDummyFiles.Class
{
    /// <summary>
    /// ダミーファイル生成処理の設定プロパティおよび関連ユーティリティ機能を提供するクラスです。
    /// </summary>
    /// <example>
    /// <code>
    /// var prop = new ClsProp();
    /// prop.OutputDir = @"C:\temp";
    /// ulong bytes = prop.GetFileSize("10MB");
    /// </code>
    /// </example>
    public partial class ClsProp
    {
        /// <summary>使用方法未指定の定数（0）</summary>
        public const int USAGE_NONE = 0;
        /// <summary>使用方法表示の定数（1）</summary>
        public const int USAGE_USAGE = 1;
        /// <summary>サンプル設定ファイル表示の定数（2）</summary>
        public const int USAGE_SHOW_SAMPLE_CONFIG = 2;

        private static readonly char[] RandomChars = @"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&'()=~;:*+-_/<>".ToCharArray();

        [GeneratedRegex(@"^(?<num>\d+)\s*(?<unit>B|K|KB|M|MB|G|GB)?$", RegexOptions.IgnoreCase)]
        private static partial Regex FileSizeRegex();

        /// <summary>
        /// <see cref="ClsProp"/> クラスの新しいインスタンスを初期化します。
        /// </summary>
        /// <example>
        /// <code>
        /// var prop = new ClsProp();
        /// </code>
        /// </example>
        public ClsProp()
        {
        }

        /// <summary>
        /// 実行ファイルのディレクトリパスを取得または設定します。
        /// </summary>
        public string ExeDir { get; set; } = "";

        /// <summary>
        /// 実行ファイルのベース名（拡張子付きファイル名）を取得または設定します。
        /// </summary>
        public string ExeBaseName { get; set; } = "";

        /// <summary>
        /// プロセスIDを取得または設定します。
        /// </summary>
        public int Pid { get; set; } = 0;

        /// <summary>
        /// ログの出力詳細レベルを取得または設定します。
        /// </summary>
        public int Verbose { get; set; } = 0;

        /// <summary>
        /// 処理の戻り値コードを取得または設定します。
        /// </summary>
        public int ReturnCode { get; set; } = MdlConst.LVL_I;

        /// <summary>
        /// ヘルプ等の使用方法表示フラグを取得または設定します。
        /// </summary>
        public int UsageFlag { get; set; } = 0;

        /// <summary>
        /// エラー発生時にスタックトレースを出力するかどうかを示す値を取得または設定します。
        /// </summary>
        public bool IsStackTrace { get; set; } = false;

        /// <summary>
        /// ダミーファイルの出力先ディレクトリパスを取得または設定します。
        /// </summary>
        public string OutputDir { get; set; } = "";

        /// <summary>
        /// 生成基準日からの開始オフセット日数（デフォルト: -7日）を取得または設定します。
        /// </summary>
        public int DateFrom { get; set; } = -7;

        /// <summary>
        /// 生成基準日からの終了オフセット日数（デフォルト: 0日）を取得または設定します。
        /// </summary>
        public int DateTo { get; set; } = 0;

        /// <summary>
        /// 生成するダミーファイルのファイル名テンプレートを取得または設定します。
        /// </summary>
        public string FilenameTemplate { get; set; } = "dummy_%Y%m%d.%H%M%S.log";

        /// <summary>
        /// 人間が読みやすい形式のファイルサイズ文字列（例: "10KB"）を取得または設定します。
        /// </summary>
        public string HumanReadableFileSize { get; set; } = "10KB";

        /// <summary>
        /// バイト単位のファイルサイズを取得または設定します。
        /// </summary>
        public ulong FileSize { get; set; } = 0;

        /// <summary>
        /// "10KB", "5MB", "1GB" などの人間が読みやすい表記の文字列をバイト数値 (ulong) に変換します。
        /// </summary>
        /// <param name="humanReadableFileSize">変換対象のファイルサイズ文字列（例: "100B", "10KB", "2MB", "1GB"）</param>
        /// <returns>バイト単位を表す64ビット無符号整数（ulong）</returns>
        /// <exception cref="ArgumentException">文字列の長さが超過している場合や、単位・数値フォーマットが不正な場合に発生します。</exception>
        /// <example>
        /// <code>
        /// var prop = new ClsProp();
        /// ulong bytes = prop.GetFileSize("10KB"); // 10240
        /// </code>
        /// </example>
        public ulong GetFileSize(string humanReadableFileSize)
        {
            if (humanReadableFileSize.Length > (1024 * 1024 + 2))
            {
                throw new ArgumentException("fileSizeの長さが長すぎます。");
            }

            Match match = FileSizeRegex().Match(humanReadableFileSize);
            if (!match.Success)
            {
                throw new ArgumentException("fileSizeの形式が不正です。例: 10, 10B, 10KB, 10MB, 10GB");
            }

            int num = int.Parse(match.Groups["num"].ValueSpan);
            ReadOnlySpan<char> unit = match.Groups["unit"].ValueSpan;

            if (unit.Equals("", StringComparison.OrdinalIgnoreCase) || unit.Equals("B", StringComparison.OrdinalIgnoreCase))
            {
                return (ulong)num;
            }
            else if (unit.Equals("K", StringComparison.OrdinalIgnoreCase) || unit.Equals("KB", StringComparison.OrdinalIgnoreCase))
            {
                return (ulong)num * 1024UL;
            }
            else if (unit.Equals("M", StringComparison.OrdinalIgnoreCase) || unit.Equals("MB", StringComparison.OrdinalIgnoreCase))
            {
                return (ulong)num * 1024UL * 1024UL;
            }
            else if (unit.Equals("G", StringComparison.OrdinalIgnoreCase) || unit.Equals("GB", StringComparison.OrdinalIgnoreCase))
            {
                return (ulong)num * 1024UL * 1024UL * 1024UL;
            }
            else
            {
                throw new ArgumentException("不正な単位が指定されています。使用可能な単位はB, K, KB, M, MB, G, GBです。");
            }
        }

        /// <summary>
        /// 現在日時から指定した日数だけオフセットした <see cref="DateTime"/> オブジェクトを取得します。
        /// </summary>
        /// <param name="days">現在日時からの相対日数（過去の場合は負数、未来の場合は正数）</param>
        /// <returns>計算されたオフセット日時</returns>
        /// <example>
        /// <code>
        /// var prop = new ClsProp();
        /// DateTime pastDate = prop.GetDateTimeFromOffset(-7);
        /// </code>
        /// </example>
        public DateTime GetDateTimeFromOffset(int days)
        {
            return DateTime.Now.AddDays(days);
        }

        /// <summary>
        /// 指定された長さのランダムな英数字および記号からなる文字列を生成します。
        /// </summary>
        /// <param name="random">乱数ジェネレーターのインスタンス（nullの場合は <see cref="Random.Shared"/> を使用）</param>
        /// <param name="length">生成する文字列の文字数</param>
        /// <returns>生成されたランダム文字列</returns>
        /// <example>
        /// <code>
        /// var prop = new ClsProp();
        /// string randomStr = prop.GetRandomString(null, 16);
        /// </code>
        /// </example>
        public string GetRandomString(Random? random, int length)
        {
            random ??= Random.Shared;
            return string.Create(length, random, (span, rnd) =>
            {
                rnd.GetItems(RandomChars, span);
            });
        }
    }
}

