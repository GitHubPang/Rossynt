using JetBrains.Annotations;
using RoslynSyntaxTreeBackend.Models;

namespace RoslynSyntaxTreeBackend.Repositories {
    public interface IProjectRepository {
        void SetTree([NotNull] Tree tree);

        [Pure]
        [CanBeNull]
        Tree GetTree();

        void RemoveTree();
    }
}
