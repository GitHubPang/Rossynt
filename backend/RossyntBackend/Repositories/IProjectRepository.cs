using JetBrains.Annotations;
using RossyntBackend.Models;

namespace RossyntBackend.Repositories {
    public interface IProjectRepository {
        public void SetTree([NotNull] Tree tree);

        [Pure]
        [CanBeNull]
        public Tree GetTree();

        public void RemoveTree();
    }
}
