using JetBrains.Annotations;

namespace RoslynSyntaxTreeBackend {
    public interface IProjectRepository {
        void SetProjectRecord([NotNull] ProjectRecord projectRecord);

        [CanBeNull]
        ProjectRecord GetProjectRecord([NotNull] string projectId);

        void RemoveProjectRecord([NotNull] string projectId);
    }
}
