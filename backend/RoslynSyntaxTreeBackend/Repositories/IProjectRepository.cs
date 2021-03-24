﻿using JetBrains.Annotations;
using RoslynSyntaxTreeBackend.Models;

namespace RoslynSyntaxTreeBackend.Repositories {
    public interface IProjectRepository {
        void SetProjectRecord([NotNull] ProjectRecord projectRecord);

        [CanBeNull]
        [Pure]
        ProjectRecord GetProjectRecord([NotNull] string projectId);

        void RemoveProjectRecord([NotNull] string projectId);
    }
}
