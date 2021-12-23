using System;
using System.Collections.Generic;
using System.Linq;
using JetBrains.Annotations;

#nullable enable

namespace RossyntBackend.Utils {
    public static class ObjectUtil {
        [Pure]
        public static IReadOnlyDictionary<string, string> GetObjectProperties(object rawObject) {
            if (rawObject == null) throw new ArgumentNullException(nameof(rawObject));

            return rawObject.GetType().GetProperties().ToDictionary(propertyInfo => propertyInfo.Name, propertyInfo => $"{propertyInfo.GetValue(rawObject)}");
        }
    }
}
