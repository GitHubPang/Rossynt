using System;
using System.Collections.Generic;
using System.Linq;
using JetBrains.Annotations;

namespace RossyntBackend.Utils {
    public static class ObjectUtil {
        [Pure]
        [NotNull]
        public static IReadOnlyDictionary<string, string> GetObjectProperties([NotNull] object rawObject) {
            if (rawObject == null) throw new ArgumentNullException(nameof(rawObject));

            return rawObject.GetType().GetProperties().ToDictionary(propertyInfo => propertyInfo.Name, propertyInfo => $"{propertyInfo.GetValue(rawObject)}");
        }
    }
}
