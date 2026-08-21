using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using CmnClsLib.Class;
using CmnClsLib.Module;

// 2026/08/15 Gemini 3.6 Flash (High) Review & Modified

namespace CreateDummyFiles.Class
{
    public class ClsMainProc
    {
        private readonly ClsLogger _logger;
        private ClsProp _prop;

        /// <summary>
        /// ClsMainProc クラスの新しいインスタンスを初期化します。
        /// </summary>
        /// <param name="logger">ログ出力オブジェクト</param>
        /// <param name="prop">アプリケーション設定プロパティ</param>
        /// <example>
        /// <code>
        /// var logger = new ClsLogger();
        /// var prop = new ClsProp();
        /// var mainProc = new ClsMainProc(logger, prop);
        /// </code>
        /// </example>
        public ClsMainProc(ClsLogger logger, ClsProp prop)
        {
            _logger = logger;
            _prop = prop;
        }

        /// <summary>
        /// アプリケーション設定プロパティを取得または設定します。
        /// </summary>
        /// <example>
        /// <code>
        /// var prop = mainProc.Prop;
        /// </code>
        /// </example>
        public ClsProp Prop { get { return _prop; } set { _prop = value; } }

        /// <summary>
        /// ダミーファイルの生成処理を実行します。
        /// </summary>
        /// <returns>処理結果コード（MdlConst.LVL_I: 正常終了, その他: エラーコード）</returns>
        /// <example>
        /// <code>
        /// int resultCode = mainProc.Execute();
        /// if (resultCode != MdlConst.LVL_I)
        /// {
        ///     // エラー処理
        /// }
        /// </code>
        /// </example>
        public int Execute()
        {
            // 出力ディレクトリが存在しない場合は作成
            switch (MdlFile.GetPathType(_prop.OutputDir))
            {
                // ディレクトリは既に存在している
                case MdlFile.PATH_IS_DIRECTORY:
                    break;
                // ファイルが存在している
                case MdlFile.PATH_IS_FILE:
                    _logger.WriteLine(MdlConst.LVL_E, "Invalid Argument -d is exist file : " + _prop.OutputDir);
                    _prop.ReturnCode = MdlConst.LVL_E;
                    break;
                // ディレクトリが存在しない
                default:
                    // ディレクトリ作成
                    if (MdlFile.CreateDirectory(_prop.OutputDir) > MdlFile.OK_MKDIR_HANTEI)
                    {
                        _logger.WriteLine(MdlConst.LVL_E, "Failed to create directory : " + _prop.OutputDir);
                        _prop.ReturnCode = MdlConst.LVL_E;
                    }
                    break;
            }
            if (_prop.ReturnCode != MdlConst.LVL_I)
            {
                return _prop.ReturnCode;
            }

            // 指定された日数を元に開始・終了日を取得
            DateTime startDate = _prop.GetDateTimeFromOffset(_prop.DateFrom);
            DateTime endDate = _prop.GetDateTimeFromOffset(_prop.DateTo);

            Random random = new();
            random.Next((int)MdlDate.GetUnixTime());

            // startDateからendDateまでの日付でファイル作成
            for (DateTime date = startDate; date <= endDate; date = date.AddDays(1))
            {
                try
                {
                    // プレースホルダの置換
                    string fileName = _prop.FilenameTemplate
                        .Replace("%Y", date.ToString("yyyy"))
                        .Replace("%y", date.ToString("yy"))
                        .Replace("%m", date.ToString("MM"))
                        .Replace("%d", date.ToString("dd"))
                        .Replace("%H", date.ToString("HH"))
                        .Replace("%M", date.ToString("mm"))
                        .Replace("%S", date.ToString("ss"))
                        .Replace("%w", ((int)date.DayOfWeek).ToString())
                        .Replace("%pid", _prop.Pid.ToString())
                        .Replace("_COMPUTERNAME_", Environment.MachineName)
                        .Replace("_USERNAME_", Environment.UserName);

                    string filePath = Path.Combine(_prop.OutputDir, fileName);
                    bool isAppend = false;

                    using (StreamWriter writer = new(filePath, isAppend, Encoding.ASCII))
                    {
                        ulong loopMax = 0;
                        int lastSize = 0;
                        int bufferSize = 1024;
                        if (_prop.FileSize > (ulong)bufferSize)
                        {
                            loopMax = _prop.FileSize / (ulong)bufferSize;
                            lastSize = (int)(_prop.FileSize % (ulong)bufferSize);
                        }
                        else
                        {
                            lastSize = (int)_prop.FileSize;
                        }
                        for (ulong i = 0; i < loopMax; i++)
                        {
                            // 指定バイト数のダミー内容（ランダムな文字列生成）
                            writer.WriteLine(_prop.GetRandomString(random, bufferSize));
                        }
                        if (lastSize > 0) writer.WriteLine(_prop.GetRandomString(random, lastSize));
                        writer.Flush();
                    }
                    _logger.WriteLine(MdlConst.LVL_NONE, "Created " + filePath);
                }
                catch (Exception ex)
                {
                    _logger.WriteLine(MdlConst.LVL_E, "Failed to create file : " + ex.Message);
                    _prop.ReturnCode = MdlConst.LVL_E;
                    break;
                }
            }
            return _prop.ReturnCode;
        }

    }
}

