using System.Runtime.CompilerServices;
using CreateDummyFiles.Class;
using CmnClsLib.Class;
using CmnClsLib.Module;

// 2026/08/15 Gemini 3.6 Flash (High) Review & Modified

[assembly: InternalsVisibleTo("TestProject1")]

namespace CreateDummyFiles
{
    /// <summary>
    /// ダミーファイル生成アプリケーションのエントリーポイントを提供するクラスです。
    /// </summary>
    internal class Program
    {
        /// <summary>
        /// アプリケーションのメインエントリーポイントです。
        /// </summary>
        /// <param name="args">コマンドライン引数の配列。</param>
        /// <returns>処理の終了コード（0: 正常終了、その他: エラーまたは警告レベル）。</returns>
        /// <example>
        /// 実行例:
        /// <code>
        /// CreateDummyFiles.exe -c 10 -o C:\Output
        /// </code>
        /// </example>
        internal static int Main(string[] args)
        {
            ClsLogger logger = new();
            ClsProp prop = new();
            ClsAppArg appArg = new(logger, prop);
            ClsMainProc mainProc = new(logger, prop);
            DateTime startTime = DateTime.Now;

            bool isOk = appArg.Parse(args);

            if (prop.Verbose > 0)
            {
                logger.WriteLine(MdlConst.LVL_NONE, $"===<<< [{prop.ExeBaseName}] START : {MdlDate.GetFormattedDate(startTime, "yyyy/MM/dd HH:mm:ss")}>>>===");
            }

            if (isOk && prop.UsageFlag == ClsProp.USAGE_NONE)
            {
                mainProc.Prop = appArg.Prop;
                prop.ReturnCode = mainProc.Execute();
            }
            else
            {
                switch (prop.UsageFlag)
                {
                    case ClsProp.USAGE_USAGE:
                        prop.ReturnCode = MdlConst.LVL_W;
                        appArg.ShowUsage();
                        break;
                    default:
                        prop.ReturnCode = MdlConst.LVL_E;
                        break;
                }
            }

            if (prop.Verbose > 0)
            {
                DateTime endTime = DateTime.Now;
                double elapsedTime = (endTime - startTime).TotalSeconds;
                logger.WriteLine(MdlConst.LVL_NONE, $"===<<< [{prop.ExeBaseName}] EXIT ({prop.ReturnCode}) : {MdlDate.GetFormattedDate(endTime, "yyyy/MM/dd HH:mm:ss")} : {elapsedTime:F3} sec>>>===");
            }

            return prop.ReturnCode;
        }
    }
}

