using CreateDummyFiles.Class;
using CmnClsLib.Class;
using CmnClsLib.Module;
using System;
using System.IO;
using Xunit;

namespace TestProject1
{
    public class UnitTest_ClsMainProc : IDisposable
    {
        private readonly string _tempDir;
        private readonly ClsLogger _logger;

        public UnitTest_ClsMainProc()
        {
            _tempDir = Path.Combine(Path.GetTempPath(), "UnitTest", "CreateDummyFiles", "ClsMainProc");
            if (Directory.Exists(_tempDir))
            {
                Directory.Delete(_tempDir, true);
            }
            Directory.CreateDirectory(_tempDir);

            _logger = new ClsLogger();
        }

        public void Dispose()
        {
            if (Directory.Exists(_tempDir))
            {
                try
                {
                    Directory.Delete(_tempDir, true);
                }
                catch
                {
                    // テスト後のクリーンアップにおける例外の無視
                }
            }
        }

        [Fact]
        public void Constructor_ShouldInitializeProperties()
        {
            var prop = new ClsProp();
            var mainProc = new ClsMainProc(_logger, prop);

            Assert.NotNull(mainProc.Prop);
            Assert.Same(prop, mainProc.Prop);
        }

        [Fact]
        public void Execute_WhenOutputDirIsExistingFile_ShouldReturnErrorCode()
        {
            // 出力ディレクトリパスとして存在するファイルを準備
            string filePathAsDir = Path.Combine(_tempDir, "exist_file.txt");
            File.WriteAllText(filePathAsDir, "dummy");

            var prop = new ClsProp
            {
                OutputDir = filePathAsDir
            };

            var mainProc = new ClsMainProc(_logger, prop);
            int resultCode = mainProc.Execute();

            Assert.Equal(MdlConst.LVL_E, resultCode);
        }

        [Fact]
        public void Execute_WhenValidOutputDir_ShouldCreateFilesSuccessfully()
        {
            string outputSubDir = Path.Combine(_tempDir, "Output");

            var prop = new ClsProp
            {
                OutputDir = outputSubDir,
                DateFrom = 0,
                DateTo = 0,
                FilenameTemplate = "dummy_%Y%m%d.log",
                FileSize = 100
            };

            var mainProc = new ClsMainProc(_logger, prop);
            int resultCode = mainProc.Execute();

            Assert.Equal(MdlConst.LVL_I, resultCode);
            Assert.True(Directory.Exists(outputSubDir));

            string expectedFileName = $"dummy_{DateTime.Now:yyyyMMdd}.log";
            string expectedFilePath = Path.Combine(outputSubDir, expectedFileName);
            Assert.True(File.Exists(expectedFilePath));
        }
    }
}
