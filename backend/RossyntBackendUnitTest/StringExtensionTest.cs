using System;
using System.Web;
using AutoFixture;
using JetBrains.Annotations;
using NUnit.Framework;
using RossyntBackend.Utils;

namespace RossyntBackendUnitTest {
    public class StringExtensionTest {
        private readonly Fixture _fixture = new Fixture();

        // ******************************************************************************** //

        private static TestCaseData[] _testCases = {
            CreateTestCase("abc", 3, "abc"),
            CreateTestCase("abc", 2, "ab"),
            CreateTestCase("abc", 1, "a"),
            CreateTestCase("abc", 0, ""),
            CreateTestCase("a😀c", 4, "a😀c"),
            CreateTestCase("a😀c", 3, "a😀"),
            CreateTestCase("a😀c", 2, "a"),
            CreateTestCase("a😀c", 1, "a"),
            CreateTestCase("a😀c", 0, ""),
            CreateTestCase("😀", 2, "😀"),
            CreateTestCase("😀", 1, ""),
            CreateTestCase("😀", 0, ""),
        };

        private static TestCaseData CreateTestCase([NotNull] string inputString, int maxLength, [NotNull] string expectedResult) {
            // Encode, to workaround issue in ReSharper/Rider that test case result is inconclusive if test case name contains emoji.
            // https://youtrack.jetbrains.com/issue/RSRP-483831
            var testCaseName = $"({HttpUtility.UrlEncode(inputString)}, {maxLength}) => {HttpUtility.UrlEncode(expectedResult)}";
            return new TestCaseData(inputString, maxLength, expectedResult).SetName(testCaseName);
        }

        [TestCaseSource(nameof(_testCases))]
        public void SurrogateSafeLeft([NotNull] string inputString, int maxLength, [NotNull] string expectedResult) {
            Assert.AreEqual(expectedResult, inputString.SurrogateSafeLeft(maxLength));
        }

        [Test]
        public void SurrogateSafeLeft_ArgumentNullException() {
            const string inputString = null;
            // ReSharper disable once AssignNullToNotNullAttribute
            Assert.Catch<ArgumentNullException>(() => _ = inputString.SurrogateSafeLeft(_fixture.Create<int>()));
        }

        [Test]
        public void SurrogateSafeLeft_ArgumentException_Length_TooSmall() {
            var inputString = _fixture.Create<string>();
            Assert.Catch<ArgumentException>(() => _ = inputString.SurrogateSafeLeft(-1));
        }

        [Test]
        public void SurrogateSafeLeft_ArgumentException_Length_TooLarge() {
            var inputString = _fixture.Create<string>();
            Assert.Catch<ArgumentException>(() => _ = inputString.SurrogateSafeLeft(inputString.Length + 1));
        }
    }
}
