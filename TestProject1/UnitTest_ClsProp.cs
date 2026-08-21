using CreateDummyFiles.Class;
using System;
using System.IO;
using Xunit;

namespace TestProject1
{
    public class UnitTest_ClsProp
    {
        private readonly string _tempDir;

        public UnitTest_ClsProp()
        {
            _tempDir = Path.Combine(Path.GetTempPath(), "UnitTest", "CreateDummyFiles", "ClsProp");
            if (!Directory.Exists(_tempDir))
            {
                Directory.CreateDirectory(_tempDir);
            }
        }

        [Fact]
        public void DefaultProperties_ShouldInitializeCorrectly()
        {
            var prop = new ClsProp();

            Assert.Equal("", prop.ExeDir);
            Assert.Equal("", prop.ExeBaseName);
            Assert.Equal(0, prop.Pid);
            Assert.Equal(0, prop.Verbose);
            Assert.Equal(0, prop.UsageFlag);
            Assert.False(prop.IsStackTrace);
            Assert.Equal("", prop.OutputDir);
            Assert.Equal(-7, prop.DateFrom);
            Assert.Equal(0, prop.DateTo);
            Assert.Equal("dummy_%Y%m%d.%H%M%S.log", prop.FilenameTemplate);
            Assert.Equal("10KB", prop.HumanReadableFileSize);
            Assert.Equal(0UL, prop.FileSize);
        }

        [Theory]
        [InlineData("10", 10UL)]
        [InlineData("10B", 10UL)]
        [InlineData("10b", 10UL)]
        [InlineData("10 K", 10240UL)]
        [InlineData("10KB", 10240UL)]
        [InlineData("10kb", 10240UL)]
        [InlineData("2 MB", 2097152UL)]
        [InlineData("1GB", 1073741824UL)]
        public void GetFileSize_ValidInput_ShouldReturnCorrectBytes(string input, ulong expected)
        {
            var prop = new ClsProp();
            ulong result = prop.GetFileSize(input);
            Assert.Equal(expected, result);
        }

        [Theory]
        [InlineData("invalid")]
        [InlineData("10TB")]
        [InlineData("-10KB")]
        public void GetFileSize_InvalidInput_ShouldThrowArgumentException(string input)
        {
            var prop = new ClsProp();
            Assert.Throws<ArgumentException>(() => prop.GetFileSize(input));
        }

        [Fact]
        public void GetFileSize_TooLongString_ShouldThrowArgumentException()
        {
            var prop = new ClsProp();
            string tooLongInput = new string('1', 1024 * 1024 + 3);
            Assert.Throws<ArgumentException>(() => prop.GetFileSize(tooLongInput));
        }

        [Fact]
        public void GetDateTimeFromOffset_ShouldReturnOffsetDateTime()
        {
            var prop = new ClsProp();
            DateTime now = DateTime.Now;

            DateTime targetDate = prop.GetDateTimeFromOffset(-7);
            Assert.True((now.AddDays(-7) - targetDate).Duration() < TimeSpan.FromSeconds(2));
        }

        [Fact]
        public void GetRandomString_ShouldReturnStringWithSpecifiedLength()
        {
            var prop = new ClsProp();

            string random1 = prop.GetRandomString(null, 16);
            Assert.Equal(16, random1.Length);

            var customRnd = new Random(12345);
            string random2 = prop.GetRandomString(customRnd, 32);
            Assert.Equal(32, random2.Length);
        }
    }
}
