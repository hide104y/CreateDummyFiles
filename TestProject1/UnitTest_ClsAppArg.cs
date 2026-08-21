using System;
using System.IO;
using CmnClsLib.Class;
using CreateDummyFiles.Class;
using Xunit;

namespace TestProject1
{
    public class UnitTest_ClsAppArg
    {
        private readonly string _tempDir;

        public UnitTest_ClsAppArg()
        {
            _tempDir = Path.Combine(Path.GetTempPath(), "UnitTest", "CreateDummyFiles", "ClsAppArg");
            if (!Directory.Exists(_tempDir))
            {
                Directory.CreateDirectory(_tempDir);
            }
        }

        [Fact]
        public void Constructor_ShouldInitializeProperties()
        {
            var logger = new ClsLogger();
            var prop = new ClsProp();

            var appArg = new ClsAppArg(logger, prop);

            Assert.Same(prop, appArg.Prop);
        }

        [Fact]
        public void Parse_ValidArguments_ShouldReturnTrueAndSetProperties()
        {
            var logger = new ClsLogger();
            var prop = new ClsProp();
            var appArg = new ClsAppArg(logger, prop);

            string outputDir = Path.Combine(_tempDir, "Output");
            string[] args = [
                "-d", outputDir,
                "-f", "-5",
                "-t", "2",
                "-n", "dummy_%Y%m%d.log",
                "-s", "5MB"
            ];

            bool result = appArg.Parse(args);

            Assert.True(result);
            Assert.Equal(outputDir, prop.OutputDir);
            Assert.Equal(-5, prop.DateFrom);
            Assert.Equal(2, prop.DateTo);
            Assert.Equal("dummy_%Y%m%d.log", prop.FilenameTemplate);
            Assert.Equal("5MB", prop.HumanReadableFileSize);
            Assert.Equal(5242880UL, prop.FileSize);
        }

        [Fact]
        public void Parse_MissingOutputDir_ShouldReturnFalse()
        {
            var logger = new ClsLogger();
            var prop = new ClsProp();
            var appArg = new ClsAppArg(logger, prop);

            string[] args = ["-f", "-5", "-s", "1KB"];

            bool result = appArg.Parse(args);

            Assert.False(result);
        }

        [Fact]
        public void Parse_DateFromGreaterThanDateTo_ShouldReturnFalse()
        {
            var logger = new ClsLogger();
            var prop = new ClsProp();
            var appArg = new ClsAppArg(logger, prop);

            string outputDir = Path.Combine(_tempDir, "Output");
            string[] args = [
                "-d", outputDir,
                "-f", "10",
                "-t", "5"
            ];

            bool result = appArg.Parse(args);

            Assert.False(result);
        }

        [Fact]
        public void Parse_InvalidSizeFormat_ShouldReturnFalse()
        {
            var logger = new ClsLogger();
            var prop = new ClsProp();
            var appArg = new ClsAppArg(logger, prop);

            string outputDir = Path.Combine(_tempDir, "Output");
            string[] args = [
                "-d", outputDir,
                "-s", "INVALID_SIZE"
            ];

            bool result = appArg.Parse(args);

            Assert.False(result);
        }

        [Fact]
        public void ShowUsage_ShouldExecuteWithoutException()
        {
            var logger = new ClsLogger();
            var prop = new ClsProp();
            var appArg = new ClsAppArg(logger, prop);

            appArg.ShowUsage();
        }
    }
}
