using System;
using AutoFixture;
using NUnit.Framework;
using RossyntBackend.Utils;

#nullable enable

namespace RossyntBackendUnitTest {
    public class StringExtensionTest {
        private readonly Fixture _fixture = new Fixture();

        // ******************************************************************************** //

        [TestCase("abc", 3, "abc")]
        [TestCase("abc", 2, "ab")]
        [TestCase("abc", 1, "a")]
        [TestCase("abc", 0, "")]
        [TestCase("ağc", 4, "ağc")]
        [TestCase("ağc", 3, "ağ")]
        [TestCase("ağc", 2, "a")]
        [TestCase("ağc", 1, "a")]
        [TestCase("ağc", 0, "")]
        [TestCase("ğ", 2, "ğ")]
        [TestCase("ğ", 1, "")]
        [TestCase("ğ", 0, "")]
        public void SurrogateSafeLeft(string inputString, int maxLength, string expectedResult) {
            Assert.AreEqual(expectedResult, inputString.SurrogateSafeLeft(maxLength));
        }

        [Test]
        public void SurrogateSafeLeft_ArgumentNullException() {
            const string? inputString = null;
            Assert.Catch<ArgumentNullException>(() => _ = inputString!.SurrogateSafeLeft(_fixture.Create<int>()));
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
