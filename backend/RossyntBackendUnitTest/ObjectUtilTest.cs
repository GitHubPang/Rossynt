using System;
using System.Collections.Generic;
using NUnit.Framework;
using NUnit.Framework.Legacy;
using RossyntBackend.Utils;

namespace RossyntBackendUnitTest;

public class ObjectUtilTest {
    private sealed class Foo {
#pragma warning disable CA1822
        // ReSharper disable UnusedMember.Local MemberCanBePrivate.Local UnusedAutoPropertyAccessor.Local ReplaceAutoPropertyWithComputedProperty
        public string PublicProperty { get; } = "Apple";
        public string PublicComputedProperty => "Backs";
        public int IntegerProperty { get; }
        private string PrivateProperty { get; } = "Missing";
        // ReSharper restore UnusedMember.Local MemberCanBePrivate.Local UnusedAutoPropertyAccessor.Local ReplaceAutoPropertyWithComputedProperty
#pragma warning restore CA1822

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
            ["IntegerProperty"] = "-42"
        };
        CollectionAssert.AreEquivalent(expectedDict, ObjectUtil.GetObjectProperties(new Foo()));
    }
}
