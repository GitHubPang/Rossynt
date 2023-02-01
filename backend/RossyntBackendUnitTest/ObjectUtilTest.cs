using System;
using System.Collections.Generic;
using NUnit.Framework;
using RossyntBackend.Utils;

namespace RossyntBackendUnitTest {
    public class ObjectUtilTest {
        private sealed class Foo {
            // ReSharper disable UnusedMember.Local MemberCanBePrivate.Local UnusedAutoPropertyAccessor.Local ReplaceAutoPropertyWithComputedProperty
            public string PublicProperty { get; } = "Apple";
            public string PublicComputedProperty => "Backs";
            public int IntegerProperty { get; }
            private string PrivateProperty { get; } = "Missing";
            // ReSharper restore UnusedMember.Local MemberCanBePrivate.Local UnusedAutoPropertyAccessor.Local ReplaceAutoPropertyWithComputedProperty

            // ******************************************************************************** //

            public Foo() {
                IntegerProperty = -42;
            }
        }

        // ******************************************************************************** //

        [Test]
        public void GetObjectProperties_ArgumentNullException() {
            const object? rawObject = null;
            Assert.Catch<ArgumentNullException>(() => _ = ObjectUtil.GetObjectProperties(rawObject!));
        }

        [Test]
        public void GetObjectProperties() {
            var expectedDict = new Dictionary<string, string> {
                ["PublicProperty"] = "Apple",
                ["PublicComputedProperty"] = "Backs",
                ["IntegerProperty"] = "-42",
            };
            CollectionAssert.AreEquivalent(expectedDict, ObjectUtil.GetObjectProperties(new Foo()));
        }
    }
}
