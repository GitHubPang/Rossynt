using System;

#nullable enable

namespace RoslynSyntaxTreeBackend {
    public sealed class ProjectRecord {
        public string ProjectId { get; }
        public Tree Tree { get; }

        // ******************************************************************************** //

        public ProjectRecord(string projectId, Tree tree) {
            ProjectId = projectId ?? throw new ArgumentNullException(nameof(projectId));
            Tree = tree ?? throw new ArgumentNullException(nameof(tree));
        }
    }
}
