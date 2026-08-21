using System;
using System.IO;
using CreateDummyFiles;
using Xunit;

namespace TestProject1
{
    public class UnitTest_Program
    {
        private readonly string _tempDir;

        public UnitTest_Program()
        {
            _tempDir = Path.Combine(Path.GetTempPath(), "UnitTest", "CreateDummyFiles", "Program");
            if (!Directory.Exists(_tempDir))
            {
                Directory.CreateDirectory(_tempDir);
            }
        }

        [Fact]
        public void Main_WithHelpOption_ShouldReturnWarningCode()
        {
            string[] args = ["-h"];
            int exitCode = Program.Main(args);

            // USAGE表示時は MdlConst.LVL_W (10) が返されることを検証
            Assert.Equal(10, exitCode);
        }

        [Fact]
        public void Main_WithInvalidOption_ShouldReturnErrorCode()
        {
            string[] args = ["-d", ""];
            int exitCode = Program.Main(args);

            // 引数不正時はエラーコード (MdlConst.LVL_E = 20) が返されることを検証
            Assert.Equal(20, exitCode);
        }

        [Fact]
        public void Main_WithValidOptions_ShouldExecuteSuccessfully()
        {
            string outputDir = Path.Combine(_tempDir, "Output");
            if (Directory.Exists(outputDir))
            {
                Directory.Delete(outputDir, true);
            }

            string[] args = ["-d", outputDir, "-s", "1KB", "-f", "0", "-t", "0"];

            int exitCode = Program.Main(args);

            Assert.Equal(0, exitCode);
            Assert.True(Directory.Exists(outputDir));
        }
    }
}
