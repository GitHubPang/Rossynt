using System;
using System.Collections.Concurrent;
using JetBrains.Annotations;
using RoslynSyntaxTreeBackend.Models;

namespace RoslynSyntaxTreeBackend.Repositories {
    // ReSharper disable AnnotationRedundancyInHierarchy
    public sealed class ProjectRepository : IProjectRepository {
        [NotNull] private readonly ConcurrentDictionary<string, ProjectRecord> _projectRecords = new ConcurrentDictionary<string, ProjectRecord>();

        // ******************************************************************************** //

        public void SetProjectRecord([NotNull] ProjectRecord projectRecord) {
            if (projectRecord == null) throw new ArgumentNullException(nameof(projectRecord));

            _projectRecords[projectRecord.ProjectId] = projectRecord;
        }

        [CanBeNull]
        [Pure]
        public ProjectRecord GetProjectRecord([NotNull] string projectId) {
            if (projectId == null) throw new ArgumentNullException(nameof(projectId));

            return _projectRecords.TryGetValue(projectId, out var projectRecord) ? projectRecord : null;
        }

        public void RemoveProjectRecord([NotNull] string projectId) {
            if (projectId == null) throw new ArgumentNullException(nameof(projectId));

            _projectRecords.TryRemove(projectId, out _);
        }
    }
    // ReSharper restore AnnotationRedundancyInHierarchy
}
