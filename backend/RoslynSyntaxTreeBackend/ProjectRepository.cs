using System;
using System.Collections.Concurrent;
using JetBrains.Annotations;

namespace RoslynSyntaxTreeBackend {
    public sealed class ProjectRepository : IProjectRepository {
        [NotNull] private readonly ConcurrentDictionary<string, ProjectRecord> _projectRecords = new ConcurrentDictionary<string, ProjectRecord>();

        // ******************************************************************************** //

        public void SetProjectRecord(ProjectRecord projectRecord) {
            if (projectRecord == null) throw new ArgumentNullException(nameof(projectRecord));

            _projectRecords[projectRecord.ProjectId] = projectRecord;
        }

        public ProjectRecord GetProjectRecord(string projectId) {
            if (projectId == null) throw new ArgumentNullException(nameof(projectId));

            return _projectRecords.TryGetValue(projectId, out var projectRecord) ? projectRecord : null;
        }

        public void RemoveProjectRecord(string projectId) {
            if (projectId == null) throw new ArgumentNullException(nameof(projectId));

            _projectRecords.TryRemove(projectId, out _);
        }
    }
}
