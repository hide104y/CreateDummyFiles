using System;
using System.Collections.Generic;
using System.IO;
using CmnClsLib.Class;
using CmnClsLib.Module;

// 2026/08/15 Gemini 3.6 Flash (High) Review & Modified

namespace CreateDummyFiles.Class
{
    /// <summary>
    /// アプリケーションのコマンドライン引数を解析し、各種プロパティを設定するクラスです。
    /// </summary>
    public class ClsAppArg
    {
        private ClsLogger _logger;
        private ClsProp _prop;
        private ClsCmmnArgs _cmmnArgs;

        /// <summary>
        /// <see cref="ClsAppArg"/> クラスの新しいインスタンスを初期化します。
        /// </summary>
        /// <param name="logger">ログ出力制御用オブジェクト</param>
        /// <param name="prop">アプリケーションプロパティ設定用オブジェクト</param>
        /// <example>
        /// <code>
        /// var logger = new ClsLogger();
        /// var prop = new ClsProp();
        /// var appArg = new ClsAppArg(logger, prop);
        /// </code>
        /// </example>
        public ClsAppArg(ClsLogger logger, ClsProp prop)
        {
            _logger = logger;
            _prop = prop;
            _cmmnArgs = new(_logger);
            _cmmnArgs.GetModuleInfo(System.Diagnostics.Process.GetCurrentProcess().MainModule?.FileName ?? string.Empty);
            _prop.ExeDir = _cmmnArgs.ExeDir;
            _prop.ExeBaseName = _cmmnArgs.ExeBaseName;
            _prop.Pid = _cmmnArgs.Pid;
        }

        /// <summary>
        /// アプリケーションの設定プロパティを取得または設定します。
        /// </summary>
        /// <example>
        /// <code>
        /// var currentProp = appArg.Prop;
        /// </code>
        /// </example>
        public ClsProp Prop { get => _prop; set => _prop = value; }

        /// <summary>
        /// コマンドライン引数を解析し、アプリケーションプロパティへ設定および検証を行います。
        /// </summary>
        /// <param name="args">コマンドライン引数の配列</param>
        /// <returns>すべての引数が正常に解析・検証された場合は <c>true</c>。無効または不足している引数がある場合は <c>false</c>。</returns>
        /// <example>
        /// <code>
        /// string[] args = ["-d", @"C:\Temp", "-s", "100MB"];
        /// bool isValid = appArg.Parse(args);
        /// </code>
        /// </example>
        public bool Parse(string[] args)
        {
            Dictionary<string, string> namedArguments = MdlArg.GetNamedArgs(args, false);
            _cmmnArgs.NamedArgs = namedArguments;
            bool isValid = _cmmnArgs.GetCommonArgs();
            string tempValue = string.Empty;

            // -----------------------------------------------------------------
            // ClsCmmnParams引数取得：ETC
            // -----------------------------------------------------------------
            _prop.UsageFlag = _cmmnArgs.IsUsage ? ClsProp.USAGE_USAGE : ClsProp.USAGE_NONE;
            _prop.Verbose = _cmmnArgs.Verbose;
            _prop.IsStackTrace = _cmmnArgs.IsStackTrace;

            // -----------------------------------------------------------------
            // Option：
            ReadOnlySpan<string[]> optionKeys =
            [
                ["d", "out"],
                ["f", "date-from"],
                ["t", "date-to"],
                ["n", "name"],
                ["s", "size"]
            ];

            foreach (string key in optionKeys[0])
            {
                if (MdlArg.ContainsKey(namedArguments, key))
                {
                    tempValue = MdlArg.GetValue(namedArguments, key);
                    if (!string.IsNullOrEmpty(tempValue))
                    {
                        _prop.OutputDir = tempValue;
                        break;
                    }
                }
            }

            foreach (string key in optionKeys[1])
            {
                if (MdlArg.ContainsKey(namedArguments, key))
                {
                    tempValue = MdlArg.GetValue(namedArguments, key);
                    if (!string.IsNullOrEmpty(tempValue))
                    {
                        int parsedInt = MdlUtil.ParseInt(tempValue, MdlConst.INT_NULL);
                        if (parsedInt != MdlConst.INT_NULL)
                        {
                            _prop.DateFrom = parsedInt;
                        }
                        break;
                    }
                }
            }

            foreach (string key in optionKeys[2])
            {
                if (MdlArg.ContainsKey(namedArguments, key))
                {
                    tempValue = MdlArg.GetValue(namedArguments, key);
                    if (!string.IsNullOrEmpty(tempValue))
                    {
                        int parsedInt = MdlUtil.ParseInt(tempValue, MdlConst.INT_NULL);
                        if (parsedInt != MdlConst.INT_NULL)
                        {
                            _prop.DateTo = parsedInt;
                        }
                        break;
                    }
                }
            }

            if (_prop.DateFrom > _prop.DateTo)
            {
                _logger.WriteLine(MdlConst.LVL_E, $"INVALID ARGUMENT : -f {_prop.DateFrom} < -t {_prop.DateTo}");
                isValid = false;
            }

            foreach (string key in optionKeys[3])
            {
                if (MdlArg.ContainsKey(namedArguments, key))
                {
                    tempValue = MdlArg.GetValue(namedArguments, key);
                    if (!string.IsNullOrEmpty(tempValue))
                    {
                        _prop.FilenameTemplate = tempValue;
                        break;
                    }
                }
            }

            foreach (string key in optionKeys[4])
            {
                if (MdlArg.ContainsKey(namedArguments, key))
                {
                    tempValue = MdlArg.GetValue(namedArguments, key);
                    if (!string.IsNullOrEmpty(tempValue))
                    {
                        _prop.HumanReadableFileSize = tempValue;
                        break;
                    }
                }
            }

            if (string.IsNullOrEmpty(_prop.OutputDir))
            {
                _logger.WriteLine(MdlConst.LVL_E, $"INVALID ARGUMENT : -d is empty : {_prop.OutputDir}");
                isValid = false;
            }

            try
            {
                _prop.FileSize = _prop.GetFileSize(_prop.HumanReadableFileSize);
            }
            catch (Exception ex)
            {
                _logger.WriteLine(MdlConst.LVL_E, $"INVALID ARGUMENT : -size {_prop.HumanReadableFileSize} ⇒ {ex.Message}");
                isValid = false;
            }

            // -----------------------------------------------------------------
            // 掃除
            // -----------------------------------------------------------------
            namedArguments.Clear();

            return isValid;
        }

        /// <summary>
        /// コマンドラインの各種オプションおよび使用方法（ヘルプメッセージ）をログに出力します。
        /// </summary>
        /// <example>
        /// <code>
        /// appArg.ShowUsage();
        /// </code>
        /// </example>
        public void ShowUsage()
        {
            string dateFromFormatted = MdlDate.GetFormattedDate(_prop.GetDateTimeFromOffset(_prop.DateFrom), "yyyy/MM/dd HH:mm:ss");
            string dateToFormatted = MdlDate.GetFormattedDate(_prop.GetDateTimeFromOffset(_prop.DateTo), "yyyy/MM/dd HH:mm:ss");

            _logger.WriteLine(MdlConst.LVL_NONE, string.Empty);
            _logger.WriteLine(MdlConst.LVL_NONE, $"Usage : {_prop.ExeDir}{Path.DirectorySeparatorChar}{_prop.ExeBaseName}.exe [Option] [Option]...");
            _logger.WriteLine(MdlConst.LVL_NONE, string.Empty);
            _logger.WriteLine(MdlConst.LVL_NONE, "Basic Option：");
            _logger.WriteLine(MdlConst.LVL_NONE, $"   -d|-out path        ：出力先ディレクトリ     （現在値={_prop.OutputDir}");
            _logger.WriteLine(MdlConst.LVL_NONE, $"   -f|-date-from +|-num：何日前からかの日数     （現在値={_prop.DateFrom} ⇒ {dateFromFormatted}");
            _logger.WriteLine(MdlConst.LVL_NONE, $"   -t|-date-to +|-num  ：何日後までかの日数     （現在値={_prop.DateTo} ⇒ {dateToFormatted}");
            _logger.WriteLine(MdlConst.LVL_NONE, $"   -nl-name name       ：ファイル名テンプレート （現在値={_prop.FilenameTemplate}");
            _logger.WriteLine(MdlConst.LVL_NONE, $"   -s|-size size       ：1-1024B|KB|MB|GB       （現在値={_prop.HumanReadableFileSize} ⇒ {_prop.FileSize}Byte");
            _logger.WriteLine(MdlConst.LVL_NONE, string.Empty);
            _logger.WriteLine(MdlConst.LVL_NONE, "Other Option：");
            _logger.WriteLine(MdlConst.LVL_NONE, $"   -v | -vv            ：冗長表示                          （現在値={_prop.Verbose}）");
            _logger.WriteLine(MdlConst.LVL_NONE, $"   -stacktrace         ：例外時STACKTRACE表示フラグ        （現在値={_prop.IsStackTrace}）");
            _logger.WriteLine(MdlConst.LVL_NONE, "   -console mode       ：メッセージ表示 off|stdout|stderr");
            _logger.WriteLine(MdlConst.LVL_NONE, string.Empty);
            _logger.WriteLine(MdlConst.LVL_NONE, "※ファイル名テンプレート書式指定子：%Y、%y、%m、%d、%H、%M、%S、%w、_COMPUTERNAME_、_USERNAME_");
            _logger.WriteLine(MdlConst.LVL_NONE, string.Empty);
            _logger.WriteLine(MdlConst.LVL_NONE, $"Return Code : {MdlConst.LVL_I}:SUCCESS / {MdlConst.LVL_E}:ERROR");
            _logger.WriteLine(MdlConst.LVL_NONE, string.Empty);
        }
    }
}

