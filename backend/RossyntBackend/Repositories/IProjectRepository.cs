using JetBrains.Annotations;
using RossyntBackend.Models;

namespace RossyntBackend.Repositories {
    public interface IProjectRepository {
        void SetTree([NotNull] Tree tree);

        [Pure]
        [CanBeNull]
        Tree GetTree();

        void RemoveTree();
    }
}
