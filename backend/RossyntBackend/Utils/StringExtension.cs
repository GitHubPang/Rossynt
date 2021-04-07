using System;
using JetBrains.Annotations;

namespace RossyntBackend.Utils {
    public static class StringExtension {
        /// <summary>
        /// Get a substring from the left side of a string.
        ///
        /// <para/>
        ///
        /// If the substring results in half of a surrogate pair at the end, that half is truncated.
        /// For example,
        /// <c>"abc😀"</c> with <paramref name="maxLength"/> <c>4</c> would give <c>"abc"</c> instead of <c>"abc"</c> plus half a face (i.e. <c>"abc\ud83d"</c>).
        /// </summary>
        /// <remarks>
        /// This method assumes that the input string is a valid UTF-16 string.
        /// </remarks>
        [Pure]
        [NotNull]
        public static string SurrogateSafeLeft([NotNull] this string inputString, int maxLength) {
            if (inputString == null) throw new ArgumentNullException(nameof(inputString));
            if (maxLength < 0) throw new ArgumentOutOfRangeException(nameof(maxLength), "Length cannot be less than zero.");
            if (maxLength > inputString.Length) throw new ArgumentOutOfRangeException(nameof(maxLength), "Length cannot be more than the length of the string.");

            for (var length = maxLength; length > 0; length--) {
                if (!char.IsHighSurrogate(inputString[length - 1])) {
                    return inputString.Substring(0, length);
                }
            }

            return "";
        }
    }
}
