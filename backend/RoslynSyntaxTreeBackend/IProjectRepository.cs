#nullable enable

namespace RoslynSyntaxTreeBackend {
    public interface IProjectRepository {
        public void SetProjectRecord(ProjectRecord projectRecord);
        public ProjectRecord? GetProjectRecord(string projectId);
        public void RemoveProjectRecord(string projectId);
    }
}
